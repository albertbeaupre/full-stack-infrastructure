package rsps.network;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

public class NetworkChannelInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel ch) {
        ChannelPipeline pipeline = ch.pipeline();

        //pipeline.addLast("decoder", new RawDecoder());
        // Add server-specific handlers to the channel pipeline
        //pipeline.addLast("handler", new ServerHandler());
    }
}
