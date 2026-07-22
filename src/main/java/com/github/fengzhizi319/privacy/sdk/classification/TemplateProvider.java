package com.github.fengzhizi319.privacy.sdk.classification;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 合规模板参数提供者（Template Provider）。
 * <p>
 * 提供 GB/T 35273、GDPR、JR/T 0197 三种合规模板的默认参数，
 * 供 {@link com.github.fengzhizi319.privacy.sdk.util.ParameterResolver} 与分类 API 使用。
 * </p>
 *
 * @author fengzhizi319
 * @since 0.1.0
 */
public final class TemplateProvider {

    private TemplateProvider() {
    }

    /** GB/T 35273 个人信息安全规范模板。 */
    private static final Map<String, Object> GBT35273 = Map.of(
        "version", "gbt35273-1.0.0",
        "default_level", "L3",
        "genomic_keywords", List.of(
            "brca1", "brca2", "tp53", "rs", "snp", "cnv", "genome", "genomic",
            "gene", "mutation", "variant", "biometric", "fingerprint", "face"
        ),
        "icd10_l4_intervals", List.of(
            Map.of("start", "B20", "end", "B24"),
            Map.of("start", "F20", "end", "F29"),
            Map.of("start", "C00", "end", "C97"),
            Map.of("start", "E10", "end", "E14")
        )
    );

    /** GDPR 通用数据保护条例模板。 */
    private static final Map<String, Object> GDPR = Map.of(
        "version", "gdpr-1.0.0",
        "default_level", "L3",
        "genomic_keywords", List.of(
            "brca1", "brca2", "tp53", "rs", "snp", "cnv", "genome", "genomic",
            "gene", "mutation", "variant", "biometric", "health", "genetic",
            "race", "ethnicity", "political", "religion", "sexual"
        ),
        "icd10_l4_intervals", List.of(
            Map.of("start", "B20", "end", "B24"),
            Map.of("start", "F20", "end", "F29"),
            Map.of("start", "C00", "end", "C97")
        )
    );

    /** JR/T 0197 金融数据安全模板。 */
    private static final Map<String, Object> JRT0197 = Map.of(
        "version", "jrt0197-1.0.0",
        "default_level", "L3",
        "genomic_keywords", List.of(
            "brca1", "brca2", "tp53", "rs", "snp", "cnv", "genome", "genomic",
            "gene", "mutation", "variant", "bank_card", "bankcard", "card_no",
            "account", "credit", "transaction", "asset", "balance"
        ),
        "icd10_l4_intervals", List.of(
            Map.of("start", "B20", "end", "B24"),
            Map.of("start", "F20", "end", "F29"),
            Map.of("start", "C00", "end", "C97")
        )
    );

    private static final Map<String, Map<String, Object>> TEMPLATES = Map.of(
        "gbt35273", GBT35273,
        "gdpr", GDPR,
        "jrt0197", JRT0197
    );

    /**
     * 获取指定合规模板的默认参数。
     *
     * @param template 模板名称，如 {@code gbt35273}、{@code gdpr}、{@code jrt0197}
     * @return 模板参数字典；模板不存在时返回空字典
     */
    public static Map<String, Object> getTemplateParams(String template) {
        if (template == null) {
            return new HashMap<>();
        }
        Map<String, Object> params = TEMPLATES.get(template.toLowerCase());
        return params == null ? new HashMap<>() : new HashMap<>(params);
    }
}
