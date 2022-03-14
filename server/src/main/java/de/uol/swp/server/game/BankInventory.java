package de.uol.swp.server.game;

import de.uol.swp.common.game.board.DevCard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This class stores the cards of the Bank
 */
public class BankInventory {
    private final List<DevCard> devCards = new ArrayList<>();

    /**
     * Constructs an inventory with a filled and shuffled development cards list
     */
    public BankInventory() {
        for (int i = 0; i < 5; i++) {
            devCards.add(DevCard.VP);
        }
        for (int i = 0; i < 14; i++) {
            devCards.add(DevCard.KNIGHT);
        }
        for (int i = 0; i < 2; i++) {
            devCards.add(DevCard.YEAR_OF_PLENTY);
        }
        for (int i = 0; i < 2; i++) {
            devCards.add(DevCard.MONOPOLY);
        }
        for (int i = 0; i < 2; i++) {
            devCards.add(DevCard.ROAD_BUILDING);
        }
        Collections.shuffle(devCards);
    }

    /**
     * Removes a development card from the bank inventory and returns it
     *
     * @return the removed development card
     */
    public DevCard getAndRemoveRandomDevCard() {
        if (!devCards.isEmpty()) {
            return devCards.remove(0);
        } else {
            return null;
        }
    }

    /**
     * Getter for the amount of Cards remaining in the bank
     *
     * @return the amount of cards in the bank
     */
    public int getAmountRemaining() {
        return devCards.size();
    }
}
