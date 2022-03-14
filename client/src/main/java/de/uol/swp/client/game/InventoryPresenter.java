package de.uol.swp.client.game;

import de.uol.swp.client.game.trade.ResourceControl;
import de.uol.swp.common.game.board.DevCard;
import de.uol.swp.common.game.board.ResourceType;
import de.uol.swp.common.game.message.inventory.DevCardDetailedCountMessage;
import de.uol.swp.common.game.message.inventory.ResourceCardCountMessage;
import de.uol.swp.common.game.request.IsUsingCardAllowedRequest;
import de.uol.swp.common.game.request.UseCardRequest;
import de.uol.swp.common.game.response.IsUsingCardAllowedResponse;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.util.Map;


/**
 * Displays the Inventory in a game
 */
public class InventoryPresenter extends AbstractGamePresenter {
    @FXML
    private ResourceControl lumber;
    @FXML
    private ResourceControl wool;
    @FXML
    private ResourceControl grain;
    @FXML
    private ResourceControl ore;
    @FXML
    private ResourceControl brick;
    @FXML
    private ResourceControl victoryPoint;
    @FXML
    private ResourceControl knight;
    @FXML
    private ResourceControl yearOfPlenty;
    @FXML
    private ResourceControl monopoly;
    @FXML
    private ResourceControl road;
    @FXML
    private ImageView victoryPointImage;
    @FXML
    private ImageView knightImage;
    @FXML
    private ImageView yearOfPlentyImage;
    @FXML
    private ImageView monopolyImage;
    @FXML
    private ImageView roadImage;
    @FXML
    private Label lumberDiff;
    @FXML
    private Label woolDiff;
    @FXML
    private Label grainDiff;
    @FXML
    private Label oreDiff;
    @FXML
    private Label brickDiff;

    @FXML
    private void showCard(MouseEvent event) {
        ResourceControl card = ((ResourceControl) event.getSource());
        if (card.equals(knight)) {
            knightImage.setVisible(true);
        } else if (card.equals(road)) {
            roadImage.setVisible(true);
        } else if (card.equals(yearOfPlenty)) {
            yearOfPlentyImage.setVisible(true);
        } else if (card.equals(monopoly)) {
            monopolyImage.setVisible(true);
        } else if (card.equals(victoryPoint)) {
            victoryPointImage.setVisible(true);
        }
    }

    @FXML
    private void hideCard(MouseEvent event) {
        ResourceControl card = ((ResourceControl) event.getSource());
        if (card.equals(knight)) {
            knightImage.setVisible(false);
        } else if (card.equals(road)) {
            roadImage.setVisible(false);
        } else if (card.equals(yearOfPlenty)) {
            yearOfPlentyImage.setVisible(false);
        } else if (card.equals(monopoly)) {
            monopolyImage.setVisible(false);
        } else if (card.equals(victoryPoint)) {
            victoryPointImage.setVisible(false);
        }
    }

    @FXML
    private void playCard(MouseEvent event) {
        ResourceControl card = ((ResourceControl) event.getSource());
        if (card.equals(knight)) {
            eventBus.post(new IsUsingCardAllowedRequest(gameSessionName, DevCard.KNIGHT));
        } else if (card.equals(road)) {
            eventBus.post(new IsUsingCardAllowedRequest(gameSessionName, DevCard.ROAD_BUILDING));
        } else if (card.equals(yearOfPlenty)) {
            eventBus.post(new IsUsingCardAllowedRequest(gameSessionName, DevCard.YEAR_OF_PLENTY));
        } else if (card.equals(monopoly)) {
            eventBus.post(new IsUsingCardAllowedRequest(gameSessionName, DevCard.MONOPOLY));
        }
    }

    /**
     * this method either sends a UseCardRequest for a devCard after the confirmation of the server, that the user is allowed to play the card
     * or gives out the reason why the user can not use the card
     *
     * @param response the IsUsingCardAllowedResponse sent from the server to (not) allow the use of a devCard
     */
    public void playCard(IsUsingCardAllowedResponse response) {
        if (response.isAllowed()) {
            DevCard devCard = response.getDevCard();
            switch (devCard) {
                case KNIGHT:
                    eventBus.post(new UseCardRequest(gameSessionName, DevCard.KNIGHT, null));
                    break;
                case ROAD_BUILDING:
                    eventBus.post(new UseCardRequest(gameSessionName, DevCard.ROAD_BUILDING, null));
                    break;
                case YEAR_OF_PLENTY:
                    gameSessionPresenter.onToggleYearOfPlentyCard();
                    break;
                case MONOPOLY:
                    gameSessionPresenter.onToggleMonopolyCard();
                    break;
                case VP:
                    break;
                default:
                    throw new IllegalArgumentException("Enum has not been accounted for");
            }
        } else {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR, response.getReason());
                alert.showAndWait();
            });
        }
    }

    private ResourceControl getResourceControl(DevCard devCard) {
        switch (devCard) {
            case VP:
                return victoryPoint;
            case KNIGHT:
                return knight;
            case YEAR_OF_PLENTY:
                return yearOfPlenty;
            case MONOPOLY:
                return monopoly;
            case ROAD_BUILDING:
                return road;
            default:
                throw new IllegalStateException();
        }
    }

    private ResourceControl getResourceControl(ResourceType resourceType) {
        switch (resourceType) {
            case LUMBER:
                return lumber;
            case WOOL:
                return wool;
            case GRAIN:
                return grain;
            case ORE:
                return ore;
            case BRICK:
                return brick;
            default:
                throw new IllegalStateException();
        }
    }

    private void filterCardsToBeHighlighted(Map<?, Integer> map, boolean isResource) {
        ParallelTransition pt = new ParallelTransition();
        for (Map.Entry<?, Integer> entry : map.entrySet()) {
            ResourceControl resourceControl;
            if (isResource) {
                resourceControl = getResourceControl((ResourceType) entry.getKey());
            } else {
                resourceControl = getResourceControl((DevCard) entry.getKey());
            }
            if (entry.getValue() - resourceControl.getCounter() != 0) {
                highlightCard(resourceControl, entry.getValue() - resourceControl.getCounter(), isResource, pt);
            }
        }
        pt.play();
    }

    private void highlightCard(ResourceControl resourceControl, int i, boolean isResource, ParallelTransition parallelTransition) {
        if (i == 0) {
            return;
        }
        int depth = 70;
        String diff;
        Color color;
        if (i > 0) {
            color = Color.GREEN;
            diff = "+" + i;
        } else {
            diff = "" + i;
            color = Color.RED;
        }
        DropShadow borderGlow = new DropShadow(depth, color);
        resourceControl.setEffect(borderGlow);
        ScaleTransition st = new ScaleTransition(Duration.seconds(0.75), resourceControl);
        st.setCycleCount(4);
        st.setAutoReverse(true);
        st.setFromX(0.95);
        st.setFromY(0.95);
        st.setToX(1.05);
        st.setToY(1.05);
        parallelTransition.getChildren().add(st);
        PauseTransition wait = new PauseTransition(Duration.seconds(3));
        wait.setOnFinished(event -> {
            resourceControl.setEffect(null);
            if (isResource) {
                getCounterLabel(resourceControl).setOpacity(0);
            }
        });
        parallelTransition.getChildren().add(wait);
        if (isResource) {
            Label label = getCounterLabel(resourceControl);
            Platform.runLater(() -> {
                label.setText(diff);
                label.setOpacity(1);
            });
        }
    }

    private Label getCounterLabel(ResourceControl resourceControl) {
        if (resourceControl.equals(lumber)) {
            return lumberDiff;
        } else if (resourceControl.equals(wool)) {
            return woolDiff;
        } else if (resourceControl.equals(grain)) {
            return grainDiff;
        } else if (resourceControl.equals(ore)) {
            return oreDiff;
        } else {
            return brickDiff;
        }
    }

    /**
     * Checks what resource is received an adds it to inventory
     *
     * @param message the ResourceCardCountMessage that updates the inventory
     */
    public void updateResourceCount(ResourceCardCountMessage message) {
        filterCardsToBeHighlighted(message.getResourceEnumMap(), true);
        Platform.runLater(() -> {
            playerInfo.setResources(message.getResourceEnumMap());
            lumber.setCounter(message.getResourceEnumMap().get(ResourceType.LUMBER));
            wool.setCounter(message.getResourceEnumMap().get(ResourceType.WOOL));
            grain.setCounter(message.getResourceEnumMap().get(ResourceType.GRAIN));
            ore.setCounter(message.getResourceEnumMap().get(ResourceType.ORE));
            brick.setCounter(message.getResourceEnumMap().get(ResourceType.BRICK));
        });
    }

    /**
     * Checks what devCard is received and adds it to inventory
     *
     * @param message the DevCardDetailedCountMessage that updates the inventory
     */
    public void updateDevCardCount(DevCardDetailedCountMessage message) {
        filterCardsToBeHighlighted(message.getDevCards(), false);
        Platform.runLater(() -> {
            knight.setCounter(message.getDevCards().get(DevCard.KNIGHT));
            road.setCounter(message.getDevCards().get(DevCard.ROAD_BUILDING));
            yearOfPlenty.setCounter(message.getDevCards().get(DevCard.YEAR_OF_PLENTY));
            monopoly.setCounter(message.getDevCards().get(DevCard.MONOPOLY));
            victoryPoint.setCounter(message.getDevCards().get(DevCard.VP));
        });
    }

    /**
     * Returns the lumber counter property
     *
     * @return the lumber counter property
     */
    public IntegerProperty lumberProperty() {
        return lumber.counterProperty();
    }

    /**
     * Returns the wool counter property
     *
     * @return the wool counter property
     */
    public IntegerProperty woolProperty() {
        return wool.counterProperty();
    }

    /**
     * Returns the grain counter property
     *
     * @return the grain counter property
     */
    public IntegerProperty grainProperty() {
        return grain.counterProperty();
    }

    /**
     * Returns the ore counter property
     *
     * @return the ore counter property
     */
    public IntegerProperty oreProperty() {
        return ore.counterProperty();
    }

    /**
     * Returns the brick counter property
     *
     * @return the brick counter property
     */
    public IntegerProperty brickProperty() {
        return brick.counterProperty();
    }
}

