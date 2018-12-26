package app.cooka.cookapp.utils;

public class StringUtils {

    /**
     * Converts a byte array into a hexadecimal string
     * @param bytes the array of bytes to convert
     * @return string representing the bytes in the array
     */
    public static String toHexString(byte[] bytes) {

        StringBuilder hexString = new StringBuilder();

        for(int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(0xff & bytes[i]);
            if(hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }

        return hexString.toString();
    }

    /**
     * Checks whether or not a given user name string represents a syntactically valid user name
     * in the sense of this application. Use this to verify that a user has typed a valid
     * user name before account creation or login. Can be called on each key input event to quickly
     * and conveniently tell the user if the user name is valid or not.
     * @param userName the user name string to check against.
     * @return true if the user name is syntactically valid; false otherwise.
     */
    public static boolean isValidUserName(final String userName) {

        return userName.matches("^([a-zA-Z0-9_]{0,32})$");
    }

    /**
     * Checks whether or not a given email address string represents a syntactically valid email
     * address in the sense of this application. Use this to verify that a user has typed a valid
     * email address before account creation or login. Can be called on each key input event to
     * quickly and conveniently tell the user if the email address is valid or not.
     * @param emailAddress the email address string to check against.
     * @return true if the email address is syntactically valid; false otherwise.
     */
    public static boolean isValidEmailAddress(final String emailAddress) {

        return emailAddress.length() <= 96 &&
            emailAddress.matches("^\\b[\\w\\.-]+@([\\w-]+\\.)+\\w{2,14}\\b$");
    }

    /**
     * Checks whether or not a given password string represents a syntactically valid password in
     * the sense of this application. Use this to verify that a user has typed a valid password
     * before account creation or login. Can be called on each key input event to quickly and
     * conveniently tell the user if the password is valid or not.
     * @param password the clear text password string to check against.
     * @return true if the password is syntactically valid; false otherwise.
     */
    public static boolean isValidPassword(final String password) {

        return password.matches("^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[a-zA-Z]).{6,64}$");
    }

    /**
     * Checks whether or not a given title string represents a valid title in the sense of this
     * application. Use this to check if recipe titles, collection titles and category names can be
     * safely used within the application and correctly stored within the database.
     * @param title the title string to check against.
     * @return true if the title is valid; false otherwise.
     */
    public static boolean isValidTitle(final String title) {

        return title.matches("^((?!_)[a-z0-9\\w][ a-z0-9_\\\"\\'\\&\\(\\)\\/\\-\\u00c0-\\u00d6\\u00d8-\\u00f6\\u00f8-\\u00ff\\u0100-\\u024f\\u0253-\\u0254\\u0256-\\u0257\\u0300-\\u036f\\u1e00-\\u1eff\\u0400-\\u04ff\\u0500-\\u0527\\u2de0-\\u2dff\\ua640-\\ua69f\\u0591-\\u05bf\\u05c1-\\u05c2\\u05c4-\\u05c5\\u05d0-\\u05ea\\u05f0-\\u05f4\\ufb12-\\ufb28\\ufb2a-\\ufb36\\ufb38-\\ufb3c\\ufb40-\\ufb41\\ufb43-\\ufb44\\ufb46-\\ufb4f\\u0610-\\u061a\\u0620-\\u065f\\u066e-\\u06d3\\u06d5-\\u06dc\\u06de-\\u06e8\\u06ea-\\u06ef\\u06fa-\\u06fc\\u0750-\\u077f\\u08a2-\\u08ac\\u08e4-\\u08fe\\ufb50-\\ufbb1\\ufbd3-\\ufd3d\\ufd50-\\ufd8f\\ufd92-\\ufdc7\\ufdf0-\\ufdfb\\ufe70-\\ufe74\\ufe76-\\ufefc\\u200c-\\u200c\\u0e01-\\u0e3a\\u0e40-\\u0e4e\\u1100-\\u11ff\\u3130-\\u3185\\ua960-\\ua97f\\uac00-\\ud7af\\ud7b0-\\ud7ff\\uffa1-\\uffdc\\u30a1-\\u30fa\\u30fc-\\u30fe\\uff66-\\uff9f\\uff10-\\uff19\\uff21-\\uff3a\\uff41-\\uff5a\\u3041-\\u3096\\u3099-\\u309e\\u3400-\\u4dbf\\u4e00-\\u9fff\\u20000-\\u2a6df\\u2a700-\\u2b73f\\u2b740-\\u2b81f\\u2f800-\\u2fa1f]{0,191})$");
    }

    /**
     * Checks whether or not a given hash tag string represents a valid tag in the sense of this
     * application. Use this to verify user input where necessary.
     * @param hashTag the hash tag to check against.
     * @return true if the hash tag is valid; false otherwise.
     */
    public static boolean isValidHashTag(final String hashTag) {

        return hashTag.length() <= 32 &&
            hashTag.matches("^([a-z0-9_\\u00c0-\\u00d6\\u00d8-\\u00f6\\u00f8-\\u00ff\\u0100-\\u024f\\u0253-\\u0254\\u0256-\\u0257\\u0300-\\u036f\\u1e00-\\u1eff\\u0400-\\u04ff\\u0500-\\u0527\\u2de0-\\u2dff\\ua640-\\ua69f\\u0591-\\u05bf\\u05c1-\\u05c2\\u05c4-\\u05c5\\u05d0-\\u05ea\\u05f0-\\u05f4\\ufb12-\\ufb28\\ufb2a-\\ufb36\\ufb38-\\ufb3c\\ufb40-\\ufb41\\ufb43-\\ufb44\\ufb46-\\ufb4f\\u0610-\\u061a\\u0620-\\u065f\\u066e-\\u06d3\\u06d5-\\u06dc\\u06de-\\u06e8\\u06ea-\\u06ef\\u06fa-\\u06fc\\u0750-\\u077f\\u08a2-\\u08ac\\u08e4-\\u08fe\\ufb50-\\ufbb1\\ufbd3-\\ufd3d\\ufd50-\\ufd8f\\ufd92-\\ufdc7\\ufdf0-\\ufdfb\\ufe70-\\ufe74\\ufe76-\\ufefc\\u200c-\\u200c\\u0e01-\\u0e3a\\u0e40-\\u0e4e\\u1100-\\u11ff\\u3130-\\u3185\\ua960-\\ua97f\\uac00-\\ud7af\\ud7b0-\\ud7ff\\uffa1-\\uffdc\\u30a1-\\u30fa\\u30fc-\\u30fe\\uff66-\\uff9f\\uff10-\\uff19\\uff21-\\uff3a\\uff41-\\uff5a\\u3041-\\u3096\\u3099-\\u309e\\u3400-\\u4dbf\\u4e00-\\u9fff\\u20000-\\u2a6df\\u2a700-\\u2b73f\\u2b740-\\u2b81f\\u2f800-\\u2fa1f]*[a-z_\\u00c0-\\u00d6\\u00d8-\\u00f6\\u00f8-\\u00ff\\u0100-\\u024f\\u0253-\\u0254\\u0256-\\u0257\\u0300-\\u036f\\u1e00-\\u1eff\\u0400-\\u04ff\\u0500-\\u0527\\u2de0-\\u2dff\\ua640-\\ua69f\\u0591-\\u05bf\\u05c1-\\u05c2\\u05c4-\\u05c5\\u05d0-\\u05ea\\u05f0-\\u05f4\\ufb12-\\ufb28\\ufb2a-\\ufb36\\ufb38-\\ufb3c\\ufb40-\\ufb41\\ufb43-\\ufb44\\ufb46-\\ufb4f\\u0610-\\u061a\\u0620-\\u065f\\u066e-\\u06d3\\u06d5-\\u06dc\\u06de-\\u06e8\\u06ea-\\u06ef\\u06fa-\\u06fc\\u0750-\\u077f\\u08a2-\\u08ac\\u08e4-\\u08fe\\ufb50-\\ufbb1\\ufbd3-\\ufd3d\\ufd50-\\ufd8f\\ufd92-\\ufdc7\\ufdf0-\\ufdfb\\ufe70-\\ufe74\\ufe76-\\ufefc\\u200c-\\u200c\\u0e01-\\u0e3a\\u0e40-\\u0e4e\\u1100-\\u11ff\\u3130-\\u3185\\ua960-\\ua97f\\uac00-\\ud7af\\ud7b0-\\ud7ff\\uffa1-\\uffdc\\u30a1-\\u30fa\\u30fc-\\u30fe\\uff66-\\uff9f\\uff10-\\uff19\\uff21-\\uff3a\\uff41-\\uff5a\\u3041-\\u3096\\u3099-\\u309e\\u3400-\\u4dbf\\u4e00-\\u9fff\\u20000-\\u2a6df\\u2a700-\\u2b73f\\u2b740-\\u2b81f\\u2f800-\\u2fa1f]*)$");
    }
}
