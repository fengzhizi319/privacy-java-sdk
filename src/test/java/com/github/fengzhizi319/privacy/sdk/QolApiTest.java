package com.github.fengzhizi319.privacy.sdk;

import com.github.fengzhizi319.privacy.sdk.api.QolApi;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link QolApi} 的单元测试。
 * <p>
 * 验证查询混淆后列表长度正确、真实查询被包含、策略选择正确、批量混淆等功能。
 * </p>
 */
class QolApiTest {

    /** 使用固定随机种子的 API 实例，保证测试可复现。 */
    private final QolApi api = new QolApi(new Random(42L));

    /**
     * 测试 obfuscateQuery：虚拟查询数量 + 真实查询 = 总数，且真实查询必然存在于结果中。
     */
    @Test
    void testObfuscateQuery() {
        String real = "糖尿病患者用药趋势";
        List<String> result = api.obfuscateQuery(real, 3, "medical", null, null);
        assertEquals(4, result.size());
        assertTrue(result.contains(real));
    }

    /**
     * 测试带元数据的查询混淆。
     */
    @Test
    void testObfuscateQueryWithDetails() {
        String real = "高血压的最新治疗方案";
        QolApi.QoLResult result = api.obfuscateQueryWithDetails(real, 3, "medical", null, null);

        assertEquals(4, result.queries.size());
        assertTrue(result.realQueryIndex >= 0 && result.realQueryIndex < result.queries.size());
        assertEquals(real, result.queries.get(result.realQueryIndex));
        assertEquals("medical", result.domain);
        assertEquals(3, result.numDummies);
    }

    /**
     * 测试语义槽位替换策略：查询包含已知疾病实体时应触发 slot_filling。
     */
    @Test
    void testSlotFillingStrategy() {
        String real = "高血压的最新治疗方案";
        QolApi.QoLResult result = api.obfuscateQueryWithDetails(real, 3, "medical", null, null);

        assertEquals("slot_filling", result.strategy);
        // dummy 不应包含原始实体
        for (int i = 0; i < result.queries.size(); i++) {
            if (i == result.realQueryIndex) continue;
            assertFalse(result.queries.get(i).contains("高血压"),
                    "dummy should not contain original entity");
        }
    }

    /**
     * 测试长度相近抽样策略：查询不包含已知实体时应使用 length_similarity。
     */
    @Test
    void testLengthSimilarityStrategy() {
        String real = "如何办理营业执照";
        QolApi.QoLResult result = api.obfuscateQueryWithDetails(real, 3, "generic", null, null);

        assertEquals("length_similarity", result.strategy);
        assertEquals(4, result.queries.size());
    }

    /**
     * 测试批量查询混淆。
     */
    @Test
    void testObfuscateQueryBatch() {
        List<String> queries = List.of("高血压饮食建议", "天气预报查询", "公积金提取流程");
        List<List<String>> results = api.obfuscateQueryBatch(queries, 2, "generic", null, null);

        assertEquals(3, results.size());
        for (int i = 0; i < results.size(); i++) {
            assertEquals(3, results.get(i).size()); // 2 dummies + 1 real
            assertTrue(results.get(i).contains(queries.get(i)),
                    "group " + i + " should contain real query");
        }
    }

    /**
     * 测试自定义词库覆盖内置词库。
     */
    @Test
    void testCustomPool() {
        List<String> customMedical = List.of("自定义查询A", "自定义查询B", "自定义查询C");
        String real = "测试查询";
        List<String> result = api.obfuscateQuery(real, 2, "medical", customMedical, null);

        assertEquals(3, result.size());
        for (String r : result) {
            if (r.equals(real)) continue;
            assertTrue(customMedical.contains(r),
                    "dummy '" + r + "' should come from custom pool");
        }
    }

    /**
     * 测试通用领域槽位替换。
     */
    @Test
    void testGenericSlotFilling() {
        String real = "社保卡丢失怎么办";
        QolApi.QoLResult result = api.obfuscateQueryWithDetails(real, 3, "generic", null, null);

        assertEquals("slot_filling", result.strategy);
    }
}
