package br.com.bdregistros.util;

public final class CpfValidator {

    private CpfValidator() {
    }

    public static String somenteDigitos(String cpf) {
        return cpf == null ? "" : cpf.replaceAll("\\D", "");
    }

    public static boolean isValid(String cpf) {
        String digits = somenteDigitos(cpf);
        if (digits.length() != 11 || digits.chars().distinct().count() == 1) {
            return false;
        }
        return checkDigit(digits, 9) && checkDigit(digits, 10);
    }

    private static boolean checkDigit(String cpf, int length) {
        int sum = 0;
        for (int i = 0; i < length; i++) {
            sum += (cpf.charAt(i) - '0') * ((length + 1) - i);
        }
        int remainder = sum % 11;
        int expected = remainder < 2 ? 0 : 11 - remainder;
        return expected == (cpf.charAt(length) - '0');
    }
}
