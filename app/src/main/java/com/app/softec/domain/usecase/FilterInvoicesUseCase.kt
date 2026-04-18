package com.app.softec.domain.usecase

import com.app.softec.data.repository.AccountRepository
import com.app.softec.domain.model.Account
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Defines the available filter categories for invoices.
 */
enum class InvoiceFilterType {
    ALL,
    ACTIVE,
    OVERDUE,
    PARTIAL,
    PAID
}

/**
 * Use case for filtering the list of invoices (Accounts) based on their payment status.
 */
class FilterInvoicesUseCase @Inject constructor(
    private val accountRepository: AccountRepository
) {

    /**
     * Retrieves a reactive stream of invoices filtered by the specified category.
     *
     * @param userId The ID of the current authenticated user.
     * @param filterType The category by which to filter the invoices.
     * @return A Flow emitting the filtered list of [Account].
     */
    operator fun invoke(userId: String, filterType: InvoiceFilterType): Flow<List<Account>> {
        val allInvoicesFlow = accountRepository.getAllAccounts(userId)

        return allInvoicesFlow.map { invoices ->
            when (filterType) {
                InvoiceFilterType.ALL -> {
                    invoices
                }
                InvoiceFilterType.ACTIVE -> {
                    invoices.filter { it.status.lowercase() == "active" }
                }
                InvoiceFilterType.OVERDUE -> {
                    // Fallback to check daysOverdue in case the status string isn't updated
                    invoices.filter { it.status.lowercase() == "overdue" || it.daysOverdue > 0 }
                }
                InvoiceFilterType.PARTIAL -> {
                    invoices.filter { it.status.lowercase() == "partial" }
                }
                InvoiceFilterType.PAID -> {
                    invoices.filter { it.status.lowercase() == "paid" }
                }
            }
        }
    }
}