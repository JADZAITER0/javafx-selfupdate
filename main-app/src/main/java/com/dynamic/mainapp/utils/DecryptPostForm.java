package com.example.selfupdate.testjavafxmvci.utils;

    public class DecryptPostForm {

        private static final String CHAR_MAP = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ+/";
        private static final int CHAR_MAP_BASE = 10;
        private static final String CHAR_MAP_DIGITS = CHAR_MAP.substring(0, CHAR_MAP_BASE);

        public static void main(String[] args) {
            String fullKey = "your-full-key"; // Replace with actual full key
            String key = "your-key";          // Replace with actual key
            int v1 = 0;                       // Replace with actual v1
            int v2 = 0;                       // Replace with actual v2

            String result = decryptPostForm(fullKey, key, v1, v2);
        }

        public static String decryptPostForm(String fullKey, String key, int v1, int v2) {
            StringBuilder result = new StringBuilder();
            int i = 0;

            while (i < fullKey.length()) {
                StringBuilder s = new StringBuilder();
                while (fullKey.charAt(i) != key.charAt(v2)) {
                    s.append(fullKey.charAt(i));
                    i++;
                }

                for (int idx = 0; idx < key.length(); idx++) {
                    char c = key.charAt(idx);
                    int index = s.indexOf(String.valueOf(c));
                    while (index != -1) {
                        s.replace(index, index + 1, String.valueOf(idx));
                        index = s.indexOf(String.valueOf(c), index + 1);
                    }
                }

                result.append((char) (getCharCode(s.toString(), v2) - v1));
                i++;
            }

            return result.toString();
        }

        public static int getCharCode(String content, int s1) {
            int j = 0;

            // Calculate the numeric value from the content
            for (int index = 0; index < content.length(); index++) {
                char c = content.charAt(content.length() - 1 - index);
                if (Character.isDigit(c)) {
                    j += Character.getNumericValue(c) * Math.pow(s1, index);
                }
            }

            // Convert the numeric value to a custom base
            StringBuilder k = new StringBuilder();
            while (j > 0) {
                k.insert(0, CHAR_MAP_DIGITS.charAt(j % CHAR_MAP_BASE));
                j = (j - (j % CHAR_MAP_BASE)) / CHAR_MAP_BASE;
            }

            return k.length() > 0 ? Integer.parseInt(k.toString()) : 0;
        }
    }
