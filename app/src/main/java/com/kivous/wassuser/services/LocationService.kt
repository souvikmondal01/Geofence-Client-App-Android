package com.kivous.wassuser.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.location.Location
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.kivous.wassuser.R
import com.kivous.wassuser.models.LocationEvent
import com.kivous.wassuser.utils.CommonUtils.db
import com.kivous.wassuser.utils.CommonUtils.isInFence
import com.kivous.wassuser.utils.Constant.CHANNEL_ID
import com.kivous.wassuser.utils.Constant.INSIDE_GEOFENCE_MSG
import com.kivous.wassuser.utils.Constant.LATITUDE
import com.kivous.wassuser.utils.Constant.LOCATION
import com.kivous.wassuser.utils.Constant.LOCATION_STATUS
import com.kivous.wassuser.utils.Constant.LONGITUDE
import com.kivous.wassuser.utils.Constant.NOTIFICATION_ID1
import com.kivous.wassuser.utils.Constant.NOTIFICATION_ID2
import com.kivous.wassuser.utils.Constant.OUTSIDE_GEOFENCE_MSG
import com.kivous.wassuser.utils.Constant.RADIUS
import org.greenrobot.eventbus.EventBus


class LocationService : Service() {
    private var fusedLocationProviderClient: FusedLocationProviderClient? = null
    private var locationCallback: LocationCallback? = null
    private var locationRequest: LocationRequest? = null
    private var notificationManager: NotificationManager? = null
    private var location: Location? = null

    override fun onCreate() {
        super.onCreate()

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        locationRequest =
            LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000).setIntervalMillis(500)
                .build()
        locationCallback = object : LocationCallback() {
            override fun onLocationAvailability(p0: LocationAvailability) {
                super.onLocationAvailability(p0)
            }

            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                onNewLocation(locationResult)
            }
        }

        notificationManager = this.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val notificationChannel =
            NotificationChannel(CHANNEL_ID, "locations", NotificationManager.IMPORTANCE_HIGH)
        notificationManager?.createNotificationChannel(notificationChannel)

    }

    @Suppress("MissingPermission")
    private fun createLocationRequest() {
        try {
            fusedLocationProviderClient?.requestLocationUpdates(
                locationRequest!!, locationCallback!!, null
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun removeLocationUpdates() {
        locationCallback?.let {
            fusedLocationProviderClient?.removeLocationUpdates(it)
        }
        stopForeground(true)
        stopSelf()
    }

    private var check = false
    private var check2 = false
    private fun onNewLocation(locationResult: LocationResult) {
        location = locationResult.lastLocation
        EventBus.getDefault().post(
            LocationEvent(
                latitude = location?.latitude,
                longitude = location?.longitude
            )
        )
        db.collection(LOCATION).document(LOCATION).addSnapshotListener { it, _ ->

            val isInFence =
                isInFence(
                    it?.get(LATITUDE).toString().toDouble(),
                    it?.get(LONGITUDE).toString().toDouble(),
                    location?.latitude.toString().toDouble(),
                    location?.longitude.toString().toDouble(),
                    it?.get(RADIUS).toString().toDouble(),
                )

            if (check != isInFence) {
                check = isInFence
                if (check) {
                    startForeground(NOTIFICATION_ID1, notificationWhenInsideFence())
                } else {
                    startForeground(NOTIFICATION_ID2, notificationWhenOutsideFence())
                }
            }

            if (check2 == isInFence) {
                check2 = !isInFence
                if (check2) {
                    startForeground(NOTIFICATION_ID2, notificationWhenOutsideFence())
                } else {
                    startForeground(NOTIFICATION_ID1, notificationWhenInsideFence())
                }
            }

        }

    }

    private fun notificationWhenInsideFence(): Notification {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(LOCATION_STATUS)
            .setContentText(
                INSIDE_GEOFENCE_MSG
            )
            .setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOngoing(true)
        notification.setChannelId(CHANNEL_ID)
        return notification.build()
    }

    private fun notificationWhenOutsideFence(): Notification {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(LOCATION_STATUS)
            .setContentText(
                OUTSIDE_GEOFENCE_MSG
            )
            .setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOngoing(true)
        notification.setChannelId(CHANNEL_ID)
        return notification.build()
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        createLocationRequest()
        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        removeLocationUpdates()
    }


}