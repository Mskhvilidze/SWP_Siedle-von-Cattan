package de.uol.swp.server.communication.netty;

import com.google.inject.Inject;
import de.uol.swp.common.message.RequestMessage;
import de.uol.swp.server.communication.ServerHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This handler is called from netty when communications happens e.g. a new connection
 * is established or data is received
 *
 * @author Marco Grawunder
 * @see io.netty.channel.ChannelInboundHandler
 * @since 2019-11-20
 */
@SuppressWarnings("java:S1186")
@Sharable
public
class NettyServerHandler implements ChannelInboundHandler {

    private static final Logger LOG = LogManager.getLogger(NettyServerHandler.class);
    private final ServerHandler delegate;

    /**
     * Constructor
     *
     * @param delegate handler who handles all communication
     * @see de.uol.swp.server.communication.ServerHandler
     * @since 2019-11-20
     */
    @Inject
    public NettyServerHandler(ServerHandler delegate) {
        this.delegate = delegate;
    }

    @Override
    public void channelRegistered(ChannelHandlerContext channelHandlerContext) {

    }

    @Override
    public void channelUnregistered(ChannelHandlerContext channelHandlerContext) {

    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        delegate.newClientConnected(new NettyMessageContext(ctx));
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        delegate.clientDisconnected(new NettyMessageContext(ctx));
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        // Server ignores everything but IRequestMessages
        if (msg instanceof RequestMessage) {
            RequestMessage requestMessage = (RequestMessage) msg;
            requestMessage.setMessageContext(new NettyMessageContext(ctx));
            delegate.process(requestMessage);
        } else {
            LOG.error("Illegal Object read from channel. Ignored!");
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext channelHandlerContext) {

    }

    @Override
    public void userEventTriggered(ChannelHandlerContext channelHandlerContext, Object object) {

    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext channelHandlerContext) {

    }

    @Override
    public void handlerAdded(ChannelHandlerContext channelHandlerContext) {

    }

    @Override
    public void handlerRemoved(ChannelHandlerContext channelHandlerContext) {

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if (ctx.channel().isActive() || ctx.channel().isOpen()) {
            LOG.error("Exception caught: ", cause);
        } else {
            delegate.clientDisconnected(new NettyMessageContext(ctx));
        }
    }

}
