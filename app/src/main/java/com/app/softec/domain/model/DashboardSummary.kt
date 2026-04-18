package com.app.softec.domain.model

data class DashboardSummary(
    val totalPendingAmount: Double,
    val overdueAccountsCount: Int,
    val followUpsNeededToday: Int,
    val overdueAccounts: List<Account>
)

