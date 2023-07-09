package com.example.worktracker.data

import com.example.worktracker.data.data_objects.dbo.LatLngYHoraActualDBO


interface AppDataSource {
    suspend fun guardarLatLngYHoraEnRoom(latLngYHoraActualDBO: LatLngYHoraActualDBO)
    suspend fun guardarRegistroLatLngEnFirestore(): Boolean
}