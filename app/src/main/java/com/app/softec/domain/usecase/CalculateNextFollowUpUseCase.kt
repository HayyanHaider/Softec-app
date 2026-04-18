package com.app.softec.domain.usecase

import com.app.softec.domain.model.Account
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

/**
 * Use case for calculating the next appropriate follow-up date for an account.
 * Suggests dates based on the account's overdue severity or user-defined intervals.
 */
class CalculateNextFollowUpUseCase @Inject constructor() {

    /**
     * Calculates the suggested next follow-up date.
     *
     * @param account The account requiring a follow-up schedule.
     * @param customDaysInterval Optional user-defined interval in days. If null, automatic logic applies.
     * @return The calculated next follow-up Date, or null if the account is already fully paid.
     */
    operator fun invoke(account: Account, customDaysInterval: Int? = null): Date? {
        // Guard clause: No follow-up needed if the account is fully paid
        if (account.status == "paid" || account.amountRemaining <= 0.0) {
            return null
        }

        val calendar = Calendar.getInstance()
        calendar.time = Date() // Baseline is always today's date

        // Use the custom interval if provided, otherwise fall back to the severity logic
        val daysToAdd = customDaysInterval ?: calculateDefaultInterval(account.daysOverdue)

        calendar.add(Calendar.DAY_OF_YEAR, daysToAdd)

        return calendar.time
    }

    /**
     * Determines the standard wait time (in days) before the next follow-up based on severity.
     * * @param daysOverdue The number of days the account is past its due date.
     * @return The number of days to wait until the next follow-up.
     */
    private fun calculateDefaultInterval(daysOverdue: Int): Int {
        return when {
            daysOverdue <= 0 -> 7     // Not overdue yet (or due today): follow up in a week
            daysOverdue <= 7 -> 3     // Initial grace period: follow up every 3 days
            daysOverdue in 8..30 -> 5 // Standard delay: follow up every 5 days
            else -> 2                 // Urgent (30+ days): follow up aggressively every 2 days
        }
    }
}