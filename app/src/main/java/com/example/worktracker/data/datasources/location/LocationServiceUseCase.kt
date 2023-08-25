package com.example.worktracker.data.datasources.location

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.location.Location
import android.os.IBinder
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.worktracker.utils.Constants


class LocationServiceUseCase(val context: Context){
    inner class LocationServiceBroadcastReceiver: BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val location = intent.getParcelableExtra<Location>(Constants.EXTRA_LOCATION)
            Log.e("LocationServiceBroadcastReceiver", location.toString())
        }
    }

    private var locationServiceBroadcastReceiver = LocationServiceBroadcastReceiver()
    private var locationService: LocationService? = null
    private var locationServiceBound = false

    private val locationServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder = service as LocationService.LocalBinder
            locationService = binder.service
            locationServiceBound = true
            locationService?.subscribeToLocationUpdates()
        }

        override fun onServiceDisconnected(name: ComponentName) {

            locationServiceBound = false
            locationService = null
        }
    }

    fun subscribeToLocationUpdatesService() {
        val serviceIntent = Intent(context, LocationService::class.java)
        context.bindService(serviceIntent, locationServiceConnection, Context.BIND_AUTO_CREATE)
        LocalBroadcastManager.getInstance(context).registerReceiver(
            locationServiceBroadcastReceiver,
            IntentFilter(Constants.ACTION_LOCATION_BROADCAST)
        )
    }
    fun unsubscribeToLocationUpdatesService() {
        context.unbindService(locationServiceConnection)
        LocalBroadcastManager.getInstance(context).unregisterReceiver(
            locationServiceBroadcastReceiver
        )
        locationService?.unsubscribeToLocationUpdates()
    }
}





