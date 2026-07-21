# privacy-java-sdk Makefile
# 提供构建、测试、覆盖率、静态分析等常用命令

.PHONY: all build test test-unit test-it coverage coverage-report lint checkstyle spotbugs \
        check format clean help package verify security javadoc

# 默认目标
all: check test package

## build: 编译项目
build:
	@echo "==> Compiling..."
	mvn -B compile -q

## test: 运行所有测试（单元 + 集成）
test:
	@echo "==> Running all tests..."
	mvn -B verify

## test-unit: 仅运行单元测试
test-unit:
	@echo "==> Running unit tests..."
	mvn -B test

## test-it: 运行集成测试
test-it:
	@echo "==> Running integration tests..."
	mvn -B verify -DskipTests=false -Dtest=skip -DfailIfNoTests=false failsafe:integration-test failsafe:verify

## coverage: 运行测试 + JaCoCo 覆盖率检查
coverage:
	@echo "==> Running tests with coverage..."
	mvn -B verify -Pcoverage

## coverage-report: 生成覆盖率报告并打开
coverage-report:
	@echo "==> Generating coverage report..."
	mvn -B test jacoco:report
	@echo "Report: target/site/jacoco/index.html"

## lint: 运行 Checkstyle 检查
lint: checkstyle

## checkstyle: 运行 Checkstyle
checkstyle:
	@echo "==> Running Checkstyle..."
	mvn -B checkstyle:check -Dcheckstyle.failOnViolation=false

## spotbugs: 运行 SpotBugs 静态分析
spotbugs:
	@echo "==> Running SpotBugs..."
	mvn -B compile spotbugs:check -Dspotbugs.failOnError=false

## check: 一键静态分析 (checkstyle + spotbugs + enforcer)
check:
	@echo "==> Running all static checks..."
	mvn -B validate compile spotbugs:check checkstyle:check -Dspotbugs.failOnError=false -Dcheckstyle.failOnViolation=false

## format: 格式化代码（使用 formatter-maven-plugin，如已配置）
format:
	@echo "==> Formatting code..."
	@echo "Note: Use IDE formatter (IntelliJ/Eclipse) with checkstyle.xml as reference."
	mvn -B checkstyle:check -Dcheckstyle.failOnViolation=false || true

## verify: 完整验证（编译 + 测试 + 覆盖率 + 静态分析）
verify:
	@echo "==> Full verification..."
	mvn -B clean verify

## security: OWASP 依赖漏洞扫描
security:
	@echo "==> Running OWASP dependency check..."
	mvn -B verify -Psecurity -DskipTests

## package: 打包 jar（跳过测试）
package:
	@echo "==> Packaging..."
	mvn -B clean package -DskipTests

## javadoc: 生成 Javadoc
javadoc:
	@echo "==> Generating Javadoc..."
	mvn -B javadoc:javadoc
	@echo "Javadoc: target/site/apidocs/index.html"

## clean: 清理构建产物
clean:
	@echo "==> Cleaning..."
	mvn -B clean

## help: 显示帮助信息
help:
	@echo "privacy-java-sdk Makefile"
	@echo ""
	@echo "Usage: make [target]"
	@echo ""
	@echo "Targets:"
	@grep -E '^## ' $(MAKEFILE_LIST) | sed 's/## /  /'
