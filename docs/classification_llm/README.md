# 数据分类 — LLM 层

## 概述

LLM（Layer 3）用于处理规则引擎与 Small-NER 置信度不足（< 0.6）或复杂语义场景，通过大模型进行零样本分类并输出结构化 JSON。

## 引擎实现

- `Qwen2VlClassifier`：基于本地 Qwen2-VL 多模态模型的分类器（需要模型目录与运行时，未安装时自动降级为 NoOp）。
- `NoOpLlmClassifier`：默认空实现。当上游置信度 < 0.6 时返回保守 fallback 结果并标记 `needsHumanReview=true`。

## 自动选择逻辑

`ClassificationApi` 构造函数按以下顺序选择引擎：

1. 如果 `params.enable_llm=false`，强制使用 `NoOpLlmClassifier`。
2. 如果 `.models/qwen2-vl-2b-instruct` 目录存在，使用 `Qwen2VlClassifier`。
3. 否则降级为 `NoOpLlmClassifier`。

## API 参考

```java
import com.github.fengzhizi319.privacy.sdk.classification.engine.LlmClassifier;
import com.github.fengzhizi319.privacy.sdk.classification.engine.NoOpLlmClassifier;
import com.github.fengzhizi319.privacy.sdk.model.classification.SensitivityLevel;

LlmClassifier llm = new NoOpLlmClassifier();
var result = llm.classify("文本", SensitivityLevel.L3, 0.5);
// result: { final_level=L3, confidence=0.5, needsHumanReview=true, ... }
```

## 自定义引擎

实现 `LlmClassifier` 接口并注入 `ClassificationApi`。

## 测试覆盖

- NoOp 默认 fallback 行为
- enable_llm=false 关闭引擎
- 自定义 LLM 注入

## 注意事项

Qwen2-VL 模型运行时依赖 Python 生态（transformers、torch）。Java SDK 仅提供接口与检测逻辑，真实模型推理可通过 gRPC 桥接到 Python agent 或未来接入 Java ONNX 运行时。
