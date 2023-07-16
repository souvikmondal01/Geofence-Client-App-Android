package com.kivous.wassuser.ui.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.kivous.wassuser.R


class LocationPermissionActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location_permission)
        locationPermission()

    }

    private fun locationPermission() {
        val backgroundLocation = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) {
            if (it) {
                startActivity(
                    Intent(
                        this,
                        AuthenticationActivity::class.java
                    ).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                )
                finishAffinity()
            } else {
                dialogBox()
            }
        }

        val locationPermissionRequest = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            when {
                permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                    backgroundLocation.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                }

                permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                    // Only approximate location access granted.
                    dialogBox()
                }

                else -> {
                    // No location access granted.
                    dialogBox()
                }
            }
        }

        locationPermissionRequest.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    private fun dialogBox() {
        val builder = MaterialAlertDialogBuilder(this)
        builder.setMessage(R.string.dialogMessage)
            .setCancelable(true)
        builder.setPositiveButton("Ok") { dialog, id ->
            openSetting()
        }
        builder.setNegativeButton("Cancel") { dialog, id ->
            finish()
        }
        val alert = builder.create()
        alert.setOnCancelListener {
            finish()
        }
        alert.show()
    }

    private fun openSetting() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val uri: Uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        startActivity(intent)
    }
}