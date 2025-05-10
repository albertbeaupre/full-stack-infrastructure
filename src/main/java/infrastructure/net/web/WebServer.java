package infrastructure.net.web;

import infrastructure.net.PacketHandler;
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
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Objects;

public class WebServer {

    private static final HashMap<Integer, PacketHandler> HANDLERS = new HashMap<>();
    private static final Logger Log = org.slf4j.LoggerFactory.getLogger(WebServer.class);
    private static final Router ROUTER = new Router();

    public static void start(int port) throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ch.pipeline().addLast(new HttpServerCodec());
                            ch.pipeline().addLast(new HttpObjectAggregator(65536));
                            ch.pipeline().addLast(new HttpContentCompressor()); // Enables gzip/deflate
                            ch.pipeline().addLast(new WebSocketServerProtocolHandler("/ws"));
                            ch.pipeline().addLast(new WebSocketHandler());
                            ch.pipeline().addLast(new StaticFileHandler());
                        }
                    });

            ChannelFuture future = bootstrap.bind(port).sync();
            Log.info("Server started on port {}", port);
            future.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();

            Log.info("Server successfully shutdown");
        }
    }

    public static void registerHandler(int packetID, PacketHandler handler) {
        if (HANDLERS.containsKey(packetID))
            throw new IllegalArgumentException("Packet ID already registered: " + packetID);
        HANDLERS.put(packetID, Objects.requireNonNull(handler, "PacketHandler cannot be null"));
    }

    public static PacketHandler getHandler(int packetID) {
        return HANDLERS.get(packetID);
    }

    public static Router getRouter() {
        return ROUTER;
    }

    public static void main(String[] args) throws Exception {
        WebServer.start(8080);
    }
}