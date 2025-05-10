package infrastructure.net.web;

import infrastructure.net.PacketHandler;
import infrastructure.net.web.ui.Designer;
import infrastructure.net.web.ui.UI;
import infrastructure.net.web.ui.css.*;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.slf4j.Logger;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.ThreadFactory;

/**
 * A WebSocket-enabled HTTP server built on Netty.
 * <p>
 * The server handles both static file requests and WebSocket connections.
 * It supports registration of packet handlers via a static handler registry
 * and provides routing capabilities through an internal {@link Router} instance.
 * <p>
 * Key features:
 * <ul>
 *   <li>Configurable port binding and asynchronous server launch</li>
 *   <li>Thread-safe packet handler registry with ID uniqueness enforcement</li>
 *   <li>Netty-based channel pipeline setup including HTTP and WebSocket support</li>
 *   <li>Optional automatic browser launch upon server startup</li>
 * </ul>
 *
 * @author Albert Beaupre
 * @version 1.0
 * @since September 2024
 */
public final class WebServer implements Runnable {

    /**
     * A global map of packet handlers indexed by unique packet IDs.
     */
    private static final HashMap<Integer, PacketHandler> PACKET_HANDLERS = new HashMap<>();

    /**
     * Logger instance for outputting server lifecycle messages and errors.
     */
    private static final Logger Log = org.slf4j.LoggerFactory.getLogger(WebServer.class);

    /**
     * The singleton router responsible for HTTP request routing.
     */
    private static final Router ROUTER = new Router();

    /**
     * The thread on which the server runs.
     */
    private static final Thread THREAD = new Thread(new WebServer(), "WebServer Thread");

    /**
     * The port the server binds to (default: 8080).
     */
    private static int PORT = 8080;

    /**
     * A flag indicating whether the system should automatically open the default web browser
     * after the web server has started.
     */
    private static boolean OPEN_BROWSER = true;

    /**
     * This constructor is private to prevent instantiation of the {@code WebServer}
     * class.
     */
    private WebServer() {
        // Inaccessible
    }

    /**
     * Starts the web server on the specified port and optionally opens the default browser.
     *
     * @param port        the port number to bind the server to
     * @param openBrowser a boolean flag indicating whether to automatically open the
     *                    default web browser pointing to the server's address
     */
    public static void start(final int port, boolean openBrowser) {
        WebServer.PORT = port;
        WebServer.OPEN_BROWSER = openBrowser;

        THREAD.start();
    }

    /**
     * Registers a {@link PacketHandler} for a specific packet ID.
     *
     * @param packetID the unique ID for incoming packet recognition
     * @param handler  the handler to process packets with this ID
     * @throws IllegalArgumentException if the packet ID is already registered
     * @throws NullPointerException     if the handler is {@code null}
     */
    public static void registerHandler(int packetID, PacketHandler handler) {
        if (PACKET_HANDLERS.containsKey(packetID))
            throw new IllegalArgumentException("Packet ID already registered: " + packetID);
        PACKET_HANDLERS.put(packetID, Objects.requireNonNull(handler, "PacketHandler cannot be null"));
    }

    /**
     * Retrieves the registered handler for the given packet ID.
     *
     * @param packetID the ID to look up
     * @return the corresponding {@link PacketHandler}, or {@code null} if not registered
     */
    public static PacketHandler getHandler(int packetID) {
        return PACKET_HANDLERS.get(packetID);
    }

    /**
     * Returns the router instance used to manage HTTP routing rules.
     *
     * @return the shared {@link Router} instance
     */
    public static Router getRouter() {
        return ROUTER;
    }

    /**
     * Launches the server from the command line on port 8080.
     *
     * @param args ignored command-line arguments
     * @throws Exception if server startup fails
     */
    public static void main(String[] args) throws Exception {
        WebServer.start(4040, true);

        WebServer.getRouter().addRoute(new Route() {
            @Override
            public void load(UI ui) {
                Designer.begin(ui)

                        .div()
                        .asParent()
                        .display(Display.FLEX)
                        .flex(Flex.AUTO)
                        .flexDirection(FlexDirection.COLUMN)
                        .alignItems(AlignItems.CENTER)
                        .justifyContent(JustifyContent.CENTER)
                        .alignContent(AlignContent.CENTER)
                        .gap("1em")
                        .width("100vw")
                        .height("100vh")

                        .textField("Username")
                        .maxWidth("300px")
                        .minWidth("150px")
                        .width("50%")

                        .password("Password")
                        .maxWidth("300px")
                        .minWidth("150px")
                        .width("50%")
                        .margin("0 0 3em 0")

                        .button("Login")
                        .onClick(e -> {
                            e.getComponent().getStyle()
                                    .width("50%")
                                    .maxWidth("50%")
                                    .minWidth("25%");
                        })
                        .minWidth("80px")
                        .maxWidth("150px")
                        .width("25%")

                        .label("Remember me?")
                        .checkbox();
            }

            @Override
            public String getPath() {
                return "/";
            }
        });
    }

    /**
     * Executes the server run loop: sets up the Netty pipeline and blocks until shutdown.
     * <p>
     * The pipeline supports:
     * <ul>
     *   <li>HTTP codec (request/response decoding and encoding)</li>
     *   <li>Object aggregation (combining HTTP content parts)</li>
     *   <li>Content compression (gzip/deflate)</li>
     *   <li>WebSocket upgrade handling</li>
     *   <li>Static file serving</li>
     * </ul>
     * Once started, it logs the server address and optionally opens the default system browser.
     */
    @Override
    public void run() {
        ThreadFactory bossFactory = new DefaultThreadFactory("WebServer-Boss");
        ThreadFactory workerFactory = new DefaultThreadFactory("WebServer-Worker");

        EventLoopGroup bossGroup = new NioEventLoopGroup(1, bossFactory);
        EventLoopGroup workerGroup = new NioEventLoopGroup(0, workerFactory);

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ch.pipeline().addLast(new HttpServerCodec());
                            ch.pipeline().addLast(new HttpObjectAggregator(65536));
                            ch.pipeline().addLast(new HttpContentCompressor());
                            ch.pipeline().addLast(new WebSocketServerProtocolHandler("/ws"));
                            ch.pipeline().addLast(new WebSocketHandler());
                            ch.pipeline().addLast(new StaticFileHandler());
                        }
                    });

            ChannelFuture future = bootstrap.bind(PORT).sync();
            Log.info("Server started on port {}", PORT);

            future.channel().eventLoop().execute(() -> {
                final String url = "http://localhost:" + PORT;
                Log.info("Web server running at {}", url);

                if (OPEN_BROWSER && Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                    try {
                        Desktop.getDesktop().browse(new URI(url));
                    } catch (IOException | URISyntaxException e) {
                        Log.error("Failed to open browser", e);
                    }
                }
            });

            future.channel().closeFuture().sync();
        } catch (Exception e) {
            throw new RuntimeException("Error on WebServer: " + e.getMessage(), e);
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            Log.info("Server successfully shutdown");
        }
    }
}
