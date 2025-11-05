package com.promtuz.chat.presentation.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.promtuz.chat.data.local.entities.User
import com.promtuz.chat.data.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn

enum class UsersGroupMode {
    NONE,

    /**
     * "Anonymous" users at top, nicknamed users afterwards in alphabetically sorted groups
     */
    BY_FIRST_LETTER
}

typealias GroupedUserList = Flow<Map<String, List<User>>>

class SavedUsersVM(
    private val application: Application,
    userRepository: UserRepository
) : ViewModel() {
    private val context: Context get() = application.applicationContext

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    val users: GroupedUserList =
        userRepository.fetchAll(_searchQuery.value)
            .map { groupUsersFlow(it, UsersGroupMode.BY_FIRST_LETTER) }
            .onEach { _isLoading.value = false }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyMap())


    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private fun groupUsersFlow(users: List<User>, mode: UsersGroupMode) = when (mode) {
        UsersGroupMode.NONE -> {
            mapOf("All" to users)
        }

        UsersGroupMode.BY_FIRST_LETTER -> {
            users.groupBy { it.nickname.firstOrNull()?.uppercase() ?: "#" }
        }
    }

    init {
        _isLoading.value = false
    }
}