package de.uol.swp.server.communication.netty;

import de.uol.swp.common.message.MessageContext;
import de.uol.swp.common.message.ResponseMessage;
import de.uol.swp.common.message.ServerMessage;
import io.netty.channel.ChannelHandlerContext;

import java.util.Objects;

/**
 * This class is used to encapsulate a netty channel handler context
 *
 * @author Marco Grawunder
 * @see de.uol.swp.common.message.MessageContext
 * @since 2019-11-20
 */
@SuppressWarnings("java:S1948")
class NettyMessageContext implements MessageContext {

    private final ChannelHandlerContext ctx;

    /**
     * Constructor
     *
     * @param ctx the ChannelHandlerContext encapsulated by this
     * @since 2019-11-20
     */
    public NettyMessageContext(ChannelHandlerContext ctx) {
        this.ctx = ctx;
    }

    /**
     * Getter for the ChannelHandlerContext encapsulated by this class
     *
     * @return the ChannelHandlerContext
     * @see io.netty.channel.ChannelHandlerContext
     * @since 2019-11-20
     */
    public ChannelHandlerContext getCtx() {
        return ctx;
    }

    @Override
    public void writeAndFlush(ResponseMessage message) {
        ctx.writeAndFlush(message);
    }

    @Override
    public void writeAndFlush(ServerMessage message) {
        ctx.writeAndFlush(message);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        NettyMessageContext that = (NettyMessageContext) object;
        return Objects.equals(ctx, that.ctx);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ctx);
    }
}
