package de.uol.swp.client.game.player;

import de.uol.swp.client.game.AbstractGamePresenter;
import de.uol.swp.client.game.GameSessionPresenter;
import de.uol.swp.common.game.dto.PlayerDTO;
import de.uol.swp.common.game.message.VPUpdateMessage;
import de.uol.swp.common.game.message.inventory.DevCardCountMessage;
import de.uol.swp.common.game.message.inventory.ResourceCardCountMessage;
import javafx.application.Platform;
import javafx.fxml.FXML;


/**
 * Main presenter for the ingame profile
 */
public class GameProfilePresenter extends AbstractGamePresenter {

    @FXML
    private ProfileElement playerOneElement;
    @FXML
    private ProfileElement playerTwoElement;
    @FXML
    private ProfileElement playerThreeElement;
    @FXML
    private ProfileElement playerFourElement;

    /**
     * Method to set the VBox of the players to visible and set
     * the player name and the color of the name
     */
    public void initialisePlayers() {
        if (players.length > 0) {
            playerOneElement.setPlayerName(players[0].getPlayerName());
            playerOneElement.setPlayerColor(GameSessionPresenter.getFXColorFromPlayerColor(players[0].getColor()));
            playerOneElement.setVisible(true);
        }
        if (players.length > 1) {
            playerTwoElement.setPlayerName(players[1].getPlayerName());
            playerTwoElement.setPlayerColor(GameSessionPresenter.getFXColorFromPlayerColor(players[1].getColor()));
            playerTwoElement.setVisible(true);

        }
        if (players.length > 2) {
            playerThreeElement.setPlayerName(players[2].getPlayerName());
            playerThreeElement.setPlayerColor(GameSessionPresenter.getFXColorFromPlayerColor(players[2].getColor()));
            playerThreeElement.setVisible(true);
        }
        if (players.length > 3) {
            playerFourElement.setPlayerName(players[3].getPlayerName());
            playerFourElement.setPlayerColor(GameSessionPresenter.getFXColorFromPlayerColor(players[3].getColor()));
            playerFourElement.setVisible(true);
        }
    }

    /**
     * Gets the correct Profile Element from player name
     *
     * @param playerName player name
     * @return the Profile Element
     */
    public ProfileElement getProfileElement(String playerName) {
        if (playerOneElement.getPlayerName().equals(playerName)) {
            return playerOneElement;
        } else if (playerTwoElement.getPlayerName().equals(playerName)) {
            return playerTwoElement;
        } else if (playerThreeElement.getPlayerName().equals(playerName)) {
            return playerThreeElement;
        } else if (playerFourElement.getPlayerName().equals(playerName)) {
            return playerFourElement;
        } else {
            throw new IllegalArgumentException("Keine valide player id");
        }
    }

    /**
     * Method of setting the development cards to the value of the resource allocation from the
     * Message for every player in the game
     *
     * @param message the DevCardCountMessage
     */
    public void setDevCardLabels(DevCardCountMessage message) {
        Platform.runLater(() -> {
            for (PlayerDTO player : players) {
                if (player.equals(message.getPlayer())) {
                    getProfileElement(player.getPlayerName()).setDevCardAmountLabel(message.getCount());
                }
            }
        });
    }

    /**
     * Method to set the resource labels to the value of the resource map from the
     * message for each player in the game
     *
     * @param message the ResourceCardCountMessage wich contains the resourceEnumMap
     */
    public void setResourceLabels(ResourceCardCountMessage message) {
        int resources = message.getResourceEnumMap().sumOfResources();
        Platform.runLater(() -> {
            for (PlayerDTO player : players) {
                if (player.equals(message.getPlayer())) {
                    getProfileElement(player.getPlayerName()).setResourceAmountLabel(resources);
                    break;
                }
            }
        });
    }

    /**
     * The method to set the VictoryPoints in the game profile for each player in the game
     *
     * @param message the the VPUpdateMessage which contains the amount of victory points
     */
    public void setVPLabels(VPUpdateMessage message) {
        int vps = message.getVps();
        Platform.runLater(() -> {
            for (PlayerDTO player : players) {
                if (player.equals(message.getPlayer())) {
                    getProfileElement(player.getPlayerName()).setVictoryPointLabel(vps);
                    break;
                }
            }
        });
    }


    /**
     * Turn the knight display on or off according to the player supplied
     *
     * @param playerWithArmyBonus player with army bonus
     */
    public void displayKnight(PlayerDTO playerWithArmyBonus) {
        Platform.runLater(() -> {
            for (PlayerDTO player : players) {
                getProfileElement(player.getPlayerName()).setKnightBonusDisplayVisible(
                        player.getPlayerName().equals(playerWithArmyBonus.getPlayerName()));
            }
        });
    }

    /**
     * Turn the icon of the longest Player road on or off.
     *
     * @param playerWithLongestRoad the player with the longest road
     */
    public void displayLongestRoad(PlayerDTO playerWithLongestRoad) {
        Platform.runLater(() -> {
            for (PlayerDTO player : players) {
                getProfileElement(player.getPlayerName()).setLongestRoadBonusVisible(player.equals(playerWithLongestRoad));
            }
        });
    }
}
