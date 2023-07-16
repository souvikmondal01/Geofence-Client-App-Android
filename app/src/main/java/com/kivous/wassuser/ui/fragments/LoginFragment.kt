package com.kivous.wassuser.ui.fragments

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.kivous.wassuser.R
import com.kivous.wassuser.databinding.FragmentLoginBinding
import com.kivous.wassuser.models.LocationEvent
import com.kivous.wassuser.services.LocationService
import com.kivous.wassuser.ui.activities.ProfileActivity
import com.kivous.wassuser.utils.CommonUtils.changeIconColorWhenEdittextNotInFocus
import com.kivous.wassuser.utils.CommonUtils.clearEdittext
import com.kivous.wassuser.utils.CommonUtils.db
import com.kivous.wassuser.utils.CommonUtils.hideKeyboard
import com.kivous.wassuser.utils.CommonUtils.isInFence
import com.kivous.wassuser.utils.CommonUtils.showPassword
import com.kivous.wassuser.utils.Constant.ENTER_EMAIL
import com.kivous.wassuser.utils.Constant.ENTER_PASSWORD
import com.kivous.wassuser.utils.Constant.LATITUDE
import com.kivous.wassuser.utils.Constant.LOCATION
import com.kivous.wassuser.utils.Constant.LOGIN_SUCCESS
import com.kivous.wassuser.utils.Constant.LONGITUDE
import com.kivous.wassuser.utils.Constant.OPEN_GMAIL
import com.kivous.wassuser.utils.Constant.RADIUS
import com.kivous.wassuser.utils.Constant.RESET_PASSWORD_MSG
import com.kivous.wassuser.utils.fragmentTo
import com.kivous.wassuser.utils.hide
import com.kivous.wassuser.utils.show
import com.kivous.wassuser.utils.toast
import com.kivous.wassuser.viewmodels.AuthViewModel
import com.kivous.wassuser.viewmodels.LoginListener
import dagger.hilt.android.AndroidEntryPoint
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

@AndroidEntryPoint
class LoginFragment : Fragment(), LoginListener {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AuthViewModel by viewModels()
    private var service: Intent? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_login, container, false)
        binding.viewModel = viewModel
        viewModel.loginListener = this
        binding.lifecycleOwner = this
        service = Intent(requireContext(), LocationService::class.java)
        // ask notification permission
        askForNotificationPermission()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        clearEdittext(binding.etEmail, binding.ivCancel)
        changeIconColorWhenEdittextNotInFocus(
            requireContext(),
            binding.etEmail,
            binding.ivCancel
        )
        changeIconColorWhenEdittextNotInFocus(
            requireContext(),
            binding.etPassword,
            binding.ivVisibility
        )

        //show password on visibility icon click
        var isShow = false
        binding.ivVisibility.setOnClickListener {
            showPassword(isShow, binding.etPassword, binding.ivVisibility)
            isShow = !isShow
        }

    }

    @Subscribe
    fun receiveLocationEvent(locationEvent: LocationEvent) {
        db.collection(LOCATION).document(LOCATION).addSnapshotListener { it, _ ->
            viewModel.isInFence =
                    // check is user inside geofence or not
                isInFence(
                    it?.get(LATITUDE).toString().toDouble(),
                    it?.get(LONGITUDE).toString().toDouble(),
                    locationEvent.latitude.toString().toDouble(),
                    locationEvent.longitude.toString().toDouble(),
                    it?.get(RADIUS).toString().toDouble(),
                )

            // if user inside geofence and already login then open Profile screen else Login screen
            if (Firebase.auth.currentUser?.uid?.isNotEmpty() == true && Firebase.auth.currentUser?.isEmailVerified == true && viewModel.isInFence) {
                if (activity != null && isAdded) {
                    requireActivity().startActivity(
                        Intent(
                            requireContext(),
                            ProfileActivity::class.java
                        ).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            .addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                    )
                    requireActivity().finishAffinity()
                }
            }

        }


    }

    override fun onStart() {
        super.onStart()
        // start location service
        requireActivity().startService(service)
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }

        _binding = null
    }

    override fun onEmptyEmail() {
        binding.apply {
            etEmail.apply {
                error = ENTER_EMAIL
                requestFocus()
            }
            pb.hide()
        }
    }

    override fun onEmptyPassword() {
        binding.apply {
            etPassword.apply {
                error = ENTER_PASSWORD
                requestFocus()
            }
            pb.hide()
        }
    }

    override fun onStarted() {
        binding.pb.show()
    }

    override fun onSuccess() {
        hideKeyboard()
        viewModel.loginUser {
            if (it == LOGIN_SUCCESS) {
                binding.pb.hide()
                return@loginUser
            }
            toast(it)
            binding.pb.hide()
        }
    }

    override fun restPassword() {
        hideKeyboard()
        viewModel.resetPassword {
            if (it == RESET_PASSWORD_MSG) {
                showSnackBar(it)
            } else {
                toast(it)
            }
            binding.pb.hide()
        }
    }

    override fun onRegisterTextClick() {
        findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
    }

    override fun locationCheckMsg() {
        toast("Currently, you are outside the geofence.")
        binding.pb.hide()
    }

    private fun showSnackBar(msg: String) {
        val snackBar = Snackbar.make(
            requireView(), msg,
            Snackbar.LENGTH_LONG
        ).setAction(OPEN_GMAIL) {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setClassName(
                "com.google.android.gm",
                "com.google.android.gm.ConversationListActivityGmail"
            )
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent)
        }
        snackBar.show()
    }


    @Suppress("IMPLICIT_CAST_TO_ANY")
    private fun askForNotificationPermission() {
        val permissionState =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.POST_NOTIFICATIONS
                )
            } else {

            }

        if (permissionState == PackageManager.PERMISSION_DENIED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    1
                )
            }
        }
    }


}