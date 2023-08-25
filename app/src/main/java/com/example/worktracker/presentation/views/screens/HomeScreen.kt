package com.example.worktracker.presentation.views.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.worktracker.data.datasources.location.LocationServiceUseCase
@Composable
fun HomeScreen() {
    val serviceStatus = remember{ mutableStateOf(false) }
    val buttonValue = remember { mutableStateOf("Start Service") }
    val locationServiceBroadcastReceiver = LocationServiceUseCase(LocalContext.current)

    DisposableEffect(Unit){
        onDispose {
            locationServiceBroadcastReceiver.unsubscribeToLocationUpdatesService()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .background(color = Color.Black),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ){
        Button(
            modifier = Modifier
                .padding(bottom = 24.dp),
            onClick = {
                startOrStopLocationService(
                    locationServiceBroadcastReceiver,
                    serviceStatus,
                    buttonValue,
                )
            }
        ) {
            Text(
                text = buttonValue.value,
                modifier = Modifier.padding(10.dp),
                color = Color.White,
                fontSize = 20.sp
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen()
}

private fun startOrStopLocationService(
    locationServiceUseCase: LocationServiceUseCase,
    serviceStatus: MutableState<Boolean>,
    buttonValue: MutableState<String>,
) {
    if (serviceStatus.value) {
        serviceStatus.value = !serviceStatus.value
        buttonValue.value = "Start Service"
        locationServiceUseCase.unsubscribeToLocationUpdatesService()
    } else {
        serviceStatus.value = !serviceStatus.value
        buttonValue.value = "Stop Service"
        locationServiceUseCase.subscribeToLocationUpdatesService()
    }
}
