package com.example.worktracker

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.location.Location
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.worktracker.ui.theme.WorkTrackerTheme
import androidx.compose.ui.res.colorResource
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.worktracker.data.AppDataSource
import com.example.worktracker.data.data_objects.dbo.LatLngYHoraActualDBO
import com.example.worktracker.utils.Constants
import com.example.worktracker.utils.Constants.ACTION_LOCATION_BROADCAST
import com.example.worktracker.utils.LocationService
import org.koin.android.ext.android.inject
import java.time.LocalTime

class MainActivity : ComponentActivity() {
    private val appDataSource: AppDataSource by inject()

    private var startButtonEnabled = true
    private var locationServiceBroadcastReceiver = LocationServiceBroadcastReceiver()
    private var locationService: LocationService? = null
    private var locationServiceBound = false

    private val locationServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder = service as LocationService.LocalBinder
            locationService = binder.service
            locationServiceBound = true
        }

        override fun onServiceDisconnected(name: ComponentName) {
            locationService = null
            locationServiceBound = false
        }
    }

    private fun subscribeToLocationUpdatesService() {
        locationService?.subscribeToLocationUpdates()
    }

    private fun unsubscribeToLocationUpdatesService() {
        locationService?.unsubscribeToLocationUpdates()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val serviceIntent = Intent(this, LocationService::class.java)
        bindService(serviceIntent, locationServiceConnection, Context.BIND_AUTO_CREATE)
        LocalBroadcastManager.getInstance(this).registerReceiver(
            locationServiceBroadcastReceiver,
            IntentFilter(ACTION_LOCATION_BROADCAST)
        )

        setContent {
            WorkTrackerTheme {
                Greeting(
                    onStartClick = {subscribeToLocationUpdatesService()},
                    onPauseClick = {unsubscribeToLocationUpdatesService()},
                    onStopClick = {},
                    startButtonEnabled = startButtonEnabled
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(
            locationServiceBroadcastReceiver
        )
        if (locationServiceBound) {
            unbindService(locationServiceConnection)
            locationServiceBound = false
        }
    }

    private inner class LocationServiceBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val location = intent.getParcelableExtra<Location>(Constants.EXTRA_LOCATION)
            val phoneCurrentHour = LocalTime.now().toString()
            Log.e("LocationServiceBroadcastReceiver",
                LatLngYHoraActualDBO(
                    location!!.latitude,
                    location.longitude,
                    phoneCurrentHour).toString()
            )
            /*lifecycleScope.launch(Dispatchers.IO){
                appDataSource.guardarGeoPointEnRoom(
                    GeoPointYHoraActualDBO(
                        geoPoint,
                        phoneCurrentHour
                    )
                )
            }*/
        }
    }
}


@Composable
fun Greeting(
    onStartClick: () -> Unit,
    onPauseClick: () -> Unit,
    onStopClick: () -> Unit,
    startButtonEnabled: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .background(colorResource(id = R.color.purple_700)),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "WorkTracker",
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.padding(bottom = 48.dp)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp, start = 24.dp, end = 24.dp)
                    .wrapContentHeight()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Button(
                        onClick = { onStartClick() },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("Start")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = { onPauseClick() },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("Pause")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = { /* TODO: Stop logic */ },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Stop")
                    }
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    WorkTrackerTheme {
        Greeting(
            onStartClick = { /* Start logic */ },
            onPauseClick = { /* Pause logic */ },
            onStopClick = { /* Stop logic */ },
            startButtonEnabled = true
        )
    }
}