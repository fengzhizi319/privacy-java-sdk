package com.github.fengzhizi319.privacy.sdk.model.classification;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据分类参数（Classification Parameters）。
 * <p>
 * 封装分类原语的运行参数，包括默认等级、各引擎开关、ICD-10 L4 区间、基因组关键词、公开字段白名单、
 * 运营统计模式以及人工覆盖映射。
 * </p>
 *
 * @author fengzhizi319
 * @since 0.1.0
 */
public class ClassificationParams {

    /** 原语版本。 */
    private String version = "1.0.0";

    /** 默认敏感度等级，当所有引擎均未命中时使用。 */
    private SensitivityLevel defaultLevel = SensitivityLevel.L3;

    /** 是否启用规则引擎。 */
    private boolean enableRuleEngine = true;

    /** 是否启用 Small NER 引擎。 */
    private boolean enableSmallNer = false;

    /** 是否启用 LLM 分类器。 */
    private boolean enableLlm = false;

    /** ICD-10 L4 区间列表，每项包含 start 与 end。 */
    private List<Icd10Interval> icd10L4Intervals = defaultIcd10Intervals();

    /** 基因组关键词列表。 */
    private List<String> genomicKeywords = defaultGenomicKeywords();

    /** 公开字段白名单。 */
    private List<String> publicFieldWhitelist = defaultPublicFieldWhitelist();

    /** 运营统计字段模式。 */
    private List<String> operationalFieldPatterns = defaultOperationalPatterns();

    /** 人工覆盖映射：字段名 -> 敏感度等级字符串。 */
    private Map<String, String> manualOverride = new HashMap<>();

    /**
     * ICD-10 L4 区间定义。
     */
    public static class Icd10Interval {
        private String start;
        private String end;

        public Icd10Interval() {
        }

        public Icd10Interval(String start, String end) {
            this.start = start;
            this.end = end;
        }

        public String getStart() {
            return start;
        }

        public void setStart(String start) {
            this.start = start;
        }

        public String getEnd() {
            return end;
        }

        public void setEnd(String end) {
            this.end = end;
        }
    }

    /**
     * 默认构造器。
     */
    public ClassificationParams() {
    }

    /**
     * 从参数映射构造分类参数。
     *
     * @param params 参数映射
     * @return 分类参数对象
     */
    public static ClassificationParams fromMap(Map<String, Object> params) {
        ClassificationParams cp = new ClassificationParams();
        if (params == null) {
            return cp;
        }
        if (params.containsKey("version")) {
            cp.version = String.valueOf(params.get("version"));
        }
        if (params.containsKey("default_level")) {
            SensitivityLevel level = SensitivityLevel.fromString(String.valueOf(params.get("default_level")));
            if (level != null) {
                cp.defaultLevel = level;
            }
        }
        if (params.containsKey("enable_rule_engine")) {
            cp.enableRuleEngine = Boolean.parseBoolean(String.valueOf(params.get("enable_rule_engine")));
        }
        if (params.containsKey("enable_small_ner")) {
            cp.enableSmallNer = Boolean.parseBoolean(String.valueOf(params.get("enable_small_ner")));
        }
        if (params.containsKey("enable_llm")) {
            cp.enableLlm = Boolean.parseBoolean(String.valueOf(params.get("enable_llm")));
        }
        if (params.containsKey("icd10_l4_intervals")) {
            cp.icd10L4Intervals = parseIcd10Intervals(params.get("icd10_l4_intervals"));
        }
        if (params.containsKey("genomic_keywords")) {
            cp.genomicKeywords = parseStringList(params.get("genomic_keywords"));
        }
        if (params.containsKey("public_field_whitelist")) {
            cp.publicFieldWhitelist = parseStringList(params.get("public_field_whitelist"));
        }
        if (params.containsKey("operational_field_patterns")) {
            cp.operationalFieldPatterns = parseStringList(params.get("operational_field_patterns"));
        }
        if (params.containsKey("manual_override")) {
            cp.manualOverride = parseStringMap(params.get("manual_override"));
        }
        return cp;
    }

    private static List<Icd10Interval> parseIcd10Intervals(Object obj) {
        List<Icd10Interval> list = new ArrayList<>();
        if (obj instanceof List) {
            for (Object item : (List<?>) obj) {
                if (item instanceof Map) {
                    Map<?, ?> m = (Map<?, ?>) item;
                    String start = m.get("start") == null ? null : String.valueOf(m.get("start"));
                    String end = m.get("end") == null ? null : String.valueOf(m.get("end"));
                    if (start != null && end != null) {
                        list.add(new Icd10Interval(start, end));
                    }
                }
            }
        }
        return list.isEmpty() ? defaultIcd10Intervals() : list;
    }

    @SuppressWarnings("unchecked")
    private static List<String> parseStringList(Object obj) {
        if (obj instanceof List) {
            List<String> result = new ArrayList<>();
            for (Object item : (List<?>) obj) {
                result.add(String.valueOf(item));
            }
            return result;
        }
        return new ArrayList<>();
    }

    @SuppressWarnings("unchecked")
    private static Map<String, String> parseStringMap(Object obj) {
        if (obj instanceof Map) {
            Map<String, String> result = new HashMap<>();
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) obj).entrySet()) {
                result.put(String.valueOf(entry.getKey()), String.valueOf(entry.getValue()));
            }
            return result;
        }
        return new HashMap<>();
    }

    private static List<Icd10Interval> defaultIcd10Intervals() {
        List<Icd10Interval> intervals = new ArrayList<>();
        intervals.add(new Icd10Interval("B20", "B24"));
        intervals.add(new Icd10Interval("F20", "F29"));
        intervals.add(new Icd10Interval("C00", "C97"));
        return intervals;
    }

    private static List<String> defaultGenomicKeywords() {
        return List.of("brca1", "brca2", "tp53", "rs", "snp", "cnv", "genome", "genomic", "gene", "mutation", "variant");
    }

    private static List<String> defaultPublicFieldWhitelist() {
        return List.of("public_report", "annual_summary", "科普");
    }

    private static List<String> defaultOperationalPatterns() {
        return List.of("turnover_rate", "device_usage", "inventory");
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public SensitivityLevel getDefaultLevel() {
        return defaultLevel;
    }

    public void setDefaultLevel(SensitivityLevel defaultLevel) {
        this.defaultLevel = defaultLevel;
    }

    public boolean isEnableRuleEngine() {
        return enableRuleEngine;
    }

    public void setEnableRuleEngine(boolean enableRuleEngine) {
        this.enableRuleEngine = enableRuleEngine;
    }

    public boolean isEnableSmallNer() {
        return enableSmallNer;
    }

    public void setEnableSmallNer(boolean enableSmallNer) {
        this.enableSmallNer = enableSmallNer;
    }

    public boolean isEnableLlm() {
        return enableLlm;
    }

    public void setEnableLlm(boolean enableLlm) {
        this.enableLlm = enableLlm;
    }

    public List<Icd10Interval> getIcd10L4Intervals() {
        return icd10L4Intervals;
    }

    public void setIcd10L4Intervals(List<Icd10Interval> icd10L4Intervals) {
        this.icd10L4Intervals = icd10L4Intervals == null ? defaultIcd10Intervals() : icd10L4Intervals;
    }

    public List<String> getGenomicKeywords() {
        return genomicKeywords;
    }

    public void setGenomicKeywords(List<String> genomicKeywords) {
        this.genomicKeywords = genomicKeywords == null ? defaultGenomicKeywords() : genomicKeywords;
    }

    public List<String> getPublicFieldWhitelist() {
        return publicFieldWhitelist;
    }

    public void setPublicFieldWhitelist(List<String> publicFieldWhitelist) {
        this.publicFieldWhitelist = publicFieldWhitelist == null ? defaultPublicFieldWhitelist() : publicFieldWhitelist;
    }

    public List<String> getOperationalFieldPatterns() {
        return operationalFieldPatterns;
    }

    public void setOperationalFieldPatterns(List<String> operationalFieldPatterns) {
        this.operationalFieldPatterns = operationalFieldPatterns == null ? defaultOperationalPatterns() : operationalFieldPatterns;
    }

    public Map<String, String> getManualOverride() {
        return manualOverride;
    }

    public void setManualOverride(Map<String, String> manualOverride) {
        this.manualOverride = manualOverride == null ? new HashMap<>() : manualOverride;
    }
}
