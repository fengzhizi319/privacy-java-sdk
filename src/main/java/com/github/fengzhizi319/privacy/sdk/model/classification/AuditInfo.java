package com.github.fengzhizi319.privacy.sdk.model.classification;

import java.time.Instant;

/**
 * 分类审计信息（Audit Info）。
 * <p>
 * 记录分类操作的版本、时间戳、规则引擎版本以及参数来源，便于事后审计与结果复现。
 * </p>
 *
 * @author fengzhizi319
 * @since 0.1.0
 */
public class AuditInfo {

    /** 原语版本，默认 1.0.0。 */
    private String version = "1.0.0";

    /** Profile 版本，来自配置文件或 default。 */
    private String profileVersion = "default";

    /** ISO-8601 格式时间戳。 */
    private String timestamp;

    /** 规则引擎版本。 */
    private String ruleEngineVersion = "1.0.0";

    /** 参数来源，例如 default、profile、request、manual。 */
    private String parameterSource = "default";

    /**
     * 默认构造器，自动填充当前 UTC 时间戳。
     */
    public AuditInfo() {
        this.timestamp = Instant.now().toString();
    }

    /**
     * 构造审计信息。
     *
     * @param version           原语版本
     * @param profileVersion    profile 版本
     * @param ruleEngineVersion 规则引擎版本
     * @param parameterSource   参数来源
     */
    public AuditInfo(String version, String profileVersion, String ruleEngineVersion, String parameterSource) {
        this.version = version;
        this.profileVersion = profileVersion;
        this.timestamp = Instant.now().toString();
        this.ruleEngineVersion = ruleEngineVersion;
        this.parameterSource = parameterSource;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getProfileVersion() {
        return profileVersion;
    }

    public void setProfileVersion(String profileVersion) {
        this.profileVersion = profileVersion;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getRuleEngineVersion() {
        return ruleEngineVersion;
    }

    public void setRuleEngineVersion(String ruleEngineVersion) {
        this.ruleEngineVersion = ruleEngineVersion;
    }

    public String getParameterSource() {
        return parameterSource;
    }

    public void setParameterSource(String parameterSource) {
        this.parameterSource = parameterSource;
    }
}
