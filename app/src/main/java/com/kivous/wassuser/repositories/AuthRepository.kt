package com.kivous.wassuser.repositories

import com.google.firebase.firestore.FirebaseFirestore
import com.kivous.wassuser.models.User
import com.kivous.wassuser.utils.CommonUtils.auth
import com.kivous.wassuser.utils.Constant.EMAIL_NOT_VERIFIED_MSG
import com.kivous.wassuser.utils.Constant.LOGIN_SUCCESS
import com.kivous.wassuser.utils.Constant.REGISTRATION_SUCCESS_MSG
import com.kivous.wassuser.utils.Constant.RESET_PASSWORD_MSG
import com.kivous.wassuser.utils.Constant.USER
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val db: FirebaseFirestore,
) {

    // Register user using email & password
    fun registerUser(user: User?, result: (String) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val authResult = auth.createUserWithEmailAndPassword(
                    user!!.email.toString(),
                    user.password.toString()
                ).await()
                user.id = authResult.user!!.uid
                // send verification email
                authResult.user!!.sendEmailVerification().await()
                //set user data to FireStore
                setUser(user)
                withContext(Dispatchers.Main) {
                    result.invoke(
                        REGISTRATION_SUCCESS_MSG
                    )
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    result.invoke(e.message.toString())
                }
            }
        }
    }

    // Login user using email & password
    fun loginUser(user: User?, result: (String) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch(Dispatchers.IO) {
            try {
                val authResult =
                    auth.signInWithEmailAndPassword(
                        user?.email.toString(),
                        user?.password.toString()
                    ).await()
                withContext(Dispatchers.Main) {
                    if (authResult.user!!.isEmailVerified) {
                        result.invoke(LOGIN_SUCCESS)
                        return@withContext
                    }
                    result.invoke(EMAIL_NOT_VERIFIED_MSG)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    result.invoke(
                        e.message.toString()
                    )
                }
            }
        }
    }

    // Reset user's password
    fun resetPassword(email: String?, result: (String) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // send reset email
                auth.sendPasswordResetEmail(email.toString()).await()
                withContext(Dispatchers.Main) {
                    result.invoke(RESET_PASSWORD_MSG)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    result.invoke(e.message.toString())
                }
            }
        }
    }

    private val userCollection =
        db.collection(USER)

    // set user data to FireStore database
    private fun setUser(user: User?) {
        CoroutineScope(Dispatchers.IO).launch(Dispatchers.IO) {
            user?.let {
                userCollection.document(user.id.toString()).set(it)
            }
        }
    }


}