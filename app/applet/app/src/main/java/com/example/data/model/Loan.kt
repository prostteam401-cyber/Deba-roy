package com.example.data.model

enum class LoanFrequency(val label: String, val bengaliLabel: String) {
    WEEKLY("Weekly", "সাপ্তাহিক"),
    MONTHLY("Monthly", "মাসিক")
}

enum class LoanStatus(val label: String, val bengaliLabel: String) {
    PENDING("Pending", "অনুমোদনের অপেক্ষায়"),
    APPROVED("Approved", "অনুমোদিত"),
    ACTIVE("Active", "চলমান"),
    COMPLETED("Completed", "পরিশোধিত"),
    DEFAULTED("Defaulted", "খেলাপী"),
    CANCELLED("Cancelled", "বাতিল")
}

enum class InstallmentStatus(val label: String, val bengaliLabel: String) {
    PAID("Paid", "পরিশোধিত"),
    UNPAID("Unpaid", "বকেয়া"),
    LATE("Late", "বিলম্বিত"),
    OVERDUE("Overdue", "খেলাপী"),
    PARTIAL("Partial", "আংশিক")
}

data class Installment(
    val id: String,
    val loanId: String,
    val installmentNo: Int,
    val dueDate: String,
    val dueAmount: Double,
    val paidAmount: Double = 0.0,
    val principalComponent: Double = 0.0,
    val interestComponent: Double = 0.0,
    val remainingBalanceAfter: Double = 0.0,
    val status: InstallmentStatus = InstallmentStatus.UNPAID,
    val paidDate: String = "",
    val collectorName: String = "",
    val receiptId: String = ""
)

data class Loan(
    val id: String,
    val loanCode: String, // e.g. LN-2026-001
    val memberId: String,
    val memberName: String,
    val departmentId: String,
    val principalAmount: Double,
    val interestRatePercent: Double, // e.g. 10.0%
    val totalPayable: Double,
    val totalInstallments: Int,
    val frequency: LoanFrequency,
    val installmentAmount: Double,
    val disbursedDate: String,
    val firstInstallmentDate: String,
    val maturityDate: String,
    val remainingBalance: Double,
    val paidPrincipal: Double = 0.0,
    val paidInterest: Double = 0.0,
    val approvedBy: String = "Manager",
    val disbursedBy: String = "Accountant",
    val status: LoanStatus = LoanStatus.ACTIVE,
    val installments: List<Installment> = emptyList()
)
