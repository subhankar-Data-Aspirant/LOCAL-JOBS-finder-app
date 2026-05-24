package com.example.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Job
import com.example.data.model.Application
import com.example.data.model.SwipedJob
import kotlinx.coroutines.flow.Flow

@Dao
interface JobDao {

    // Jobs
    @Query("SELECT * FROM jobs ORDER BY timestamp DESC")
    fun getAllJobsFlow(): Flow<List<Job>>

    @Query("SELECT * FROM jobs WHERE id = :jobId LIMIT 1")
    suspend fun getJobById(jobId: Int): Job?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJob(job: Job): Long

    @Delete
    suspend fun deleteJob(job: Job)

    @Query("DELETE FROM jobs")
    suspend fun deleteAllJobs()

    // Applications
    @Query("SELECT * FROM applications ORDER BY timestamp DESC")
    fun getAllApplicationsFlow(): Flow<List<Application>>

    @Query("SELECT * FROM applications WHERE jobId = :jobId ORDER BY timestamp DESC")
    fun getApplicationsForJobFlow(jobId: Int): Flow<List<Application>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApplication(application: Application): Long

    @Update
    suspend fun updateApplication(application: Application)

    @Query("DELETE FROM applications WHERE id = :applicationId")
    suspend fun deleteApplicationById(applicationId: Int)

    // Swiped Jobs
    @Query("SELECT * FROM swiped_jobs")
    fun getAllSwipedJobsFlow(): Flow<List<SwipedJob>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSwipedJob(swipedJob: SwipedJob)

    @Query("DELETE FROM swiped_jobs")
    suspend fun clearAllSwipedJobs()
}
