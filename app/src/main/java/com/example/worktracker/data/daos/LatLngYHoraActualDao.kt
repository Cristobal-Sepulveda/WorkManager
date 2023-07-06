package com.example.worktracker.data.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.worktracker.data.data_objects.dbo.LatLngYHoraActualDBO

@Dao
interface LatLngYHoraActualDao{

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun guardarLatLngYHoraActual(latLngYHoraActualDBO: LatLngYHoraActualDBO)

    @Query("select * from LatLngYHoraActualDBO")
    fun obtenerLatLngYHoraActuales(): List<LatLngYHoraActualDBO>

    @Query("delete from LatLngYHoraActualDBO")
    fun eliminarLatLngYHoraActuales()

}