package com.rameez.hel.data

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.rameez.hel.data.model.WIPModel
import com.rameez.hel.utils.ApplicationClass

@Database(entities = [WIPModel::class], version = 1)
@TypeConverters(Converters::class)
abstract class WIPDatabase : RoomDatabase() {

    abstract fun wipDao(): WIPDao

    companion object {

        private var INSTANCE: WIPDatabase? = null
        fun getDatabase(): WIPDatabase? {
            if (INSTANCE == null) {
                synchronized(this) {
                    INSTANCE = Room.databaseBuilder(
                        ApplicationClass.application.baseContext,
                        WIPDatabase::class.java,
                        "wips_database"
                    ).build()
                }
            }
            return INSTANCE
        }
    }
}