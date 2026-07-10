package com.github.fengzhizi319.privacy.sdk.util;

import com.github.fengzhizi319.privacy.sdk.model.PrivacyContext;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 隐私参数解析器（Parameter Resolver）。
 * <p>
 * 按照以下优先级合并参数，为各隐私原语生成最终执行参数：
 * </p>
 * <ol>
 *   <li>内置默认值（{@link #defaultParams(String)}）</li>
 *   <li>模板参数（当前版本为空，保留扩展）</li>
 *   <li>YAML profile 中对应 primitive 的配置（{@link #profileParams(String)}）</li>
 *   <li>上下文覆盖（当前版本为空，保留扩展）</li>
 *   <li>请求参数（最高优先级）</li>
 * </ol>
 * 合并完成后会执行参数校验（{@link #validate(String, Map)}）。
 *
 * @author fengzhizi319
 * @since 0.1.0
 */
public class ParameterResolver {

    /** 从 YAML 加载的原始 profile 配置映射。 */
    private final Map<String, Object> profile;

    /**
     * 构造空解析器，内部不含任何 profile 配置。
     */
    public ParameterResolver() {
        this.profile = new HashMap<>();
    }

    /**
     * 从指定 YAML 文件构造解析器。
     *
     * @param profilePath YAML 文件路径；若为 {@code null} 或文件不存在，则等效于空解析器
     * @throws RuntimeException 当 YAML 解析失败时抛出
     */
    public ParameterResolver(Path profilePath) {
        if (profilePath == null || !Files.exists(profilePath)) {
            this.profile = new HashMap<>();
        } else {
            try (InputStream is = Files.newInputStream(profilePath)) {
                Yaml yaml = new Yaml();
                Object loaded = yaml.load(is);
                this.profile = loaded instanceof Map ? (Map<String, Object>) loaded : new HashMap<>();
            } catch (Exception e) {
                throw new RuntimeException("Failed to load privacy profile: " + profilePath, e);
            }
        }
    }

    /**
     * 解析指定隐私原语的执行参数。
     *
     * @param primitive     隐私原语名称，例如 "dp"、"k_anonymity"、"sanitization"、"qol"
     * @param action        动作名称（当前保留，用于未来区分同一原语的不同操作）
     * @param requestParams 请求传入的参数映射，可为 {@code null}
     * @param context       隐私上下文
     * @return 合并并校验后的参数映射
     * @throws IllegalArgumentException 当参数校验失败时抛出
     */
    public Map<String, Object> resolve(String primitive, String action,
                                       Map<String, Object> requestParams,
                                       PrivacyContext context) {
        Map<String, Object> params = new HashMap<>();
        // 1. defaults
        params.putAll(defaultParams(primitive));
        // 2. template (simplified: use profile default template if exists)
        params.putAll(templateParams(primitive));
        // 3. profile primitive params
        params.putAll(profileParams(primitive));
        // 4. context overrides
        params.putAll(contextOverrides(primitive, context));
        // 5. request params (highest priority)
        if (requestParams != null) {
            params.putAll(requestParams);
        }
        validate(primitive, params);
        return params;
    }

    /**
     * 获取指定原语的内置默认参数。
     *
     * @param primitive 隐私原语名称
     * @return 默认参数映射
     */
    private Map<String, Object> defaultParams(String primitive) {
        return switch (primitive) {
            case "dp" -> Map.of("epsilon", 1.0, "delta", 1e-5, "mechanism", "laplace");
            case "k_anonymity" -> Map.of("k", 5, "l", 2, "t", 0.2, "max_depth", 10);
            case "sanitization" -> Map.of("engine", "mask");
            case "qol" -> Map.of("num_dummies", 3);
            case "classification" -> Map.of(
                "version", "1.0.0",
                "default_level", "L3",
                "enable_rule_engine", true,
                "enable_small_ner", false,
                "enable_llm", false,
                "icd10_l4_intervals", List.of(
                    Map.of("start", "B20", "end", "B24"),
                    Map.of("start", "F20", "end", "F29"),
                    Map.of("start", "C00", "end", "C97")
                ),
                "genomic_keywords", List.of("brca1", "brca2", "tp53", "rs", "snp", "cnv", "genome", "genomic", "gene", "mutation", "variant"),
                "public_field_whitelist", List.of("public_report", "annual_summary", "科普"),
                "operational_field_patterns", List.of("turnover_rate", "device_usage", "inventory"),
                "manual_override", Map.of()
            );
            default -> new HashMap<>();
        };
    }

    /**
     * 获取模板参数（当前为简化实现，返回空映射）。
     * <p>
     * 未来可结合 {@link PrivacyContext} 中的 purpose 或敏感度标签匹配不同模板。
     * </p>
     *
     * @param primitive 隐私原语名称
     * @return 模板参数映射
     */
    private Map<String, Object> templateParams(String primitive) {
        // Simplified: could match by context purpose/sensitivity
        return new HashMap<>();
    }

    /**
     * 从 YAML profile 的 {@code primitives} 节点读取对应原语参数。
     *
     * @param primitive 隐私原语名称
     * @return profile 中配置的参数映射，若不存在则为空映射
     */
    private Map<String, Object> profileParams(String primitive) {
        Object primitives = profile.get("primitives");
        if (primitives instanceof Map) {
            Object p = ((Map<String, Object>) primitives).get(primitive);
            if (p instanceof Map) {
                return new HashMap<>((Map<String, Object>) p);
            }
        }
        return new HashMap<>();
    }

    /**
     * 根据上下文生成覆盖参数（当前为简化实现，返回空映射）。
     *
     * @param primitive 隐私原语名称
     * @param context   隐私上下文
     * @return 上下文覆盖参数映射
     */
    private Map<String, Object> contextOverrides(String primitive, PrivacyContext context) {
        // Simplified: no complex context overrides in MVP
        return new HashMap<>();
    }

    /**
     * 校验合并后的参数是否合法。
     *
     * @param primitive 隐私原语名称
     * @param params    合并后的参数映射
     * @throws IllegalArgumentException 当 epsilon 非正或 k 小于 2 时抛出
     */
    private void validate(String primitive, Map<String, Object> params) {
        if ("dp".equals(primitive)) {
            double epsilon = ((Number) params.getOrDefault("epsilon", 0.0)).doubleValue();
            if (epsilon <= 0) {
                throw new IllegalArgumentException("DP epsilon must be positive");
            }
        } else if ("k_anonymity".equals(primitive)) {
            int k = ((Number) params.getOrDefault("k", 0)).intValue();
            if (k < 2) {
                throw new IllegalArgumentException("K-Anonymity k must be >= 2");
            }
        } else if ("classification".equals(primitive)) {
            String defaultLevel = String.valueOf(params.getOrDefault("default_level", "L3"));
            if (com.github.fengzhizi319.privacy.sdk.model.classification.SensitivityLevel.fromString(defaultLevel) == null) {
                throw new IllegalArgumentException("Classification default_level must be one of L1~L5");
            }
        }
    }
}
