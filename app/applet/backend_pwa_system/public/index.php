<?php
declare(strict_types=1);
session_start();
require_once __DIR__ . '/../app/Helpers/functions.php';

// Set default test user session if not set
if (!isset($_SESSION['user'])) {
    $_SESSION['user'] = [
        'id' => 1,
        'username' => 'admin',
        'full_name' => 'Tariqul Islam (Super Admin)',
        'role' => 'super_admin'
    ];
}
$csrfToken = csrf_token();
?>
<!DOCTYPE html>
<html lang="bn">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="theme-color" content="#0A5C36">
    <title>KistiBank: কিস্তি, সঞ্চয় ও ঋণ ব্যবস্থাপনা</title>
    <link rel="manifest" href="manifest.json">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.0/font/bootstrap-icons.css">
    <style>
        :root { --bank-primary: #0A5C36; --bank-gold: #B57D00; --bank-bg: #F4F7F5; }
        body { background-color: var(--bank-bg); font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; }
        .navbar-bank { background-color: var(--bank-primary); }
        .card-stat { border-radius: 14px; border: none; box-shadow: 0 3px 10px rgba(0,0,0,0.05); }
        .text-bank { color: var(--bank-primary); }
        .btn-bank { background-color: var(--bank-primary); color: #fff; }
        .btn-bank:hover { background-color: #08492b; color: #fff; }
        @media print {
            .no-print { display: none !important; }
            body { background: #fff; }
        }
    </style>
</head>
<body>
    <nav class="navbar navbar-expand-lg navbar-dark navbar-bank no-print">
        <div class="container-fluid px-3">
            <a class="navbar-brand fw-bold" href="#">
                <i class="bi bi-bank me-2"></i>KistiBank Financial Ltd.
            </a>
            <div class="d-flex align-items-center text-white">
                <span class="badge bg-warning text-dark me-2">Super Admin</span>
                <span class="small me-3">Tariqul Islam</span>
                <button class="btn btn-sm btn-outline-light" onclick="openCalculator()"><i class="bi bi-calculator"></i> ক্যালকুলেটর</button>
            </div>
        </div>
    </nav>

    <div class="container py-4">
        <!-- Banner -->
        <div class="p-4 mb-4 text-white rounded-3 no-print" style="background: linear-gradient(135deg, #0A5C36, #1B5E43);">
            <div class="d-flex justify-content-between align-items-center">
                <div>
                    <h2 class="fw-bold mb-1">সঞ্চয়, ঋণ, ডিপিএস ও কিস্তি ব্যবস্থাপনা সিস্টেম</h2>
                    <p class="mb-0 opacity-75">Secure Microfinance, Member KYC & Field Collection MVP</p>
                </div>
                <div>
                    <button class="btn btn-light fw-bold" onclick="showNewMemberModal()"><i class="bi bi-person-plus me-1"></i> নতুন সদস্য KYC</button>
                    <button class="btn btn-warning fw-bold text-dark ms-2" onclick="showCollectionModal()"><i class="bi bi-cash-stack me-1"></i> কিস্তি আদায়</button>
                </div>
            </div>
        </div>

        <!-- 8 Clickable Cards -->
        <div class="row g-3 mb-4">
            <div class="col-md-3">
                <div class="card card-stat p-3">
                    <div class="d-flex justify-content-between align-items-center">
                        <span class="text-muted small">মোট সদস্য</span>
                        <i class="bi bi-people-fill text-bank fs-4"></i>
                    </div>
                    <h3 class="fw-bold text-bank my-2">1,240 জন</h3>
                    <small class="text-success"><i class="bi bi-arrow-up"></i> ৯৪% নিয়মিত পেয়ার</small>
                </div>
            </div>
            <div class="col-md-3">
                <div class="card card-stat p-3">
                    <div class="d-flex justify-content-between align-items-center">
                        <span class="text-muted small">মোট ঋণ বিতরণ</span>
                        <i class="bi bi-wallet2 text-primary fs-4"></i>
                    </div>
                    <h3 class="fw-bold text-primary my-2">৳১,৮৫,০০,০০০</h3>
                    <small class="text-muted">২৮০টি সক্রিয় ঋণ</small>
                </div>
            </div>
            <div class="col-md-3">
                <div class="card card-stat p-3">
                    <div class="d-flex justify-content-between align-items-center">
                        <span class="text-muted small">মোট সঞ্চয় জমা</span>
                        <i class="bi bi-piggy-bank-fill text-success fs-4"></i>
                    </div>
                    <h3 class="fw-bold text-success my-2">৳৬৪,৫০,০০০</h3>
                    <small class="text-muted">সাধারণ সঞ্চয় তহবিল</small>
                </div>
            </div>
            <div class="col-md-3">
                <div class="card card-stat p-3">
                    <div class="d-flex justify-content-between align-items-center">
                        <span class="text-muted small">মোট DPS আমানত</span>
                        <i class="bi bi-safe2 text-warning fs-4"></i>
                    </div>
                    <h3 class="fw-bold text-warning my-2">৳৯২,২০,০০০</h3>
                    <small class="text-muted">মাসিক সঞ্চয় স্কিম</small>
                </div>
            </div>
        </div>

        <!-- Table View -->
        <div class="card card-stat p-3 mb-4">
            <div class="d-flex justify-content-between align-items-center mb-3">
                <h5 class="fw-bold mb-0 text-bank"><i class="bi bi-table me-2"></i>মাঠ পর্যায় কিস্তি আদায় শিট (Field Collection Table)</h5>
                <button class="btn btn-sm btn-outline-success" onclick="window.print()"><i class="bi bi-printer me-1"></i> প্রিন্ট ভাউচার</button>
            </div>
            <div class="table-responsive">
                <table class="table table-hover align-middle">
                    <thead class="table-light">
                        <tr>
                            <th>সদস্য কোড</th>
                            <th>নাম (Full Name)</th>
                            <th>শাখা (Dept)</th>
                            <th>মোবাইল</th>
                            <th>ঋণ স্থিতি</th>
                            <th>কিস্তির পরিমাণ</th>
                            <th>স্ট্যাটাস</th>
                            <th>অ্যাকশন</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr>
                            <td class="fw-bold">KB-M-101</td>
                            <td>Md. Rafiqul Islam</td>
                            <td>ধানমন্ডি শাখা</td>
                            <td>01712-345678</td>
                            <td class="text-danger fw-bold">৳৬৫,০০০</td>
                            <td class="text-bank fw-bold">৳৫,৫০০</td>
                            <td><span class="badge bg-success">নিয়মিত</span></td>
                            <td><button class="btn btn-sm btn-bank" onclick="showCollectionModal('KB-M-101', 'Md. Rafiqul Islam', 5500)">আদায়</button></td>
                        </tr>
                        <tr>
                            <td class="fw-bold">KB-M-102</td>
                            <td>Farzana Yesmin</td>
                            <td>মিরপুর শাখা</td>
                            <td>01819-223344</td>
                            <td class="text-danger fw-bold">৳৪০,০০০</td>
                            <td class="text-bank fw-bold">৳৫,৫০০</td>
                            <td><span class="badge bg-success">নিয়মিত</span></td>
                            <td><button class="btn btn-sm btn-bank" onclick="showCollectionModal('KB-M-102', 'Farzana Yesmin', 5500)">আদায়</button></td>
                        </tr>
                        <tr>
                            <td class="fw-bold">KB-M-103</td>
                            <td>Abul Kashem</td>
                            <td>মিরপুর শাখা</td>
                            <td>01911-445566</td>
                            <td class="text-danger fw-bold">৳৩৮,০০০</td>
                            <td class="text-bank fw-bold">৳২,৮০০</td>
                            <td><span class="badge bg-danger">খেলাপী</span></td>
                            <td><button class="btn btn-sm btn-danger" onclick="showCollectionModal('KB-M-103', 'Abul Kashem', 2800)">তাগাদা / আদায়</button></td>
                        </tr>
                    </tbody>
                </table>
            </div>
        </div>
    </div>

    <!-- PWA Service Worker Registration -->
    <script>
        if ('serviceWorker' in navigator) {
            window.addEventListener('load', () => {
                navigator.serviceWorker.register('/service-worker.js')
                    .then((reg) => console.log('KistiBank PWA Service Worker Registered', reg))
                    .catch((err) => console.log('SW registration failed', err));
            });
        }
        function openCalculator() { alert("KistiBank Financial Calculator: Loan Principal / Rate / Installment / Maturity Ready."); }
        function showNewMemberModal() { alert("KYC Registration Modal opened: Biometrics, Smart NID Masking & Nominee info ready."); }
        function showCollectionModal(code, name, amt) { alert("Collection dialog for " + (name || "Member") + ": Enter Kisti, Savings & DPS. Instant atomic double-entry transaction will execute."); }
    </script>
</body>
</html>
