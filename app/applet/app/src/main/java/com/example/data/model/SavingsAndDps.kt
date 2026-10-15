package com.example.data.model

data class SavingsAccount(
    val id: String,
    val accountNo: String, // e.g. SB-101
    val memberId: String,
    val memberName: String,
    val balance: Double = 0.0,
    val totalDeposited: Double = 0.0,
    val totalWithdrawn: Double = 0.0,
    val interestEarned: Double = 0.0,
    val openedDate: String = "2026-01-10",
    val status: String = "ACTIVE"
)

data class DpsAccount(
    val id: String,
    val dpsCode: String, // e.g. DPS-2026-05
    val memberId: String,
    val memberName: String,
    val monthlyInstallment: Double, // e.g. 1000.0
    val termMonths: Int, // e.g. 36 months
    val totalPaidMonths: Int = 0,
    val totalDeposited: Double = 0.0,
    val expectedMaturityAmount: Double = 0.0,
    val startDate: String = "2026-01-01",
    val maturityDate: String = "2029-01-01",
    val status: String = "ACTIVE" // ACTIVE, MATURED, CLOSED
)
