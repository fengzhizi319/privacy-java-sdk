# 数据脱敏测试策略

## 1. 测试概览

| 测试类型 | 覆盖范围 | 文件 |
|---|---|---|
| 单元测试 | 各字段类型脱敏规则 | `MaskingApiTest.java` |
| 集成测试 | 整记录/批量脱敏 | `MaskingApiTest.java` |
| 边界测试 | 短值、空值、非法输入 | `MaskingApiTest.java` |

## 2. 字段类型脱敏测试

```java
@Test
void testMaskMobile() {
    PrivacyClient client = new PrivacyClient();
    assertEquals("138****5678", client.maskValue("mobile", "13812345678", "test"));
}

@Test
void testMaskIdCard() {
    PrivacyClient client = new PrivacyClient();
    assertEquals("110101********1234", client.maskValue("id_card", "110101199001011234", "test"));
}

@Test
void testMaskName() {
    PrivacyClient client = new PrivacyClient();
    assertEquals("张*", client.maskValue("name", "张三", "test"));
    assertEquals("张**丰", client.maskValue("name", "张三丰", "test"));
}

@Test
void testMaskEmail() {
    PrivacyClient client = new PrivacyClient();
    assertEquals("z***n@example.com", client.maskValue("email", "zhangsan@example.com", "test"));
}

@Test
void testMaskAddress() {
    PrivacyClient client = new PrivacyClient();
    assertEquals("北京市朝阳区****", client.maskValue("address", "北京市朝阳区某某街道123号", "test"));
}

@Test
void testMaskBankCard() {
    PrivacyClient client = new PrivacyClient();
    assertEquals("6222 **** **** 0123", client.maskValue("bank_card", "6222021234567890123", "test"));
}
```

## 3. 整记录脱敏测试

```java
@Test
void testMaskRecordDoesNotModifyOriginal() {
    PrivacyClient client = new PrivacyClient();
    Map<String, Object> record = new HashMap<>();
    record.put("phone", "13812345678");
    record.put("name", "张三");

    Map<String, Object> masked = client.maskRecord(record, "test");

    // 原记录不变
    assertEquals("13812345678", record.get("phone"));
    // 新记录已脱敏
    assertEquals("138****5678", masked.get("phone"));
    assertEquals("张*", masked.get("name"));
}
```

## 4. 批量脱敏测试

```java
@Test
void testMaskBatch() {
    PrivacyClient client = new PrivacyClient();
    List<String> fields = Arrays.asList("mobile", "email");
    List<String> values = Arrays.asList("13800001111", "a@b.com");

    List<String> result = client.maskBatch(fields, values, "test");
    assertEquals(2, result.size());
    assertEquals("138****1111", result.get(0));
}

@Test
void testMaskBatchLengthMismatch() {
    PrivacyClient client = new PrivacyClient();
    assertThrows(IllegalArgumentException.class, () ->
        client.maskBatch(Arrays.asList("a", "b"), Arrays.asList("1"), "test"));
}
```

## 5. HMAC 与截断测试

```java
@Test
void testHashValueDeterministic() {
    PrivacyClient client = new PrivacyClient();
    String h1 = client.hashValue("test", "salt");
    String h2 = client.hashValue("test", "salt");
    assertEquals(h1, h2);
}

@Test
void testHashValueDifferentSalt() {
    PrivacyClient client = new PrivacyClient();
    String h1 = client.hashValue("test", "salt1");
    String h2 = client.hashValue("test", "salt2");
    assertNotEquals(h1, h2);
}

@Test
void testTruncate() {
    PrivacyClient client = new PrivacyClient();
    assertEquals("110101", client.truncate("110101199001011234", 6));
    assertEquals("短", client.truncate("短文本", 1));
}
```

## 6. 运行测试

```bash
mvn test -Dtest=MaskingApiTest
```

## 7. 测试检查清单

- [ ] 各字段类型脱敏规则正确
- [ ] 整记录脱敏不修改原记录
- [ ] 批量脱敏长度校验
- [ ] HMAC 确定性（同输入同盐同输出）
- [ ] 截断长度正确
- [ ] 短值不崩溃
- [ ] 空字符串处理
