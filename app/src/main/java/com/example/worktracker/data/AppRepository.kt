package com.example.worktracker.data

import android.content.Context
import android.util.Log
import com.example.worktracker.R
import com.example.worktracker.data.daos.LatLngYHoraActualDao
import com.example.worktracker.data.data_objects.dbo.LatLngYHoraActualDBO
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*
import java.time.LocalDate
import java.util.*


class AppRepository(
    private val context: Context,
    private val latLngYHoraActualDao: LatLngYHoraActualDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
): AppDataSource {

    private val cloudDB = FirebaseFirestore.getInstance()

    override suspend fun guardarLatLngYHoraEnRoom(
        latLngYHoraActualDBO: LatLngYHoraActualDBO
    ) {
        withContext(ioDispatcher){
            latLngYHoraActualDao.guardarLatLngYHoraActual(latLngYHoraActualDBO)
        }
    }

    override suspend fun guardarRegistroLatLngEnFirestore():Boolean = withContext(ioDispatcher){

        val deferred = CompletableDeferred<Boolean>()

        val date = LocalDate.now().toString()
        val lista = latLngYHoraActualDao.obtenerLatLngYHoraActuales()
        val data = hashMapOf(
            "fecha" to date,
            "recorridoDelDia" to lista
        )

        cloudDB.collection("RegistroDeJornada")
            .document()
            .set(data)
            .addOnFailureListener{
                deferred.complete(false)
            }
            .addOnSuccessListener{
                CoroutineScope(ioDispatcher).launch {
                    latLngYHoraActualDao.eliminarLatLngYHoraActuales()
                }
                deferred.complete(true)
            }
        return@withContext deferred.await()
    }
}

























