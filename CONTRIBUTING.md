# Contributing to privacy-java-sdk

Thank you for your interest in contributing! This guide will help you get started.

## Development Setup

```bash
# Clone and enter the repo
git clone https://github.com/fengzhizi319/privacy-java-sdk.git
cd privacy-java-sdk

# Build (requires JDK 17+, Maven 3.8+)
make build

# Run tests
make test

# Install pre-commit hooks
pre-commit install
```

## Code Style

- **Java 17** syntax with Lombok-style patterns (manual getters/setters in this project)
- **Checkstyle**: enforced via `checkstyle.xml` (run `make checkstyle`)
- **SpotBugs**: static analysis (run `make spotbugs`)
- **Javadoc**: all public classes and methods must have Javadoc (bilingual 中文 + English)
- **License header**: Apache-2.0 header on all source files

```bash
# Run all static checks
make check

# Run tests with coverage
make coverage
```

## Testing

```bash
# Unit tests only
make test-unit

# All tests (unit + integration)
make test

# With coverage report
make coverage-report
# Open: target/site/jacoco/index.html
```

- Use **JUnit 5** (`@Test` from `org.junit.jupiter.api`)
- Use **jqwik** for property-based testing of DP statistical guarantees
- Use **JMH** for performance benchmarks (`src/test/.../benchmark/`)
- Integration tests use `maven-failsafe-plugin` (class name `*IT.java`)

## Project Structure

```
src/main/java/com/github/fengzhizi319/privacy/sdk/
├── api/              # Core privacy APIs (DpApi, MaskingApi, KAnonymityApi, QolApi, LocalDpApi)
├── classification/   # Data classification engine
├── exception/        # Custom exceptions
├── model/            # Data models and results
├── util/             # Utilities (BudgetAccountant, PrivacyMetrics, ParameterResolver)
├── PrivacyClient.java    # Main entry point
└── PrivacyProfile.java   # YAML-based configuration
```

## Pull Request Process

1. Fork the repository and create a feature branch
2. Write tests for new functionality
3. Ensure `make verify` passes (compile + test + coverage + static analysis)
4. Update CHANGELOG.md under `[Unreleased]`
5. Submit PR with clear description

## Commit Convention

Use [Conventional Commits](https://www.conventionalcommits.org/):

```
feat: add noisyHistogram to DpApi
fix: budget isolation in multi-namespace scenario
docs: update user manual for LocalDp
test: add jqwik property tests for Gaussian mechanism
```

## Reporting Issues

- Use GitHub Issues for bug reports and feature requests
- Include: Java version, SDK version, minimal reproduction code
- For security vulnerabilities, see [SECURITY.md](SECURITY.md)
