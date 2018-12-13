package app.cooka.cookapp.utils;

public class StringUtils {

    /**
     * Converts a byte array into a hexadecimal string
     * @param bytes the array of bytes to convert
     * @return string representing the bytes in the array
     */
    public static String toHexString(byte[] bytes) {

        StringBuilder hexString = new StringBuilder();

        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }

        return hexString.toString();
    }
}
