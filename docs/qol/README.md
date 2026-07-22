# 查询混淆（QoL）模块

通过向真实查询中混入虚假查询（dummy queries）降低查询意图泄露风险。支持医疗与通用领域内置 dummy 池、自定义池、语义槽位替换。

## 文档索引

| 文档 | 说明 |
|---|---|
| [产品需求](prd.md) | 功能需求与验收标准 |
| [设计文档](design.md) | 算法原理与架构设计 |
| [API 参考](api_reference.md) | 接口签名与参数说明 |
| [使用示例](examples.md) | 典型场景代码示例 |
| [测试策略](testing.md) | 测试方法与检查清单 |
| [运维手册](ops.md) | 池管理与安全注意事项 |

## 快速开始

```java
import com.github.fengzhizi319.privacy.sdk.PrivacyClient;

PrivacyClient client = new PrivacyClient();

// 单条混淆
List<String> result = client.obfuscateQuery("高血压用药", 3, "medical", null, null);

// 批量混淆
List<List<String>> batch = client.obfuscateQueryBatch(queries, 3, "medical", null, null);

// 带详情
QolApi.QoLResult detail = client.obfuscateQueryWithDetails("查询", 5, "medical", null, null);
```

## API 概览

| 方法 | 说明 |
|---|---|
| `qol().obfuscateQuery(...)` | 单条查询混淆 |
| `qol().obfuscateQueryBatch(...)` | 批量查询混淆 |
| `qol().obfuscateQueryWithDetails(...)` | 带元数据的混淆 |
