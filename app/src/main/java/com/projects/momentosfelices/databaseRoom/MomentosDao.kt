package com.projects.momentosfelices.databaseRoom

import androidx.room.*
import kotlinx.coroutines.flow.Flow


@Dao
interface MomentosDao {

    @Insert
    suspend fun insert(momentosEntity: MomentosEntity)

    @Update
    suspend fun update(momentosEntity: MomentosEntity)

    @Delete
    suspend fun delete(momentosEntity: MomentosEntity)

    @Query("SELECT * FROM `moments-table`")
    fun fetchAllMoments(): Flow<List<MomentosEntity>>

    @Query("SELECT * FROM `moments-table` WHERE id =:id")
    fun fetchAllMomentsById(id:Int):Flow<MomentosEntity>
}