package com.github.fengzhizi319.privacy.sdk.classification.engine;

import com.github.fengzhizi319.privacy.sdk.model.classification.FieldClassificationResult;
import com.github.fengzhizi319.privacy.sdk.model.classification.SensitivityLevel;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 基于本地 Qwen2-VL-2B-Instruct 的多模态 LLM 分类器占位实现。
 * <p>
 * 与 Python 侧的 {@code Qwen2VLClassifier} 保持语义对齐：当本地存在
 * {@code .models/qwen2-vl-2b-instruct} 模型目录时启用，否则安全降级为
 * {@link NoOpLlmClassifier}。当前 Java SDK 尚未引入 transformers/torch 等运行时依赖，
 * 因此即使模型目录存在，{@link #classify(String, String, FieldClassificationResult, SensitivityLevel)}
 * 也会回退到无操作兜底逻辑，避免破坏分类流程。
 * </p>
 *
 * @author fengzhizi319
 * @since 0.1.0
 */
public class Qwen2VlClassifier implements LlmClassifier {

    /** 默认本地模型目录：项目根目录下的 .models/qwen2-vl-2b-instruct。 */
    private static final String DEFAULT_MODEL_PATH = ".models/qwen2-vl-2b-instruct";

    private final String modelPath;
    private final boolean available;
    private final NoOpLlmClassifier fallback = new NoOpLlmClassifier();

    /**
     * 使用默认模型路径构造分类器。
     */
    public Qwen2VlClassifier() {
        this(defaultModelPath());
    }

    /**
     * 使用指定模型路径构造分类器。
     *
     * @param modelPath 本地模型目录路径
     */
    public Qwen2VlClassifier(String modelPath) {
        this.modelPath = modelPath == null ? defaultModelPath() : modelPath;
        this.available = Files.isDirectory(Paths.get(this.modelPath));
    }

    private static String defaultModelPath() {
        return Paths.get(System.getProperty("user.dir"), DEFAULT_MODEL_PATH).toString();
    }

    /**
     * 返回模型路径（用于上层检测）。
     *
     * @return 模型目录路径
     */
    public String getModelPath() {
        return modelPath;
    }

    /**
     * 判断本地模型目录是否存在。
     *
     * @return true 表示模型目录存在
     */
    public boolean isAvailable() {
        return available;
    }

    @Override
    public FieldClassificationResult classify(String fieldName, String value,
                                              FieldClassificationResult upstream,
                                              SensitivityLevel defaultLevel) {
        if (!available) {
            return fallback.classify(fieldName, value, upstream, defaultLevel);
        }
        // 模型目录存在，但完整 Qwen2-VL 推理尚未在 Java SDK 中实现；安全降级。
        return fallback.classify(fieldName, value, upstream, defaultLevel);
    }
}
