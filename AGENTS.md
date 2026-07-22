# privacy-java-sdk — Agent 开发指南

## 项目概述

`privacy-java-sdk` 是 Java 本地隐私保护 SDK，提供可直接嵌入 Java 后端应用的隐私算法库。当前版本与 Python `privacy-local-agent`、Go `privacy-go-sdk` 保持算法接口与功能一致。

## 技术栈

- Java 17
- Maven
- 运行时依赖：`snakeyaml`（必选）、`sqlite-jdbc`（可选，预算持久化）、`onnxruntime`（可选，NER）

## 目录结构

```text
src/main/java/com/github/fengzhizi319/privacy/sdk/
├── PrivacyClient.java          # 主入口
├── PrivacyProfile.java           # YAML Profile 加载
├── api/                          # 处理原语 API
│   ├── MaskingApi.java
│   ├── DpApi.java
│   ├── LocalDpApi.java           # 本地差分隐私
│   ├── KAnonymityApi.java
│   └── QolApi.java
├── classification/             # 数据分类
│   ├── ClassificationApi.java
│   ├── ClassificationClient.java
│   ├── TemplateProvider.java
│   ├── async/                  # 异步任务
│   ├── composite/              # 复合规则
│   ├── engine/                 # 规则引擎、NER、LLM
│   ├── review/                 # 复核存储
│   └── util/                   # 校验工具
├── model/                       # 数据模型
│   ├── PrivacyContext.java
│   ├── PrivacyResult.java
│   └── classification/          # 分类模型
├── util/                        # BudgetAccountant、ParameterResolver
└── exception/                   # 异常体系

src/test/java/...                 # JUnit 5 测试
docs/                             # SDLC 文档
```

## 构建与测试

```bash
# 编译测试
mvn clean test

# 打包（跳过测试）
mvn clean package -DskipTests

# 安装到本地 Maven 仓库
mvn clean install -DskipTests
```

## 如何添加新原语

1. 在 `api/` 下实现核心算法类，确保无状态或线程安全。
2. 在 `PrivacyClient` 中添加 API 访问器与便捷方法。
3. 在 `ParameterResolver.defaultParams` 中补充默认参数。
4. 在 `util/BudgetAccountant` 中考虑是否需要预算消耗。
5. 添加 JUnit 5 测试。
6. 在 `docs/` 下创建对应目录并写入 SDLC 文档。
7. 更新 `README.md` 与 `AGENTS.md`。

## 代码规范

- 文件头包含 Apache-2.0 许可注释（如项目已有）。
- 公共 API 使用 Javadoc，中英双语注释。
- 优先使用 JDK 17 语法（`switch` 表达式、`var` 局部变量等）。
- 避免引入新的非可选运行时依赖；可选依赖在 `pom.xml` 中标记 `<optional>true</optional>`。

## 测试规范

- 每个公共类都应有对应 `*Test.java`。
- DP 测试需考虑随机性，使用 seeded `Random` 注入。
- 预算测试应清理 `BudgetAccountant` 单例状态或使用独立 namespace。
- 运行全部测试：

```bash
mvn test
```
