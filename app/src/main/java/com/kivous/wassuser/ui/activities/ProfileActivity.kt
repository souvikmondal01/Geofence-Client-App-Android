package com.kivous.wassuser.ui.activities

import android.content.Intent
import android.nfc.tech.NfcA
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.kivous.wassuser.R
import com.kivous.wassuser.databinding.ActivityProfileBinding
import com.kivous.wassuser.models.LocationEvent
import com.kivous.wassuser.services.LocationService
import com.kivous.wassuser.utils.CommonUtils
import com.kivous.wassuser.utils.CommonUtils.auth
import com.kivous.wassuser.utils.CommonUtils.db
import com.kivous.wassuser.utils.Constant
import com.kivous.wassuser.utils.Constant.EMAIL
import com.kivous.wassuser.utils.Constant.NAME
import com.kivous.wassuser.utils.Constant.USER
import com.kivous.wassuser.viewmodels.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

@AndroidEntryPoint
class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding
    private var service: Intent? = null
    private val viewModel: AuthViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_profile)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

        service = Intent(this, LocationService::class.java)

        binding.btnSignOut.setOnClickListener {
            // user sign out
            auth.signOut()
            // stop location service
            stopService(service)
            finishAffinity()
        }

        // fetch user data from FireStore database
        db.collection(USER).document(auth.currentUser!!.uid).addSnapshotListener { it, e ->
            binding.tvName.text = it?.get(NAME).toString()
            binding.tvEmail.text = it?.get(EMAIL).toString()
        }

    }

    @Subscribe
    fun receiveLocationEvent(locationEvent: LocationEvent) {
        binding.tvLat.text = "Latitude: ${locationEvent.latitude}"
        binding.tvLong.text = "Longitude: ${locationEvent.longitude}"

        db.collection(Constant.LOCATION).document(Constant.LOCATION).addSnapshotListener { it, _ ->
            viewModel.isInFence =
                    // check is user inside geofence or not
                CommonUtils.isInFence(
                    it?.get(Constant.LATITUDE).toString().toDouble(),
                    it?.get(Constant.LONGITUDE).toString().toDouble(),
                    locationEvent.latitude.toString().toDouble(),
                    locationEvent.longitude.toString().toDouble(),
                    it?.get(Constant.RADIUS).toString().toDouble(),
                )

            // when user in Profile Screen and exit the geofence area
            if (!viewModel.isInFence) {
                startActivity(
                    Intent(
                        this,
                        LocationPermissionActivity::class.java
                    ).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                )
                finishAffinity()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
    }

}