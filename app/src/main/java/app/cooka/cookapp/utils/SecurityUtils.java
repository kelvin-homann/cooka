package app.cooka.cookapp.utils;

import android.util.Base64;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Random;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import app.cooka.cookapp.model.DatabaseClient;

public class SecurityUtils {

    public static final int DEFAULT_SALT_LENGTH = 32;

    private static final Random RANDOM = new SecureRandom();
    private static final String ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final int ITERATIONS = 10000;
    private static final int KEY_LENGTH = 256;

    /**
     * Hashes the input data with the PBKDF algorithm by using the provided salt
     * @param data the data to be hashed
     * @param salt the salt to be applied
     * @return a byte array with the hash of the data
     */
    public static byte[] hash(char[] data, byte[] salt) {

        PBEKeySpec spec = new PBEKeySpec(data, salt, ITERATIONS, KEY_LENGTH);
        Arrays.fill(data, Character.MIN_VALUE);
        try {
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            return skf.generateSecret(spec).getEncoded();
        }
        catch(NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new AssertionError("Error while hashing a password: " + e.getMessage(), e);
        }
        finally {
            spec.clearPassword();
        }
    }

    /**
     * Generates a salt in the specified length from an alphanumerical alphabet
     * @param length the length of the salt to be generated
     * @return the salt string in the given length
     */
    public static String generateSalt(int length) {

        StringBuilder saltString = new StringBuilder(length);
        for(int i = 0; i < length; i++)
            saltString.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));

        return new String(saltString);
    }

    /**
     * Generates a hashed password using the hash function and the provided salt
     * @param password the clear text password to be secured
     * @param salt the salt to be used by the hash function
     * @return the hashed and base64 encoded password as a string
     */
    public static String generateHashedPassword(String password, String salt) {

        String hashedPasswordBase64 = null;
        byte[] securePassword = hash(password.toCharArray(), salt.getBytes());
        hashedPasswordBase64 = Base64.encodeToString(securePassword, Base64.NO_WRAP | Base64.URL_SAFE);
        return hashedPasswordBase64;
    }

    /**
     * Verifies the provided password with the given hashed password and salt
     * @param providedPassword the password to be verified
     * @param hashedPassword the original hashed password to check against
     * @param salt the original salt
     * @return true if the password matches the original hashed password
     */
    public static boolean verifyPassword(String providedPassword, String hashedPassword,
        String salt)
    {
        return generateHashedPassword(providedPassword, salt).equals(hashedPassword);
    }

    /**
     * Generates a message digest (MD5) hash from the provided data
     * @param data the data to be hashed
     * @return the hash string; null if an error occurred
     */
    public static String generateMd5Hash(byte[] data) {

        String md5hash = null;
        try {
            byte[] hash = MessageDigest.getInstance("MD5").digest(data);
            BigInteger bigInt = new BigInteger(1, hash);
            md5hash = bigInt.toString(16);
        }
        catch(NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return md5hash;
    }

    /**
     * Generates a new access token to associated with a user login
     * @return the access token as a string; null if an error occurred
     */
    public static String generateAccessToken() {

        String data = generateSalt(10) + System.currentTimeMillis();
        return generateMd5Hash(data.getBytes());
    }
}
