package de.uol.swp.server;

import com.google.inject.Guice;
import com.google.inject.Injector;
import de.uol.swp.server.chat.ChatService;
import de.uol.swp.server.communication.ServerHandler;
import de.uol.swp.server.communication.netty.NettyServerHandler;
import de.uol.swp.server.communication.netty.Server;
import de.uol.swp.server.di.ServerModule;
import de.uol.swp.server.game.InventoryService;
import de.uol.swp.server.game.session.GameSessionService;
import de.uol.swp.server.lobby.LobbyService;
import de.uol.swp.server.usermanagement.AuthenticationService;
import de.uol.swp.server.usermanagement.UserService;
import io.netty.channel.ChannelHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class handles the startup of the server.
 */
@SuppressWarnings({"PMD.AvoidCatchingGenericException"})
public class ServerApp {

    private static final Logger LOG = LogManager.getLogger(ServerApp.class);
    private static String dataBaseUrl = "jdbc:mysql://duemmer.informatik.uni-oldenburg.de:50090/catan?useLegacyDatetimeCode=false&serverTimezone=UTC";

    /**
     * Main Method
     * <p>
     * This method handles the creation of the server components and the start of
     * the server
     *
     * @param args 1. server port, 2. database URL (optional)
     *             50092 "jdbc:mysql://duemmer.informatik.uni-oldenburg.de:50090/catan?useLegacyDatetimeCode=false&serverTimezone=UTC"
     * @since 2017-03-17
     */
    public static void main(String[] args) throws Exception {
        int port = -1;
        if (args.length == 1) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (Exception e) {
                LOG.info("Incorrect Argument, using default port: {}", e.getMessage());
            }
        } else if (args.length == 2) {
            try {
                port = Integer.parseInt(args[0]);
                dataBaseUrl = args[1];
            } catch (Exception e) {
                LOG.info("Incorrect Argument, using default port and default database: {}", e.getMessage());
            }
        }
        if (port < 0) {
            port = 50_092;
        }
        LOG.info("Starting Server on port {}", port);

        if ("jdbc:mysql://localhost:50090/catan?useLegacyDatetimeCode=false&serverTimezone=UTC".equals(dataBaseUrl)) {
            LOG.info("Server uses local database");
        } else if ("jdbc:mysql://duemmer.informatik.uni-oldenburg.de:50090/catan?useLegacyDatetimeCode=false&serverTimezone=UTC".equals(dataBaseUrl)) {
            LOG.info("Server uses external database");
        } else {
            LOG.warn("Server uses unknown database: {}", dataBaseUrl);
        }

        // create components
        Injector injector = Guice.createInjector(new ServerModule());
        createServices(injector);
        ServerHandler serverHandler = injector.getInstance(ServerHandler.class);
        ChannelHandler channelHandler = new NettyServerHandler(serverHandler);
        Server server = new Server(channelHandler);
        server.start(port);
    }

    /**
     * Helper method to create the services the server uses and for the time being
     * the test users
     *
     * @param injector the google guice injector used for dependency injection
     * @since 2019-09-18
     */
    private static void createServices(Injector injector) {
        // Remark: As these services are not referenced by any other class
        // we will need to create instances here (and inject dependencies)
        injector.getInstance(UserService.class);
        injector.getInstance(AuthenticationService.class);
        injector.getInstance(LobbyService.class);
        injector.getInstance(GameSessionService.class);
        injector.getInstance(ChatService.class);
        injector.getInstance(InventoryService.class);
    }

    /**
     * Method that gets the dataBaseUrl which is specified in the ServerApp.
     *
     * @return the dataBaseUrl for the database connection
     * @since 2021-06-27
     */
    public static String getDataBaseUrl() {
        return dataBaseUrl;
    }
}
