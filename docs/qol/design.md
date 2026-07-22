# 查询混淆（QoL）设计文档

## 1. 概述

查询混淆模块通过在真实查询中混入虚假查询保护用户查询隐私。支持医疗/通用领域内置池与自定义池。

## 2. 算法流程

```
输入: query, numDummies, domain, customPools
  ├─ 1. 确定 dummy 池（自定义 > 内置）
  ├─ 2. 从池中抽样 numDummies 条
  ├─ 3. 语义槽位替换（可选）
  ├─ 4. 合并真实查询与 dummy
  └─ 5. 随机打乱顺序
```

## 3. 模块结构

```
src/main/java/com/github/fengzhizi319/privacy/sdk/
├── api/
│   └── QolApi.java           # 查询混淆核心 API
└── PrivacyClient.java        # 入口客户端
```

## 4. 接口设计

```java
public class QolApi {
    public List<String> obfuscateQuery(String query, int numDummies, String domain,
        List<String> medicalPool, List<String> genericPool);

    public List<List<String>> obfuscateQueryBatch(List<String> queries, int numDummies,
        String domain, List<String> medicalPool, List<String> genericPool);

    public QoLResult obfuscateQueryWithDetails(String query, int numDummies, String domain,
        List<String> medicalPool, List<String> genericPool);

    public static class QoLResult {
        public List<String> queries;
        public String domain;
        public int numDummies;
        public String strategy;
    }
}
```

## 5. 线程安全

- QolApi 无可变状态，所有方法为纯函数
- 随机数使用 ThreadLocalRandom，线程安全
