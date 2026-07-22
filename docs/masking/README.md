# 数据脱敏（Masking）模块

基于字段名关键字自动识别敏感类型，对手机号、身份证、姓名、银行卡、邮箱、地址等常见 PII 提供格式保留的掩码规则。支持单字段、整记录、批量字段多种调用方式，并提供 HMAC 哈希与截断工具。

## 文档索引

| 文档 | 说明 |
|---|---|
| [产品需求](prd.md) | 功能需求与验收标准 |
| [设计文档](design.md) | 字段识别规则与脱敏算法 |
| [API 参考](api_reference.md) | 接口签名与参数说明 |
| [使用示例](examples.md) | 典型场景代码示例 |
| [测试策略](testing.md) | 测试方法与检查清单 |
| [运维手册](ops.md) | 规则维护与盐值管理 |

## 快速开始

```java
import com.github.fengzhizi319.privacy.sdk.PrivacyClient;

PrivacyClient client = new PrivacyClient();

// 单字段脱敏
String masked = client.maskValue("mobile", "13812345678", "api");
// 输出: 138****5678

// 整记录脱敏
Map<String, Object> record = Map.of("name", "张三", "phone", "13800001111");
Map<String, Object> maskedRecord = client.maskRecord(record, "response");

// HMAC 哈希
String hash = client.hashValue("13812345678", "my-salt");
```

## API 概览

| 方法 | 说明 |
|---|---|
| `masking().maskValue(field, value, ctx)` | 单字段脱敏 |
| `masking().maskRecord(record, ctx)` | 整记录脱敏 |
| `masking().maskBatch(fields, values, ctx)` | 批量脱敏 |
| `masking().hashValue(value, salt)` | HMAC 哈希 |
| `masking().truncate(value, keep)` | 字符串截断 |

## 支持的字段类型

| 字段名关键字 | 脱敏效果 |
|---|---|
| mobile/phone/tel | `138****5678` |
| id_card/idcard | `110101********1234` |
| email/mail | `z***n@example.com` |
| addr/address | `北京市朝阳区****` |
| name | `张*` |
| bank/card_no | `6222 **** **** 0123` |
