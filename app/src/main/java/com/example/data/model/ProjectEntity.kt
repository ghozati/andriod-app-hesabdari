package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val status: String = STATUS_ACTIVE // "Active", "Archived"
) {
    companion object {
        const val STATUS_ACTIVE = "Active"
        const val STATUS_ARCHIVED = "Archived"
    }
}
