package com.example.data.model

enum class MemberStatus(val label: String, val bengaliLabel: String) {
    ACTIVE("Active", "সক্রিয়"),
    INACTIVE("Inactive", "নিষ্ক্রিয়"),
    PENDING_APPROVAL("Pending Approval", "অনুমোদনের অপেক্ষায়"),
    BLOCKED("Blocked", "স্থগিত")
}

enum class PayerCategory(val label: String, val bengaliLabel: String) {
    REGULAR("Regular Payer", "নিয়মিত কিস্তি প্রদানকারী"),
    LATE("Late Payer", "বিলম্বিত পরিশোধকারী"),
    OVERDUE("Overdue", "খেলাপী/বকেয়া"),
    COMPLETED("Completed", "ঋণ পরিশোধিত"),
    NO_LOAN("No Active Loan", "কোন ঋণ নেই")
}

data class KycData(
    val docType: String = "National ID (NID)",
    val docNumber: String = "",
    val isVerified: Boolean = true,
    val verifiedBy: String = "Manager",
    val verifiedDate: String = "2026-01-10",
    val remarks: String = "Biometric & Smart NID verified"
) {
    val maskedDocNumber: String
        get() {
            if (docNumber.length <= 4) return docNumber
            val visible = docNumber.takeLast(4)
            val prefix = docNumber.take(3)
            return "$prefix **** **** $visible"
        }
}

data class Nominee(
    val name: String = "",
    val relationship: String = "",
    val mobile: String = "",
    val nid: String = "",
    val address: String = ""
)

data class Member(
    val id: String,
    val memberCode: String, // e.g. KB-M-101
    val fullName: String,
    val fatherName: String,
    val motherName: String,
    val dateOfBirth: String,
    val gender: String,
    val bloodGroup: String,
    val occupation: String,
    val maritalStatus: String,
    val education: String,
    val primaryMobile: String,
    val secondaryMobile: String = "",
    val email: String = "",
    val presentAddress: String,
    val permanentAddress: String,
    val departmentId: String,
    val departmentName: String,
    val kyc: KycData,
    val nominee: Nominee,
    val status: MemberStatus = MemberStatus.ACTIVE,
    val payerCategory: PayerCategory = PayerCategory.REGULAR,
    val registrationDate: String = "2026-01-15",
    val avatarInitials: String = "",
    // Financial summaries (derived from transactions/accounts)
    val totalLoanDisbursed: Double = 0.0,
    val loanBalance: Double = 0.0,
    val totalSavings: Double = 0.0,
    val totalDps: Double = 0.0,
    val totalInstallments: Int = 0,
    val paidInstallments: Int = 0,
    val remainingInstallments: Int = 0,
    val overdueInstallments: Int = 0,
    val nextInstallmentDue: String = "",
    val nextInstallmentAmount: Double = 0.0,
    val lastPaymentDate: String = ""
)
