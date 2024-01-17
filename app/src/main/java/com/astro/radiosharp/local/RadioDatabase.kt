package com.astro.radiosharp.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.astro.radiosharp.model.FavClass
import com.astro.radiosharp.model.RadioClass


//Aktivierung eines neuen Room-Table für die Favoriten-Liste:
//"Auslagerung der Favoriten_Liste in ein seperates Table (FavClass) um
// mögliche Bugs & Fehlerquellen zu vermeiden."
@Database(entities = [RadioClass::class, FavClass::class], version = 2) // Annotation sorgt für die Markierung
// der Abstrakten Klasse mit der der Room Database.
abstract class RadioDatabase : RoomDatabase() {// Die abstrakte Klasse RadioDatabase erbt von RoomDatabase


    abstract val radioDatabaseDao: RadioDatabaseDao
    // Hier wird der Datentyp RadioDatabaseDao deklariert, um von der Repositoty darauf zuzugreifen.

}

private lateinit var INSTANCE: RadioDatabase

fun getDatabase(context: Context): RadioDatabase {
    synchronized(RadioDatabase::class.java) {
        if (!::INSTANCE.isInitialized) { // Hier wird überprüft ob die Instanz schon initialisiert wurde,
            // wenn nicht wird sie initialisiert.
            INSTANCE = Room.databaseBuilder( // Hier wird die Datenbank mit hilfe des builders erstellt und initialisiert.
                context.applicationContext,
                RadioDatabase::class.java, "RadioDatabase"
            )
                .fallbackToDestructiveMigration() // Wenn die Datenbankversion erhöht wird und es keine passende Migration gibt.
                // Dann werden alle Tabellen neu erstellt.
                .build()
        }
    }
    return INSTANCE // wenn die Instanz initialisiert wurde geben wir sie zurück,
// wenn nicht wird sie zuerst erstellt und dann zurückgegeben.
}



