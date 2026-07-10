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
        assertTrue(client.dpCount(100L, 1.0, "laplace") >= 0);
        assertTrue(client.dpSum(List.of(1.0, 2.0, 3.0), 1.0, "laplace") > 0);
        assertTrue(client.dpMean(List.of(10.0, 20.0, 30.0), 1.0, "laplace") > 0);
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

        List<String> obfuscated = client.obfuscateQuery("test", 3, "medical");
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
}
