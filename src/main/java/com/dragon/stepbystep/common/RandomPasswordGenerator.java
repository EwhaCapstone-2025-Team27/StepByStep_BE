package com.dragon.stepbystep.common;

import java.security.SecureRandom;

public final class RandomPasswordGenerator {
    private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGIT = "0123456789";
    private static final String SPECIAL = "!@#$%^&*()-_=+[]{};:,.?";
    private static final String ALL = UPPER + LOWER + DIGIT + SPECIAL;

    private static final SecureRandom RND = new SecureRandom();

    private RandomPasswordGenerator() {}

    //길이 12+ 권장. 각 문자군 최소 1개 보장
    public static String generate(int length) {
        if (length < 12) length = 12;

        StringBuilder sb = new StringBuilder(length);

        sb.append(pick(UPPER)).append(pick(LOWER)).append(pick(DIGIT)).append(pick(SPECIAL));

        for (int i = 4; i < length; i++) {
            sb.append(pick(ALL));
        }

        char[] arr = sb.toString().toCharArray();
        for (int i = 0; i < arr.length; i++) {
            int j = RND.nextInt(arr.length);
            char t = arr[i]; arr[i] = arr[j]; arr[j] = t;
        }
        return new String(arr);
    }

    private static char pick(String s) { return s.charAt(RND.nextInt(s.length())); }
}
