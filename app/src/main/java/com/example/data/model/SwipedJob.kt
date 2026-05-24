package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "swiped_jobs")
data class SwipedJob(
    @PrimaryKey val jobId: Int,
    val isLiked: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)
