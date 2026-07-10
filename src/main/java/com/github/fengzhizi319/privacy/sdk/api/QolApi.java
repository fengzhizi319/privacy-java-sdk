package com.github.fengzhizi319.privacy.sdk.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 查询混淆 API（Query Obfuscation Layer API）。
 * <p>
 * 通过向真实查询中混入若干条虚拟查询（dummy queries）并随机打乱位置，
 * 降低外部观察者通过查询内容推断用户真实意图的能力。
 * </p>
 *
 * <p><b>实现注意点：</b>当前仅内置医疗（medical）与通用（generic）两类 dummy 语料池，
 * 实际部署建议根据业务领域动态加载语料并定期更新。</p>
 *
 * @author fengzhizi319
 * @since 0.1.0
 */
public class QolApi {

    /** 用于选择 dummy 查询与插入位置的随机数生成器。 */
    private final Random random = new Random();

    /**
     * 将真实查询混入指定数量的虚拟查询中，并随机插入到结果列表的某个位置。
     *
     * @param query      真实查询字符串
     * @param numDummies 虚拟查询数量，需大于等于 0
     * @param domain     领域标识，例如 "medical"；非 medical 时使用通用语料池
     * @return 包含真实查询与虚拟查询的列表，总长度为 {@code numDummies + 1}
     */
    public List<String> obfuscateQuery(String query, int numDummies, String domain) {
        List<String> dummies = new ArrayList<>();
        for (int i = 0; i < numDummies; i++) {
            dummies.add(generateDummy(domain));
        }
        // insert real query at random position
        int pos = random.nextInt(dummies.size() + 1);
        dummies.add(pos, query);
        return dummies;
    }

    /**
     * 根据领域随机生成一条虚拟查询。
     *
     * @param domain 领域标识
     * @return 随机选择的虚拟查询字符串
     */
    private String generateDummy(String domain) {
        List<String> medical = List.of(
            "高血压患者的日常饮食建议",
            "糖尿病患者运动注意事项",
            "冠心病的早期症状有哪些",
            "流感疫苗接种人群建议",
            "儿童常见过敏反应处理"
        );
        List<String> generic = List.of(
            "天气预报查询",
            "附近医院挂号流程",
            "健康档案如何查询",
            "医保报销比例说明",
            "体检报告解读指南"
        );
        List<String> pool = "medical".equalsIgnoreCase(domain) ? medical : generic;
        return pool.get(random.nextInt(pool.size()));
    }
}
