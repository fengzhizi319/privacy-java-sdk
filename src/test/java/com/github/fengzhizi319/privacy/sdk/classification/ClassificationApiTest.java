package com.github.fengzhizi319.privacy.sdk.classification;

import com.github.fengzhizi319.privacy.sdk.PrivacyProfile;
import com.github.fengzhizi319.privacy.sdk.model.classification.ClassificationResult;
import com.github.fengzhizi319.privacy.sdk.model.classification.EngineLayer;
import com.github.fengzhizi319.privacy.sdk.model.classification.FieldClassificationResult;
import com.github.fengzhizi319.privacy.sdk.model.classification.RecordClassificationResult;
import com.github.fengzhizi319.privacy.sdk.model.classification.SecurityTag;
import com.github.fengzhizi319.privacy.sdk.model.classification.SensitivityLevel;
import com.github.fengzhizi319.privacy.sdk.model.classification.TableClassificationResult;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link ClassificationApi} 的单元测试。
 * <p>
 * 覆盖规范中定义的 20 个通用测试用例，以及参数覆盖与表聚合场景。
 * </p>
 *
 * <p><b>说明：</b>测试用例 1 中的身份证号在规范原文中校验和存在笔误，
 * 本测试使用校验和正确的同前缀号码 110101199001011237 来验证合法身份证识别。</p>
 *
 * @author fengzhizi319
 * @since 0.1.0
 */
class ClassificationApiTest {

    /** 被测 API 实例。 */
    private final ClassificationApi api = new ClassificationApi();

    /**
     * 辅助方法：断言字段结果中存在指定类别的标签。
     */
    private void assertHasTag(FieldClassificationResult result, SensitivityLevel expectedLevel, String expectedCategory) {
        assertNotNull(result, "result should not be null");
        assertEquals(expectedLevel, result.getFinalLevel(),
            "finalLevel mismatch for " + expectedCategory);
        boolean found = result.getTags().stream()
            .anyMatch(tag -> expectedCategory.equals(tag.getCategory()));
        assertTrue(found, "expected tag category " + expectedCategory + " not found in " + result.getTags());
    }

    /**
     * 辅助方法：断言字段结果中不存在指定类别的标签。
     */
    private void assertNoTag(FieldClassificationResult result, String unexpectedCategory) {
        assertNotNull(result, "result should not be null");
        boolean found = result.getTags().stream()
            .anyMatch(tag -> unexpectedCategory.equals(tag.getCategory()));
        assertFalse(found, "unexpected tag category " + unexpectedCategory + " found in " + result.getTags());
    }

    /**
     * 测试用例 1：合法中国大陆身份证号。
     */
    @Test
    void testIdCardValid() {
        // 规范原文 11010119900101123X 校验和不匹配；使用校验和正确的 110101199001011237
        FieldClassificationResult r = api.classifyField("id_card", "110101199001011237", null);
        assertHasTag(r, SensitivityLevel.L3, "PII_ID_CARD");
        assertEquals(1.0, r.getConfidence());
        assertEquals(EngineLayer.RULE, r.getEngineLayer());
    }

    /**
     * 测试用例 2：校验和错误的身份证号不应被识别为 PII_ID_CARD。
     */
    @Test
    void testIdCardInvalidChecksum() {
        FieldClassificationResult r = api.classifyField("id_card", "110101199001011234", null);
        assertNoTag(r, "PII_ID_CARD");
        assertTrue(r.getFinalLevel().getRank() <= 3, "fallback level should be L3 or lower");
    }

    /**
     * 测试用例 3：合法手机号。
     */
    @Test
    void testMobileValid() {
        FieldClassificationResult r = api.classifyField("mobile", "13800138000", null);
        assertHasTag(r, SensitivityLevel.L3, "PII_MOBILE");
    }

    /**
     * 测试用例 4：非法手机号前缀不应被识别为 PII_MOBILE。
     */
    @Test
    void testMobileInvalidPrefix() {
        FieldClassificationResult r = api.classifyField("mobile", "12800138000", null);
        assertNoTag(r, "PII_MOBILE");
        assertTrue(r.getFinalLevel().getRank() <= 3, "fallback level should be L3 or lower");
    }

    /**
     * 测试用例 5：上海医保卡号（校验和正确）。
     */
    @Test
    void testShanghaiMedicalCardValid() {
        FieldClassificationResult r = api.classifyField("medical_card", "123456789", null);
        assertHasTag(r, SensitivityLevel.L3, "PII_MEDICAL_CARD");
    }

    /**
     * 测试用例 6：ICD-10 HIV 区间 B21.1。
     */
    @Test
    void testIcd10Hiv() {
        FieldClassificationResult r = api.classifyField("diagnosis", "B21.1", null);
        assertHasTag(r, SensitivityLevel.L4, "MEDICAL_ICD10_HIV");
    }

    /**
     * 测试用例 7：ICD-10 精神疾病区间 F25。
     */
    @Test
    void testIcd10Psychiatric() {
        FieldClassificationResult r = api.classifyField("diagnosis", "F25", null);
        assertHasTag(r, SensitivityLevel.L4, "MEDICAL_ICD10_PSYCHIATRIC");
    }

    /**
     * 测试用例 8：ICD-10 恶性肿瘤区间 C78.0。
     */
    @Test
    void testIcd10Cancer() {
        FieldClassificationResult r = api.classifyField("diagnosis", "C78.0", null);
        assertHasTag(r, SensitivityLevel.L4, "MEDICAL_ICD10_CANCER");
    }

    /**
     * 测试用例 9：ICD-10 普通编码 J18.9。
     */
    @Test
    void testIcd10General() {
        FieldClassificationResult r = api.classifyField("diagnosis", "J18.9", null);
        assertHasTag(r, SensitivityLevel.L3, "MEDICAL_ICD10_GENERAL");
    }

    /**
     * 测试用例 10：字段名包含 brca1。
     */
    @Test
    void testGenomicBrca1() {
        FieldClassificationResult r = api.classifyField("brca1_status", "positive", null);
        assertHasTag(r, SensitivityLevel.L5, "GENOMIC_BRCA_TP53");
    }

    /**
     * 测试用例 11：rs 编号字段。
     */
    @Test
    void testGenomicRsNumber() {
        FieldClassificationResult r = api.classifyField("rs_number", "rs12345", null);
        assertHasTag(r, SensitivityLevel.L5, "GENOMIC_VARIANT");
    }

    /**
     * 测试用例 12：BAM 文件头。
     */
    @Test
    void testGenomicBamHeader() {
        FieldClassificationResult r = api.classifyField("file_content", "BAM\u0001header", null);
        assertHasTag(r, SensitivityLevel.L5, "GENOMIC_BAM");
    }

    /**
     * 测试用例 13：VCF 文件头。
     */
    @Test
    void testGenomicVcfHeader() {
        FieldClassificationResult r = api.classifyField("file_content", "##fileformat=VCFv4.2", null);
        assertHasTag(r, SensitivityLevel.L5, "GENOMIC_VCF");
    }

    /**
     * 测试用例 14：SAM/BAM 序列头 @SQ。
     */
    @Test
    void testGenomicSamHeader() {
        FieldClassificationResult r = api.classifyField("file_content", "@SQ SN:chr1 LN:1000", null);
        assertHasTag(r, SensitivityLevel.L5, "GENOMIC_BAM");
    }

    /**
     * 测试用例 15：长基因序列片段。
     */
    @Test
    void testGenomicSequence() {
        String sequence = "ATCGATCGATCG".repeat(5); // 60 chars
        FieldClassificationResult r = api.classifyField("sequence", sequence, null);
        assertHasTag(r, SensitivityLevel.L5, "GENOMIC_SEQUENCE");
    }

    /**
     * 测试用例 16：公开报表字段白名单。
     */
    @Test
    void testPublicReport() {
        FieldClassificationResult r = api.classifyField("public_report", "2023 annual summary", null);
        assertHasTag(r, SensitivityLevel.L1, "PUBLIC_REPORT");
    }

    /**
     * 测试用例 17：运营统计字段。
     */
    @Test
    void testOperationalStat() {
        FieldClassificationResult r = api.classifyField("turnover_rate", "0.85", null);
        assertHasTag(r, SensitivityLevel.L2, "OPERATIONAL_STAT");
    }

    /**
     * 测试用例 18：普通姓名无高敏感标签。
     */
    @Test
    void testNameFallback() {
        FieldClassificationResult r = api.classifyField("name", "Alice", null);
        assertNoTag(r, "PII_ID_CARD");
        assertNoTag(r, "PII_MOBILE");
        assertNoTag(r, "MEDICAL_ICD10_HIV");
        assertNoTag(r, "GENOMIC_BRCA_TP53");
        assertTrue(r.getFinalLevel().getRank() <= 3, "fallback level should be L3 or lower");
    }

    /**
     * 测试用例 19：单条记录聚合，最终等级取字段最高 L4。
     */
    @Test
    void testRecordAggregation() {
        Map<String, Object> record = new LinkedHashMap<>();
        record.put("id_card", "110101199001011237");
        record.put("mobile", "13800138000");
        record.put("diagnosis", "B21.1");

        RecordClassificationResult r = api.classifyRecord(record, null);
        assertEquals(SensitivityLevel.L4, r.getFinalLevel());
        assertTrue(r.getAggregatedTags().stream()
                .anyMatch(tag -> "PII_ID_CARD".equals(tag.getCategory())),
            "should contain PII_ID_CARD tag");
        assertTrue(r.getAggregatedTags().stream()
                .anyMatch(tag -> "MEDICAL_ICD10_HIV".equals(tag.getCategory())),
            "should contain MEDICAL_ICD10_HIV tag");
    }

    /**
     * 测试用例 20：整张表聚合，最终等级取最高 L5。
     */
    @Test
    void testTableAggregation() {
        List<String> schema = List.of("id_card", "brca1_status", "diagnosis");
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id_card", "110101199001011237");
        row.put("brca1_status", "positive");
        row.put("diagnosis", "C78.0");

        TableClassificationResult r = api.classifyTable(schema, List.of(row), null);
        assertEquals(SensitivityLevel.L5, r.getFinalLevel());
        assertTrue(r.getAggregatedTags().stream()
                .anyMatch(tag -> "GENOMIC_BRCA_TP53".equals(tag.getCategory())),
            "should contain GENOMIC_BRCA_TP53 tag");
        assertTrue(r.getAggregatedTags().stream()
                .anyMatch(tag -> "MEDICAL_ICD10_CANCER".equals(tag.getCategory())),
            "should contain MEDICAL_ICD10_CANCER tag");
    }

    /**
     * 额外测试：人工覆盖参数可将高敏感字段降级为指定等级。
     */
    @Test
    void testManualOverride() {
        Map<String, Object> params = Map.of(
            "manual_override", Map.of("id_card", "L1")
        );
        FieldClassificationResult r = api.classifyField("id_card", "110101199001011237", params);
        assertEquals(SensitivityLevel.L1, r.getFinalLevel());
        assertTrue(r.getTags().stream()
                .anyMatch(tag -> "MANUAL_OVERRIDE".equals(tag.getCategory())),
            "should contain MANUAL_OVERRIDE tag");
    }

    /**
     * 额外测试：通过请求参数禁用规则引擎后，字段 fallback 到默认等级。
     */
    @Test
    void testDisableRuleEngine() {
        Map<String, Object> params = Map.of("enable_rule_engine", false);
        FieldClassificationResult r = api.classifyField("id_card", "110101199001011237", params);
        assertNoTag(r, "PII_ID_CARD");
        assertEquals(SensitivityLevel.L3, r.getFinalLevel());
    }

    /**
     * 额外测试：JSON 对象输入按记录分类。
     */
    @Test
    void testClassifyJsonObject() {
        String json = "{\"id_card\":\"110101199001011237\",\"diagnosis\":\"B21.1\"}";
        ClassificationResult result = api.classifyJson(json, null);
        assertNotNull(result.getRecordResult());
        assertEquals(SensitivityLevel.L4, result.getRecordResult().getFinalLevel());
    }

    /**
     * 额外测试：JSON 数组输入按表分类。
     */
    @Test
    void testClassifyJsonArray() {
        String json = "[{\"id_card\":\"110101199001011237\",\"brca1_status\":\"positive\"}]";
        ClassificationResult result = api.classifyJson(json, null);
        assertNotNull(result.getTableResult());
        assertEquals(SensitivityLevel.L5, result.getTableResult().getFinalLevel());
    }

    /**
     * 额外测试：ClassificationClient 暴露 classification() 访问器。
     */
    @Test
    void testClassificationClientAccessor() {
        ClassificationClient client = new ClassificationClient();
        assertNotNull(client.classification());
        FieldClassificationResult r = client.classification().classifyField("mobile", "13800138000", null);
        assertHasTag(r, SensitivityLevel.L3, "PII_MOBILE");
    }

    /**
     * 额外测试：ClassificationClient 支持传入 PrivacyProfile。
     */
    @Test
    void testClassificationClientWithProfile() {
        PrivacyProfile profile = PrivacyProfile.empty();
        ClassificationClient client = new ClassificationClient(profile);
        assertNotNull(client.getProfile());
        assertNotNull(client.classification());
    }

    /**
     * 额外测试：标签字符串表示符合规范。
     */
    @Test
    void testTagStringRepresentation() {
        FieldClassificationResult r = api.classifyField("mobile", "13800138000", null);
        SecurityTag tag = r.getTags().get(0);
        assertEquals("L3_PII_MOBILE", tag.toTagString());
    }
}
