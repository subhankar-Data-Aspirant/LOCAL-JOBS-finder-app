package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "applications")
data class Application(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val jobId: Int,
    val jobTitle: String,
    val shopName: String,
    val workerName: String,
    val workerPhone: String,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "Applied" // Applied, Accepted, Contacted, Rejected
)
