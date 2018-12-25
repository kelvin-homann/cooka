package app.cooka.cookapp.utils;

public class ColorUtils {

    /**
     * Generates a hexadecimal color value from a hash generated from the input.
     *      Can be used to generate a user name or user id dependent color value.
     * @param bytes the data (may come from string input) to hash and create a hexadecimal color
     *      value from.
     * @param prefixWithHashTag whether or not to prefix the returned color value with a hash tag
     *      depending on its intended usage.
     * @return the hexadecimal color value with or without the prefix hash tag #
     */
    public static String generateHexColorHash(byte[] bytes, boolean prefixWithHashTag) {

        StringBuilder hexString = new StringBuilder();

        if(prefixWithHashTag)
            hexString.append('#');
        for(int i = 0; i < bytes.length && i < 3; i++) {
            String hex = Integer.toHexString(0xff & bytes[i]);
            if(hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }

        return hexString.toString();
    }

    /**
     * Generates a hexadecimal color value from a hash generated from the input.
     *      Can be used to generate a user name or user id dependent color value.
     * @param stringHash the integer hash from a string that can be obtained from any string using
     *      the standard String method s.hashCode().
     * @param prefixWithHashTag whether or not to prefix the returned color value with a hash tag
     *      depending on its intended usage.
     * @return the hexadecimal color value with or without the prefix hash tag #
     */
    public static String generateHexColorHash(int stringHash, boolean prefixWithHashTag) {

        StringBuilder hexString = new StringBuilder();

        if(prefixWithHashTag)
            hexString.append('#');
        hexString.append(Integer.toHexString(((stringHash >> 16) & 0xff)));
        hexString.append(Integer.toHexString(((stringHash >> 8) & 0xff)));
        hexString.append(Integer.toHexString((stringHash & 0xff)));
        return hexString.toString();
    }
}
