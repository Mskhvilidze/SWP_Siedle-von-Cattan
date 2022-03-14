package de.uol.swp.server.game.state;

import de.uol.swp.common.game.TradeOffer;
import de.uol.swp.common.game.board.Port;
import de.uol.swp.common.game.board.ResourceType;
import de.uol.swp.common.game.message.trade.AllTradesCanceledMessage;
import de.uol.swp.common.game.request.trade.*;
import de.uol.swp.server.exception.*;
import de.uol.swp.server.game.InventoryService;
import de.uol.swp.server.game.Player;
import de.uol.swp.server.game.session.GameSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Set;

/**
 * This state is called during the PlayState and handles all trade requests
 */
public enum TradeState implements GameState {

    INSTANCE;

    private static final Logger LOG = LogManager.getLogger(TradeState.class);

    @Override
    public void next(GameSession gameSession, StateContext context) {
        gameSession.setCurrentState(PlayState.INSTANCE);
    }

    @Override
    public void action(GameSession gameSession, StateContext context) throws GameStateException {
        if (!(context.getLastRequest() instanceof AbstractTradeRequest)) {
            //invalid state for request
            ensureLastTradeLeavesTradeState(gameSession, context);
            throw new InvalidGameStateException();
        }
        LOG.debug("New Trade Request {} in {}", context.getLastRequest(), gameSession.getGameSessionName());
        AbstractTradeRequest request = (AbstractTradeRequest) context.getLastRequest();
        injectOfferingPlayerFromSender(request);
        Player offeringPlayer = gameSession.getPlayer(request.getTradeOffer().getOfferingPlayer());

        if (offeringPlayer == null) {
            ensureLastTradeLeavesTradeState(gameSession, context);
            throw new TradeException("Offering Player " + request.getTradeOffer().getOfferingPlayer() + " not in game session");
        }
        Player sender = gameSession.getPlayer(request.getUserNameFromSender());

        if (request instanceof StartTradeRequest) {
            ensurePlayerIsCurrentTurn(gameSession, offeringPlayer);
            TradeOffer newTradeOffer = request.getTradeOffer();
            ensureTradeOfferHasResources(gameSession, context, newTradeOffer);
            if (!offeringPlayer.hasResources(newTradeOffer.getOffer())) {
                ensureLastTradeLeavesTradeState(gameSession, context);
                throw new TradeNotEnoughResourcesException("Offering Player does not have resources");
            }
            ensurePlayerHasTradeOfferIs(offeringPlayer, newTradeOffer, false);

            ensureSenderEqualsPlayerIs(sender, offeringPlayer, true);//TODO: Vlt Subklasse von der Exception? Brauch ja ne Antwort an den Spieler

            gameSession.sendNewTradeOfferToAllPlayers(newTradeOffer);
            gameSession.getPlayersList().forEach(player -> player.addTradeOffer(newTradeOffer));
            startTradeLog(gameSession, offeringPlayer.getPlayerName(), newTradeOffer);
        } else if (request instanceof StartBankTradeRequest) {
            ensurePlayerIsCurrentTurn(gameSession, sender);
            TradeOffer newTradeOffer = request.getTradeOffer();
            ensureTradeOfferHasResources(gameSession, context, newTradeOffer);
            if (!sender.hasResources(newTradeOffer.getOffer())) {
                ensureLastTradeLeavesTradeState(gameSession, context);
                throw new TradeNotEnoughResourcesException("Sender does not have resources");
            }
            bankTrade(gameSession, newTradeOffer, sender);
        } else if (request instanceof StartCounterTradeOffer) {
            TradeOffer offerToBeCountered = request.getTradeOffer();
            ensureSenderEqualsPlayerIs(sender, offeringPlayer, false);
            ensurePlayerHasTradeOfferIs(sender, offerToBeCountered, true);
            ensurePlayerHasTradeOfferIs(offeringPlayer, offerToBeCountered, true);

            TradeOffer newTradeOffer = ((StartCounterTradeOffer) request).getNewTradeOffer();
            ensureTradeOfferHasResources(gameSession, context, newTradeOffer);
            if (!sender.hasResources(newTradeOffer.getOffer())) {
                ensureLastTradeLeavesTradeState(gameSession, context);
                throw new TradeNotEnoughResourcesException("Offering Player does not have resources");
            }
            newTradeOffer.setReceivingPlayer(offerToBeCountered.getOfferingPlayer());
            newTradeOffer.setOfferingPlayer(sender.getPlayerName());

            ensurePlayerHasTradeOfferIs(offeringPlayer, newTradeOffer, false);
            ensurePlayerHasTradeOfferIs(sender, newTradeOffer, false);

            if (offerToBeCountered.getReceivingPlayer() == null) {
                offeringPlayer.addCounterTradeOffer(offerToBeCountered, newTradeOffer);
                sender.addCounterTradeOffer(offerToBeCountered, newTradeOffer);
            } else {
                TradeOffer original = offeringPlayer.removeCounterTradeOffer(offerToBeCountered);
                sender.removeCounterTradeOffer(offerToBeCountered);
                gameSession.sendTradeCanceledToAllPlayers(offerToBeCountered);
                offeringPlayer.addCounterTradeOffer(original, newTradeOffer);
                sender.addCounterTradeOffer(original, newTradeOffer);
            }

            gameSession.sendCounterTradeOffer(newTradeOffer);
            startCounterTradeLog(gameSession, sender.getPlayerName(), newTradeOffer);
        } else if (request instanceof AcceptTradeOfferRequest) {
            TradeOffer acceptedTradeOffer = request.getTradeOffer();
            Player receiver = gameSession.getPlayer(((AcceptTradeOfferRequest) request).getTradeReceiver());
            ensureSenderEqualsPlayerIs(sender, offeringPlayer, true);
            if (!receiver.hasInterestTradeOffer(acceptedTradeOffer)) {
                throw new TradeException("Receiving player " + receiver.getPlayerName() + " does not have interest in Trade Offer");
            }
            ensurePlayerHasTradeOfferIs(offeringPlayer, acceptedTradeOffer, true);

            if (acceptedTradeOffer.getReceivingPlayer() == null) {
                for (Player player : gameSession.getPlayers()) {
                    Set<TradeOffer> counters = player.removeTradeOffer(acceptedTradeOffer);
                    for (TradeOffer counter : counters) {
                        player.removeCounterTradeOffer(counter);
                        if (player.equals(offeringPlayer)) {
                            gameSession.sendTradeCanceledToAllPlayers(counter);
                        }
                    }
                }
                gameSession.getPlayersList().forEach(p -> p.removeTradeOffer(acceptedTradeOffer));
            } else {
                TradeOffer originalTradeOffer = sender.removeCounterTradeOffer(acceptedTradeOffer);
                receiver.removeCounterTradeOffer(acceptedTradeOffer);
                for (Player player : gameSession.getPlayers()) {
                    Set<TradeOffer> counters = player.removeTradeOffer(originalTradeOffer);
                    for (TradeOffer counter : counters) {
                        if (!counter.equals(acceptedTradeOffer)) {
                            player.removeCounterTradeOffer(counter);
                            if (player.equals(offeringPlayer)) {
                                gameSession.sendTradeCanceledToAllPlayers(counter);
                            }
                        }
                    }
                }
                gameSession.getPlayersList().forEach(p -> p.removeTradeOffer(originalTradeOffer));
                gameSession.sendTradeCanceledToAllPlayers(originalTradeOffer);
            }
            gameSession.sendTradeAcceptedToAllPlayers(acceptedTradeOffer, receiver);

            //Trade offer ganz akzeptiert
            try {
                InventoryService inventoryService = gameSession.getInventoryService();
                inventoryService.addAndRemoveResources(offeringPlayer, acceptedTradeOffer.getWant(), acceptedTradeOffer.getOffer());
                inventoryService.addAndRemoveResources(receiver, acceptedTradeOffer.getOffer(), acceptedTradeOffer.getWant());
            } catch (OverDrawException exception) {
                ensureLastTradeLeavesTradeState(gameSession, context);
                throw new TradeException();
            }
            successfulTradeLog(gameSession, offeringPlayer.getPlayerName(), receiver.getPlayerName(), acceptedTradeOffer);
        } else if (request instanceof InterestTradeOfferRequest) {
            TradeOffer interest = request.getTradeOffer();
            if (!sender.hasResources(interest.getWant())) {
                throw new TradeNotEnoughResourcesException(
                        "Interested Player does not have resources"); //TODO: Vlt Subklasse von der Exception? Brauch ja ne Antwort an den Spieler
            }

            ensurePlayerHasTradeOfferIs(offeringPlayer, interest, true);
            ensurePlayerHasTradeOfferIs(sender, interest, true);

            ensureSenderEqualsPlayerIs(sender, offeringPlayer, false);

            sender.interestTradeOffer(interest);
            gameSession.sendTradeInterestToOfferingPlayer(interest, sender.getPlayerName());
        } else if (request instanceof DeclineTradeOfferRequest) {
            TradeOffer declinedOffer = request.getTradeOffer();
            if (declinedOffer.getReceivingPlayer() != null) {
                //CounterTradeOffer
                Player receivingPlayer = gameSession.getPlayer(declinedOffer.getReceivingPlayer());
                ensureSenderEqualsPlayerIs(sender, receivingPlayer, true);
                ensurePlayerHasTradeOfferIs(receivingPlayer, declinedOffer, true);

                offeringPlayer.removeTradeOffer(declinedOffer);
                receivingPlayer.removeTradeOffer(declinedOffer);
                gameSession.sendTradeCanceledToAllPlayers(declinedOffer);
            } else { //TradeOffer
                ensureSenderEqualsPlayerIs(sender, offeringPlayer, false);
                sender.declineTradeOffer(declinedOffer);
                boolean declined = true;
                for (Player player : gameSession.getPlayers()) {
                    if (!player.hasDeclinedTradeOffer(declinedOffer) && !player.equals(offeringPlayer)) {
                        LOG.debug("User {} has not declined Trade offer {} | {}", player, declinedOffer.getOffer(), declinedOffer.getWant());
                        declined = false;
                    }
                }
                if (declined) {
                    gameSession.getPlayersList().forEach(player -> player.removeTradeOffer(declinedOffer));
                    declinedTradeLog(gameSession, offeringPlayer.getPlayerName());
                    gameSession.sendTradeCanceledToAllPlayers(declinedOffer);
                } else {
                    gameSession.sendTradeDeclinedToOfferingPlayer(declinedOffer, sender.getPlayerName());
                }
            }
        } else if (request instanceof CancelTradeOfferRequest) {
            TradeOffer cancelledOffer = request.getTradeOffer();
            ensureSenderEqualsPlayerIs(sender, offeringPlayer, true);
            if (cancelledOffer.getReceivingPlayer() != null) { //CounterTradeOffer
                offeringPlayer.removeTradeOffer(cancelledOffer);
                gameSession.getPlayer(cancelledOffer.getReceivingPlayer()).removeTradeOffer(cancelledOffer);
            } else { //TradeOffer
                gameSession.getPlayersList().forEach(player -> player.removeTradeOffer(cancelledOffer));
            }
            gameSession.sendTradeCanceledToAllPlayers(cancelledOffer);
        }
        ensureLastTradeLeavesTradeState(gameSession, context);
    }

    @Override
    public void endTurn(GameSession gameSession, StateContext context) {
        gameSession.getPlayersList().forEach(Player::clearTradeOffers);
        String gameSessionName = gameSession.getGameSessionName();
        gameSession.getGameSessionService().sendToAllInGameSession(gameSessionName, new AllTradesCanceledMessage(gameSessionName));
        gameSession.sendLogMessage("Alle Handelsangebote wurden abgebrochen");
        GameState.super.endTurn(gameSession, context);
    }

    private void ensureLastTradeLeavesTradeState(GameSession gameSession, StateContext context) {
        Player[] players = gameSession.getPlayers();
        for (Player player : players) {
            if (player.hasAnyTradeOffers()) {
                return;
            }
        }
        next(gameSession, context);
    }

    /**
     * This method ensures that a given player is equal to the player whose turn it is
     *
     * @param gameSession the current game session
     * @param player      any player
     * @throws TradeException if it is not the given players turn
     */
    private void ensurePlayerIsCurrentTurn(GameSession gameSession, Player player) throws TradeException {
        if (!gameSession.getWhoseTurn().equals(player)) {
            throw new TradeException("It is not this players turn:" + player.getPlayerName());
        }
    }

    private void ensureTradeOfferHasResources(GameSession gameSession, StateContext context, TradeOffer tradeOffer) throws TradeException {
        if (tradeOffer.getOffer().sumOfResources() + tradeOffer.getWant().sumOfResources() == 0) { //checks if the trade is empty
            ensureLastTradeLeavesTradeState(gameSession, context);
            throw new TradeException("Trade is empty");
        }
    }

    /**
     * This method ensures that a given sender is either equal or not equal to a player depending on the boolean flag
     *
     * @param sender any player
     * @param player any player
     * @param flag   whether the sender should equal player or not
     * @throws TradeException if flag is {@code true} and sender does not equal player,
     *                        or if flag is {@code false} and sender does equal player
     * @implNote the exception thrown by this method should either not be caught to allow for easier testing or be rethrown
     */
    private void ensureSenderEqualsPlayerIs(Player sender, Player player, boolean flag) throws TradeException {
        if (sender.equals(player) != flag) {
            throw new TradeException("Sender " + sender.getPlayerName() + " Player " + player.getPlayerName() + " " + !flag);
        }
    }

    /**
     * This method ensures that a given player is either does or does not have the given {@code TradeOffer} depending on the boolean flag
     *
     * @param player     the player that should or shouldn't have the trade offer
     * @param tradeOffer any {@code TradeOffer}
     * @param flag       whether the player should have the trade offer or not
     * @throws TradeOfferException if flag is {@code true} and player does not have the trade offer,
     *                             or if flag is {@code false} and player does have the trade offer
     * @implNote the exception thrown by this method should either not be caught to allow for easier testing or be rethrown
     */
    private void ensurePlayerHasTradeOfferIs(Player player, TradeOffer tradeOffer, boolean flag) throws TradeOfferException {
        if (player.hasTradeOffer(tradeOffer) != flag) {
            throw new TradeOfferException(player.createDTO(), !flag);
        }
    }

    /**
     * Injects the sender as offering player into the trade offer if no offering has been entered yet
     *
     * @param request the request that contains the trade offer
     */
    private void injectOfferingPlayerFromSender(AbstractTradeRequest request) {
        TradeOffer tradeOffer = request.getTradeOffer();
        if (tradeOffer.getOfferingPlayer() == null) {
            tradeOffer.setOfferingPlayer(request.getUserNameFromSender());
        }
    }

    /**
     * This method sends a successful trade message into the trade log to be seen by every player in the gameSession
     *
     * @param gameSession    gameSession the trade was made in
     * @param offeringPlayer player, who initialized the trade
     * @param receiver       the player that is been trade with
     * @param tradeOffer     resources that been exchanged
     */
    private void successfulTradeLog(GameSession gameSession, String offeringPlayer, String receiver, TradeOffer tradeOffer) {
        String log = "%s tauscht %s gegen %s mit %s";
        gameSession.sendLogMessage(
                String.format(log, offeringPlayer, tradeOffer.getOffer().toChatFormat(), tradeOffer.getWant().toChatFormat(), receiver));
    }

    /**
     * This method is called, when a player starts a trade offer
     *
     * @param gameSession    gameSession the player made a trade offer in
     * @param offeringPlayer the player, who wants to trade
     * @param tradeOffer     the resources that the player offers and wants
     */
    private void startTradeLog(GameSession gameSession, String offeringPlayer, TradeOffer tradeOffer) {
        if (tradeOffer.getOffer().sumOfResources() == 0) {
            gameSession.sendLogMessage(
                    offeringPlayer + " hat einen Handel gestartet und möchte " + tradeOffer.getWant().toChatFormat() + " geschenkt bekommen.");
        } else if (tradeOffer.getWant().sumOfResources() == 0) {
            gameSession.sendLogMessage(
                    offeringPlayer + " hat einen Handel gestartet und möchte " + tradeOffer.getOffer().toChatFormat() + " verschenken.");
        } else {
            gameSession.sendLogMessage(
                    offeringPlayer + " hat einen Handel gestartet " + tradeOffer.getOffer().toChatFormat() + " gegen " + tradeOffer.getWant().toChatFormat());
        }
    }

    /**
     * This method is called, when every player declines a trade offer
     *
     * @param gameSession    gameSession the player made a trade offer in
     * @param offeringPlayer the player, who wanted to trade.
     */
    private void declinedTradeLog(GameSession gameSession, String offeringPlayer) {
        gameSession.sendLogMessage("Handelsangebot von " + offeringPlayer + " wurde von allen Spielern abgelehnt.");
    }

    /**
     * This method is called, when a player creates a counter offer to a trade offer
     *
     * @param gameSession    gameSession the player makes a counter offer in
     * @param offeringPlayer the player that creates a counter offer
     * @param tradeOffer     the resources of the counter offer the player offers and wants
     */
    private void startCounterTradeLog(GameSession gameSession, String offeringPlayer, TradeOffer tradeOffer) {
        if (tradeOffer.getOffer().sumOfResources() == 0) {
            gameSession.sendLogMessage(
                    offeringPlayer + " hat einen Gegenangebot gestartet und möchte " + tradeOffer.getWant().toChatFormat() + " geschenkt bekommen.");
        } else if (tradeOffer.getWant().sumOfResources() == 0) {
            gameSession.sendLogMessage(
                    offeringPlayer + " hat einen Gegenangebot gestartet und möchte " + tradeOffer.getOffer().toChatFormat() + " verschenken.");
        } else {
            gameSession.sendLogMessage(
                    offeringPlayer + " hat ein Gegenangebot gestartet " + tradeOffer.getOffer().toChatFormat() + " gegen " + tradeOffer.getWant().toChatFormat());
        }
    }

    /**
     * This method handles the trade with the bank.
     * It checks if enough resources have been offered and if the player has any or a special port.
     *
     * @param gameSession the session the player send the bankTradeOffer from
     * @param tradeOffer  amount of wanted and offered resources
     * @param sender      player who wants to trade with the bank
     */
    private void bankTrade(GameSession gameSession, TradeOffer tradeOffer, Player sender) throws TradeException {
        InventoryService inventoryService = gameSession.getInventoryService();
        int bankResources = 0;
        int requiredResources = 0;

        if (tradeOffer.getOffer().get(ResourceType.LUMBER) != 0) {
            if (sender.getPorts().contains(Port.LUMBER)) {
                requiredResources += tradeOffer.getOffer().get(ResourceType.LUMBER) / 2;
            } else if (sender.getPorts().contains(Port.ANY)) {
                requiredResources += tradeOffer.getOffer().get(ResourceType.LUMBER) / 3;
            } else {
                requiredResources += tradeOffer.getOffer().get(ResourceType.LUMBER) / 4;
            }
        }

        if (tradeOffer.getOffer().get(ResourceType.WOOL) != 0) {
            if (sender.getPorts().contains(Port.WOOL)) {
                requiredResources += tradeOffer.getOffer().get(ResourceType.WOOL) / 2;
            } else if (sender.getPorts().contains(Port.ANY)) {
                requiredResources += tradeOffer.getOffer().get(ResourceType.WOOL) / 3;
            } else {
                requiredResources += tradeOffer.getOffer().get(ResourceType.WOOL) / 4;
            }
        }

        if (tradeOffer.getOffer().get(ResourceType.GRAIN) != 0) {
            if (sender.getPorts().contains(Port.GRAIN)) {
                requiredResources += tradeOffer.getOffer().get(ResourceType.GRAIN) / 2;
            } else if (sender.getPorts().contains(Port.ANY)) {
                requiredResources += tradeOffer.getOffer().get(ResourceType.GRAIN) / 3;
            } else {
                requiredResources += tradeOffer.getOffer().get(ResourceType.GRAIN) / 4;
            }
        }

        if (tradeOffer.getOffer().get(ResourceType.ORE) != 0) {
            if (sender.getPorts().contains(Port.ORE)) {
                requiredResources += tradeOffer.getOffer().get(ResourceType.ORE) / 2;
            } else if (sender.getPorts().contains(Port.ANY)) {
                requiredResources += tradeOffer.getOffer().get(ResourceType.ORE) / 3;
            } else {
                requiredResources += tradeOffer.getOffer().get(ResourceType.ORE) / 4;
            }
        }

        if (tradeOffer.getOffer().get(ResourceType.BRICK) != 0) {
            if (sender.getPorts().contains(Port.BRICK)) {
                requiredResources += tradeOffer.getOffer().get(ResourceType.BRICK) / 2;
            } else if (sender.getPorts().contains(Port.ANY)) {
                requiredResources += tradeOffer.getOffer().get(ResourceType.BRICK) / 3;
            } else {
                requiredResources += tradeOffer.getOffer().get(ResourceType.BRICK) / 4;
            }
        }

        bankResources += tradeOffer.getWant().get(ResourceType.LUMBER);
        bankResources += tradeOffer.getWant().get(ResourceType.WOOL);
        bankResources += tradeOffer.getWant().get(ResourceType.GRAIN);
        bankResources += tradeOffer.getWant().get(ResourceType.ORE);
        bankResources += tradeOffer.getWant().get(ResourceType.BRICK);

        if (requiredResources == bankResources) {
            bankTradeResourceExchange(gameSession, tradeOffer, sender, inventoryService);
        } else {
            gameSession.sendBankTradeInvalid(sender.getPlayerName());
            LOG.debug("{} : Wrong trade exchange ratio.", sender.getPlayerName());
        }
    }

    /**
     * This method exchanges the resources between player and bank
     *
     * @param gameSession      the session the player send the bankTradeOffer from
     * @param tradeOffer       amount of wanted and offered resources
     * @param offeringPlayer   player who wants to trade with the bank
     * @param inventoryService service to alter a players inventory
     */
    private void bankTradeResourceExchange(GameSession gameSession, TradeOffer tradeOffer, Player offeringPlayer,
                                           InventoryService inventoryService) throws TradeException {
        try {
            inventoryService.removeResources(offeringPlayer, tradeOffer.getOffer());
            inventoryService.addResources(offeringPlayer, tradeOffer.getWant());
            successfulBankTradeLog(gameSession, offeringPlayer.getPlayerName(), tradeOffer);
        } catch (OverDrawException exception) {
            throw new TradeException();
        }
    }

    /**
     * This method sends a successful bank trade message into the trade log to be seen by every player in the gameSession
     *
     * @param gameSession    gameSession the trade was made in
     * @param offeringPlayer player, who initialized the trade
     * @param tradeOffer     resources that been exchanged
     */
    private void successfulBankTradeLog(GameSession gameSession, String offeringPlayer, TradeOffer tradeOffer) {
        String log = "%s tauscht %s gegen %s mit %s.";
        gameSession.sendLogMessage(
                String.format(log, offeringPlayer, tradeOffer.getOffer().toChatFormat(), tradeOffer.getWant().toChatFormat(), "der Bank"));
    }
}
