package com.livelocationtracker

import android.app.ActivityManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.Looper
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import com.google.android.gms.location.*
import com.livelocationtracker.utils.TrackingUtility
import timber.log.Timber
import java.util.concurrent.TimeUnit

class LocationTrackerService  : LifecycleService() {

    companion object {
        private val TAG = LocationTrackerService::class.java.simpleName
        private const val NOTIFICATION_CHANNEL_ID = "channel_01"
        private const val NOTIFICATION_ID = 1101
        private const val EXTRA_STARTED_FROM_NOTIFICATION = "started_from_notification"
        private const val SMALLEST_DISPLACEMENT_100_METERS = 100F
        private const val INTERVAL_TIME = 1000L
        private const val FASTEST_INTERVAL_TIME = 500L
    }

    private lateinit var locationRequest: LocationRequest
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var location: Location

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (TrackingUtility.hasLocationPermissions(this)) {
            locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult?) {
                    super.onLocationResult(locationResult)
                    onNewLocation(locationResult!!.lastLocation)
                }
            }
            createLocationRequest()
            requestLocationUpdates()
        } else {
            removeLocationUpdates()
        }

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForegroundService()
        return super.onStartCommand(intent, flags, START_NOT_STICKY)
    }


    override fun onDestroy() {
        removeLocationUpdates()
        super.onDestroy()
    }


    private fun requestLocationUpdates() {
        Toast.makeText(this, "requestLocationUpdates ", Toast.LENGTH_SHORT).show()

        Timber.tag(TAG).i("Requesting location updates")
        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest, locationCallback, Looper.getMainLooper()
            )
        } catch (unlikely: SecurityException) {
            Timber.tag(TAG).e("Lost location permission. Could not request updates. $unlikely")
        }
    }


    private fun removeLocationUpdates() {
        Toast.makeText(this, "removeLocationUpdates " , Toast.LENGTH_SHORT).show()
        Timber.tag(TAG).i("Removing location updates")
        try {
            fusedLocationClient.removeLocationUpdates(locationCallback)
            stopSelf()
        } catch (unlikely: SecurityException) {
            Timber.tag(TAG).e("Lost location permission. Could not remove updates. $unlikely")
        }
    }

    private fun onNewLocation(location: Location) {
//        Timber.tag(TAG).i("New location: $location")
        Toast.makeText(this, "Location " + location.latitude, Toast.LENGTH_SHORT).show()
        this.location = location

    }


    private fun createLocationRequest() {
        locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
            smallestDisplacement = SMALLEST_DISPLACEMENT_100_METERS
            interval = TimeUnit.SECONDS.toMillis(INTERVAL_TIME)
            fastestInterval = TimeUnit.SECONDS.toMillis(FASTEST_INTERVAL_TIME)
        }
    }


    @SuppressWarnings("deprecation")
    private fun serviceIsRunningInForeground(context: Context): Boolean {
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Integer.MAX_VALUE)) {
            if (javaClass.name == service.service.className) {
                if (service.foreground) {
                    return true
                }
            }
        }
        return false
    }


    private fun startForegroundService() {

        val notificationManager = getSystemService(NOTIFICATION_SERVICE)
                as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }
        val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setAutoCancel(false) //para que siempre este visible si lo tocamos
            .setOngoing(true) //swipe away
            .setContentTitle("Location Tracking")
            .setContentText("Keep Going......!!!")
            .setContentIntent(getMainActivity())


        startForeground(NOTIFICATION_ID, notificationBuilder.build())

    }

    private fun getMainActivity() = PendingIntent.getActivity(
        this,
        0,
        Intent(this, MainActivity::class.java),
        PendingIntent.FLAG_UPDATE_CURRENT
    )

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            EXTRA_STARTED_FROM_NOTIFICATION,
            NotificationManager.IMPORTANCE_HIGH
        )
        notificationManager.createNotificationChannel(channel)

    }


}