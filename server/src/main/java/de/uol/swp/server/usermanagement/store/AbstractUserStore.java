package de.uol.swp.server.usermanagement.store;


import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;

/**
 * Base class for all kinds of different UserStores
 *
 * @author Marco Grawunder
 * @see de.uol.swp.server.usermanagement.store.UserStore
 * @since 2019-09-04
 */
public abstract class AbstractUserStore implements UserStore {

    private static final int ITERATIONS = 1000;
    private static final int SALT_BYTES = 20;

    private static byte[] pbkdf2(char[] password, byte[] salt) throws NoSuchAlgorithmException, InvalidKeySpecException {
        KeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, SALT_BYTES * 8);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        return factory.generateSecret(spec).getEncoded();
    }

    /**
     * Calculates the hash and salt for a given String
     *
     * @param password the String to calculate the hash and salt for
     * @return a two-dimensional byte array containing a hash array at index 0 and a salt array at index 1
     */
    protected byte[][] createHashWithSalt(String password) {
        return createHashWithSalt(password.toCharArray());
    }

    /**
     * Calculates the hash and salt for a given char array
     *
     * @param password the char array to calculate the hash and salt for
     * @return a two-dimensional byte array containing a hash and a salt array
     */
    protected byte[][] createHashWithSalt(char... password) {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_BYTES];
        random.nextBytes(salt);
        try {
            byte[] hash = pbkdf2(password, salt);
            return new byte[][]{salt, hash};
        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Calculates the hash for a given password and salt
     *
     * @param password the char array to calculate the hash for
     * @param salt     the salt used to calculate the hash
     * @return a byte array containing the hash
     */
    protected byte[] createHashFromSalt(char[] password, byte[] salt) {
        try {
            return pbkdf2(password, salt);
        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Validates a given password by comparing its hash with a stored hash
     *
     * @param password     the password that needs to be validated
     * @param passwordHash the hash that the password is supposed to match
     * @param passwordSalt the salt used to calculate the hash
     * @return {@code true} if the password is correct, otherwise {@code false}
     */
    protected boolean validatePassword(String password, byte[] passwordHash, byte[] passwordSalt) {
        byte[] testHash = createHashFromSalt(password.toCharArray(), passwordSalt);
        return Arrays.equals(testHash, passwordHash);
    }
}
