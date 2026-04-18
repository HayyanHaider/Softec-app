package com.app.softec.domain.usecase

import com.app.softec.data.repository.AccountRepository
import com.app.softec.domain.model.Account
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Use case for retrieving and sorting overdue accounts.
 * Assists in managing overdue accounts by providing an organized list that can be
 * sorted by priority (days overdue) or amount due.
 */
class GetOverdueAccountsUseCase @Inject constructor(
    private val accountRepository: AccountRepository
) {

    /**
     * Retrieves an organized list of overdue accounts for the given user.
     *
     * @param userId The unique identifier of the current user.
     * @param sortBy The preferred sorting method. Defaults to highest priority (most days overdue).
     * @return A Flow containing the sorted list of overdue accounts.
     */
    operator fun invoke(
        userId: String,
        sortBy: SortOption = SortOption.DAYS_OVERDUE_DESC
    ): Flow<List<Account>> {
        return accountRepository.getOverdueAccounts(userId).map { accounts ->
            when (sortBy) {
                SortOption.DAYS_OVERDUE_DESC -> accounts.sortedByDescending { it.daysOverdue }
                SortOption.DAYS_OVERDUE_ASC -> accounts.sortedBy { it.daysOverdue }
                SortOption.AMOUNT_DESC -> accounts.sortedByDescending { it.amountRemaining }
                SortOption.AMOUNT_ASC -> accounts.sortedBy { it.amountRemaining }
            }
        }
    }

    /**
     * Defines the available sorting criteria for the overdue accounts list.
     */
    enum class SortOption {
        DAYS_OVERDUE_DESC, // Priority: Longest overdue first
        DAYS_OVERDUE_ASC,  // Priority: Most recently overdue first
        AMOUNT_DESC,       // Priority: Highest amount remaining first
        AMOUNT_ASC         // Priority: Lowest amount remaining first
    }
}