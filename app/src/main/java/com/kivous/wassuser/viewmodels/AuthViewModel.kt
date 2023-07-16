package com.kivous.wassuser.viewmodels

import android.view.View
import androidx.lifecycle.ViewModel
import com.kivous.wassuser.models.User
import com.kivous.wassuser.repositories.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import org.greenrobot.eventbus.EventBus
import javax.inject.Inject
import kotlin.math.pow

@HiltViewModel
class AuthViewModel @Inject constructor(private val repository: AuthRepository) :
    ViewModel() {
    var name: String? = null
    var email: String? = null
    var password: String? = null
    var registerListener: RegisterListener? = null
    var loginListener: LoginListener? = null
    var isInFence: Boolean = false

    fun registerUser(result: (String) -> Unit) {
        repository.registerUser(User(null, name, email, password)) {
            result.invoke(it)
        }
    }

    fun loginUser(result: (String) -> Unit) {
        repository.loginUser(User(null, name, email, password)) {
            result.invoke(it)
        }
    }

    fun resetPassword(result: (String) -> Unit) {
        repository.resetPassword(email.toString()) {
            result.invoke(it)
        }
    }

    fun onRegisterClick(view: View) {
        registerListener?.onStarted()
        if (name.isNullOrEmpty()) {
            registerListener?.onEmptyName()
            return
        }
        if (email.isNullOrEmpty()) {
            registerListener?.onEmptyEmail()
            return
        }
        if (password.isNullOrEmpty()) {
            registerListener?.onEmptyPassword()
            return
        }
        registerListener?.onSuccess()
    }


    fun onLoginTextClick(view: View) {
        registerListener?.onLoginTextClick()
    }

    fun onLoginClick(view: View) {
        loginListener?.onStarted()
        if (email.isNullOrEmpty()) {
            loginListener?.onEmptyEmail()
            return
        }
        if (password.isNullOrEmpty()) {
            loginListener?.onEmptyPassword()
            return
        }
        if (isInFence) {
            loginListener?.onSuccess()
            return
        }
        loginListener?.locationCheckMsg()
    }

    fun onRegisterTextClick(view: View) {
        loginListener?.onRegisterTextClick()
    }

    fun onForgotPassword(view: View) {
        loginListener?.onStarted()
        if (email.isNullOrEmpty()) {
            loginListener?.onEmptyEmail()
            return
        }
        loginListener?.restPassword()
    }

}


interface RegisterListener {
    fun onEmptyName()
    fun onEmptyEmail()
    fun onEmptyPassword()
    fun onStarted()
    fun onSuccess()
    fun onLoginTextClick()
}

interface LoginListener {
    fun onEmptyEmail()
    fun onEmptyPassword()
    fun onStarted()
    fun onSuccess()
    fun restPassword()
    fun onRegisterTextClick()
    fun locationCheckMsg()
}
