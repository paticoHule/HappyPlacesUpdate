package com.projects.momentosfelices.databaseRoom

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [MomentosEntity::class], version = 1)
abstract class MomentosDatabase:RoomDatabase() {

    abstract fun momentosDao(): MomentosDao

    companion object{
        @Volatile
        private var INSTANCE: MomentosDatabase? = null

        fun getInstance(context: Context): MomentosDatabase {
            synchronized(this){
                var instance = INSTANCE
                if(instance == null){
                    instance = Room.databaseBuilder(context.applicationContext,
                        MomentosDatabase::class.java,"moments-table").fallbackToDestructiveMigration().build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}