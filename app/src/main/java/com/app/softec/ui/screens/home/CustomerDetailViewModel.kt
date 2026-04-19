package com.app.softec.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.softec.core.result.Resource
import com.app.softec.data.local.entity.SyncItemEntity
import com.app.softec.data.repository.AccountRepository
import com.app.softec.data.repository.SyncItemRepository
import com.app.softec.domain.model.Account
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class CustomerDetailUiState(
    val isLoading: Boolean = true,
    val customer: SyncItemEntity? = null,
    val phone: String = "",
    val email: String = "",
    val invoices: List<Account> = emptyList(),
    val totalAmountDue: Double = 0.0,
    val totalAmountPaid: Double = 0.0,
    val totalAmountRemaining: Double = 0.0,
    val overdueInvoicesCount: Int = 0,
    val errorMessage: String? = null
)

@HiltViewModel
class CustomerDetailViewModel @Inject constructor(
    private val syncItemRepository: SyncItemRepository,
    private val accountRepository: AccountRepository
) : ViewModel() {

    private val _detailState = MutableStateFlow(CustomerDetailUiState())
    val detailState: StateFlow<CustomerDetailUiState> = _detailState.asStateFlow()

    private var detailJob: Job? = null

    fun loadCustomerDetail(customerId: String) {
        detailJob?.cancel()
        detailJob = viewModelScope.launch {
            combine(
                syncItemRepository.observeItems(),
                accountRepository.getAccountsByCustomerId(customerId)
            ) { syncItemsResource, accounts ->
                when (syncItemsResource) {
                    is Resource.Loading -> CustomerDetailUiState(isLoading = true)
                    is Resource.Error -> CustomerDetailUiState(
                        isLoading = false,
                        errorMessage = syncItemsResource.message
                    )

                    is Resource.Success -> {
                        val customer = syncItemsResource.data.firstOrNull { it.id == customerId }
                            ?: return@combine CustomerDetailUiState(
                                isLoading = false,
                                errorMessage = "Customer not found."
                            )
                        val (phone, email) = parseCustomerNotes(customer.notes)
                        CustomerDetailUiState(
                            isLoading = false,
                            customer = customer,
                            phone = phone,
                            email = email,
                            invoices = accounts.sortedByDescending { it.dueDate },
                            totalAmountDue = accounts.sumOf { it.totalAmountDue },
                            totalAmountPaid = accounts.sumOf { it.amountPaid },
                            totalAmountRemaining = accounts.sumOf { it.amountRemaining },
                            overdueInvoicesCount = accounts.count {
                                it.status.equals("overdue", ignoreCase = true) || it.daysOverdue > 0
                            },
                            errorMessage = null
                        )
                    }
                }
            }.collect { state ->
                _detailState.value = state
            }
        }
    }
}
