package com.github.fengzhizi319.privacy.sdk;

import com.github.fengzhizi319.privacy.sdk.api.DpApi;
import com.github.fengzhizi319.privacy.sdk.api.KAnonymityApi;
import com.github.fengzhizi319.privacy.sdk.api.MaskingApi;
import com.github.fengzhizi319.privacy.sdk.api.QolApi;
import com.github.fengzhizi319.privacy.sdk.util.BudgetAccountant;

/**
 * 隐私计算 Java SDK 的入口客户端（Privacy Client / Entry Point）。
 * <p>
 * 负责聚合四类隐私处理原语 API：脱敏（Masking）、差分隐私（DP）、K-匿名（K-Anonymity）、查询混淆（QoL）。
 * 通过 {@link PrivacyProfile} 完成统一配置，通过 {@link BudgetAccountant} 统一管控隐私预算。
 * </p>
 *
 * <p><b>实现注意点：</b>当前版本各 API 实例在构造时即初始化，内部不含远程调用，均为本地内存计算。</p>
 *
 * @author fengzhizi319
 * @since 0.1.0
 */
public class PrivacyClient {

    /** 当前客户端关联的隐私配置 profile。 */
    private final PrivacyProfile profile;

    /** 脱敏 API 实例。 */
    private final MaskingApi maskingApi;

    /** 差分隐私 API 实例。 */
    private final DpApi dpApi;

    /** K-匿名 API 实例。 */
    private final KAnonymityApi kAnonymityApi;

    /** 查询混淆（Query Obfuscation Layer）API 实例。 */
    private final QolApi qolApi;

    /**
     * 使用默认空配置构造客户端。
     * <p>
     * 等价于 {@code new PrivacyClient(PrivacyProfile.empty())}。
     * 差分隐私将使用 namespace 为 "default"、epsilon=10.0、delta=1e-4 的默认预算。
     * </p>
     */
    public PrivacyClient() {
        this(PrivacyProfile.empty());
    }

    /**
     * 使用指定配置构造客户端，并采用默认隐私预算。
     *
     * @param profile 隐私配置 profile，不能为 {@code null}
     */
    public PrivacyClient(PrivacyProfile profile) {
        this.profile = profile;
        this.maskingApi = new MaskingApi();
        this.dpApi = new DpApi(BudgetAccountant.getInstance("default", 10.0, 1e-4));
        this.kAnonymityApi = new KAnonymityApi();
        this.qolApi = new QolApi();
    }

    /**
     * 使用指定配置和指定隐私预算构造客户端。
     *
     * @param profile 隐私配置 profile，不能为 {@code null}
     * @param budget  隐私预算记账本实例，用于差分隐私等消耗预算的原语
     */
    public PrivacyClient(PrivacyProfile profile, BudgetAccountant budget) {
        this.profile = profile;
        this.maskingApi = new MaskingApi();
        this.dpApi = new DpApi(budget);
        this.kAnonymityApi = new KAnonymityApi();
        this.qolApi = new QolApi();
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
     * 获取脱敏 API。
     *
     * @return {@link MaskingApi} 实例
     */
    public MaskingApi masking() {
        return maskingApi;
    }

    /**
     * 获取差分隐私 API。
     *
     * @return {@link DpApi} 实例
     */
    public DpApi dp() {
        return dpApi;
    }

    /**
     * 获取 K-匿名 API。
     *
     * @return {@link KAnonymityApi} 实例
     */
    public KAnonymityApi kAnonymity() {
        return kAnonymityApi;
    }

    /**
     * 获取查询混淆 API。
     *
     * @return {@link QolApi} 实例
     */
    public QolApi qol() {
        return qolApi;
    }
}
