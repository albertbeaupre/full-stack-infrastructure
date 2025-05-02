package infrastructure.net.web;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Netty handler for serving static files from a predefined directory.
 * <p>
 * Caches file contents in memory keyed by request URI for efficient repeated access.
 * Resolves requested URIs to real file paths under {@link #STATIC_ROOT}, falls back
 * to {@link #DEFAULT_FILE} when no matching resource is found, and responds with
 * the appropriate MIME type determined by {@link WebFileType}.
 * <p>
 * Threadsafety:
 * <ul>
 *   <li>{@link #CONTENT_CACHE} is a shared mutable map; concurrent access may require
 *       external synchronization or replacement with a concurrent map in high-throughput scenarios.</li>
 * </ul>
 *
 * @author Albert
 * @version 1.0
 * @since May 2, 2025
 */
public class StaticFileHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    /**
     * In-memory cache mapping request URIs to loaded file bytes.
     * Key: the raw request URI string (including query parameters, if any).
     * Value: the byte array returned by the loader for that URI.
     */
    private static final ConcurrentHashMap<String, byte[]> CONTENT_CACHE = new ConcurrentHashMap<>();

    /**
     * The base directory on the filesystem from which static resources are served.
     * Paths are resolved against this root.
     */
    private static final Path STATIC_ROOT = Paths.get("src/main/resources/web");

    /**
     * Default file to serve when the requested resource is not found
     * (e.g., for directory roots or missing pages).
     */
    private static final String DEFAULT_FILE = "index.html";

    /**
     * Handles incoming HTTP GET requests for FullHttpRequest messages.
     * <p>
     * Resolves the requested URI to a filesystem path, determines the file type,
     * loads (and caches) the content, and writes an HTTP response with the proper
     * Content-Type and Content-Length headers.
     *
     * @param ctx     the Netty channel handler context
     * @param request the inbound HTTP request
     * @throws Exception on I/O or handler errors
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        Path requestedPath = resolveRequestedPath(request.uri());
        String filename = requestedPath.getFileName().toString();
        WebFileType fileType = WebFileType.fromFilename(filename);

        byte[] content = loadContent(request.uri(), requestedPath, fileType);

        sendResponse(ctx, content, fileType);
    }

    /**
     * Resolves a requested URI to an actual file path under {@link #STATIC_ROOT}.
     * <p>
     * Normalizes the URI (removing leading slash or mapping "/" to the default file),
     * resolves against the static root, and checks existence. Falls back to DEFAULT_FILE
     * if the target file does not exist or is not a regular file.
     *
     * @param uri the raw request URI (e.g., "/css/style.css" or "/")
     * @return the absolute path of the resource to serve
     */
    private Path resolveRequestedPath(String uri) {
        String normalized = normalizeUri(uri);
        Path resolved = STATIC_ROOT.resolve(normalized).normalize();
        if (Files.exists(resolved) && Files.isRegularFile(resolved)) {
            return resolved;
        } else {
            // Serve index.html if not found
            return STATIC_ROOT.resolve(DEFAULT_FILE);
        }
    }

    /**
     * Converts a request URI into a relative file path string.
     * <p>
     * Maps the root URI "/" to {@link #DEFAULT_FILE}, otherwise strips the leading slash.
     *
     * @param uri the raw request URI
     * @return the relative path under STATIC_ROOT
     */
    private String normalizeUri(String uri) {
        return "/".equals(uri) ? DEFAULT_FILE : uri.substring(1);
    }

    /**
     * Loads the content for the given URI, using an in-memory cache.
     * <p>
     * If the URI is not yet in {@link #CONTENT_CACHE}, invokes the
     * appropriate {@link WebFileType#load(Path)} method to read (and
     * possibly minify) the file. On I/O error, returns an empty byte array.
     *
     * @param uri      the original request URI (cache key)
     * @param path     the resolved filesystem path to load
     * @param fileType the determined file type (for loader logic)
     * @return the file’s bytes, or an empty array on error
     */
    private byte[] loadContent(String uri, Path path, WebFileType fileType) {
        return CONTENT_CACHE.computeIfAbsent(uri, key -> {
            try {
                return fileType.load(path);
            } catch (IOException e) {
                return new byte[0];
            }
        });
    }

    /**
     * Writes an HTTP/1.1 200 OK response with the given content and headers.
     * <p>
     * Wraps the byte array into a Netty buffer, sets Content-Type and Content-Length,
     * then flushes the response downstream.
     *
     * @param ctx      the Netty channel handler context
     * @param content  the payload bytes to send in the response body
     * @param fileType the file type (provides MIME content type)
     */
    private void sendResponse(ChannelHandlerContext ctx, byte[] content, WebFileType fileType) {
        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.OK,
                Unpooled.wrappedBuffer(content)
        );
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, fileType.getContentType());
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, content.length);
        ctx.writeAndFlush(response);
    }

    /**
     * Forces a reload of the given URI’s file into the in-memory cache,
     * replacing any previously cached bytes.
     *
     * @param uri the request URI key (e.g. "/css/style.css")
     * @throws IOException if an I/O error occurs reading the file
     */
    public void reload(String uri) throws IOException {
        // Resolve the actual file path on disk
        Path path = resolveRequestedPath(uri);

        String filename = path.getFileName().toString();
        WebFileType fileType = WebFileType.fromFilename(filename);

        // Read fresh content (using the same loader logic)
        byte[] freshBytes = fileType.load(path);

        // Overwrite the cache entry
        CONTENT_CACHE.put(uri, freshBytes);
    }
}