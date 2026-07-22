# 差分隐私（DP）产品需求文档

## 1. 背景

差分隐私（Differential Privacy）通过在统计查询结果中注入精心校准的随机噪声，确保单条记录的加入或移除不会显著改变查询输出，从而为个体数据提供严格的数学隐私保证。

## 2. 目标

- 提供 Laplace 与 Gaussian 两种噪声机制。
- 支持 Count、Sum、Mean、Histogram 等基础聚合查询。
- 支持对已聚合值注入噪声（NoisyCount/NoisySum/NoisyMean）。
- 支持向量求和、自适应裁剪、分组聚合等高级算子。
- 与 BudgetAccountant 集成，自动管控隐私预算。

## 3. 功能需求

### 3.1 核心功能

| 编号 | 需求 | 优先级 |
|---|---|---|
| DP-01 | Count：带噪声计数 | P0 |
| DP-02 | Sum：带噪声求和（含 clipping） | P0 |
| DP-03 | Mean：带噪声均值 | P0 |
| DP-04 | Histogram：带噪声直方图 | P1 |
| DP-05 | NoisyCount：对已聚合计数加噪 | P0 |
| DP-06 | NoisySum：对已聚合求和加噪 | P0 |
| DP-07 | NoisyMean：对已聚合均值加噪 | P1 |
| DP-08 | VectorSum：向量维度求和加噪 | P1 |
| DP-09 | AdaptiveClip：自适应裁剪 | P2 |
| DP-10 | GroupBy：分组聚合加噪 | P2 |
| DP-11 | Laplace 机制 | P0 |
| DP-12 | Gaussian 机制 | P0 |

### 3.2 非功能需求

| 编号 | 需求 | 指标 |
|---|---|---|
| DP-NF01 | 低延迟 | 单次查询 < 1ms |
| DP-NF02 | 无外部依赖 | 纯本地计算 |
| DP-NF03 | 线程安全 | 并发调用无竞争 |
| DP-NF04 | 预算集成 | 自动消耗 BudgetAccountant |

## 4. 用户故事

### 4.1 统计报表发布

> 作为数据分析师，我需要对用户年龄、消费金额等字段进行 DP 统计后发布报表，确保无法从报表反推个体数据。

### 4.2 实时计数保护

> 作为后端开发者，我需要对 API 调用次数进行带噪声计数后展示，防止通过精确计数推断用户行为。

## 5. 验收标准

- [ ] Count 结果在真实值附近波动（噪声量与 ε 成反比）
- [ ] Sum 支持 clipping 限制敏感度
- [ ] Mean = NoisySum / NoisyCount
- [ ] Histogram 各 bin 独立加噪
- [ ] Laplace 与 Gaussian 机制均可用
- [ ] 每次查询自动消耗预算
- [ ] 预算耗尽时抛出异常

## 6. 约束与限制

- 当前采用顺序组合定理，未实现 RDP/zCDP
- 敏感度需调用方根据业务确定
- 不提供隐私损失的数学证明工具
