package de.uol.swp.common.lobby.dto;

/**
 * Utility class used to create the {@code LobbyOption} enums
 *
 * @see LobbyOption
 */
@SuppressWarnings("PMD.ClassNamingConventions")
public class LobbyOptions {
    public static final LobbyOption<Integer> LOBBY_SIZE = IntegerOption.LOBBY_SIZE;
    public static final LobbyOption<Integer> NUM_VICTORY_POINTS = IntegerOption.NUM_VICTORY_POINTS;
    public static final LobbyOption<Integer> TIMER_DURATION = IntegerOption.TIMER_DURATION;
    public static final LobbyOption<Boolean> PRIVATE_LOBBY = BooleanOption.PRIVATE_LOBBY;
    public static final LobbyOption<Boolean> DEBUG = BooleanOption.DEBUG;

    /**
     * Clamp Integer values to a given range
     *
     * @param val the value to clamp
     * @param min the minimum value
     * @param max the maximum value
     * @return the clamped value
     */
    public static int clampInt(int val, int min, int max) {
        return Math.max(min, Math.min(max, val));
    }

    /**
     * Integer lobby options and the range of their values.
     * Should be used to communicate a change in a lobby's option.
     */
    private enum IntegerOption implements LobbyOption<Integer> {
        /**
         * The number of victory points necessary to win a game
         */
        NUM_VICTORY_POINTS(5, 15, 10),
        /**
         * The number of players that can enter the lobby
         */
        LOBBY_SIZE(3, 4, 4),
        /**
         * The amount of time each player has for their turn
         */
        TIMER_DURATION(20, 120, 60);

        private final Integer min;
        private final Integer max;
        private final Integer defaultValue;

        IntegerOption(int min, int max, int defaultValue) {
            this.min = min;
            this.max = max;
            this.defaultValue = defaultValue;
        }

        @Override
        public Integer getDefaultValue() {
            return defaultValue;
        }

        @Override
        public Integer getMin() {
            return min;
        }

        @Override
        public Integer getMax() {
            return max;
        }

        @Override
        public String toString() {
            return "IntegerOption:" + this.name() + "{" +
                    "min=" + min +
                    ", max=" + max +
                    ", defaultValue=" + defaultValue +
                    "}";
        }
    }

    /**
     * Boolean lobby options and their default values.
     * Should be used to communicate a change in a lobby's option.
     */
    private enum BooleanOption implements LobbyOption<Boolean> {
        /**
         * If the lobby should be visible to others
         */
        PRIVATE_LOBBY(false),
        /**
         * If the game should have the debug menu
         */
        DEBUG(false);

        private final Boolean defaultValue;

        BooleanOption(boolean defaultValue) {
            this.defaultValue = defaultValue;
        }

        @Override
        public Boolean getDefaultValue() {
            return defaultValue;
        }

        /**
         * @throws UnsupportedOperationException not implemented
         */
        @Override
        public Boolean getMin() {
            throw new UnsupportedOperationException(BooleanOption.class + ": does not have a minimum");
        }

        /**
         * @throws UnsupportedOperationException not implemented
         */
        @Override
        public Boolean getMax() {
            throw new UnsupportedOperationException(BooleanOption.class + ": does not have a maximum");
        }

        @Override
        public String toString() {
            return "BooleanOption:" + this.name() + "{" +
                    "defaultValue=" + defaultValue +
                    "}";
        }
    }
}
