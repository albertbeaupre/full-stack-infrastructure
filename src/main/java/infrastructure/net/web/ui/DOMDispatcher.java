package infrastructure.net.web.ui;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
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
     * Queues the updates from another {@code DOMDispatcher} into this dispatcher and clears
     * the update queue of the provided dispatcher. This allows for merging update queues
     * from multiple dispatchers into a single dispatcher.
     *
     * @param dispatcher the {@code DOMDispatcher} whose updates should be merged into this dispatcher
     * @return this {@code DOMDispatcher} instance for method chaining
     */
    public DOMDispatcher queue(DOMDispatcher dispatcher) {
        updateQueue.addAll(dispatcher.updateQueue);
        dispatcher.updateQueue.clear();
        return this;
    }

    /**
     * Flushes the update queue to a single client channel over WebSocket.
     *
     * @param channel the channel to send updates to
     */
    public void flush(Channel channel) {
        if (updateQueue.isEmpty() || channel == null || !channel.isActive()) return;

        ByteBuf header = Unpooled.directBuffer(3);
        header.writeByte(1); // opcode for DOM updates
        header.writeShort(updateQueue.size());

        CompositeByteBuf packet = Unpooled.compositeBuffer();
        packet.addComponent(true, header); // 'true' to increase writer index

        for (DOMUpdate update : updateQueue)
            packet.addComponent(true, update.encode());

        channel.writeAndFlush(new BinaryWebSocketFrame(packet));
        updateQueue.clear();
    }

}