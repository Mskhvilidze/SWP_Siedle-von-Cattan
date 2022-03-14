package de.uol.swp.server.di;

import com.google.common.eventbus.EventBus;
import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import de.uol.swp.server.game.session.GameSessionFactory;
import de.uol.swp.server.usermanagement.store.DataBaseUserStore;
import de.uol.swp.server.usermanagement.store.UserStore;

/**
 * Module that provides classes needed by the Server.
 *
 * @author Marco Grawunder
 * @since 2019-09-18
 */
@SuppressWarnings("UnstableApiUsage")
public class ServerModule extends AbstractModule {

    private final EventBus bus = new EventBus();
    private final UserStore store = new DataBaseUserStore(false);

    @Override
    protected void configure() {
        install(new FactoryModuleBuilder().build(GameSessionFactory.class));
        bind(UserStore.class).toInstance(store);
        bind(EventBus.class).toInstance(bus);
    }
}
