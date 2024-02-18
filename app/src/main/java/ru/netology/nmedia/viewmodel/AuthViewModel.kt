package ru.netology.nmedia.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import ru.netology.nmedia.auth.AppAuth
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(private val appAuth: AppAuth) : ViewModel() {
    val data = appAuth.authStateFlow

    val authenticated: Boolean
        get() = data.value.id != 0L

    fun login(login: String, pass: String, context: Context) = viewModelScope.launch {
        appAuth.login(login, pass, context)
    }
}