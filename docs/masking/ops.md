# 数据脱敏运维手册

## 1. 概述

脱敏模块为纯本地计算，无外部依赖。运维重点在于字段识别规则维护与 HMAC 盐值管理。

## 2. 性能特征

| 操作 | 时间复杂度 | 说明 |
|---|---|---|
| 单字段脱敏 | O(1) | 字符串操作 |
| 整记录脱敏 | O(F) | F=字段数 |
| 批量脱敏 | O(N) | N=字段数 |
| HMAC 哈希 | O(1) | 固定长度输出 |

## 3. 字段识别维护

### 3.1 新增字段类型

当业务出现新的敏感字段时，需在 `MaskingApi` 中添加识别规则：

1. 在 `guessFieldType` 中添加关键字匹配
2. 实现对应的 `maskXxx` 方法
3. 添加单元测试

### 3.2 字段名规范

确保数据库/API 字段命名包含可识别关键字：

| 推荐命名 | 识别为 |
|---|---|
| `user_mobile`, `phone_number` | 手机号 |
| `id_card_no`, `identity_number` | 身份证 |
| `user_email`, `mail_address` | 邮箱 |
| `home_address`, `addr_detail` | 地址 |
| `user_name`, `real_name` | 姓名 |
| `bank_card_no`, `card_number` | 银行卡 |

## 4. HMAC 盐值管理

### 4.1 盐值配置

```java
// 从配置中心或环境变量获取盐值
String salt = System.getenv("PRIVACY_HMAC_SALT");
if (salt == null || salt.isEmpty()) {
    throw new IllegalStateException("HMAC salt not configured");
}
String hash = client.hashValue(sensitiveValue, salt);
```

### 4.2 安全建议

- 盐值长度 ≥ 32 字符
- 不要硬编码在代码中
- 生产环境使用 KMS 管理
- 盐值泄露后需重新哈希所有数据

## 5. 故障排查

| 问题 | 原因 | 解决方案 |
|---|---|---|
| 字段未被脱敏 | 字段名不含识别关键字 | 检查命名或添加规则 |
| 脱敏结果异常 | 值长度短于保留位数 | 检查数据质量 |
| HMAC 结果不一致 | 盐值不同 | 统一盐值配置 |
| 批量脱敏报错 | 列表长度不一致 | 检查调用参数 |

## 6. 最佳实践

1. **统一字段命名规范**：确保敏感字段包含可识别关键字
2. **API 网关层脱敏**：在响应返回前统一脱敏
3. **日志脱敏前置**：在日志框架中集成脱敏
4. **盐值安全管理**：使用 KMS 或配置中心
5. **定期审查规则**：随业务迭代更新识别规则
