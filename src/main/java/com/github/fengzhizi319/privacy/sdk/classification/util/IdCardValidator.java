package com.github.fengzhizi319.privacy.sdk.classification.util;

import java.util.regex.Pattern;

/**
 * 中国大陆身份证号校验器（ID Card Validator）。
 * <p>
 * 支持 18 位身份证格式校验与加权校验和计算。
 * </p>
 *
 * @author fengzhizi319
 * @since 0.1.0
 */
public class IdCardValidator {

    /** 18 位身份证正则表达式。 */
    private static final Pattern ID_CARD_PATTERN = Pattern.compile(
        "^[1-9]\\d{5}(18|19|20)\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])\\d{3}[\\dXx]$"
    );

    /** 前 17 位加权因子。 */
    private static final int[] WEIGHTS = {7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2};

    /** 校验字符映射表。 */
    private static final char[] CHECK_CHARS = {'1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2'};

    /**
     * 校验字符串是否为合法的中国大陆 18 位身份证号。
     *
     * @param value 待校验字符串
     * @return 合法时返回 {@code true}
     */
    public static boolean isValid(String value) {
        if (value == null || value.length() != 18) {
            return false;
        }
        if (!ID_CARD_PATTERN.matcher(value).matches()) {
            return false;
        }
        return checkSum(value);
    }

    /**
     * 仅校验格式正则，不计算校验和。
     *
     * @param value 待校验字符串
     * @return 格式匹配时返回 {@code true}
     */
    public static boolean matchesPattern(String value) {
        return value != null && ID_CARD_PATTERN.matcher(value).matches();
    }

    /**
     * 计算并比较身份证校验和。
     *
     * @param value 18 位身份证字符串
     * @return 校验和通过时返回 {@code true}
     */
    private static boolean checkSum(String value) {
        int sum = 0;
        for (int i = 0; i < 17; i++) {
            char c = value.charAt(i);
            if (!Character.isDigit(c)) {
                return false;
            }
            sum += (c - '0') * WEIGHTS[i];
        }
        char expected = CHECK_CHARS[sum % 11];
        char actual = Character.toUpperCase(value.charAt(17));
        return expected == actual;
    }
}
