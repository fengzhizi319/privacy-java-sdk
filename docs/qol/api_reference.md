# 查询混淆（QoL）API 参考

## 1. 概述

QoL API 通过 `PrivacyClient.qol()` 获取 `QolApi` 实例，或通过便捷方法直接调用。

## 2. 核心 API

### 2.1 obfuscateQuery

```java
public List<String> obfuscateQuery(String query, int numDummies, String domain,
    List<String> medicalPool, List<String> genericPool)
```

| 参数 | 类型 | 说明 |
|---|---|---|
| query | String | 真实查询 |
| numDummies | int | dummy 数量 |
| domain | String | `"medical"` 或 `"generic"` |
| medicalPool | List\<String\> | 自定义医疗池（可为 null） |
| genericPool | List\<String\> | 自定义通用池（可为 null） |

**返回值：** 包含真实查询与 dummy 的混合列表。

---

### 2.2 obfuscateQueryBatch

```java
public List<List<String>> obfuscateQueryBatch(List<String> queries, int numDummies,
    String domain, List<String> medicalPool, List<String> genericPool)
```

---

### 2.3 obfuscateQueryWithDetails

```java
public QoLResult obfuscateQueryWithDetails(String query, int numDummies, String domain,
    List<String> medicalPool, List<String> genericPool)
```

**QoLResult 结构：**

```java
public static class QoLResult {
    public List<String> queries;    // 混淆后查询列表
    public String domain;           // 使用的领域
    public int numDummies;          // dummy 数量
    public String strategy;         // 混淆策略
}
```

## 3. 便捷方法

| 方法 | 等价调用 |
|---|---|
| `client.obfuscateQuery(...)` | `client.qol().obfuscateQuery(...)` |
| `client.obfuscateQueryBatch(...)` | `client.qol().obfuscateQueryBatch(...)` |
| `client.obfuscateQueryWithDetails(...)` | `client.qol().obfuscateQueryWithDetails(...)` |

## 4. 线程安全

所有 QolApi 方法为线程安全。
