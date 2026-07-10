# Data Classification 运维与集成手册

## 1. 快速开始

### Maven 依赖

已在 `privacy-java-sdk` 中提供，无需额外依赖：

```xml
<dependency>
  <groupId>com.github.fengzhizi319</groupId>
  <artifactId>privacy-java-sdk</artifactId>
  <version>0.1.0-SNAPSHOT</version>
</dependency>
```

### 通过 ClassificationClient 调用

```java
import com.github.fengzhizi319.privacy.sdk.classification.ClassificationClient;
import com.github.fengzhizi319.privacy.sdk.model.classification.FieldClassificationResult;
import com.github.fengzhizi319.privacy.sdk.model.classification.RecordClassificationResult;
import com.github.fengzhizi319.privacy.sdk.model.classification.TableClassificationResult;
import com.github.fengzhizi319.privacy.sdk.model.classification.ClassificationResult;

ClassificationClient client = new ClassificationClient();

// 单字段
FieldClassificationResult field = client.classification()
    .classifyField("id_card", "110101199001011237", null);

// 单条记录
Map<String, Object> record = Map.of(
    "id_card", "110101199001011237",
    "diagnosis", "B21.1"
);
RecordClassificationResult recordResult = client.classification()
    .classifyRecord(record, null);

// 表
List<String> schema = List.of("id_card", "diagnosis");
List<Map<String, Object>> rows = List.of(record);
TableClassificationResult tableResult = client.classification()
    .classifyTable(schema, rows, null);

// JSON
ClassificationResult result = client.classification()
    .classifyJson("[{\"id_card\":\"110101199001011237\"}]", null);
```

## 2. YAML Profile 配置

在 `profile.yaml` 中配置分类参数：

```yaml
primitives:
  classification:
    version: "1.0.0"
    default_level: "L3"
    enable_rule_engine: true
    enable_small_ner: false
    enable_llm: false
    icd10_l4_intervals:
      - { start: "B20", end: "B24" }
      - { start: "F20", end: "F29" }
      - { start: "C00", end: "C97" }
    genomic_keywords:
      - "brca1"
      - "brca2"
      - "tp53"
    public_field_whitelist:
      - "public_report"
      - "annual_summary"
      - "科普"
    operational_field_patterns:
      - "turnover_rate"
      - "device_usage"
      - "inventory"
    manual_override:
      id_card: "L1"
```

加载：

```java
ClassificationClient client = new ClassificationClient(PrivacyProfile.fromYaml("profile.yaml"));
```

## 3. 运行时参数覆盖

方法级参数优先级高于 Profile：

```java
Map<String, Object> params = Map.of(
    "enable_small_ner", true,
    "manual_override", Map.of("name", "L1")
);
FieldClassificationResult r = client.classification()
    .classifyField("name", "Alice", params);
```

## 4. 结果解读

| 字段 | 含义 |
|---|---|
| `finalLevel` | 最终敏感度等级 L1~L5 |
| `confidence` | 置信度 0.0~1.0 |
| `engineLayer` | 命中引擎：RULE / SMALL_NER / LLM |
| `needsHumanReview` | 是否建议人工复核 |
| `reasoning` | 可解释文本 |
| `tags` | 命中的安全标签列表 |

## 5. 常见问题

### Q: 默认 LLM 会调用远程服务吗？
A: 不会。默认 `NoOpLlmClassifier` 仅在本地做保守处理，不发起任何网络请求。

### Q: 如何接入真实 NER/LLM？
A: 实现 `SmallNerEngine` 或 `LlmClassifier` 接口，并通过自定义构造函数注入：

```java
import com.github.fengzhizi319.privacy.sdk.classification.ClassificationApi;

ClassificationApi api = new ClassificationApi(
    profile, ruleEngine, myNer, myLlm
);
```

### Q: 分类结果可用于做什么？
A: 可作为脱敏策略路由输入、字段访问控制标签、合规审计日志、SecretFlow 组件元数据。
