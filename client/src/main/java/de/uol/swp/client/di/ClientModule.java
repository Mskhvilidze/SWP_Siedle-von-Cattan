package de.uol.swp.client.di;

import com.google.common.eventbus.EventBus;
import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import de.uol.swp.client.ClientConnection;
import de.uol.swp.client.ClientConnectionFactory;
import de.uol.swp.client.SceneManager;
import de.uol.swp.client.SceneManagerFactory;
import de.uol.swp.client.user.ClientUserService;
import de.uol.swp.client.user.UserService;
import javafx.fxml.FXMLLoader;

/**
 * Module that provides classes needed by the client.
 *
 * @author Marco Grawunder
 * @since 2019-09-18
 */
@SuppressWarnings("UnstableApiUsage")
public class ClientModule extends AbstractModule {
    private final EventBus eventBus = new EventBus();

    @Override
    protected void configure() {
        install(new FactoryModuleBuilder().implement(SceneManager.class, SceneManager.class).
                build(SceneManagerFactory.class));
        install(new FactoryModuleBuilder().implement(ClientConnection.class, ClientConnection.class).
                build(ClientConnectionFactory.class));
        bind(FXMLLoader.class).toProvider(FXMLLoaderProvider.class);
        bind(EventBus.class).toInstance(eventBus);
        bind(ClientUserService.class).to(UserService.class);
    }
}
