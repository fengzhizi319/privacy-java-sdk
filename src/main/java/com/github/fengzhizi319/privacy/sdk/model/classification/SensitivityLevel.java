package com.github.fengzhizi319.privacy.sdk.model.classification;

/**
 * 敏感度等级枚举（Sensitivity Level）。
 * <p>
 * 等级从低到高：L1（公开）&lt; L2（低风险）&lt; L3（中风险）&lt; L4（高风险）&lt; L5（极高风险）。
 * 提供等级比较、取最大值以及字符串解析能力。
 * </p>
 *
 * @author fengzhizi319
 * @since 0.1.0
 */
public enum SensitivityLevel {

    /** 公开 / Public。 */
    L1(1),

    /** 低风险 / Low risk。 */
    L2(2),

    /** 中风险 / Medium risk。 */
    L3(3),

    /** 高风险 / High risk。 */
    L4(4),

    /** 极高风险 / Critical risk。 */
    L5(5);

    /** 等级对应的数值，用于比较。 */
    private final int rank;

    SensitivityLevel(int rank) {
        this.rank = rank;
    }

    /**
     * 获取等级数值。
     *
     * @return 等级数值，L1=1，L5=5
     */
    public int getRank() {
        return rank;
    }

    /**
     * 判断当前等级是否高于或等于另一个等级。
     *
     * @param other 另一个敏感度等级
     * @return 当前等级更高或相等时返回 {@code true}
     */
    public boolean isHigherOrEqual(SensitivityLevel other) {
        return this.rank >= other.rank;
    }

    /**
     * 返回两个等级中较高的一个。
     *
     * @param a 等级 a
     * @param b 等级 b
     * @return 较高等级；若其中一个为 {@code null}，则返回非空者
     */
    public static SensitivityLevel max(SensitivityLevel a, SensitivityLevel b) {
        if (a == null) {
            return b;
        }
        if (b == null) {
            return a;
        }
        return a.rank >= b.rank ? a : b;
    }

    /**
     * 从字符串解析敏感度等级，大小写不敏感。
     *
     * @param value 等级字符串，例如 "L3"
     * @return 对应的 {@link SensitivityLevel}，无法解析时返回 {@code null}
     */
    public static SensitivityLevel fromString(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            return valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
