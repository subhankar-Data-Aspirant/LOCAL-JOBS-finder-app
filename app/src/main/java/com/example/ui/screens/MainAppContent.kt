package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.model.Application
import com.example.data.model.Job
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContent(viewModel: MainViewModel) {
    val navController = rememberNavController()
    val isSplashFinished by viewModel.isSplashFinished.collectAsStateWithLifecycle()
    val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()
    val userRole by viewModel.userRole.collectAsStateWithLifecycle()

    // Control structural routing when session changes
    LaunchedEffect(isSplashFinished, isLoggedIn, userRole) {
        if (!isSplashFinished) {
            navController.navigate("splash") {
                popUpTo(0) { inclusive = true }
            }
        } else if (!isLoggedIn) {
            navController.navigate("login") {
                popUpTo(0) { inclusive = true }
            }
        } else if (userRole == "None") {
            navController.navigate("role_selection") {
                popUpTo(0) { inclusive = true }
            }
        } else if (userRole == "Worker") {
            navController.navigate("worker_home") {
                popUpTo(0) { inclusive = true }
            }
        } else if (userRole == "ShopOwner") {
            navController.navigate("owner_dashboard") {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = "splash",
        modifier = Modifier.fillMaxSize()
    ) {
        // 1. Splash Screen
        composable("splash") {
            SplashScreen(onFinish = { viewModel.finishSplash() })
        }

        // 2. Login Screen
        composable("login") {
            LoginScreen(
                onLogin = { name, phone, role ->
                    viewModel.login(role, name, phone)
                }
            )
        }

        // 3. Choose Role Screen
        composable("role_selection") {
            RoleSelectionScreen(
                onSelectRole = { role ->
                    viewModel.setRole(role)
                }
            )
        }

        // 4. Worker Home with swipe
        composable("worker_home") {
            WorkerHomeScreen(
                viewModel = viewModel,
                navController = navController,
                onViewJobDetails = { jobId ->
                    navController.navigate("job_details/$jobId")
                }
            )
        }

        // 5. Job Details Screen
        composable(
            route = "job_details/{jobId}",
            arguments = listOf(navArgument("jobId") { type = NavType.IntType })
        ) { backStackEntry ->
            val jobId = backStackEntry.arguments?.getInt("jobId") ?: 0
            JobDetailsScreen(
                jobId = jobId,
                viewModel = viewModel,
                onBack = { navController.navigateUp() }
            )
        }

        // 6. Active Applications Screen
        composable("worker_applications") {
            WorkerApplicationsScreen(
                viewModel = viewModel,
                navController = navController
            )
        }

        // 7. Shop Owner Dashboard (with listed jobs and applications)
        composable("owner_dashboard") {
            OwnerDashboardScreen(
                viewModel = viewModel,
                navController = navController,
                onCreateJobClick = {
                    navController.navigate("owner_create_job")
                }
            )
        }

        // 8. Create Job Post Form Screen
        composable("owner_create_job") {
            CreateJobScreen(
                viewModel = viewModel,
                onBack = { navController.navigateUp() }
            )
        }

        // 9. Profile Screen
        composable("profile") {
            ProfileScreen(
                viewModel = viewModel,
                navController = navController
            )
        }
    }
}

// ==========================================
// SCREEN 1: SPLASH SCREEN
// ==========================================
@Composable
fun SplashScreen(onFinish: () -> Unit) {
    var startAnimation by remember { mutableStateOf(false) }
    val scale = animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.4f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    LaunchedEffect(Unit) {
        startAnimation = true
        delay(2200)
        onFinish()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(SlateDark, SlateMedium)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            // App visual logo
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .scale(scale.value)
                    .clip(RoundedCornerShape(28.dp))
                    .background(IndigoPrimary)
                    .shadow(12.dp, RoundedCornerShape(28.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = "Gig Swipe Logo",
                    tint = EmeraldSecondary,
                    modifier = Modifier.size(64.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Local Job Swipe",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    letterSpacing = 0.5.sp
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Instant shifts, urgent cash, nearby hiring.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 15.sp,
                    color = SlateLight,
                    textAlign = TextAlign.Center
                )
            )

            Spacer(modifier = Modifier.height(50.dp))

            CircularProgressIndicator(
                color = EmeraldSecondary,
                strokeWidth = 3.dp,
                modifier = Modifier.size(36.dp)
            )
        }

        // Trademark at bottom
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "HYPERLOCAL COLLABORATIVE WORKSPACE",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = SlateLight.copy(alpha = 0.6f),
                    letterSpacing = 1.5.sp
                )
            )
        }
    }
}

// ==========================================
// SCREEN 2: LOGIN SCREEN
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(onLogin: (name: String, phone: String, role: String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("Worker") } // Worker or ShopOwner
    var showError by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBackground)
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Welcome to JobSwipe",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = SlateDark,
                    fontSize = 28.sp
                ),
                modifier = Modifier.testTag("login_welcome_text")
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Login to find urgent work or post immediate jobs",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = SlateLight,
                    textAlign = TextAlign.Center
                )
            )

            Spacer(modifier = Modifier.height(36.dp))

            // Role selection buttons inside card style
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFE2E8F0))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                val roles = listOf("Worker", "ShopOwner")
                roles.forEach { role ->
                    val isSelected = selectedRole == role
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) IndigoPrimary else Color.Transparent)
                            .clickable { selectedRole = role }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (role == "Worker") "Worker" else "Shop Owner",
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else SlateDark,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Name field
            OutlinedTextField(
                value = name,
                onValueChange = { name = it; showError = false },
                label = { Text(if (selectedRole == "Worker") "Your Name" else "Shop / Business Name") },
                placeholder = { Text(if (selectedRole == "Worker") "e.g. Subho Das" else "e.g. Gupta Groceries") },
                leadingIcon = {
                    Icon(
                        imageVector = if (selectedRole == "Worker") Icons.Default.Person else Icons.Default.Store,
                        contentDescription = "Name Icon",
                        tint = SlateLight
                    )
                },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = IndigoPrimary,
                    unfocusedBorderColor = CardBorder
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("username_input")
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Phone Field
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it; showError = false },
                label = { Text("Phone Number") },
                placeholder = { Text("+91 XXXXX XXXXX") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = "Phone Icon",
                        tint = SlateLight
                    )
                },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = IndigoPrimary,
                    unfocusedBorderColor = CardBorder
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("phone_input")
            )

            if (showError) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = "Error icon",
                        tint = RoseTertiary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Please enter valid parameters.",
                        color = RoseTertiary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Login Button
            Button(
                onClick = {
                    if (name.isBlank() || phone.isBlank()) {
                        showError = true
                    } else {
                        onLogin(name, phone, selectedRole)
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = IndigoPrimary,
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("login_button")
            ) {
                Text(
                    text = if (selectedRole == "Worker") "Find Jobs Now" else "Post a Job Now",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}

// ==========================================
// SCREEN 3: CHOOSE ROLE SCREEN
// ==========================================
@Composable
fun RoleSelectionScreen(onSelectRole: (String) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Select Your Role",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = SlateDark,
                    fontSize = 28.sp
                ),
                modifier = Modifier.testTag("role_selection_title")
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Switch at any time instantly from your profile settings.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = SlateLight,
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            Spacer(modifier = Modifier.height(36.dp))

            // Role Card 1: Worker
            RoleCard(
                title = "I want to work (Worker)",
                description = "Browse, swipe left or right, apply for local part-time shifts and contact owners instantly.",
                icon = Icons.Default.Work,
                color = IndigoPrimary,
                modifier = Modifier
                    .clickable { onSelectRole("Worker") }
                    .testTag("worker_role_card")
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Role Card 2: Shop Owner
            RoleCard(
                title = "I want to hire (Shop Owner)",
                description = "Post urgent temporary posts, review applicants, and call or message them on WhatsApp immediately.",
                icon = Icons.Default.Store,
                color = EmeraldSecondary,
                modifier = Modifier
                    .clickable { onSelectRole("ShopOwner") }
                    .testTag("owner_role_card")
            )
        }
    }
}

@Composable
fun RoleCard(
    title: String,
    description: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        border = BorderStroke(1.dp, CardBorder),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = LightSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = color,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = SlateDark,
                        fontSize = 17.sp
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = SlateLight,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                )
            }
        }
    }
}


// ==========================================
// SCREEN 4: WORKER HOME (SWIPE SCREEN)
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkerHomeScreen(
    viewModel: MainViewModel,
    navController: NavController,
    onViewJobDetails: (Int) -> Unit
) {
    val currentJobs by viewModel.availableWorkerJobs.collectAsStateWithLifecycle()
    val workerName by viewModel.loggedInUserName.collectAsStateWithLifecycle()
    val workerLocation by viewModel.loggedInUserLocation.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Place,
                                contentDescription = "Location icon",
                                tint = IndigoPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = workerLocation,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = SlateLight
                                )
                            )
                        }
                        Text(
                            text = "Hello, $workerName 👋",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = SlateDark
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            viewModel.resetAllSwipesAndApplications()
                            Toast.makeText(context, "Refresh feed: Swipes and Applications Reset!", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.RestartAlt,
                            contentDescription = "Reset gestures",
                            tint = IndigoPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = LightSurface)
            )
        },
        bottomBar = {
            WorkerBottomNav(navController = navController, activeTab = "swipe")
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(LightBackground)
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // Swipe card Stack Container
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                if (currentJobs.isEmpty()) {
                    EmptySwipeState(
                        onReset = { viewModel.resetAllSwipesAndApplications() }
                    )
                } else {
                    // Show top card with dragging gesture. Show next card static underneath.
                    val reversedList = currentJobs.take(2).reversed()
                    reversedList.forEachIndexed { index, job ->
                        val isTopCard = index == reversedList.lastIndex
                        if (isTopCard) {
                            SwipeableJobCard(
                                job = job,
                                onSwipeLeft = {
                                    viewModel.performSwipe(job.id, isLiked = false)
                                    Toast.makeText(context, "Passed: ${job.title}", Toast.LENGTH_SHORT).show()
                                },
                                onSwipeRight = {
                                    viewModel.performSwipe(job.id, isLiked = true)
                                    Toast.makeText(context, "Applied to: ${job.title}! 👍", Toast.LENGTH_LONG).show()
                                },
                                onClickDetail = {
                                    onViewJobDetails(job.id)
                                }
                            )
                        } else {
                            // Secondary Card (gives spatial stack effect)
                            Card(
                                shape = RoundedCornerShape(28.dp),
                                border = BorderStroke(1.dp, CardBorder),
                                colors = CardDefaults.cardColors(containerColor = LightSurface),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(0.82f)
                                    .offset(y = 8.dp)
                                    .scale(0.96f)
                                    .shadow(2.dp, RoundedCornerShape(28.dp))
                            ) {}
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons underneath if we have jobs
            if (currentJobs.isNotEmpty()) {
                val topJob = currentJobs.first()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Pass/Cancel Button (Swipe Left)
                    IconButton(
                        onClick = {
                            scope.launch {
                                viewModel.performSwipe(topJob.id, isLiked = false)
                                Toast.makeText(context, "Passed: ${topJob.title}", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .size(56.dp)
                            .shadow(2.dp, CircleShape)
                            .background(LightSurface, CircleShape)
                            .border(1.dp, CardBorder, CircleShape)
                            .testTag("action_pass_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Pass item",
                            tint = RoseTertiary,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(32.dp))

                    // Info Details Center Button
                    IconButton(
                        onClick = {
                            onViewJobDetails(topJob.id)
                        },
                        modifier = Modifier
                            .size(46.dp)
                            .shadow(2.dp, CircleShape)
                            .background(IndigoPrimary.copy(alpha = 0.1f), CircleShape)
                            .border(1.dp, IndigoPrimary.copy(alpha = 0.3f), CircleShape)
                            .testTag("action_info_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "See details",
                            tint = IndigoPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(32.dp))

                    // Apply Button (Swipe Right)
                    IconButton(
                        onClick = {
                            scope.launch {
                                viewModel.performSwipe(topJob.id, isLiked = true)
                                Toast.makeText(context, "Applied to ${topJob.title}! 💼", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .size(56.dp)
                            .shadow(2.dp, CircleShape)
                            .background(EmeraldSecondary, CircleShape)
                            .testTag("action_apply_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Apply item",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }
    }
}

// Swipeable Card implementation utilizing detectDragGestures
@Composable
fun SwipeableJobCard(
    job: Job,
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit,
    onClickDetail: () -> Unit
) {
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    val swipeThreshold = 350f
    val scope = rememberCoroutineScope()

    val cardOffset = animateIntOffsetAsState(
        targetValue = IntOffset(offsetX.roundToInt(), offsetY.roundToInt()),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "card_offset"
    )

    val tiltAngle = offsetX * 0.04f

    Card(
        shape = RoundedCornerShape(28.dp),
        border = BorderStroke(2.dp, if (job.isUrgent) RoseTertiary.copy(alpha = 0.6f) else CardBorder),
        colors = CardDefaults.cardColors(containerColor = LightSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.82f)
            .offset { cardOffset.value }
            .rotate(tiltAngle)
            .shadow(6.dp, RoundedCornerShape(28.dp))
            .pointerInput(job.id) {
                detectDragGestures(
                    onDragCancel = {
                        offsetX = 0f
                        offsetY = 0f
                    },
                    onDragEnd = {
                        if (offsetX > swipeThreshold) {
                            scope.launch {
                                delay(50)
                                onSwipeRight()
                            }
                        } else if (offsetX < -swipeThreshold) {
                            scope.launch {
                                delay(50)
                                onSwipeLeft()
                            }
                        } else {
                            offsetX = 0f
                            offsetY = 0f
                        }
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        offsetX += dragAmount.x
                        offsetY += dragAmount.y
                    }
                )
            }
            .clickable { onClickDetail() }
            .testTag("job_card_${job.id}")
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                // Header tags row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.DirectionsWalk,
                            contentDescription = "distance icon",
                            tint = SlateLight,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = job.location,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = SlateLight
                        )
                    }

                    if (job.isUrgent) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(RedLightAlert)
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Urgent Badge",
                                    tint = RedDarkAlert,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "URGENT",
                                    color = RedDarkAlert,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Title
                Text(
                    text = job.title,
                    fontWeight = FontWeight.ExtraBold,
                    color = SlateDark,
                    fontSize = 24.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Shop name
                Text(
                    text = "at ${job.shopName}",
                    fontWeight = FontWeight.Bold,
                    color = IndigoPrimary,
                    fontSize = 16.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Divider(color = CardBorder, thickness = 1.dp)

                Spacer(modifier = Modifier.height(16.dp))

                // Work details summary indicators
                Row(modifier = Modifier.fillMaxWidth()) {
                    // Pay indicator
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AttachMoney,
                                contentDescription = "payment icon",
                                tint = EmeraldSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "PAYMENT",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = SlateLight
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = job.payment,
                            fontWeight = FontWeight.ExtraBold,
                            color = EmeraldSecondary,
                            fontSize = 18.sp
                        )
                    }

                    // Hours indicator
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = "hours icon",
                                tint = SlateDark,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "SHIFT TIME",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = SlateLight
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = job.workingHours,
                            fontWeight = FontWeight.Bold,
                            color = SlateDark,
                            fontSize = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Mini description
                Text(
                    text = "Description:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = SlateLight
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = job.description,
                    color = SlateDark,
                    fontSize = 14.sp,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.weight(1f))

                // Bottom CTA hint
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(LightBackground)
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Swipe Right to Apply • Swipe Left to Pass",
                        fontWeight = FontWeight.SemiBold,
                        color = SlateLight,
                        fontSize = 12.sp
                    )
                }
            }

            // Swipe Badges Overlays (visibly overlays when user drags horizontally)
            if (offsetX > 80f) {
                Box(
                    modifier = Modifier
                        .padding(24.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(3.dp, EmeraldSecondary, RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.9f))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .align(Alignment.TopStart)
                ) {
                    Text(
                        text = "APPLY",
                        color = EmeraldSecondary,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 24.sp
                    )
                }
            } else if (offsetX < -80f) {
                Box(
                    modifier = Modifier
                        .padding(24.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(3.dp, RoseTertiary, RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.9f))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .align(Alignment.TopEnd)
                ) {
                    Text(
                        text = "PASS",
                        color = RoseTertiary,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 24.sp
                    )
                }
            }
        }
    }
}

@Composable
fun EmptySwipeState(onReset: () -> Unit) {
    Card(
        shape = RoundedCornerShape(28.dp),
        border = BorderStroke(1.dp, CardBorder),
        colors = CardDefaults.cardColors(containerColor = LightSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.82f)
            .shadow(1.dp, RoundedCornerShape(28.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(28.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(IndigoPrimary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Completed icon",
                    tint = IndigoPrimary,
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "All swiped out!",
                fontWeight = FontWeight.Bold,
                color = SlateDark,
                fontSize = 21.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "You've swiped all nearby jobs! Check 'Applications' tab to trace active statuses, or click below to refresh and swipe again.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = SlateLight,
                    textAlign = TextAlign.Center,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onReset,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary)
            ) {
                Icon(
                    imageVector = Icons.Default.Loop,
                    contentDescription = "Refresh icon",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Replenish Card Stack",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}


// ==========================================
// SCREEN 5: JOB DETAILS SCREEN
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobDetailsScreen(
    jobId: Int,
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val allJobs by viewModel.allJobs.collectAsStateWithLifecycle()
    val workerApplications by viewModel.workerApplications.collectAsStateWithLifecycle()
    val job = allJobs.find { it.id == jobId }
    val isAlreadyApplied = workerApplications.any { it.jobId == jobId }

    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Job Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Go Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = LightSurface)
            )
        }
    ) { innerPadding ->
        if (job == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("Job details not found. It might have been deleted.", color = SlateDark)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(LightBackground)
                    .padding(innerPadding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Card header info
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = LightSurface),
                    border = BorderStroke(1.dp, CardBorder)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(
                                text = job.shopName,
                                color = IndigoPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            if (job.isUrgent) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(RedLightAlert)
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "URGENT",
                                        color = RedDarkAlert,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = job.title,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = SlateDark
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Place,
                                contentDescription = "Location",
                                tint = SlateLight,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = job.location,
                                color = SlateLight,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Pay Rate & working hours indicators
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Pay rates Card
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = LightSurface),
                        border = BorderStroke(1.dp, CardBorder)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AttachMoney,
                                    contentDescription = "payment index",
                                    tint = EmeraldSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "WAGE RATE",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = SlateLight
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = job.payment,
                                fontWeight = FontWeight.ExtraBold,
                                color = EmeraldSecondary,
                                fontSize = 18.sp
                            )
                        }
                    }

                    // Shift details card
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = LightSurface),
                        border = BorderStroke(1.dp, CardBorder)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AccessTime,
                                    contentDescription = "clock index",
                                    tint = SlateDark,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "WORKING TIME",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = SlateLight
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = job.workingHours,
                                fontWeight = FontWeight.Bold,
                                color = SlateDark,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Detail specs parameters (Workers Needed, contact info)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = LightSurface),
                    border = BorderStroke(1.dp, CardBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Workers Needed:",
                                fontWeight = FontWeight.SemiBold,
                                color = SlateLight,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "${job.workersNeeded} Positions",
                                fontWeight = FontWeight.Bold,
                                color = SlateDark,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Description Title and contents
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = LightSurface),
                    border = BorderStroke(1.dp, CardBorder)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Job Description",
                            fontWeight = FontWeight.Bold,
                            color = SlateDark,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = job.description,
                            color = SlateDark,
                            fontSize = 14.sp,
                            lineHeight = 22.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))

                // Interaction controls: Call, WhatsApp, Apply Button
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = LightSurface),
                    border = BorderStroke(1.dp, CardBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Contact shop owner directly:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = SlateLight
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Dial Phone Button
                            Button(
                                onClick = {
                                    val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                                        data = Uri.parse("tel:${job.phone}")
                                    }
                                    context.startActivity(dialIntent)
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFE2E8F0),
                                    contentColor = SlateDark
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("action_call_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Phone,
                                    contentDescription = "Call"
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Call", fontWeight = FontWeight.Bold)
                            }

                            // WhatsApp API redirect Button
                            Button(
                                onClick = {
                                    val url = "https://api.whatsapp.com/send?phone=${job.phone}&text=Hi%20${job.shopName},%20I'm%20interested%20in%20your%20part-time%20job%20post:%20${job.title}%20listed%20on%20JobSwipe."
                                    val waIntent = Intent(Intent.ACTION_VIEW).apply {
                                        data = Uri.parse(url)
                                    }
                                    context.startActivity(waIntent)
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF25D366),
                                    contentColor = Color.White
                                ),
                                modifier = Modifier
                                    .weight(1.2f)
                                    .height(48.dp)
                                    .testTag("action_whatsapp_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Message,
                                    contentDescription = "WhatsApp"
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("WhatsApp", fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Unified quick apply CTA
                        Button(
                            onClick = {
                                if (!isAlreadyApplied) {
                                    viewModel.applyForJob(job.id)
                                    Toast.makeText(context, "Applied to ${job.title} successfully!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            enabled = !isAlreadyApplied,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isAlreadyApplied) Color(0xFFE2E8F0) else IndigoPrimary,
                                contentColor = if (isAlreadyApplied) SlateLight else Color.White
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("apply_now_button")
                        ) {
                            Icon(
                                imageVector = if (isAlreadyApplied) Icons.Default.CheckCircle else Icons.Default.Check,
                                contentDescription = if (isAlreadyApplied) "Applied" else "Apply"
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isAlreadyApplied) "Applied — Awaiting Merchant Contact" else "Submit Gig Application Now",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

// Custom horizontal slider state helper
@Composable
fun rememberScrollState(): androidx.compose.foundation.ScrollState {
    return androidx.compose.foundation.rememberScrollState()
}


// ==========================================
// SCREEN 6: APPLICATIONS SCREEN (WORKER)
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkerApplicationsScreen(
    viewModel: MainViewModel,
    navController: NavController
) {
    val myApplications by viewModel.workerApplications.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Applications", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = LightSurface)
            )
        },
        bottomBar = {
            WorkerBottomNav(navController = navController, activeTab = "applications")
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(LightBackground)
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            if (myApplications.isEmpty()) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.WorkOutline,
                            contentDescription = "Empty",
                            tint = SlateLight,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No submissions yet",
                            fontWeight = FontWeight.Bold,
                            color = SlateDark
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Swipe right on worker job cards to submit immediate applications.",
                            color = SlateLight,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(myApplications) { app ->
                        ApplicationCard(
                            app = app,
                            onCancel = {
                                viewModel.deleteApplication(app.id)
                                Toast.makeText(context, "Cancelled Application for ${app.jobTitle}", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ApplicationCard(
    app: Application,
    onCancel: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, CardBorder),
        colors = CardDefaults.cardColors(containerColor = LightSurface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = app.shopName,
                    color = IndigoPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )

                // Render dynamic colored status pill
                val (bgStatus, textStatus) = when (app.status) {
                    "Contacted" -> Pair(BlueLightInfo, BlueDarkInfo)
                    "Accepted" -> Pair(GreenLightSuccess, GreenDarkSuccess)
                    "Rejected" -> Pair(RedLightAlert, RedDarkAlert)
                    else -> Pair(Color(0xFFF1F5F9), SlateLight)
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(bgStatus)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = app.status.uppercase(),
                        color = textStatus,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = app.jobTitle,
                fontWeight = FontWeight.ExtraBold,
                color = SlateDark,
                fontSize = 17.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Divider(color = CardBorder, thickness = 1.dp)

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Applied on Mobile App",
                    fontSize = 12.sp,
                    color = SlateLight
                )

                Text(
                    text = "Cancel Application",
                    color = RoseTertiary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    modifier = Modifier
                        .clickable { onCancel() }
                        .padding(vertical = 4.dp, horizontal = 8.dp)
                )
            }
        }
    }
}


// ==========================================
// SCREEN 7: SHOP OWNER DASHBOARD SCREEN
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerDashboardScreen(
    viewModel: MainViewModel,
    navController: NavController,
    onCreateJobClick: () -> Unit
) {
    val myJobs by viewModel.ownerJobs.collectAsStateWithLifecycle()
    val receivedApps by viewModel.ownerReceivedApplications.collectAsStateWithLifecycle()
    val ownerName by viewModel.ownerName.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var activeTab by remember { mutableStateOf("jobs") } // jobs, applications

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = ownerName,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = SlateDark
                            )
                        )
                        Text(
                            text = "Merchant Staffing Dashboard",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = SlateLight
                            )
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = LightSurface)
            )
        },
        floatingActionButton = {
            if (activeTab == "jobs") {
                FloatingActionButton(
                    onClick = onCreateJobClick,
                    containerColor = IndigoPrimary,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("add_job_fab")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Create new job listing")
                }
            }
        },
        bottomBar = {
            OwnerBottomNav(navController = navController, activeTab = "dashboard")
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(LightBackground)
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // Sub tab bar selectors
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFE2E8F0))
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (activeTab == "jobs") IndigoPrimary else Color.Transparent)
                        .clickable { activeTab = "jobs" }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.List,
                            contentDescription = "Active Jobs Tab",
                            tint = if (activeTab == "jobs") Color.White else SlateDark,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Active Jobs (${myJobs.size})",
                            fontWeight = FontWeight.Bold,
                            color = if (activeTab == "jobs") Color.White else SlateDark,
                            fontSize = 13.sp
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1.1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (activeTab == "applications") IndigoPrimary else Color.Transparent)
                        .clickable { activeTab = "applications" }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.People,
                            contentDescription = "Applications Tab",
                            tint = if (activeTab == "applications") Color.White else SlateDark,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Applications (${receivedApps.size})",
                            fontWeight = FontWeight.Bold,
                            color = if (activeTab == "applications") Color.White else SlateDark,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tab view execution
            if (activeTab == "jobs") {
                if (myJobs.isEmpty()) {
                    Box(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Storefront,
                                contentDescription = "Store Empty",
                                tint = SlateLight,
                                modifier = Modifier.size(56.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No active listings",
                                fontWeight = FontWeight.Bold,
                                color = SlateDark
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Press the + FAB button to post an urgent local gig instantly.",
                                color = SlateLight,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(myJobs) { job ->
                            OwnerJobCard(
                                job = job,
                                onDelete = {
                                    viewModel.deleteJob(job)
                                    Toast.makeText(context, "Deleted gig: ${job.title}", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }
                }
            } else {
                // Applications tab view
                if (receivedApps.isEmpty()) {
                    Box(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Face,
                                contentDescription = "People Empty",
                                tint = SlateLight,
                                modifier = Modifier.size(56.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No applicants yet",
                                fontWeight = FontWeight.Bold,
                                color = SlateDark
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Your urgent gig is broadcasted to nearby workers. Wait for swipe interactions!",
                                color = SlateLight,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(receivedApps) { app ->
                            ReceivedApplicationCard(
                                app = app,
                                onSetStatus = { newStatus ->
                                    viewModel.updateApplicationStatus(app.id, newStatus)
                                    Toast.makeText(context, "Applicant status: $newStatus", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OwnerJobCard(
    job: Job,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, if (job.isUrgent) RoseTertiary.copy(alpha = 0.5f) else CardBorder),
        colors = CardDefaults.cardColors(containerColor = LightSurface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AttachMoney,
                        contentDescription = "compensation icon",
                        tint = EmeraldSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = job.payment,
                        color = EmeraldSecondary,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp
                    )
                }

                if (job.isUrgent) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(RedLightAlert)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "URGENT",
                            color = RedDarkAlert,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = job.title,
                fontWeight = FontWeight.ExtraBold,
                color = SlateDark,
                fontSize = 18.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = job.description,
                color = SlateLight,
                fontSize = 13.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Divider(color = CardBorder, thickness = 1.dp)

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row {
                    Icon(
                        imageVector = Icons.Default.People,
                        contentDescription = "Limit needed info",
                        tint = SlateLight,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Needed: ${job.workersNeeded} workers",
                        fontSize = 12.sp,
                        color = SlateLight,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Remove gig post",
                        tint = RoseTertiary
                    )
                }
            }
        }
    }
}

@Composable
fun ReceivedApplicationCard(
    app: Application,
    onSetStatus: (String) -> Unit
) {
    val context = LocalContext.current
    Card(
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, CardBorder),
        colors = CardDefaults.cardColors(containerColor = LightSurface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Wants to Work",
                        color = IndigoPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = app.workerName,
                        fontWeight = FontWeight.ExtraBold,
                        color = SlateDark,
                        fontSize = 18.sp
                    )
                }

                // Application status display tag
                val (bgStatus, textStatus) = when (app.status) {
                    "Contacted" -> Pair(BlueLightInfo, BlueDarkInfo)
                    "Accepted" -> Pair(GreenLightSuccess, GreenDarkSuccess)
                    "Rejected" -> Pair(RedLightAlert, RedDarkAlert)
                    else -> Pair(Color(0xFFF1F5F9), SlateLight)
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(bgStatus)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = app.status.uppercase(),
                        color = textStatus,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Applied For Post: ${app.jobTitle}",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = SlateLight
            )

            Spacer(modifier = Modifier.height(14.dp))

            Divider(color = CardBorder, thickness = 1.dp)

            Spacer(modifier = Modifier.height(14.dp))

            // Action: WhatsApp/Dial Worker directly
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Dial Applicant
                OutlinedButton(
                    onClick = {
                        val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                            data = Uri.parse("tel:${app.workerPhone}")
                        }
                        context.startActivity(dialIntent)
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SlateDark),
                    border = BorderStroke(1.dp, CardBorder),
                    modifier = Modifier.weight(1f).height(40.dp)
                ) {
                    Icon(imageVector = Icons.Default.Phone, contentDescription = "call worker", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Call", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                // WhatsApp Applicant
                Button(
                    onClick = {
                        val url = "https://api.whatsapp.com/send?phone=${app.workerPhone}&text=Hello%20${app.workerName},%20we%20reviewed%20your%20JobSwipe%20application%20for%20our%20job:%20${app.jobTitle}!"
                        val waIntent = Intent(Intent.ACTION_VIEW).apply {
                            data = Uri.parse(url)
                        }
                        context.startActivity(waIntent)
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                    modifier = Modifier.weight(1.2f).height(40.dp)
                ) {
                    Icon(imageVector = Icons.Default.Message, contentDescription = "msg worker", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("WhatsApp", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Update Status Buttons (Contacted, Accept, Reject)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Set Contacted status button
                Text(
                    text = "Contacted",
                    color = BlueDarkInfo,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(BlueLightInfo)
                        .clickable { onSetStatus("Contacted") }
                        .padding(vertical = 8.dp),
                    textAlign = TextAlign.Center
                )

                // Accept button
                Text(
                    text = "Accept",
                    color = GreenDarkSuccess,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(GreenLightSuccess)
                        .clickable { onSetStatus("Accepted") }
                        .padding(vertical = 8.dp),
                    textAlign = TextAlign.Center
                )

                // Reject button
                Text(
                    text = "Reject",
                    color = RedDarkAlert,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(RedLightAlert)
                        .clickable { onSetStatus("Rejected") }
                        .padding(vertical = 8.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}


// ==========================================
// SCREEN 8: CREATE JOB POST FORM SCREEN
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateJobScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var payment by remember { mutableStateOf("") }
    var workingHours by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var workersNeeded by remember { mutableStateOf("1") }
    var phone by remember { mutableStateOf("") }
    var isUrgent by remember { mutableStateOf(false) }

    var showError by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Set matching contact info from currently logged-in shop owner details
    val ownerPhone by viewModel.ownerPhone.collectAsStateWithLifecycle()
    val ownerLocation by viewModel.ownerLocation.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        phone = ownerPhone
        location = ownerLocation
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Post Urgent Gig", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Go Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = LightSurface)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(LightBackground)
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = LightSurface),
                border = BorderStroke(1.dp, CardBorder)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Gig Post Specifications",
                        fontWeight = FontWeight.ExtraBold,
                        color = SlateDark,
                        fontSize = 17.sp
                    )

                    // Job Title
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it; showError = false },
                        label = { Text("Job Title") },
                        placeholder = { Text("e.g. Cafe Barista, Grocery Assistant") },
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = IndigoPrimary,
                            unfocusedBorderColor = CardBorder
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("job_title_input")
                    )

                    // Description
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it; showError = false },
                        label = { Text("Job Description") },
                        placeholder = { Text("Explain task, what tools are needed, special tips...") },
                        shape = RoundedCornerShape(10.dp),
                        minLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = IndigoPrimary,
                            unfocusedBorderColor = CardBorder
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("job_desc_input")
                    )

                    // Payment
                    OutlinedTextField(
                        value = payment,
                        onValueChange = { payment = it; showError = false },
                        label = { Text("Payment (e.g. ₹150 / Hr or ₹1,000 / Day)") },
                        placeholder = { Text("e.g. ₹150 / Hr or On-The-Spot Cash") },
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = IndigoPrimary,
                            unfocusedBorderColor = CardBorder
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("job_payment_input")
                    )

                    // Working Hours
                    OutlinedTextField(
                        value = workingHours,
                        onValueChange = { workingHours = it; showError = false },
                        label = { Text("Working Hours / Shift Time") },
                        placeholder = { Text("e.g. 4 PM - 9 PM, Weekend 11 AM - 8 PM") },
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = IndigoPrimary,
                            unfocusedBorderColor = CardBorder
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("job_hours_input")
                    )

                    // Location
                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it; showError = false },
                        label = { Text("Location Address") },
                        placeholder = { Text("e.g. Salt Lake Block EC (0.4 km away)") },
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = IndigoPrimary,
                            unfocusedBorderColor = CardBorder
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("job_location_input")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Workers Needed Input
                        OutlinedTextField(
                            value = workersNeeded,
                            onValueChange = { workersNeeded = it; showError = false },
                            label = { Text("Workers Needed") },
                            placeholder = { Text("e.g. 1") },
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = IndigoPrimary,
                                unfocusedBorderColor = CardBorder
                            ),
                            modifier = Modifier.weight(1f).testTag("job_workers_input")
                        )

                        // Phone Info Input
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it; showError = false },
                            label = { Text("Contact Phone") },
                            placeholder = { Text("+91XXXXXXXXXX") },
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = IndigoPrimary,
                                unfocusedBorderColor = CardBorder
                            ),
                            modifier = Modifier.weight(1.4f).testTag("job_phone_input")
                        )
                    }

                    // Urgent hiring toggle item
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(LightBackground)
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "Mark as Urgent Hiring", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(text = "Appends alert badges to boost view count.", fontSize = 11.sp, color = SlateLight)
                        }
                        Switch(
                            checked = isUrgent,
                            onCheckedChange = { isUrgent = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = RoseTertiary, checkedTrackColor = RedLightAlert)
                        )
                    }

                    if (showError) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Error, contentDescription = null, tint = RoseTertiary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("All fields are mandatory.", color = RoseTertiary, fontSize = 13.sp)
                        }
                    }
                }
            }

            // Submit Button
            Button(
                onClick = {
                    val workersCount = workersNeeded.toIntOrNull() ?: 1
                    if (title.isBlank() || description.isBlank() || payment.isBlank() ||
                        workingHours.isBlank() || location.isBlank() || phone.isBlank()) {
                        showError = true
                    } else {
                        viewModel.postJob(
                            title = title,
                            description = description,
                            payment = payment,
                            workingHours = workingHours,
                            location = location,
                            workersNeeded = workersCount,
                            phone = phone,
                            isUrgent = isUrgent
                        )
                        Toast.makeText(context, "Gig listing with title '$title' posted live! 🎯", Toast.LENGTH_LONG).show()
                        onBack()
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("submit_job_button")
            ) {
                Text("Broadcast Gig Live", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}


// ==========================================
// SCREEN 9: PROFILE SCREEN (SWITCH ROLES SCREEN)
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: MainViewModel,
    navController: NavController
) {
    val currentRole by viewModel.userRole.collectAsStateWithLifecycle()

    val workerName by viewModel.loggedInUserName.collectAsStateWithLifecycle()
    val workerPhone by viewModel.loggedInUserPhone.collectAsStateWithLifecycle()
    val workerLocation by viewModel.loggedInUserLocation.collectAsStateWithLifecycle()

    val ownerShopName by viewModel.ownerName.collectAsStateWithLifecycle()
    val ownerPhone by viewModel.ownerPhone.collectAsStateWithLifecycle()
    val ownerLocation by viewModel.ownerLocation.collectAsStateWithLifecycle()

    val myApplications by viewModel.workerApplications.collectAsStateWithLifecycle()
    val myOwnerJobs by viewModel.ownerJobs.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Profile", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = LightSurface)
            )
        },
        bottomBar = {
            if (currentRole == "Worker") {
                WorkerBottomNav(navController = navController, activeTab = "profile")
            } else {
                OwnerBottomNav(navController = navController, activeTab = "profile")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(LightBackground)
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile Card Details
            Card(
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, CardBorder),
                colors = CardDefaults.cardColors(containerColor = LightSurface),
                modifier = Modifier.fillMaxWidth().testTag("profile_top_card")
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(IndigoPrimary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (currentRole == "Worker") Icons.Default.Person else Icons.Default.Store,
                            contentDescription = "Avatar",
                            tint = IndigoPrimary,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = if (currentRole == "Worker") workerName else ownerShopName,
                        fontWeight = FontWeight.ExtraBold,
                        color = SlateDark,
                        fontSize = 20.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = if (currentRole == "Worker") "Worker Profile" else "Shop Owner Profile",
                        color = IndigoPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        letterSpacing = 0.8.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Divider(color = CardBorder, thickness = 1.dp)

                    Spacer(modifier = Modifier.height(14.dp))

                    // Details specs
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Phone:", fontWeight = FontWeight.Medium, color = SlateLight, fontSize = 13.sp)
                        Text(
                            text = if (currentRole == "Worker") workerPhone else ownerPhone,
                            fontWeight = FontWeight.Bold,
                            color = SlateDark,
                            fontSize = 13.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Location:", fontWeight = FontWeight.Medium, color = SlateLight, fontSize = 13.sp)
                        Text(
                            text = if (currentRole == "Worker") workerLocation else ownerLocation,
                            fontWeight = FontWeight.Bold,
                            color = SlateDark,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // Stats Card block
            Card(
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, CardBorder),
                colors = CardDefaults.cardColors(containerColor = LightSurface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (currentRole == "Worker") "${myApplications.size}" else "${myOwnerJobs.size}",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 24.sp,
                            color = IndigoPrimary
                        )
                        Text(
                            text = if (currentRole == "Worker") "Applications" else "Listed Gigs",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = SlateLight
                        )
                    }

                    Box(modifier = Modifier.width(1.dp).height(44.dp).background(CardBorder))

                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (currentRole == "Worker") "Active" else "Hiring",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            color = EmeraldSecondary
                        )
                        Text(
                            text = "Market Status",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = SlateLight
                        )
                    }
                }
            }

            // CTA: Action Switching Roles
            Card(
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, CardBorder),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
                modifier = Modifier.fillMaxWidth().testTag("switch_role_action_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Instant Testing Utilities",
                        fontWeight = FontWeight.Bold,
                        color = SlateDark,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Want to test how the other side interacts? Toggle roles instantly below without logging out.",
                        fontSize = 12.sp,
                        color = SlateLight,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            val targetRole = if (currentRole == "Worker") "ShopOwner" else "Worker"
                            viewModel.setRole(targetRole)
                            Toast.makeText(context, "Swapped role view to: $targetRole", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                        modifier = Modifier.fillMaxWidth().height(42.dp).testTag("action_role_swap_button")
                    ) {
                        Icon(imageVector = Icons.Default.Cached, contentDescription = "swap index")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (currentRole == "Worker") "Switch to Shop Owner view" else "Switch to Worker feed",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }

            // Logout row button
            Button(
                onClick = {
                    viewModel.logout()
                    Toast.makeText(context, "Logged out safely.", Toast.LENGTH_SHORT).show()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = RoseTertiary),
                border = BorderStroke(1.dp, RoseTertiary.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("profile_logout_button")
            ) {
                Icon(imageVector = Icons.Default.Logout, contentDescription = "logout icon")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Logout from Session", fontWeight = FontWeight.Bold)
            }
        }
    }
}


// ==========================================
// NAVIGATION BARS HELPERS
// ==========================================
@Composable
fun WorkerBottomNav(
    navController: NavController,
    activeTab: String
) {
    NavigationBar(
        containerColor = LightSurface,
        tonalElevation = 6.dp,
        windowInsets = WindowInsets.navigationBars
    ) {
        // Swipe Tab item
        NavigationBarItem(
            selected = activeTab == "swipe",
            onClick = {
                navController.navigate("worker_home") {
                    popUpTo("worker_home") { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = "Swipe feed icon"
                )
            },
            label = { Text("Swipe", fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = IndigoPrimary,
                selectedTextColor = IndigoPrimary,
                indicatorColor = IndigoPrimary.copy(alpha = 0.1f),
                unselectedIconColor = SlateLight,
                unselectedTextColor = SlateLight
            )
        )

        // Applications Tab Item
        NavigationBarItem(
            selected = activeTab == "applications",
            onClick = {
                navController.navigate("worker_applications") {
                    popUpTo("worker_home") { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.List,
                    contentDescription = "Applications icon"
                )
            },
            label = { Text("Applications", fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = IndigoPrimary,
                selectedTextColor = IndigoPrimary,
                indicatorColor = IndigoPrimary.copy(alpha = 0.1f),
                unselectedIconColor = SlateLight,
                unselectedTextColor = SlateLight
            )
        )

        // Profile tab item
        NavigationBarItem(
            selected = activeTab == "profile",
            onClick = {
                navController.navigate("profile") {
                    popUpTo("worker_home") { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile tab icon"
                )
            },
            label = { Text("Profile", fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = IndigoPrimary,
                selectedTextColor = IndigoPrimary,
                indicatorColor = IndigoPrimary.copy(alpha = 0.1f),
                unselectedIconColor = SlateLight,
                unselectedTextColor = SlateLight
            )
        )
    }
}

@Composable
fun OwnerBottomNav(
    navController: NavController,
    activeTab: String
) {
    NavigationBar(
        containerColor = LightSurface,
        tonalElevation = 6.dp,
        windowInsets = WindowInsets.navigationBars
    ) {
        // Owner Dashboard Tab item
        NavigationBarItem(
            selected = activeTab == "dashboard",
            onClick = {
                navController.navigate("owner_dashboard") {
                    popUpTo("owner_dashboard") { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.Storefront,
                    contentDescription = "Merchant dashboard tag"
                )
            },
            label = { Text("Dashboard", fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = IndigoPrimary,
                selectedTextColor = IndigoPrimary,
                indicatorColor = IndigoPrimary.copy(alpha = 0.1f),
                unselectedIconColor = SlateLight,
                unselectedTextColor = SlateLight
            )
        )

        // Owner Profile Tab Item
        NavigationBarItem(
            selected = activeTab == "profile",
            onClick = {
                navController.navigate("profile") {
                    popUpTo("owner_dashboard") { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile management tab"
                )
            },
            label = { Text("Profile", fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = IndigoPrimary,
                selectedTextColor = IndigoPrimary,
                indicatorColor = IndigoPrimary.copy(alpha = 0.1f),
                unselectedIconColor = SlateLight,
                unselectedTextColor = SlateLight
            )
        )
    }
}
