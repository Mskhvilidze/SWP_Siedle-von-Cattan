package de.uol.swp.server.game.state;

import de.uol.swp.common.game.board.Coord;
import de.uol.swp.common.game.board.PieceType;
import de.uol.swp.common.message.RequestMessage;
import de.uol.swp.server.game.Player;
import de.uol.swp.server.game.board.Board;
import de.uol.swp.server.game.board.BoardUtils;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Stores import context for the current state like the last request sent by a player
 */
public class StateContext {
    private final Map<Player, Boolean> discardedBefore = new HashMap<>();
    private final ReentrantLock inputLock = new ReentrantLock();
    /*
    Gehört hier nicht wirklich rein weil dass ja eher TurnContext ist.
    Sollten später noch mehr Sachen dazu kommen die über mehrere States hinweg
    gespeichert werden müssen, dann kann mans ja noch verschieben
     */
    private boolean devCardPlayedBeforeDice = false;
    private boolean setupPhaseCompleted = false;
    private boolean robberPlaced;
    private RequestMessage lastRequest;
    private int freeBuildableRoads = 0;
    private Player playerWithLargestArmy;
    private Map<Player, SetupInfo> setupInfos = new HashMap<>();
    private int setupPhase = 0;
    private int nextDiceResult = -1;

    /**
     * Returns the lock that is being used to prevent user input from breaking the game state
     *
     * @return the lock that is being used to prevent user input from breaking the game state
     */
    public ReentrantLock getInputLock() {
        return inputLock;
    }

    /**
     * Clears the hasDiscardedMap for use in the next discard Phase
     */
    public void initDiscardMap(Player[] players) {
        this.discardedBefore.clear();
        for (Player player : players) {
            discardedBefore.put(player, player.getNumOfResourcesToDiscardFromInventory() <= 0);
        }
    }

    /**
     * Map Of players that have already discarded Resources
     *
     * @return map of players that have already discarded Resources
     */
    public Map<Player, Boolean> getDiscardedBefore() {
        return discardedBefore;
    }

    /**
     * Returns whether the current player has played a dev card before throwing their dice
     *
     * @return {@code true} if the current player has played a dev card before throwing their dice
     */
    public boolean isDevCardPlayedBeforeDice() {
        return devCardPlayedBeforeDice;
    }

    /**
     * Sets whether the current player has played a dev card before throwing their dice.
     * Should only be set from the {@link DiceState}
     *
     * @param devCardPlayedBeforeDice has the player played a dev card?
     */
    public void setDevCardPlayedBeforeDice(boolean devCardPlayedBeforeDice) {
        this.devCardPlayedBeforeDice = devCardPlayedBeforeDice;
    }

    /**
     * Returns the request that influences the current state
     *
     * @return the request that influences the current state
     */
    public RequestMessage getLastRequest() {
        return lastRequest;
    }

    /**
     * Sets the request that influences the current state
     *
     * @param lastRequest the request that influences the current state
     */
    public void setLastRequest(RequestMessage lastRequest) {
        this.lastRequest = lastRequest;
    }

    /**
     * Getter for the setup phase boolean
     *
     * @return true if the setup phase is completed
     */
    public boolean isSetupPhaseCompleted() {
        return setupPhaseCompleted;
    }

    /**
     * If the setup phase is completed this is set to true
     *
     * @param setupPhaseCompleted true if the setup phase is completed
     */
    public void setSetupPhaseCompleted(boolean setupPhaseCompleted) {
        this.setupPhaseCompleted = setupPhaseCompleted;
    }

    /**
     * Getter for map containing every player and the amount of placements they made in the setup phase.
     *
     * @return map of every player and the amount of placements they made in the setup phase.
     */
    public Map<Player, SetupInfo> getSetupInfos() {
        return setupInfos;
    }

    /**
     * Setter for the map of every player and the amount of placements they made in the setup phase.
     *
     * @param setupInfos map of every player and the amount of placements in setupPhase
     */
    public void setSetupInfos(Map<Player, SetupInfo> setupInfos) {
        this.setupInfos = setupInfos;
    }

    /**
     * Returns the current phase of the setup state
     *
     * @return the current phase of the setup state
     * @see SetupInfo#isBehind
     */
    public int getSetupPhase() {
        return setupPhase;
    }

    /**
     * Sets the current phase of the setup state. Should be increased everytime the last player placed their road
     *
     * @param setupPhase the current phase of the setup state
     */
    public void setSetupPhase(int setupPhase) {
        this.setupPhase = setupPhase;
    }

    /**
     * Gets the amount of freeBuildableRoads
     */
    public int getFreeBuildableRoads() {
        return freeBuildableRoads;
    }

    /**
     * Sets the amount of freeBuildableRoads
     *
     * @param freeBuildableRoads amount of freeBuildableRoads
     */
    public void setFreeBuildableRoads(int freeBuildableRoads) {
        this.freeBuildableRoads = freeBuildableRoads;
    }

    /**
     * Returns the player with the most played KnightCards
     *
     * @return player with the most knight cards played
     */
    public Player getPlayerWithLargestArmy() {
        return playerWithLargestArmy;
    }

    /**
     * Sets the player with the most played KnightCards
     *
     * @param playerWithLargestArmy
     */
    public void setPlayerWithLargestArmy(Player playerWithLargestArmy) {
        this.playerWithLargestArmy = playerWithLargestArmy;
    }

    /**
     * Getter for the robberplaced Value
     * While in teh Placing State the robber can be placed but the players choice on who to robb is still pending
     *
     * @return true if the robber is already placed
     */
    public boolean isRobberPlaced() {
        return this.robberPlaced;
    }

    /**
     * Setter for the robberPlaced Value
     *
     * @param value
     */
    public void setRobberPlaced(boolean value) {
        this.robberPlaced = value;
    }

    /**
     * Returns the next dice result
     *
     * @return the next dice result
     */
    public int getNextDiceResult() {
        return nextDiceResult;
    }

    /**
     * Sets the next dice result
     *
     * @param nextDiceResult the next dice result
     */
    public void setNextDiceResult(int nextDiceResult) {
        this.nextDiceResult = nextDiceResult;
    }

    /**
     * Stores important player information about the setup state
     */
    public static class SetupInfo {

        private final Deque<Coord> placedSettlements = new ArrayDeque<>();
        private final Deque<Coord> placedRoads = new ArrayDeque<>();

        /**
         * Returns a list of the valid roads that the player can place their roads at
         *
         * @param board the current board
         * @return a list of the valid roads that the player can place their roads at
         */
        public List<Coord> getRoads(Board board) {
            List<Coord> roads = new ArrayList<>();
            if (!placedSettlements.isEmpty()) {
                roads.addAll(BoardUtils.getEdgeNeighboursFromCornerWithoutWater(board, placedSettlements.peek()));
            }
            return roads;
        }

        /**
         * Returns whether the player has placed all their pieces in the setup state
         *
         * @param maxAmountOfPlacements the max amount of placements the player can do
         * @return {@code true} if the player has placed all their pieces in the setup state
         */
        public boolean isDone(int maxAmountOfPlacements) {
            return placedSettlements.size() + placedRoads.size() >= maxAmountOfPlacements;
        }

        /**
         * Returns the piece type that the player has to place
         *
         * @return the piece type that the player has to place
         */
        public PieceType nextPiece() {
            if (placedSettlements.size() > placedRoads.size()) {
                return PieceType.ROAD;
            } else {
                return PieceType.SETTLEMENT;
            }
        }

        /**
         * Returns whether the player has already placed their first settlement
         *
         * @return {@code true} if the player has already placed their first settlement
         */
        public boolean isSecondSettlement() {
            return placedSettlements.size() == 1;
        }

        /**
         * Stores the given position of the given piece to control road placement
         *
         * @param pieceType the piece type of the placed piece
         * @param coord     the position of the placed piece
         */
        public void addPiece(PieceType pieceType, Coord coord) {
            if (pieceType == PieceType.SETTLEMENT) {
                placedSettlements.addFirst(coord);
            } else if (pieceType == PieceType.ROAD) {
                placedRoads.addFirst(coord);
            }
        }

        /**
         * Returns whether the player is behind the other players.
         * <p>
         * Should be used to check if the server has to place the players pieces at the end of the turn
         *
         * @param context the state context of the current game session
         * @return {@code true} if the player has not placed enough pieces for the current {@link StateContext#getSetupPhase()}
         */
        public boolean isBehind(StateContext context) {
            if (context.getSetupPhase() == 0 && placedSettlements.size() + placedRoads.size() < 2) {
                return true;
            } else {
                return context.getSetupPhase() == 1 && placedSettlements.size() + placedRoads.size() < 4;
            }
        }
    }
}
