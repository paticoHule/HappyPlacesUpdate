package com.projects.momentosfelices.databaseRoom

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "moments-table")
data class MomentosEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val tiutlo: String = "",
    val descripcion: String = "",
    val date: String = "",
    val urlImg: String = "",
    val location: String = ""
) : Serializable
