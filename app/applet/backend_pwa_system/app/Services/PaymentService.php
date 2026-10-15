<?php
declare(strict_types=1);

namespace App\Services;

use PDO;
use Exception;

require_once __DIR__ . '/../Helpers/functions.php';

class PaymentService {

    public function recordCollection(array $payload): array {
        $pdo = db();

        $memberId = (int)($payload['member_id'] ?? 0);
        $kistiAmount = (float)($payload['kisti_amount'] ?? 0.0);
        $savingsDeposit = (float)($payload['savings_deposit'] ?? 0.0);
        $dpsDeposit = (float)($payload['dps_deposit'] ?? 0.0);
        $paymentMethod = (string)($payload['payment_method'] ?? 'CASH');
        $signature = (string)($payload['signature'] ?? 'Digital Verified');
        $idempotencyKey = (string)($payload['idempotency_key'] ?? '');

        $totalCollected = $kistiAmount + $savingsDeposit + $dpsDeposit;
        if ($totalCollected <= 0.0) {
            throw new Exception("Collection amount must be greater than zero.");
        }

        try {
            $pdo->beginTransaction();

            // Idempotency check
            if (!empty($idempotencyKey)) {
                $chk = $pdo->prepare("SELECT id FROM payments WHERE idempotency_key = :key FOR UPDATE");
                $chk->execute([':key' => $idempotencyKey]);
                if ($chk->fetch()) {
                    throw new Exception("Duplicate payment request detected (Idempotency Token already processed).");
                }
            }

            // Lock Member Record
            $mStmt = $pdo->prepare("SELECT * FROM members WHERE id = :id FOR UPDATE");
            $mStmt->execute([':id' => $memberId]);
            $member = $mStmt->fetch();
            if (!$member) {
                throw new Exception("Member not found.");
            }

            // Find Active Loan
            $lStmt = $pdo->prepare("SELECT * FROM loans WHERE member_id = :mid AND status = 'ACTIVE' LIMIT 1 FOR UPDATE");
            $lStmt->execute([':mid' => $memberId]);
            $loan = $lStmt->fetch();

            $prevLoanBal = $loan ? (float)$loan['remaining_balance'] : 0.0;
            $newLoanBal = $prevLoanBal;
            $principalApplied = 0.0;
            $interestApplied = 0.0;
            $instNo = 0;

            if ($loan && $kistiAmount > 0.0) {
                $iStmt = $pdo->prepare("SELECT * FROM loan_installments WHERE loan_id = :lid AND status != 'PAID' ORDER BY installment_no ASC LIMIT 1 FOR UPDATE");
                $iStmt->execute([':lid' => $loan['id']]);
                $inst = $iStmt->fetch();

                if ($inst) {
                    $instNo = (int)$inst['installment_no'];
                    $due = (float)$inst['due_amount'];
                    $isPaid = $kistiAmount >= $due;

                    $uInst = $pdo->prepare("UPDATE loan_installments SET paid_amount = paid_amount + :paid, status = :status, paid_date = CURDATE(), paid_at = NOW() WHERE id = :id");
                    $uInst->execute([
                        ':paid'   => $kistiAmount,
                        ':status' => $isPaid ? 'PAID' : 'PARTIAL',
                        ':id'     => $inst['id']
                    ]);
                }

                $newLoanBal = max(0.0, $prevLoanBal - $kistiAmount);
                $ratio = (float)$loan['principal_amount'] / (float)$loan['total_payable'];
                $principalApplied = round($kistiAmount * $ratio, 2);
                $interestApplied = $kistiAmount - $principalApplied;

                $uLoan = $pdo->prepare("UPDATE loans SET remaining_balance = :bal, paid_principal = paid_principal + :p, paid_interest = paid_interest + :i, status = IF(:bal <= 0, 'COMPLETED', 'ACTIVE') WHERE id = :id");
                $uLoan->execute([
                    ':bal' => $newLoanBal,
                    ':p'   => $principalApplied,
                    ':i'   => $interestApplied,
                    ':id'  => $loan['id']
                ]);
            }

            // Update Savings
            $sStmt = $pdo->prepare("SELECT * FROM savings_accounts WHERE member_id = :mid FOR UPDATE");
            $sStmt->execute([':mid' => $memberId]);
            $savings = $sStmt->fetch();
            $prevSavingsBal = $savings ? (float)$savings['balance'] : 0.0;
            $newSavingsBal = $prevSavingsBal + $savingsDeposit;

            if ($savings && $savingsDeposit > 0.0) {
                $uSav = $pdo->prepare("UPDATE savings_accounts SET balance = balance + :dep, total_deposited = total_deposited + :dep WHERE id = :id");
                $uSav->execute([':dep' => $savingsDeposit, ':id' => $savings['id']]);
            }

            // Update DPS
            $dStmt = $pdo->prepare("SELECT * FROM dps_accounts WHERE member_id = :mid AND status = 'ACTIVE' LIMIT 1 FOR UPDATE");
            $dStmt->execute([':mid' => $memberId]);
            $dps = $dStmt->fetch();
            $prevDpsBal = $dps ? (float)$dps['total_deposited'] : 0.0;
            $newDpsBal = $prevDpsBal + $dpsDeposit;

            if ($dps && $dpsDeposit > 0.0) {
                $uDps = $pdo->prepare("UPDATE dps_accounts SET total_deposited = total_deposited + :dep, total_paid_months = total_paid_months + 1 WHERE id = :id");
                $uDps->execute([':dep' => $dpsDeposit, ':id' => $dps['id']]);
            }

            // Insert Immutable Payment Record
            $txnCode = generate_transaction_id();
            $receiptNo = generate_receipt_number();
            $currentUser = user();

            $pStmt = $pdo->prepare(
                "INSERT INTO payments (
                    transaction_code, receipt_number, member_id, department_id, collector_id,
                    loan_id, installment_no, loan_kisti_amount, principal_applied, interest_applied,
                    savings_deposit, dps_deposit, total_collected,
                    previous_loan_balance, new_loan_balance,
                    previous_savings_balance, new_savings_balance,
                    previous_dps_balance, new_dps_balance,
                    payment_method, digital_signature, idempotency_key
                ) VALUES (
                    :txn, :rcp, :mid, :dept, :col,
                    :lid, :inst, :kisti, :pr, :intr,
                    :sav, :dps, :tot,
                    :plb, :nlb,
                    :psb, :nsb,
                    :pdb, :ndb,
                    :method, :sig, :ik
                )"
            );

            $pStmt->execute([
                ':txn'    => $txnCode,
                ':rcp'    => $receiptNo,
                ':mid'    => $memberId,
                ':dept'   => $member['department_id'],
                ':col'    => $currentUser['id'] ?? 1,
                ':lid'    => $loan['id'] ?? null,
                ':inst'   => $instNo,
                ':kisti'  => $kistiAmount,
                ':pr'     => $principalApplied,
                ':intr'   => $interestApplied,
                ':sav'    => $savingsDeposit,
                ':dps'    => $dpsDeposit,
                ':tot'    => $totalCollected,
                ':plb'    => $prevLoanBal,
                ':nlb'    => $newLoanBal,
                ':psb'    => $prevSavingsBal,
                ':nsb'    => $newSavingsBal,
                ':pdb'    => $prevDpsBal,
                ':ndb'    => $newDpsBal,
                ':method' => $paymentMethod,
                ':sig'    => $signature,
                ':ik'     => $idempotencyKey ?: null
            ]);

            audit_log('COLLECTION_COMMITTED', 'PAYMENT', $txnCode, "Collected ৳{$totalCollected} from Member {$member['member_code']}");

            $pdo->commit();

            return [
                'transaction_code' => $txnCode,
                'receipt_number'   => $receiptNo,
                'total_collected'  => $totalCollected,
                'new_loan_balance' => $newLoanBal,
                'new_savings_balance' => $newSavingsBal,
                'new_dps_balance' => $newDpsBal
            ];

        } catch (\Throwable $e) {
            $pdo->rollBack();
            throw $e;
        }
    }
}
