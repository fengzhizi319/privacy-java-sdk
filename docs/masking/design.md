# 数据脱敏（Masking）设计文档

## 1. 概述

脱敏模块通过字段名关键字匹配自动识别敏感类型，并应用格式保留的掩码规则。支持单字段、整记录、批量字段多种调用方式，并提供 HMAC 哈希与截断工具。

## 2. 设计目标

- 基于字段名子串匹配（大小写不敏感）自动推断类型。
- 对手机号、身份证、姓名、银行卡、邮箱、地址提供默认掩码。
- 脱敏后保持数据格式（长度、分隔符等）。
- 纯本地计算，无外部依赖。

## 3. 字段识别规则

`guessFieldType(fieldName)` 通过子串匹配识别类型：

| 字段名关键字 | 识别类型 | 脱敏方法 |
|---|---|---|
| `mobile` / `phone` / `tel` | mobile | maskMobile |
| `id_card` / `idcard` / `identity` | id_card | maskIdCard |
| `email` / `mail` | email | maskEmail |
| `addr` / `address` | address | maskAddress |
| `name` | name | maskName |
| `bank` / `card_no` | bank_card | maskBankCard |
| 其他 | default | maskDefault |

## 4. 脱敏规则

### 4.1 手机号
保留前 3 位与后 4 位，中间替换为 `****`：
```
13812345678 → 138****5678
```

### 4.2 身份证
保留前 6 位与后 4 位，中间替换为 `********`：
```
110101199001011234 → 110101********1234
```

### 4.3 姓名
- 2 字：保留首字 + `*`
- 3+ 字：保留首尾字，中间 `**`
```
张三 → 张*
张三丰 → 张**丰
```

### 4.4 银行卡
保留前 4 后 4，中间以空格分隔 `****`：
```
6222021234567890123 → 6222 **** **** 0123
```

### 4.5 邮箱
保留用户名首尾字符，中间 `***`，域名完整保留：
```
zhangsan@example.com → z***n@example.com
```

### 4.6 地址
保留前 6 个字符，剩余替换为 `****`：
```
北京市朝阳区某某街道123号 → 北京市朝阳区****
```

### 4.7 默认策略
保留前后各 3 位，中间用 `*` 填充。

## 5. 模块结构

```
src/main/java/com/github/fengzhizi319/privacy/sdk/
├── api/
│   └── MaskingApi.java         # 脱敏核心 API
└── PrivacyClient.java          # 入口客户端
```

## 6. 接口设计

### 6.1 MaskingApi 核心方法

```java
public class MaskingApi {
    public String maskValue(String fieldName, String value, String context);
    public Map<String, Object> maskRecord(Map<String, Object> record, String context);
    public List<String> maskBatch(List<String> fieldNames, List<String> values, String context);
    public String hashValue(String value, String salt);
    public String truncate(String value, int keepPrefix);
}
```

### 6.2 线程安全

- MaskingApi 无可变状态，所有方法为纯函数
- 可在多线程环境中安全并发调用

## 7. HMAC 哈希

使用 HMAC-SHA256 算法，提供确定性哈希：

```java
Mac mac = Mac.getInstance("HmacSHA256");
mac.init(new SecretKeySpec(salt.getBytes(), "HmacSHA256"));
byte[] hash = mac.doFinal(value.getBytes());
return Hex.encodeHexString(hash).substring(0, 16);
```

## 8. 与其他模块的关系

| 模块 | 关系 |
|---|---|
| 分类 (classification) | 可先分类再决定是否脱敏 |
| K-匿名 (kano) | 互补：脱敏保护单字段，K-匿名保护整表 |
| DP | 互补：脱敏保护原始值，DP 保护统计结果 |
