package infrastructure.web;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Static file handler that serves resources from the /web directory.
 * If the requested path does not correspond to a file, it falls back to index.html
 * to support frontend routes (like /about or /contact).
 */
public class StaticFileHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        String uri = request.uri();
        if (uri.equals("/")) uri = "/index.html";

        Path staticRoot = Paths.get("src/main/resources/web");
        Path requestedPath = staticRoot.resolve(uri.substring(1)).normalize();

        // If file does not exist or isn't a file, fallback to index.html for client-side routing
        if (!Files.exists(requestedPath) || !Files.isRegularFile(requestedPath)) {
            requestedPath = staticRoot.resolve("index.html");
        }

        byte[] content = Files.readAllBytes(requestedPath);
        String contentType = getContentType(requestedPath.getFileName().toString());

        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.OK,
                Unpooled.wrappedBuffer(content)
        );

        response.headers().set(HttpHeaderNames.CONTENT_TYPE, contentType);
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, content.length);
        ctx.writeAndFlush(response);
    }

    private String getContentType(String filename) {
        if (filename.endsWith(".js")) return "application/javascript";
        if (filename.endsWith(".css")) return "text/css";
        if (filename.endsWith(".html")) return "text/html";
        if (filename.endsWith(".json")) return "application/json";
        if (filename.endsWith(".svg")) return "image/svg+xml";
        if (filename.endsWith(".png")) return "image/png";
        if (filename.endsWith(".jpg") || filename.endsWith(".jpeg")) return "image/jpeg";
        return "application/octet-stream";
    }
}