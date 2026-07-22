# 查询混淆测试策略

## 1. 测试概览

| 测试类型 | 覆盖范围 | 文件 |
|---|---|---|
| 单元测试 | 混淆逻辑、池选择 | `QolApiTest.java` |
| 边界测试 | 空输入、极端参数 | `QolApiTest.java` |

## 2. 核心测试

```java
@Test
void testObfuscateQueryContainsReal() {
    PrivacyClient client = new PrivacyClient();
    List<String> result = client.obfuscateQuery("高血压用药", 3, "medical", null, null);
    assertTrue(result.contains("高血压用药"));
    assertEquals(4, result.size());
}

@Test
void testObfuscateQueryBatch() {
    PrivacyClient client = new PrivacyClient();
    List<String> queries = Arrays.asList("查询A", "查询B");
    List<List<String>> results = client.obfuscateQueryBatch(queries, 2, "generic", null, null);
    assertEquals(2, results.size());
    assertTrue(results.get(0).contains("查询A"));
    assertTrue(results.get(1).contains("查询B"));
}

@Test
void testCustomPoolPriority() {
    PrivacyClient client = new PrivacyClient();
    List<String> custom = Arrays.asList("自定义A", "自定义B", "自定义C");
    List<String> result = client.obfuscateQuery("真实", 2, "generic", null, custom);
    assertEquals(3, result.size());
}

@Test
void testZeroDummies() {
    PrivacyClient client = new PrivacyClient();
    List<String> result = client.obfuscateQuery("仅真实", 0, "generic", null, null);
    assertEquals(1, result.size());
    assertEquals("仅真实", result.get(0));
}

@Test
void testWithDetails() {
    PrivacyClient client = new PrivacyClient();
    QolApi.QoLResult result = client.obfuscateQueryWithDetails("测试", 3, "medical", null, null);
    assertEquals("medical", result.domain);
    assertEquals(3, result.numDummies);
    assertEquals(4, result.queries.size());
}
```

## 3. 运行测试

```bash
mvn test -Dtest=QolApiTest
```

## 4. 测试检查清单

- [ ] 结果包含真实查询
- [ ] 结果长度 = 1 + numDummies
- [ ] 自定义池优先
- [ ] 批量混淆各条独立
- [ ] numDummies=0 仅返回真实查询
- [ ] 带详情返回正确元数据
