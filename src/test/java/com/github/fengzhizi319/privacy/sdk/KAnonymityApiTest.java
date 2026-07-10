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
}
