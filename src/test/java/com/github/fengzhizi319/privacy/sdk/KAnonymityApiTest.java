package com.github.fengzhizi319.privacy.sdk;

import com.github.fengzhizi319.privacy.sdk.api.KAnonymityApi;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link KAnonymityApi} 的单元测试。
 * <p>
 * 验证单条记录在不同泛化层次结构下的泛化结果。
 * </p>
 */
class KAnonymityApiTest {

    /** 被测 API 实例。 */
    private final KAnonymityApi api = new KAnonymityApi();

    /**
     * 测试对 age、zipcode、gender 三个准标识符进行泛化，并保留非准标识符列 disease。
     */
    @Test
    void testAnonymizeRecord() {
        Map<String, KAnonymityApi.GeneralizationHierarchy> hierarchies = Map.of(
            "age", KAnonymityApi.ageHierarchy(),
            "zipcode", KAnonymityApi.zipcodeHierarchy(),
            "gender", KAnonymityApi.genderHierarchy()
        );

        Map<String, Object> record = Map.of(
            "age", "28",
            "zipcode", "518057",
            "gender", "女",
            "disease", "胃癌"
        );

        Map<String, Object> result = api.anonymizeRecord(
            record, java.util.List.of("age", "zipcode", "gender"), hierarchies, 5
        );

        assertTrue(result.get("age").toString().startsWith("["));
        assertEquals("518***", result.get("zipcode"));
        assertEquals("*", result.get("gender"));
        assertEquals("胃癌", result.get("disease"));
    }

    @Test
    void testKAnonymizeTable() {
        java.util.List<Map<String, Object>> rows = java.util.List.of(
            Map.of("age", 25.0, "zipcode", "100001", "gender", "M", "disease", "A"),
            Map.of("age", 26.0, "zipcode", "100002", "gender", "M", "disease", "B"),
            Map.of("age", 27.0, "zipcode", "100003", "gender", "M", "disease", "C"),
            Map.of("age", 55.0, "zipcode", "200001", "gender", "F", "disease", "D"),
            Map.of("age", 56.0, "zipcode", "200002", "gender", "F", "disease", "E"),
            Map.of("age", 57.0, "zipcode", "200003", "gender", "F", "disease", "F")
        );
        java.util.List<Map<String, Object>> result = api.kAnonymizeTable(
            rows, java.util.List.of("age", "zipcode", "gender"), 3, 10
        );
        assertEquals(6, result.size());
    }
}
