package de.uol.swp.client.game.trade;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import de.uol.swp.client.game.AbstractGamePresenter;
import de.uol.swp.client.game.GameSessionPresenter;
import de.uol.swp.common.game.PlayerColor;
import de.uol.swp.common.game.TradeOffer;
import de.uol.swp.common.game.dto.PlayerDTO;
import de.uol.swp.common.game.message.trade.NewTradeOfferMessage;
import de.uol.swp.common.game.message.trade.TradeOfferDeclinedMessage;
import de.uol.swp.common.game.message.trade.TradeOfferInterestMessage;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.layout.VBox;

import java.util.Set;

/**
 * The presenter for incoming trade offers. Is included in the {@link GameSessionPresenter}
 * <p>
 * Stacks multiple OfferControl instances on top of each other where each instance represents a trade offer
 */
public class OfferPresenter extends AbstractGamePresenter {

    private final BiMap<OfferControl, TradeOffer> activeOffers = HashBiMap.create();
    @FXML
    private VBox root;

    /**
     * Removes all children of this node. Called by fxml to remove the offer controls created in the fxml file
     */
    public void initialize() {
        root.getChildren().removeAll(root.getChildren());
    }

    private OfferControl getNextOfferPane(boolean receiving, TradeOffer tradeOffer) {
        OfferControl offerControl = new OfferControl(players, receiving, tradeOffer, getPlayerColor(tradeOffer.getOfferingPlayer()));
        if (receiving) {
            offerControl.getAcceptButton(playerInfo, tradeOffer).setOnMouseClicked(event -> {
                offerControl.getAcceptButton(playerInfo, tradeOffer).setDisable(true);
                offerControl.getDeclineButton().setDisable(true);
                offerControl.getEditButton().setDisable(true);
                gameSessionService.interestTradeOffer(gameSessionName, activeOffers.get(offerControl));
            });
            offerControl.getDeclineButton().setOnMouseClicked(event -> {
                offerControl.getAcceptButton(playerInfo, tradeOffer).setDisable(true);
                offerControl.getDeclineButton().setDisable(true);
                offerControl.getEditButton().setDisable(true);
                gameSessionService.declineTradeOffer(gameSessionName, activeOffers.get(offerControl));
            });
            offerControl.getEditButton().setOnMouseClicked(
                    event -> gameSessionPresenter.openCounterOfferTradeWindow(activeOffers.get(offerControl)));
        } else {
            offerControl.getCancelButton().setOnMouseClicked(
                    event -> gameSessionService.cancelTradeOffer(gameSessionName, activeOffers.get(offerControl)));

            Set<PlayerSymbolControl> playerSymbols = Set.of(offerControl.getSymbolsOfReceivingPlayers());
            playerSymbols.forEach(playerSymbol -> {
                for (PlayerDTO player : players) {
                    if (player.getColor() == playerSymbol.getPlayerColor() &&
                            (tradeOffer.getReceivingPlayer() == null || player.getPlayerName().equals(tradeOffer.getReceivingPlayer()))) {
                        playerSymbol.setOnMouseClicked(event -> {
                            if (playerSymbol.isAccepted()) {
                                gameSessionService.acceptTradeOffer(gameSessionName, activeOffers.get(offerControl), player.getPlayerName());
                            }
                        });
                        playerSymbol.setVisible(true);
                        break;
                    } else {
                        playerSymbol.setVisible(false);
                    }
                }
            });
        }
        Platform.runLater(() -> root.getChildren().add(offerControl));
        return offerControl;
    }

    /**
     * Displays a new trade offer and initializes its OfferControl
     *
     * @param message the {@code NewTradeOfferMessage} that signals an incoming trade offer
     */
    public void displayNewTradeOffer(NewTradeOfferMessage message) {
        TradeOffer tradeOffer = message.getTradeOffer();
        OfferControl control = getNextOfferPane(!tradeOffer.getOfferingPlayer().equals(userInfo.getLoggedInUser().getUsername()), tradeOffer);
        activeOffers.put(control, tradeOffer);
    }

    /**
     * Removes a trade offer and its OfferControl
     *
     * @param tradeOffer the trade offer that is to be removed
     */
    public void removeTradeOffer(TradeOffer tradeOffer) {
        OfferControl control = activeOffers.inverse().get(tradeOffer);
        Platform.runLater(() -> root.getChildren().remove(control));
        activeOffers.remove(control);
    }

    /**
     * Displays the interest of a receiving player in the corresponding OfferControl
     *
     * @param message the {@code TradeOfferInterestMessage} that signals the interest
     */
    public void displayTradeOfferInterest(TradeOfferInterestMessage message) {
        TradeOffer tradeOffer = message.getTradeOffer();
        OfferControl control = activeOffers.inverse().get(tradeOffer);
        control.interestPlayer(message.getInterestedPlayer());
    }

    /**
     * Displays the refusal of a receiving player in the corresponding OfferControl
     *
     * @param message the {@code TradeOfferDeclinedMessage} that signals a refusal
     */
    public void displayTradeOfferDecline(TradeOfferDeclinedMessage message) {
        OfferControl control = activeOffers.inverse().get(message.getTradeOffer());
        control.declinePlayer(message.getDeclined());
    }

    /**
     * Removes all active OfferControls from this pane
     */
    public void clearAllTrades() {
        BiMap<OfferControl, TradeOffer> tempOffers = HashBiMap.create(activeOffers);
        for (OfferControl control : tempOffers.keySet()) {
            Platform.runLater(() -> root.getChildren().remove(control));
            activeOffers.remove(control);
        }
    }

    /**
     * getter for the color of the player
     *
     * @param playerName name of the player
     * @return color of the player
     */
    private PlayerColor getPlayerColor(String playerName) {
        for (PlayerDTO player : players) {
            if (player.getPlayerName().equals(playerName)) {
                return player.getColor();
            }
        }
        return null;
    }
}
