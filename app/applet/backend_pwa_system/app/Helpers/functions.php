<?php
declare(strict_types=1);

require_once __DIR__ . '/../../config/database.php';

use Config\Database;

function db(): PDO {
    return Database::getConnection();
}

function user(): ?array {
    return $_SESSION['user'] ?? null;
}

function auth(): bool {
    return isset($_SESSION['user']['id']);
}

function hasRole(string ...$roles): bool {
    $currentUser = user();
    if (!$currentUser) return false;
    return in_array($currentUser['role'], $roles, true);
}

function csrf_token(): string {
    if (empty($_SESSION['csrf_token'])) {
        $_SESSION['csrf_token'] = bin2hex(random_bytes(32));
    }
    return $_SESSION['csrf_token'];
}

function verify_csrf(?string $token): bool {
    if (empty($token) || empty($_SESSION['csrf_token'])) {
        return false;
    }
    return hash_equals($_SESSION['csrf_token'], $token);
}

function sanitize_output(string $str): string {
    return htmlspecialchars($str, ENT_QUOTES | ENT_SUBSTITUTE, 'UTF-8');
}

function format_money(float $amount, string $symbol = '৳'): string {
    return $symbol . number_format($amount, 2);
}

function generate_member_id(int $count): string {
    return 'KB-M-' . str_pad((string)($count + 1), 4, '0', STR_PAD_LEFT);
}

function generate_transaction_id(): string {
    return 'TXN-' . date('Ymd') . '-' . strtoupper(bin2hex(random_bytes(3)));
}

function generate_loan_id(int $count): string {
    return 'LN-' . date('Y') . '-' . str_pad((string)($count + 1), 3, '0', STR_PAD_LEFT);
}

function generate_receipt_number(): string {
    return 'RCP-' . date('ymd') . '-' . mt_rand(1000, 9999);
}

function json_success(string $message, array $data = []): void {
    header('Content-Type: application/json; charset=utf-8');
    echo json_encode([
        'success' => true,
        'message' => $message,
        'data' => $data
    ]);
    exit;
}

function json_error(string $message, array $errors = [], int $statusCode = 400): void {
    http_response_code($statusCode);
    header('Content-Type: application/json; charset=utf-8');
    echo json_encode([
        'success' => false,
        'message' => $message,
        'errors' => $errors
    ]);
    exit;
}

function audit_log(string $action, string $resourceType, string $resourceId, string $details): void {
    try {
        $currentUser = user();
        $stmt = db()->prepare(
            "INSERT INTO audit_logs (user_id, user_name, user_role, action, resource_type, resource_id, ip_address, user_agent, details) 
             VALUES (:uid, :uname, :urole, :action, :rtype, :rid, :ip, :ua, :details)"
        );
        $stmt->execute([
            ':uid'     => $currentUser['id'] ?? null,
            ':uname'   => $currentUser['full_name'] ?? 'System / Anonymous',
            ':urole'   => $currentUser['role'] ?? 'Guest',
            ':action'  => $action,
            ':rtype'   => $resourceType,
            ':rid'     => $resourceId,
            ':ip'      => $_SERVER['REMOTE_ADDR'] ?? '127.0.0.1',
            ':ua'      => substr($_SERVER['HTTP_USER_AGENT'] ?? 'Unknown', 0, 255),
            ':details' => $details
        ]);
    } catch (\Throwable $e) {
        error_log("Audit log failed: " . $e->getMessage());
    }
}
