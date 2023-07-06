package com.example.worktracker.data

import android.content.Context
import com.example.worktracker.data.daos.LatLngYHoraActualDao
import com.example.worktracker.data.data_objects.dbo.LatLngYHoraActualDBO
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*
import java.util.*

@Suppress("LABEL_NAME_CLASH")
class AppRepository(private val context: Context,
                    private val latLngYHoraActualDao: LatLngYHoraActualDao,
                    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO): AppDataSource {

    private val cloudDB = FirebaseFirestore.getInstance()

    override suspend fun guardarLatLngYHoraEnRoom(latLngYHoraActualDBO: LatLngYHoraActualDBO) {

    }
}

























