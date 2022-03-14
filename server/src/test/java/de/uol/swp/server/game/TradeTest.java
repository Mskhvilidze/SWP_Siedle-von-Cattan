package de.uol.swp.server.game;

import com.google.common.eventbus.DeadEvent;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import de.uol.swp.common.game.TradeOffer;
import de.uol.swp.common.game.board.ResourceType;
import de.uol.swp.common.game.dto.UserDTO;
import de.uol.swp.common.game.message.trade.*;
import de.uol.swp.common.game.request.AbstractGameRequest;
import de.uol.swp.common.game.request.trade.*;
import de.uol.swp.common.lobby.GameLobby;
import de.uol.swp.common.message.ServerMessage;
import de.uol.swp.common.user.Session;
import de.uol.swp.common.user.User;
import de.uol.swp.server.exception.GameStateException;
import de.uol.swp.server.exception.TradeException;
import de.uol.swp.server.exception.TradeNotEnoughResourcesException;
import de.uol.swp.server.exception.TradeOfferException;
import de.uol.swp.server.game.session.GameSession;
import de.uol.swp.server.game.session.GameSessionFactory;
import de.uol.swp.server.game.session.GameSessionManagement;
import de.uol.swp.server.game.session.GameSessionService;
import de.uol.swp.server.game.state.TradeState;
import de.uol.swp.server.usermanagement.AuthenticationService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TradeTest {

    final GameSessionFactory gameSessionFactory = mock(GameSessionFactory.class);
    final User test1 = new UserDTO("test1", "test", "test@test.com");
    final EventBus bus = new EventBus();
    final GameSessionManagement gameSessionManagement = new GameSessionManagement(gameSessionFactory);
    final GameSessionService gameSessionService = new GameSessionService(gameSessionManagement, bus, mock(AuthenticationService.class));
    final InventoryService inventoryService = new InventoryService(bus, gameSessionService);
    final GameSession fullGameSession = createFullGameSession();
    final List<Object> events = new ArrayList<>();
    CountDownLatch lock = new CountDownLatch(1);
    Object event;

    /**
     * Handles DeadEvents detected on the EventBus
     * <p>
     * If a DeadEvent is detected the event variable of this class gets updated
     * to its event and its event is printed to the console output.
     *
     * @param event the DeadEvent detected on the EventBus
     * @since 2019-10-10
     */
    @Subscribe
    void handle(DeadEvent event) {
        this.event = event.getEvent();
        events.add(this.event);
        lock.countDown();
    }

    /**
     * Helper method run before each test case
     * <p>
     * This method resets the variable event to null and registers the object of
     * this class to the EventBus.
     */
    @BeforeEach
    void registerBus() {
        event = null;
        events.clear();
        bus.register(this);
    }

    /**
     * Helper method run after each test case
     * <p>
     * This method only unregisters the object of this class from the EventBus.
     */
    @AfterEach
    void deregisterBus() {
        bus.unregister(this);
        fullGameSession.stopGame();
    }


    private GameSession createFullGameSession() {
        User test2 = new UserDTO("test2", "test", "test@test.com");
        User test3 = new UserDTO("test3", "test", "test@test.com");
        User test4 = new UserDTO("test4", "test", "test@test.com");
        GameLobby gameLobby = new GameLobby("gameLobby", test1, false);
        gameLobby.joinUser(test2);
        gameLobby.joinUser(test3);
        gameLobby.joinUser(test4);
        when(gameSessionFactory.create(gameLobby)).thenReturn(new GameSession(gameLobby, gameSessionService, inventoryService, bus));
        GameSession gameSession = gameSessionService.createGameSession(gameLobby);
        gameSession.playerReady(test1.getUsername());
        gameSession.playerReady(test2.getUsername());
        gameSession.playerReady(test3.getUsername());
        gameSession.playerReady(test4.getUsername());
        return gameSession;
    }

    private <T extends ServerMessage> T eventsContains(Class<T> clazz) {
        for (Object event : events) {
            if (clazz.isInstance(event)) {
                return (T) event;
            }
        }
        return null;
    }

    private void insertSenderIntoRequest(AbstractTradeRequest request, String sender) {
        Session session = mock(Session.class);
        request.setSession(session);
        when(session.getUser()).thenReturn(new UserDTO(sender, "", ""));
    }

    @Test
    void turnEndRequest_ShouldCancelAllTrades() {

    }

    @BeforeEach
    void setupTradeState() {
        fullGameSession.setCurrentState(TradeState.INSTANCE);
    }

    @Test
    @DisplayName("Offering player not being in Session should throw a TradeException")
    void offeringPlayerNotInSession_ShouldThrowTradeException() {
        TradeOffer tradeOffer = new TradeOffer();
        tradeOffer.setOfferingPlayer("notInSession");
        AbstractTradeRequest request = new AbstractTradeRequest(fullGameSession.getGameSessionName(), tradeOffer) {
        };
        insertSenderIntoRequest(request, fullGameSession.getPlayer(0).getPlayerName());

        TradeException exception = assertThrows(TradeException.class, () -> fullGameSession.userInput(request));
        assertEquals("Offering Player notInSession not in game session", exception.getMessage());
    }

    @Test
    @DisplayName("Request not being an instance of AbstractTradeRequest should throw a InvalidGameStateException")
    void notAbstractTradeRequest_ShouldThrowInvalidGameStateException() {
        AbstractGameRequest abstractGameRequest = new AbstractGameRequest("") {
        };
        assertThrows(GameStateException.class, () -> fullGameSession.userInput(abstractGameRequest));
    }

    private void addTradeOfferToAllPlayers(GameSession gameSession, TradeOffer tradeOffer) {
        for (Player player : gameSession.getPlayers()) {
            player.addTradeOffer(tradeOffer);
        }
    }


    @Nested
    class NewTrade {
        private StartTradeRequest createStartTradeRequestFromPlayer(String offeringPlayer) {
            TradeOffer tradeOffer = new TradeOffer();
            tradeOffer.setOfferingPlayer(offeringPlayer);
            return new StartTradeRequest("", tradeOffer);
        }

        @Test
        void offeringPlayerNotCurrentTurn_ShouldThrowException() {
            Player notTurn = fullGameSession.getPlayer(1);
            StartTradeRequest request = createStartTradeRequestFromPlayer(notTurn.getPlayerName());
            var exception = assertThrows(TradeException.class, () -> fullGameSession.userInput(request));
            assertEquals("It is not this players turn:" + notTurn.getPlayerName(), exception.getMessage());
        }

        @Test
        void tradeOfferNotHaveResources_ShouldThrowException() {
            StartTradeRequest request = createStartTradeRequestFromPlayer(fullGameSession.getPlayer(0).getPlayerName());
            request.getTradeOffer().setWant(ResourceType.LUMBER, 0);
            var exception = assertThrows(TradeException.class, () -> fullGameSession.userInput(request));
            assertEquals("Trade is empty", exception.getMessage());
        }

        @ParameterizedTest
        @EnumSource(value = ResourceType.class)
        void offeringPlayerNotHaveResources_ShouldThrowException(ResourceType resource) {
            StartTradeRequest request = createStartTradeRequestFromPlayer(fullGameSession.getPlayer(0).getPlayerName());
            insertSenderIntoRequest(request, fullGameSession.getPlayer(0).getPlayerName());
            request.getTradeOffer().setOffer(resource, 1);

            var exception = assertThrows(TradeNotEnoughResourcesException.class, () -> fullGameSession.userInput(request));
            assertEquals("Offering Player does not have resources", exception.getMessage());
        }

        @Test
        @DisplayName("Offering player isn't the sender should throw a TradeException")
        void offeringPlayerNotSender_ShouldThrowTradeException() {
            String offeringPlayerName = fullGameSession.getPlayer(0).getPlayerName();
            String senderName = fullGameSession.getPlayer(1).getPlayerName();
            StartTradeRequest request = createStartTradeRequestFromPlayer(offeringPlayerName);
            insertSenderIntoRequest(request, senderName);
            TradeException exception = assertThrows(TradeException.class, () -> fullGameSession.userInput(request));
            assertEquals("Sender " + senderName + " Player " + offeringPlayerName + " false", exception.getMessage());
        }

        @Test
        @DisplayName("Offering player already having the TradeOffer should throw a TradeOfferException")
        void offeringPlayerAlreadyHaveTradeOffer_ShouldThrowTradeOfferException() {
            StartTradeRequest request = createStartTradeRequestFromPlayer(fullGameSession.getPlayer(0).getPlayerName());
            fullGameSession.getPlayer(0).addTradeOffer(request.getTradeOffer());

            TradeOfferException exception = assertThrows(TradeOfferException.class, () -> fullGameSession.userInput(request));
            assertEquals(fullGameSession.getPlayer(0).createDTO(), exception.getPlayer());
            assertTrue(exception.isAvailable());
        }

        @Test
        @DisplayName("Should Add TradeOffer to all players")
        void shouldAddTradeOfferToAllPlayer() throws GameStateException {
            StartTradeRequest request = createStartTradeRequestFromPlayer(fullGameSession.getPlayer(0).getPlayerName());
            insertSenderIntoRequest(request, fullGameSession.getPlayer(0).getPlayerName());

            for (int i = 0; i < 4; i++) {
                assertFalse(fullGameSession.getPlayer(i).hasTradeOffer(request.getTradeOffer()));
            }
            fullGameSession.userInput(request);
            for (int i = 0; i < 4; i++) {
                assertTrue(fullGameSession.getPlayer(i).hasTradeOffer(request.getTradeOffer()));
            }
        }

        @Test
        @DisplayName("Should send TradeOffer to all players")
        void shouldSendTradeOfferToAllPlayers() throws GameStateException, InterruptedException {
            StartTradeRequest request = createStartTradeRequestFromPlayer(fullGameSession.getPlayer(0).getPlayerName());
            insertSenderIntoRequest(request, fullGameSession.getPlayer(0).getPlayerName());

            fullGameSession.userInput(request);
            lock.await(1000, TimeUnit.MILLISECONDS);
            NewTradeOfferMessage event = eventsContains(NewTradeOfferMessage.class);
            assertNotNull(event);
            assertTrue(event.getReceiver().isEmpty());
        }
    }

    @Nested
    class NewBankTrade {
        private StartBankTradeRequest createStartBankTradeRequestFromPlayer(String sender) {
            TradeOffer tradeOffer = new TradeOffer();
            StartBankTradeRequest request = new StartBankTradeRequest("", tradeOffer);
            insertSenderIntoRequest(request, sender);
            return request;
        }

        @ParameterizedTest
        @EnumSource(value = ResourceType.class)
        void senderNotHaveResources_ShouldThrowException(ResourceType resource) {
            StartBankTradeRequest request = createStartBankTradeRequestFromPlayer(fullGameSession.getPlayer(0).getPlayerName());
            request.getTradeOffer().setOffer(resource, 1);

            var exception = assertThrows(TradeNotEnoughResourcesException.class, () -> fullGameSession.userInput(request));
            assertEquals("Sender does not have resources", exception.getMessage());
        }

        @Test
        void senderNotCurrentTurn_ShouldThrowException() {
            Player notTurn = fullGameSession.getPlayer(1);
            StartBankTradeRequest request = createStartBankTradeRequestFromPlayer(notTurn.getPlayerName());
            var exception = assertThrows(TradeException.class, () -> fullGameSession.userInput(request));
            assertEquals("It is not this players turn:" + notTurn.getPlayerName(), exception.getMessage());
        }

        @Test
        void tradeOfferNotHaveResources_ShouldThrowException() {
            StartBankTradeRequest request = createStartBankTradeRequestFromPlayer(fullGameSession.getPlayer(0).getPlayerName());
            request.getTradeOffer().setWant(ResourceType.LUMBER, 0);
            var exception = assertThrows(TradeException.class, () -> fullGameSession.userInput(request));
            assertEquals("Trade is empty", exception.getMessage());
        }
    }

    @Nested
    class NewCounterTrade {
        private StartCounterTradeOffer createCounterTradeRequestFromPlayer(String offeringPlayer) {
            TradeOffer oldTradeOffer = new TradeOffer();
            oldTradeOffer.setOfferingPlayer(offeringPlayer);
            TradeOffer tradeOffer = new TradeOffer();
            return new StartCounterTradeOffer("", oldTradeOffer, tradeOffer);
        }

        @ParameterizedTest
        @EnumSource(value = ResourceType.class)
        void offeringPlayerNotHaveResources_ShouldThrowException(ResourceType resource) {
            Player oldOfferingPlayer = fullGameSession.getPlayer(0);
            Player newOfferingPlayer = fullGameSession.getPlayer(1);
            StartCounterTradeOffer request = createCounterTradeRequestFromPlayer(oldOfferingPlayer.getPlayerName());
            insertSenderIntoRequest(request, newOfferingPlayer.getPlayerName());
            oldOfferingPlayer.addTradeOffer(request.getTradeOffer());
            newOfferingPlayer.addTradeOffer(request.getTradeOffer());
            request.getNewTradeOffer().setOffer(resource, 1);

            var exception = assertThrows(TradeNotEnoughResourcesException.class, () -> fullGameSession.userInput(request));
            assertEquals("Offering Player does not have resources", exception.getMessage());
        }

        @Test
        void tradeOfferNotHaveResources_ShouldThrowException() {
            Player oldOfferingPlayer = fullGameSession.getPlayer(0);
            Player newOfferingPlayer = fullGameSession.getPlayer(1);
            StartCounterTradeOffer request = createCounterTradeRequestFromPlayer(oldOfferingPlayer.getPlayerName());
            insertSenderIntoRequest(request, newOfferingPlayer.getPlayerName());
            oldOfferingPlayer.addTradeOffer(request.getTradeOffer());
            newOfferingPlayer.addTradeOffer(request.getTradeOffer());
            request.getNewTradeOffer().setWant(ResourceType.LUMBER, 0);

            var exception = assertThrows(TradeException.class, () -> fullGameSession.userInput(request));
            assertEquals("Trade is empty", exception.getMessage());
        }

        @Test
        @DisplayName("Offering player is the sender should throw a TradeException")
        void offeringPlayerIsSender_ShouldThrowTradeException() {
            String offeringPlayer = fullGameSession.getPlayer(1).getPlayerName();
            StartCounterTradeOffer request = createCounterTradeRequestFromPlayer(offeringPlayer);
            insertSenderIntoRequest(request, offeringPlayer);
            TradeException exception = assertThrows(TradeException.class, () -> fullGameSession.userInput(request));
            assertEquals("Sender " + offeringPlayer + " Player " + offeringPlayer + " true", exception.getMessage());
        }

        @Test
        @DisplayName("Sending player not already having the old TradeOffer should throw a TradeOfferException")
        void senderNotAlreadyHaveOldTradeOffer_ShouldThrowTradeOfferException() {
            Player offeringPlayer = fullGameSession.getPlayer(0);
            Player senderPlayer = fullGameSession.getPlayer(1);
            StartCounterTradeOffer request = createCounterTradeRequestFromPlayer(offeringPlayer.getPlayerName());
            insertSenderIntoRequest(request, senderPlayer.getPlayerName());
            offeringPlayer.addTradeOffer(request.getTradeOffer());

            TradeOfferException exception = assertThrows(TradeOfferException.class, () -> fullGameSession.userInput(request));
            assertEquals(senderPlayer.createDTO(), exception.getPlayer());
            assertFalse(exception.isAvailable());
        }

        @Test
        @DisplayName("Offering player not already having the old TradeOffer should throw a TradeOfferException")
        void offeringPlayerNotAlreadyHaveOldTradeOffer_ShouldThrowTradeOfferException() {
            Player offeringPlayer = fullGameSession.getPlayer(0);
            Player senderPlayer = fullGameSession.getPlayer(1);
            StartCounterTradeOffer request = createCounterTradeRequestFromPlayer(offeringPlayer.getPlayerName());
            insertSenderIntoRequest(request, senderPlayer.getPlayerName());
            senderPlayer.addTradeOffer(request.getTradeOffer());

            TradeOfferException exception = assertThrows(TradeOfferException.class, () -> fullGameSession.userInput(request));
            assertEquals(offeringPlayer.createDTO(), exception.getPlayer());
            assertFalse(exception.isAvailable());
        }

        private TradeOffer swapTradeOffer(TradeOffer oldTradeOffer, String newOfferingPlayer) {
            TradeOffer newTradeOffer = new TradeOffer(oldTradeOffer.getOffer().copy(), oldTradeOffer.getWant().copy());
            newTradeOffer.setOfferingPlayer(newOfferingPlayer);
            newTradeOffer.setReceivingPlayer(oldTradeOffer.getOfferingPlayer());
            return newTradeOffer;
        }

        @Test
        @DisplayName("Old offering player already having the new TradeOffer should throw a TradeOfferException")
        void oldOfferingPlayerAlreadyHaveNewTradeOffer_ShouldThrowTradeOfferException() {
            Player oldOfferingPlayer = fullGameSession.getPlayer(0);
            Player newOfferingPlayer = fullGameSession.getPlayer(1);
            StartCounterTradeOffer request = createCounterTradeRequestFromPlayer(oldOfferingPlayer.getPlayerName());
            insertSenderIntoRequest(request, newOfferingPlayer.getPlayerName());
            oldOfferingPlayer.addTradeOffer(request.getTradeOffer());
            newOfferingPlayer.addTradeOffer(request.getTradeOffer());

            TradeOffer newTradeOffer = swapTradeOffer(request.getTradeOffer(), newOfferingPlayer.getPlayerName());
            oldOfferingPlayer.addTradeOffer(newTradeOffer);

            TradeOfferException exception = assertThrows(TradeOfferException.class, () -> fullGameSession.userInput(request));
            assertEquals(oldOfferingPlayer.createDTO(), exception.getPlayer());
            assertTrue(exception.isAvailable());
        }

        @Test
        @DisplayName("Should add the new TradeOffer to both players")
        void shouldAddNewTradeOfferToBothPlayers() throws GameStateException {
            Player oldOfferingPlayer = fullGameSession.getPlayer(0);
            Player newOfferingPlayer = fullGameSession.getPlayer(1);
            StartCounterTradeOffer request = createCounterTradeRequestFromPlayer(oldOfferingPlayer.getPlayerName());
            insertSenderIntoRequest(request, newOfferingPlayer.getPlayerName());
            oldOfferingPlayer.addTradeOffer(request.getTradeOffer());
            newOfferingPlayer.addTradeOffer(request.getTradeOffer());
            TradeOffer newTradeOffer = swapTradeOffer(request.getTradeOffer(), newOfferingPlayer.getPlayerName());

            assertFalse(oldOfferingPlayer.hasTradeOffer(newTradeOffer));
            assertFalse(newOfferingPlayer.hasTradeOffer(newTradeOffer));
            fullGameSession.userInput(request);
            assertTrue(oldOfferingPlayer.hasTradeOffer(newTradeOffer));
            assertTrue(newOfferingPlayer.hasTradeOffer(newTradeOffer));
        }

        @Test
        @DisplayName("Should send the new TradeOffer to all players")
        void shouldSendNewTradeOfferToAllPlayers() throws GameStateException, InterruptedException {
            Player oldOfferingPlayer = fullGameSession.getPlayer(0);
            Player newOfferingPlayer = fullGameSession.getPlayer(1);
            StartCounterTradeOffer request = createCounterTradeRequestFromPlayer(oldOfferingPlayer.getPlayerName());
            insertSenderIntoRequest(request, newOfferingPlayer.getPlayerName());
            oldOfferingPlayer.addTradeOffer(request.getTradeOffer());
            newOfferingPlayer.addTradeOffer(request.getTradeOffer());

            fullGameSession.userInput(request);
            lock.await(1000, TimeUnit.MILLISECONDS);

            NewTradeOfferMessage event = eventsContains(NewTradeOfferMessage.class);
            assertNotNull(event);
            assertTrue(event.getReceiver().isEmpty());
        }
    }

    @Nested
    class AcceptTrade {

        @Nested
        class Trade {
            private AcceptTradeOfferRequest createAcceptTradeOfferRequest(String offeringPlayer, String receivingPlayer) {
                TradeOffer tradeOffer = new TradeOffer();
                tradeOffer.setOfferingPlayer(offeringPlayer);
                return new AcceptTradeOfferRequest("", tradeOffer, receivingPlayer);
            }

            private TradeOffer createCounter(TradeOffer tradeOffer, String newOfferingPlayer) {
                TradeOffer counter = new TradeOffer();
                counter.setOfferingPlayer(newOfferingPlayer);
                counter.setReceivingPlayer(tradeOffer.getOfferingPlayer());
                return counter;
            }

            @Test
            void shouldRemoveCountersToOriginalTrade() throws GameStateException {
                Player offeringPlayer = fullGameSession.getPlayer(0);
                Player receivingPlayer = fullGameSession.getPlayer(1);
                Player other1 = fullGameSession.getPlayer(2);
                Player other2 = fullGameSession.getPlayer(3);
                AcceptTradeOfferRequest request = createAcceptTradeOfferRequest(offeringPlayer.getPlayerName(), receivingPlayer.getPlayerName());
                insertSenderIntoRequest(request, offeringPlayer.getPlayerName());
                offeringPlayer.addTradeOffer(request.getTradeOffer());
                receivingPlayer.addTradeOffer(request.getTradeOffer());
                other1.addTradeOffer(request.getTradeOffer());
                other2.addTradeOffer(request.getTradeOffer());
                receivingPlayer.interestTradeOffer(request.getTradeOffer());
                receivingPlayer.getInventory().setLumber(1);
                TradeOffer receivingCounter = createCounter(request.getTradeOffer(), receivingPlayer.getPlayerName());
                TradeOffer otherCounter1 = createCounter(request.getTradeOffer(), other1.getPlayerName());
                TradeOffer otherCounter2 = createCounter(request.getTradeOffer(), other2.getPlayerName());
                offeringPlayer.addCounterTradeOffer(request.getTradeOffer(), receivingCounter);
                offeringPlayer.addCounterTradeOffer(request.getTradeOffer(), otherCounter1);
                offeringPlayer.addCounterTradeOffer(request.getTradeOffer(), otherCounter2);
                receivingPlayer.addCounterTradeOffer(request.getTradeOffer(), receivingCounter);
                other1.addCounterTradeOffer(request.getTradeOffer(), otherCounter1);
                other2.addCounterTradeOffer(request.getTradeOffer(), otherCounter2);

                assertTrue(offeringPlayer.hasTradeOffer(receivingCounter));
                assertTrue(offeringPlayer.hasTradeOffer(otherCounter1));
                assertTrue(offeringPlayer.hasTradeOffer(otherCounter2));
                assertTrue(receivingPlayer.hasTradeOffer(receivingCounter));
                assertTrue(other1.hasTradeOffer(otherCounter1));
                assertTrue(other2.hasTradeOffer(otherCounter2));

                fullGameSession.userInput(request);

                assertFalse(offeringPlayer.hasTradeOffer(receivingCounter));
                assertFalse(offeringPlayer.hasTradeOffer(otherCounter1));
                assertFalse(offeringPlayer.hasTradeOffer(otherCounter2));
                assertFalse(receivingPlayer.hasTradeOffer(receivingCounter));
                assertFalse(other1.hasTradeOffer(otherCounter1));
                assertFalse(other2.hasTradeOffer(otherCounter2));
            }

            @Test
            void shouldRemoveTradeFromAllPlayers() throws GameStateException {
                Player offeringPlayer = fullGameSession.getPlayer(0);
                Player receivingPlayer = fullGameSession.getPlayer(1);
                Player other1 = fullGameSession.getPlayer(2);
                Player other2 = fullGameSession.getPlayer(3);
                AcceptTradeOfferRequest request = createAcceptTradeOfferRequest(offeringPlayer.getPlayerName(), receivingPlayer.getPlayerName());
                insertSenderIntoRequest(request, offeringPlayer.getPlayerName());
                offeringPlayer.addTradeOffer(request.getTradeOffer());
                receivingPlayer.addTradeOffer(request.getTradeOffer());
                other1.addTradeOffer(request.getTradeOffer());
                other2.addTradeOffer(request.getTradeOffer());
                receivingPlayer.interestTradeOffer(request.getTradeOffer());
                receivingPlayer.getInventory().setLumber(1);

                assertTrue(offeringPlayer.hasTradeOffer(request.getTradeOffer()));
                assertTrue(receivingPlayer.hasTradeOffer(request.getTradeOffer()));
                assertTrue(other1.hasTradeOffer(request.getTradeOffer()));
                assertTrue(other2.hasTradeOffer(request.getTradeOffer()));

                fullGameSession.userInput(request);

                assertFalse(offeringPlayer.hasTradeOffer(request.getTradeOffer()));
                assertFalse(receivingPlayer.hasTradeOffer(request.getTradeOffer()));
                assertFalse(other1.hasTradeOffer(request.getTradeOffer()));
                assertFalse(other2.hasTradeOffer(request.getTradeOffer()));
            }
        }

        @Nested
        class CounterTrade {
            private AcceptTradeOfferRequest createAcceptTradeOfferRequest(String offeringPlayer, String receivingPlayer) {
                TradeOffer tradeOffer = new TradeOffer();
                tradeOffer.setOfferingPlayer(offeringPlayer);
                tradeOffer.setReceivingPlayer(receivingPlayer);
                return new AcceptTradeOfferRequest("", tradeOffer, receivingPlayer);
            }

            @Test
            @DisplayName("Offering player not already having the TradeOffer should throw a TradeOfferException")
            void offeringPlayerNotAlreadyHaveTradeOffer_ShouldThrowTradeOfferException() {
                Player offeringPlayer = fullGameSession.getPlayer(0);
                Player receivingPlayer = fullGameSession.getPlayer(1);
                AcceptTradeOfferRequest request = createAcceptTradeOfferRequest(offeringPlayer.getPlayerName(), receivingPlayer.getPlayerName());
                insertSenderIntoRequest(request, offeringPlayer.getPlayerName());
                receivingPlayer.interestTradeOffer(request.getTradeOffer());

                TradeOfferException exception = assertThrows(TradeOfferException.class, () -> fullGameSession.userInput(request));
                assertEquals(offeringPlayer.createDTO(), exception.getPlayer());
                assertFalse(exception.isAvailable());
            }

            @Test
            @DisplayName("Offering player isn't the sender should throw a TradeException")
            void offeringPlayerNotSender_ShouldThrowTradeException() {
                Player offeringPlayer = fullGameSession.getPlayer(0);
                Player receivingPlayer = fullGameSession.getPlayer(1);
                AcceptTradeOfferRequest request = createAcceptTradeOfferRequest(offeringPlayer.getPlayerName(), receivingPlayer.getPlayerName());
                insertSenderIntoRequest(request, receivingPlayer.getPlayerName());
                offeringPlayer.addTradeOffer(request.getTradeOffer());
                receivingPlayer.interestTradeOffer(request.getTradeOffer());

                TradeException exception = assertThrows(TradeException.class, () -> fullGameSession.userInput(request));
                assertEquals("Sender " + receivingPlayer.getPlayerName() + " Player " + offeringPlayer.getPlayerName() + " false",
                        exception.getMessage());
            }

            @Test
            @DisplayName("Receiving player not having interest should throw a TradeException")
            void receiverNotHaveInterestInTrade_ShouldThrowTradeException() {
                Player offeringPlayer = fullGameSession.getPlayer(0);
                String receivingPlayerName = fullGameSession.getPlayer(1).getPlayerName();
                AcceptTradeOfferRequest request = createAcceptTradeOfferRequest(offeringPlayer.getPlayerName(), receivingPlayerName);
                insertSenderIntoRequest(request, offeringPlayer.getPlayerName());
                offeringPlayer.addTradeOffer(request.getTradeOffer());

                TradeException exception = assertThrows(TradeException.class, () -> fullGameSession.userInput(request));
                assertEquals("Receiving player " + receivingPlayerName + " does not have interest in Trade Offer", exception.getMessage());
            }

            @Test
            void shouldRemoveOriginalTradeFromAllPlayers() throws GameStateException {
                Player offeringPlayer = fullGameSession.getPlayer(0);
                Player receivingPlayer = fullGameSession.getPlayer(1);
                Player other1 = fullGameSession.getPlayer(2);
                Player other2 = fullGameSession.getPlayer(3);
                TradeOffer original = new TradeOffer();
                original.setOfferingPlayer(receivingPlayer.getPlayerName());
                fullGameSession.getPlayersList().forEach(player -> player.addTradeOffer(original));
                AcceptTradeOfferRequest request = createAcceptTradeOfferRequest(offeringPlayer.getPlayerName(), receivingPlayer.getPlayerName());
                insertSenderIntoRequest(request, offeringPlayer.getPlayerName());
                offeringPlayer.addCounterTradeOffer(original, request.getTradeOffer());
                receivingPlayer.addCounterTradeOffer(original, request.getTradeOffer());
                receivingPlayer.interestTradeOffer(request.getTradeOffer());
                receivingPlayer.getInventory().setLumber(1);

                assertTrue(offeringPlayer.hasTradeOffer(original));
                assertTrue(receivingPlayer.hasTradeOffer(original));
                assertTrue(other1.hasTradeOffer(original));
                assertTrue(other2.hasTradeOffer(original));

                fullGameSession.userInput(request);

                assertFalse(offeringPlayer.hasTradeOffer(original));
                assertFalse(receivingPlayer.hasTradeOffer(original));
                assertFalse(other1.hasTradeOffer(original));
                assertFalse(other2.hasTradeOffer(original));
            }

            @Test
            void shouldRemoveOtherCountersToOriginalTrade() throws GameStateException {
                Player offeringPlayer = fullGameSession.getPlayer(0);
                Player receivingPlayer = fullGameSession.getPlayer(1);
                Player other1 = fullGameSession.getPlayer(2);
                Player other2 = fullGameSession.getPlayer(3);
                TradeOffer original = new TradeOffer();
                original.setOfferingPlayer(receivingPlayer.getPlayerName());
                fullGameSession.getPlayersList().forEach(player -> player.addTradeOffer(original));
                AcceptTradeOfferRequest request = createAcceptTradeOfferRequest(offeringPlayer.getPlayerName(), receivingPlayer.getPlayerName());
                insertSenderIntoRequest(request, offeringPlayer.getPlayerName());
                offeringPlayer.addCounterTradeOffer(original, request.getTradeOffer());
                receivingPlayer.addCounterTradeOffer(original, request.getTradeOffer());
                receivingPlayer.interestTradeOffer(request.getTradeOffer());
                receivingPlayer.getInventory().setLumber(1);
                TradeOffer otherCounter1 = new TradeOffer();
                TradeOffer otherCounter2 = new TradeOffer();
                offeringPlayer.addCounterTradeOffer(original, otherCounter1);
                offeringPlayer.addCounterTradeOffer(original, otherCounter2);
                other1.addCounterTradeOffer(original, otherCounter1);
                other2.addCounterTradeOffer(original, otherCounter2);

                assertTrue(offeringPlayer.hasTradeOffer(otherCounter1));
                assertTrue(offeringPlayer.hasTradeOffer(otherCounter2));
                assertTrue(other1.hasTradeOffer(otherCounter1));
                assertTrue(other2.hasTradeOffer(otherCounter2));

                fullGameSession.userInput(request);

                assertFalse(offeringPlayer.hasTradeOffer(otherCounter1));
                assertFalse(offeringPlayer.hasTradeOffer(otherCounter2));
                assertFalse(other1.hasTradeOffer(otherCounter1));
                assertFalse(other2.hasTradeOffer(otherCounter2));
            }

            @Test
            @DisplayName("Should add and remove resources from both players")
            void shouldAddAndRemoveResourcesFromBothPlayers() throws GameStateException {
                Player offeringPlayer = fullGameSession.getPlayer(0);
                Player receivingPlayer = fullGameSession.getPlayer(1);
                receivingPlayer.getInventory().setLumber(1);
                AcceptTradeOfferRequest request = createAcceptTradeOfferRequest(offeringPlayer.getPlayerName(), receivingPlayer.getPlayerName());
                insertSenderIntoRequest(request, offeringPlayer.getPlayerName());
                offeringPlayer.addTradeOffer(request.getTradeOffer());
                receivingPlayer.addTradeOffer(request.getTradeOffer());
                receivingPlayer.interestTradeOffer(request.getTradeOffer());

                assertTrue(offeringPlayer.hasTradeOffer(request.getTradeOffer()));
                assertTrue(receivingPlayer.hasTradeOffer(request.getTradeOffer()));

                fullGameSession.userInput(request);

                assertFalse(offeringPlayer.hasTradeOffer(request.getTradeOffer()));
                assertFalse(receivingPlayer.hasTradeOffer(request.getTradeOffer()));
            }

            @Test
            @DisplayName("Should send trade accepted to all players")
            void shouldSendAcceptedToAllPlayers() throws GameStateException, InterruptedException {
                Player offeringPlayer = fullGameSession.getPlayer(0);
                Player receivingPlayer = fullGameSession.getPlayer(1);
                offeringPlayer.getInventory().setLumber(1);
                receivingPlayer.getInventory().setLumber(1);
                AcceptTradeOfferRequest request = createAcceptTradeOfferRequest(offeringPlayer.getPlayerName(), receivingPlayer.getPlayerName());
                insertSenderIntoRequest(request, offeringPlayer.getPlayerName());
                offeringPlayer.addTradeOffer(request.getTradeOffer());
                receivingPlayer.addTradeOffer(request.getTradeOffer());
                receivingPlayer.interestTradeOffer(request.getTradeOffer());

                fullGameSession.userInput(request);
                lock.await(1000, TimeUnit.MILLISECONDS);
                TradeOfferAcceptedMessage event = eventsContains(TradeOfferAcceptedMessage.class);
                assertNotNull(event);
                assertEquals(receivingPlayer.createDTO(), event.getTradeReceiver());
            }
        }
    }

    @Nested
    class InterestTrade {
        private InterestTradeOfferRequest createInterestTradeOfferRequest(String offeringPlayer) {
            TradeOffer tradeOffer = new TradeOffer();
            tradeOffer.setOfferingPlayer(offeringPlayer);
            return new InterestTradeOfferRequest("", tradeOffer);
        }

        @Test
        @DisplayName("Sending Player not having resources should throw TradeException")
        void senderNotHaveResources_ShouldThrowTradeException() {
            Player offeringPlayer = fullGameSession.getPlayer(0);
            Player sendingPlayer = fullGameSession.getPlayer(1);
            InterestTradeOfferRequest request = createInterestTradeOfferRequest(offeringPlayer.getPlayerName());
            insertSenderIntoRequest(request, sendingPlayer.getPlayerName());

            TradeException exception = assertThrows(TradeException.class, () -> fullGameSession.userInput(request));
            assertEquals("Interested Player does not have resources", exception.getMessage());
        }

        @Test
        @DisplayName("Offering player not already having the TradeOffer should throw a TradeOfferException")
        void offeringPlayerNotAlreadyHaveTradeOffer_ShouldThrowTradeOfferException() {
            Player offeringPlayer = fullGameSession.getPlayer(0);
            Player interestPlayer = fullGameSession.getPlayer(1);
            interestPlayer.getInventory().setLumber(1);
            InterestTradeOfferRequest request = createInterestTradeOfferRequest(offeringPlayer.getPlayerName());
            insertSenderIntoRequest(request, interestPlayer.getPlayerName());

            TradeOfferException exception = assertThrows(TradeOfferException.class, () -> fullGameSession.userInput(request));
            assertEquals(offeringPlayer.createDTO(), exception.getPlayer());
            assertFalse(exception.isAvailable());
        }

        @Test
        @DisplayName("Sending player not already having the TradeOffer should throw a TradeOfferException")
        void senderNotHaveTradeOffer_ShouldThrowTradeOfferException() {
            Player offeringPlayer = fullGameSession.getPlayer(0);
            Player sendingPlayer = fullGameSession.getPlayer(1);
            sendingPlayer.getInventory().setLumber(1);
            InterestTradeOfferRequest request = createInterestTradeOfferRequest(offeringPlayer.getPlayerName());
            insertSenderIntoRequest(request, sendingPlayer.getPlayerName());
            offeringPlayer.addTradeOffer(request.getTradeOffer());

            TradeOfferException exception = assertThrows(TradeOfferException.class, () -> fullGameSession.userInput(request));
            assertEquals(sendingPlayer.createDTO(), exception.getPlayer());
            assertFalse(exception.isAvailable());
        }

        @Test
        @DisplayName("Offering player is the sender should throw a TradeException")
        void offeringPlayerIsSender_ShouldThrowTradeException() {
            Player offeringPlayer = fullGameSession.getPlayer(0);
            offeringPlayer.getInventory().setLumber(1);
            InterestTradeOfferRequest request = createInterestTradeOfferRequest(offeringPlayer.getPlayerName());
            insertSenderIntoRequest(request, offeringPlayer.getPlayerName());
            offeringPlayer.addTradeOffer(request.getTradeOffer());

            TradeException exception = assertThrows(TradeException.class, () -> fullGameSession.userInput(request));
            assertEquals("Sender " + offeringPlayer.getPlayerName() + " Player " + offeringPlayer.getPlayerName() + " true",
                    exception.getMessage());
        }

        @Test
        @DisplayName("Should add interest to sending player")
        void shouldAddInterestToSendingPlayer() throws GameStateException {
            Player offeringPlayer = fullGameSession.getPlayer(0);
            Player sendingPlayer = fullGameSession.getPlayer(1);
            sendingPlayer.getInventory().setLumber(1);
            InterestTradeOfferRequest request = createInterestTradeOfferRequest(offeringPlayer.getPlayerName());
            insertSenderIntoRequest(request, sendingPlayer.getPlayerName());
            offeringPlayer.addTradeOffer(request.getTradeOffer());
            sendingPlayer.addTradeOffer(request.getTradeOffer());

            assertFalse(sendingPlayer.hasInterestTradeOffer(request.getTradeOffer()));

            fullGameSession.userInput(request);

            assertTrue(sendingPlayer.hasInterestTradeOffer(request.getTradeOffer()));
        }

        @Test
        @DisplayName("Should send interest to offering player")
        void shouldSendInterestToOfferingPlayer() throws GameStateException, InterruptedException {
            Player offeringPlayer = fullGameSession.getPlayer(0);
            Player sendingPlayer = fullGameSession.getPlayer(1);
            offeringPlayer.getInventory().setLumber(1);
            sendingPlayer.getInventory().setLumber(1);
            InterestTradeOfferRequest request = createInterestTradeOfferRequest(offeringPlayer.getPlayerName());
            insertSenderIntoRequest(request, sendingPlayer.getPlayerName());
            offeringPlayer.addTradeOffer(request.getTradeOffer());
            sendingPlayer.addTradeOffer(request.getTradeOffer());

            fullGameSession.userInput(request);
            lock.await(1000, TimeUnit.MILLISECONDS);

            assertEquals(TradeOfferInterestMessage.class, event.getClass());
        }
    }

    @Nested
    class DeclineTrade {
        private DeclineTradeOfferRequest createDeclineTradeOfferRequest(String offeringPlayer) {
            TradeOffer tradeOffer = new TradeOffer();
            tradeOffer.setOfferingPlayer(offeringPlayer);
            return new DeclineTradeOfferRequest("", tradeOffer);
        }

        @Nested
        class CounterTrade {
            private DeclineTradeOfferRequest createCounterDeclineTradeOfferRequest(String offeringPlayer, String receivingPlayer) {
                DeclineTradeOfferRequest request = createDeclineTradeOfferRequest(offeringPlayer);
                request.getTradeOffer().setReceivingPlayer(receivingPlayer);
                return request;
            }

            @Test
            @DisplayName("Receiving player isn't the sender should throw a TradeException")
            void receivingPlayerNotSender_ShouldThrowTradeException() {
                String offeringPlayerName = fullGameSession.getPlayer(0).getPlayerName();
                String receivingPlayerName = fullGameSession.getPlayer(1).getPlayerName();
                DeclineTradeOfferRequest request = createCounterDeclineTradeOfferRequest(offeringPlayerName, receivingPlayerName);
                insertSenderIntoRequest(request, offeringPlayerName);

                TradeException exception = assertThrows(TradeException.class, () -> fullGameSession.userInput(request));
                assertEquals("Sender " + offeringPlayerName + " Player " + receivingPlayerName + " false", exception.getMessage());
            }

            @Test
            @DisplayName("Receiving player not already having the TradeOffer should throw a TradeOfferException")
            void receivingPlayerNotHaveTradeOffer_ShouldThrowTradeOfferException() {
                Player offeringPlayer = fullGameSession.getPlayer(0);
                Player receivingPlayer = fullGameSession.getPlayer(1);
                DeclineTradeOfferRequest request = createCounterDeclineTradeOfferRequest(offeringPlayer.getPlayerName(),
                        receivingPlayer.getPlayerName());
                insertSenderIntoRequest(request, receivingPlayer.getPlayerName());

                TradeOfferException exception = assertThrows(TradeOfferException.class, () -> fullGameSession.userInput(request));
                assertEquals(receivingPlayer.createDTO(), exception.getPlayer());
                assertFalse(exception.isAvailable());
            }

            @Test
            @DisplayName("Should remove TradeOffer from both players")
            void shouldRemoveTradeOfferFromBothPlayers() throws GameStateException {
                Player offeringPlayer = fullGameSession.getPlayer(0);
                Player receivingPlayer = fullGameSession.getPlayer(1);
                DeclineTradeOfferRequest request = createCounterDeclineTradeOfferRequest(offeringPlayer.getPlayerName(),
                        receivingPlayer.getPlayerName());
                insertSenderIntoRequest(request, receivingPlayer.getPlayerName());
                offeringPlayer.addTradeOffer(request.getTradeOffer());
                receivingPlayer.addTradeOffer(request.getTradeOffer());

                assertTrue(offeringPlayer.hasTradeOffer(request.getTradeOffer()));
                assertTrue(receivingPlayer.hasTradeOffer(request.getTradeOffer()));

                fullGameSession.userInput(request);

                assertFalse(offeringPlayer.hasTradeOffer(request.getTradeOffer()));
                assertFalse(receivingPlayer.hasTradeOffer(request.getTradeOffer()));
            }

            @Test
            @DisplayName("Should send canceled to all players")
            void shouldSendCanceledToAllPlayers() throws GameStateException, InterruptedException {
                lock = new CountDownLatch(2);
                Player offeringPlayer = fullGameSession.getPlayer(0);
                Player receivingPlayer = fullGameSession.getPlayer(1);
                DeclineTradeOfferRequest request = createCounterDeclineTradeOfferRequest(offeringPlayer.getPlayerName(),
                        receivingPlayer.getPlayerName());
                insertSenderIntoRequest(request, receivingPlayer.getPlayerName());
                receivingPlayer.addTradeOffer(request.getTradeOffer());

                fullGameSession.userInput(request);
                lock.await(1000, TimeUnit.MILLISECONDS);
                TradeOfferCanceledMessage event = eventsContains(TradeOfferCanceledMessage.class);
                assertNotNull(event);
                assertTrue(event.getReceiver().isEmpty());
            }
        }

        @Nested
        class Trade {

            @Test
            @DisplayName("Should send declined with sending player if some players haven't declined yet")
            void shouldSendDeclinedWithSenderIfNotAllDeclined() throws GameStateException, InterruptedException {
                Player offeringPlayer = fullGameSession.getPlayer(0);
                Player receivingPlayer = fullGameSession.getPlayer(1);
                DeclineTradeOfferRequest request = createDeclineTradeOfferRequest(offeringPlayer.getPlayerName());
                insertSenderIntoRequest(request, receivingPlayer.getPlayerName());
                addTradeOfferToAllPlayers(fullGameSession, request.getTradeOffer());

                fullGameSession.userInput(request);
                lock.await(1000, TimeUnit.MILLISECONDS);
                assertEquals(TradeOfferDeclinedMessage.class, event.getClass());
                assertEquals(receivingPlayer.getPlayerName(), ((TradeOfferDeclinedMessage) event).getDeclined().getPlayerName());
            }

            @Test
            @DisplayName("Should send canceled if all players have declined")
            void shouldSendCanceledIfAllDeclined() throws GameStateException, InterruptedException {
                lock = new CountDownLatch(2);
                Player offeringPlayer = fullGameSession.getPlayer(0);
                Player receivingPlayer = fullGameSession.getPlayer(1);
                Player receivingPlayer2 = fullGameSession.getPlayer(2);
                Player receivingPlayer3 = fullGameSession.getPlayer(3);
                DeclineTradeOfferRequest request = createDeclineTradeOfferRequest(offeringPlayer.getPlayerName());
                insertSenderIntoRequest(request, receivingPlayer.getPlayerName());
                addTradeOfferToAllPlayers(fullGameSession, request.getTradeOffer());
                receivingPlayer2.declineTradeOffer(request.getTradeOffer());
                receivingPlayer3.declineTradeOffer(request.getTradeOffer());

                fullGameSession.userInput(request);
                lock.await(1000, TimeUnit.MILLISECONDS);
                TradeOfferCanceledMessage event = eventsContains(TradeOfferCanceledMessage.class);
                assertNotNull(event);
            }

            @Test
            @DisplayName("Should remove TradeOffer from all players if they have declined")
            void shouldRemoveTradeOfferFromAllPlayerIfAllDeclined() throws GameStateException {
                Player offeringPlayer = fullGameSession.getPlayer(0);
                Player receivingPlayer = fullGameSession.getPlayer(1);
                Player receivingPlayer2 = fullGameSession.getPlayer(2);
                Player receivingPlayer3 = fullGameSession.getPlayer(3);
                DeclineTradeOfferRequest request = createDeclineTradeOfferRequest(offeringPlayer.getPlayerName());
                insertSenderIntoRequest(request, receivingPlayer.getPlayerName());
                addTradeOfferToAllPlayers(fullGameSession, request.getTradeOffer());
                receivingPlayer2.declineTradeOffer(request.getTradeOffer());
                receivingPlayer3.declineTradeOffer(request.getTradeOffer());

                assertAll(
                        () -> assertTrue(offeringPlayer.hasTradeOffer(request.getTradeOffer())),
                        () -> assertTrue(receivingPlayer.hasTradeOffer(request.getTradeOffer())),
                        () -> assertTrue(receivingPlayer2.hasTradeOffer(request.getTradeOffer())),
                        () -> assertTrue(receivingPlayer3.hasTradeOffer(request.getTradeOffer())));

                fullGameSession.userInput(request);

                assertAll(
                        () -> assertFalse(offeringPlayer.hasTradeOffer(request.getTradeOffer())),
                        () -> assertFalse(receivingPlayer.hasTradeOffer(request.getTradeOffer())),
                        () -> assertFalse(receivingPlayer2.hasTradeOffer(request.getTradeOffer())),
                        () -> assertFalse(receivingPlayer3.hasTradeOffer(request.getTradeOffer())));
            }
        }
    }

    @Nested
    class CancelTrade {
        @Test
        @DisplayName("Offering player isn't the sender should throw a TradeException")
        void offeringPlayerNotSender_ShouldThrowTradeException() {
            Player offeringPlayer = fullGameSession.getPlayer(0);
            Player sender = fullGameSession.getPlayer(1);
            CancelTradeOfferRequest request = new CancelTradeOfferRequest("", new TradeOffer());
            request.getTradeOffer().setOfferingPlayer(offeringPlayer.getPlayerName());
            insertSenderIntoRequest(request, sender.getPlayerName());

            TradeException exception = assertThrows(TradeException.class, () -> fullGameSession.userInput(request));
            assertEquals("Sender " + sender.getPlayerName() + " Player " + offeringPlayer.getPlayerName() + " false", exception.getMessage());
        }

        @Test
        @DisplayName("Should remove TradeOffer from both players if it was a counter trade")
        void shouldRemoveTradeOfferFromBothPlayerIfCounterTrade() throws GameStateException {
            Player offeringPlayer = fullGameSession.getPlayer(0);
            Player receivingPlayer = fullGameSession.getPlayer(1);
            CancelTradeOfferRequest request = new CancelTradeOfferRequest("", new TradeOffer());
            insertSenderIntoRequest(request, offeringPlayer.getPlayerName());
            request.getTradeOffer().setOfferingPlayer(offeringPlayer.getPlayerName());
            request.getTradeOffer().setReceivingPlayer(receivingPlayer.getPlayerName());
            offeringPlayer.addTradeOffer(request.getTradeOffer());
            receivingPlayer.addTradeOffer(request.getTradeOffer());

            assertTrue(offeringPlayer.hasTradeOffer(request.getTradeOffer()));
            assertTrue(receivingPlayer.hasTradeOffer(request.getTradeOffer()));

            fullGameSession.userInput(request);

            assertFalse(offeringPlayer.hasTradeOffer(request.getTradeOffer()));
            assertFalse(receivingPlayer.hasTradeOffer(request.getTradeOffer()));
        }

        @Test
        @DisplayName("Should remove the TradeOffer from all players if it was a normal trade")
        void shouldRemoveTradeOfferFromAllPlayerIfTrade() throws GameStateException {
            Player offeringPlayer = fullGameSession.getPlayer(0);
            CancelTradeOfferRequest request = new CancelTradeOfferRequest("", new TradeOffer());
            insertSenderIntoRequest(request, offeringPlayer.getPlayerName());
            request.getTradeOffer().setOfferingPlayer(offeringPlayer.getPlayerName());
            addTradeOfferToAllPlayers(fullGameSession, request.getTradeOffer());

            Player[] players = fullGameSession.getPlayers();

            for (Player player : players) {
                assertTrue(player.hasTradeOffer(request.getTradeOffer()));
            }

            fullGameSession.userInput(request);

            for (Player player : players) {
                assertFalse(player.hasTradeOffer(request.getTradeOffer()));
            }
        }

        @Test
        @DisplayName("Should send canceled if it was a counter trade")
        void shouldSendCanceledIfCounterTrade() throws GameStateException, InterruptedException {
            lock = new CountDownLatch(2);
            Player offeringPlayer = fullGameSession.getPlayer(0);
            Player receivingPlayer = fullGameSession.getPlayer(1);
            CancelTradeOfferRequest request = new CancelTradeOfferRequest("", new TradeOffer());
            insertSenderIntoRequest(request, offeringPlayer.getPlayerName());
            request.getTradeOffer().setOfferingPlayer(offeringPlayer.getPlayerName());
            request.getTradeOffer().setReceivingPlayer(receivingPlayer.getPlayerName());

            fullGameSession.userInput(request);
            lock.await(1000, TimeUnit.MILLISECONDS);
            TradeOfferCanceledMessage event = eventsContains(TradeOfferCanceledMessage.class);
            assertNotNull(event);
        }

        @Test
        @DisplayName("Should send canceled if it was a trade")
        void shouldSendCanceledIfTrade() throws GameStateException, InterruptedException {
            lock = new CountDownLatch(2);
            Player offeringPlayer = fullGameSession.getPlayer(0);
            CancelTradeOfferRequest request = new CancelTradeOfferRequest("", new TradeOffer());
            insertSenderIntoRequest(request, offeringPlayer.getPlayerName());
            request.getTradeOffer().setOfferingPlayer(offeringPlayer.getPlayerName());
            addTradeOfferToAllPlayers(fullGameSession, request.getTradeOffer());

            fullGameSession.userInput(request);
            lock.await(1000, TimeUnit.MILLISECONDS);
            TradeOfferCanceledMessage event = eventsContains(TradeOfferCanceledMessage.class);
            assertNotNull(event);
        }
    }
}
