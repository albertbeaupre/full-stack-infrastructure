package infrastructure.net.web;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.google.javascript.jscomp.*;
import com.google.javascript.jscomp.jarjar.com.google.common.collect.ImmutableList;
import com.googlecode.htmlcompressor.compressor.HtmlCompressor;
import com.helger.css.ECSSVersion;
import com.helger.css.decl.CascadingStyleSheet;
import com.helger.css.reader.CSSReader;
import com.helger.css.reader.CSSReaderSettings;
import com.helger.css.writer.CSSWriter;
import com.helger.css.writer.CSSWriterSettings;
import infrastructure.io.Loader;

import javax.annotation.concurrent.Immutable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Enumeration of supported web file types, each associated with:
 * <ul>
 *   <li>a file extension (e.g. ".js") for lookup;</li>
 *   <li>a MIME content type for HTTP responses;</li>
 *   <li>a {@link Loader} function to read (and optionally minify) the file’s bytes.</li>
 * </ul>
 * <p>
 * Provides utilities to:
 * <ul>
 *   <li>Determine the {@code WebFileType} from a filename;</li>
 *   <li>Load and transform file content into a byte array suitable for serving over HTTP.</li>
 * </ul>
 *
 * @author Albert
 * @version 1.0
 * @since May 2, 2025
 */
public enum WebFileType {

    /**
     * JavaScript files. Reads and minifies by stripping comments and collapsing whitespace.
     */
    JAVASCRIPT(".js", "application/javascript", WebFileType::loadAndMinifyJs),

    /**
     * HTML files. Reads and minifies by stripping HTML comments and collapsing whitespace.
     */
    HTML(".html", "text/html", WebFileType::loadAndMinifyHtml),

    /**
     * CSS files. Reads and minifies by stripping CSS comments and collapsing whitespace.
     */
    CSS(".css", "text/css", WebFileType::loadAndMinifyCss),

    /**
     * JSON files. Reads and minifies by stripping comments and collapsing whitespace.
     */
    JSON(".json", "application/json", WebFileType::loadAndMinifyJson),

    /**
     * SVG image files. Reads raw bytes without transformation.
     */
    SVG(".svg", "image/svg+xml", Files::readAllBytes),

    /**
     * PNG image files. Reads raw bytes without transformation.
     */
    PNG(".png", "image/png", Files::readAllBytes),

    /**
     * JPG image files. Reads raw bytes without transformation.
     */
    JPG(".jpg", "image/jpeg", Files::readAllBytes),

    /**
     * JPEG image files. Reads raw bytes without transformation.
     */
    JPEG(".jpeg", "image/jpeg", Files::readAllBytes),

    /**
     * Fallback for any other file types. Reads raw bytes without transformation.
     */
    OTHER("", "application/octet-stream", Files::readAllBytes);

    /**
     * File extension used to identify this type (e.g. ".css").
     */
    private final String extension;

    /**
     * HTTP Content-Type header value for this file type.
     */
    private final String contentType;

    /**
     * Function to load (and optionally transform) file data.
     */
    private final Loader loader;

    /**
     * Constructs an enum constant with the given metadata.
     *
     * @param extension   the file extension (including the leading dot), or empty for fallback
     * @param contentType the MIME type to set in HTTP responses
     * @param loader      function to read and (optionally) minify the file’s bytes
     */
    WebFileType(String extension, String contentType, Loader loader) {
        this.extension = extension;
        this.contentType = contentType;
        this.loader = loader;
    }

    /**
     * Returns the file extension associated with this type.
     *
     * @return the extension string (e.g. ".js"), or empty for OTHER
     */
    public String getExtension() {
        return extension;
    }

    /**
     * Returns the HTTP Content-Type header value for this type.
     *
     * @return the MIME type string (e.g. "text/css")
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * Loads the file bytes at the given {@code path}, applying any
     * minification or transformation defined by this type’s loader.
     *
     * @param path the filesystem path to read
     * @return byte array containing the file’s content (possibly minified)
     * @throws IOException if an I/O error occurs during loading
     */
    public byte[] load(Path path) throws IOException {
        return loader.load(path);
    }

    /**
     * Infers the {@code WebFileType} from a filename by matching its
     * lowercase extension against known types. Defaults to {@link #OTHER}
     * if no match is found.
     *
     * @param filename the name of the file (may include directories)
     * @return the matching {@code WebFileType}, or OTHER if none match
     */
    public static WebFileType fromFilename(String filename) {
        String lower = filename.toLowerCase();
        for (WebFileType type : values()) {
            if (!type.extension.isEmpty() && lower.endsWith(type.extension)) {
                return type;
            }
        }
        return OTHER;
    }

    /**
     * Reads a JavaScript file, compilers it into the most minimal format.
     */
    private static byte[] loadAndMinifyJs(Path path) throws IOException {
        String code = Files.readString(path, StandardCharsets.UTF_8);
        Compiler compiler = new Compiler();
        CompilerOptions options = new CompilerOptions();
        CompilationLevel.SIMPLE_OPTIMIZATIONS.setOptionsForCompilationLevel(options);

        SourceFile input = SourceFile.fromCode(path.getFileName().toString(), code);
        Result result = compiler.compile(ImmutableList.of(), ImmutableList.of(input), options);
        if (result.success) {
            return compiler.toSource().getBytes(StandardCharsets.UTF_8);
        } else {
            return code.replaceAll("[\\r\\n\\t]+", "").getBytes();
        }
    }

    /**
     * Reads an HTML file, strips HTML comments, multi spaces, quotes, and compresses inner javascript or css.
     */
    private static byte[] loadAndMinifyHtml(Path path) throws IOException {
        HtmlCompressor compressor = new HtmlCompressor();
        compressor.setRemoveComments(true);
        compressor.setRemoveMultiSpaces(true);
        compressor.setCompressCss(true);
        String html = Files.readString(path);
        try {
            return compressor.compress(html).getBytes(StandardCharsets.UTF_8);
        } catch (Exception e) {
            return html.replaceAll("[\\r\\n\\t]+", "").getBytes();
        }
    }

    /**
     * Reads a CSS file, strips CSS comments, then collapses whitespace.
     */
    private static byte[] loadAndMinifyCss(Path path) throws IOException {
        CascadingStyleSheet css = CSSReader.readFromFile(path.toFile(), new CSSReaderSettings());
        if (css != null) {
            CSSWriterSettings settings = new CSSWriterSettings(ECSSVersion.CSS30, true).setRemoveUnnecessaryCode(true);
            CSSWriter writer = new CSSWriter(settings);
            return writer.getCSSAsString(css).getBytes();
        } else {
            return Files.readAllBytes(path);
        }
    }

    /**
     * Reads a JSON file, strips both block and line comments, then collapses whitespace.
     */
    private static byte[] loadAndMinifyJson(Path path) throws IOException {
        JSONObject object = com.alibaba.fastjson.JSON.parseObject(Files.readAllBytes(path));
        return com.alibaba.fastjson.JSON.toJSONString(object, SerializerFeature.EMPTY).getBytes();
    }
}