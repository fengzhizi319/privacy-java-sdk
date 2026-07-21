package com.github.fengzhizi319.privacy.sdk;

import com.github.fengzhizi319.privacy.sdk.api.DpApi;
import com.github.fengzhizi319.privacy.sdk.api.KAnonymityApi;
import com.github.fengzhizi319.privacy.sdk.api.LocalDpApi;
import com.github.fengzhizi319.privacy.sdk.api.MaskingApi;
import com.github.fengzhizi319.privacy.sdk.api.QolApi;
import com.github.fengzhizi319.privacy.sdk.classification.ClassificationApi;
import com.github.fengzhizi319.privacy.sdk.model.classification.ClassificationResult;
import com.github.fengzhizi319.privacy.sdk.model.classification.FieldClassificationResult;
import com.github.fengzhizi319.privacy.sdk.model.classification.RecordClassificationResult;
import com.github.fengzhizi319.privacy.sdk.model.classification.TableClassificationResult;
import com.github.fengzhizi319.privacy.sdk.util.BudgetAccountant;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 隐私计算 Java SDK 的入口客户端（Privacy Client / Entry Point）。
 * <p>
 * 负责聚合脱敏（Masking）、差分隐私（DP）、K-匿名（K-Anonymity）、查询混淆（QoL）与数据敏感度分类（Classification）等数据处理能力。
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

    /** 隐私预算记账本。 */
    private final BudgetAccountant budget;

    /** 脱敏 API 实例。 */
    private final MaskingApi maskingApi;

    /** 差分隐私 API 实例。 */
    private final DpApi dpApi;

    /** K-匿名 API 实例。 */
    private final KAnonymityApi kAnonymityApi;

    /** 查询混淆（Query Obfuscation Layer）API 实例。 */
    private final QolApi qolApi;

    /** 数据分类 API 实例。 */
    private final ClassificationApi classificationApi;

    /** 本地差分隐私 API 实例。 */
    private final LocalDpApi localDpApi;

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
        String ns = profile.getNamespace();
        if (ns == null || ns.isEmpty()) {
            ns = "default";
        }
        this.budget = BudgetAccountant.getInstance(ns, 10.0, 1e-4);
        this.maskingApi = new MaskingApi();
        this.dpApi = new DpApi(this.budget);
        this.kAnonymityApi = new KAnonymityApi();
        this.qolApi = new QolApi();
        this.classificationApi = new ClassificationApi(profile);
        this.localDpApi = new LocalDpApi();
    }

    /**
     * 使用指定配置和指定隐私预算构造客户端。
     *
     * @param profile 隐私配置 profile，不能为 {@code null}
     * @param budget  隐私预算记账本实例，用于差分隐私等消耗预算的原语
     */
    public PrivacyClient(PrivacyProfile profile, BudgetAccountant budget) {
        this.profile = profile;
        this.budget = budget;
        this.maskingApi = new MaskingApi();
        this.dpApi = new DpApi(budget);
        this.kAnonymityApi = new KAnonymityApi();
        this.qolApi = new QolApi();
        this.classificationApi = new ClassificationApi(profile);
        this.localDpApi = new LocalDpApi();
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
     * 获取当前客户端关联的隐私预算记账本。
     *
     * @return {@link BudgetAccountant} 实例
     */
    public BudgetAccountant budget() {
        return budget;
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

    /**
     * 获取数据分类 API。
     *
     * @return {@link ClassificationApi} 实例
     */
    public ClassificationApi classification() {
        return classificationApi;
    }

    /**
     * 获取本地差分隐私 API。
     *
     * @return {@link LocalDpApi} 实例
     */
    public LocalDpApi localDp() {
        return localDpApi;
    }

    // --- 快捷原语封装方法 / Convenience Wrappers ---

    /**
     * 对单个字段值进行脱敏，是 masking().maskValue 的便捷封装。
     */
    public String maskValue(String fieldName, String value, String context) {
        return maskingApi.maskValue(fieldName, value, context);
    }

    /**
     * 返回基于 HMAC-SHA256 的确定性截断哈希值，是 masking().hashValue 的便捷封装。
     */
    public String hashValue(String value, String salt) {
        return maskingApi.hashValue(value, salt);
    }

    /**
     * 对字段值进行截断处理，是 masking().truncate 的便捷封装。
     */
    public String truncate(String value, int keepPrefix) {
        return maskingApi.truncate(value, keepPrefix);
    }

    /**
     * 对记录中所有字符串字段值进行脱敏，是 masking().maskRecord 的便捷封装。
     */
    public Map<String, Object> maskRecord(Map<String, Object> record, String context) {
        return maskingApi.maskRecord(record, context);
    }

    /**
     * 返回带差分隐私噪声的计数结果，是 dp().count 的便捷封装。
     */
    public double dpCount(List<Double> values, double epsilon, double delta, String mechanism) {
        return dpApi.count(values, epsilon, delta, mechanism);
    }

    /**
     * 对真实计数值注入加噪结果，是 dp().count 的便捷封装。
     */
    public double dpCount(long trueCount, double epsilon, double delta, String mechanism) {
        return dpApi.count(trueCount, epsilon, delta, mechanism);
    }

    /**
     * 返回带差分隐私噪声的求和结果，是 dp().sum 的便捷封装。
     */
    public double dpSum(List<Double> values, double epsilon, double delta, String mechanism, Double clipLower, Double clipUpper) {
        return dpApi.sum(values, epsilon, delta, mechanism, clipLower, clipUpper);
    }

    /**
     * 返回带差分隐私噪声的均值结果，是 dp().mean 的便捷封装。
     */
    public double dpMean(List<Double> values, double epsilon, double delta, String mechanism, Double clipLower, Double clipUpper) {
        return dpApi.mean(values, epsilon, delta, mechanism, clipLower, clipUpper);
    }

    /**
     * 对单条记录按 K-匿名要求进行泛化，是 kAnonymity().anonymizeRecord 的便捷封装。
     */
    public Map<String, Object> kAnonymizeRecord(Map<String, Object> record,
                                               List<String> qiCols,
                                               Map<String, KAnonymityApi.GeneralizationHierarchy> hierarchies,
                                               int k) {
        return kAnonymityApi.anonymizeRecord(record, qiCols, hierarchies, k);
    }

    /**
     * 对整张表使用 Mondrian 算法进行 K-匿名泛化，是 kAnonymity().kAnonymizeTable 的便捷封装。
     */
    public List<Map<String, Object>> kAnonymizeTable(List<Map<String, Object>> rows, List<String> qiCols, int k, int maxDepth) {
        return kAnonymityApi.kAnonymizeTable(rows, qiCols, k, maxDepth);
    }

    /**
     * 在真实查询中混入若干假查询以隐藏真实意图，是 qol().obfuscateQuery 的便捷封装。
     * 支持语义槽位替换与长度相近抽样策略。
     */
    public List<String> obfuscateQuery(String query, int numDummies, String domain, List<String> medicalPool, List<String> genericPool) {
        return qolApi.obfuscateQuery(query, numDummies, domain, medicalPool, genericPool);
    }

    /**
     * 对查询进行混淆并返回包含元数据的 QoLResult。
     */
    public QolApi.QoLResult obfuscateQueryWithDetails(String query, int numDummies, String domain, List<String> medicalPool, List<String> genericPool) {
        return qolApi.obfuscateQueryWithDetails(query, numDummies, domain, medicalPool, genericPool);
    }

    /**
     * 批量对查询进行混淆。
     */
    public List<List<String>> obfuscateQueryBatch(List<String> queries, int numDummies, String domain, List<String> medicalPool, List<String> genericPool) {
        return qolApi.obfuscateQueryBatch(queries, numDummies, domain, medicalPool, genericPool);
    }

    /**
     * 对单个字段进行分类，是 classification().classifyField 的便捷封装。
     */
    public FieldClassificationResult classifyField(String fieldName, Object value, Map<String, Object> params) {
        return classificationApi.classifyField(fieldName, value, params);
    }

    // --- 批量脱敏便捷封装 / Batch Masking Wrappers ---

    /**
     * 批量对字段值进行脱敏，是 masking().maskBatch 的便捷封装。
     */
    public List<String> maskBatch(List<String> fieldNames, List<String> values, String context) {
        return maskingApi.maskBatch(fieldNames, values, context);
    }

    // --- DP 高级算子便捷封装 / DP Advanced Wrappers ---

    /**
     * 返回带差分隐私噪声的直方图计数。
     */
    public Map<String, Double> dpHistogram(List<String> values, List<String> categories, double epsilon, double delta, String mechanism) {
        return dpApi.histogram(values, categories, epsilon, delta, mechanism);
    }

    /**
     * 对已聚合计数注入 DP 噪声。
     */
    public double dpNoisyCount(double trueCount, double epsilon, double delta, String mechanism) {
        return dpApi.noisyCount(trueCount, epsilon, delta, mechanism);
    }

    /**
     * 对已聚合求和注入 DP 噪声。
     */
    public double dpNoisySum(double trueSum, double sensitivity, double epsilon, double delta, String mechanism) {
        return dpApi.noisySum(trueSum, sensitivity, epsilon, delta, mechanism);
    }

    /**
     * 对已聚合 sum/count 注入 DP 噪声后得到均值。
     */
    public double dpNoisyMean(double trueSum, double trueCount, double sensitivity, double epsilon, double delta, String mechanism, double minCount) {
        return dpApi.noisyMean(trueSum, trueCount, sensitivity, epsilon, delta, mechanism, minCount);
    }

    /**
     * 对已聚合直方图计数注入 DP 噪声。
     */
    public Map<String, Double> dpNoisyHistogram(Map<String, Double> trueCounts, double epsilon, double delta, String mechanism) {
        return dpApi.noisyHistogram(trueCounts, epsilon, delta, mechanism);
    }

    /**
     * 对高维向量执行 L2 截断并注入 DP 噪声。
     */
    public double[] dpVectorSum(List<double[]> vectors, double maxNorm, double epsilon, double delta, String mechanism) {
        return dpApi.vectorSum(vectors, maxNorm, epsilon, delta, mechanism);
    }

    /**
     * 对高维向量执行 DP 均值。
     */
    public double[] dpVectorMean(List<double[]> vectors, double maxNorm, double epsilon, double delta, String mechanism, double minCount) {
        return dpApi.vectorMean(vectors, maxNorm, epsilon, delta, mechanism, minCount);
    }

    /**
     * 使用 DP 自适应二分搜索估计截断上下界。
     */
    public double[] dpAdaptiveClip(List<Double> values, double epsilon, double targetQuantile, int numIterations, double initialClip) {
        return dpApi.adaptiveClip(values, epsilon, targetQuantile, numIterations, initialClip);
    }

    /**
     * 实现 Tau-Thresholding 差分隐私 Group-By 过滤。
     */
    public Map<String, Double> dpGroupBy(List<Map<String, Object>> rows, String groupCol, String targetCol, String agg, double epsilon, double delta, Double clipLower, Double clipUpper, String mechanism) {
        return dpApi.groupBy(rows, groupCol, targetCol, agg, epsilon, delta, clipLower, clipUpper, mechanism);
    }

    // --- 本地 DP 便捷封装 / Local DP Wrappers ---

    /**
     * 批量对二值数据进行本地 DP 扰动。
     */
    public List<Integer> perturbBinaryBatch(List<Integer> values, double epsilon) {
        return localDpApi.perturbBinaryBatch(values, epsilon);
    }

    /**
     * 批量对类别型数据进行本地 DP 扰动。
     */
    public List<String> perturbCategoricalBatch(List<String> values, List<String> categories, double epsilon) {
        return localDpApi.perturbCategoricalBatch(values, categories, epsilon);
    }

    /**
     * 根据扰动后的二值样本估计真实频率。
     */
    public double estimateBinaryFrequency(List<Integer> reported, double epsilon) {
        return localDpApi.estimateBinaryFrequency(reported, epsilon);
    }

    /**
     * 根据扰动后的类别样本估计各类别真实频率。
     */
    public Map<String, Double> estimateCategoricalHistogram(List<String> reported, List<String> categories, double epsilon) {
        return localDpApi.estimateCategoricalHistogram(reported, categories, epsilon);
    }

    // --- 预算查询 / Budget Query ---

    /**
     * 查询当前命名空间下剩余隐私预算。
     */
    public Map<String, Double> budgetRemaining() {
        return budget.remaining();
    }

    /**
     * 对单条记录进行分类，是 classification().classifyRecord 的便捷封装。
     */
    public RecordClassificationResult classifyRecord(Map<String, Object> record, Map<String, Object> params) {
        return classificationApi.classifyRecord(record, params);
    }

    /**
     * 对整张表进行分类，是 classification().classifyTable 的便捷封装。
     */
    public TableClassificationResult classifyTable(List<String> schema, List<Map<String, Object>> rows, Map<String, Object> params) {
        return classificationApi.classifyTable(schema, rows, params);
    }

    /**
     * 解析 JSON 字节并进行分类，是 classification().classifyJson 的便捷封装。
     */
    public ClassificationResult classifyJson(String jsonString, Map<String, Object> params) {
        return classificationApi.classifyJson(jsonString, params);
    }

    /**
     * 从 ResultSet 读取结果并进行分类，是 classification().classifyResultSet 的便捷封装。
     */
    public TableClassificationResult classifyResultSet(java.sql.ResultSet rs, Map<String, Object> params) {
        return classificationApi.classifyResultSet(rs, params);
    }

    // --- 默认泛化层次结构便捷方法 / Default hierarchies ---

    /**
     * 返回年龄字段的默认泛化层次结构。
     */
    public static KAnonymityApi.GeneralizationHierarchy ageHierarchy() {
        return KAnonymityApi.ageHierarchy();
    }

    /**
     * 返回邮编字段的默认泛化层次结构。
     */
    public static KAnonymityApi.GeneralizationHierarchy zipcodeHierarchy() {
        return KAnonymityApi.zipcodeHierarchy();
    }

    /**
     * 返回性别字段的默认泛化层次结构。
     */
    public static KAnonymityApi.GeneralizationHierarchy genderHierarchy() {
        return KAnonymityApi.genderHierarchy();
    }

    /**
     * 根据输入数据特点，自动推荐差分隐私（DP）和 K-匿名（K-Anonymity）参数并持久化保存。
     *
     * @param values   待分析的 DP 数值列表，可为 {@code null}
     * @param rows     待分析的 K-Anonymity 表格数据列表，可为 {@code null}
     * @param qiCols   准标识符列，可为 {@code null}
     * @return 推荐的参数映射
     */
    public Map<String, Object> recommendAndSaveParams(List<Double> values, List<Map<String, Object>> rows, List<String> qiCols) {
        Map<String, Object> recommendations = new HashMap<>();
        String ns = profile != null ? profile.getNamespace() : "default";

        // 1. 推荐差分隐私（DP）参数
        if (values != null && !values.isEmpty()) {
            int n = values.size();
            List<Double> sortedVals = new java.util.ArrayList<>(values);
            java.util.Collections.sort(sortedVals);

            int p5Idx = (int) (n * 0.05);
            int p95Idx = (int) (n * 0.95);
            if (p95Idx >= n) {
                p95Idx = n - 1;
            }

            double clipLower = sortedVals.get(p5Idx);
            double clipUpper = sortedVals.get(p95Idx);
            if (clipLower == clipUpper) {
                clipLower -= 1.0;
                clipUpper += 1.0;
            }

            double recommendedDelta = 1e-5;
            if (n > 0) {
                double val = 1.0 / (10.0 * n * n);
                if (val < recommendedDelta) {
                    recommendedDelta = val;
                }
            }

            Map<String, Object> dpParams = Map.of(
                "epsilon", 1.0,
                "delta", recommendedDelta,
                "mechanism", "laplace",
                "clip_lower", clipLower,
                "clip_upper", clipUpper
            );
            com.github.fengzhizi319.privacy.sdk.util.ParameterResolver.savePersonalizedParams(ns, "dp", dpParams);
            recommendations.put("dp", dpParams);
        }

        // 2. 推荐 K-Anonymity 参数
        if (rows != null && !rows.isEmpty()) {
            int n = rows.size();
            int recommendedK = n / 10;
            if (recommendedK < 2) {
                recommendedK = 2;
            } else if (recommendedK > 10) {
                recommendedK = 10;
            }

            Map<String, Object> kanoParams = Map.of(
                "k", recommendedK,
                "max_depth", 10
            );
            com.github.fengzhizi319.privacy.sdk.util.ParameterResolver.savePersonalizedParams(ns, "k_anonymity", kanoParams);
            recommendations.put("k_anonymity", kanoParams);
        }

        return recommendations;
    }
}
