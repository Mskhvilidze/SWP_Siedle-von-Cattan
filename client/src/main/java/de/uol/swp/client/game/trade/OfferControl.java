package de.uol.swp.client.game.trade;

import de.uol.swp.client.game.player.PlayerInfo;
import de.uol.swp.common.game.PlayerColor;
import de.uol.swp.common.game.TradeOffer;
import de.uol.swp.common.game.board.ResourceEnumMap;
import de.uol.swp.common.game.board.ResourceType;
import de.uol.swp.common.game.dto.PlayerDTO;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This control is used to display incoming trade offers
 */
public class OfferControl extends Pane {
    private static final Logger LOG = LogManager.getLogger(OfferControl.class);

    private final Map<PlayerColor, PlayerSymbolControl> playerSymbols = new EnumMap<>(PlayerColor.class);
    @FXML
    private ResourceControl offeredLumber;
    @FXML
    private ResourceControl offeredWool;
    @FXML
    private ResourceControl offeredGrain;
    @FXML
    private ResourceControl offeredOre;
    @FXML
    private ResourceControl offeredBrick;
    @FXML
    private ResourceControl requestedLumber;
    @FXML
    private ResourceControl requestedWool;
    @FXML
    private ResourceControl requestedGrain;
    @FXML
    private ResourceControl requestedOre;
    @FXML
    private ResourceControl requestedBrick;
    @FXML
    private Button acceptButton;
    @FXML
    private Button declineButton;
    @FXML
    private Button editButton;
    @FXML
    private Button cancelButton;
    @FXML
    private Pane receivingPane;
    @FXML
    private Pane offeringPane;
    @FXML
    private PlayerSymbolControl offeringSymbol;
    @FXML
    private PlayerSymbolControl firstSymbol;
    @FXML
    private PlayerSymbolControl secondSymbol;
    @FXML
    private PlayerSymbolControl thirdSymbol;

    /**
     * Constructor
     * <p>
     * Enables different buttons depending on whether this client is receiving or offering the trade offer
     *
     * @param receiving  whether this client is receiving the trade offer or is offering it
     * @param tradeOffer the trade offer that this displays
     * @param color      the color of the offering player
     */
    public OfferControl(PlayerDTO[] players, boolean receiving, TradeOffer tradeOffer, PlayerColor color) {
        this();
        receivingPane.setVisible(receiving);
        offeringPane.setVisible(!receiving);
        ResourceEnumMap offer = tradeOffer.getOffer();
        offeredLumber.setCounter(offer.get(ResourceType.LUMBER));
        offeredWool.setCounter(offer.get(ResourceType.WOOL));
        offeredGrain.setCounter(offer.get(ResourceType.GRAIN));
        offeredOre.setCounter(offer.get(ResourceType.ORE));
        offeredBrick.setCounter(offer.get(ResourceType.BRICK));
        ResourceEnumMap request = tradeOffer.getWant();
        requestedLumber.setCounter(request.get(ResourceType.LUMBER));
        requestedWool.setCounter(request.get(ResourceType.WOOL));
        requestedGrain.setCounter(request.get(ResourceType.GRAIN));
        requestedOre.setCounter(request.get(ResourceType.ORE));
        requestedBrick.setCounter(request.get(ResourceType.BRICK));
        offeringSymbol.setColor(color);
        playerSymbols.put(color, offeringSymbol);

        Set<PlayerColor> set = Arrays.stream(players).map(PlayerDTO::getColor).collect(Collectors.toSet());
        set.remove(color);
        int symbolId = 0;
        for (PlayerColor playerColor : set) {
            playerSymbols.put(playerColor, getPlayerSymbol(symbolId));
            getPlayerSymbol(symbolId).setColor(playerColor);
            symbolId++;
        }
        for (int i = symbolId; i < 3; i++) {
            offeringPane.getChildren().remove(getPlayerSymbol(i));
        }
    }

    /**
     * Constructor for an empty OfferControl. Needed for fxml files
     */
    public OfferControl() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/trade/OfferControl.fxml"));
        loader.setRoot(this);
        loader.setController(this);
        try {
            loader.load();
        } catch (IOException exception) {
            LOG.error("OfferControl loading failed at {} because of {}", exception.getMessage(), exception.getCause());
        }
    }

    /**
     * Returns an array containing the symbols of all receiving players
     *
     * @return an array containing the symbols of all receiving players
     */
    public PlayerSymbolControl[] getSymbolsOfReceivingPlayers() {
        return new PlayerSymbolControl[]{firstSymbol, secondSymbol, thirdSymbol};
    }

    /**
     * Returns the PlayerSymbol that is associated with the PlayerColor
     *
     * @param playerColor the PlayerColor whose associated PlayerSymbol is to be returned
     * @return the PlayerSymbol that is associated with the PlayerColor
     */
    public PlayerSymbolControl getPlayerSymbolByPlayerColor(PlayerColor playerColor) {
        return playerSymbols.get(playerColor);
    }

    /**
     * Displays that a given player has declined the trade offer
     *
     * @param declined the player that has declined the trade offer
     */
    public void declinePlayer(PlayerDTO declined) {
        getPlayerSymbolByPlayerColor(declined.getColor()).setDeclined(true);
    }

    /**
     * Displays that a given player has shown interest in the trade offer
     *
     * @param interested the player that has shown interest in the trade offer
     */
    public void interestPlayer(PlayerDTO interested) {
        getPlayerSymbolByPlayerColor(interested.getColor()).setAccepted(true);
    }

    /**
     * sets the accept button to visible if the player has enough resources to fulfill the trade
     *
     * @param playerInfo every information about the player
     * @param tradeOffer resources of the trade offer
     * @return the accept button and its visibility
     */
    public Button getAcceptButton(PlayerInfo playerInfo, TradeOffer tradeOffer) {
        acceptButton.setVisible(playerInfo.getResources().hasResources(tradeOffer.getWant()));
        return acceptButton;
    }

    public Button getDeclineButton() {
        return declineButton;
    }

    public Button getEditButton() {
        return editButton;
    }

    public Button getCancelButton() {
        return cancelButton;
    }

    private PlayerSymbolControl getPlayerSymbol(int symbolId) {
        switch (symbolId) {
            case 0:
                return firstSymbol;
            case 1:
                return secondSymbol;
            case 2:
                return thirdSymbol;
            default:
                throw new IllegalArgumentException();
        }
    }
}
