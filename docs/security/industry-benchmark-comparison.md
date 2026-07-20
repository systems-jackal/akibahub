# Security Posture vs. Industry Standards & Statistics

*Last updated: 2026-07-14. All statistics below are cited to their source; figures are from the IBM Cost of a Data Breach Report 2025 (Ponemon Institute, 600 organizations across 17 industries) unless otherwise noted.*

## Why this comparison matters for a student project

A security review is more convincing — and more honest — when it's benchmarked against real numbers rather than general best-practice language. This document does two things: (1) places Akiba Hub against the formal standard the industry actually uses to grade fintech application security (OWASP ASVS), and (2) uses published breach-cost data to explain *why* specific controls in this codebase (ledger design, incident response readiness, detection speed) matter in dollar terms, not just abstract "best practice."

---

## 1. Where Akiba Hub sits on the OWASP ASVS scale

OWASP's Application Security Verification Standard defines three levels of increasing rigor:

- **Level 1** — baseline, fully automatable, appropriate for low-risk applications. "Every application should aim for at least Level 1."
- **Level 2** — the standard most financial-services organizations target for customer-facing applications handling sensitive data or business-critical transactions; requires both automated and manual verification.
- **Level 3** — reserved for the highest-value/highest-sensitivity systems (large-scale financial trading, critical infrastructure, government) and requires comprehensive architecture review and extensive penetration testing.

**Where Akiba Hub is realistically positioned right now:** solidly past Level 1, partway into Level 2. The controls this hardening pass added — consistent access control enforcement, input validation at the point money moves, an immutable ledger, idempotency, short-lived tokens with revocable refresh tokens — are exactly the category of control ASVS Level 2 asks for. What's missing to *claim* Level 2 formally: MFA (an explicit ASVS Level 2 authentication requirement), a real automated test suite backing up manual verification, and a dependency-scanning step that actually runs (declared in `pom.xml` but not wired into CI — see `risk-register.md` R-15).

**Why this matters concretely:** Akiba Hub, as a chama/savings platform handling personal financial data and eventually real money movement via M-Pesa, is the exact category ASVS explicitly names as needing Level 2 as a baseline, not Level 1. Treating this as a Level 1 (basic/automatable-only) application would be a mismatch between the standard and the actual risk profile.

---

## 2. What breach-cost data says about *why* specific decisions here matter

From IBM's Cost of a Data Breach Report 2025 (the industry's most-cited annual benchmark, drawing on 600 breached organizations across 17 industries):

- **Financial services averages $5.56M per breach** — the second-highest of all 17 industries studied, behind only healthcare, and 25% above the $4.44M global average.
- **The global average breach lifecycle is 241 days** (181 days to identify + 60 days to contain) — the shortest in nine years, but still nearly eight months.
- **Breaches contained within 200 days cost $3.87M on average; those exceeding 200 days cost $5.01M** — a $1.14M penalty specifically for slow detection.
- **A tested incident response plan is the single largest cost reducer identified in the report ($2.66M in average savings).**
- **51% of breaches are caused by malicious/criminal attack** (vs. human error at 26%, IT failure at 23%); **phishing is the single most common initial access vector at 16%.**

**How this maps to decisions made in this codebase:**

| Industry finding | What it implies | Where Akiba Hub stands |
|---|---|---|
| Detection speed is the single biggest cost driver | Fast, reliable detection of anomalies (a ledger that doesn't balance, an unusual spike in failed logins) is worth more than almost any other single control | **Gap.** No monitoring/alerting layer exists yet (`risk-register.md` R-17) — this is the single highest-leverage thing to build next from a pure cost-avoidance standpoint, not just a "nice to have." |
| Financial services is a specifically high-cost target sector | Generic web-app security posture isn't sufficient; sector-specific risk (fraud, regulatory fines, customer financial loss) needs explicit modeling | Addressed structurally: the ledger redesign exists *specifically because* "we validated inputs" isn't sufficient for a financial ledger — the audit-trail and reconciliation properties it adds are a direct response to this class of risk. |
| Tested incident response plans save the most money of any single measure | Having a plan and having *tested* it are different — an untested runbook doesn't deliver this benefit | **Gap.** No incident response plan exists yet for this project. Given it's a fintech-track project, drafting even a one-page IR runbook (who does what if a breach is suspected, how transfers get frozen, how affected users get notified) would be a high-value, low-effort addition. |
| Phishing is the single most common attack vector | Technical controls alone (rate limiting, IDOR fixes) don't address the most statistically common way real breaches start | Outside this codebase's direct control (phishing targets people, not code), but relevant context for *why* MFA (R-10, still open) matters disproportionately — MFA is specifically the control that limits the blast radius of a successful phishing attack, since a stolen password alone stops being sufficient. |

---

## 3. Honest self-assessment: what a real fintech at Akiba Hub's stage would additionally have

This section exists to keep the comparison honest rather than self-congratulatory. Established fintechs (M-Pesa itself, Paystack, Stripe) that Akiba Hub's design decisions were informed by typically also have, which this project does not yet:

- **A dedicated security/compliance function** — even a single named person accountable for security decisions, not folded into general engineering.
- **Regular, independent penetration testing** — not just self-review (which is what this entire engagement has been: two people, no external red team).
- **Formal KYC/AML tooling**, typically via a third-party identity verification provider, with ongoing transaction monitoring for suspicious patterns — not just a uniqueness check on a national ID number at registration.
- **A bug bounty or responsible disclosure program** — a channel for external researchers to report issues, currently nonexistent here.
- **Regulatory registration and licensing** as a financial service provider under the relevant jurisdiction's framework (in Kenya: CBK oversight considerations, POCAMLA compliance for AML) — a legal/business requirement, not a code-level one, but material to whether this could actually operate with real money.

None of this diminishes the engineering work done — a genuinely double-entry ledger, consistent access control, and revocable session management are not trivial, and plenty of shipped products lack them. But a school project framed around cybersecurity is stronger for explicitly naming the difference between "hardened for what it is" and "ready to hold real depositor funds," rather than implying the two are the same thing.

---

## Sources

- IBM Security, *Cost of a Data Breach Report 2025* (Ponemon Institute), ibm.com/reports/data-breach
- OWASP Foundation, *Application Security Verification Standard (ASVS)*, owasp.org/www-project-application-security-verification-standard