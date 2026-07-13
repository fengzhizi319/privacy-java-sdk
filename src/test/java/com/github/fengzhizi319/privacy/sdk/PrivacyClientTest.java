package com.github.fengzhizi319.privacy.sdk;

import com.github.fengzhizi319.privacy.sdk.classification.ClassificationClient;
import com.github.fengzhizi319.privacy.sdk.model.classification.FieldClassificationResult;
import com.github.fengzhizi319.privacy.sdk.model.classification.SensitivityLevel;
import org.junit.jupiter.api.Test;

import java.sql.ResultSet;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PrivacyClientTest {

    private final PrivacyClient client = new PrivacyClient();

    @Test
    void testMaskingWrappers() {
        assertEquals("138****5678", client.maskValue("mobile", "13812345678", ""));
        assertEquals("深圳市南山区***", client.truncate("深圳市南山区科技南十二路18号", 6));
        
        Map<String, Object> record = Map.of(
            "mobile", "13812345678",
            "age", 25
        );
        Map<String, Object> masked = client.maskRecord(record, "");
        assertEquals("138****5678", masked.get("mobile"));
        assertEquals(25, masked.get("age"));
    }

    @Test
    void testDpWrappers() {
        assertTrue(client.dpCount(100L, 1.0, 0.0, "laplace") >= 0);
        assertTrue(client.dpSum(List.of(1.0, 2.0, 3.0), 1.0, 0.0, "laplace", null, null) > 0);
        assertTrue(client.dpMean(List.of(10.0, 20.0, 30.0), 1.0, 0.0, "laplace", null, null) > 0);
    }

    @Test
    void testKAnonymityAndQol() {
        Map<String, Object> record = Map.of("age", "25");
        Map<String, Object> generalized = client.kAnonymizeRecord(
            record,
            List.of("age"),
            Map.of("age", PrivacyClient.ageHierarchy()),
            5
        );
        assertEquals("[25-30]", generalized.get("age"));

        // Test Mondrian Table Anonymization
        List<Map<String, Object>> rows = List.of(
            Map.of("age", 25.0, "zipcode", "100001", "gender", "M", "disease", "A"),
            Map.of("age", 26.0, "zipcode", "100002", "gender", "M", "disease", "B"),
            Map.of("age", 27.0, "zipcode", "100003", "gender", "M", "disease", "C"),
            Map.of("age", 55.0, "zipcode", "200001", "gender", "F", "disease", "D"),
            Map.of("age", 56.0, "zipcode", "200002", "gender", "F", "disease", "E"),
            Map.of("age", 57.0, "zipcode", "200003", "gender", "F", "disease", "F")
        );
        List<Map<String, Object>> resTable = client.kAnonymizeTable(rows, List.of("age", "zipcode", "gender"), 3, 10);
        assertEquals(6, resTable.size());

        List<String> obfuscated = client.obfuscateQuery("test", 3, "medical", null, null);
        assertEquals(4, obfuscated.size());
    }

    @Test
    void testClassificationWrappers() {
        FieldClassificationResult r = client.classifyField("mobile", "13800138000", null);
        assertEquals(SensitivityLevel.L3, r.getFinalLevel());

        ClassificationClient cClient = new ClassificationClient();
        FieldClassificationResult r2 = cClient.classifyField("mobile", "13800138000", null);
        assertEquals(SensitivityLevel.L3, r2.getFinalLevel());
    }

    @Test
    void testClassifyResultSet() throws Exception {
        ResultSet rs = (ResultSet) java.lang.reflect.Proxy.newProxyInstance(
            ResultSet.class.getClassLoader(),
            new Class<?>[]{ResultSet.class},
            new java.lang.reflect.InvocationHandler() {
                private int count = 0;
                @Override
                public Object invoke(Object proxy, java.lang.reflect.Method method, Object[] args) throws Throwable {
                    if ("next".equals(method.getName())) {
                        count++;
                        return count <= 1;
                    }
                    if ("getMetaData".equals(method.getName())) {
                        return java.lang.reflect.Proxy.newProxyInstance(
                            java.sql.ResultSetMetaData.class.getClassLoader(),
                            new Class<?>[]{java.sql.ResultSetMetaData.class},
                            (p, m, a) -> {
                                if ("getColumnCount".equals(m.getName())) {
                                    return 1;
                                }
                                if ("getColumnLabel".equals(m.getName())) {
                                    return "mobile";
                                }
                                return null;
                            }
                        );
                    }
                    if ("getObject".equals(method.getName())) {
                        return "13800138000";
                    }
                    return null;
                }
            }
        );

        var tableResult = client.classifyResultSet(rs, null);
        assertEquals(SensitivityLevel.L3, tableResult.getFinalLevel());
        assertEquals("mobile", tableResult.getSchema().get(0));
    }

    @Test
    void testRecommendAndSaveParams() throws Exception {
        java.nio.file.Path yamlPath = java.nio.file.Path.of("personalized-profiles.yaml");
        java.nio.file.Files.deleteIfExists(yamlPath);
        try {
            List<Double> values = List.of(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0);
            List<Map<String, Object>> rows = List.of(Map.of("age", 20));
            Map<String, Object> result = client.recommendAndSaveParams(values, rows, null);
            assertNotNull(result.get("dp"));
            assertNotNull(result.get("k_anonymity"));
            assertTrue(java.nio.file.Files.exists(yamlPath));
        } finally {
            java.nio.file.Files.deleteIfExists(yamlPath);
        }
    }
}
