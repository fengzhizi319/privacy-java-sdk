package com.github.fengzhizi319.privacy.sdk.classification;

import com.github.fengzhizi319.privacy.sdk.PrivacyProfile;
import com.github.fengzhizi319.privacy.sdk.classification.engine.DefaultRuleEngine;
import com.github.fengzhizi319.privacy.sdk.classification.engine.LlmClassifier;
import com.github.fengzhizi319.privacy.sdk.classification.engine.NoOpLlmClassifier;
import com.github.fengzhizi319.privacy.sdk.classification.engine.NoOpSmallNerEngine;
import com.github.fengzhizi319.privacy.sdk.classification.engine.RuleEngine;
import com.github.fengzhizi319.privacy.sdk.classification.engine.SmallNerEngine;
import com.github.fengzhizi319.privacy.sdk.model.PrivacyContext;
import com.github.fengzhizi319.privacy.sdk.model.classification.AuditInfo;
import com.github.fengzhizi319.privacy.sdk.model.classification.ClassificationParams;
import com.github.fengzhizi319.privacy.sdk.model.classification.ClassificationResult;
import com.github.fengzhizi319.privacy.sdk.model.classification.EngineLayer;
import com.github.fengzhizi319.privacy.sdk.model.classification.FieldClassificationResult;
import com.github.fengzhizi319.privacy.sdk.model.classification.RecordClassificationResult;
import com.github.fengzhizi319.privacy.sdk.model.classification.SecurityTag;
import com.github.fengzhizi319.privacy.sdk.model.classification.SensitivityLevel;
import com.github.fengzhizi319.privacy.sdk.model.classification.TableClassificationResult;
import com.github.fengzhizi319.privacy.sdk.util.JsonParser;
import com.github.fengzhizi319.privacy.sdk.util.ParameterResolver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 数据分类 API（Data Classification API）。
 * <p>
 * 提供字段级、记录级、表级以及 JSON 输入的分类能力。分类流程为：
 * </p>
 * <ol>
 *   <li>参数治理：合并默认值、Profile、请求参数与人工覆盖。</li>
 *   <li>规则引擎（Layer 1）：字段名 + 值规则匹配。</li>
 *   <li>Small NER（Layer 2）：在规则未命中或等级较低时识别命名实体。</li>
 *   <li>LLM 兜底（Layer 3）：在置信度不足时保守处理。</li>
 *   <li>人工覆盖：最终按 manualOverride 调整等级。</li>
 * </ol>
 *
 * @author fengzhizi319
 * @since 0.1.0
 */
public class ClassificationApi {

    /** 隐私配置 profile，用于参数解析。 */
    private final PrivacyProfile profile;

    /** 规则引擎实例。 */
    private final RuleEngine ruleEngine;

    /** Small NER 引擎实例。 */
    private final SmallNerEngine smallNerEngine;

    /** LLM 分类器实例。 */
    private final LlmClassifier llmClassifier;

    /**
     * 使用默认配置与默认引擎构造 API。
     */
    public ClassificationApi() {
        this(PrivacyProfile.empty(), new DefaultRuleEngine(), new NoOpSmallNerEngine(), new NoOpLlmClassifier());
    }

    /**
     * 使用指定配置与默认引擎构造 API。
     *
     * @param profile 隐私配置 profile
     */
    public ClassificationApi(PrivacyProfile profile) {
        this(profile, new DefaultRuleEngine(), new NoOpSmallNerEngine(), new NoOpLlmClassifier());
    }

    /**
     * 使用自定义引擎构造 API。
     *
     * @param profile        隐私配置 profile
     * @param ruleEngine     规则引擎
     * @param smallNerEngine Small NER 引擎
     * @param llmClassifier  LLM 分类器
     */
    public ClassificationApi(PrivacyProfile profile, RuleEngine ruleEngine,
                             SmallNerEngine smallNerEngine, LlmClassifier llmClassifier) {
        this.profile = profile == null ? PrivacyProfile.empty() : profile;
        this.ruleEngine = ruleEngine == null ? new DefaultRuleEngine() : ruleEngine;
        this.smallNerEngine = smallNerEngine == null ? new NoOpSmallNerEngine() : smallNerEngine;
        this.llmClassifier = llmClassifier == null ? new NoOpLlmClassifier() : llmClassifier;
    }

    /**
     * 对单个字段进行分类。
     *
     * @param fieldName 字段名称
     * @param value     字段值
     * @param params    请求参数，可为 {@code null}
     * @return 字段级分类结果
     */
    public FieldClassificationResult classifyField(String fieldName, Object value, Map<String, Object> params) {
        ClassificationParams cp = resolveParams(params);
        String strValue = value == null ? "" : value.toString();

        FieldClassificationResult result = createEmptyResult(fieldName, strValue, cp);

        // Layer 1: Rule engine
        if (cp.isEnableRuleEngine()) {
            List<SecurityTag> ruleTags = ruleEngine.classifyField(fieldName, value, cp);
            if (!ruleTags.isEmpty()) {
                applyRuleResult(result, ruleTags);
            }
        }

        // Layer 2: Small NER（仅在规则未命中或等级 <= L3 时运行）
        if (cp.isEnableSmallNer() && shouldRunNer(result)) {
            List<SecurityTag> nerTags = smallNerEngine.recognize(fieldName, strValue);
            if (!nerTags.isEmpty()) {
                applyNerResult(result, nerTags);
            }
        }

        // Layer 3: LLM fallback（置信度不足时）
        if (cp.isEnableLlm() && result.getConfidence() < 0.6) {
            result = llmClassifier.classify(fieldName, strValue, result, cp.getDefaultLevel());
        }

        // Manual override
        applyManualOverride(result, fieldName, cp);

        return result;
    }

    /**
     * 对单条记录进行分类。
     *
     * @param record 记录映射，key 为字段名，value 为字段值
     * @param params 请求参数，可为 {@code null}
     * @return 记录级分类结果
     */
    public RecordClassificationResult classifyRecord(Map<String, Object> record, Map<String, Object> params) {
        ClassificationParams cp = resolveParams(params);
        RecordClassificationResult recordResult = new RecordClassificationResult(0);

        if (record == null || record.isEmpty()) {
            recordResult.setFinalLevel(cp.getDefaultLevel());
            recordResult.setConfidence(0.0);
            return recordResult;
        }

        int index = 0;
        for (Map.Entry<String, Object> entry : record.entrySet()) {
            FieldClassificationResult fieldResult = classifyField(entry.getKey(), entry.getValue(), params);
            recordResult.getFieldResults().put(entry.getKey(), fieldResult);
            index++;
        }

        aggregateRecordResult(recordResult);
        return recordResult;
    }

    /**
     * 对整张表/批次进行分类。
     *
     * @param schema 列名列表
     * @param rows   行数据，每行为字段名到字段值的映射
     * @param params 请求参数，可为 {@code null}
     * @return 表级分类结果
     */
    public TableClassificationResult classifyTable(List<String> schema, List<Map<String, Object>> rows, Map<String, Object> params) {
        ClassificationParams cp = resolveParams(params);
        TableClassificationResult tableResult = new TableClassificationResult(schema);

        if (rows == null || rows.isEmpty()) {
            tableResult.setFinalLevel(cp.getDefaultLevel());
            tableResult.setConfidence(0.0);
            return tableResult;
        }

        for (int i = 0; i < rows.size(); i++) {
            Map<String, Object> record = rows.get(i) == null ? Collections.emptyMap() : rows.get(i);
            RecordClassificationResult recordResult = classifyRecord(record, params);
            recordResult.setRecordIndex(i);
            tableResult.getRecordResults().add(recordResult);
        }

        aggregateTableResult(tableResult);
        return tableResult;
    }

    /**
     * 对 JSON 字符串进行分类。
     * <p>
     * 若解析为对象，则按单条记录分类；若解析为数组，则按表分类。
     * </p>
     *
     * @param jsonString JSON 字符串
     * @param params     请求参数，可为 {@code null}
     * @return 分类结果包装器
     */
    public ClassificationResult classifyJson(String jsonString, Map<String, Object> params) {
        Object parsed = JsonParser.parse(jsonString);
        AuditInfo auditInfo = buildAuditInfo();

        if (parsed instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> record = (Map<String, Object>) parsed;
            RecordClassificationResult recordResult = classifyRecord(record, params);
            return new ClassificationResult(recordResult, auditInfo);
        }

        if (parsed instanceof List) {
            @SuppressWarnings("unchecked")
            List<Object> list = (List<Object>) parsed;
            List<String> schema = new ArrayList<>();
            List<Map<String, Object>> rows = new ArrayList<>();
            for (Object item : list) {
                if (item instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> row = (Map<String, Object>) item;
                    rows.add(row);
                    for (String key : row.keySet()) {
                        if (!schema.contains(key)) {
                            schema.add(key);
                        }
                    }
                }
            }
            TableClassificationResult tableResult = classifyTable(schema, rows, params);
            return new ClassificationResult(tableResult, auditInfo);
        }

        // 解析失败：返回空记录结果
        RecordClassificationResult emptyResult = new RecordClassificationResult(0);
        ClassificationParams cp = resolveParams(params);
        emptyResult.setFinalLevel(cp.getDefaultLevel());
        emptyResult.setConfidence(0.0);
        return new ClassificationResult(emptyResult, auditInfo);
    }

    /**
     * 解析并合并分类参数。
     */
    private ClassificationParams resolveParams(Map<String, Object> requestParams) {
        ParameterResolver resolver = profile.getResolver();
        Map<String, Object> merged = resolver.resolve("classification", "classify", requestParams, new PrivacyContext());
        return ClassificationParams.fromMap(merged);
    }

    /**
     * 创建空结果（默认等级）。
     */
    private FieldClassificationResult createEmptyResult(String fieldName, String strValue, ClassificationParams cp) {
        FieldClassificationResult result = new FieldClassificationResult(fieldName);
        result.setFieldValue(strValue);
        result.setFinalLevel(cp.getDefaultLevel());
        result.setConfidence(0.0);
        result.setEngineLayer(EngineLayer.RULE);
        result.setNeedsHumanReview(false);
        result.setReasoning("未命中任何规则，按默认等级处理");
        result.setTags(new ArrayList<>());
        return result;
    }

    /**
     * 判断是否应运行 NER：规则未命中或最终等级 <= L3。
     */
    private boolean shouldRunNer(FieldClassificationResult result) {
        return result.getTags() == null || result.getTags().isEmpty()
            || (result.getFinalLevel() != null && result.getFinalLevel().getRank() <= 3);
    }

    /**
     * 应用规则引擎结果。
     */
    private void applyRuleResult(FieldClassificationResult result, List<SecurityTag> tags) {
        result.setTags(new ArrayList<>(tags));
        SensitivityLevel maxLevel = tags.stream()
            .map(SecurityTag::getLevel)
            .reduce(SensitivityLevel.L1, SensitivityLevel::max);
        result.setFinalLevel(maxLevel);
        result.setConfidence(1.0);
        result.setEngineLayer(EngineLayer.RULE);
        result.setNeedsHumanReview(false);
        String ruleIds = tags.stream()
            .map(SecurityTag::getRuleId)
            .distinct()
            .collect(Collectors.joining(", "));
        result.setReasoning("命中规则: " + ruleIds);
    }

    /**
     * 应用 NER 结果并执行升级规则。
     */
    private void applyNerResult(FieldClassificationResult result, List<SecurityTag> nerTags) {
        boolean hasDisease = false;
        boolean hasPii = false;
        double diseaseConfidence = 0.0;
        double piiConfidence = 0.0;
        int piiCount = 0;
        List<SecurityTag> adopted = new ArrayList<>(result.getTags());

        for (SecurityTag tag : nerTags) {
            String category = tag.getCategory();
            if ("SENSITIVE_DISEASE".equals(category)) {
                hasDisease = true;
                diseaseConfidence = Math.max(diseaseConfidence, tag.getConfidence());
            } else if ("PII_NAME".equals(category) || "PII_ID".equals(category)) {
                hasPii = true;
                piiConfidence += tag.getConfidence();
                piiCount++;
                adopted.add(tag);
            } else if ("GENOMIC_HINT".equals(category)) {
                SecurityTag upgraded = new SecurityTag(
                    SensitivityLevel.L5, "GENOMIC_HINT", tag.getConfidence(), "SMALL_NER", "NER_G_001"
                );
                upgraded.setNeedsHumanReview(true);
                adopted.add(upgraded);
            } else {
                adopted.add(tag);
            }
        }

        if (hasDisease && hasPii) {
            double avgConfidence = (diseaseConfidence + (piiCount > 0 ? piiConfidence / piiCount : 0)) / 2.0;
            SecurityTag compound = new SecurityTag(
                SensitivityLevel.L4, "SENSITIVE_DISEASE_WITH_PII", avgConfidence, "SMALL_NER", "NER_001"
            );
            adopted.add(compound);
        }

        result.setTags(adopted);
        SensitivityLevel maxLevel = adopted.stream()
            .map(SecurityTag::getLevel)
            .reduce(result.getFinalLevel(), SensitivityLevel::max);
        result.setFinalLevel(maxLevel);

        double nerConfidence = nerTags.stream()
            .mapToDouble(SecurityTag::getConfidence)
            .max()
            .orElse(0.0);
        result.setConfidence(nerConfidence);
        result.setEngineLayer(EngineLayer.SMALL_NER);

        if (nerConfidence > 0.9) {
            result.setNeedsHumanReview(false);
        } else if (nerConfidence >= 0.7) {
            result.setNeedsHumanReview(true);
        } else {
            result.setNeedsHumanReview(true);
        }
        result.setReasoning("NER 识别到实体，置信度=" + String.format("%.2f", nerConfidence));
    }

    /**
     * 应用人工覆盖。
     */
    private void applyManualOverride(FieldClassificationResult result, String fieldName, ClassificationParams cp) {
        if (fieldName == null || cp.getManualOverride() == null) {
            return;
        }
        String override = cp.getManualOverride().get(fieldName);
        if (override == null) {
            return;
        }
        SensitivityLevel level = SensitivityLevel.fromString(override);
        if (level == null) {
            return;
        }
        result.setFinalLevel(level);
        result.setEngineLayer(EngineLayer.RULE);
        result.setNeedsHumanReview(false);
        result.setReasoning("人工覆盖为 " + level);

        SecurityTag manualTag = new SecurityTag(level, "MANUAL_OVERRIDE", 1.0, "MANUAL", "MANUAL_001");
        manualTag.setNeedsHumanReview(false);
        if (result.getTags() == null) {
            result.setTags(new ArrayList<>());
        }
        result.getTags().add(manualTag);
    }

    /**
     * 聚合记录级结果。
     */
    private void aggregateRecordResult(RecordClassificationResult recordResult) {
        SensitivityLevel maxLevel = null;
        double maxConfidence = 0.0;
        boolean needsReview = false;
        List<SecurityTag> aggregated = new ArrayList<>();

        for (FieldClassificationResult fieldResult : recordResult.getFieldResults().values()) {
            maxLevel = SensitivityLevel.max(maxLevel, fieldResult.getFinalLevel());
            maxConfidence = Math.max(maxConfidence, fieldResult.getConfidence());
            needsReview = needsReview || fieldResult.isNeedsHumanReview();
            if (fieldResult.getTags() != null) {
                aggregated.addAll(fieldResult.getTags());
            }
        }

        recordResult.setFinalLevel(maxLevel == null ? SensitivityLevel.L1 : maxLevel);
        recordResult.setConfidence(maxConfidence);
        recordResult.setNeedsHumanReview(needsReview);
        recordResult.setAggregatedTags(aggregated);
    }

    /**
     * 聚合表级结果。
     */
    private void aggregateTableResult(TableClassificationResult tableResult) {
        SensitivityLevel maxLevel = null;
        double maxConfidence = 0.0;
        boolean needsReview = false;
        List<SecurityTag> aggregated = new ArrayList<>();

        for (RecordClassificationResult recordResult : tableResult.getRecordResults()) {
            maxLevel = SensitivityLevel.max(maxLevel, recordResult.getFinalLevel());
            maxConfidence = Math.max(maxConfidence, recordResult.getConfidence());
            needsReview = needsReview || recordResult.isNeedsHumanReview();
            if (recordResult.getAggregatedTags() != null) {
                aggregated.addAll(recordResult.getAggregatedTags());
            }
        }

        tableResult.setFinalLevel(maxLevel == null ? SensitivityLevel.L1 : maxLevel);
        tableResult.setConfidence(maxConfidence);
        tableResult.setNeedsHumanReview(needsReview);
        tableResult.setAggregatedTags(aggregated);
    }

    /**
     * 构造审计信息。
     */
    private AuditInfo buildAuditInfo() {
        AuditInfo auditInfo = new AuditInfo();
        auditInfo.setRuleEngineVersion(ruleEngine.getVersion());
        return auditInfo;
    }

    /**
     * 从 ResultSet 读取结果并进行分类。
     *
     * @param rs     ResultSet 实例，由调用方负责关闭
     * @param params 请求参数，可为 {@code null}
     * @return 表级分类结果
     * @throws RuntimeException 当 SQL 操作失败时抛出
     */
    public TableClassificationResult classifyResultSet(java.sql.ResultSet rs, Map<String, Object> params) {
        if (rs == null) {
            throw new IllegalArgumentException("ResultSet is null");
        }
        try {
            java.sql.ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            List<String> schema = new ArrayList<>(columnCount);
            for (int i = 1; i <= columnCount; i++) {
                schema.add(metaData.getColumnLabel(i));
            }

            List<Map<String, Object>> rows = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> record = new LinkedHashMap<>(columnCount);
                for (int i = 1; i <= columnCount; i++) {
                    String colName = metaData.getColumnLabel(i);
                    Object val = rs.getObject(i);
                    record.put(colName, val);
                }
                rows.add(record);
            }
            return classifyTable(schema, rows, params);
        } catch (java.sql.SQLException e) {
            throw new RuntimeException("Failed to classify ResultSet", e);
        }
    }

}
