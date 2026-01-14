package com.prepstack.ui.screen

import android.app.Activity
import androidx.compose.animation.*
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prepstack.ads.AdManager
import com.prepstack.ads.BannerAdView
import com.prepstack.domain.model.DifficultyLevel
import com.prepstack.domain.model.Question
import com.prepstack.domain.model.QuestionType
import com.prepstack.ui.components.EnhancedTopBar
import com.prepstack.ui.viewmodel.QuizState
import com.prepstack.ui.viewmodel.QuizViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Professional Quiz Screen for Tech Interview Prep
 * Replicates real exam experience with timer, state management, and feedback
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
    viewModel: QuizViewModel,
    domainId: String,
    topicId: String? = null,
    onBackClick: () -> Unit,
    onQuizComplete: (score: Int, total: Int, timeTaken: Long) -> Unit = { _, _, _ -> }
) {
    // Load questions from JSON based on topic or domain
    LaunchedEffect(topicId, domainId) {
        if (topicId != null) {
            viewModel.loadQuizByTopic(topicId)
        } else {
            viewModel.loadQuizByDomain(domainId, questionCount = 10)
        }
    }
    
    val quizState by viewModel.quizState.collectAsState()
    
    when (quizState) {
        is QuizState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator()
                    Text(
                        text = "Loading quiz questions...",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
        is QuizState.Error -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(64.dp)
                    )
                    Text(
                        text = "Error Loading Quiz",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = (quizState as QuizState.Error).message,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(onClick = onBackClick) {
                        Text("Go Back")
                    }
                }
            }
        }
        is QuizState.Success -> {
            val questions = (quizState as QuizState.Success).questions
            QuizContent(
                questions = questions,
                onBackClick = onBackClick,
                onQuizComplete = onQuizComplete
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizContent(
    questions: List<Question>,
    onBackClick: () -> Unit,
    onQuizComplete: (score: Int, total: Int, timeTaken: Long) -> Unit
) {
    var currentQuestionIndex by remember { mutableStateOf(0) }
    var selectedOption by remember { mutableStateOf<String?>(null) }
    var isAnswerSubmitted by remember { mutableStateOf(false) }
    var showExplanation by remember { mutableStateOf(false) }
    var isBookmarked by remember { mutableStateOf(false) }
    var isMarkedForReview by remember { mutableStateOf(false) }
    var showExitDialog by remember { mutableStateOf(false) }
    
    // Add adManager
    val context = LocalContext.current
    val adManager = remember { AdManager(context) }
    
    // Track ad loading state
    var isAdLoaded by remember { mutableStateOf(false) }
    var adLoadAttempted by remember { mutableStateOf(false) }
    
    // Load rewarded ad with additional status tracking
    LaunchedEffect(Unit) {
        // Preload rewarded ad for the end of quiz
        adLoadAttempted = true
        
        // Try to load ad multiple times with a delay if needed
        for (i in 1..3) {
            if (!isAdLoaded) {
                adManager.loadRewardedAd(
                    adUnitId = AdManager.getRewardedAdId(),
                    onAdLoaded = { 
                        isAdLoaded = true
                        println("✅ Rewarded ad loaded successfully")
                    },
                    onAdFailedToLoad = { errorCode ->
                        println("❌ Failed to load rewarded ad: $errorCode")
                        isAdLoaded = false
                    }
                )
                
                // Wait before trying again
                if (!isAdLoaded && i < 3) {
                    delay(3000) // Wait 3 seconds before trying again
                }
            }
        }
    }
    
    // Track answer status
    val answeredQuestions = remember { mutableStateMapOf<Int, Boolean>() }
    val skippedQuestions = remember { mutableStateMapOf<Int, Boolean>() }
    val reviewQuestions = remember { mutableStateMapOf<Int, Boolean>() }
    val correctAnswers = remember { mutableStateMapOf<Int, Boolean>() }
    
    // Timer state - 10 minutes default
    val totalTimeSeconds = 600
    var remainingSeconds by remember { mutableStateOf(totalTimeSeconds) }
    val scope = rememberCoroutineScope()
    
    // State for ad dialog
    var showAdLoadingDialog by remember { mutableStateOf(false) }
    
    // Function to handle quiz completion with ad
    val completeQuizWithAd = {
        val score = correctAnswers.count { it.value }
        val total = questions.size
        val timeTaken = totalTimeSeconds.toLong() - remainingSeconds
        
        // Set dialog state to true to trigger showing the dialog
        showAdLoadingDialog = true
    }
    
    // Show dialog if state is true
    if (showAdLoadingDialog) {
        AlertDialog(
            // Prevent dismissing by clicking outside (user must watch ad)
            onDismissRequest = { },
            containerColor = MaterialTheme.colorScheme.surface,
            title = null, // Remove default title for custom layout
            text = { 
                Box(contentAlignment = Alignment.TopCenter) {
                    // Decorative background gradient
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFF667eea).copy(alpha = 0.15f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )
                    
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Trophy or medal icon
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            modifier = Modifier
                                .size(64.dp)
                                .padding(8.dp),
                            tint = Color(0xFFFFC107)
                        )
                        
                        Text(
                            text = "QUIZ COMPLETED!",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 20.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Your results are ready!",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    textAlign = TextAlign.Center
                                )
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = " ?/${questions.size} questions correct",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Text(
                                    text = "Watch a quick ad to see your detailed results",
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                        }
                        
                        // Animated progress bar with pulsating effect
                        val infiniteTransition = rememberInfiniteTransition(label = "loading")
                        val progressAlpha by infiniteTransition.animateFloat(
                            initialValue = 0.4f,
                            targetValue = 0.9f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(700),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "alpha"
                        )
                        

                        Spacer(modifier = Modifier.height(4.dp))
                        

                    }
                }
            },
            confirmButton = { 
                Button(
                    onClick = {
                        showAdLoadingDialog = false
                        
                        // Activity context needed for full screen ads
                        val activity = context as? Activity
                        val scoreVal = correctAnswers.count { it.value }
                        val totalVal = questions.size
                        val timeTakenVal = totalTimeSeconds.toLong() - remainingSeconds
                        
                        activity?.let {
                            if (adLoadAttempted) {
                                println("🎬 Attempting to show rewarded ad")
                                // Show rewarded ad before completing
                                adManager.showRewardedAd(
                                    activity = it,
                                    adUnitId = AdManager.getRewardedAdId(),
                                    onRewarded = {
                                        println("🎁 User earned reward from ad")
                                        // Give any in-app reward here if needed
                                    },
                                    onAdClosed = {
                                        println("🏁 Ad closed, navigating to results")
                                        // Navigate to results after ad closes
                                        onQuizComplete(scoreVal, totalVal, timeTakenVal)
                                    }
                                )
                            } else {
                                println("⚠️ Ad not loaded, navigating directly")
                                onQuizComplete(scoreVal, totalVal, timeTakenVal)
                            }
                        } ?: run {
                            // If activity context is not available, just navigate
                            println("⚠️ Activity context not available")
                            onQuizComplete(correctAnswers.count { it.value }, 
                                          questions.size, 
                                          totalTimeSeconds.toLong() - remainingSeconds)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "WATCH AD TO CONTINUE", 
                            fontWeight = FontWeight.ExtraBold,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            },
            // No dismiss button - user must watch ad
            dismissButton = null
        )
    }
    
    // Timer countdown
    LaunchedEffect(Unit) {
        scope.launch {
            while (remainingSeconds > 0) {
                delay(1000)
                remainingSeconds--
            }
            // Auto-submit when time ends
            completeQuizWithAd()
        }
    }
    
    val currentQuestion = questions[currentQuestionIndex]
    val isLastQuestion = currentQuestionIndex == questions.size - 1
    
    // Timer state colors
    val timerColor = when {
        remainingSeconds <= 5 -> Color.Red
        remainingSeconds <= 10 -> Color(0xFFFF9800)
        else -> MaterialTheme.colorScheme.primary
    }
    
    // Exit confirmation dialog
    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            icon = { Icon(Icons.Default.Warning, contentDescription = null) },
            title = { Text("Exit Quiz?") },
            text = { Text("Are you sure you want to exit? Your progress will be lost.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showExitDialog = false
                        onBackClick()
                    }
                ) {
                    Text("Exit")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text("Continue")
                }
            }
        )
    }
    
    Scaffold(
        topBar = {
            QuizTopBar(
                quizTitle = "Quiz",
                difficulty = currentQuestion.difficulty,
                currentQuestion = currentQuestionIndex + 1,
                totalQuestions = questions.size,
                remainingSeconds = remainingSeconds,
                timerColor = timerColor,
                onBackClick = { showExitDialog = true }
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Progress Bar
            LinearProgressIndicator(
                progress = (currentQuestionIndex + 1).toFloat() / questions.size,
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Question Card
                QuestionCard(
                    questionNumber = currentQuestionIndex + 1,
                    questionText = currentQuestion.questionText,
                    questionType = currentQuestion.type,
                    difficulty = currentQuestion.difficulty,
                    isBookmarked = isBookmarked,
                    isMarkedForReview = isMarkedForReview,
                    onBookmarkClick = { isBookmarked = !isBookmarked },
                    onMarkForReviewClick = { 
                        isMarkedForReview = !isMarkedForReview
                        reviewQuestions[currentQuestionIndex] = !isMarkedForReview
                    }
                )
                
                // Show different UI based on question type
                when (currentQuestion.type) {
                    QuestionType.MCQ -> {
                        // MCQ Options
                        currentQuestion.options.forEach { option ->
                            OptionCard(
                                optionText = option,
                                isSelected = selectedOption == option,
                                isCorrect = isAnswerSubmitted && option == currentQuestion.correctAnswer,
                                isIncorrect = isAnswerSubmitted && selectedOption == option && option != currentQuestion.correctAnswer,
                                isEnabled = !isAnswerSubmitted,
                                onClick = {
                                    if (!isAnswerSubmitted) {
                                        selectedOption = option
                                    }
                                }
                            )
                        }
                    }
                    QuestionType.THEORY -> {
                        // Theory Question - Show answer after submission
                        TheoryAnswerCard(
                            isSubmitted = isAnswerSubmitted,
                            correctAnswer = currentQuestion.correctAnswer
                        )
                    }
                }
                
                // Explanation Section (shown after submission)
                // For MCQ: show if answer is correct/incorrect
                // For THEORY: show explanation without correct/incorrect indicator
                AnimatedVisibility(
                    visible = isAnswerSubmitted,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    if (currentQuestion.type == QuestionType.MCQ) {
                        ExplanationCard(
                            isCorrect = selectedOption == currentQuestion.correctAnswer,
                            explanation = currentQuestion.explanation,
                            showExplanation = showExplanation,
                            onToggleExplanation = { showExplanation = !showExplanation }
                        )
                    } else {
                        // For THEORY questions, show explanation in a neutral way
                        TheoryExplanationCard(
                            explanation = currentQuestion.explanation,
                            showExplanation = showExplanation,
                            onToggleExplanation = { showExplanation = !showExplanation }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Action Buttons
            ActionButtonsSection(
                questionType = currentQuestion.type,
                selectedOption = selectedOption,
                isAnswerSubmitted = isAnswerSubmitted,
                isLastQuestion = isLastQuestion,
                onSubmit = {
                    // For MCQ, check if option is selected
                    // For THEORY, just reveal the answer
                    if (currentQuestion.type == QuestionType.THEORY || selectedOption != null) {
                        isAnswerSubmitted = true
                        showExplanation = true
                        answeredQuestions[currentQuestionIndex] = true
                        
                        // For MCQ, track if answer was correct
                        if (currentQuestion.type == QuestionType.MCQ) {
                            correctAnswers[currentQuestionIndex] = selectedOption == currentQuestion.correctAnswer
                        } else {
                            // For THEORY, we can't automatically mark as correct/incorrect
                            // User should self-assess, but we'll mark as "answered"
                            correctAnswers[currentQuestionIndex] = true
                        }
                    }
                },
                onNext = {
                    if (isLastQuestion) {
                        // Show rewarded ad and then navigate to results
                        completeQuizWithAd()
                    } else {
                        // Move to next question
                        currentQuestionIndex++
                        selectedOption = null
                        isAnswerSubmitted = false
                        showExplanation = false
                        isBookmarked = false
                        isMarkedForReview = reviewQuestions[currentQuestionIndex] ?: false
                    }
                },
                onSkip = {
                    if (!isLastQuestion) {
                        skippedQuestions[currentQuestionIndex] = true
                        currentQuestionIndex++
                        selectedOption = null
                        isAnswerSubmitted = false
                        showExplanation = false
                        isBookmarked = false
                        isMarkedForReview = reviewQuestions[currentQuestionIndex] ?: false
                    }
                }
            )
            
            // Banner Ad
            BannerAdView(
                placement = "quiz_screen_bottom"
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizTopBar(
    quizTitle: String,
    difficulty: DifficultyLevel,
    currentQuestion: Int,
    totalQuestions: Int,
    remainingSeconds: Int,
    timerColor: Color,
    onBackClick: () -> Unit
) {
    val gradientColors = listOf(
        Color(0xFF667eea),
        Color(0xFF764ba2)
    )
    
    TopAppBar(
        title = {
            Column {
                Text(
                    text = quizTitle,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 20.sp
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Difficulty chip with white background
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = Color.White.copy(alpha = 0.25f)
                    ) {
                        Text(
                            text = difficulty.name,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    Text(
                        text = "$currentQuestion / $totalQuestions",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.9f),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
        },
        actions = {
            // Timer
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = Color.White.copy(alpha = 0.25f),
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = "Timer",
                        tint = if (remainingSeconds <= 10) Color(0xFFFFEB3B) else Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "${remainingSeconds / 60}:${(remainingSeconds % 60).toString().padStart(2, '0')}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (remainingSeconds <= 10) Color(0xFFFFEB3B) else Color.White
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent
        ),
        modifier = Modifier.background(
            brush = Brush.linearGradient(
                colors = gradientColors,
                start = Offset(0f, 0f),
                end = Offset(1000f, 0f)
            )
        )
    )
}

@Composable
fun DifficultyChip(difficulty: DifficultyLevel) {
    val (color, text) = when (difficulty) {
        DifficultyLevel.EASY -> Color(0xFF4CAF50) to "Easy"
        DifficultyLevel.MEDIUM -> Color(0xFFFF9800) to "Medium"
        DifficultyLevel.HARD -> Color(0xFFF44336) to "Hard"
    }
    
    Surface(
        shape = MaterialTheme.shapes.small,
        color = color.copy(alpha = 0.15f)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = color,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun QuestionCard(
    questionNumber: Int,
    questionText: String,
    questionType: QuestionType,
    difficulty: DifficultyLevel,
    isBookmarked: Boolean,
    isMarkedForReview: Boolean,
    onBookmarkClick: () -> Unit,
    onMarkForReviewClick: () -> Unit
) {
    // Gradient colors based on question type
    val gradientColors = when (questionType) {
        QuestionType.MCQ -> listOf(
            Color(0xFF667eea),
            Color(0xFF764ba2)
        )
        QuestionType.THEORY -> listOf(
            Color(0xFFf093fb),
            Color(0xFFf5576c)
        )
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = MaterialTheme.shapes.large
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = gradientColors,
                        start = Offset(0f, 0f),
                        end = Offset(1000f, 1000f)
                    )
                )
        ) {
            // Decorative circles
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .offset(x = (-40).dp, y = (-40).dp)
                    .background(
                        color = Color.White.copy(alpha = 0.1f),
                        shape = CircleShape
                    )
            )
            
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 20.dp, y = (-20).dp)
                    .background(
                        color = Color.White.copy(alpha = 0.08f),
                        shape = CircleShape
                    )
            )
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header with question number and actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Question Number Badge
                        Surface(
                            shape = CircleShape,
                            color = Color.White,
                            shadowElevation = 4.dp
                        ) {
                            Text(
                                text = "Q$questionNumber",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = gradientColors[0],
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }
                        
                        // Question Type Badge
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = Color.White.copy(alpha = 0.25f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = when (questionType) {
                                        QuestionType.MCQ -> Icons.Default.CheckCircle
                                        QuestionType.THEORY -> Icons.Default.Description
                                    },
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = when (questionType) {
                                        QuestionType.MCQ -> "MCQ"
                                        QuestionType.THEORY -> "Theory"
                                    },
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        // Bookmark
                        IconButton(onClick = onBookmarkClick) {
                            Icon(
                                imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = "Bookmark",
                                tint = if (isBookmarked) Color(0xFFFFD700) else Color.White.copy(alpha = 0.8f)
                            )
                        }
                        
                        // Mark for review
                        IconButton(onClick = onMarkForReviewClick) {
                            Icon(
                                imageVector = Icons.Default.Flag,
                                contentDescription = "Mark for review",
                                tint = if (isMarkedForReview) Color(0xFFFF5722) else Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
                
                // Question text
                Text(
                    text = questionText,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 26.sp,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun QuestionTypeBadge(questionType: QuestionType) {
    val (color, text, icon) = when (questionType) {
        QuestionType.MCQ -> Triple(
            Color(0xFF2196F3),
            "MCQ",
            Icons.Default.CheckCircle
        )
        QuestionType.THEORY -> Triple(
            Color(0xFF9C27B0),
            "Theory",
            Icons.Default.Description
        )
    }
    
    Surface(
        shape = MaterialTheme.shapes.small,
        color = color.copy(alpha = 0.15f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
fun TheoryAnswerCard(
    isSubmitted: Boolean,
    correctAnswer: String
) {
    AnimatedVisibility(
        visible = isSubmitted,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF2196F3).copy(alpha = 0.1f)
            ),
            border = BorderStroke(2.dp, Color(0xFF2196F3).copy(alpha = 0.3f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = Color(0xFF2196F3),
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Expected Answer",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2196F3)
                    )
                }
                
                Divider(color = Color(0xFF2196F3).copy(alpha = 0.3f))
                
                Text(
                    text = correctAnswer,
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 22.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = Color(0xFFFFF9C4)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Lightbulb,
                            contentDescription = null,
                            tint = Color(0xFFF57C00),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Theory questions help you understand concepts deeply. Read the answer carefully.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF5D4037)
                        )
                    }
                }
            }
        }
    }
    
    // Show placeholder before submission
    if (!isSubmitted) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Description,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(48.dp)
                )
                Text(
                    text = "This is a Theory Question",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "The expected answer will be revealed when you submit",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun OptionCard(
    optionText: String,
    isSelected: Boolean,
    isCorrect: Boolean,
    isIncorrect: Boolean,
    isEnabled: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        isCorrect -> Color(0xFF4CAF50).copy(alpha = 0.15f)
        isIncorrect -> Color(0xFFF44336).copy(alpha = 0.15f)
        isSelected -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surface
    }
    
    val borderColor = when {
        isCorrect -> Color(0xFF4CAF50)
        isIncorrect -> Color(0xFFF44336)
        isSelected -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = isEnabled) { onClick() },
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = BorderStroke(2.dp, borderColor),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 1.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Indicator icon
            when {
                isCorrect -> Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Correct",
                    tint = Color(0xFF4CAF50)
                )
                isIncorrect -> Icon(
                    imageVector = Icons.Default.Cancel,
                    contentDescription = "Incorrect",
                    tint = Color(0xFFF44336)
                )
                isSelected -> Icon(
                    imageVector = Icons.Default.RadioButtonChecked,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary
                )
                else -> Icon(
                    imageVector = Icons.Default.RadioButtonUnchecked,
                    contentDescription = "Not selected",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Text(
                text = optionText,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected || isCorrect || isIncorrect) FontWeight.SemiBold else FontWeight.Normal
            )
        }
    }
}

@Composable
fun TheoryExplanationCard(
    explanation: String,
    showExplanation: Boolean,
    onToggleExplanation: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF9C27B0).copy(alpha = 0.1f)
        ),
        border = BorderStroke(2.dp, Color(0xFF9C27B0).copy(alpha = 0.3f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = null,
                    tint = Color(0xFF9C27B0),
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Detailed Explanation",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF9C27B0)
                )
            }
            
            Divider(color = Color(0xFF9C27B0).copy(alpha = 0.3f))
            
            // Explanation toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExplanation() }
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (showExplanation) "Hide Details" else "Show Details",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = if (showExplanation) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = "Toggle explanation"
                )
            }
            
            // Explanation content
            AnimatedVisibility(
                visible = showExplanation,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Text(
                    text = explanation,
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 22.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun ExplanationCard(
    isCorrect: Boolean,
    explanation: String,
    showExplanation: Boolean,
    onToggleExplanation: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isCorrect) 
                Color(0xFF4CAF50).copy(alpha = 0.1f) 
            else 
                Color(0xFFF44336).copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Result header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isCorrect) Icons.Default.CheckCircle else Icons.Default.Cancel,
                        contentDescription = null,
                        tint = if (isCorrect) Color(0xFF4CAF50) else Color(0xFFF44336),
                        modifier = Modifier.size(28.dp)
                    )
                    Text(
                        text = if (isCorrect) "Correct!" else "Incorrect",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isCorrect) Color(0xFF4CAF50) else Color(0xFFF44336)
                    )
                }
            }
            
            Divider()
            
            // Explanation toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExplanation() }
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Explanation",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = if (showExplanation) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = "Toggle explanation"
                )
            }
            
            // Explanation content
            AnimatedVisibility(
                visible = showExplanation,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Text(
                    text = explanation,
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 22.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun ActionButtonsSection(
    questionType: QuestionType,
    selectedOption: String?,
    isAnswerSubmitted: Boolean,
    isLastQuestion: Boolean,
    onSubmit: () -> Unit,
    onNext: () -> Unit,
    onSkip: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Primary action button
        if (!isAnswerSubmitted) {
            // For MCQ, require selection. For THEORY, always enabled
            val isSubmitEnabled = when (questionType) {
                QuestionType.MCQ -> selectedOption != null
                QuestionType.THEORY -> true
            }
            
            val buttonText = when (questionType) {
                QuestionType.MCQ -> "Submit Answer"
                QuestionType.THEORY -> "Reveal Answer"
            }
            
            Button(
                onClick = onSubmit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = isSubmitEnabled,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (questionType == QuestionType.THEORY)
                        Color(0xFF9C27B0)
                    else
                        MaterialTheme.colorScheme.primary
                )
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (questionType == QuestionType.THEORY) {
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = null
                        )
                    }
                    Text(
                        text = buttonText,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            // Skip button
            if (!isLastQuestion) {
                OutlinedButton(
                    onClick = onSkip,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(
                        text = "Skip Question",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        } else {
            // Next/Finish button
            Button(
                onClick = onNext,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isLastQuestion) 
                        Color(0xFF4CAF50) 
                    else 
                        MaterialTheme.colorScheme.primary
                )
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isLastQuestion) "Finish Quiz" else "Next Question",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        imageVector = if (isLastQuestion) Icons.Default.Done else Icons.Default.ArrowForward,
                        contentDescription = null
                    )
                }
            }
        }
    }
}
