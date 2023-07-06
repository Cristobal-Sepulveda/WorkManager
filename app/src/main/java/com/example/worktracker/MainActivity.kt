package com.example.worktracker

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.content.SharedPreferences
import android.location.Location
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.rememberScaffoldState
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.worktracker.ui.theme.WorkTrackerTheme
import androidx.compose.ui.res.colorResource
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.worktracker.data.AppDataSource
import com.example.worktracker.data.data_objects.dbo.LatLngYHoraActualDBO
import com.example.worktracker.utils.Constants
import com.example.worktracker.utils.Constants.ACTION_LOCATION_BROADCAST
import com.example.worktracker.utils.LocationService
import com.example.worktracker.utils.mostrarSnackBarEnMainThread
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import java.time.LocalTime

class MainActivity : ComponentActivity() {
    private val appDataSource: AppDataSource by inject()
    private var locationServiceBroadcastReceiver = LocationServiceBroadcastReceiver()
    private var locationService: LocationService? = null
    private var locationServiceBound = false

    private inner class LocationServiceBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val location = intent.getParcelableExtra<Location>(Constants.EXTRA_LOCATION)
            val phoneCurrentHour = LocalTime.now().toString()
            lifecycleScope.launch(Dispatchers.IO){
                appDataSource.guardarLatLngYHoraEnRoom(
                    LatLngYHoraActualDBO(
                        location!!.latitude,
                        location.longitude,
                        phoneCurrentHour
                    )
                )
            }
        }
    }

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

    private suspend fun guardarRegistroLatLngEnFirestore(){
        locationService?.unsubscribeToLocationUpdates()
        val task = appDataSource.guardarRegistroLatLngEnFirestore()
        Log.e("MainActivity", "guardarRegistroLatLngEnFirestore: ${task.second}")
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
                    onStopClick = {
                        lifecycleScope.launch(Dispatchers.IO){
                            guardarRegistroLatLngEnFirestore()
                        }
                    },
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

}

@Composable
fun Greeting(onStartClick: () -> Unit, onPauseClick: () -> Unit, onStopClick: () -> Unit, ) {
    val startButtonEnabled = remember { mutableStateOf(true) }
    val scaffoldState: ScaffoldState = rememberScaffoldState()
    val coroutineScope: CoroutineScope = rememberCoroutineScope()

    Scaffold(
        scaffoldState = scaffoldState,
        modifier = Modifier.fillMaxSize(),
        content = {padding ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .background(colorResource(id = R.color.purple_700)),
            ) {
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "WorkTracker",
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(vertical = 48.dp)
                )

                Spacer(modifier = Modifier.weight(1f))

                /*Start Button*/
                Button(
                    onClick = {
                        startButtonEnabled.value = false
                        onStartClick()
                        coroutineScope.launch {
                            scaffoldState.snackbarHostState.showSnackbar(
                                message = "El registro de ruta ha comenzado",
                                duration = SnackbarDuration.Short
                            )
                        }
                    },
                    enabled = startButtonEnabled.value
                ) { Text("Start") }

                /*Pause Button*/
                Button(
                    onClick = {
                        startButtonEnabled.value = true
                        onPauseClick()
                    },
                    enabled = !startButtonEnabled.value
                ) { Text("Pause") }

                /*Stop Button*/
                Button(
                    onClick = {
                        startButtonEnabled.value = true
                        onStopClick()
                    },
                    enabled = !startButtonEnabled.value
                ) { Text("Stop") }

            }
        },
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    WorkTrackerTheme {
        Greeting(
            onStartClick = { /* Start logic */ },
            onPauseClick = { /* Pause logic */ },
            onStopClick = { /* Stop logic */ },
        )
    }
}

/*
@Preview(showBackground = true)
@Composable
fun DisplaySnackBar(){
    val scaffoldState: ScaffoldState = rememberScaffoldState()
    val coroutineScope: CoroutineScope = rememberCoroutineScope()

    Scaffold(scaffoldState = scaffoldState){
        Button(onClick = {
            coroutineScope.launch {
                scaffoldState.snackbarHostState.showSnackbar(
                    message = "El registro de ruta ha comenzado",
                    duration = SnackbarDuration.Long
                )
            }
            Text(text = "Show SnackBar")
        }
    }
}*/
