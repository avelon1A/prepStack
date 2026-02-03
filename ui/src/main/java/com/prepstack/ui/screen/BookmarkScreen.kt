package com.prepstack.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prepstack.ads.BannerAdView
import com.prepstack.domain.model.DifficultyLevel
import com.prepstack.domain.model.Question
import com.prepstack.domain.model.QuestionType
import com.prepstack.ui.components.EnhancedTopBar
import com.prepstack.ui.viewmodel.BookmarkUiState
import com.prepstack.ui.viewmodel.BookmarkViewModel

/**
 * Bookmark Screen - Displays all bookmarked questions
 * Follows the same design pattern as QuestionListScreen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarkScreen(
    viewModel: BookmarkViewModel,
    onQuestionClick: (String) -> Unit,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            EnhancedTopBar(
                title = "Bookmarks",
                gradientColors = listOf(Color(0xFF3E78B6), Color(0xFF3890EF)), // More professional blue gradient
                onBackClick = onBackClick,
                actions = {
                    // Refresh button
                    IconButton(
                        onClick = {
                            // Force refresh with loading state
                            viewModel.refresh()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = Color.Black
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            when (uiState) {
                is BookmarkUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                
                is BookmarkUiState.Empty -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.BookmarkBorder,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                            
                            Text(
                                text = "No Bookmarks Yet",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            
                            Text(
                                text = "Bookmark questions to save them here\nfor quick access later",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                
                is BookmarkUiState.Success -> {
                    val questions = (uiState as BookmarkUiState.Success).questions
                    
                    Column {
                        // Header with count
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${questions.size} Bookmarked Question${if (questions.size != 1) "s" else ""}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                
                                Icon(
                                    imageVector = Icons.Default.Bookmark,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(questions) { question ->
                                BookmarkedQuestionItem(
                                    question = question,
                                    onClick = { onQuestionClick(question.id) },
                                    onRemoveBookmark = {
                                        viewModel.removeBookmark(question.id)
                                    }
                                )
                            }
                        }
                        
                        BannerAdView()
                    }
                }
                
                is BookmarkUiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                            
                            Text(
                                text = (uiState as BookmarkUiState.Error).message,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center
                            )
                            
                            Button(onClick = { viewModel.refresh() }) {
                                Text("Try Again")
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Composable for displaying a single bookmarked question item
 * Follows the same design as QuestionItem in QuestionListScreen
 */
@Composable
fun BookmarkedQuestionItem(
    question: Question,
    onClick: () -> Unit,
    onRemoveBookmark: () -> Unit
) {
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
                    
                    // Remove bookmark button
                    IconButton(onClick = onRemoveBookmark) {
                        Icon(
                            imageVector = Icons.Default.Bookmark,
                            contentDescription = "Remove Bookmark",
                            tint = Color(0xFFFF6B6B),
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
                        
                        // Remove bookmark button
                        IconButton(onClick = onRemoveBookmark) {
                            Icon(
                                imageVector = Icons.Default.Bookmark,
                                contentDescription = "Remove Bookmark",
                                tint = Color(0xFFFF6B6B),
                                modifier = Modifier.size(24.dp)
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
