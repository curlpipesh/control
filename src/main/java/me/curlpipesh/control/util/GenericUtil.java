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
 * @author audrey
 * @since 12/5/15.
 */
@SuppressWarnings("unused")
public class GenericUtil {
    private static final Random random;

    public static String[] removeFirst(final String[] strings) {
        final String[] arr = new String[strings.length - 1];
        System.arraycopy(strings, 1, arr, 0, arr.length);
        return arr;
    }

    public static String implode(final String[] strings, final String separator) {
        final StringBuilder sb = new StringBuilder();
        Arrays.stream(strings).forEach(s -> sb.append(s).append(separator));
        return sb.toString().trim();
    }

    public static double getRandomNormalizedDouble() {
        return random.nextDouble();
    }

    static {
        random = new Random(System.currentTimeMillis() ^ (System.identityHashCode(System.class) & System.nanoTime()));
    }

    public static class AES {
        private String mode;
        private Cipher cipher;
        private Key aesKey;
        private String key;

        public AES(final String key) {
            this.mode = "AES";
            try {
                this.cipher = Cipher.getInstance(this.mode);
            } catch(NoSuchAlgorithmException | NoSuchPaddingException e) {
                e.printStackTrace();
            }
            this.aesKey = new SecretKeySpec(key.getBytes(), this.mode);
            this.key = key;
        }

        public byte[] encrypt(final String text) {
            try {
                this.cipher.init(1, this.aesKey);
                return this.cipher.doFinal(text.getBytes());
            } catch(InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
                e.printStackTrace();
                return new byte[0];
            }
        }

        public byte[] decrypt(final byte[] bytes) {
            try {
                this.cipher.init(2, this.aesKey);
                return this.cipher.doFinal(bytes);
            } catch(InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
                e.printStackTrace();
                return new byte[0];
            }
        }
    }
}
