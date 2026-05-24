package com.example.data.repository

import com.example.data.database.JobDao
import com.example.data.model.Job
import com.example.data.model.Application
import com.example.data.model.SwipedJob
import kotlinx.coroutines.flow.Flow

class JobRepository(private val jobDao: JobDao) {

    val allJobs: Flow<List<Job>> = jobDao.getAllJobsFlow()
    
    val allApplications: Flow<List<Application>> = jobDao.getAllApplicationsFlow()
    
    val allSwipedJobs: Flow<List<SwipedJob>> = jobDao.getAllSwipedJobsFlow()

    suspend fun getJobById(jobId: Int): Job? {
        return jobDao.getJobById(jobId)
    }

    suspend fun insertJob(job: Job): Long {
        return jobDao.insertJob(job)
    }

    suspend fun deleteJob(job: Job) {
        jobDao.deleteJob(job)
    }

    suspend fun insertApplication(application: Application): Long {
        return jobDao.insertApplication(application)
    }

    suspend fun updateApplication(application: Application) {
        jobDao.updateApplication(application)
    }

    suspend fun deleteApplicationById(applicationId: Int) {
        jobDao.deleteApplicationById(applicationId)
    }

    suspend fun insertSwipedJob(swipedJob: SwipedJob) {
        jobDao.insertSwipedJob(swipedJob)
    }

    suspend fun clearSwipes() {
        jobDao.clearAllSwipedJobs()
    }

    fun getApplicationsForJob(jobId: Int): Flow<List<Application>> {
        return jobDao.getApplicationsForJobFlow(jobId)
    }
}
