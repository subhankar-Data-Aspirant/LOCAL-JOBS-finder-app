package com.example.ui.viewmodel

import android.app.Application as AndroidApplication
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.model.Application
import com.example.data.model.Job
import com.example.data.model.SwipedJob
import com.example.data.repository.JobRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(
    application: AndroidApplication,
    private val repository: JobRepository
) : AndroidViewModel(application) {

    // Simulated local session state
    private val _userRole = MutableStateFlow("None") // None, Worker, ShopOwner
    val userRole: StateFlow<String> = _userRole.asStateFlow()

    private val _loggedInUserName = MutableStateFlow("Subho Das")
    val loggedInUserName: StateFlow<String> = _loggedInUserName.asStateFlow()

    private val _loggedInUserPhone = MutableStateFlow("+91 98765 43210")
    val loggedInUserPhone: StateFlow<String> = _loggedInUserPhone.asStateFlow()

    private val _loggedInUserLocation = MutableStateFlow("Salt Lake, Sector 2")
    val loggedInUserLocation: StateFlow<String> = _loggedInUserLocation.asStateFlow()

    private val _ownerName = MutableStateFlow("Gupta Groceries")
    val ownerName: StateFlow<String> = _ownerName.asStateFlow()

    private val _ownerContact = MutableStateFlow("Sunil Gupta")
    val ownerContact: StateFlow<String> = _ownerContact.asStateFlow()

    private val _ownerPhone = MutableStateFlow("+91 98321 09876")
    val ownerPhone: StateFlow<String> = _ownerPhone.asStateFlow()

    private val _ownerLocation = MutableStateFlow("Sector 5, Salt Lake")
    val ownerLocation: StateFlow<String> = _ownerLocation.asStateFlow()

    // Splash State
    private val _isSplashFinished = MutableStateFlow(false)
    val isSplashFinished: StateFlow<Boolean> = _isSplashFinished.asStateFlow()

    // Login State
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    // Flows from Repository
    val allJobs: StateFlow<List<Job>> = repository.allJobs.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allApplications: StateFlow<List<Application>> = repository.allApplications.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allSwipedJobs: StateFlow<List<SwipedJob>> = repository.allSwipedJobs.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Unswiped & Available jobs for worker card stack
    val availableWorkerJobs: StateFlow<List<Job>> = combine(allJobs, allSwipedJobs) { jobs, swipes ->
        val swipedIds = swipes.map { it.jobId }.toSet()
        jobs.filter { job -> job.id !in swipedIds }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Applications submitted by current worker
    val workerApplications: StateFlow<List<Application>> = combine(allApplications, _loggedInUserPhone) { apps, phone ->
        apps.filter { it.workerPhone == phone }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Jobs owned by current Shop Owner
    val ownerJobs: StateFlow<List<Job>> = combine(allJobs, _ownerName) { jobs, name ->
        jobs.filter { it.shopName.equals(name, ignoreCase = true) }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Applications received for current Owner's jobs
    val ownerReceivedApplications: StateFlow<List<Application>> = combine(allApplications, ownerJobs) { apps, myJobs ->
        val myJobIds = myJobs.map { it.id }.toSet()
        apps.filter { it.jobId in myJobIds }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        prepopulateJobsIfEmpty()
    }

    private fun prepopulateJobsIfEmpty() {
        viewModelScope.launch {
            repository.allJobs.collect { jobs ->
                if (jobs.isEmpty()) {
                    val defaultJobs = listOf(
                        Job(
                            title = "Store Assistant",
                            shopName = "Gupta Groceries",
                            description = "Help with sorting and packing fresh grocery shipments. Fast checkout support needed as well. Friendly environment, on-the-spot cash payment.",
                            payment = "₹150 / Hr",
                            workingHours = "4 PM - 8 PM (Today)",
                            location = "Sector 2, Salt Lake (0.2 km)",
                            phone = "+919832109876",
                            workersNeeded = 2,
                            isUrgent = true
                        ),
                        Job(
                            title = "Cafe Barista Helper",
                            shopName = "The Brew Room",
                            description = "Looking for a welcoming barista helper to secure orders, rinse mugs, serve sweet bakes, and brew standard filter coffee. Coffee lover preferred!",
                            payment = "₹220 / Hr",
                            workingHours = "5 PM - 10 PM (5 Hrs)",
                            location = "Sector 1 Cafe Street (1.1 km)",
                            phone = "+919900998877",
                            workersNeeded = 1,
                            isUrgent = false
                        ),
                        Job(
                            title = "Urgent Delivery Rider",
                            shopName = "Hot & Spicy Pizza",
                            description = "Require an urgent rider to handle peak dinner rush deliveries in the Salt Lake Sector 5 area. Must bring own two-wheeler and safety helmet. Fuel allowance included.",
                            payment = "₹90 / Delivery + Tips",
                            workingHours = "7 PM - 11 PM (4 Hrs)",
                            location = "Sector 5 Mall Road (0.8 km)",
                            phone = "+919876543210",
                            workersNeeded = 2,
                            isUrgent = true
                        ),
                        Job(
                            title = "Weekend Promoter",
                            shopName = "Nutan Apparel Outlet",
                            description = "Promote our new apparel fashion line near the main entrance mall plaza. Distribute printed leaflets, share coupon codes, and guide shoppers to our stalls.",
                            payment = "₹1,000 / Day",
                            workingHours = "11 AM - 8 PM (Sat-Sun)",
                            location = "Milan Mela Grounds (2.4 km)",
                            phone = "+919123456789",
                            workersNeeded = 4,
                            isUrgent = true
                        ),
                        Job(
                            title = "Pet Bathing Assistant",
                            shopName = "Happy Paws Dog Salon",
                            description = "Energetic assistant wanted to bathe, pet-dry, and hold dogs during professional grooming. Should love pets and have clean, patient handling skills.",
                            payment = "₹180 / Hr",
                            workingHours = "1 PM - 6 PM (5 Hrs)",
                            location = "Block EC, Salt Lake (1.5 km)",
                            phone = "+919001122334",
                            workersNeeded = 1,
                            isUrgent = false
                        )
                    )
                    defaultJobs.forEach { repository.insertJob(it) }
                }
            }
        }
    }

    // Navigation and Session Control
    fun finishSplash() {
        _isSplashFinished.value = true
    }

    fun login(role: String, name: String, phone: String) {
        _userRole.value = role
        if (role == "Worker") {
            _loggedInUserName.value = name
            _loggedInUserPhone.value = phone
        } else {
            _ownerName.value = name
            _ownerPhone.value = phone
        }
        _isLoggedIn.value = true
    }

    fun setRole(role: String) {
        _userRole.value = role
    }

    fun logout() {
        _isLoggedIn.value = false
        _userRole.value = "None"
    }

    fun updateWorkerProfile(name: String, phone: String, location: String) {
        _loggedInUserName.value = name
        _loggedInUserPhone.value = phone
        _loggedInUserLocation.value = location
    }

    fun updateOwnerProfile(shopName: String, contact: String, phone: String, location: String) {
        _ownerName.value = shopName
        _ownerContact.value = contact
        _ownerPhone.value = phone
        _ownerLocation.value = location
    }

    // Job Operations
    fun postJob(
        title: String,
        description: String,
        payment: String,
        workingHours: String,
        location: String,
        workersNeeded: Int,
        phone: String,
        isUrgent: Boolean
    ) {
        viewModelScope.launch {
            val job = Job(
                title = title,
                shopName = _ownerName.value,
                description = description,
                payment = payment,
                workingHours = workingHours,
                location = location,
                phone = phone,
                workersNeeded = workersNeeded,
                isUrgent = isUrgent
            )
            repository.insertJob(job)
        }
    }

    fun deleteJob(job: Job) {
        viewModelScope.launch {
            repository.deleteJob(job)
        }
    }

    // Swiping Gestures Handling
    fun performSwipe(jobId: Int, isLiked: Boolean) {
        viewModelScope.launch {
            repository.insertSwipedJob(SwipedJob(jobId, isLiked))
            if (isLiked) {
                // If Liked, apply to job instantly
                applyForJob(jobId)
            }
        }
    }

    fun applyForJob(jobId: Int) {
        viewModelScope.launch {
            val job = repository.getJobById(jobId) ?: return@launch
            
            // Check if application already exists to avoid redundant insertions
            val exists = workerApplications.value.any { it.jobId == jobId }
            if (!exists) {
                val app = Application(
                    jobId = jobId,
                    jobTitle = job.title,
                    shopName = job.shopName,
                    workerName = _loggedInUserName.value,
                    workerPhone = _loggedInUserPhone.value,
                    status = "Applied"
                )
                repository.insertApplication(app)
            }
        }
    }

    fun updateApplicationStatus(applicationId: Int, newStatus: String) {
        viewModelScope.launch {
            val app = allApplications.value.find { it.id == applicationId } ?: return@launch
            val updated = app.copy(status = newStatus)
            repository.updateApplication(updated)
        }
    }

    fun deleteApplication(applicationId: Int) {
        viewModelScope.launch {
            repository.deleteApplicationById(applicationId)
        }
    }

    fun resetAllSwipesAndApplications() {
        viewModelScope.launch {
            repository.clearSwipes()
            // Keep user created jobs, but we can clear swipes easily
        }
    }

    // Factory to construct instances of MainViewModel cleanly
    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>,
                extras: CreationExtras
            ): T {
                val application = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY])
                val database = AppDatabase.getDatabase(application)
                val repository = JobRepository(database.jobDao())
                return MainViewModel(application, repository) as T
            }
        }
    }
}
