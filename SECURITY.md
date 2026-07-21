# Security Policy

## Supported Versions

| Version | Supported |
|---------|-----------|
| 0.2.x   | ✅        |
| 0.1.x   | ⚠️ critical only |

## Reporting a Vulnerability

We take the security of privacy-java-sdk seriously. If you believe you have found a
security vulnerability, please report it responsibly.

**Please do NOT open a public GitHub issue for security vulnerabilities.**

### How to Report

1. Navigate to: **Security → Advisories → Report a vulnerability**
2. Include:
   - Description of the vulnerability
   - Steps to reproduce
   - Potential impact
   - Suggested fix (if any)

### Response Timeline

- **Acknowledgment**: Within 48 hours
- **Initial assessment**: Within 7 days
- **Fix timeline**: Critical: 7 days, High: 14 days, Medium: 30 days

### Scope

The following are in scope:
- Privacy budget manipulation / bypass
- Noise generation weaknesses (predictable RNG)
- Data leakage through masking/hash reversal
- Deserialization vulnerabilities
- Dependency vulnerabilities (CVSS ≥ 7)

### Out of Scope

- Social engineering attacks
- Physical access attacks
- Denial of service via resource exhaustion (unless trivially exploitable)

## Security Measures

This project implements the following security practices:

- **OWASP Dependency Check**: automated CVE scanning (`make security`)
- **SpotBugs**: static analysis for common vulnerability patterns
- **ThreadLocalRandom**: cryptographically appropriate RNG for DP noise
- **Input validation**: parameter bounds checking in all public APIs
- **Immutable results**: PrivacyResult returns unmodifiable views
