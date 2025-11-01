package com.promtuz.chat.presentation.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.ViewModel
import com.promtuz.chat.data.repository.UserRepository

class ShareIdentityVM(
    private val application: Application,
    private val userRepository: UserRepository
) : ViewModel() {
    private val context: Context get() = application.applicationContext


}