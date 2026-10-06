package com.aub.mobilebanking.phone.eg.utils;

public class Hex {
    private static final String BCD_PAD = "F";
    private static final char[] HEX_DIGITS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    private Hex() {
    }

    /**
     * Converts s string representing hexadecimal values into an
     * array of bytes of those same values.
     *
     * @param hexString a string containing hexadecimal digits
     * @return A byte array containing binary data decoded from
     * the supplied byte array (representing characters).
     * @throws IllegalArgumentException Thrown if an odd number of characters is supplied
     *                                  to this function
     */
    public static byte[] decode(final String hexString) {
        char[] data = hexString.toCharArray();
        int len = data.length;

        if ((len & 0x1) != 0) {
            throw new IllegalArgumentException("Odd number of characters.");
        }

        byte[] out = new byte[len >> 1];

        int i = 0;
        for (int j = 0; j < len; ++i) {
            int f = toDigit(data[j], j) << 4;
            ++j;
            f |= toDigit(data[j], j);
            ++j;
            out[i] = (byte) (f & 0xFF);
        }

        return out;
    }

    /**
     * Converts an array of bytes into an array of characters representing the hexadecimal
     * values of each byte in order.
     *
     * @param data byte[] to convert to Hex characters
     * @return A String containing hexadecimal characters
     */
    public static String encode(final byte... data) {
        int l = data.length;
        char[] out = new char[l << 1];

        int i = 0;
        for (int j = 0; i < l; ++i) {
            out[(j++)] = HEX_DIGITS[((0xF0 & data[i]) >>> 4)];
            out[(j++)] = HEX_DIGITS[(0xF & data[i])];
        }
        return new String(out);
    }

    private static int toDigit(char ch, int index) {
        int digit = Character.digit(ch, 16);
        if (digit == -1) {
            throw new IllegalArgumentException("Illegal hexadecimal character " + ch + " at index " + index);
        }
        return digit;
    }

    public static byte[] oddEncode(final String possiblyOddHexes) {
        if (possiblyOddHexes.length() % 2 == 0) {
            return decode(possiblyOddHexes);
        } else {
            return decode("0" + possiblyOddHexes);
        }
    }

    /**
     * BCD String -> byte[] (adds F padding to right for odd number of digits)
     *
     * @param v
     * @return
     */
    public static byte[] bcd2bFpad(String... v) {
        String val = normalizeBcd(v);
        if (val == null) {
            return null;
        }
        if (val.length() % 2 == 0) {
            return decode(val);
        } else {
            return decode(val + BCD_PAD);
        }
    }

    private static char normalizeBcd(char c) {
        return HEX_DIGITS[bcdDigit(c) & 0x0F];
    }

    private static String normalizeBcd(String... v) {
        StringBuilder buf = new StringBuilder();
        boolean notNull = false;
        for (String s : v) {
            if (s != null) {
                notNull = true;
                for (int i = 0; i < s.length(); i++) {
                    buf.append(normalizeBcd(s.charAt(i)));
                }
            }
        }
        return notNull ? buf.toString() : null;
    }

    private static int bcdDigit(char c) {
        if (c >= 0x30 && c <= 0x3F) { // digits
            return c;
        } else if (c > 0x40 && c < 0x47) { // uppercase
            return c - 0x37;
        } else if (c > 0x60 && c < 0x67) { // lowercase
            return c - 0x57;
        } else {
            throw new IllegalArgumentException("Invalid bcd " + c);
        }
    }
}
