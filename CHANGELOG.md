# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.2.0] - 2024-XX-XX

### Added
- **DP Advanced**: histogram, noisyCount/Sum/Mean/Histogram, vectorSum/Mean, adaptiveClip, groupBy (Tau-Thresholding)
- **Local DP**: LocalDpApi — binary/categorical randomized response with unbiased estimators
- **Masking**: email/address field type recognition, maskBatch bulk masking
- **QoL**: semantic slot-filling strategy, length-similarity sampling, extended pools (20 items), obfuscateQueryBatch, QoLResult metadata
- **Observability**: SLF4J logging, optional Micrometer metrics
- **Thread Safety**: ThreadLocalRandom for all DP/QoL APIs (zero contention under concurrency)
- **Build**: JaCoCo coverage (60% line minimum), SpotBugs, Checkstyle, OWASP dependency-check, japicmp, maven-enforcer
- **CI**: GitHub Actions pipeline (build → test → coverage → static analysis)
- **Publishing**: Maven Central ready (source/javadoc/gpg/nexus-staging)
- **Tests**: jqwik property-based tests for DP statistical guarantees, integration tests
- **API**: PrivacyClient.Builder for fluent configuration
- **JPMS**: module-info.java for Java module system support
- **Serialization**: Model classes implement Serializable
- LICENSE (Apache-2.0)

### Changed
- PrivacyResult getters now return unmodifiable views (immutability guarantee)
- DpApi/LocalDpApi/QolApi default constructors use ThreadLocalRandom instead of shared Random
- ParameterResolver uses SLF4J instead of System.err
- Budget bumped to 0.2.0-SNAPSHOT

### Fixed
- PrivacyClientTest budget isolation issue (dedicated namespace)

## [0.1.0] - 2024-XX-XX

### Added
- Initial release: Masking, DP (count/sum/mean), K-Anonymity (Mondrian), Query Obfuscation
- Data Classification module (RuleEngine + SmallNer + LLM pluggable)
- BudgetAccountant with namespace isolation
- ParameterResolver with priority-based parameter merging
- PrivacyResult audit wrapper (data + paramsUsed + proof + warnings)
- Dockerfile multi-stage build
- Comprehensive documentation (PRD/Design/Implementation/Testing/UserManual)
