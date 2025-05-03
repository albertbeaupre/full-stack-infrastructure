package infrastructure.net.web.ui;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;

import java.util.ArrayList;
import java.util.List;

/**
 * The DOMDispatcher is responsible for queuing DOM updates and flushing them to the client
 * over WebSocket as a single binary packet. This is used within a UIContext to manage efficient,
 * batched DOM synchronization.
 */
public class DOMDispatcher {

    private final List<DOMUpdate> updateQueue = new ArrayList<>();

    /**
     * Queues a new DOM update for the client.
     *
     * @param updates the DOM updates to enqueue
     * @return the dispatcher instance for chaining
     */
    public DOMDispatcher queue(DOMUpdate... updates) {
        updateQueue.addAll(List.of(updates));
        return this;
    }

    /**
     * Flushes the update queue to a single client channel over WebSocket.
     *
     * @param channel the channel to send updates to
     */
    public void flush(Channel channel) {
        if (updateQueue.isEmpty() || channel == null || !channel.isActive()) return;

        ByteBuf packet = Unpooled.buffer();
        packet.writeByte(1); // opcode for dom updates
        packet.writeShort(updateQueue.size());

        for (DOMUpdate update : updateQueue)
            packet.writeBytes(update.encode());

        channel.writeAndFlush(new BinaryWebSocketFrame(packet));
        updateQueue.clear();
    }
}