package com.app.softec.ui.screens.invoice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.softec.core.result.Resource
import com.app.softec.data.auth.AuthRepository
import com.app.softec.data.local.entity.SyncItemEntity
import com.app.softec.data.repository.AccountRepository
import com.app.softec.data.repository.CustomerRepository
import com.app.softec.data.repository.FollowUpRepository
import com.app.softec.data.repository.SyncItemRepository
import com.app.softec.domain.model.Account
import com.app.softec.domain.model.Customer
import com.app.softec.domain.model.FollowUp
import com.app.softec.domain.usecase.CalculateNextFollowUpUseCase
import com.app.softec.domain.usecase.CreateInvoiceUseCase
import com.app.softec.domain.usecase.FilterInvoicesUseCase
import com.app.softec.domain.usecase.InvoiceFilterType
import com.app.softec.domain.usecase.GenerateReminderMessageUseCase
import com.app.softec.domain.usecase.GenerateAIReminderMessageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Calendar
import java.util.Date
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class InvoiceListItemUi(
    val account: Account,
    val customerName: String
)

data class InvoiceCustomerOptionUi(
    val customerId: String,
    val customerName: String,
    val contactNumber: String
)

data class InvoiceListUiState(
    val isLoading: Boolean = true,
    val invoices: List<InvoiceListItemUi> = emptyList(),
    val availableCustomers: List<InvoiceCustomerOptionUi> = emptyList(),
    val totalOutstanding: Double = 0.0,
    val highPriorityCount: Int = 0,
    val overdueCount: Int = 0,
    val errorMessage: String? = null
)

data class InvoiceDetailUiState(
    val isLoading: Boolean = true,
    val account: Account? = null,
    val customerName: String = "",
    val errorMessage: String? = null
)

enum class FollowUpContactChannel(val value: String) {
    WHATSAPP("whatsapp"),
    SMS("sms"),
    EMAIL("email")
}

data class FollowUpUiState(
    val isLoading: Boolean = true,
    val accountId: String? = null,
    val customerName: String = "",
    val contactNumber: String = "",
    val customerEmail: String? = null,
    val selectedChannel: FollowUpContactChannel = FollowUpContactChannel.WHATSAPP,
    val selectedIntervalDays: Int? = null,
    val isCustomDateSelected: Boolean = false,
    val suggestedMessage: String = "",
    val draftMessage: String = "",
    val nextFollowUpDate: Date? = null,
    val isSubmitting: Boolean = false,
    val submissionCompleted: Boolean = false,
    val isGeneratingAI: Boolean = false,
    val aiGeneratedMessage: String? = null,
    val errorMessage: String? = null
)

data class InvoiceEditorUiState(
    val isLoading: Boolean = true,
    val isEditMode: Boolean = false,
    val invoiceId: String? = null,
    val customerId: String? = null,
    val customerName: String = "",
    val contactNumber: String = "",
    val customerEmail: String? = null,
    val totalAmountInput: String = "",
    val amountPaidInput: String = "0",
    val dueDateMillis: Long = Date().time,
    val notes: String = "",
    val isSaving: Boolean = false,
    val saveCompleted: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class InvoiceViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val syncItemRepository: SyncItemRepository,
    private val accountRepository: AccountRepository,
    private val customerRepository: CustomerRepository,
    private val followUpRepository: FollowUpRepository,
    private val createInvoiceUseCase: CreateInvoiceUseCase,
    private val filterInvoicesUseCase: FilterInvoicesUseCase,
    private val generateReminderMessageUseCase: GenerateReminderMessageUseCase,
    private val generateAIReminderMessageUseCase: GenerateAIReminderMessageUseCase,
    private val calculateNextFollowUpUseCase: CalculateNextFollowUpUseCase
) : ViewModel() {

    private val _listState = MutableStateFlow(InvoiceListUiState())
    val listState: StateFlow<InvoiceListUiState> = _listState.asStateFlow()

    private val _detailState = MutableStateFlow(InvoiceDetailUiState())
    val detailState: StateFlow<InvoiceDetailUiState> = _detailState.asStateFlow()

    private val _followUpState = MutableStateFlow(FollowUpUiState())
    val followUpState: StateFlow<FollowUpUiState> = _followUpState.asStateFlow()

    private val _editorState = MutableStateFlow(InvoiceEditorUiState())
    val editorState: StateFlow<InvoiceEditorUiState> = _editorState.asStateFlow()

    private var currentUserId: String? = null
    private var followUpAccount: Account? = null
    private var editingAccount: Account? = null

    init {
        observeInvoiceList()
    }

    fun observeInvoiceList() {
        viewModelScope.launch {
            _listState.update { it.copy(isLoading = true, errorMessage = null) }

            val userId = authRepository.authState().first()?.uid
            if (userId == null) {
                _listState.update {
                    it.copy(
                        isLoading = false,
                        invoices = emptyList(),
                        errorMessage = "Please sign in to view invoices."
                    )
                }
                return@launch
            }

            currentUserId = userId
            accountRepository.bindRealtimeSync(userId, viewModelScope)
            runCatching {
                accountRepository.pullFromFirestore(userId)
                accountRepository.syncUnsynced(userId)
            }.onFailure { throwable ->
                _listState.update {
                    it.copy(errorMessage = throwable.message ?: "Invoice sync with Firebase failed.")
                }
            }

            combine(
                filterInvoicesUseCase(userId, InvoiceFilterType.ALL),
                customerRepository.getAllCustomers(userId),
                syncItemRepository.observeItems().map { resource ->
                    when (resource) {
                        is Resource.Success -> resource.data
                        is Resource.Loading -> emptyList()
                        is Resource.Error -> emptyList()
                    }
                }
            ) { accounts, customers, syncItems ->
                val customersById = customers.associateBy { it.customerId }
                val syncItemsById = syncItems.associateBy { it.id }
                val invoices = accounts
                    .sortedWith(
                        compareByDescending<Account> { it.daysOverdue }
                            .thenByDescending { it.amountRemaining }
                            .thenBy { it.dueDate }
                    )
                    .map { account ->
                        val fallbackSyncItem = syncItemsById[account.customerId]
                        InvoiceListItemUi(
                            account = account,
                            customerName = customersById[account.customerId]?.customerName
                                ?: fallbackSyncItem?.title
                                ?: "Unknown Customer"
                        )
                    }

                val availableCustomers = syncItems
                    .sortedBy { it.title.lowercase() }
                    .map { item ->
                        InvoiceCustomerOptionUi(
                            customerId = item.id,
                            customerName = item.title,
                            contactNumber = item.extractFieldFromNotes("Phone") ?: ""
                        )
                    }

                invoices to availableCustomers
            }.collect { (invoices, availableCustomers) ->
                val overdueInvoices = invoices.filter { invoice ->
                    invoice.account.status.lowercase() == "overdue" || invoice.account.daysOverdue > 0
                }
                _listState.value = InvoiceListUiState(
                    isLoading = false,
                    invoices = invoices,
                    availableCustomers = availableCustomers,
                    totalOutstanding = invoices.sumOf { it.account.amountRemaining },
                    highPriorityCount = overdueInvoices.count { it.account.daysOverdue >= 30 },
                    overdueCount = overdueInvoices.size,
                    errorMessage = null
                )
            }
        }
    }

    fun deleteInvoice(account: Account) {
        viewModelScope.launch {
            val userId = resolveUserId() ?: return@launch
            runCatching {
                accountRepository.deleteAccount(account, userId)
            }.onFailure { throwable ->
                _listState.update {
                    it.copy(errorMessage = throwable.message ?: "Unable to delete invoice.")
                }
            }
        }
    }

    fun loadInvoiceDetail(accountId: String) {
        viewModelScope.launch {
            _detailState.value = InvoiceDetailUiState(isLoading = true)
            runCatching {
                val account = accountRepository.getAccountById(accountId)
                    ?: error("The selected invoice was not found.")
                val customer = customerRepository.getCustomerFlow(account.customerId).first()
                val customerName = customer?.customerName ?: "Unknown Customer"

                InvoiceDetailUiState(
                    isLoading = false,
                    account = account,
                    customerName = customerName,
                    errorMessage = null
                )
            }.onSuccess { state ->
                _detailState.value = state
            }.onFailure { throwable ->
                _detailState.value = InvoiceDetailUiState(
                    isLoading = false,
                    account = null,
                    customerName = "",
                    errorMessage = throwable.message ?: "Unable to load invoice details."
                )
            }
        }
    }

    fun prepareFollowUp(accountId: String) {
        viewModelScope.launch {
            _followUpState.value = FollowUpUiState(isLoading = true)

            runCatching {
                val account = accountRepository.getAccountById(accountId)
                    ?: error("Unable to load invoice for follow-up.")
                val customer = customerRepository.getCustomerFlow(account.customerId).first()
                val customerName = customer?.customerName ?: "Unknown Customer"
                val contactNumber = customer?.contactNumber.orEmpty()
                val customerEmail = customer?.email
                val suggestedMessage = buildSuggestedMessage(account, customer)
                val nextFollowUpDate = calculateNextFollowUpUseCase(account)

                followUpAccount = account

                FollowUpUiState(
                    isLoading = false,
                    accountId = account.id,
                    customerName = customerName,
                    contactNumber = contactNumber,
                    customerEmail = customerEmail,
                    selectedChannel = resolveDefaultFollowUpChannel(contactNumber, customerEmail),
                    selectedIntervalDays = null,
                    isCustomDateSelected = false,
                    suggestedMessage = suggestedMessage,
                    draftMessage = suggestedMessage,
                    nextFollowUpDate = nextFollowUpDate,
                    isSubmitting = false,
                    submissionCompleted = false,
                    errorMessage = null
                )
            }.onSuccess { state ->
                _followUpState.value = state
            }.onFailure { throwable ->
                _followUpState.value = FollowUpUiState(
                    isLoading = false,
                    errorMessage = throwable.message ?: "Unable to initialize follow-up."
                )
            }
        }
    }

    fun updateDraftMessage(message: String) {
        _followUpState.update { it.copy(draftMessage = message) }
    }

    fun updateFollowUpChannel(channel: FollowUpContactChannel) {
        _followUpState.update { it.copy(selectedChannel = channel, errorMessage = null) }
    }

    fun setFollowUpError(message: String) {
        _followUpState.update { it.copy(errorMessage = message) }
    }

    fun updateFollowUpInterval(days: Int?) {
        val account = followUpAccount ?: return
        val nextDate = calculateNextFollowUpUseCase(account, days)
        _followUpState.update {
            it.copy(
                selectedIntervalDays = days,
                isCustomDateSelected = false,
                nextFollowUpDate = nextDate
            )
        }
    }

    fun updateFollowUpDate(date: Date) {
        _followUpState.update {
            it.copy(
                selectedIntervalDays = null,
                isCustomDateSelected = true,
                nextFollowUpDate = date
            )
        }
    }

    fun submitFollowUp() {
        viewModelScope.launch {
            val account = followUpAccount
            val state = _followUpState.value
            val userId = resolveUserId()

            if (account == null || userId == null) {
                _followUpState.update {
                    it.copy(errorMessage = "Unable to submit follow-up for this invoice.")
                }
                return@launch
            }

            val channelError = validateFollowUpChannel(state)
            if (channelError != null) {
                _followUpState.update { it.copy(errorMessage = channelError) }
                return@launch
            }

            _followUpState.update { it.copy(isSubmitting = true, errorMessage = null) }

            runCatching {
                val now = Date()
                val followUp = FollowUp(
                    id = "",
                    accountId = account.id,
                    customerId = account.customerId,
                    followUpDate = now,
                    status = "pending",
                    outcome = null,
                    contactMethod = state.selectedChannel.value,
                    suggestedMessage = state.suggestedMessage,
                    actualMessage = state.draftMessage.ifBlank { state.suggestedMessage },
                    promiseDate = null,
                    nextFollowUpDate = state.nextFollowUpDate,
                    createdAt = now,
                    updatedAt = now,
                    notes = null
                )

                followUpRepository.insertFollowUp(followUp, userId)

                accountRepository.updateAccount(
                    account.copy(
                        lastFollowUpDate = now,
                        nextFollowUpDate = state.nextFollowUpDate,
                        updatedAt = now
                    ),
                    userId
                )
            }.onSuccess {
                _followUpState.update {
                    it.copy(
                        isSubmitting = false,
                        submissionCompleted = true,
                        errorMessage = null
                    )
                }
            }.onFailure { throwable ->
                _followUpState.update {
                    it.copy(
                        isSubmitting = false,
                        submissionCompleted = false,
                        errorMessage = throwable.message ?: "Unable to submit follow-up."
                    )
                }
            }
        }
    }

    fun clearFollowUpCompletion() {
        _followUpState.update { it.copy(submissionCompleted = false) }
    }

    fun generateAIMessage() {
        viewModelScope.launch {
            val account = followUpAccount
            val state = _followUpState.value

            if (account == null) {
                _followUpState.update {
                    it.copy(errorMessage = "Account data not loaded. Please try again.")
                }
                return@launch
            }

            _followUpState.update { it.copy(isGeneratingAI = true, errorMessage = null) }

            runCatching {
                val customer = customerRepository.getCustomerFlow(account.customerId).first()
                    ?: throw Exception("Customer information not found.")
                generateAIReminderMessageUseCase(account, customer)
            }.onSuccess { aiMessage ->
                _followUpState.update {
                    it.copy(
                        isGeneratingAI = false,
                        aiGeneratedMessage = aiMessage,
                        draftMessage = aiMessage,
                        errorMessage = null
                    )
                }
            }.onFailure { throwable ->
                _followUpState.update {
                    it.copy(
                        isGeneratingAI = false,
                        errorMessage = throwable.message ?: "Failed to generate AI message."
                    )
                }
            }
        }
    }

    fun useSystemPrompt() {
        viewModelScope.launch {
            val account = followUpAccount
            if (account == null) {
                _followUpState.update {
                    it.copy(errorMessage = "Account data not loaded. Please try again.")
                }
                return@launch
            }
            runCatching {
                val customer = customerRepository.getCustomerFlow(account.customerId).first()
                    ?: throw IllegalStateException("Customer information not found.")
                generateReminderMessageUseCase(account, customer)
            }.onSuccess { urgencyBasedPrompt ->
                _followUpState.update {
                    it.copy(
                        draftMessage = urgencyBasedPrompt,
                        errorMessage = null
                    )
                }
            }.onFailure { throwable ->
                _followUpState.update {
                    it.copy(
                        errorMessage = throwable.message ?: "Unable to load system prompt."
                    )
                }
            }
        }
    }

    fun prepareEditor(invoiceId: String?, selectedCustomerId: String?) {
        viewModelScope.launch {
            if (invoiceId.isNullOrBlank()) {
                runCatching {
                    val customerId = selectedCustomerId
                        ?: error("Choose a customer before creating an invoice.")
                    val selectedSyncItem = getSyncItemById(customerId)
                        ?: error("The selected customer is no longer available.")

                    editingAccount = null
                    InvoiceEditorUiState(
                        isLoading = false,
                        isEditMode = false,
                        customerId = selectedSyncItem.id,
                        customerName = selectedSyncItem.title,
                        contactNumber = selectedSyncItem.extractFieldFromNotes("Phone") ?: "",
                        customerEmail = selectedSyncItem.extractFieldFromNotes("Email"),
                        dueDateMillis = defaultDueDateMillis(),
                        amountPaidInput = "0",
                        errorMessage = null
                    )
                }.onSuccess { state ->
                    _editorState.value = state
                }.onFailure { throwable ->
                    _editorState.value = InvoiceEditorUiState(
                        isLoading = false,
                        isEditMode = false,
                        errorMessage = throwable.message ?: "Unable to start invoice creation."
                    )
                }
                return@launch
            }

            _editorState.value = InvoiceEditorUiState(isLoading = true)
            runCatching {
                val account = accountRepository.getAccountById(invoiceId)
                    ?: error("Unable to load invoice.")
                val customer = customerRepository.getCustomerFlow(account.customerId).first()
                editingAccount = account

                InvoiceEditorUiState(
                    isLoading = false,
                    isEditMode = true,
                    invoiceId = account.id,
                    customerId = account.customerId,
                    customerName = customer?.customerName.orEmpty(),
                    contactNumber = customer?.contactNumber.orEmpty(),
                    customerEmail = customer?.email,
                    totalAmountInput = account.totalAmountDue.toDisplayInput(),
                    amountPaidInput = account.amountPaid.toDisplayInput(),
                    dueDateMillis = account.dueDate.time,
                    notes = account.notes.orEmpty(),
                    errorMessage = null
                )
            }.onSuccess { state ->
                _editorState.value = state
            }.onFailure { throwable ->
                _editorState.value = InvoiceEditorUiState(
                    isLoading = false,
                    errorMessage = throwable.message ?: "Unable to load invoice editor."
                )
            }
        }
    }

    fun updateEditorTotalAmount(value: String) {
        _editorState.update { it.copy(totalAmountInput = value, errorMessage = null) }
    }

    fun updateEditorAmountPaid(value: String) {
        _editorState.update { it.copy(amountPaidInput = value, errorMessage = null) }
    }

    fun updateEditorDueDate(millis: Long) {
        _editorState.update { it.copy(dueDateMillis = millis, errorMessage = null) }
    }

    fun updateEditorNotes(value: String) {
        _editorState.update { it.copy(notes = value, errorMessage = null) }
    }

    fun clearEditorCompletion() {
        _editorState.update { it.copy(saveCompleted = false) }
    }

    fun saveEditorInvoice() {
        viewModelScope.launch {
            val userId = resolveUserId()
            if (userId == null) {
                _editorState.update {
                    it.copy(errorMessage = "Please sign in to save invoices.")
                }
                return@launch
            }

            val state = _editorState.value
            val totalAmount = state.totalAmountInput.toAmountOrNull()
            val amountPaid = state.amountPaidInput.toAmountOrNull()

            if (totalAmount == null || totalAmount <= 0.0) {
                _editorState.update {
                    it.copy(errorMessage = "Enter a valid total amount due.")
                }
                return@launch
            }

            if (amountPaid == null || amountPaid < 0.0) {
                _editorState.update {
                    it.copy(errorMessage = "Enter a valid amount paid.")
                }
                return@launch
            }

            if (amountPaid > totalAmount) {
                _editorState.update {
                    it.copy(errorMessage = "Amount paid cannot exceed total amount.")
                }
                return@launch
            }

            if (!state.isEditMode && state.customerId.isNullOrBlank()) {
                _editorState.update {
                    it.copy(errorMessage = "Choose a customer before creating an invoice.")
                }
                return@launch
            }

            _editorState.update { it.copy(isSaving = true, errorMessage = null) }

            val dueDate = Date(state.dueDateMillis)
            val notes = state.notes.trim().ifBlank { null }

            runCatching {
                if (state.isEditMode) {
                    val existing = editingAccount ?: error("Unable to find invoice to update.")
                    val remaining = (totalAmount - amountPaid).coerceAtLeast(0.0)
                    val now = Date()
                    val updated = existing.copy(
                        totalAmountDue = totalAmount,
                        amountPaid = amountPaid,
                        amountRemaining = remaining,
                        dueDate = dueDate,
                        updatedAt = now,
                        notes = notes,
                        status = determineStatus(remaining, amountPaid, dueDate),
                        daysOverdue = calculateDaysOverdue(dueDate)
                    )
                    val suggestedNextFollowUp =
                        if (updated.status == "paid") null else calculateNextFollowUpUseCase(updated)

                    accountRepository.updateAccount(
                        updated.copy(nextFollowUpDate = suggestedNextFollowUp),
                        userId
                    )
                    accountRepository.syncUnsynced(userId)
                } else {
                    val customerId = state.customerId
                        ?: error("Choose a customer before creating an invoice.")

                    val existingCustomer = customerRepository.getCustomerFlow(customerId).first()
                    val customer = Customer(
                        customerId = customerId,
                        customerName = state.customerName.ifBlank {
                            existingCustomer?.customerName ?: "Unknown Customer"
                        },
                        contactNumber = state.contactNumber.ifBlank {
                            existingCustomer?.contactNumber.orEmpty()
                        },
                        email = state.customerEmail ?: existingCustomer?.email
                    )
                    customerRepository.insertCustomer(customer, userId)

                    createInvoiceUseCase(
                        userId = userId,
                        customerId = customerId,
                        totalAmountDue = totalAmount,
                        amountPaid = amountPaid,
                        dueDate = dueDate,
                        notes = notes
                    ).getOrThrow()

                    customerRepository.syncUnsynced(userId)
                    accountRepository.syncUnsynced(userId)
                }
            }.onSuccess {
                _editorState.update {
                    it.copy(
                        isSaving = false,
                        saveCompleted = true,
                        errorMessage = null
                    )
                }
            }.onFailure { throwable ->
                _editorState.update {
                    it.copy(
                        isSaving = false,
                        saveCompleted = false,
                        errorMessage = throwable.message ?: "Unable to save invoice."
                    )
                }
            }
        }
    }

    private suspend fun resolveUserId(): String? {
        currentUserId?.let { return it }
        val userId = authRepository.authState().first()?.uid
        currentUserId = userId
        return userId
    }

    private suspend fun buildSuggestedMessage(account: Account, customer: Customer?): String {
        return if (customer != null) {
            generateReminderMessageUseCase(account, customer)
        } else {
            "Dear customer, this is a reminder that invoice ${account.id} has an outstanding " +
                "balance of ${account.amountRemaining}. Please let us know your payment plan."
        }
    }

    private suspend fun getSyncItemById(itemId: String): SyncItemEntity? {
        return when (val resource = syncItemRepository.observeItems().first { it !is Resource.Loading }) {
            is Resource.Success -> resource.data.firstOrNull { it.id == itemId }
            is Resource.Error -> null
            is Resource.Loading -> null
        }
    }

    private fun SyncItemEntity.extractFieldFromNotes(label: String): String? {
        val notesValue = notes ?: return null
        val regex = Regex("$label\\s*:\\s*([^•]+)", RegexOption.IGNORE_CASE)
        return regex.find(notesValue)
            ?.groupValues
            ?.getOrNull(1)
            ?.trim()
            ?.takeIf { it.isNotBlank() }
    }

    private fun defaultDueDateMillis(): Long {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, 7)
        return calendar.timeInMillis
    }

    private fun resolveDefaultFollowUpChannel(
        contactNumber: String,
        customerEmail: String?
    ): FollowUpContactChannel {
        if (contactNumber.isNotBlank()) return FollowUpContactChannel.WHATSAPP
        if (!customerEmail.isNullOrBlank()) return FollowUpContactChannel.EMAIL
        return FollowUpContactChannel.SMS
    }

    private fun validateFollowUpChannel(state: FollowUpUiState): String? {
        return when (state.selectedChannel) {
            FollowUpContactChannel.WHATSAPP,
            FollowUpContactChannel.SMS -> {
                if (state.contactNumber.isBlank()) {
                    "No phone number available for this customer."
                } else {
                    null
                }
            }

            FollowUpContactChannel.EMAIL -> {
                if (state.customerEmail.isNullOrBlank()) {
                    "No email address available for this customer."
                } else {
                    null
                }
            }
        }
    }

    private fun determineStatus(
        amountRemaining: Double,
        amountPaid: Double,
        dueDate: Date
    ): String {
        if (amountRemaining <= 0.0) return "paid"
        if (dueDate.before(Date())) return "overdue"
        if (amountPaid > 0.0) return "partial"
        return "active"
    }

    private fun calculateDaysOverdue(dueDate: Date): Int {
        val today = Date()
        if (today <= dueDate) return 0
        return ((today.time - dueDate.time) / (1000 * 60 * 60 * 24)).toInt()
    }

    private fun String.toAmountOrNull(): Double? {
        if (isBlank()) return null
        val normalized = trim().replace(",", ".")
        return normalized.toDoubleOrNull()
    }

    private fun Double.toDisplayInput(): String {
        return if (this % 1.0 == 0.0) {
            toLong().toString()
        } else {
            toString()
        }
    }
}
