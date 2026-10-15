package com.example.data.model

data class TransactionRecord(
    val id: String,
    val transactionCode: String, // e.g. TXN-20261014-001
    val receiptNumber: String, // e.g. RCP-8821
    val memberId: String,
    val memberName: String,
    val departmentId: String,
    val loanId: String = "",
    val installmentNo: Int = 0,
    val employeeId: String,
    val employeeName: String,
    val date: String,
    val time: String,
    // Allocation breakdown
    val loanKistiAmount: Double = 0.0,
    val principalApplied: Double = 0.0,
    val interestApplied: Double = 0.0,
    val savingsDeposit: Double = 0.0,
    val dpsDeposit: Double = 0.0,
    val totalCollected: Double = 0.0,
    // Running balance snapshots for audit trail
    val previousLoanBalance: Double = 0.0,
    val newLoanBalance: Double = 0.0,
    val previousSavingsBalance: Double = 0.0,
    val newSavingsBalance: Double = 0.0,
    val previousDpsBalance: Double = 0.0,
    val newDpsBalance: Double = 0.0,
    val paymentMethod: String = "CASH",
    val digitalSignature: String = "Verified Digitally by Collector",
    val isReversed: Boolean = false,
    val reversalReason: String = "",
    val idempotencyKey: String = ""
)

data class AuditLog(
    val id: String,
    val timestamp: String,
    val userId: String,
    val userName: String,
    val userRole: String,
    val action: String, // e.g. "COLLECTION_RECORDED", "KYC_VIEWED", "LOAN_APPROVED", "REVERSAL_PROCESSED"
    val resource: String, // e.g. "MEMBER", "LOAN", "TRANSACTION", "DEPARTMENT"
    val resourceId: String,
    val details: String,
    val ipAddress: String = "192.168.1.45",
    val status: String = "SUCCESS"
)
