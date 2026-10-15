-- ============================================================================
-- KistiBank: Database Seed Data
-- Passwords: All hashed with password_hash('password123', PASSWORD_BCRYPT)
-- Hash: $2y$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi
-- ============================================================================

-- Roles
INSERT INTO roles (id, name, display_name, bengali_name, description) VALUES
(1, 'super_admin', 'Super Admin', 'সুপার অ্যাডমিন', 'Full system administrative control'),
(2, 'manager', 'Branch Manager', 'শাখা ব্যবস্থাপক', 'Manages branches, loans, and staff'),
(3, 'collection_officer', 'Collection Officer', 'মাঠকর্মী', 'Field collections in assigned departments'),
(4, 'accountant', 'Accountant', 'হিসাবরক্ষক', 'Financial records and reconciliations'),
(5, 'auditor', 'Auditor', 'নিরীক্ষক', 'Read-only compliance and audit inspections');

-- Users
INSERT INTO users (id, username, email, mobile, password_hash, full_name, role_id) VALUES
(1, 'admin', 'admin@kistibank.com', '+8801711000001', '$2y$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Tariqul Islam', 1),
(2, 'manager', 'manager@kistibank.com', '+8801711000002', '$2y$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Anisur Rahman', 2),
(3, 'officer1', 'kamal@kistibank.com', '+8801711000003', '$2y$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Md. Kamal Hossain', 3),
(4, 'officer2', 'salma@kistibank.com', '+8801711000004', '$2y$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Salma Khatun', 3),
(5, 'accountant', 'accountant@kistibank.com', '+8801711000005', '$2y$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Hasan Mahmud', 4),
(6, 'auditor', 'auditor@kistibank.com', '+8801711000006', '$2y$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Nusrat Jahan', 5);

-- Departments
INSERT INTO departments (id, code, name, area_name, description) VALUES
(1, 'DHA-01', 'ধানমন্ডি শাখা (Dhanmondi A)', 'Dhanmondi & Sobhanbagh', 'Primary micro-credit zone'),
(2, 'MIR-02', 'মিরপুর শাখা (Mirpur B)', 'Mirpur 1, 2, 10', 'Retail cottage traders loan branch'),
(3, 'UTT-03', 'উত্তরা শাখা (Uttara C)', 'Sectors 3, 7, 11', 'Micro-enterprise lending'),
(4, 'GAZ-04', 'গাজীপুর পল্লী এরিয়া', 'Joydebpur & Konabari', 'Agro-farming community kisti'),
(5, 'SAV-05', 'সাভার শিল্প এরিয়া', 'Ashulia & Savar Bazar', 'Textile and artisan small loans');

-- Department Assignments
INSERT INTO collection_assignments (user_id, department_id) VALUES
(3, 1),
(3, 2),
(4, 3),
(4, 4);

-- Institution Settings
INSERT INTO institution_settings (setting_key, setting_value) VALUES
('institution_name', 'KistiBank Microfinance & Savings Ltd.'),
('institution_bengali_name', 'কিস্তিব্যাংক ক্ষুদ্রঋণ ও সঞ্চয় সমিতি'),
('currency_symbol', '৳'),
('currency_code', 'BDT'),
('address', 'Level 4, Shanchay Bhaban, Motijheel C/A, Dhaka-1000'),
('phone', '+880 1700-123456'),
('email', 'support@kistibank.com'),
('office_hours', 'Sat - Thu: 9:00 AM - 5:00 PM (Friday Closed)');
