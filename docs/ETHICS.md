# Ethics, Privacy & Professional Standards Report

**Project:** Smart Amusement Park Management System (v2)  
**Academic Requirement:** Review Rubrics - Professional Ethics & Standards (CO3)

---

## 1. Principles of Data Minimization (Privacy by Design)
In accordance with international data protection principles (such as GDPR and India's Digital Personal Data Protection Act):
- **Minimal Collection:** The system collects strictly two personal data attributes: **Visitor Full Name** and **Age**.
- **No Unnecessary Telemetry:** No biometrics, government identifiers, contact numbers, or home addresses are collected at the ticket counter.
- **Safety Justification:** Age is collected solely to enforce physical ride safety constraints (preventing young children from boarding high-speed attractions such as the Roller Coaster or motorized Go-Kart).

---

## 2. Informed Consent Notice on Entry Form
The visitor entry interface displays an explicit consent notice directly below the ticket creation panel:
> *"Privacy Consent: Personal data (Name & Age) is recorded strictly for capacity safety compliance and ticket verification. Financial booking history is immutable and maintained permanently for audit integrity."*

Visitors and park guests are informed before ticket issuance how their data is processed.

---

## 3. Financial Record Immutability (Anti-Fraud & Audit Integrity)
Amusement parks handle substantial daily cash and electronic revenue. A common point of fraud in paper or spreadsheet systems is the retroactive deletion of tickets by dishonest operators:
- **Zero Deletion Policy:** The system enforces an architectural **no-delete** policy across all storage tiers (Postgres Row Level Security forbids `DELETE` queries; Java `ParkService` and C `dsa` modules have no delete commands on historical transactions).
- **Status-Based Cancellation:** When a visitor cancels a ticket, the status transitions to `CANCELLED`. The transaction amount, visitor name, ride ID, and timestamp remain permanently recorded in the audit trail.
- **Auditable Reconciliation:** Daily revenue reconciliations evaluate confirmed versus cancelled tickets with full cryptographic auditability.

---

## 4. Honest Fare Transparency & Concessions
- **Fixed Ride Pricing:** Ticket prices are pre-configured in the catalogue and cannot be manually overridden or inflated by counter staff.
- **Explicit Concession Display:** Visitors under the age of 12 automatically receive a **50% Child Discount**. The discount is prominently broken down on the UI preview and the printed receipt (`Task1Fundamentals`), ensuring complete transparency without hidden fees.

---

## 5. Credential Security & Secret Management
- **No Hardcoded Keys:** Cloud database credentials (Supabase URL and API keys) are strictly segregated into `java/config/supabase.properties`.
- **Git-Ignored Secrets:** Only a sanitized template (`supabase.properties.example`) is checked into source control. Production secrets are never leaked into source code or build logs.

---

## 6. Physical Safety & Inclusivity
- **Minimum Age Enforcement:** High-risk rides automatically reject bookings for underage visitors via `AgeRestrictionException`.
- **Accessible Attractions:** Attractions with gentle dynamics (such as the Ferris Wheel, minimum age 5) provide safe family entertainment.
