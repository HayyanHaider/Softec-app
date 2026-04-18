package com.app.softec.ui.screens.invoices

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.softec.core.ui.components.UiState
import com.app.softec.data.auth.AuthRepository
import com.app.softec.data.repository.AccountRepository
import com.app.softec.data.repository.CustomerRepository
import com.app.softec.data.repository.FollowUpRepository
import com.app.softec.data.repository.SyncRepository
import com.app.softec.domain.model.Account
import com.app.softec.domain.model.Customer
import com.app.softec.domain.model.FollowUp
import com.app.softec.domain.usecase.CalculateNextFollowUpUseCase
import com.app.softec.domain.usecase.GenerateReminderMessageUseCase
import com.app.softec.domain.usecase.GetOverdueAccountsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Date
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

data class AccountWithCustomer(
    val account: Account,
    val customer: Customer?
)

data class OverdueAccountsSuccess(
    val accounts: List<AccountWithCustomer>,
    val sortOption: GetOverdueAccountsUseCase.SortOption,
    val schedulingAccountId: String? = null,
    val suggestedFollowUpDate: Date? = null,
    val selectedFollowUpDate: Date? = null,
    val showDatePicker: Boolean = false,
    val syncingInProgress: Boolean = false,
    val lastError: String? = null
)

@HiltViewModel
class OverdueAccountsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val accountRepository: AccountRepository,
    private val customerRepository: CustomerRepository,
    private val followUpRepository: FollowUpRepository,
    private val syncRepository: SyncRepository,
    private val getOverdueAccountsUseCase: GetOverdueAccountsUseCase,
    private val calculateNextFollowUpUseCase: CalculateNextFollowUpUseCase,
    private val generateReminderMessageUseCase: GenerateReminderMessageUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<OverdueAccountsSuccess>>(UiState.Loading)
    val uiState: StateFlow<UiState<OverdueAccountsSuccess>> = _uiState.asStateFlow()

    private var currentUserId: String? = null

    init {
        observeUserAndLoadOverdueAccounts()
    }

    private fun observeUserAndLoadOverdueAccounts() {
        viewModelScope.launch {
            authRepository.authState().collectLatest { user ->
                if (user != null) {
                    currentUserId = user.uid
                    loadOverdueAccounts(GetOverdueAccountsUseCase.SortOption.DAYS_OVERDUE_DESC)
                } else {
                    _uiState.value = UiState.Error("User not authenticated")
                }
            }
        }
    }

    private fun loadOverdueAccounts(sortOption: GetOverdueAccountsUseCase.SortOption) {
        viewModelScope.launch {
            val userId = currentUserId ?: return@launch
            _uiState.value = UiState.Loading

            try {
                getOverdueAccountsUseCase(userId, sortOption).collectLatest { accounts ->
                    // Enrich accounts with customer data
                    val enrichedAccounts = accounts.map { account ->
                        val customer = try {
                            customerRepository.getCustomerFlow(account.customerId)
                                .map { it }
                                .collectLatest { customerData ->
                                    // This is async, so we'll handle this differently
                                }
                            // For now, we'll fetch customer synchronously in a suspending context
                            null
                        } catch (e: Exception) {
                            null
                        }
                        AccountWithCustomer(account, customer)
                    }

                    val currentState = _uiState.value
                    val successData = if (currentState is UiState.Success) {
                        currentState.data.copy(
                            accounts = enrichedAccounts,
                            sortOption = sortOption
                        )
                    } else {
                        OverdueAccountsSuccess(
                            accounts = enrichedAccounts,
                            sortOption = sortOption
                        )
                    }
                    _uiState.value = UiState.Success(successData)

                    // Now fetch customer data in the background and update
                    enrichedAccounts.forEach { accountWithCustomer ->
                        launch {
                            customerRepository.getCustomerFlow(accountWithCustomer.account.customerId)
                                .collectLatest { customer ->
                                    val currentSuccess = _uiState.value as? UiState.Success ?: return@collectLatest
                                    val updatedAccounts = currentSuccess.data.accounts.map { acc ->
                                        if (acc.account.id == accountWithCustomer.account.id) {
                                            acc.copy(customer = customer)
                                        } else {
                                            acc
                                        }
                                    }
                                    _uiState.value = UiState.Success(
                                        currentSuccess.data.copy(accounts = updatedAccounts)
                                    )
                                }
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Failed to load overdue accounts")
            }
        }
    }

    fun changeSortOption(sortOption: GetOverdueAccountsUseCase.SortOption) {
        loadOverdueAccounts(sortOption)
    }

    fun onScheduleFollowUp(accountId: String) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState !is UiState.Success) return@launch

            val account = currentState.data.accounts
                .find { it.account.id == accountId }?.account ?: return@launch

            val suggestedDate = calculateNextFollowUpUseCase(account)

            _uiState.value = currentState.copy(
                data = currentState.data.copy(
                    schedulingAccountId = accountId,
                    suggestedFollowUpDate = suggestedDate,
                    selectedFollowUpDate = suggestedDate,
                    showDatePicker = true
                )
            )
        }
    }

    fun onDateSelected(date: Date) {
        val currentState = _uiState.value
        if (currentState !is UiState.Success) return

        _uiState.value = currentState.copy(
            data = currentState.data.copy(
                selectedFollowUpDate = date
            )
        )
    }

    fun onConfirmSchedule() {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState !is UiState.Success) return@launch

            val userId = currentUserId ?: return@launch
            val accountId = currentState.data.schedulingAccountId ?: return@launch
            val selectedDate = currentState.data.selectedFollowUpDate ?: return@launch

            val account = currentState.data.accounts
                .find { it.account.id == accountId }?.account ?: return@launch
            val customer = currentState.data.accounts
                .find { it.account.id == accountId }?.customer

            try {
                // Generate reminder message
                val message = if (customer != null) {
                    generateReminderMessageUseCase(account, customer)
                } else {
                    "Follow-up reminder for account ${account.id}"
                }

                // Create FollowUp
                val followUp = FollowUp(
                    id = UUID.randomUUID().toString(),
                    accountId = account.id,
                    customerId = account.customerId,
                    followUpDate = selectedDate,
                    status = "pending",
                    outcome = null,
                    contactMethod = null,
                    suggestedMessage = message,
                    actualMessage = null,
                    promiseDate = null,
                    nextFollowUpDate = selectedDate,
                    createdAt = Date(),
                    updatedAt = Date(),
                    notes = null
                )

                // Insert follow-up locally
                followUpRepository.insertFollowUp(followUp, userId)

                // Update account with follow-up dates
                val updatedAccount = account.copy(
                    lastFollowUpDate = Date(),
                    nextFollowUpDate = selectedDate,
                    updatedAt = Date()
                )
                accountRepository.updateAccount(updatedAccount, userId)

                // Trigger sync
                _uiState.value = currentState.copy(
                    data = currentState.data.copy(
                        syncingInProgress = true,
                        showDatePicker = false,
                        schedulingAccountId = null,
                        lastError = null
                    )
                )

                syncRepository.syncAllUnsynced(userId).onSuccess {
                    _uiState.value = currentState.copy(
                        data = currentState.data.copy(
                            syncingInProgress = false,
                            selectedFollowUpDate = null,
                            suggestedFollowUpDate = null
                        )
                    )
                    // Reload to reflect changes
                    loadOverdueAccounts(currentState.data.sortOption)
                }.onFailure { error ->
                    _uiState.value = currentState.copy(
                        data = currentState.data.copy(
                            syncingInProgress = false,
                            lastError = error.message ?: "Sync failed"
                        )
                    )
                }
            } catch (e: Exception) {
                _uiState.value = currentState.copy(
                    data = currentState.data.copy(
                        syncingInProgress = false,
                        lastError = e.message ?: "Failed to schedule follow-up"
                    )
                )
            }
        }
    }

    fun onDismissDatePicker() {
        val currentState = _uiState.value
        if (currentState !is UiState.Success) return

        _uiState.value = currentState.copy(
            data = currentState.data.copy(
                showDatePicker = false,
                schedulingAccountId = null,
                selectedFollowUpDate = null,
                suggestedFollowUpDate = null
            )
        )
    }

    fun onLaunchContactIntent(accountId: String, contactMethod: String) {
        val currentState = _uiState.value
        if (currentState !is UiState.Success) return

        val accountWithCustomer = currentState.data.accounts
            .find { it.account.id == accountId } ?: return

        // Emit a one-shot event for the UI layer to handle intent launching
        viewModelScope.launch {
            // This will be handled by a separate one-shot event flow in a complete implementation
            // For now, the UI layer will directly launch intents based on this method call
        }
    }
}
