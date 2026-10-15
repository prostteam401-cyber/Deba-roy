package com.example.data.repository

import com.example.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class KistiBankRepository {

    // Current logged-in user
    private val _currentUser = MutableStateFlow(SeedData.defaultUsers[0])
    val currentUser: StateFlow<User> = _currentUser.asStateFlow()

    private val _members = MutableStateFlow(SeedData.initialMembers)
    val members: StateFlow<List<Member>> = _members.asStateFlow()

    private val _loans = MutableStateFlow(SeedData.initialLoans)
    val loans: StateFlow<List<Loan>> = _loans.asStateFlow()

    private val _departments = MutableStateFlow(SeedData.initialDepartments)
    val departments: StateFlow<List<Department>> = _departments.asStateFlow()

    private val _savingsAccounts = MutableStateFlow(SeedData.initialSavings)
    val savingsAccounts: StateFlow<List<SavingsAccount>> = _savingsAccounts.asStateFlow()

    private val _dpsAccounts = MutableStateFlow(SeedData.initialDps)
    val dpsAccounts: StateFlow<List<DpsAccount>> = _dpsAccounts.asStateFlow()

    private val _transactions = MutableStateFlow(SeedData.initialTransactions)
    val transactions: StateFlow<List<TransactionRecord>> = _transactions.asStateFlow()

    private val _auditLogs = MutableStateFlow(SeedData.initialAuditLogs)
    val auditLogs: StateFlow<List<AuditLog>> = _auditLogs.asStateFlow()

    private val _notices = MutableStateFlow(SeedData.initialNotices)
    val notices: StateFlow<List<Notice>> = _notices.asStateFlow()

    private val _teamMembers = MutableStateFlow(SeedData.teamMembers)
    val teamMembers: StateFlow<List<TeamMember>> = _teamMembers.asStateFlow()

    private val _services = MutableStateFlow(SeedData.bankServices)
    val services: StateFlow<List<BankService>> = _services.asStateFlow()

    private val _settings = MutableStateFlow(InstitutionSettings())
    val settings: StateFlow<InstitutionSettings> = _settings.asStateFlow()

    val availableUsers: List<User> = SeedData.defaultUsers

    private fun getCurrentDate(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    private fun getCurrentTime(): String {
        return SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
    }

    fun switchUser(user: User) {
        _currentUser.value = user
        logAudit(
            action = "USER_LOGIN_SWITCH",
            resource = "USER",
            resourceId = user.id,
            details = "Logged in as ${user.fullName} (${user.role.displayName})"
        )
    }

    fun logAudit(action: String, resource: String, resourceId: String, details: String) {
        val user = _currentUser.value
        val log = AuditLog(
            id = UUID.randomUUID().toString(),
            timestamp = "${getCurrentDate()} ${getCurrentTime()}",
            userId = user.id,
            userName = user.fullName,
            userRole = user.role.displayName,
            action = action,
            resource = resource,
            resourceId = resourceId,
            details = details,
            ipAddress = "192.168.1.100",
            status = "SUCCESS"
        )
        _auditLogs.value = listOf(log) + _auditLogs.value
    }

    // Role-based department check
    fun canAccessDepartment(deptId: String): Boolean {
        val user = _currentUser.value
        if (user.role == UserRole.SUPER_ADMIN || user.role == UserRole.MANAGER || 
            user.role == UserRole.ACCOUNTANT || user.role == UserRole.AUDITOR) {
            return true
        }
        return user.assignedDepartmentIds.contains(deptId)
    }

    // Member Registration
    fun registerMember(
        fullName: String,
        fatherName: String,
        motherName: String,
        dob: String,
        gender: String,
        bloodGroup: String,
        occupation: String,
        maritalStatus: String,
        education: String,
        primaryMobile: String,
        secondaryMobile: String,
        email: String,
        presentAddress: String,
        permanentAddress: String,
        departmentId: String,
        docType: String,
        docNumber: String,
        nomineeName: String,
        nomineeRel: String,
        nomineeMobile: String,
        nomineeAddress: String
    ): Result<Member> {
        val dept = _departments.value.find { it.id == departmentId }
            ?: return Result.failure(Exception("Invalid Department ID"))

        val newCode = "KB-M-${100 + _members.value.size + 1}"
        val memberId = UUID.randomUUID().toString()

        val kyc = KycData(
            docType = docType,
            docNumber = docNumber,
            isVerified = true,
            verifiedBy = _currentUser.value.fullName,
            verifiedDate = getCurrentDate()
        )

        val nominee = Nominee(
            name = nomineeName,
            relationship = nomineeRel,
            mobile = nomineeMobile,
            address = nomineeAddress
        )

        val newMember = Member(
            id = memberId,
            memberCode = newCode,
            fullName = fullName,
            fatherName = fatherName,
            motherName = motherName,
            dateOfBirth = dob,
            gender = gender,
            bloodGroup = bloodGroup,
            occupation = occupation,
            maritalStatus = maritalStatus,
            education = education,
            primaryMobile = primaryMobile,
            secondaryMobile = secondaryMobile,
            email = email,
            presentAddress = presentAddress,
            permanentAddress = permanentAddress,
            departmentId = dept.id,
            departmentName = dept.name,
            kyc = kyc,
            nominee = nominee,
            status = MemberStatus.ACTIVE,
            payerCategory = PayerCategory.NO_LOAN,
            registrationDate = getCurrentDate(),
            avatarInitials = fullName.split(" ").take(2).mapNotNull { it.firstOrNull()?.toString() }.joinToString("")
        )

        _members.value = listOf(newMember) + _members.value

        // Automatically create savings account
        val newSavings = SavingsAccount(
            id = UUID.randomUUID().toString(),
            accountNo = "SB-${100 + _savingsAccounts.value.size + 1}",
            memberId = memberId,
            memberName = fullName,
            balance = 0.0,
            openedDate = getCurrentDate()
        )
        _savingsAccounts.value = listOf(newSavings) + _savingsAccounts.value

        logAudit(
            action = "MEMBER_REGISTERED",
            resource = "MEMBER",
            resourceId = newCode,
            details = "New member $fullName registered under department ${dept.name}"
        )

        return Result.success(newMember)
    }

    // Loan Creation & Schedule Generation
    fun createLoan(
        memberId: String,
        principalAmount: Double,
        interestRatePercent: Double,
        frequency: LoanFrequency,
        totalInstallments: Int,
        firstInstallmentDate: String
    ): Result<Loan> {
        val member = _members.value.find { it.id == memberId }
            ?: return Result.failure(Exception("Member not found"))

        val totalInterest = principalAmount * (interestRatePercent / 100.0)
        val totalPayable = principalAmount + totalInterest
        val installmentAmount = Math.round((totalPayable / totalInstallments) * 100.0) / 100.0

        val loanId = UUID.randomUUID().toString()
        val loanCode = "LN-${202600 + _loans.value.size + 1}"

        // Generate Installment Schedule
        val schedule = mutableListOf<Installment>()
        var runningBal = totalPayable
        for (i in 1..totalInstallments) {
            val due = if (i == totalInstallments) runningBal else installmentAmount
            runningBal -= due
            val principalPart = due * (principalAmount / totalPayable)
            val interestPart = due - principalPart

            schedule.add(
                Installment(
                    id = UUID.randomUUID().toString(),
                    loanId = loanId,
                    installmentNo = i,
                    dueDate = calculateNextDueDate(firstInstallmentDate, i - 1, frequency),
                    dueAmount = due,
                    paidAmount = 0.0,
                    principalComponent = Math.round(principalPart * 100.0) / 100.0,
                    interestComponent = Math.round(interestPart * 100.0) / 100.0,
                    remainingBalanceAfter = Math.max(0.0, Math.round(runningBal * 100.0) / 100.0),
                    status = InstallmentStatus.UNPAID
                )
            )
        }

        val newLoan = Loan(
            id = loanId,
            loanCode = loanCode,
            memberId = member.id,
            memberName = member.fullName,
            departmentId = member.departmentId,
            principalAmount = principalAmount,
            interestRatePercent = interestRatePercent,
            totalPayable = totalPayable,
            totalInstallments = totalInstallments,
            frequency = frequency,
            installmentAmount = installmentAmount,
            disbursedDate = getCurrentDate(),
            firstInstallmentDate = firstInstallmentDate,
            maturityDate = schedule.lastOrNull()?.dueDate ?: firstInstallmentDate,
            remainingBalance = totalPayable,
            approvedBy = _currentUser.value.fullName,
            disbursedBy = "Disbursement Desk",
            status = LoanStatus.ACTIVE,
            installments = schedule
        )

        _loans.value = listOf(newLoan) + _loans.value

        // Update member's summary
        updateMemberFinancialSummary(memberId)

        logAudit(
            action = "LOAN_CREATED",
            resource = "LOAN",
            resourceId = loanCode,
            details = "Loan of ৳$principalAmount created for ${member.fullName}"
        )

        return Result.success(newLoan)
    }

    private fun calculateNextDueDate(baseDate: String, offset: Int, freq: LoanFrequency): String {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = sdf.parse(baseDate) ?: Date()
            val cal = java.util.Calendar.getInstance()
            cal.time = date
            if (freq == LoanFrequency.WEEKLY) {
                cal.add(java.util.Calendar.DAY_OF_YEAR, offset * 7)
            } else {
                cal.add(java.util.Calendar.MONTH, offset)
            }
            sdf.format(cal.time)
        } catch (e: Exception) {
            baseDate
        }
    }

    // Atomic Collection & Financial Ledger
    fun recordCollection(
        memberId: String,
        loanKistiAmount: Double,
        savingsDeposit: Double,
        dpsDeposit: Double,
        paymentMethod: String = "CASH",
        collectorSignature: String = "Digital Signed by Collector",
        idempotencyKey: String = UUID.randomUUID().toString()
    ): Result<TransactionRecord> {
        val member = _members.value.find { it.id == memberId }
            ?: return Result.failure(Exception("Member not found"))

        // Duplicate payment protection
        if (_transactions.value.any { it.idempotencyKey == idempotencyKey && it.idempotencyKey.isNotBlank() }) {
            return Result.failure(Exception("Duplicate payment detected (Idempotency Key already processed)"))
        }

        val activeLoan = _loans.value.find { it.memberId == memberId && it.status == LoanStatus.ACTIVE }
        val savingsAcc = _savingsAccounts.value.find { it.memberId == memberId }
        val dpsAcc = _dpsAccounts.value.find { it.memberId == memberId && it.status == "ACTIVE" }

        val prevLoanBal = activeLoan?.remainingBalance ?: 0.0
        val prevSavingsBal = savingsAcc?.balance ?: 0.0
        val prevDpsBal = dpsAcc?.totalDeposited ?: 0.0

        // Strict Server-side math validation
        val totalCollected = loanKistiAmount + savingsDeposit + dpsDeposit
        if (totalCollected <= 0.0) {
            return Result.failure(Exception("Payment amount must be greater than zero"))
        }

        var newLoanBal = prevLoanBal
        var principalPart = 0.0
        var interestPart = 0.0
        var updatedInstallmentNo = 0

        // Process Loan Installment
        if (activeLoan != null && loanKistiAmount > 0) {
            val currentSchedule = activeLoan.installments.toMutableList()
            val nextUnpaidIdx = currentSchedule.indexOfFirst { it.status != InstallmentStatus.PAID }
            if (nextUnpaidIdx != -1) {
                val inst = currentSchedule[nextUnpaidIdx]
                updatedInstallmentNo = inst.installmentNo
                val isFullyPaid = loanKistiAmount >= inst.dueAmount
                val updatedInst = inst.copy(
                    paidAmount = inst.paidAmount + loanKistiAmount,
                    status = if (isFullyPaid) InstallmentStatus.PAID else InstallmentStatus.PARTIAL,
                    paidDate = getCurrentDate(),
                    collectorName = _currentUser.value.fullName,
                    receiptId = "RCP-${System.currentTimeMillis() % 100000}"
                )
                currentSchedule[nextUnpaidIdx] = updatedInst
            }
            newLoanBal = Math.max(0.0, prevLoanBal - loanKistiAmount)
            principalPart = loanKistiAmount * (activeLoan.principalAmount / activeLoan.totalPayable)
            interestPart = loanKistiAmount - principalPart

            val updatedLoan = activeLoan.copy(
                remainingBalance = newLoanBal,
                paidPrincipal = activeLoan.paidPrincipal + principalPart,
                paidInterest = activeLoan.paidInterest + interestPart,
                status = if (newLoanBal <= 0.0) LoanStatus.COMPLETED else LoanStatus.ACTIVE,
                installments = currentSchedule
            )
            _loans.value = _loans.value.map { if (it.id == activeLoan.id) updatedLoan else it }
        }

        // Process Savings Deposit
        var newSavingsBal = prevSavingsBal
        if (savingsDeposit > 0) {
            newSavingsBal = prevSavingsBal + savingsDeposit
            if (savingsAcc != null) {
                val updatedSavings = savingsAcc.copy(
                    balance = newSavingsBal,
                    totalDeposited = savingsAcc.totalDeposited + savingsDeposit
                )
                _savingsAccounts.value = _savingsAccounts.value.map { if (it.id == savingsAcc.id) updatedSavings else it }
            }
        }

        // Process DPS Deposit
        var newDpsBal = prevDpsBal
        if (dpsDeposit > 0 && dpsAcc != null) {
            newDpsBal = prevDpsBal + dpsDeposit
            val updatedDps = dpsAcc.copy(
                totalDeposited = newDpsBal,
                totalPaidMonths = dpsAcc.totalPaidMonths + 1
            )
            _dpsAccounts.value = _dpsAccounts.value.map { if (it.id == dpsAcc.id) updatedDps else it }
        }

        val txnId = UUID.randomUUID().toString()
        val txnCode = "TXN-${SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())}-${(1000 + _transactions.value.size + 1)}"
        val receiptNo = "RCP-${10000 + _transactions.value.size + 1}"

        val txnRecord = TransactionRecord(
            id = txnId,
            transactionCode = txnCode,
            receiptNumber = receiptNo,
            memberId = member.id,
            memberName = member.fullName,
            departmentId = member.departmentId,
            loanId = activeLoan?.id ?: "",
            installmentNo = updatedInstallmentNo,
            employeeId = _currentUser.value.id,
            employeeName = _currentUser.value.fullName,
            date = getCurrentDate(),
            time = getCurrentTime(),
            loanKistiAmount = loanKistiAmount,
            principalApplied = Math.round(principalPart * 100.0) / 100.0,
            interestApplied = Math.round(interestPart * 100.0) / 100.0,
            savingsDeposit = savingsDeposit,
            dpsDeposit = dpsDeposit,
            totalCollected = totalCollected,
            previousLoanBalance = prevLoanBal,
            newLoanBalance = newLoanBal,
            previousSavingsBalance = prevSavingsBal,
            newSavingsBalance = newSavingsBal,
            previousDpsBalance = prevDpsBal,
            newDpsBalance = newDpsBal,
            paymentMethod = paymentMethod,
            digitalSignature = collectorSignature,
            idempotencyKey = idempotencyKey
        )

        // Atomic commit to transaction ledger
        _transactions.value = listOf(txnRecord) + _transactions.value

        // Recompute member financial totals
        updateMemberFinancialSummary(memberId)

        logAudit(
            action = "COLLECTION_COMMITTED",
            resource = "TRANSACTION",
            resourceId = txnCode,
            details = "Collected ৳$totalCollected (Loan: ৳$loanKistiAmount, Sav: ৳$savingsDeposit, DPS: ৳$dpsDeposit) for ${member.fullName}"
        )

        return Result.success(txnRecord)
    }

    // Double-Entry Ledger Reversal
    fun reverseTransaction(transactionId: String, reason: String): Result<Boolean> {
        val user = _currentUser.value
        if (user.role != UserRole.SUPER_ADMIN && user.role != UserRole.MANAGER) {
            return Result.failure(Exception("Only Super Admin or Manager can authorize transaction reversals."))
        }

        val txn = _transactions.value.find { it.id == transactionId }
            ?: return Result.failure(Exception("Transaction not found"))

        if (txn.isReversed) {
            return Result.failure(Exception("Transaction is already reversed."))
        }

        // Mark as reversed
        val reversedTxn = txn.copy(
            isReversed = true,
            reversalReason = "$reason (Authorized by ${user.fullName})"
        )
        _transactions.value = _transactions.value.map { if (it.id == txn.id) reversedTxn else it }

        // Restore loan balance
        val loan = _loans.value.find { it.id == txn.loanId }
        if (loan != null && txn.loanKistiAmount > 0) {
            val restoredLoan = loan.copy(
                remainingBalance = loan.remainingBalance + txn.loanKistiAmount,
                paidPrincipal = Math.max(0.0, loan.paidPrincipal - txn.principalApplied),
                paidInterest = Math.max(0.0, loan.paidInterest - txn.interestApplied),
                status = LoanStatus.ACTIVE
            )
            _loans.value = _loans.value.map { if (it.id == loan.id) restoredLoan else it }
        }

        // Restore savings
        val savings = _savingsAccounts.value.find { it.memberId == txn.memberId }
        if (savings != null && txn.savingsDeposit > 0) {
            val restoredSavings = savings.copy(
                balance = Math.max(0.0, savings.balance - txn.savingsDeposit),
                totalDeposited = Math.max(0.0, savings.totalDeposited - txn.savingsDeposit)
            )
            _savingsAccounts.value = _savingsAccounts.value.map { if (it.id == savings.id) restoredSavings else it }
        }

        // Restore DPS
        val dps = _dpsAccounts.value.find { it.memberId == txn.memberId }
        if (dps != null && txn.dpsDeposit > 0) {
            val restoredDps = dps.copy(
                totalDeposited = Math.max(0.0, dps.totalDeposited - txn.dpsDeposit),
                totalPaidMonths = Math.max(0, dps.totalPaidMonths - 1)
            )
            _dpsAccounts.value = _dpsAccounts.value.map { if (it.id == dps.id) restoredDps else it }
        }

        updateMemberFinancialSummary(txn.memberId)

        logAudit(
            action = "TRANSACTION_REVERSED",
            resource = "TRANSACTION",
            resourceId = txn.transactionCode,
            details = "Reversal of ৳${txn.totalCollected} authorized. Reason: $reason"
        )

        return Result.success(true)
    }

    private fun updateMemberFinancialSummary(memberId: String) {
        val member = _members.value.find { it.id == memberId } ?: return
        val activeLoan = _loans.value.find { it.memberId == memberId && it.status != LoanStatus.CANCELLED }
        val savings = _savingsAccounts.value.find { it.memberId == memberId }
        val dps = _dpsAccounts.value.find { it.memberId == memberId }

        val totalDisbursed = activeLoan?.principalAmount ?: 0.0
        val loanBal = activeLoan?.remainingBalance ?: 0.0
        val savBal = savings?.balance ?: 0.0
        val dpsBal = dps?.totalDeposited ?: 0.0

        val totalInst = activeLoan?.totalInstallments ?: 0
        val paidInst = activeLoan?.installments?.count { it.status == InstallmentStatus.PAID } ?: 0
        val overdueInst = activeLoan?.installments?.count { it.status == InstallmentStatus.OVERDUE || it.status == InstallmentStatus.LATE } ?: 0
        val remainingInst = Math.max(0, totalInst - paidInst)

        val nextInst = activeLoan?.installments?.firstOrNull { it.status != InstallmentStatus.PAID }

        val category = when {
            activeLoan == null -> PayerCategory.NO_LOAN
            activeLoan.status == LoanStatus.COMPLETED -> PayerCategory.COMPLETED
            overdueInst > 0 -> PayerCategory.OVERDUE
            paidInst > 0 && overdueInst == 0 -> PayerCategory.REGULAR
            else -> PayerCategory.REGULAR
        }

        val updatedMember = member.copy(
            totalLoanDisbursed = totalDisbursed,
            loanBalance = loanBal,
            totalSavings = savBal,
            totalDps = dpsBal,
            totalInstallments = totalInst,
            paidInstallments = paidInst,
            remainingInstallments = remainingInst,
            overdueInstallments = overdueInst,
            nextInstallmentDue = nextInst?.dueDate ?: "N/A",
            nextInstallmentAmount = nextInst?.dueAmount ?: 0.0,
            lastPaymentDate = getCurrentDate(),
            payerCategory = category
        )

        _members.value = _members.value.map { if (it.id == memberId) updatedMember else it }
    }

    // Auto-scan overdue installments
    fun autoDetectMissedInstallments() {
        val today = getCurrentDate()
        var overdueCount = 0

        val updatedLoans = _loans.value.map { loan ->
            if (loan.status == LoanStatus.ACTIVE) {
                val updatedSchedule = loan.installments.map { inst ->
                    if (inst.status == InstallmentStatus.UNPAID && inst.dueDate < today) {
                        overdueCount++
                        inst.copy(status = InstallmentStatus.OVERDUE)
                    } else {
                        inst
                    }
                }
                loan.copy(installments = updatedSchedule)
            } else {
                loan
            }
        }
        _loans.value = updatedLoans

        // Refresh all members
        _members.value.forEach { updateMemberFinancialSummary(it.id) }

        logAudit(
            action = "AUTO_MISSED_INSTALLMENTS_CHECK",
            resource = "SYSTEM",
            resourceId = "CRON",
            details = "Automated missed installment detector executed. $overdueCount installments flagged overdue."
        )
    }

    fun addNotice(title: String, content: String, priority: String) {
        val notice = Notice(
            id = UUID.randomUUID().toString(),
            title = title,
            content = content,
            publishDate = getCurrentDate(),
            expiryDate = "2026-12-31",
            priority = priority
        )
        _notices.value = listOf(notice) + _notices.value
        logAudit("NOTICE_PUBLISHED", "NOTICE", notice.id, "Published notice: $title")
    }

    fun updateSettings(newSettings: InstitutionSettings) {
        _settings.value = newSettings
        logAudit("SETTINGS_UPDATED", "SYSTEM", "CONFIG", "Updated institution parameters")
    }
}
