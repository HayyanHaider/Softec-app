package com.app.softec.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.softec.core.result.Resource
import com.app.softec.core.ui.components.UiState
import com.app.softec.data.auth.AuthRepository
import com.app.softec.data.local.entity.SyncItemEntity
import com.app.softec.data.repository.SyncItemRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Date
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val syncItemRepository: SyncItemRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<SyncItemEntity>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<SyncItemEntity>>> = _uiState.asStateFlow()

    init {
        observeLocalItems()
        initializeRemoteSync()
    }

    private fun initializeRemoteSync() {
        viewModelScope.launch {
            if (!ensureAuthenticated()) {
                return@launch
            }
            syncItemRepository.bindRealtimeSync(viewModelScope)
            syncNow()
        }
    }

    private fun observeLocalItems() {
        viewModelScope.launch {
            syncItemRepository.observeItems().collectLatest { resource ->
                _uiState.value = resource.toUiState()
            }
        }
    }

    fun addSampleItem() {
        viewModelScope.launch {
            val now = Date()
            val item = SyncItemEntity(
                id = UUID.randomUUID().toString(),
                title = "Hackathon item ${now.time}",
                notes = "Created from local Room-first flow",
                tags = listOf("sample", "hackathon"),
                updatedAt = now,
                isSynced = false
            )
            syncItemRepository.upsert(item)
        }
    }

    fun addCustomer(
        name: String,
        phone: String,
        email: String
    ) {
        if (name.isBlank()) {
            return
        }

        viewModelScope.launch {
            val now = Date()
            val notes = buildList {
                if (phone.isNotBlank()) add("Phone: $phone")
                if (email.isNotBlank()) add("Email: $email")
            }.joinToString(separator = " • ")

            val item = SyncItemEntity(
                id = UUID.randomUUID().toString(),
                title = name.trim(),
                notes = notes.ifBlank { null },
                tags = emptyList(),
                updatedAt = now,
                isSynced = false
            )
            syncItemRepository.upsert(item)
        }
    }

    fun deleteCustomer(item: SyncItemEntity) {
        viewModelScope.launch {
            val result = syncItemRepository.delete(item)
            if (result is Resource.Error) {
                _uiState.value = UiState.Error(result.message)
            }
        }
    }

    fun syncNow() {
        viewModelScope.launch {
            val result = syncItemRepository.refreshFromRemote()
            if (result is Resource.Error) {
                _uiState.value = UiState.Error(result.message)
            }
        }
    }

    private suspend fun ensureAuthenticated(): Boolean {
        val currentUser = authRepository.authState().first()
        if (currentUser != null) {
            return true
        }

        _uiState.value = UiState.Error("Sign in to start syncing your data")
        return false
    }

    private fun Resource<List<SyncItemEntity>>.toUiState(): UiState<List<SyncItemEntity>> {
        return when (this) {
            is Resource.Loading -> UiState.Loading
            is Resource.Success -> UiState.Success(data)
            is Resource.Error -> UiState.Error(message)
        }
    }
}
