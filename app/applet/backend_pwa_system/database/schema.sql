-- ============================================================================
-- KistiBank: Bank Loan, Kisti, Savings, DPS & KYC Management System
-- Database Schema: MySQL 8.0+ / InnoDB / UTF8MB4
-- ============================================================================

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS audit_logs;
DROP TABLE IF EXISTS login_logs;
DROP TABLE IF EXISTS drafts;
DROP TABLE IF EXISTS reversals;
DROP TABLE IF EXISTS signatures;
DROP TABLE IF EXISTS documents;
DROP TABLE IF EXISTS collection_assignments;
DROP TABLE IF EXISTS financial_ledger;
DROP TABLE IF EXISTS payment_allocations;
DROP TABLE IF EXISTS payments;
DROP TABLE IF EXISTS loan_installments;
DROP TABLE IF EXISTS loan_approvals;
DROP TABLE IF EXISTS loans;
DROP TABLE IF EXISTS dps_transactions;
DROP TABLE IF EXISTS dps_accounts;
DROP TABLE IF EXISTS savings_transactions;
DROP TABLE IF EXISTS savings_accounts;
DROP TABLE IF EXISTS nominees;
DROP TABLE IF EXISTS member_kyc;
DROP TABLE IF EXISTS members;
DROP TABLE IF EXISTS departments;
DROP TABLE IF EXISTS notices;
DROP TABLE IF EXISTS team_members;
DROP TABLE IF EXISTS institution_settings;
DROP TABLE IF EXISTS role_permissions;
DROP TABLE IF EXISTS permissions;
DROP TABLE IF EXISTS user_roles;
DROP TABLE IF EXISTS roles;
DROP TABLE IF EXISTS users;
SET FOREIGN_KEY_CHECKS = 1;

-- 1. Roles & Permissions
CREATE TABLE roles (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    display_name VARCHAR(100) NOT NULL,
    bengali_name VARCHAR(100) NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE permissions (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    display_name VARCHAR(150) NOT NULL,
    module VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE role_permissions (
    role_id BIGINT UNSIGNED NOT NULL,
    permission_id BIGINT UNSIGNED NOT NULL,
    PRIMARY KEY (role_id, permission_id),
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Users & Employees
CREATE TABLE users (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    mobile VARCHAR(20) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(150) NOT NULL,
    role_id BIGINT UNSIGNED NOT NULL,
    is_active TINYINT(1) DEFAULT 1,
    failed_login_attempts INT DEFAULT 0,
    locked_until TIMESTAMP NULL,
    last_login_at TIMESTAMP NULL,
    last_login_ip VARCHAR(45) NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (role_id) REFERENCES roles(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Departments / Areas
CREATE TABLE departments (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(30) NOT NULL UNIQUE,
    name VARCHAR(150) NOT NULL,
    area_name VARCHAR(150) NOT NULL,
    description TEXT,
    is_active TINYINT(1) DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE collection_assignments (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNSIGNED NOT NULL,
    department_id BIGINT UNSIGNED NOT NULL,
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    assigned_by BIGINT UNSIGNED NULL,
    is_active TINYINT(1) DEFAULT 1,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (department_id) REFERENCES departments(id) ON DELETE CASCADE,
    UNIQUE KEY uq_user_dept (user_id, department_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. Members & KYC
CREATE TABLE members (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    member_code VARCHAR(30) NOT NULL UNIQUE,
    full_name VARCHAR(150) NOT NULL,
    father_name VARCHAR(150),
    mother_name VARCHAR(150),
    date_of_birth DATE,
    gender ENUM('Male', 'Female', 'Other') DEFAULT 'Male',
    blood_group VARCHAR(10),
    occupation VARCHAR(100),
    marital_status VARCHAR(50),
    education VARCHAR(100),
    primary_mobile VARCHAR(20) NOT NULL UNIQUE,
    secondary_mobile VARCHAR(20),
    email VARCHAR(100),
    present_address TEXT NOT NULL,
    permanent_address TEXT NOT NULL,
    department_id BIGINT UNSIGNED NOT NULL,
    status ENUM('ACTIVE', 'INACTIVE', 'PENDING', 'BLOCKED') DEFAULT 'ACTIVE',
    payer_status ENUM('REGULAR', 'LATE', 'OVERDUE', 'COMPLETED', 'NO_LOAN') DEFAULT 'NO_LOAN',
    registration_date DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (department_id) REFERENCES departments(id),
    INDEX idx_member_dept (department_id),
    INDEX idx_member_mobile (primary_mobile),
    INDEX idx_member_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE member_kyc (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    member_id BIGINT UNSIGNED NOT NULL UNIQUE,
    doc_type ENUM('Smart NID', 'National ID', 'Passport', 'Birth Certificate') DEFAULT 'Smart NID',
    doc_number_encrypted TEXT NOT NULL,
    doc_number_masked VARCHAR(50) NOT NULL,
    is_verified TINYINT(1) DEFAULT 0,
    verified_by BIGINT UNSIGNED NULL,
    verified_at TIMESTAMP NULL,
    remarks TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (member_id) REFERENCES members(id) ON DELETE CASCADE,
    FOREIGN KEY (verified_by) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE nominees (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    member_id BIGINT UNSIGNED NOT NULL,
    name VARCHAR(150) NOT NULL,
    relationship VARCHAR(50) NOT NULL,
    mobile VARCHAR(20),
    address TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (member_id) REFERENCES members(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. Loans & Installments
CREATE TABLE loans (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    loan_code VARCHAR(30) NOT NULL UNIQUE,
    member_id BIGINT UNSIGNED NOT NULL,
    department_id BIGINT UNSIGNED NOT NULL,
    principal_amount DECIMAL(18,2) NOT NULL,
    interest_rate_percent DECIMAL(5,2) NOT NULL,
    total_interest DECIMAL(18,2) NOT NULL,
    total_payable DECIMAL(18,2) NOT NULL,
    total_installments INT NOT NULL,
    frequency ENUM('WEEKLY', 'MONTHLY') DEFAULT 'MONTHLY',
    installment_amount DECIMAL(18,2) NOT NULL,
    disbursed_date DATE NOT NULL,
    first_installment_date DATE NOT NULL,
    maturity_date DATE NOT NULL,
    remaining_balance DECIMAL(18,2) NOT NULL,
    paid_principal DECIMAL(18,2) DEFAULT 0.00,
    paid_interest DECIMAL(18,2) DEFAULT 0.00,
    approved_by BIGINT UNSIGNED NULL,
    status ENUM('PENDING', 'APPROVED', 'ACTIVE', 'COMPLETED', 'DEFAULTED', 'CANCELLED') DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (member_id) REFERENCES members(id),
    FOREIGN KEY (department_id) REFERENCES departments(id),
    FOREIGN KEY (approved_by) REFERENCES users(id),
    INDEX idx_loan_member (member_id),
    INDEX idx_loan_dept (department_id),
    INDEX idx_loan_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE loan_installments (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    loan_id BIGINT UNSIGNED NOT NULL,
    installment_no INT NOT NULL,
    due_date DATE NOT NULL,
    due_amount DECIMAL(18,2) NOT NULL,
    principal_component DECIMAL(18,2) NOT NULL,
    interest_component DECIMAL(18,2) NOT NULL,
    paid_amount DECIMAL(18,2) DEFAULT 0.00,
    remaining_balance_after DECIMAL(18,2) NOT NULL,
    status ENUM('PAID', 'UNPAID', 'LATE', 'OVERDUE', 'PARTIAL') DEFAULT 'UNPAID',
    paid_date DATE NULL,
    paid_at TIMESTAMP NULL,
    collector_id BIGINT UNSIGNED NULL,
    receipt_id VARCHAR(50) NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (loan_id) REFERENCES loans(id) ON DELETE CASCADE,
    FOREIGN KEY (collector_id) REFERENCES users(id),
    INDEX idx_inst_loan (loan_id),
    INDEX idx_inst_due (due_date),
    INDEX idx_inst_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. Savings & DPS
CREATE TABLE savings_accounts (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    account_no VARCHAR(30) NOT NULL UNIQUE,
    member_id BIGINT UNSIGNED NOT NULL UNIQUE,
    balance DECIMAL(18,2) DEFAULT 0.00,
    total_deposited DECIMAL(18,2) DEFAULT 0.00,
    total_withdrawn DECIMAL(18,2) DEFAULT 0.00,
    interest_earned DECIMAL(18,2) DEFAULT 0.00,
    opened_date DATE NOT NULL,
    status ENUM('ACTIVE', 'DORMANT', 'CLOSED') DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (member_id) REFERENCES members(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE dps_accounts (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    dps_code VARCHAR(30) NOT NULL UNIQUE,
    member_id BIGINT UNSIGNED NOT NULL,
    monthly_installment DECIMAL(18,2) NOT NULL,
    term_months INT NOT NULL,
    total_paid_months INT DEFAULT 0,
    total_deposited DECIMAL(18,2) DEFAULT 0.00,
    expected_maturity_amount DECIMAL(18,2) NOT NULL,
    start_date DATE NOT NULL,
    maturity_date DATE NOT NULL,
    status ENUM('ACTIVE', 'MATURED', 'CLOSED') DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (member_id) REFERENCES members(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. Payments & Financial Transactions (Double-Entry Ready)
CREATE TABLE payments (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    transaction_code VARCHAR(40) NOT NULL UNIQUE,
    receipt_number VARCHAR(40) NOT NULL UNIQUE,
    member_id BIGINT UNSIGNED NOT NULL,
    department_id BIGINT UNSIGNED NOT NULL,
    collector_id BIGINT UNSIGNED NOT NULL,
    loan_id BIGINT UNSIGNED NULL,
    installment_no INT DEFAULT 0,
    loan_kisti_amount DECIMAL(18,2) DEFAULT 0.00,
    principal_applied DECIMAL(18,2) DEFAULT 0.00,
    interest_applied DECIMAL(18,2) DEFAULT 0.00,
    savings_deposit DECIMAL(18,2) DEFAULT 0.00,
    dps_deposit DECIMAL(18,2) DEFAULT 0.00,
    total_collected DECIMAL(18,2) NOT NULL,
    previous_loan_balance DECIMAL(18,2) DEFAULT 0.00,
    new_loan_balance DECIMAL(18,2) DEFAULT 0.00,
    previous_savings_balance DECIMAL(18,2) DEFAULT 0.00,
    new_savings_balance DECIMAL(18,2) DEFAULT 0.00,
    previous_dps_balance DECIMAL(18,2) DEFAULT 0.00,
    new_dps_balance DECIMAL(18,2) DEFAULT 0.00,
    payment_method VARCHAR(50) DEFAULT 'CASH',
    digital_signature TEXT,
    idempotency_key VARCHAR(100) UNIQUE,
    is_reversed TINYINT(1) DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (member_id) REFERENCES members(id),
    FOREIGN KEY (department_id) REFERENCES departments(id),
    FOREIGN KEY (collector_id) REFERENCES users(id),
    FOREIGN KEY (loan_id) REFERENCES loans(id),
    INDEX idx_payment_txn (transaction_code),
    INDEX idx_payment_member (member_id),
    INDEX idx_payment_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE reversals (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    payment_id BIGINT UNSIGNED NOT NULL,
    authorized_by BIGINT UNSIGNED NOT NULL,
    reason TEXT NOT NULL,
    reversal_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (payment_id) REFERENCES payments(id),
    FOREIGN KEY (authorized_by) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 8. Audit Logs & System Tables
CREATE TABLE audit_logs (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNSIGNED NULL,
    user_name VARCHAR(150),
    user_role VARCHAR(100),
    action VARCHAR(100) NOT NULL,
    resource_type VARCHAR(100) NOT NULL,
    resource_id VARCHAR(100),
    ip_address VARCHAR(45),
    user_agent TEXT,
    details TEXT,
    status VARCHAR(50) DEFAULT 'SUCCESS',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_audit_action (action),
    INDEX idx_audit_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE notices (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    priority ENUM('HIGH', 'NORMAL') DEFAULT 'NORMAL',
    publish_date DATE NOT NULL,
    expiry_date DATE NOT NULL,
    is_published TINYINT(1) DEFAULT 1,
    created_by BIGINT UNSIGNED NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (created_by) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE institution_settings (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    setting_key VARCHAR(100) NOT NULL UNIQUE,
    setting_value TEXT,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
