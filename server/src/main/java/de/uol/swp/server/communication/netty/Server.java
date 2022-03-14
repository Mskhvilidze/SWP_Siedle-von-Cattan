package de.uol.swp.server.communication.netty;

import de.uol.swp.common.MyObjectDecoder;
import de.uol.swp.common.MyObjectEncoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;

import java.net.InetSocketAddress;

/**
 * This class handles opening a port clients can connect to.
 *
 * @author Marco Grawunder
 * @since 2019-11-20
 */
@SuppressWarnings({"PMD.AccessorMethodGeneration"})
public class Server {

    private final ChannelHandler serverHandler;

    /**
     * Constructor
     * <p>
     * Creates a new Server Object
     *
     * @see io.netty.channel.ChannelHandler
     * @see de.uol.swp.server.communication.ServerHandler
     * @since 2019-11-20
     */
    public Server(ChannelHandler serverHandler) {
        this.serverHandler = serverHandler;
    }

    /**
     * Start a new server on given port
     *
     * @param port port number the server shall be reachable on
     * @throws Exception server failed to start e.g. because the port is already in use
     * @see InetSocketAddress
     * @since 2019-11-20
     */
    @SuppressWarnings("java:S112")
    public void start(int port) throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                    .localAddress(new InetSocketAddress(port)).childHandler(new ChannelInitializer<SocketChannel>() {

                @Override
                protected void initChannel(SocketChannel channel) {
                    // Encoder and decoder are both needed! Send and
                    // receive serializable objects
                    channel.pipeline().addLast(new MyObjectEncoder());
                    channel.pipeline().addLast(new MyObjectDecoder(ClassResolvers.cacheDisabled(null)));
                    // must be last in the pipeline else they will not
                    // get encoded/decoded objects but ByteBuf
                    channel.pipeline().addLast(serverHandler);
                }

            });
            // Just wait for server shutdown
            ChannelFuture future = bootstrap.bind().sync();
            future.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully().sync();
            workerGroup.shutdownGracefully().sync();
        }
    }


}
