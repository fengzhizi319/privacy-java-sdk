# K-匿名测试策略

## 1. 测试概览

| 测试类型 | 覆盖范围 | 文件 |
|---|---|---|
| 单元测试 | 泛化层次、单记录泛化 | `KAnonymityApiTest.java` |
| 集成测试 | Mondrian 表格泛化 | `KAnonymityApiTest.java` |
| 边界测试 | 异常输入 | `KAnonymityApiTest.java` |

## 2. 核心测试

```java
@Test
void testAnonymizeRecord() {
    PrivacyClient client = new PrivacyClient();
    Map<String, Object> record = Map.of("age", "28", "zipcode", "518057", "disease", "A");
    var hierarchies = Map.of("age", KAnonymityApi.ageHierarchy(), "zipcode", KAnonymityApi.zipcodeHierarchy());

    Map<String, Object> result = client.kAnonymizeRecord(record, List.of("age", "zipcode"), hierarchies, 5);

    assertNotEquals("28", result.get("age"));
    assertEquals("A", result.get("disease")); // 非 QI 不变
}

@Test
void testKAnonymizeTableEquivalenceClass() {
    PrivacyClient client = new PrivacyClient();
    List<Map<String, Object>> rows = List.of(
        Map.of("age", 25, "zipcode", "100001"),
        Map.of("age", 26, "zipcode", "100002"),
        Map.of("age", 55, "zipcode", "200001"),
        Map.of("age", 56, "zipcode", "200002")
    );

    List<Map<String, Object>> result = client.kAnonymizeTable(rows, List.of("age", "zipcode"), 2, 10);
    assertEquals(4, result.size());
}

@Test
void testInsufficientRows() {
    PrivacyClient client = new PrivacyClient();
    List<Map<String, Object>> rows = List.of(Map.of("age", 25));
    assertThrows(IllegalArgumentException.class, () ->
        client.kAnonymizeTable(rows, List.of("age"), 5, 10));
}
```

## 3. 运行测试

```bash
mvn test -Dtest=KAnonymityApiTest
```

## 4. 测试检查清单

- [ ] 内置层次各级泛化正确
- [ ] 单记录非 QI 字段不变
- [ ] Mondrian 等价组 ≥ k
- [ ] 行数不足 k 报错
- [ ] 空 qiCols 报错
