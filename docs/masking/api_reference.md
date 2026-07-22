# 数据脱敏（Masking）API 参考

## 1. 概述

脱敏 API 通过 `PrivacyClient.masking()` 获取 `MaskingApi` 实例，或通过 `PrivacyClient` 上的便捷方法直接调用。

## 2. 核心 API

### 2.1 maskValue

对单个字段值进行脱敏。

```java
public String maskValue(String fieldName, String value, String context)
```

| 参数 | 类型 | 说明 |
|---|---|---|
| fieldName | String | 字段名（用于推断类型） |
| value | String | 原始值 |
| context | String | 上下文标识（用于日志） |

**返回值：** 脱敏后的字符串。

---

### 2.2 maskRecord

对整条记录的所有字符串字段进行脱敏。

```java
public Map<String, Object> maskRecord(Map<String, Object> record, String context)
```

| 参数 | 类型 | 说明 |
|---|---|---|
| record | Map\<String, Object\> | 原始记录 |
| context | String | 上下文标识 |

**返回值：** 新 Map，原记录不被修改。

---

### 2.3 maskBatch

批量对字段值进行脱敏。

```java
public List<String> maskBatch(List<String> fieldNames, List<String> values, String context)
```

| 参数 | 类型 | 说明 |
|---|---|---|
| fieldNames | List\<String\> | 字段名列表 |
| values | List\<String\> | 对应值列表 |
| context | String | 上下文标识 |

**返回值：** 脱敏后的值列表。

**异常：** 两个列表长度不一致时抛出 `IllegalArgumentException`。

---

### 2.4 hashValue

基于 HMAC-SHA256 的确定性哈希。

```java
public String hashValue(String value, String salt)
```

| 参数 | 类型 | 说明 |
|---|---|---|
| value | String | 原始值 |
| salt | String | HMAC 盐值 |

**返回值：** 16 位十六进制哈希字符串。

---

### 2.5 truncate

保留前 N 个字符，剩余截断。

```java
public String truncate(String value, int keepPrefix)
```

| 参数 | 类型 | 说明 |
|---|---|---|
| value | String | 原始值 |
| keepPrefix | int | 保留前缀长度 |

## 3. 便捷方法

| 方法 | 等价调用 |
|---|---|
| `client.maskValue(field, value, ctx)` | `client.masking().maskValue(field, value, ctx)` |
| `client.maskRecord(record, ctx)` | `client.masking().maskRecord(record, ctx)` |
| `client.maskBatch(fields, values, ctx)` | `client.masking().maskBatch(fields, values, ctx)` |
| `client.hashValue(value, salt)` | `client.masking().hashValue(value, salt)` |
| `client.truncate(value, keep)` | `client.masking().truncate(value, keep)` |

## 4. 字段类型推断规则

| 字段名包含 | 推断类型 | 脱敏效果 |
|---|---|---|
| mobile/phone/tel | 手机号 | `138****5678` |
| id_card/idcard/identity | 身份证 | `110101********1234` |
| email/mail | 邮箱 | `z***n@example.com` |
| addr/address | 地址 | `北京市朝阳区****` |
| name | 姓名 | `张*` |
| bank/card_no | 银行卡 | `6222 **** **** 0123` |
| 其他 | 默认 | `abc***xyz` |

## 5. 线程安全

所有 MaskingApi 方法为线程安全，可在多线程环境中并发调用。
