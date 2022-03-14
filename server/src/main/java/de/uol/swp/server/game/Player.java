package de.uol.swp.server.game;

import de.uol.swp.common.game.Inventory;
import de.uol.swp.common.game.PlayerColor;
import de.uol.swp.common.game.TradeOffer;
import de.uol.swp.common.game.board.*;
import de.uol.swp.common.game.dto.MapNode;
import de.uol.swp.common.game.dto.PlayerDTO;
import de.uol.swp.server.game.board.Board;
import de.uol.swp.server.game.board.BoardUtils;
import de.uol.swp.server.game.mapnode.NodeOffsetHelper;
import de.uol.swp.server.game.mapobject.BuildablePiece;
import de.uol.swp.server.game.mapobject.RoadPiece;
import de.uol.swp.server.game.mapobject.SettlementPiece;
import de.uol.swp.server.game.session.GameSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Contains all player information in a game session
 */
public class Player {

    private static final Logger LOG = LogManager.getLogger(Player.class);
    private final Random rand = new Random();
    private final GameSession gameSession;
    private final Board board;
    private final int playerId;
    private final Set<Coord> legalRoads = new HashSet<>();
    private final Set<Coord> legalSettlements = new HashSet<>();
    private final Set<Coord> legalCities = new HashSet<>();
    private final Set<Coord> currentCornerPieces = new HashSet<>();
    private final Inventory inventory;
    private final Set<Port> ports = EnumSet.noneOf(Port.class);
    private final Set<TradeOffer> openTradeOffers = new HashSet<>();
    private final Map<TradeOffer, TradeOffer> counterTradeOffers = new HashMap<>();

    private final Set<TradeOffer> declinedTradeOffers = new HashSet<>();
    private final Set<TradeOffer> interestTradeOffers = new HashSet<>();
    private String playerName;
    private boolean bot;
    private int numOfBuildingVP = 0;
    private PlayerColor color;
    private int numOfKnights = 0;
    private boolean playedDevCardThisTurn = false;

    /**
     * Constructor
     *
     * @param gameSession of the player
     * @param playerName  the name of the player
     * @param playerId    the id of the player
     */
    public Player(GameSession gameSession, String playerName, int playerId, boolean bot) {
        this.gameSession = gameSession;
        this.board = gameSession.getBoard();
        this.playerName = playerName;
        this.playerId = playerId;
        this.inventory = new Inventory();
        this.bot = bot;

        initLegalMovesList();
    }

    /**
     * Returns whether this player is a bot
     *
     * @return {@code true} if this player is a bot, otherwise {@code false}
     */
    public boolean isBot() {
        return bot;
    }

    /**
     * Fill the legal moves attributes with every legal settlement move.
     * At the start of the game every settlement move is legal
     */
    private void initLegalMovesList() {
        legalSettlements.clear();
        legalSettlements.addAll(board.getAllCornerCoords());
    }


    /**
     * Adds the given trade offer to the players open trade offers
     *
     * @param tradeOffer the new trade offer of the player
     */
    public void addTradeOffer(TradeOffer tradeOffer) {
        openTradeOffers.add(tradeOffer);
        LOG.debug("Trade Offer added to user {}: {} | {}", playerName, tradeOffer.getOffer(), tradeOffer.getWant());
    }

    /**
     * Adds the given counter trade offer to the players open trade offers
     * and remembers the connection to the original trade offer
     *
     * @param oldTradeOffer the original trade offer of the player
     * @param newTradeOffer the counter trade offer to the original trade offer
     */
    public void addCounterTradeOffer(TradeOffer oldTradeOffer, TradeOffer newTradeOffer) {
        openTradeOffers.add(newTradeOffer);
        counterTradeOffers.put(newTradeOffer, oldTradeOffer);
        LOG.debug("Counter Trade Offer added to user {}: {} | {}", playerName, newTradeOffer.getOffer(), newTradeOffer.getWant());
    }

    /**
     * Removes the given trade offer from the players open trade offers. This includes removing the declined and interested trade offers
     *
     * @param tradeOffer the trade offer that should be removed
     * @return a {@code Set} containing the counter trade offers to the given trade offer
     */
    public Set<TradeOffer> removeTradeOffer(TradeOffer tradeOffer) {
        Set<TradeOffer> counters = new HashSet<>();
        if (openTradeOffers.contains(tradeOffer) || declinedTradeOffers.contains(tradeOffer) || interestTradeOffers.contains(tradeOffer)) {
            //TODO: Multimap für counter trade offer?
            openTradeOffers.remove(tradeOffer);
            declinedTradeOffers.remove(tradeOffer);
            interestTradeOffers.remove(tradeOffer);
            LOG.debug("Trade Offer removed from user {}: {} | {}", playerName, tradeOffer.getOffer(), tradeOffer.getWant());
            counterTradeOffers.entrySet().stream().filter(entry -> entry.getValue().equals(tradeOffer)).forEach(
                    entry -> counters.add(entry.getKey()));
        }
        return counters;
    }

    /**
     * Removes the given counter trade offer from the players open trade offers. This includes removing the declined and interested trade offers
     *
     * @param tradeOffer the trade offer that should be removed
     * @return the original trade offer of the counter trade offer
     */
    public TradeOffer removeCounterTradeOffer(TradeOffer tradeOffer) {
        if (openTradeOffers.contains(tradeOffer) || declinedTradeOffers.contains(tradeOffer) || interestTradeOffers.contains(tradeOffer)) {
            //TODO: Multimap für counter trade offer?
            openTradeOffers.remove(tradeOffer);
            declinedTradeOffers.remove(tradeOffer);
            interestTradeOffers.remove(tradeOffer);
            LOG.debug("Trade Offer removed from user {}: {} | {}", playerName, tradeOffer.getOffer(), tradeOffer.getWant());

            if (counterTradeOffers.containsKey(tradeOffer)) {
                TradeOffer counter = counterTradeOffers.remove(tradeOffer);
                LOG.debug("Counter Trade Offer removed from user {}: {} | {}", playerName, counter.getOffer(), counter.getWant());
                return counter;
            }
        }
        return null;
    }

    /**
     * Adds the given trade offer to the players declined trade offers
     *
     * @param tradeOffer the trade offer that the player has declined
     */
    public void declineTradeOffer(TradeOffer tradeOffer) {
        declinedTradeOffers.add(tradeOffer);
        LOG.debug("Trade Offer declined by user {}: {} | {}", playerName, tradeOffer.getOffer(), tradeOffer.getWant());
    }

    /**
     * Adds the given trade offer to the players interested trade offers
     *
     * @param tradeOffer the trade offer that the player is interested in
     */
    public void interestTradeOffer(TradeOffer tradeOffer) {
        interestTradeOffers.add(tradeOffer);
        LOG.debug("Trade Offer interest by user {}: {} | {}", playerName, tradeOffer.getOffer(), tradeOffer.getWant());
    }

    /**
     * Returns all interest and open trade offers
     *
     * @return all interest and open trade offers
     */
    public Set<TradeOffer> getTradeOffers() {
        Set<TradeOffer> returnSet = new HashSet<>(interestTradeOffers);
        returnSet.addAll(openTradeOffers);
        return returnSet;
    }

    /**
     * checks if a player has already played a dev card this turn
     *
     * @return the flag for if a player has played a dev card this turn
     */
    public boolean hasPlayedDevCardThisTurn() {
        return playedDevCardThisTurn;
    }

    /**
     * setter for playedDevCardThisTurn
     *
     * @param playedDevCardThisTurn the flag for if a player has played a dev card this turn
     */
    public void setPlayedDevCardThisTurn(boolean playedDevCardThisTurn) {
        this.playedDevCardThisTurn = playedDevCardThisTurn;
    }

    /**
     * this method is called when a player is exchanged by a bot
     *
     * @param name the name of the bot
     */
    public void setPlayerToBot(String name) {
        this.playerName = name;
        this.bot = true;
    }

    /**
     * this method is called when a bot is exchanged by a player
     *
     * @param name the name of the bot
     */
    public void setBotToPlayer(String name) {
        this.bot = false;
        this.playerName = name;
    }


    /**
     * Returns whether this player has any open trade offers
     *
     * @return {@code true} if this player has any open trade offers, otherwise {@code false}
     */
    public boolean hasAnyTradeOffers() {
        return !openTradeOffers.isEmpty();
    }

    /**
     * Returns whether the player has received or offered the given trade offer by comparing
     * the offering player, the receiving player, the offered resources and the wanted resources
     *
     * @param tradeOffer the trade offer that should be checked
     * @return {@code true} if the player has the trade offer, otherwise {@code false}
     */
    public boolean hasTradeOffer(TradeOffer tradeOffer) {
        return openTradeOffers.contains(tradeOffer);
    }

    /**
     * Returns whether the player has declined the given trade offer by comparing
     * the offering player, the receiving player, the offered resources and the wanted resources
     *
     * @param tradeOffer the trade offer that should be checked
     * @return {@code true} if the player has declined the trade offer, otherwise {@code false}
     */
    public boolean hasDeclinedTradeOffer(TradeOffer tradeOffer) {
        return declinedTradeOffers.contains(tradeOffer);
    }

    /**
     * Returns whether the player has shown interest in the given trade offer by comparing
     * the offering player, the receiving player, the offered resources and the wanted resources
     *
     * @param tradeOffer the trade offer that should be checked
     * @return {@code true} if the player has shown interest in the trade offer, otherwise {@code false}
     */
    public boolean hasInterestTradeOffer(TradeOffer tradeOffer) {
        return interestTradeOffers.contains(tradeOffer);
    }

    /**
     * Adds a port to a player used for trading
     *
     * @param port port which should be added to the player
     */
    public void addPort(Port port) {
        ports.add(port);
    }

    /**
     * Removes the active trade offers from this player
     */
    public void clearTradeOffers() {
        openTradeOffers.clear();
        declinedTradeOffers.clear();
        interestTradeOffers.clear();
        counterTradeOffers.clear();
    }

    /**
     * Update the number of Victory points of the player with the given amount.
     *
     * @param amount the amount of victory points awarded or deduced.
     */
    public void updateBuildingVictoryPoints(int amount) {
        this.numOfBuildingVP += amount;
        gameSession.checkVictory();
    }

    /**
     * Checks if player has enough resources for building
     *
     * @param piece the piece that requires resources
     * @return boolean true if the player has enough resources
     */
    public boolean hasEnoughResources(PieceType piece) {
        switch (piece) {
            case CITY:
                return hasResources(piece.getCosts()) && this.inventory.getNumOfAvailableCities() > 0;
            case ROAD:
                return hasResources(piece.getCosts()) && this.inventory.getNumOfAvailableRoads() > 0;
            case SETTLEMENT:
                return hasResources(piece.getCosts()) && this.inventory.getNumOfAvailableSettlements() > 0;
            default:
                throw new IllegalStateException("Enum not accounted for " + piece);
        }
    }


    /**
     * Updates the legal node lists for this player around the given piece
     *
     * @param coord          The coord to update
     * @param buildablePiece The buildable that will be used to update the legal lists.
     */
    public void updateLegalNodes(Coord coord, BuildablePiece buildablePiece) {

        if (buildablePiece.getPlayerId() == playerId) {
            updateLegalNodeForOwner(coord, buildablePiece);
        } else {
            updateLegalNode(coord, buildablePiece);
        }
    }

    /**
     * Initializes the legal roads for this player around the starting settlements and roads
     */
    public void initLegalRoadNodes() {
        legalSettlements.clear();
        for (Coord settlementCoord : currentCornerPieces) {
            for (Coord edgeCoord : BoardUtils.getEdgeNeighboursFromCornerWithoutWater(board, settlementCoord)) {
                RoadPiece road = board.getRoadPiece(edgeCoord);
                if (road == null) {
                    legalRoads.add(edgeCoord);
                } else {
                    setLegalRoads(BoardUtils.getEdgeNeighboursWithoutWater(board, edgeCoord));
                }
            }
        }
    }

    /**
     * Updates the legal setup nodes for this player around the given piece by clearing the last legal roads
     *
     * @param coord          the position of the placed piece
     * @param buildablePiece the piece that was placed
     */
    public void updateLegalSetupNodes(Coord coord, BuildablePiece buildablePiece) {
        if (buildablePiece.getPlayerId() == playerId) {
            legalRoads.clear();
            updateLegalNodeForOwner(coord, buildablePiece);
        } else {
            updateLegalNode(coord, buildablePiece);
        }
    }

    private void setLegalRoads(Coord[] coords) {
        for (Coord edgeCoord : coords) {
            RoadPiece road = board.getRoadPiece(edgeCoord);
            if (road == null) {
                legalRoads.add(edgeCoord);
            }
        }
    }

    /**
     * Returns whether the player has enough of the given resource
     *
     * @param resource the resource that should be checked
     * @param count    the amount of the resource the player should have
     * @return {@code true} if the player has more or equal the amount of resource, otherwise {@code false}
     */
    public boolean hasResource(ResourceType resource, int count) {
        return inventory.hasResource(resource, count);
    }

    /**
     * Returns whether the player has enough of the given resources
     *
     * @param resources the amount of resources that the player should have
     * @return {@code true} if the player has more or equal the amount of resources, otherwise {@code false}
     */
    public boolean hasResources(ResourceEnumMap resources) {
        return inventory.hasResources(resources);
    }


    /**
     * Updates the nodes where pieces can be placed for the player who placed it
     *
     * @param coord          coord of the node where a piece has been placed
     * @param buildablePiece the piece which has been placed by this player
     */
    private void updateLegalNodeForOwner(Coord coord, BuildablePiece buildablePiece) {
        updateLegalNode(coord, buildablePiece);
        if (buildablePiece.getPieceType() == PieceType.ROAD) {
            Coord[] edgeCoords = BoardUtils.getEdgeNeighboursWithoutWater(board, coord);
            var coords = Arrays.stream(edgeCoords).filter(edge -> board.hasNoRoadBreakOnAtLeastOneSide(edge, this)).toArray(Coord[]::new);
            setLegalRoads(coords);
            for (Coord cornerCoord : Coord.getCornersFromEdge(coord)) {
                if (!board.containsSettlementOnAdjacentCoords(cornerCoord) && board.getCornerPiece(
                        cornerCoord) == null) {
                    legalSettlements.add(cornerCoord);
                }
            }
        } else if (buildablePiece.getPieceType() == PieceType.SETTLEMENT) {
            currentCornerPieces.add(coord);
            legalCities.add(coord);
            setLegalRoads(BoardUtils.getEdgeNeighboursFromCornerWithoutWater(board, coord).toArray(Coord[]::new));
        } else if (buildablePiece.getPieceType() == PieceType.CITY) {
            currentCornerPieces.add(coord);
            legalCities.remove(coord);
        }
    }

    /**
     * Create a PlayerDTO object to use for Messages
     *
     * @return PlayerDTO object
     */
    public PlayerDTO createDTO() {
        return new PlayerDTO(this.playerName, this.playerId, this.color, getNumOfPublicVP(), this.ports);
    }

    /**
     * Updates the nodes where pieces can be placed
     *
     * @param coord          coord of the node where a piece has been placed
     * @param buildablePiece the piece which has been placed
     */
    private void updateLegalNode(Coord coord, BuildablePiece buildablePiece) {
        if (buildablePiece instanceof RoadPiece) {
            legalRoads.remove(coord);
        } else if (buildablePiece instanceof SettlementPiece) {
            legalSettlements.remove(coord);
            for (Coord corner : BoardUtils.getCornerNeighboursWithoutWater(board, coord)) {
                legalSettlements.remove(corner);
            }
            for (Coord edge : BoardUtils.getEdgeNeighboursFromCornerWithoutWater(board, coord)) {
                if (!board.hasNoRoadBreakOnAtLeastOneSide(edge, this)) {
                    legalRoads.remove(edge);
                }
            }
        }
    }

    /**
     * Returns the player id
     *
     * @return the player id
     */
    public int getPlayerId() {
        return playerId;
    }

    /**
     * Returns the playerName
     *
     * @return the playerName
     */
    public String getPlayerName() {
        return playerName;
    }

    /**
     * getter for playerColor
     *
     * @return the color assigned to the player
     */
    public PlayerColor getColor() {
        return color;
    }

    /**
     * setter for the color of the player
     *
     * @param color that is assigned to the player
     */

    public void setColor(PlayerColor color) {
        this.color = color;
    }

    @Override
    public int hashCode() {
        return Objects.hash(playerId);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Player player = (Player) obj;
        return playerId == player.playerId;
    }

    @Override
    public String toString() {
        return "Player{" +
                "playerName='" + playerName + '\'' +
                ", playerId=" + playerId +
                '}';
    }

    /**
     * Checks Placement rules for a given piece at a given Coordinate
     *
     * @param coord the coordinate for the piece
     * @param piece the piece at the coordinate
     * @return true if the piece is allowed to be placed here
     */
    @SuppressWarnings({"PMD.CyclomaticComplexity"})
    public boolean checkBoardRules(Coord coord, PieceType piece) {
        switch (piece) {
            case ROAD:
                if (!board.isEmptyCoord(coord)) return false;
                if (board.containsPlayerSettlementAdjacentToEdge(coord, this)) return true;
                if (board.containsPlayerRoadAdjacentToEdge(coord, this) && board.hasNoRoadBreakOnAtLeastOneSide(coord, this)) return true;
                break;
            case SETTLEMENT:
                if (!board.isEmptyCoord(coord)) return false;
                if (board.containsPlayerRoadsAdjacentToCorner(coord, this) && !board.containsSettlementOnAdjacentCoords(coord)) return true;
                break;
            case CITY:
                if (board.hasPlayerSettlementOnCoord(coord, this)) {
                    return true;
                }
                break;
            default:
                LOG.error("Wrong PieceType");
        }
        return false;
    }

    /**
     * Return a random legal Settlement coordinate
     *
     * @return random legal CornerCoord
     */
    public Coord getRandomLegalSettlement() {
        if (legalSettlements.isEmpty()) {
            return null;
        }
        return (Coord) legalSettlements.toArray()[rand.nextInt(legalSettlements.size())];
    }

    /**
     * Return a random legal Road coordinate
     *
     * @return random legal EdgeCoord
     */
    public Coord getRandomLegalRoad() {
        if (legalRoads.isEmpty()) {
            return null;
        }
        return (Coord) legalRoads.toArray()[rand.nextInt(legalRoads.size())];
    }

    /**
     * Return a random legal City coordinate
     *
     * @return random legal EdgeCoord
     */
    public Coord getRandomLegalCity() {
        if (legalCities.isEmpty()) {
            return null;
        }
        return (Coord) legalCities.toArray()[rand.nextInt(legalCities.size())];
    }

    /**
     * Returns how many knights this player has played
     *
     * @return how many knights this player has played
     */
    public int getNumOfKnights() {
        return numOfKnights;
    }

    /**
     * Increments the number of knights this player has played
     */
    public void incrNumOfKnights() {
        numOfKnights++;
    }

    /**
     * Return a random legal RoadEdge connected to the given Corner
     *
     * @param coord the coord connection
     * @return the random Edge connected to the coord
     */
    @SuppressWarnings({"PMD.AvoidInstantiatingObjectsInLoops"})
    public Coord getRandomLegalRoadConnectedToCornerCoord(Coord coord) {

        Coord[] offset = NodeOffsetHelper.getEdgeOffsetFromCornerDirection(coord.getDir());
        HashSet<Coord> validNeighbors = new HashSet<>();
        for (int i = 0; i < 3; ++i) {
            Coord neighbor = new Coord(offset[i].getX() + coord.getX(), offset[i].getY() + coord.getY(), offset[i].getDir(), MapNode.EDGE);
            if (legalRoads.contains(neighbor)) {
                validNeighbors.add(neighbor);
            }
        }
        return (Coord) validNeighbors.toArray()[rand.nextInt(validNeighbors.size())];

    }

    /**
     * Returns the gameSessionName
     *
     * @return the gameSessionName
     */
    public String getGameSessionName() {
        return gameSession.getLobby().getName();
    }

    /**
     * Gets the amount of resources a player has to discard from their inventory.
     * Uses Math.ceil to get the rounded up of division by 2 from an int.
     *
     * @return number of resources a player has to discard in RobberDiscardState
     */
    public int getNumOfResourcesToDiscardFromInventory() {
        final int discardThreshold = 7;
        ResourceEnumMap resources = inventory.getResources();
        AtomicInteger sum = new AtomicInteger();
        resources.forEach((resource, amount) -> sum.addAndGet(amount));
        if (sum.get() > discardThreshold) {
            return (int) Math.ceil((double) sum.get() / 2.0);
        } else {
            return 0;
        }
    }

    /**
     * Returns a ResourceEnumMap filled with random resources from this players inventory without removing them
     *
     * @param amount the amount of random resources that should be returned
     * @return a ResourceEnumMap filled with random resources from this players inventory
     */
    public ResourceEnumMap getRandomResources(int amount) {
        List<ResourceType> resources = inventory.removeRandomResources(amount);
        ResourceEnumMap returnResources = new ResourceEnumMap();
        for (ResourceType resource : resources) {
            inventory.increaseResource(resource, 1);
            returnResources.computeIfPresent(resource, (resourceType, integer) -> integer += 1);
        }
        return returnResources;
    }

    /**
     * Returns the number of public victory points this player has
     * <p>
     * This includes VPs from Buildings, largest army bonus and longest road bonus
     *
     * @return the number of public victory points this player has
     */
    public int getNumOfPublicVP() {
        int publicVP = numOfBuildingVP;
        if (hasLongestRoad()) publicVP += 2;
        if (hasLargestArmy()) publicVP += 2;
        return publicVP;
    }

    /**
     * Returns the total number of victory points this player has
     * <p>
     * This includes VPs from Buildings, VP cards, largest army bonus and longest road bonus
     *
     * @return the total number of victory points this player has
     */
    public int getNumOfTotalVP() {
        return getNumOfPublicVP() + inventory.getVictoryPointCardCount();
    }

    /**
     * Returns whether this player has the largest army
     *
     * @return {@code true} if this player has the largest army, otherwise {@code false}
     */
    public boolean hasLargestArmy() {
        return equals(gameSession.getContext().getPlayerWithLargestArmy());
    }

    /**
     * Returns whether this player has the longest road
     *
     * @return {@code true} if this player has the longest road, otherwise {@code false}
     */
    public boolean hasLongestRoad() {
        return equals(gameSession.getPlayerWithLongestRoad());
    }

    /**
     * Getter for the inventory
     *
     * @return the players inventory
     */
    public Inventory getInventory() {
        return inventory;
    }

    /**
     * Getter for the ports
     *
     * @return ports
     */
    public Set<Port> getPorts() {
        return ports;
    }

    /**
     * Removes the given amount of resources from the resources that the player already has
     *
     * @param resourceType the resources that should be removed from the player
     */
    public int getAmountOfResources(ResourceType resourceType) {
        return inventory.getResource(resourceType);
    }


    /**
     * Getter for legal Nodes
     *
     * @return a Set of Coords for the legal nodes
     */
    public Set<Coord> getLegalNodes(PieceType pieceType) {
        switch (pieceType) {
            case ROAD:
                return Collections.unmodifiableSet(legalRoads);
            case SETTLEMENT:
                return Collections.unmodifiableSet(legalSettlements);
            case CITY:
                return Collections.unmodifiableSet(legalCities);
            default:
                return new HashSet<>();
        }
    }
}