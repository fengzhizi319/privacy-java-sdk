package com.github.fengzhizi319.privacy.sdk.classification.engine;

import com.github.fengzhizi319.privacy.sdk.model.classification.SecurityTag;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 基于 ONNX Runtime 的本地轻量级 Small-NER 引擎占位实现。
 * <p>
 * 与 Python 侧的 {@code ONNXSmallNerEngine} 保持语义对齐：当本地存在
 * {@code .models/raner_cmeee.onnx} 且 ONNX Runtime 类可用时启用，否则安全降级为无操作。
 * 本类仅使用反射访问 {@code ai.onnxruntime.OrtEnvironment} 与 {@code ai.onnxruntime.OrtSession}，
 * 因此编译期不依赖 ONNX Runtime，运行时也不强制要求该类存在。
 * </p>
 * <p>
 * 当前实现仅完成模型与依赖检测；由于 ONNX 输入输出规格未完全确定，
 * 当模型真实可加载时 {@link #extract(String)} 会抛出
 * {@link UnsupportedOperationException}，外层 {@link #recognize(String, String)} 会捕获该异常并返回空列表，
 * 确保不破坏分类流程。
 * </p>
 *
 * @author fengzhizi319
 * @since 0.1.0
 */
public class OnnxSmallNerEngine implements SmallNerEngine {

    /** 默认 ONNX 模型路径：项目根目录下的 .models/raner_cmeee.onnx。 */
    private static final String DEFAULT_MODEL_PATH = ".models/raner_cmeee.onnx";

    private final Path modelPath;

    /**
     * 使用默认模型路径构造引擎。
     */
    public OnnxSmallNerEngine() {
        this(defaultModelPath().toString());
    }

    /**
     * 使用指定模型路径构造引擎。
     *
     * @param modelPath ONNX 模型文件路径
     */
    public OnnxSmallNerEngine(String modelPath) {
        this.modelPath = modelPath == null ? defaultModelPath() : Paths.get(modelPath);
    }

    private static Path defaultModelPath() {
        return Paths.get(System.getProperty("user.dir"), DEFAULT_MODEL_PATH);
    }

    /**
     * 返回模型路径（用于上层检测）。
     *
     * @return 模型路径
     */
    public String getModelPath() {
        return modelPath.toString();
    }

    @Override
    public List<Map<String, Object>> extract(String text) {
        if (text == null || text.isEmpty()) {
            return Collections.emptyList();
        }
        if (!Files.exists(modelPath)) {
            return Collections.emptyList();
        }
        if (!isOnnxRuntimeAvailable()) {
            return Collections.emptyList();
        }
        // 模型与依赖均存在，但完整推理逻辑尚未在 Java SDK 中实现。
        throw new UnsupportedOperationException(
            "ONNX Small-NER inference is not yet implemented in this Java SDK; model present at " + modelPath
        );
    }

    @Override
    public List<SecurityTag> recognize(String fieldName, String text) {
        try {
            List<Map<String, Object>> entities = extract(text);
            if (entities == null || entities.isEmpty()) {
                return Collections.emptyList();
            }
            // 若未来 extract 返回实体，可在此映射为 SecurityTag。
            return Collections.emptyList();
        } catch (UnsupportedOperationException e) {
            // 安全降级，避免未实现的 ONNX 推理中断分类流程
            return Collections.emptyList();
        }
    }

    /**
     * 通过反射检查 ONNX Runtime 是否位于类路径中。
     *
     * @return true 表示 {@code ai.onnxruntime.OrtEnvironment} 与 {@code OrtSession} 可用
     */
    public static boolean isOnnxRuntimeAvailable() {
        try {
            Class.forName("ai.onnxruntime.OrtEnvironment");
            Class.forName("ai.onnxruntime.OrtSession");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
