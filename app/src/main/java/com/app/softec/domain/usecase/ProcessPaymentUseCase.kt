package com.app.softec.domain.usecase

import com.app.softec.data.repository.AccountRepository
import com.app.softec.data.repository.PaymentRepository
import com.app.softec.domain.model.PaymentHistory
import java.util.Date
import java.util.UUID
import javax.inject.Inject

/**
 * Use case for processing a payment for a specific account.
 * Handles creating the payment history record and updating the account's balance and status.
 */
class ProcessPaymentUseCase @Inject constructor(
    private val accountRepository: AccountRepository,
    private val paymentRepository: PaymentRepository
) {

    /**
     * Processes a payment and updates the corresponding account.
     *
     * @param userId The unique identifier of the current user.
     * @param accountId The ID of the account making the payment.
     * @param amount The amount being paid.
     * @param paymentMethod Optional method of payment (e.g., "cash", "bank_transfer").
     * @param transactionId Optional reference ID for the transaction.
     * @param notes Optional notes regarding the payment.
     * @return Result indicating success or failure with an exception.
     */
    suspend operator fun invoke(
        userId: String,
        accountId: String,
        amount: Double,
        paymentMethod: String? = null,
        transactionId: String? = null,
        notes: String? = null
    ): Result<Unit> {
        return try {
            // 1. Validate payment amount
            if (amount <= 0) {
                return Result.failure(IllegalArgumentException("Payment amount must be greater than zero."))
            }

            // 2. Fetch the account to verify existence and remaining balance
            val account = accountRepository.getAccountById(accountId)
                ?: return Result.failure(IllegalArgumentException("Account not found."))

            if (amount > account.amountRemaining) {
                return Result.failure(IllegalArgumentException("Payment amount exceeds the remaining balance."))
            }

            val currentDate = Date()

            // 3. Create and save the PaymentHistory record
            val payment = PaymentHistory(
                id = UUID.randomUUID().toString(),
                accountId = account.id,
                customerId = account.customerId,
                amount = amount,
                paymentDate = currentDate,
                paymentMethod = paymentMethod,
                transactionId = transactionId,
                notes = notes,
                createdAt = currentDate
            )
            paymentRepository.insertPayment(payment, userId)

            // 4. Calculate new account totals and determine the new status
            val newAmountPaid = account.amountPaid + amount
            val newAmountRemaining = account.amountRemaining - amount

            val newStatus = when {
                newAmountRemaining <= 0.0 -> "paid"
                else -> "partial"
            }

            // 5. Update the Account record
            val updatedAccount = account.copy(
                amountPaid = newAmountPaid,
                amountRemaining = newAmountRemaining,
                status = newStatus,
                updatedAt = currentDate
            )

            accountRepository.updateAccount(updatedAccount, userId)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}