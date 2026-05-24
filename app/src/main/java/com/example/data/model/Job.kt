package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "jobs")
data class Job(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val shopName: String,
    val description: String,
    val payment: String,
    val workingHours: String,
    val location: String,
    val phone: String,
    val workersNeeded: Int,
    val isUrgent: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)
