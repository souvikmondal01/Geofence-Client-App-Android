package com.kivous.wassuser.ui.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.kivous.wassuser.R
import com.kivous.wassuser.databinding.ActivityAuthenticationBinding
import com.kivous.wassuser.utils.setStatusBarColor
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AuthenticationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAuthenticationBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_authentication)
        setStatusBarColor(R.color.orange_500)

        binding.cvBackArrow.setOnClickListener { finish() }
        binding.cvSettings.setOnClickListener {}
        binding.lavWave.playAnimation()
    }
}