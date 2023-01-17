package com.example.radiosharp.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.radiosharp.model.RadioClass


@Database(entities = [RadioClass::class], version = 2)
abstract class RadioDatabase : RoomDatabase() {

    abstract val radioDatabaseDao: RadioDatabaseDao

}

private lateinit var INSTANCE: RadioDatabase

fun getDatabase(context: Context): RadioDatabase {
    synchronized(RadioDatabase::class.java) {
        if (!::INSTANCE.isInitialized) {
            INSTANCE = Room.databaseBuilder(
                context.applicationContext,
                RadioDatabase::class.java, "RadioDatabase")
                .fallbackToDestructiveMigration()
                .build()
        }
    }
    return INSTANCE
}



