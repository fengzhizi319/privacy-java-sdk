# K-匿名设计文档

## 1. 概述

K-匿名模块提供单记录泛化与 Mondrian 表格泛化两种能力，通过准标识符泛化确保数据集中每条记录至少与 k-1 条其他记录不可区分。

## 2. 算法原理

### 2.1 单记录泛化

根据 k 值选择泛化层次级别：`level = min(k / 5, maxLevel)`

```java
public interface GeneralizationHierarchy {
    String generalize(String value, int level);
    int maxLevel();
}
```

### 2.2 Mondrian 多维分区

1. 选择跨度最大的 QI 维度
2. 按中位数分割为两组
3. 递归直到每组 ≥ k 条或达到 maxDepth
4. 数值型输出区间 `[min-max]`，分类型输出集合 `{a,b,c}`

### 2.3 内置泛化层次

| 层次 | Level 0 | Level 1 | Level 2 |
|---|---|---|---|
| Age | 28 | 20-30 | * |
| Zipcode | 518057 | 518*** | * |
| Gender | 男 | * | — |

## 3. 模块结构

```
src/main/java/com/github/fengzhizi319/privacy/sdk/
├── api/
│   └── KAnonymityApi.java    # K-匿名核心 API
└── PrivacyClient.java        # 入口客户端
```

## 4. 接口设计

```java
public class KAnonymityApi {
    public Map<String, Object> anonymizeRecord(
        Map<String, Object> record, List<String> qiCols,
        Map<String, GeneralizationHierarchy> hierarchies, int k);

    public List<Map<String, Object>> kAnonymizeTable(
        List<Map<String, Object>> rows, List<String> qiCols, int k, int maxDepth);
}
```

## 5. 线程安全

- KAnonymityApi 无可变状态
- 所有方法为纯函数，线程安全
