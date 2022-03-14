package de.uol.swp.common.lobby;

import de.uol.swp.common.lobby.dto.LobbyOption;
import de.uol.swp.common.lobby.dto.LobbyOptions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Test class for LobbyOptions
 *
 * @see LobbyOptions
 */
class LobbyOptionsTest {

    /**
     * This test whether the {@link LobbyOptions#clampInt(int, int, int) clampInt} method properly clamps integer values
     * <p>
     * It fails if an entered integer value is outside of the given boundaries after calling the method.
     */
    @Test
    void clampIntTest() {
        assertEquals(2, LobbyOptions.clampInt(1, 2, 4));
        assertEquals(4, LobbyOptions.clampInt(5, 2, 4));
    }

    /**
     * This test checks whether the {@link LobbyOption#getMin BooleanOption.getMin()} and
     * {@link LobbyOption#getMax BooleanOption.getMax()} methods throw an {@code UnsupportedOperationException}
     * <p>
     * It fails if any of the two methods does not throw an {@code UnsupportedOperationException}.
     */
    @Test
    void booleanOptionBoundaries_ShouldThrowException() {
        Assertions.assertAll(() -> {
            assertThrows(UnsupportedOperationException.class, LobbyOptions.PRIVATE_LOBBY::getMin);
            assertThrows(UnsupportedOperationException.class, LobbyOptions.PRIVATE_LOBBY::getMax);
        });
    }

    /**
     * Nested Test class for all tests of the {@link LobbyOption#toString()} methods
     */
    @Nested
    class OptionToString {
        /**
         * This test checks whether the {@link LobbyOption#toString() toString()} method of the
         * {@link LobbyOptions#LOBBY_SIZE LOBBY_SIZE} option returns the correct String
         * <p>
         * It fails if the returned String does not match the template.
         */
        @Test
        void lobbySize() {
            assertEquals("IntegerOption:" + "LOBBY_SIZE" + "{" +
                    "min=" + LobbyOptions.LOBBY_SIZE.getMin() +
                    ", max=" + LobbyOptions.LOBBY_SIZE.getMax() +
                    ", defaultValue=" + LobbyOptions.LOBBY_SIZE.getDefaultValue() +
                    "}", LobbyOptions.LOBBY_SIZE.toString());
        }

        /**
         * This test checks whether the {@link LobbyOption#toString() toString()} method of the
         * {@link LobbyOptions#NUM_VICTORY_POINTS NUM_VICTORY_POINTS} option returns the correct String
         * <p>
         * It fails if the returned String does not match the template.
         */
        @Test
        void numOfVictoryPoints() {
            assertEquals("IntegerOption:" + "NUM_VICTORY_POINTS" + "{" +
                    "min=" + LobbyOptions.NUM_VICTORY_POINTS.getMin() +
                    ", max=" + LobbyOptions.NUM_VICTORY_POINTS.getMax() +
                    ", defaultValue=" + LobbyOptions.NUM_VICTORY_POINTS.getDefaultValue() +
                    "}", LobbyOptions.NUM_VICTORY_POINTS.toString());
        }

        /**
         * This test checks whether the {@link LobbyOption#toString() toString()} method of the
         * {@link LobbyOptions#TIMER_DURATION TIMER_DURATION} option returns the correct String
         * <p>
         * It fails if the returned String does not match the template.
         */
        @Test
        void timerDuration() {
            assertEquals("IntegerOption:" + "TIMER_DURATION" + "{" +
                    "min=" + LobbyOptions.TIMER_DURATION.getMin() +
                    ", max=" + LobbyOptions.TIMER_DURATION.getMax() +
                    ", defaultValue=" + LobbyOptions.TIMER_DURATION.getDefaultValue() +
                    "}", LobbyOptions.TIMER_DURATION.toString());
        }

        /**
         * This test checks whether the {@link LobbyOption#toString() toString()} method of the
         * {@link LobbyOptions#PRIVATE_LOBBY PRIVATE_LOBBY} option returns the correct String
         * <p>
         * It fails if the returned String does not match the template.
         */
        @Test
        void privateLobby() {
            assertEquals("BooleanOption:" + "PRIVATE_LOBBY" + "{" +
                    "defaultValue=" + LobbyOptions.PRIVATE_LOBBY.getDefaultValue() +
                    "}", LobbyOptions.PRIVATE_LOBBY.toString());
        }
    }
}
