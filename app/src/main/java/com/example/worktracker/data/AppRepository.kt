package com.example.worktracker.data

import android.content.Context
import com.example.worktracker.R
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
        withContext(ioDispatcher){
            latLngYHoraActualDao.guardarLatLngYHoraActual(latLngYHoraActualDBO)
        }
    }

    override suspend fun guardarRegistroLatLngEnFirestore(): Pair<Boolean, Int> = withContext(ioDispatcher){
        withContext(ioDispatcher){
            val date = Calendar.getInstance().time.toString()
            val deferred = CompletableDeferred<Pair<Boolean,Int>>()
            cloudDB.collection("RegistroDeJornada")
                .document(date)
                .set(latLngYHoraActualDao.obtenerLatLngYHoraActuales())
                .addOnFailureListener{
                    deferred.complete(Pair(false, R.string.registro_guardado_fail))
                }
                .addOnSuccessListener{
                    latLngYHoraActualDao.eliminarLatLngYHoraActuales()
                    deferred.complete(Pair(true, R.string.registro_guardado_exito))
                }
            return@withContext deferred.await()
        }
    }
}

























