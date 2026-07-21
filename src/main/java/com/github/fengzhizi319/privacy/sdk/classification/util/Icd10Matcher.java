package com.github.fengzhizi319.privacy.sdk.classification.util;

import java.util.regex.Pattern;

/**
 * ICD-10 编码匹配器（ICD-10 Matcher）。
 * <p>
 * 校验 ICD-10 编码格式，并判断编码是否落在指定的 L4 敏感区间（如 HIV、精神疾病、恶性肿瘤）。
 * </p>
 *
 * @author fengzhizi319
 * @since 0.1.0
 */
public final class Icd10Matcher {

    private Icd10Matcher() {
        // 工具类，禁止实例化
    }

    /** ICD-10 编码格式：1 位字母 + 2 位数字，可选小数点后 0~2 位数字。 */
    private static final Pattern ICD10_PATTERN = Pattern.compile("^[A-Z][0-9]{2}(\\.?[0-9]{0,2})?$");

    /**
     * 判断字符串是否符合 ICD-10 编码格式。
     *
     * @param value 待校验字符串
     * @return 符合格式时返回 {@code true}
     */
    public static boolean isValidCode(String value) {
        return value != null && ICD10_PATTERN.matcher(value).matches();
    }

    /**
     * 将 ICD-10 编码归一化为“字母 + 两位数字”形式，便于区间比较。
     *
     * @param code 原始 ICD-10 编码，例如 "B21.1"
     * @return 归一化后的编码，例如 "B21"；格式非法时返回 {@code null}
     */
    public static String normalize(String code) {
        if (!isValidCode(code)) {
            return null;
        }
        String base = code;
        if (base.contains(".")) {
            base = base.substring(0, base.indexOf('.'));
        }
        return base.toUpperCase();
    }

    /**
     * 判断编码是否落在闭区间 [start, end] 内。
     * <p>
     * 比较规则：先比较字母顺序，字母相同再比较两位数字。
     * </p>
     *
     * @param code  归一化后的编码，例如 "B21"
     * @param start 区间起点，例如 "B20"
     * @param end   区间终点，例如 "B24"
     * @return 在区间内时返回 {@code true}
     */
    public static boolean inRange(String code, String start, String end) {
        if (code == null || start == null || end == null) {
            return false;
        }
        return compare(code, start) >= 0 && compare(code, end) <= 0;
    }

    /**
     * 比较两个归一化 ICD-10 编码的大小。
     *
     * @param a 编码 a
     * @param b 编码 b
     * @return 小于返回负数，等于返回 0，大于返回正数
     */
    public static int compare(String a, String b) {
        char letterA = a.charAt(0);
        char letterB = b.charAt(0);
        if (letterA != letterB) {
            return letterA - letterB;
        }
        int numA = Integer.parseInt(a.substring(1, 3));
        int numB = Integer.parseInt(b.substring(1, 3));
        return numA - numB;
    }
}
