package com.prepstack.voiceinterview.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.prepstack.voiceinterview.core.model.*
import com.prepstack.voiceinterview.core.model.InterviewSummary as CoreInterviewSummary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale
import com.prepstack.voiceinterview.R
import java.util.*

/**
 * Main screen for the Voice Interview feature.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceInterviewScreen(
    viewModel: VoiceInterviewViewModel = viewModel(),
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showExitDialog by remember { mutableStateOf(false) }
    
    // Show confirmation dialog when trying to exit during active interview
    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("Exit Interview?") },
            text = { Text("Are you sure you want to exit the interview? Your progress will be lost.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showExitDialog = false
                        // Navigate back immediately without waiting for interview to end
                        onBackClick()
                    }
                ) {
                    Text("Exit")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Voice Interview") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (uiState) {
                is VoiceInterviewUiState.Loading -> {
                    LoadingScreen()
                }
                is VoiceInterviewUiState.TopicSelection -> {
                    TopicSelectionScreen(
                        topics = (uiState as VoiceInterviewUiState.TopicSelection).topics,
                        onTopicSelected = { topic -> viewModel.selectTopic(topic) }
                    )
                }
                is VoiceInterviewUiState.ConfigureInterview -> {
                    val state = uiState as VoiceInterviewUiState.ConfigureInterview
                    InterviewConfigScreen(
                        topic = state.topic,
                        onStartInterview = { config -> 
                            viewModel.startInterview(config) 
                        },
                        onBackToTopicSelection = { viewModel.resetTopicSelection() }
                    )
                }
                is VoiceInterviewUiState.ActiveInterview -> {
                    val state = uiState as VoiceInterviewUiState.ActiveInterview
                    ActiveInterviewScreen(
                        interviewState = state.interviewState,
                        onStartListening = { viewModel.startListening() },
                        onStopListening = { viewModel.stopListening() },
                        onSubmitTextAnswer = { answer -> viewModel.submitTextAnswer(answer) },
                        onContinue = { viewModel.nextQuestion() },
                        onEndInterview = { viewModel.endInterview() }
                    )
                }
                is VoiceInterviewUiState.InterviewSummary -> {
                    val state = uiState as VoiceInterviewUiState.InterviewSummary
                    InterviewSummaryScreen(
                        summary = state.summary,
                        onDone = { viewModel.resetInterview() }
                    )
                }
                is VoiceInterviewUiState.Error -> {
                    val state = uiState as VoiceInterviewUiState.Error
                    ErrorScreen(
                        errorMessage = state.errorMessage,
                        onRetry = { viewModel.retryAfterError() }
                    )
                }
            }
        }
    }
}

@Composable
fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            )
            Text(
                text = "Initializing Interview System...",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
fun TopicSelectionScreen(
    topics: List<InterviewTopic>,
    onTopicSelected: (InterviewTopic) -> Unit
) {
    var customTopicName by remember { mutableStateOf("") }
    var selectedDifficulty by remember { mutableStateOf(DifficultyLevel.INTERMEDIATE) }
    var showCustomSection by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Select Interview Topic",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Custom Topic Section - Enhanced UI
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            shape = RoundedCornerShape(20.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showCustomSection = !showCustomSection }
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                modifier = Modifier.size(48.dp),
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Create,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            
                            Column {
                                Text(
                                    text = "Create Your Own Topic",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Personalize your interview experience",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        ) {
                            Icon(
                                imageVector = if (showCustomSection) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = if (showCustomSection) "Collapse" else "Expand",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                    
                    if (showCustomSection) {
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        // Topic Input with better styling
                        OutlinedTextField(
                            value = customTopicName,
                            onValueChange = { customTopicName = it },
                            label = { Text("What topic do you want to master?") },
                            placeholder = { Text("e.g., Flutter, Docker, AWS, GraphQL...") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            trailingIcon = {
                                if (customTopicName.isNotEmpty()) {
                                    IconButton(onClick = { customTopicName = "" }) {
                                        Icon(
                                            imageVector = Icons.Default.Clear,
                                            contentDescription = "Clear",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                            )
                        )
                        
                        // Popular suggestions
                        if (customTopicName.isEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "💡 Popular topics:",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(listOf("Flutter", "Docker", "AWS", "GraphQL", "TypeScript", "MongoDB")) { suggestion ->
                                    SuggestionChip(
                                        onClick = { customTopicName = suggestion },
                                        label = { Text(suggestion, style = MaterialTheme.typography.bodySmall) },
                                        icon = {
                                            Icon(
                                                Icons.Default.Add,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        // Difficulty selection with icons
                        Text(
                            text = "Choose Your Challenge Level",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(DifficultyLevel.values()) { difficulty ->
                                val isSelected = difficulty == selectedDifficulty
                                val difficultyIcon = when (difficulty) {
                                    DifficultyLevel.BEGINNER -> "🌱"
                                    DifficultyLevel.INTERMEDIATE -> "⚡"
                                    DifficultyLevel.ADVANCED -> "🔥"
                                    DifficultyLevel.EXPERT -> "👑"
                                }
                                
                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .clickable { selectedDifficulty = difficulty },
                                    color = if (isSelected) 
                                        MaterialTheme.colorScheme.primary 
                                    else 
                                        MaterialTheme.colorScheme.surface,
                                    border = if (!isSelected) 
                                        BorderStroke(2.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)) 
                                    else null,
                                    shadowElevation = if (isSelected) 4.dp else 0.dp
                                ) {
                                    Column(
                                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = difficultyIcon,
                                            fontSize = 24.sp
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = difficulty.name.lowercase()
                                                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        // Start button with gradient effect
                        Button(
                            onClick = {
                                if (customTopicName.isNotBlank()) {
                                    val customTopic = InterviewTopic(
                                        id = "custom_${customTopicName.lowercase().replace(" ", "_")}",
                                        name = customTopicName,
                                        description = "Custom interview topic: $customTopicName",
                                        category = InterviewCategory.TECHNICAL,
                                        difficultyLevels = listOf(selectedDifficulty),
                                        estimatedTimeMinutes = 15,
                                        iconUrl = ""
                                    )
                                    onTopicSelected(customTopic)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            enabled = customTopicName.isNotBlank(),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 4.dp,
                                pressedElevation = 8.dp
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Start Custom Interview",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Divider with text
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HorizontalDivider(
                modifier = Modifier.weight(1f),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )
            Text(
                text = "  Or choose a predefined topic  ",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            HorizontalDivider(
                modifier = Modifier.weight(1f),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(topics) { topic ->
                TopicCard(topic = topic, onClick = { onTopicSelected(topic) })
            }
        }
    }
}

@Composable
fun TopicCard(topic: InterviewTopic, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = topic.iconUrl,
                contentDescription = topic.name,
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(8.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = topic.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = topic.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
                
                Row(
                    modifier = Modifier.padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    
                    Text(
                        text = "${topic.estimatedTimeMinutes} min",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Icon(
                        imageVector = when (topic.category) {
                            InterviewCategory.TECHNICAL -> Icons.Default.Code
                            InterviewCategory.BEHAVIORAL -> Icons.Default.Person
                            InterviewCategory.SYSTEM_DESIGN -> Icons.Default.Architecture
                            InterviewCategory.LEADERSHIP -> Icons.Default.Groups
                            else -> Icons.Default.Info
                        },
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    
                    Text(
                        text = topic.category.name.lowercase()
                            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }
            
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Select",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun InterviewConfigScreen(
    topic: InterviewTopic,
    onStartInterview: (InterviewConfig) -> Unit,
    onBackToTopicSelection: () -> Unit
) {
    var selectedDifficulty by remember { mutableStateOf(topic.difficultyLevels.first()) }
    var durationMinutes by remember { mutableStateOf(15) }
    
    // Calculate question count dynamically based on difficulty and duration (like real interviews)
    val questionCount = remember(selectedDifficulty, durationMinutes) {
        calculateQuestionCount(selectedDifficulty, durationMinutes)
    }
    
    // Calculate average time per question
    val avgTimePerQuestion = if (questionCount > 0) durationMinutes / questionCount else 0
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            IconButton(onClick = onBackToTopicSelection) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            
            Text(
                text = "Configure ${topic.name} Interview",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
        
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Topic info
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = topic.iconUrl,
                        contentDescription = topic.name,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(8.dp)
                    )
                    
                    Column(modifier = Modifier.padding(start = 16.dp)) {
                        Text(
                            text = topic.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Text(
                            text = topic.category.name.lowercase()
                                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Divider(modifier = Modifier.padding(vertical = 16.dp))
                
                // Difficulty selection
                Text(
                    text = "Difficulty Level",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    items(topic.difficultyLevels) { difficulty ->
                        val isSelected = difficulty == selectedDifficulty
                        
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { selectedDifficulty = difficulty },
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                            border = if (isSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                        ) {
                            Text(
                                text = difficulty.name.lowercase()
                                    .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
                
                // Duration
                Text(
                    text = "Approximate Duration: $durationMinutes minutes",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
                
                Slider(
                    value = durationMinutes.toFloat(),
                    onValueChange = { durationMinutes = it.toInt() },
                    valueRange = 5f..30f,
                    steps = 5,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                
                // Interview summary info
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Questions",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "$questionCount",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Avg Time/Q",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${avgTimePerQuestion} min",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                text = "Total Time",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "$durationMinutes min",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Start button
        Button(
            onClick = {
                val config = InterviewConfig(
                    topicId = topic.id,
                    difficultyLevel = selectedDifficulty,
                    durationMinutes = durationMinutes,
                    questionCount = questionCount,
                    adaptiveFeedback = true
                )
                onStartInterview(config)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            )
        ) {
            Icon(
                imageVector = Icons.Default.Mic,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Text(
                text = "Start Voice Interview",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun ActiveInterviewScreen(
    interviewState: InterviewState,
    onStartListening: () -> Unit,
    onStopListening: () -> Unit,
    onSubmitTextAnswer: (String) -> Unit,
    onContinue: () -> Unit,
    onEndInterview: () -> Unit
) {
    var textAnswer by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Status indicator
        StatusIndicator(status = interviewState.status)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Main content area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (interviewState.status) {
                InterviewStatus.INITIALIZING -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                
                InterviewStatus.WAITING_FOR_QUESTION,
                InterviewStatus.PRESENTING_QUESTION -> {
                    // Show only wave animation without question text
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            VoiceRiveAnimation(
                                animationResId = R.raw.takingbear,
                                isListening = false,
                                modifier = Modifier.size(200.dp)
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            Text(
                                text = "Listen carefully...",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                InterviewStatus.LISTENING -> {
                    // Show wave animation while listening
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            VoiceRiveAnimation(
                                animationResId = R.raw.takingbear,
                                isListening = true,
                                modifier = Modifier.size(200.dp)
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            Text(
                                text = "Listening...",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            
                            // Show error message if there's a recoverable error
                            if (!interviewState.error.isNullOrEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Surface(
                                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "⚠️ ${interviewState.error}. Trying again...",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            } else {
                                // Show helpful hint
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Speak clearly into your microphone",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(32.dp))
                            
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 32.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                OutlinedButton(
                                    onClick = onStopListening,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Stop")
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                OutlinedButton(
                                    onClick = {
                                        scope.launch {
                                            onStopListening()
                                            delay(300)
                                            textAnswer = " " // Set to space to trigger dialog
                                        }
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        Icons.Default.KeyboardAlt,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Type")
                                }
                            }
                        }
                    }
                }
                
                InterviewStatus.PROCESSING_RESPONSE -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator()
                            Text("Processing your answer...")
                        }
                    }
                }
                
                InterviewStatus.PRESENTING_FEEDBACK -> {
                    interviewState.currentResponse?.let { response ->
                        // Auto-proceed to next question or end interview after showing feedback
                        LaunchedEffect(response.questionId) {
                            delay(3000) // Wait 3 seconds to read feedback
                            if (response.nextQuestionId != null) {
                                onContinue() // Move to next question
                            } else {
                                onEndInterview() // Finish interview
                            }
                        }
                        
                        FeedbackCard(response = response)
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Show auto-proceed indicator
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            LinearProgressIndicator(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp))
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (response.nextQuestionId != null) 
                                    "Moving to next question..." 
                                else 
                                    "Finishing interview...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // Option to skip waiting
                            TextButton(onClick = {
                                if (response.nextQuestionId != null) {
                                    onContinue() // Move to next question
                                } else {
                                    onEndInterview() // Finish interview
                                }
                            }) {
                                Text("Skip waiting")
                            }
                        }
                    }
                }
                
                InterviewStatus.COMPLETED,
                InterviewStatus.ERROR -> {
                    // These states are handled by the parent composable
                }
            }
            
            // Type answer dialog
            if (textAnswer.isNotEmpty()) {
                // Clear the initial space character if present
                LaunchedEffect(Unit) {
                    if (textAnswer.trim().isEmpty()) {
                        textAnswer = ""
                    }
                }
                
                Dialog(onDismissRequest = { textAnswer = "" }) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Type your answer",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            OutlinedTextField(
                                value = textAnswer,
                                onValueChange = { textAnswer = it },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("Your answer") },
                                minLines = 3,
                                placeholder = { Text("Type your answer here...") }
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(onClick = { textAnswer = "" }) {
                                    Text("Cancel")
                                }
                                
                                Spacer(modifier = Modifier.width(8.dp))
                                
                                Button(
                                    onClick = {
                                        onSubmitTextAnswer(textAnswer)
                                        textAnswer = ""
                                    },
                                    enabled = textAnswer.isNotBlank()
                                ) {
                                    Text("Submit")
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // Bottom controls
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Show the microphone button only in certain states
            if (interviewState.status == InterviewStatus.WAITING_FOR_QUESTION || 
                interviewState.status == InterviewStatus.PRESENTING_QUESTION) {
                // Start listening button
                Button(
                    onClick = onStartListening,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Start Listening"
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = "Start Speaking",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
            
            // End interview button
            TextButton(
                onClick = onEndInterview,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text("End Interview")
            }
        }
    }
}

@Composable
fun StatusIndicator(status: InterviewStatus) {
    val statusText = when (status) {
        InterviewStatus.INITIALIZING -> "Initializing..."
        InterviewStatus.WAITING_FOR_QUESTION -> "Preparing question..."
        InterviewStatus.PRESENTING_QUESTION -> "Question ready"
        InterviewStatus.LISTENING -> "Listening..."
        InterviewStatus.PROCESSING_RESPONSE -> "Processing your answer..."
        InterviewStatus.PRESENTING_FEEDBACK -> "Feedback"
        InterviewStatus.COMPLETED -> "Interview completed"
        InterviewStatus.ERROR -> "Error occurred"
    }
    
    val statusColor = when (status) {
        InterviewStatus.INITIALIZING, 
        InterviewStatus.WAITING_FOR_QUESTION -> MaterialTheme.colorScheme.tertiary
        InterviewStatus.PRESENTING_QUESTION -> MaterialTheme.colorScheme.primary
        InterviewStatus.LISTENING -> Color(0xFF4CAF50) // Green
        InterviewStatus.PROCESSING_RESPONSE -> MaterialTheme.colorScheme.tertiary
        InterviewStatus.PRESENTING_FEEDBACK -> MaterialTheme.colorScheme.secondary
        InterviewStatus.COMPLETED -> MaterialTheme.colorScheme.primary
        InterviewStatus.ERROR -> MaterialTheme.colorScheme.error
    }
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = statusColor.copy(alpha = 0.1f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = statusColor,
                shape = CircleShape,
                modifier = Modifier.size(12.dp)
            ) { }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Text(
                text = statusText,
                style = MaterialTheme.typography.bodyMedium,
                color = statusColor
            )
        }
    }
}

@Composable
fun CircularWaveAnimation(isListening: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave_animation")
    
    // Create multiple animated waves with different delays and speeds
    val waves = List(4) { index ->
        val delay = index * 200
        val duration = 2000 + (index * 100)
        
        val scale by infiniteTransition.animateFloat(
            initialValue = 0.3f,
            targetValue = 1.5f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = duration,
                    easing = LinearEasing
                ),
                repeatMode = RepeatMode.Restart,
                initialStartOffset = StartOffset(delay)
            ),
            label = "wave_scale_$index"
        )
        
        val alpha by infiniteTransition.animateFloat(
            initialValue = 0.8f,
            targetValue = 0f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = duration,
                    easing = LinearEasing
                ),
                repeatMode = RepeatMode.Restart,
                initialStartOffset = StartOffset(delay)
            ),
            label = "wave_alpha_$index"
        )
        
        scale to alpha
    }
    
    // Pulsing effect for center circle when listening
    val centerPulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isListening) 1.15f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1000,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "center_pulse"
    )
    
    Box(
        modifier = Modifier
            .size(280.dp)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        // Draw animated wave circles
        waves.forEach { (scale, alpha) ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        this.alpha = alpha
                    }
                    .background(
                        color = if (isListening) 
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                        else 
                            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f),
                        shape = CircleShape
                    )
            )
        }
        
        // Center circle with gradient
        Box(
            modifier = Modifier
                .size(120.dp)
                .graphicsLayer {
                    scaleX = centerPulse
                    scaleY = centerPulse
                }
                .background(
                    brush = Brush.radialGradient(
                        colors = if (isListening) listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                        ) else listOf(
                            MaterialTheme.colorScheme.tertiary,
                            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.7f)
                        )
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isListening) Icons.Default.Mic else Icons.Default.VolumeUp,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(48.dp)
            )
        }
    }
}

@Composable
fun QuestionCard(question: InterviewQuestion) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text(
                        text = question.difficultyLevel.name.lowercase()
                            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = question.type.name.lowercase()
                            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                            .replace("_", " "),
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = "Duration",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                
                Text(
                    text = "${question.expectedDurationSeconds / 60} min",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
            
            Text(
                text = question.text,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
    }
}

@Composable
fun ListeningIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val sizes = List(5) { index ->
        val delay = index * 100
        val animatedValue by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = keyframes {
                    durationMillis = 1500
                    0f at 0 with LinearEasing
                    1f at 750 with LinearEasing
                    0f at 1500 with LinearEasing
                },
                repeatMode = RepeatMode.Restart,
                initialStartOffset = StartOffset(delay)
            ),
            label = "pulse-$index"
        )
        animatedValue
    }
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .background(
                MaterialTheme.colorScheme.surface,
                RoundedCornerShape(8.dp)
            )
            .border(
                1.dp,
                MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                RoundedCornerShape(8.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            sizes.forEach { size ->
                Box(
                    modifier = Modifier
                        .width(8.dp)
                        .height(8.dp + (size * 32).dp)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                    MaterialTheme.colorScheme.primary
                                )
                            ),
                            RoundedCornerShape(4.dp)
                        )
                )
            }
        }
        
        Text(
            text = "Listening... Speak your answer",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 8.dp)
        )
    }
}

@Composable
fun FeedbackCard(response: InterviewResponse) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Score indicator
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Score",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
                val scoreColor = when {
                    response.evaluation.score >= 8 -> Color(0xFF4CAF50) // Green
                    response.evaluation.score >= 6 -> Color(0xFFFFC107) // Yellow
                    else -> Color(0xFFF44336) // Red
                }
                
                Surface(
                    color = scoreColor,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "${response.evaluation.score}/10",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
            
            Divider(modifier = Modifier.padding(vertical = 16.dp))
            
            // Feedback summary
            Text(
                text = "Feedback",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = response.evaluation.feedbackSummary,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
            )
            
            // Strengths
            Text(
                text = "Strengths",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            
            response.evaluation.strengths.forEach { strength ->
                Row(
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier
                            .size(16.dp)
                            .padding(top = 4.dp)
                    )
                    
                    Text(
                        text = strength,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Improvements
            Text(
                text = "Areas for Improvement",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            
            response.evaluation.improvements.forEach { improvement ->
                Row(
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(16.dp)
                            .padding(top = 4.dp)
                    )
                    
                    Text(
                        text = improvement,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Additional metrics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Communication",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Text(
                        text = "${response.evaluation.communicationClarity}/10",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                if (response.evaluation.technicalAccuracy != null) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Technical Accuracy",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Text(
                            text = "${response.evaluation.technicalAccuracy}/10",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun InterviewSummaryScreen(
    summary: CoreInterviewSummary,
    onDone: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Interview Summary",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Overall score card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Overall Score",
                    style = MaterialTheme.typography.titleMedium
                )
                
                Text(
                    text = "${summary.overallScore}",
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Text(
                    text = "out of 10",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Text(
                    text = "${summary.completedQuestionsCount} of ${summary.totalQuestionsAsked} questions completed",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Feedback
        Text(
            text = "General Feedback",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = summary.generalFeedback,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 8.dp)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Strengths
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF4CAF50).copy(alpha = 0.1f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Strengths",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4CAF50)
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    summary.strengthAreas.forEach { strength ->
                        Row(
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(16.dp)
                            )
                            
                            Text(
                                text = strength,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Improvements
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Areas to Improve",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    summary.improvementAreas.forEach { area ->
                        Row(
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowUpward,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            
                            Text(
                                text = area,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Done button
        Button(
            onClick = onDone,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(
                text = "Done",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun ErrorScreen(
    errorMessage: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.ErrorOutline,
            contentDescription = "Error",
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(64.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Error",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.error
        )
        
        Text(
            text = errorMessage,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
        )
        
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            )
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Retry"
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Text("Try Again")
        }
    }
}

/**
 * Calculate dynamic question count based on difficulty level and interview duration.
 * Mimics real interview scenarios where:
 * - Beginner: More questions, simpler and quicker to answer
 * - Intermediate: Moderate questions, balanced complexity
 * - Advanced: Fewer questions, more in-depth and time-consuming
 * - Expert: Even fewer questions, highly complex and detailed
 */
private fun calculateQuestionCount(difficulty: DifficultyLevel, durationMinutes: Int): Int {
    // Base time per question varies by difficulty (in minutes)
    val timePerQuestion = when (difficulty) {
        DifficultyLevel.BEGINNER -> 2.0      // Simpler questions, faster to answer
        DifficultyLevel.INTERMEDIATE -> 2.5  // Moderate complexity
        DifficultyLevel.ADVANCED -> 3.5      // More complex, requires deeper thought
        DifficultyLevel.EXPERT -> 4.5        // Highly complex, detailed explanations needed
    }
    
    // Calculate base question count
    val calculatedCount = (durationMinutes / timePerQuestion).toInt()
    
    // Ensure reasonable bounds (3-12 questions)
    return calculatedCount.coerceIn(3, 12)
}