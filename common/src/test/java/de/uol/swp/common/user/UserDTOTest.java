package de.uol.swp.common.user;

import de.uol.swp.common.game.dto.UserDTO;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test Class for the UserDTO
 *
 * @author Marco Grawunder
 * @since 2019-09-04
 */

@SuppressWarnings({"PMD.LinguisticNaming", "PMD.MethodNamingConventions"})
class UserDTOTest {

    static final User USER_PLAIN_PASSWORD = new UserDTO("marco", "marco", "marco@grawunder.de");
    static final User SECOND_USER = new UserDTO("marco2", "marco", "marco@grawunder.de");
    static final User USER_HASH_PASSWORD = new UserDTO("id", "marco3", new byte[]{4, 2}, new byte[]{4, 2}, "marco@grawunder.de");

    /**
     * Test subclass for all functions related to creating a user
     */
    @Nested
    class CreateUser {
        /**
         * This test check whether the username can be null
         * <p>
         * If both constructors do not throw an Exception the test fails
         */
        @Test
        void withEmptyName() {
            assertThrows(AssertionError.class, () -> new UserDTO(null, "", ""));
            assertThrows(AssertionError.class, () -> new UserDTO("", null, new byte[0], new byte[0], ""));
        }

        /**
         * This test check whether the password or the password hash and salt can be null
         * <p>
         * If both constructors do not throw an Exception the test fails
         */
        @Test
        void withEmptyPassword() {
            assertThrows(AssertionError.class, () -> new UserDTO("", null, ""));
            assertThrows(AssertionError.class, () -> new UserDTO("", "", null, null, ""));
        }

        /**
         * This test check whether the username can be null
         * <p>
         * If the constructor does not throw an Exception the test fails
         */
        @Test
        void withEmptyId() {
            assertThrows(AssertionError.class, () -> new UserDTO(null, "", new byte[0], new byte[0], ""));
        }

        /**
         * Test subclass for all functions related to creating a user based on an existing user
         */
        @Nested
        class WithExistingUser {
            /**
             * This test checks if the copy constructor for UserDTO objects with user inputs (plain password) works correctly
             * <p>
             * This test fails if any of the fields mismatch or the objects are not considered equal
             */
            @Test
            void withPlainPassword() {
                User newUser = UserDTO.createWithPlainPassword(USER_PLAIN_PASSWORD);
                assertEquals(USER_PLAIN_PASSWORD, newUser);

                assertEquals(USER_PLAIN_PASSWORD.getUsername(), newUser.getUsername());
                assertEquals(USER_PLAIN_PASSWORD.getPlainPassword(), newUser.getPlainPassword());
                assertEquals(USER_PLAIN_PASSWORD.getEMail(), newUser.getEMail());
            }

            /**
             * This test checks if the copy constructor works correctly
             * <p>
             * This test fails if any of the fields mismatch or the objects are not considered equal
             */
            @Test
            void withHashedPassword() {
                User newUser = UserDTO.create(USER_HASH_PASSWORD);
                assertEquals(USER_HASH_PASSWORD, newUser);

                assertEquals(USER_HASH_PASSWORD.getUsername(), newUser.getUsername());
                assertArrayEquals(USER_HASH_PASSWORD.getPasswordHash(), newUser.getPasswordHash());
                assertArrayEquals(USER_HASH_PASSWORD.getPasswordSalt(), newUser.getPasswordSalt());
                assertEquals(USER_HASH_PASSWORD.getEMail(), newUser.getEMail());
            }

            /**
             * This test checks if the createWithoutPassword functions generate the Object correctly
             * <p>
             * This test fails if the usernames or emails do not match or the password is not empty.
             *
             * @since 2019-09-04
             */
            @Test
            void withoutHashedPassword() {
                User newUser = UserDTO.createWithoutPassword(USER_HASH_PASSWORD);
                assertEquals(USER_HASH_PASSWORD, newUser);

                assertEquals(USER_HASH_PASSWORD.getUsername(), newUser.getUsername());
                assertEquals(0, newUser.getPasswordHash().length);
                assertEquals(0, newUser.getPasswordSalt().length);
                assertEquals(USER_HASH_PASSWORD.getEMail(), newUser.getEMail());
            }
        }
    }

    /**
     * This test checks if the getWithoutPassword function generates the Object correctly
     * <p>
     * This test fails if the usernames do not match or the password is not empty.
     *
     * @since 2019-09-04
     */
    @Test
    void getWithoutPassword() {
        User userWithoutPlainPassword = USER_PLAIN_PASSWORD.getWithoutPassword();

        assertEquals(USER_PLAIN_PASSWORD.getPlainPassword(), userWithoutPlainPassword.getUsername());
        assertNull(userWithoutPlainPassword.getPlainPassword());
        assertEquals(USER_PLAIN_PASSWORD.getUsername(), userWithoutPlainPassword.getUsername());

        User userWithoutHashedPassword = USER_PLAIN_PASSWORD.getWithoutPassword();

        assertEquals(USER_PLAIN_PASSWORD.getPlainPassword(), userWithoutHashedPassword.getUsername());
        assertEquals(0, userWithoutHashedPassword.getPasswordHash().length);
        assertEquals(0, userWithoutHashedPassword.getPasswordSalt().length);
        assertEquals(USER_PLAIN_PASSWORD.getUsername(), userWithoutHashedPassword.getUsername());
    }

    /**
     * Test if two different users are equal
     * <p>
     * This test fails if they are considered equal
     *
     * @since 2019-09-04
     */
    @Test
    void usersNotEquals_User() {
        assertNotEquals(USER_HASH_PASSWORD, SECOND_USER);
    }

    /**
     * Test of compare function
     * <p>
     * This test compares two different users. It fails if the function returns
     * that both of them are equal.
     *
     * @since 2019-09-04
     */
    @Test
    void userCompare() {
        assertNotEquals(0, USER_HASH_PASSWORD.compareTo(SECOND_USER));
    }

    /**
     * Test if the HashCode of a copied object matches the one of the original
     * <p>
     * This test fails if the codes do not match
     *
     * @since 2019-09-04
     */
    @Test
    void testHashCode() {
        User newUser = UserDTO.create(USER_HASH_PASSWORD);
        assertEquals(USER_HASH_PASSWORD.hashCode(), newUser.hashCode());
    }
}