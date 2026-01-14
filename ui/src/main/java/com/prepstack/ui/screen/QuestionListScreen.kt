package com.prepstack.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.prepstack.ads.BannerAdView
import com.prepstack.core.util.Constants
import com.prepstack.domain.model.DifficultyLevel
import com.prepstack.domain.model.Question
import com.prepstack.domain.model.QuestionType
import com.prepstack.ui.components.EnhancedTopBar
import com.prepstack.ui.viewmodel.QuestionListUiState
import com.prepstack.ui.viewmodel.QuestionListViewModel

/**
 * Question List Screen - Shows all questions in a topic
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionListScreen(
    topicId: String,
    viewModel: QuestionListViewModel,
    onQuestionClick: (String) -> Unit = {},
    onBackClick: () -> Unit
) {
    LaunchedEffect(topicId) {
        viewModel.loadQuestions(topicId)
    }
    
    val uiState by viewModel.uiState.collectAsState()
    var showFilterMenu by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf<QuestionType?>(null) }
    var sortOrder by remember { mutableStateOf("easy_hard") } // default, easy_hard, hard_easy
    
    Scaffold(
        topBar = {
            EnhancedTopBar(
                title = "Questions",
                gradientColors = listOf(Color(0xFF3E78B6), Color(0xFF3890EF)),
                onBackClick = onBackClick,
                actions = {
                    // Filter Button
                    IconButton(onClick = { showFilterMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Filter",
                            tint = Color.White
                        )
                    }
                    DropdownMenu(
                        expanded = showFilterMenu,
                        onDismissRequest = { showFilterMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("All Questions") },
                            onClick = {
                                selectedFilter = null
                                showFilterMenu = false
                            },
                            leadingIcon = {
                                if (selectedFilter == null) {
                                    Icon(Icons.Default.Check, null)
                                }
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("MCQ Only") },
                            onClick = {
                                selectedFilter = QuestionType.MCQ
                                showFilterMenu = false
                            },
                            leadingIcon = {
                                if (selectedFilter == QuestionType.MCQ) {
                                    Icon(Icons.Default.Check, null)
                                }
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Theory Only") },
                            onClick = {
                                selectedFilter = QuestionType.THEORY
                                showFilterMenu = false
                            },
                            leadingIcon = {
                                if (selectedFilter == QuestionType.THEORY) {
                                    Icon(Icons.Default.Check, null)
                                }
                            }
                        )
                    }
                    
                    // Sort Button
                    IconButton(onClick = { showSortMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.Sort,
                            contentDescription = "Sort",
                            tint = Color.White
                        )
                    }
                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Default Order") },
                            onClick = {
                                sortOrder = "default"
                                showSortMenu = false
                            },
                            leadingIcon = {
                                if (sortOrder == "default") {
                                    Icon(Icons.Default.Check, null)
                                }
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Easy → Hard") },
                            onClick = {
                                sortOrder = "easy_hard"
                                showSortMenu = false
                            },
                            leadingIcon = {
                                if (sortOrder == "easy_hard") {
                                    Icon(Icons.Default.Check, null)
                                }
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Hard → Easy") },
                            onClick = {
                                sortOrder = "hard_easy"
                                showSortMenu = false
                            },
                            leadingIcon = {
                                if (sortOrder == "hard_easy") {
                                    Icon(Icons.Default.Check, null)
                                }
                            }
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            when (uiState) {
                is QuestionListUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                
                is QuestionListUiState.Success -> {
                    val allQuestions = (uiState as QuestionListUiState.Success).questions
                    
                    // Apply filters and sorting
                    val questions = allQuestions
                        .let { list ->
                            if (selectedFilter != null) {
                                list.filter { it.type == selectedFilter }
                            } else list
                        }
                        .let { list ->
                            when (sortOrder) {
                                "easy_hard" -> list.sortedBy {
                                    when (it.difficulty) {
                                        DifficultyLevel.EASY -> 1
                                        DifficultyLevel.MEDIUM -> 2
                                        DifficultyLevel.HARD -> 3
                                    }
                                }
                                "hard_easy" -> list.sortedByDescending {
                                    when (it.difficulty) {
                                        DifficultyLevel.EASY -> 1
                                        DifficultyLevel.MEDIUM -> 2
                                        DifficultyLevel.HARD -> 3
                                    }
                                }
                                else -> list
                            }
                        }
                    
                    if (questions.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No questions available",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(questions) { question ->
                                QuestionItem(
                                    question = question,
                                    onClick = { onQuestionClick(question.id) },
                                    onBookmarkClick = {
                                        viewModel.toggleBookmark(
                                            question.id,
                                            question.topicId,
                                            question.domainId
                                        )
                                    }
                                )
                            }
                        }
                        
                        BannerAdView()
                    }
                }
                
                is QuestionListUiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = (uiState as QuestionListUiState.Error).message,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QuestionItem(
    question: Question,
    onClick: () -> Unit,
    onBookmarkClick: () -> Unit
) {
    // Simple design for THEORY, gradient for MCQ
    val isTheory = question.type == QuestionType.THEORY
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isTheory) 1.dp else 4.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = if (isTheory) 
                MaterialTheme.colorScheme.surface 
            else 
                Color.Transparent
        ),
        border = if (isTheory) 
            BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant) 
        else 
            null
    ) {
        if (isTheory) {
            // Simple design for THEORY
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Top row with badges and bookmark
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Question type badge
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = MaterialTheme.shapes.small
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Description,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = question.type.name,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                        
                        // Difficulty badge
                        Surface(
                            color = when (question.difficulty) {
                                DifficultyLevel.EASY -> Color(0xFF4CAF50).copy(alpha = 0.15f)
                                DifficultyLevel.MEDIUM -> Color(0xFFFF9800).copy(alpha = 0.15f)
                                DifficultyLevel.HARD -> Color(0xFFF44336).copy(alpha = 0.15f)
                            },
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(
                                text = question.difficulty.name,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = when (question.difficulty) {
                                    DifficultyLevel.EASY -> Color(0xFF4CAF50)
                                    DifficultyLevel.MEDIUM -> Color(0xFFFF9800)
                                    DifficultyLevel.HARD -> Color(0xFFF44336)
                                }
                            )
                        }
                    }
                    
                    // Bookmark button
                    IconButton(onClick = onBookmarkClick) {
                        Icon(
                            imageVector = if (question.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Bookmark",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                
                // Question text
                Text(
                    text = question.questionText,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 24.sp,
                    maxLines = 3
                )
            }
        } else {
            // Gradient design for MCQ
            val gradientColors = listOf(
                Color(0xFF667eea),
                Color(0xFF764ba2)
            )
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.linearGradient(
                            colors = gradientColors,
                            start = Offset(0f, 0f),
                            end = Offset(1200f, 1200f)
                        )
                    )
            ) {
                // Decorative circles
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .offset(x = (-30).dp, y = (-30).dp)
                        .background(
                            color = Color.White.copy(alpha = 0.12f),
                            shape = CircleShape
                        )
                )
                
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .align(Alignment.BottomEnd)
                        .offset(x = 15.dp, y = 15.dp)
                        .background(
                            color = Color.White.copy(alpha = 0.08f),
                            shape = CircleShape
                        )
                )
                
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Top row with badges and bookmark
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Question type badge
                            Surface(
                                color = Color.White.copy(alpha = 0.25f),
                                shape = MaterialTheme.shapes.small
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = question.type.name,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                            
                            // Difficulty badge
                            Surface(
                                color = when (question.difficulty) {
                                    DifficultyLevel.EASY -> Color(0xFF4CAF50).copy(alpha = 0.3f)
                                    DifficultyLevel.MEDIUM -> Color(0xFFFF9800).copy(alpha = 0.3f)
                                    DifficultyLevel.HARD -> Color(0xFFF44336).copy(alpha = 0.3f)
                                },
                                shape = MaterialTheme.shapes.small
                            ) {
                                Text(
                                    text = question.difficulty.name,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                        
                        // Bookmark button
                        IconButton(onClick = onBookmarkClick) {
                            Icon(
                                imageVector = if (question.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = "Bookmark",
                                tint = Color.White.copy(alpha = 0.8f),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    
                    // Question text
                    Text(
                        text = question.questionText,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        lineHeight = 24.sp,
                        maxLines = 3
                    )
                }
            }
        }
    }
}
