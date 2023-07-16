package com.kivous.wassuser.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.kivous.wassuser.R
import com.kivous.wassuser.databinding.FragmentRegisterBinding
import com.kivous.wassuser.utils.CommonUtils.changeIconColorWhenEdittextNotInFocus
import com.kivous.wassuser.utils.CommonUtils.clearEdittext
import com.kivous.wassuser.utils.CommonUtils.hideKeyboard
import com.kivous.wassuser.utils.CommonUtils.showPassword
import com.kivous.wassuser.utils.Constant.ENTER_EMAIL
import com.kivous.wassuser.utils.Constant.ENTER_NAME
import com.kivous.wassuser.utils.Constant.ENTER_PASSWORD
import com.kivous.wassuser.utils.Constant.OPEN_GMAIL
import com.kivous.wassuser.utils.Constant.REGISTRATION_SUCCESS_MSG
import com.kivous.wassuser.utils.hide
import com.kivous.wassuser.utils.show
import com.kivous.wassuser.utils.toast
import com.kivous.wassuser.viewmodels.AuthViewModel
import com.kivous.wassuser.viewmodels.RegisterListener
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class RegisterFragment : Fragment(), RegisterListener {
    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_register,
            container,
            false
        )
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        viewModel.registerListener = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        clearEdittext(binding.etName, binding.ivCancelName)
        clearEdittext(binding.etEmail, binding.ivCancelEmail)

        changeIconColorWhenEdittextNotInFocus(
            requireContext(),
            binding.etName,
            binding.ivCancelName
        )
        changeIconColorWhenEdittextNotInFocus(
            requireContext(),
            binding.etEmail,
            binding.ivCancelEmail
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onEmptyName() {
        binding.apply {
            etName.apply {
                error = ENTER_NAME
                requestFocus()
            }
            pb.hide()
        }
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
        viewModel.registerUser {
            if (it == REGISTRATION_SUCCESS_MSG) {
                showSnackBar(it)
            } else {
                toast(it)
            }
            binding.pb.hide()
            hideKeyboard()
        }
    }

    override fun onLoginTextClick() {
        findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
    }


    // show SnackBar
    private fun showSnackBar(msg: String) {
        val snackBar = Snackbar.make(
            requireView(), msg,
            Snackbar.LENGTH_LONG
        ).setAction(OPEN_GMAIL) {
            // open to gmail app
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

}