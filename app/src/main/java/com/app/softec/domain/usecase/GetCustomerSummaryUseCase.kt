package com.app.softec.domain.usecase

import com.app.softec.data.repository.AccountRepository
import com.app.softec.data.repository.CustomerRepository
import com.app.softec.domain.model.Account
import com.app.softec.domain.model.Customer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import javax.inject.Inject

/**
 * Data class representing a complete overview of a customer's financial standing.
 */
data class CustomerSummary(
    val customer: Customer,
    val invoices: List<Account>, // Represents the individual accounts/invoices
    val totalAmountDue: Double,
    val totalAmountPaid: Double,
    val totalAmountRemaining: Double,
    val overdueInvoicesCount: Int
)

/**
 * Use case for aggregating all Account (Invoice) data associated with a single customer
 * to provide a summarized view of their outstanding balances and payment history.
 */
class GetCustomerSummaryUseCase @Inject constructor(
    private val customerRepository: CustomerRepository,
    private val accountRepository: AccountRepository
) {

    /**
     * Retrieves a real-time reactive summary for a specific customer.
     * * @param customerId The ID of the target customer.
     * @return A Flow emitting the aggregated [CustomerSummary].
     */
    operator fun invoke(customerId: String): Flow<CustomerSummary> {
        
        // 1. Fetch the reactive streams from repositories
        val customerFlow = customerRepository.getCustomerFlow(customerId).filterNotNull()
        val accountsFlow = accountRepository.getAccountsByCustomerId(customerId)

        // 2. Combine the streams and calculate aggregates whenever underlying data changes
        return combine(customerFlow, accountsFlow) { customer, accounts ->
            
            val totalDue = accounts.sumOf { it.totalAmountDue }
            val totalPaid = accounts.sumOf { it.amountPaid }
            val totalRemaining = accounts.sumOf { it.amountRemaining }
            val overdueCount = accounts.count { it.status == "overdue" || it.daysOverdue > 0 }

            CustomerSummary(
                customer = customer,
                invoices = accounts,
                totalAmountDue = totalDue,
                totalAmountPaid = totalPaid,
                totalAmountRemaining = totalRemaining,
                overdueInvoicesCount = overdueCount
            )
        }
    }
}