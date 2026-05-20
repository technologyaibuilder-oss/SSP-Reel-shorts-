package com.example.data

import android.content.Context
import androidx.room.Room

object DatabaseProvider {
    private var database: AppDatabase? = null
    private var repository: AppRepository? = null

    fun getDatabase(context: Context): AppDatabase {
        return database ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "ssp_reel_shorts_db"
            ).fallbackToDestructiveMigration().build()
            database = instance
            instance
        }
    }

    fun getRepository(context: Context): AppRepository {
        return repository ?: synchronized(this) {
            val dao = getDatabase(context).dao()
            val repo = AppRepository(dao)
            repository = repo
            repo
        }
    }
}
