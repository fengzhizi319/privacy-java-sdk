package com.github.fengzhizi319.privacy.sdk.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * K-匿名 API（K-Anonymity API）。
 * <p>
 * 对单条记录中的准标识符（Quasi-Identifiers）按预定义的泛化层次结构（Generalization Hierarchy）进行泛化，
 * 使得发布后的数据集中每条记录至少与 {@code k-1} 条其他记录不可区分。
 * </p>
 *
 * <p><b>实现注意点：</b>当前版本为单记录演示实现，{@link #chooseLevel(int, GeneralizationHierarchy)}
 * 采用简化策略：k 每增加 5 提升一层泛化，实际生产应基于等价类大小动态调整。</p>
 *
 * @author fengzhizi319
 * @since 0.1.0
 */
public class KAnonymityApi {

    /**
     * 对单条记录进行 K-匿名泛化。
     * <p>
     * 仅处理 {@code qiCols} 中指定的准标识符列；若某列存在对应的泛化层次结构，则按计算出的泛化层级替换原值，
     * 非准标识符列原样保留。
     * </p>
     *
     * @param record       原始记录，key 为列名，value 为原始值
     * @param qiCols       准标识符列名列表
     * @param hierarchies  各准标识符列对应的泛化层次结构映射
     * @param k            K-匿名参数，k 越大通常泛化程度越高
     * @return 泛化后的新记录；内部为 {@link HashMap} 副本，不修改原记录
     */
    public Map<String, Object> anonymizeRecord(Map<String, Object> record,
                                               List<String> qiCols,
                                               Map<String, GeneralizationHierarchy> hierarchies,
                                               int k) {
        Map<String, Object> result = new HashMap<>(record);
        for (String col : qiCols) {
            GeneralizationHierarchy h = hierarchies.get(col);
            Object value = record.get(col);
            if (h != null && value != null) {
                result.put(col, h.generalize(value.toString(), chooseLevel(k, h)));
            }
        }
        return result;
    }

    /**
     * 根据 k 值与泛化层次最大层级选择泛化层级。
     * <p>
     * 简化规则：{@code level = max(1, min(k / 5, maxLevel))}。
     * </p>
     *
     * @param k 目标 K-匿名参数
     * @param h 泛化层次结构
     * @return 选中的泛化层级，范围 [1, maxLevel]
     */
    private int chooseLevel(int k, GeneralizationHierarchy h) {
        // Simplified: higher k => higher generalization level
        int max = h.maxLevel();
        int level = Math.min((k / 5), max);
        return Math.max(level, 1);
    }

    /**
     * 泛化层次结构接口，定义如何按层级对原始值进行泛化以及最大泛化层级。
     */
    public interface GeneralizationHierarchy {

        /**
         * 按指定层级对值进行泛化。
         *
         * @param value 原始值
         * @param level 泛化层级，0 表示不泛化
         * @return 泛化后的字符串
         */
        String generalize(String value, int level);

        /**
         * 返回该层次结构支持的最大泛化层级。
         *
         * @return 最大层级（包含）
         */
        int maxLevel();
    }

    /**
     * 年龄泛化层次结构工厂方法。
     * <p>
     * 层级说明：
     * <ul>
     *   <li>0：原值</li>
     *   <li>1：5 岁区间，如 [25-30]</li>
     *   <li>2：10 岁区间，如 [20-30]</li>
     *   <li>3：20 岁区间，如 [20-40]</li>
     *   <li>其他：完全泛化为 "*"</li>
     * </ul>
     * </p>
     *
     * @return 年龄泛化层次结构实例
     */
    public static GeneralizationHierarchy ageHierarchy() {
        return new GeneralizationHierarchy() {
            @Override
            public String generalize(String value, int level) {
                int age = Integer.parseInt(value);
                return switch (level) {
                    case 0 -> value;
                    case 1 -> {
                        int start = (age / 5) * 5;
                        yield String.format("[%d-%d]", start, start + 5);
                    }
                    case 2 -> {
                        int start = (age / 10) * 10;
                        yield String.format("[%d-%d]", start, start + 10);
                    }
                    case 3 -> {
                        int start = (age / 20) * 20;
                        yield String.format("[%d-%d]", start, start + 20);
                    }
                    default -> "*";
                };
            }

            @Override
            public int maxLevel() {
                return 4;
            }
        };
    }

    /**
     * 邮编泛化层次结构工厂方法。
     * <p>
     * 层级说明：
     * <ul>
     *   <li>0：原值</li>
     *   <li>1：保留前 3 位</li>
     *   <li>2：保留前 2 位</li>
     *   <li>3：保留前 1 位</li>
     *   <li>其他：完全泛化为 "*"</li>
     * </ul>
     * </p>
     *
     * @return 邮编泛化层次结构实例
     */
    public static GeneralizationHierarchy zipcodeHierarchy() {
        return new GeneralizationHierarchy() {
            @Override
            public String generalize(String value, int level) {
                return switch (level) {
                    case 0 -> value;
                    case 1 -> value.length() >= 3 ? value.substring(0, 3) + "***" : value;
                    case 2 -> value.length() >= 2 ? value.substring(0, 2) + "****" : value;
                    case 3 -> value.length() >= 1 ? value.charAt(0) + "*****" : value;
                    default -> "*";
                };
            }

            @Override
            public int maxLevel() {
                return 4;
            }
        };
    }

    /**
     * 性别泛化层次结构工厂方法。
     * <p>
     * 仅支持两级：0 为原值，大于等于 1 泛化为 "*"。
     * </p>
     *
     * @return 性别泛化层次结构实例
     */
    public static GeneralizationHierarchy genderHierarchy() {
        return new GeneralizationHierarchy() {
            @Override
            public String generalize(String value, int level) {
                return level >= 1 ? "*" : value;
            }

            @Override
            public int maxLevel() {
                return 1;
            }
        };
    }
}
