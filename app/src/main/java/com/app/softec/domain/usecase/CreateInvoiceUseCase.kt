package com.app.softec.domain.usecase

import com.app.softec.data.repository.AccountRepository
import com.app.softec.domain.model.Account
import java.util.Date
import java.util.UUID
import javax.inject.Inject

/**
 * Use case for creating a new invoice (Account record) for a specific customer.
 * Handles the initialization of default values like timestamps, initial status, and unique IDs.
 */
class CreateInvoiceUseCase @Inject constructor(
    private val accountRepository: AccountRepository
) {

    /**
     * Creates and saves a new invoice.
     *
     * @param userId The ID of the currently logged-in user.
     * @param customerId The ID of the customer this invoice belongs to.
     * @param totalAmountDue The total debt amount.
     * @param dueDate The date by which the amount should be paid.
     * @param notes Optional notes regarding this invoice.
     * @return A Result indicating success or failure.
     */
    suspend operator fun invoke(
        userId: String,
        customerId: String,
        totalAmountDue: Double,
        dueDate: Date,
        notes: String? = null
    ): Result<Unit> {
        return try {
            val today = Date()
            
            // Determine initial status
            val isOverdue = dueDate.before(today)
            val status = if (isOverdue) "overdue" else "active"
            val daysOverdue = if (isOverdue) calculateDaysBetween(dueDate, today) else 0

            // Construct the updated Account/Invoice model
            val newInvoice = Account(
                id = UUID.randomUUID().toString(),
                customerId = customerId,
                totalAmountDue = totalAmountDue,
                amountPaid = 0.0,
                amountRemaining = totalAmountDue,
                dueDate = dueDate,
                createdAt = today,
                updatedAt = today,
                lastFollowUpDate = null,
                nextFollowUpDate = null, // Can be set later by CalculateNextFollowUpUseCase
                status = status,
                notes = notes,
                daysOverdue = daysOverdue
            )

            // Save to local database (which will be synced to Firestore later)
            accountRepository.insertAccount(newInvoice, userId)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Helper function to calculate the difference in days between two dates.
     */
    private fun calculateDaysBetween(start: Date, end: Date): Int {
        val diffInMillis = end.time - start.time
        return (diffInMillis / (1000 * 60 * 60 * 24)).toInt()
    }
}