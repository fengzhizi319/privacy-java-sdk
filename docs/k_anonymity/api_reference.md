# K-匿名 API 参考

## 1. 概述

K-匿名 API 通过 `PrivacyClient.kAnonymity()` 获取 `KAnonymityApi` 实例，或通过便捷方法直接调用。

## 2. 核心 API

### 2.1 anonymizeRecord

对单条记录按泛化层次进行准标识符泛化。

```java
public Map<String, Object> anonymizeRecord(
    Map<String, Object> record,
    List<String> qiCols,
    Map<String, KAnonymityApi.GeneralizationHierarchy> hierarchies,
    int k)
```

| 参数 | 类型 | 说明 |
|---|---|---|
| record | Map\<String, Object\> | 原始记录 |
| qiCols | List\<String\> | 准标识符列名 |
| hierarchies | Map\<String, GeneralizationHierarchy\> | 各列的泛化层次 |
| k | int | 匿名阈值 |

---

### 2.2 kAnonymizeTable

对整张表使用 Mondrian 算法进行 K-匿名泛化。

```java
public List<Map<String, Object>> kAnonymizeTable(
    List<Map<String, Object>> rows,
    List<String> qiCols,
    int k,
    int maxDepth)
```

| 参数 | 类型 | 说明 |
|---|---|---|
| rows | List\<Map\<String, Object\>\> | 数据行 |
| qiCols | List\<String\> | 准标识符列名 |
| k | int | 匿名阈值（≥ 2） |
| maxDepth | int | 最大递归深度 |

## 3. 便捷方法

| 方法 | 等价调用 |
|---|---|
| `client.kAnonymizeRecord(record, qiCols, hierarchies, k)` | `client.kAnonymity().anonymizeRecord(...)` |
| `client.kAnonymizeTable(rows, qiCols, k, maxDepth)` | `client.kAnonymity().kAnonymizeTable(...)` |

## 4. GeneralizationHierarchy 接口

```java
public interface GeneralizationHierarchy {
    String generalize(String value, int level);
    int maxLevel();
}
```

## 5. 异常

| 异常 | 触发条件 |
|---|---|
| `IllegalArgumentException` | k < 2、qiCols 为空、行数 < k |
