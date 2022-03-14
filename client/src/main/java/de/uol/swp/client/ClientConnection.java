package de.uol.swp.client;

import com.google.common.eventbus.DeadEvent;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.uol.swp.common.MyObjectDecoder;
import de.uol.swp.common.message.*;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectEncoder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * The ClientConnection Connection class
 * <p>
 * This Class manages connecting to a server, disconnecting  from the server and
 * handling of incoming and outgoing messages.
 *
 * @author Marco Grawunder
 * @since 2017-03-17
 */
@SuppressWarnings({"UnstableApiUsage", "PMD.ConstructorCallsOverridableMethod"})
public class ClientConnection {

    private static final Logger LOG = LogManager.getLogger(ClientConnection.class);

    private final String host;
    private final int port;
    private final List<ConnectionListener> connectionListener = new CopyOnWriteArrayList<>();
    private EventLoopGroup group;
    private EventBus eventBus;
    private Channel channel;

    /**
     * Creates a new connection to a specific port on the given host
     *
     * @param host the server name or IP to connect to
     * @param port the server port to connect to
     * @since 2017-03-17
     */
    @Inject
    public ClientConnection(@Assisted String host, @Assisted int port, EventBus eventBus) {
        this.host = host;
        this.port = port;
        setEventBus(eventBus);
    }

    /**
     * Sets the EventBus for the object
     * <p>
     * Sets the EventBus for the object and registers the object to it.
     *
     * @param eventBus the new EventBus to set
     * @implNote If the object already has an EventBus it is replaced but not unregistered
     * @since 2019-09-18
     */
    public void setEventBus(EventBus eventBus) {
        this.eventBus = eventBus;
        eventBus.register(this);
    }

    /**
     * The netty init method
     * <p>
     * The example method on how to initialize a connection to a server via netty.
     * Inside the ChannelInitializer multiple settings are made with the {@code
     * pipeline.addLast()} method. Things usually added are encoders, decoders and
     * the ChannelHandler.
     *
     * @throws Exception If the connection failed
     * @implNote If no ChannelHandler is added, communication will not be possible
     * @since 2017-03-17
     */
    @SuppressWarnings("java:S112")
    public void start() throws Exception {
        group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group).channel(NioSocketChannel.class).remoteAddress(new InetSocketAddress(host, port))
                    .handler(new ChannelInitializer<SocketChannel>() {

                        @Override
                        protected void initChannel(SocketChannel channel) {
                            // Add both Encoder and Decoder to send and receive serializable objects
                            channel.pipeline().addLast(new ObjectEncoder());
                            channel.pipeline().addLast(new MyObjectDecoder(ClassResolvers.cacheDisabled(null)));
                            // Add a client handler
                            channel.pipeline().addLast(new ClientHandler(ClientConnection.this));
                        }
                    });
            ChannelFuture future = bootstrap.connect().sync();
            future.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully().sync();
        }
    }

    /**
     * Disconnects the client from the server
     * <p>
     * Disconnects the client from the server and prints the stack trace if an
     * InterruptedException is thrown.
     *
     * @since 2017-03-17
     */
    public void close() {
        try {
            group.shutdownGracefully().sync();
        } catch (InterruptedException e) {
            LOG.error(e.getStackTrace());
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Calls the ConnectionEstablished method of every ConnectionListener added
     * to this.
     *
     * @param channel the netty channel the new Connection is established on
     * @see de.uol.swp.client.ConnectionListener
     * @since 2017-03-17
     */
    protected void fireConnectionEstablished(Channel channel) {
        for (ConnectionListener listener : connectionListener) {
            listener.connectionEstablished(channel);
        }
        this.channel = channel;
    }

    /**
     * Add a new ConnectionListener to the ConnectionListener Array of this Object
     *
     * @param listener the ConnectionListener to add to the Array
     * @see de.uol.swp.client.ConnectionListener
     * @since 2017-03-17
     */
    public void addConnectionListener(ConnectionListener listener) {
        this.connectionListener.add(listener);
    }

    /**
     * Processes the incoming messages
     * <p>
     * This method posts the message it gets on the EventBus if it is a ServerMessage
     * or a ResponseMessage and writes "Received message. Post on event bus " and
     * the Message to the LOG if the LOG-Level is set to DEBUG or higher.
     * If it is a different kind of Message, it gets discarded and with LOG-Level
     * set to WARN or higher "Can only process ServerMessage and ResponseMessage.
     * Received " and the message are written to the LOG.
     *
     * @param inMessage the incoming messages read by the ClientHandler
     * @see de.uol.swp.client.ClientHandler
     * @since 2017-03-17
     */
    public void receivedMessage(Message inMessage) {
        if (inMessage instanceof ServerMessage || inMessage instanceof ResponseMessage) {
            LOG.debug("Received message. Post on event bus {}", inMessage);
            eventBus.post(inMessage);
        } else {
            LOG.warn("Can only process ServerMessage and ResponseMessage. Received {}", inMessage);
        }
    }

    /**
     * Handles RequestMessages detected on the EventBus
     * <p>
     * If the client is connected to the server and the channel of this object
     * is set the RequestMessage given to this method is send to the server.
     * Otherwise "Some tries to send a message, but server is not connected" is
     * written to the LOG if the LOG-Level is set to WARN or higher.
     *
     * @param message the RequestMessage object to  send to the server
     * @since 2019-08-29
     */
    @Subscribe
    public void onRequestMessage(RequestMessage message) {
        if (channel != null) {
            channel.writeAndFlush(message);
        } else {
            LOG.warn("Some tries to send a message, but server is not connected");
        }
    }

    /**
     * Handles ExceptionMessages found on the EventBus
     * <p>
     * If an ExceptionMessage object is detected on the EventBus, this method is called.
     * It calls the exceptionOccurred method of every ConnectionListener in the
     * ConnectionListener array.
     *
     * @param message the ExceptionMessage object found on the EventBus
     * @since 2017-03-17
     */
    @Subscribe
    public void onExceptionMessage(ExceptionMessage message) {
        for (ConnectionListener l : connectionListener) {
            l.exceptionOccurred(message.getException());
        }
    }

    /**
     * Handles errors produced by the EventBus
     * <p>
     * If an DeadEvent object is detected on the EventBus, this method is called.
     * It writes "DeadEvent detected " and the error message of the detected DeadEvent
     * object to the log, if the loglevel is set to WARN or higher.
     *
     * @param deadEvent the DeadEvent object found on the EventBus
     * @since 2017-03-17
     */
    @Subscribe
    public void onDeadEvent(DeadEvent deadEvent) {
        LOG.warn("DeadEvent detected {}", deadEvent);
    }

    /**
     * Handles the distribution of throwable messages
     * <p>
     * This method distributes throwable messages to the ConnectionListeners.
     * It calls the exceptionOccurred method of every ConnectionListener in the
     * ConnectionListener array passing them the message.
     *
     * @param message the ExceptionMessage object found on the EventBus
     * @see de.uol.swp.client.ClientHandler
     * @since 2017-03-17
     */
    public void process(Throwable message) {
        for (ConnectionListener l : connectionListener) {
            l.exceptionOccurred(message.getMessage());
        }
    }


}
