package com.example.radiosharp.local

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.radiosharp.model.FavClass
import com.example.radiosharp.model.RadioClass

@Dao
interface RadioDatabaseDao {

//============================================================
// -------------- DATABASE: "RadioClass" --------------
//============================================================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(station: MutableList<RadioClass>)

    @Query("SELECT*FROM RadioClass")
    fun getAll(): LiveData<MutableList<RadioClass>>

    @Delete
    suspend fun delete(station:RadioClass)

    @Query("DELETE FROM RadioClass")
    suspend fun deleteAll()

//============================================================
// -------------- DATABASE: "FavClass" --------------
//============================================================

    @Query ("SELECT * FROM FavClass")
    suspend fun getAllFav(): MutableList<FavClass>

    @Query ("SELECT * FROM FavClass WHERE name LIKE '%' || :name || '%'")
    suspend fun getAllFavByName(name:String): MutableList<FavClass>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFav(station: FavClass)

    @Delete
    suspend fun deleteFav(station:FavClass)

    @Query("DELETE FROM FavClass")
    suspend fun deleteAllFav()

//    @Update
//    suspend fun updateFav(favoritesList: LiveData<MutableList<RadioClass>>)

//    @Query ("DELETE FROM RadioClass WHERE favorite = 1")
//    fun deleteNotFav(): LiveData<MutableList<RadioClass>>

//    Favoriten auf "null" setzen
//    UPDATE RadioClass SET favorite = 0 WHERE favorite = 0

}