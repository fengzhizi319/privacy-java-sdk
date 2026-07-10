package com.github.fengzhizi319.privacy.sdk.classification;

import com.github.fengzhizi319.privacy.sdk.PrivacyProfile;

/**
 * 数据分类 Java SDK 的独立入口客户端（Classification Client / Entry Point）。
 * <p>
 * 仅聚合数据分类相关能力，通过 {@link PrivacyProfile} 完成统一配置。
 * 内部不含远程调用，均为本地内存计算。
 * </p>
 *
 * @author fengzhizi319
 * @since 0.1.0
 */
public class ClassificationClient {

    /** 当前客户端关联的隐私配置 profile。 */
    private final PrivacyProfile profile;

    /** 数据分类 API 实例。 */
    private final ClassificationApi classificationApi;

    /**
     * 使用默认空配置构造客户端。
     * <p>
     * 等价于 {@code new ClassificationClient(PrivacyProfile.empty())}。
     * </p>
     */
    public ClassificationClient() {
        this(PrivacyProfile.empty());
    }

    /**
     * 使用指定配置构造客户端。
     *
     * @param profile 隐私配置 profile，不能为 {@code null}
     */
    public ClassificationClient(PrivacyProfile profile) {
        this.profile = profile;
        this.classificationApi = new ClassificationApi(profile);
    }

    /**
     * 获取当前客户端关联的隐私配置。
     *
     * @return {@link PrivacyProfile} 实例，不会为 {@code null}
     */
    public PrivacyProfile getProfile() {
        return profile;
    }

    /**
     * 获取数据分类 API。
     *
     * @return {@link ClassificationApi} 实例
     */
    public ClassificationApi classification() {
        return classificationApi;
    }

    /**
     * 对单个字段进行分类。
     */
    public com.github.fengzhizi319.privacy.sdk.model.classification.FieldClassificationResult classifyField(String fieldName, Object value, java.util.Map<String, Object> params) {
        return classificationApi.classifyField(fieldName, value, params);
    }

    /**
     * 对单条记录进行分类。
     */
    public com.github.fengzhizi319.privacy.sdk.model.classification.RecordClassificationResult classifyRecord(java.util.Map<String, Object> record, java.util.Map<String, Object> params) {
        return classificationApi.classifyRecord(record, params);
    }

    /**
     * 对整张表/批次进行分类。
     */
    public com.github.fengzhizi319.privacy.sdk.model.classification.TableClassificationResult classifyTable(java.util.List<String> schema, java.util.List<java.util.Map<String, Object>> rows, java.util.Map<String, Object> params) {
        return classificationApi.classifyTable(schema, rows, params);
    }

    /**
     * 对 JSON 字符串进行分类。
     */
    public com.github.fengzhizi319.privacy.sdk.model.classification.ClassificationResult classifyJson(String jsonString, java.util.Map<String, Object> params) {
        return classificationApi.classifyJson(jsonString, params);
    }

    /**
     * 从 ResultSet 读取结果并进行分类。
     */
    public com.github.fengzhizi319.privacy.sdk.model.classification.TableClassificationResult classifyResultSet(java.sql.ResultSet rs, java.util.Map<String, Object> params) {
        return classificationApi.classifyResultSet(rs, params);
    }
}
