# K-匿名模块

对单条记录或整张表执行准标识符泛化，使得发布后的数据中每条记录至少与 k-1 条其他记录不可区分。支持内置年龄/邮编/性别泛化层次与 Mondrian 多维分区表格泛化。

## 文档索引

| 文档 | 说明 |
|---|---|
| [产品需求](prd.md) | 功能需求与验收标准 |
| [设计文档](design.md) | 算法原理与架构设计 |
| [API 参考](api_reference.md) | 接口签名与参数说明 |
| [使用示例](examples.md) | 典型场景代码示例 |
| [测试策略](testing.md) | 测试方法与检查清单 |
| [运维手册](ops.md) | 性能调优与故障排查 |

## 快速开始

```java
import com.github.fengzhizi319.privacy.sdk.PrivacyClient;

PrivacyClient client = new PrivacyClient();

// 单记录泛化
Map<String, Object> result = client.kAnonymizeRecord(record, qiCols, hierarchies, 5);

// 表格泛化
List<Map<String, Object>> anonymized = client.kAnonymizeTable(rows, qiCols, 2, 10);
```

## API 概览

| 方法 | 说明 |
|---|---|
| `kAnonymity().anonymizeRecord(...)` | 单记录泛化 |
| `kAnonymity().kAnonymizeTable(...)` | Mondrian 表格泛化 |
