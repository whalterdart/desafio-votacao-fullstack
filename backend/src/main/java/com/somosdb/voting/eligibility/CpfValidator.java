package com.somosdb.voting.eligibility;

import java.util.regex.Pattern;

public final class CpfValidator {
    private static final Pattern NON_DIGIT = Pattern.compile("\\D");
    private CpfValidator() {}

    public static String normalize(String cpf) {
        return cpf == null ? "" : NON_DIGIT.matcher(cpf).replaceAll("");
    }

    public static boolean isValid(String value) {
        String cpf = normalize(value);
        if (cpf.length() != 11 || cpf.chars().distinct().count() == 1) return false;
        try {
            int first = digit(cpf, 9, 10);
            int second = digit(cpf, 10, 11);
            return first == Character.digit(cpf.charAt(9), 10)
                    && second == Character.digit(cpf.charAt(10), 10);
        } catch (RuntimeException ex) {
            return false;
        }
    }

    private static int digit(String cpf, int length, int weight) {
        int sum = 0;
        for (int i = 0; i < length; i++) sum += Character.digit(cpf.charAt(i), 10) * (weight - i);
        int result = 11 - (sum % 11);
        return result >= 10 ? 0 : result;
    }
}

