package me.curlpipesh.control.util;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Random;

/**
 * Generic utilities. Strings, random numbers, AES, yoiu name it.
 *
 * @author audrey
 * @since 12/5/15.
 */
@SuppressWarnings("unused")
public final class GenericUtil {
    /**
     * RNG to use
     */
    private static final Random random;

    private GenericUtil() {
    }

    /**
     * Remove the first item from a String array. This method does no input
     * validation.
     *
     * @param strings The String array to shorted.
     * @return The input, without the first element.
     */
    public static String[] removeFirst(final String[] strings) {
        final String[] arr = new String[strings.length - 1];
        System.arraycopy(strings, 1, arr, 0, arr.length);
        return arr;
    }

    /**
     * Convert an array of strings into a single string, delimited by the given
     * separator.
     *
     * @param strings The array to implode
     * @param separator The delimiter to use
     * @return A String contained the imploded array
     */
    public static String implode(final String[] strings, final String separator) {
        final StringBuilder sb = new StringBuilder();
        Arrays.stream(strings).forEach(s -> sb.append(s).append(separator));
        return sb.toString().trim();
    }

    /**
     * Get a pseudo-random double on [0, 1)
     *
     * @return See above
     */
    public static double getRandomNormalizedDouble() {
        return random.nextDouble();
    }

    static {
        random = new Random(System.currentTimeMillis() ^ System.identityHashCode(System.class) & System.nanoTime());
    }

    /**
     * Utility class for AES encryption. Currently unused.
     */
    public static class AES {
        private final String mode;
        private Cipher cipher;
        private final Key aesKey;
        private final String key;

        public AES(final String key) {
            mode = "AES";
            try {
                cipher = Cipher.getInstance(mode);
            } catch(NoSuchAlgorithmException | NoSuchPaddingException e) {
                e.printStackTrace();
            }
            aesKey = new SecretKeySpec(key.getBytes(), mode);
            this.key = key;
        }

        public byte[] encrypt(final String text) {
            try {
                cipher.init(1, aesKey);
                return cipher.doFinal(text.getBytes());
            } catch(InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
                e.printStackTrace();
                return new byte[0];
            }
        }

        public byte[] decrypt(final byte[] bytes) {
            try {
                cipher.init(2, aesKey);
                return cipher.doFinal(bytes);
            } catch(InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
                e.printStackTrace();
                return new byte[0];
            }
        }
    }
}
