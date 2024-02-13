package ru.netology.nmedia.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.netology.nmedia.auth.AppAuth

class AuthViewModel : ViewModel() {
    val data = AppAuth.getInstance().authStateFlow

    val authenticated: Boolean
        get() = data.value.id != 0L

    fun login(login: String, pass: String, context: Context) = viewModelScope.launch {
        AppAuth.login(login, pass, context)
    }
}