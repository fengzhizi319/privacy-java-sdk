# 数据分类分级（Classification）

## 概述

通过三层漏斗式分类引擎自动识别敏感数据并输出 L1~L5 敏感度等级与标签。支持字段级、记录级、表级分类，以及 JSON、DataFrame、SQL 结果集等多种输入格式。

## 分类架构

```text
Layer 1 RULE        → 规则匹配，置信度 1.0
   ↓ (level <= L3 或未命中且启用 Small-NER)
Layer 2 SMALL_NER   → 医学实体识别，可选升级 / 人工复核
   ↓ (启用 LLM 或置信度 < 0.6)
Layer 3 LLM         → 零样本语义分类，结构化输出
   ↓
Composite Rules     → 跨字段组合后处理，升级敏感度
   ↓
manual_override     → 字段级最终等级覆盖
   ↓
Review Store        → 收集 needsHumanReview 样本
```

## API 参考

```java
import com.github.fengzhizi319.privacy.sdk.classification.ClassificationClient;
import com.github.fengzhizi319.privacy.sdk.model.classification.*;

ClassificationClient client = new ClassificationClient();

// 字段级
FieldClassificationResult f = client.classifyField(
    "id_card", "110101199001011237", null
);

// 记录级
RecordClassificationResult r = client.classifyRecord(
    Map.of("id_card", "110101199001011237", "mobile", "13800138000"),
    null
);

// 表级
TableClassificationResult t = client.classifyTable(
    List.of("id_card", "diagnosis"),
    List.of(Map.of("id_card", "...", "diagnosis", "B21.1")),
    null
);

// JSON / DataFrame / SQL
ClassificationResult json = client.classifyJson("{...}", null);
TableClassificationResult df = client.classifyDataFrame(rows, null);
TableClassificationResult sql = client.classifySqlResult(rows, null);

// 异步任务
String jobId = client.classifyTableAsync(schema, rows, null);
ClassificationJob job = client.getClassificationJob(jobId);

// 复核
client.confirmReview(reviewId, "L1", "reviewer", "comment");
List<ReviewEntry> reviews = client.exportReviews(true);
```

## 合规模板

支持 `gbt35273`、`gdpr`、`jrt0197` 三种模板，通过 `params` 传入 `template` 激活：

```java
Map<String, Object> params = Map.of("template", "gbt35273");
TableClassificationResult t = client.classifyTable(schema, rows, params);
```

## 复合规则

当记录中同时命中指定字段模式数达到 `minMatches` 时，升级敏感度：

```java
Map<String, Object> params = Map.of("composite_rules", List.of(Map.of(
    "fieldPatterns", List.of("name", "id_card", "mobile"),
    "minMatches", 3,
    "targetLevel", "L5",
    "category", "COMPOSITE_PII_COMBO",
    "ruleId", "COMP_001"
)));
```

## 参数说明

| 参数 | 类型 | 说明 |
|---|---|---|
| default_level | String | L1~L5 默认兜底等级 |
| enable_rule_engine | boolean | 是否启用规则引擎 |
| enable_small_ner | boolean | 是否启用 Small-NER |
| enable_llm | boolean | 是否启用 LLM |
| template | String | 合规模板名 |
| composite_rules | List | 复合规则列表 |
| shadow_mode | boolean | 是否同时计算影子规则结果 |
| enable_review | boolean | 是否收集复核样本 |

## 测试覆盖

- 20 个标准分类用例（身份证、手机号、ICD-10、基因组等）
- 合规模板（gbt35273/gdpr/jrt0197）
- 手动覆盖、影子模式、复合规则
- 异步任务与复核导出
