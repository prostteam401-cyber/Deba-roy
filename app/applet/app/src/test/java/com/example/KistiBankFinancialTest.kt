package com.example

import com.example.data.model.LoanFrequency
import com.example.data.model.UserRole
import com.example.data.repository.KistiBankRepository
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class KistiBankFinancialTest {

    private lateinit var repository: KistiBankRepository

    @Before
    fun setup() {
        repository = KistiBankRepository()
    }

    @Test
    fun testLoanCreationAndSchedule() {
        val member = repository.members.value.first()
        val result = repository.createLoan(
            memberId = member.id,
            principalAmount = 100000.0,
            interestRatePercent = 10.0,
            frequency = LoanFrequency.MONTHLY,
            totalInstallments = 20,
            firstInstallmentDate = "2026-11-01"
        )

        assertTrue(result.isSuccess)
        val loan = result.getOrNull()!!
        assertEquals(110000.0, loan.totalPayable, 0.01)
        assertEquals(5500.0, loan.installmentAmount, 0.01)
        assertEquals(20, loan.installments.size)
        assertEquals(110000.0, loan.remainingBalance, 0.01)
    }

    @Test
    fun testAtomicCollectionUpdatesBalances() {
        val member = repository.members.value.find { it.loanBalance > 0 }!!
        val prevLoan = member.loanBalance
        val prevSavings = member.totalSavings

        val collectionResult = repository.recordCollection(
            memberId = member.id,
            loanKistiAmount = 5000.0,
            savingsDeposit = 1000.0,
            dpsDeposit = 0.0,
            paymentMethod = "CASH"
        )

        assertTrue(collectionResult.isSuccess)
        val txn = collectionResult.getOrNull()!!
        assertEquals(6000.0, txn.totalCollected, 0.01)
        assertEquals(prevLoan - 5000.0, txn.newLoanBalance, 0.01)
        assertEquals(prevSavings + 1000.0, txn.newSavingsBalance, 0.01)

        val updatedMember = repository.members.value.find { it.id == member.id }!!
        assertEquals(prevLoan - 5000.0, updatedMember.loanBalance, 0.01)
        assertEquals(prevSavings + 1000.0, updatedMember.totalSavings, 0.01)
    }

    @Test
    fun testTransactionReversalWorkflow() {
        val member = repository.members.value.find { it.loanBalance > 0 }!!
        val collectionResult = repository.recordCollection(
            memberId = member.id,
            loanKistiAmount = 3000.0,
            savingsDeposit = 500.0,
            dpsDeposit = 0.0
        )
        val txn = collectionResult.getOrNull()!!

        // Only manager / admin can reverse
        val reversalResult = repository.reverseTransaction(txn.id, "Incorrect cash deposit amount entered by mistake")
        assertTrue(reversalResult.isSuccess)

        val reversedTxn = repository.transactions.value.find { it.id == txn.id }!!
        assertTrue(reversedTxn.isReversed)
    }

    @Test
    fun testMaskedNidDoesNotExposeFullDigits() {
        val member = repository.members.value.first()
        val masked = member.kyc.maskedDocNumber
        assertTrue(masked.contains("****"))
        assertFalse(masked == member.kyc.docNumber)
    }

    @Test
    fun testRoleBasedDepartmentAccess() {
        // Field officer with dept-01 and dept-02
        val officer = repository.availableUsers.find { it.role == UserRole.COLLECTION_OFFICER }!!
        repository.switchUser(officer)

        assertTrue(repository.canAccessDepartment("dept-01"))
        assertFalse(repository.canAccessDepartment("dept-05"))
    }
}
