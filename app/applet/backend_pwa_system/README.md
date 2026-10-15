# KistiBank: Secure Bank Loan, Kisti, Savings, DPS & KYC Management System

Production-ready MVP architecture for microfinance and banking institutions, supporting both a **Native Android Mobile App** and a **PHP 8.2+ MySQL PWA Backend System**.

---

## 🌟 Key Features

1. **Role-Based Access Control (RBAC):**
   - Super Admin (Full access)
   - Branch Manager (Loans, approvals, staff)
   - Collection Officer / Field Employee (Scoped to assigned departments only)
   - Accountant (Ledgers, reconciliation, vouchers)
   - Auditor (Read-only compliance & tamper-evident audit trails)

2. **Member KYC & Nominee Management:**
   - Multi-step KYC registration (Personal, Contact, Nominee, Document)
   - Encrypted and masked NID protection (`1988 **** **** 7890`)
   - Audit-logged unmasking

3. **Loans & Automated Installment (Kisti) Scheduler:**
   - Flexible loan terms: Weekly or Monthly
   - Automatic installment schedule generation (Principal, Interest breakdown, Due date, Balance)
   - Real-time loan balance updates

4. **Field Installment Collection Form & Digital Signature:**
   - Excel-like spreadsheet table for quick scanning
   - Real-time server-validated calculation preview
   - Touch/stylus digital signature pad
   - Idempotency key protection against duplicate submissions
   - Instant printable/shareable official receipt slip with QR verification

5. **Savings & DPS Modules:**
   - General flexible savings accounts (Deposits, withdrawals, profits)
   - DPS Deposit Pension Scheme with monthly installments and maturity tracking

6. **Automated Missed / Overdue Installment Detector:**
   - Flags overdue members without creating fake ledger transactions
   - Regular payer recognition with incentive perks

7. **Double-Entry Financial Ledger & Immutable Audit Logs:**
   - No direct balance overrides; all balances derived from atomic transaction vouchers
   - Reversal workflow with mandatory justification and audit trail

8. **Built-in Financial & Math Calculator:**
   - General math functions
   - Loan EMI / Kisti calculator
   - DPS maturity calculator
   - Session calculation history

9. **Export & Reports:**
   - Daily collection, department summary, outstanding loans
   - Formula-injection-safe CSV/Excel export
   - Print-friendly layout

---

## 🛠️ Backend Installation & Setup (PHP/MySQL)

### Requirements:
- PHP 8.2 or higher (with `pdo_mysql`, `mbstring`, `openssl`)
- MySQL 8.0 or higher
- Web server: Apache / Nginx
- HTTPS enabled (mandatory for PWA Service Workers)

### Steps:
1. **Clone or copy the project:**
   ```bash
   cd backend_pwa_system
   ```
2. **Setup Database:**
   ```bash
   mysql -u root -p -e "CREATE DATABASE kistibank_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
   mysql -u root -p kistibank_db < database/schema.sql
   mysql -u root -p kistibank_db < database/seeders.sql
   ```
3. **Configure Environment:**
   ```bash
   cp .env.example .env
   # Edit .env with your DB credentials
   ```
4. **Deploy Web Server:**
   Point document root to `backend_pwa_system/public/`.
5. **Seed Credentials (Dev Only):**
   - Super Admin: `admin` / `password123`
   - Manager: `manager` / `password123`
   - Field Officer: `officer1` / `password123`

---

## 📱 Android Application (Kotlin & Jetpack Compose)

The companion native Android application is pre-built under `app/src/main/` with:
- Modern Material 3 Emerald & Gold financial theming
- 8 Clickable Dashboard Metric Cards
- Interactive Collection Trend & Portfolio Canvas Charts
- Touch Signature Pad
- Excel-like & Field Collection views
- Thermal/PDF Receipt Generator
- Built-in Financial Calculator Dialog
- Role switcher for testing all roles seamlessly
