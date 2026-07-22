# 数据分类 — Small-NER 层

## 概述

Small-NER（Layer 2）基于轻量级医疗领域命名实体识别模型，识别疾病、症状、药物、手术、解剖部位等实体，对规则引擎未命中的字段进行补充升级。

## 引擎实现

- `DefaultRuleEngine`：Layer 1 规则引擎，置信度 1.0。
- `OnnxSmallNerEngine`：基于 ONNX Runtime 的本地 NER 引擎（可选依赖，未安装模型时自动降级为 NoOp）。
- `NoOpSmallNerEngine`：默认空实现，不返回任何实体。

## 自动选择逻辑

`ClassificationApi` 构造函数按以下顺序选择引擎：

1. 如果 `params.enable_small_ner=false`，强制使用 `NoOpSmallNerEngine`。
2. 如果 `.models/raner_cmeee.onnx` 存在且 ONNX Runtime 类在 classpath 中，使用 `OnnxSmallNerEngine`。
3. 否则降级为 `NoOpSmallNerEngine`。

## API 参考

```java
import com.github.fengzhizi319.privacy.sdk.classification.engine.SmallNerEngine;
import com.github.fengzhizi319.privacy.sdk.classification.engine.NoOpSmallNerEngine;

SmallNerEngine ner = new NoOpSmallNerEngine();
List<Map<String, Object>> entities = ner.extract("患者患有糖尿病和高血压");
```

## 自定义引擎

实现 `SmallNerEngine` 接口并注入 `ClassificationApi`：

```java
SmallNerEngine myNer = text -> List.of(Map.of(
    "label", "MEDICAL_DISEASE",
    "text", "糖尿病",
    "confidence", 0.95,
    "start", 3,
    "end", 6
));
// 通过 ClassificationApi 构造函数注入
```

## 测试覆盖

- NoOp 引擎默认返回空
- enable_small_ner=false 关闭引擎
- 自定义引擎注入与标签映射
