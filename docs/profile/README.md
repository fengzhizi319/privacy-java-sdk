# 参数治理（Profile / Parameter Resolver）

## 概述

通过 YAML Profile 与个性化参数文件管理各隐私原语的默认参数。支持默认值、Profile、模板、个性化参数、请求参数五级优先级合并。

## API 参考

```java
import com.github.fengzhizi319.privacy.sdk.PrivacyProfile;
import com.github.fengzhizi319.privacy.sdk.PrivacyClient;

PrivacyProfile profile = PrivacyProfile.fromYaml("privacy-profile.yaml");
PrivacyClient client = new PrivacyClient(profile);

// 推荐并保存个性化参数
Map<String, Object> rec = client.recommendAndSaveParams(
    List.of(1.0, 2.0, 3.0, 100.0),
    List.of(Map.of("age", 25, "zipcode", "100001")),
    List.of("age", "zipcode")
);
```

## YAML Profile 示例

```yaml
version: "1.0"
namespace: hospital_a

primitives:
  dp:
    epsilon: 1.0
    delta: 1.0e-5
    mechanism: laplace
  k_anonymity:
    k: 5
    l: 2
    t: 0.2
    max_depth: 10
  qol:
    num_dummies: 3
  classification:
    default_level: "L3"
    enable_rule_engine: true
    enable_small_ner: false
    enable_llm: false
    template: "gbt35273"
```

## 参数优先级

1. 请求参数（最高）
2. 个性化参数（personalized-profiles.yaml）
3. Profile 中 primitives 配置
4. 合规模板默认参数
5. 内置默认值（最低）

## 环境变量

| 变量 | 说明 |
|---|---|
| `PRIVACY_PROFILE` | 默认 Profile 路径 |
| `PRIVACY_PERSONALIZED_PROFILE` | 个性化参数文件路径，默认 `personalized-profiles.yaml` |

## 测试覆盖

- 默认值加载
- Profile 覆盖
- 请求参数最终覆盖
- 个性化参数持久化与读取
- 参数校验（epsilon > 0, k >= 2）
