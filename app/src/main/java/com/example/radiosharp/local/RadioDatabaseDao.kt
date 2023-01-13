package com.example.radiosharp.local

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.radiosharp.model.RadioClass

@Dao
interface RadioDatabaseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(station: List<RadioClass>)

    @Query("SELECT*FROM RadioClass")
    fun getAll(): LiveData<List<RadioClass>>

    @Delete
    suspend fun delete(station:RadioClass)

    @Query("DELETE FROM RadioClass")
    suspend fun deleteAll()

    @Update
    suspend fun update(station:RadioClass)

    @Query ("SELECT * FROM RadioClass WHERE favorite = 1")
    fun getFav(): LiveData<List<RadioClass>>

    //Favoriten auf "null" setzen
    // UPDATE RadioClass SET favorite = 0 WHERE favorite = 1

}