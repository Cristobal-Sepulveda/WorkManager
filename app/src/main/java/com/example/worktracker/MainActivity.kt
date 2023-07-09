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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.rememberScaffoldState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.modifier.modifierLocalConsumer
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
                    appDataSource
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
fun Greeting(onStartClick: () -> Unit,
             onPauseClick: () -> Unit,
             appDataSource: AppDataSource?) {

    val startButtonEnabled = remember { mutableStateOf(true) }
    val scaffoldState: ScaffoldState = rememberScaffoldState()
    val coroutineScope: CoroutineScope = rememberCoroutineScope()
    val showAlert = remember { mutableStateOf(false) }

    fun guardarRegistroLatLngEnFirestore(){
        coroutineScope.launch(Dispatchers.IO) {
            val task = appDataSource!!.guardarRegistroLatLngEnFirestore()

            if(!task){
                coroutineScope.launch {
                    scaffoldState.snackbarHostState.showSnackbar(
                        message = "Error. Intentelo nuevamente.",
                        duration = SnackbarDuration.Short
                    )
                }
            }else{
                onPauseClick()
                showAlert.value = false
                startButtonEnabled.value = true
                coroutineScope.launch {
                    scaffoldState.snackbarHostState.showSnackbar(
                        message = "Registro guardado exitosamente",
                        duration = SnackbarDuration.Short
                    )
                }
            }
        }
    }

    if (showAlert.value) {
        AlertDialog(
            title = { Text("Atención") },
            text = { Text("Si confirmas, tu recorrido se borrará del celular y será enviado a la nube.")},
            onDismissRequest = { showAlert.value = false },
            confirmButton = {
                Button(
                    onClick = {
                        guardarRegistroLatLngEnFirestore()
                    },
                    content = { Text("OK") }
                )
            }
        )
    }

    Scaffold(
        scaffoldState = scaffoldState,
        modifier = Modifier.fillMaxSize(),
        floatingActionButton = {
            if(!startButtonEnabled.value) {
                Column{
                    /*FAB Stop*/
                    FloatingActionButton(
                        onClick = {
                            showAlert.value = true
                        },
                        modifier = Modifier.padding(0.dp, 8.dp)
                    ) {
                        Icon(Icons.Default.Stop,"")
                    }
                    /*FAB Pause*/
                    FloatingActionButton(
                        onClick = {
                            onPauseClick()
                            startButtonEnabled.value = true
                            coroutineScope.launch {
                                scaffoldState.snackbarHostState.showSnackbar(
                                    message = "El registro de ruta se ha pausado",
                                    duration = SnackbarDuration.Short
                                )
                            }
                        },
                        modifier = Modifier.padding(0.dp, 48.dp)
                    ) {
                        Icon(Icons.Default.Pause, "")
                    }
                }
            }else{
                FloatingActionButton(
                    onClick = {
                        onStartClick()
                        startButtonEnabled.value = false
                        coroutineScope.launch {
                            scaffoldState.snackbarHostState.showSnackbar(
                                message = "El registro de ruta ha comenzado",
                                duration = SnackbarDuration.Short
                            )
                        }
                    },
                    modifier = Modifier.padding(0.dp, 48.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, "Localized description")
                }
            }
        },
        content = { contentPadding ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding)
                    .background(colorResource(id = R.color.background)),
            ) {
                Text(
                    text = "WorkTracker",
                    modifier = Modifier.padding(vertical = 48.dp)
                )
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
            null
        )
    }
}
