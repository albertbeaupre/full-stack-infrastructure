package rsps.network;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.DefaultEventLoop;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

/**
 * The {@code Server} class is responsible for starting and running a network server using the Netty framework.
 * It implements the {@link Runnable} interface to allow its operation in a separate thread.
 * <p>
 * This class configures the server using Netty's {@link ServerBootstrap}, which is used to bind to a specific port,
 * manage server channels, and configure options for client connections. The server utilizes virtual threads
 * via the {@link ExecutorService} for handling tasks in a highly efficient manner.
 * <p>
 * The server listens on port 7777, logs relevant events using Java's built-in {@link Logger}, and gracefully shuts down
 * when necessary. It also ensures proper handling of uncaught exceptions within the server thread.
 *
 * @author Albert Beaupre
 */
public final class Server implements Runnable {

    // Logger for logging server-related information and warnings
    private static final Logger Log = Logger.getLogger(Server.class.getName());

    /**
     * Starts the server by creating a new thread and running the {@code Server} class inside it.
     * The thread is named "Server-Thread" for better identification, and an uncaught exception handler
     * is attached to log or print any exceptions that may occur during runtime.
     */
    public static void start() {
        // Create and start a new thread for the server
        Thread thread = new Thread(new Server(), "Server-Thread");
        thread.start();

        // Set an uncaught exception handler to log/print stack trace in case of an exception
        thread.setUncaughtExceptionHandler((_, e) -> e.printStackTrace());
    }

    /**
     * The core logic of the server, executed when the server thread is started. This method sets up the server
     * using Netty and configures important networking components such as event loops, channel handlers,
     * and connection options.
     */
    @Override
    public void run() {
        // ExecutorService that manages virtual threads for handling tasks
        ExecutorService service = Executors.newVirtualThreadPerTaskExecutor();

        // Create two EventLoopGroup objects (boss and worker) to handle connection requests and I/O events
        EventLoopGroup boss = new DefaultEventLoop(service);
        EventLoopGroup worker = new DefaultEventLoop(service);

        try {
            // Create and configure the Netty ServerBootstrap
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap
                    .group(boss, worker)  // Assign boss and worker groups
                    .channel(NioServerSocketChannel.class)  // Specify the use of NIO for socket channels
                    .handler(new LoggingHandler(LogLevel.INFO))  // Attach a logging handler to log server events
                    .childHandler(new NetworkChannelInitializer())  // Set the channel initializer for child channels
                    .option(ChannelOption.SO_BACKLOG, 200)  // Set backlog size for pending connections
                    .childOption(ChannelOption.TCP_NODELAY, true)  // Disable Nagle's algorithm for low-latency transmission
                    .childOption(ChannelOption.SO_KEEPALIVE, true);  // Enable TCP keep-alive for long-lived connections

            // Bind the server to port 7777 and wait until the binding is complete
            ChannelFuture future = bootstrap.bind(7777).sync();
            Log.info("Network server bound to port 7777");

            // Wait until the server's channel is closed, which means the server is shutting down
            future.channel().closeFuture().sync();
        } catch (Exception e) {
            // Log any exception that occurs during server setup or runtime
            Log.warning(String.format("Error loading server: %s", e.getMessage()));
        } finally {
            // Shutdown the boss and worker EventLoopGroups gracefully to free resources
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }
}