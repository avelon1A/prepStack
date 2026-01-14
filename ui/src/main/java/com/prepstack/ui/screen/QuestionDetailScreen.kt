package com.prepstack.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.prepstack.ads.BannerAdView
import com.prepstack.core.util.Constants
import com.prepstack.domain.model.Question
import com.prepstack.domain.model.QuestionType
import com.prepstack.ui.components.EnhancedTopBar
import com.prepstack.ui.viewmodel.QuestionDetailUiState
import com.prepstack.ui.viewmodel.QuestionDetailViewModel

/**
 * Question Detail Screen - Shows complete question details with answer and explanation
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionDetailScreen(
    questionId: String,
    viewModel: QuestionDetailViewModel,
    onBackClick: () -> Unit
) {
    LaunchedEffect(questionId) {
        viewModel.loadQuestion(questionId)
        // Auto-reveal answer and explanation for THEORY questions
        viewModel.revealAnswer()
        viewModel.revealExplanation()
    }
    
    val uiState by viewModel.uiState.collectAsState()
    val selectedAnswer by viewModel.selectedAnswer.collectAsState()
    val showAnswer by viewModel.showAnswer.collectAsState()
    val showExplanation by viewModel.showExplanation.collectAsState()
    
    Scaffold(
        topBar = {
            EnhancedTopBar(
                title = "Question Details",
                gradientColors = listOf(Color(0xFF11998e), Color(0xFF38ef7d)),
                onBackClick = onBackClick,
                actions = {
                    if (uiState is QuestionDetailUiState.Success) {
                        val question = (uiState as QuestionDetailUiState.Success).question
                        val isBookmarked by viewModel.isBookmarked(question.id).collectAsState(initial = false)
                        
                        IconButton(
                            onClick = {
                                viewModel.toggleBookmark(
                                    question.id,
                                    question.topicId,
                                    question.domainId
                                )
                            }
                        ) {
                            Icon(
                                imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = "Bookmark",
                                tint = if (isBookmarked) Color(0xFFFFD700) else Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        when (uiState) {
            is QuestionDetailUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            
            is QuestionDetailUiState.Success -> {
                val question = (uiState as QuestionDetailUiState.Success).question
                
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Question Type and Difficulty Badge
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                color = if (question.type == QuestionType.MCQ) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    MaterialTheme.colorScheme.secondaryContainer
                                },
                                shape = MaterialTheme.shapes.small
                            ) {
                                Text(
                                    text = question.type.name,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            
                            Surface(
                                color = when (question.difficulty.name) {
                                    "EASY" -> Color(0xFF4CAF50).copy(alpha = 0.2f)
                                    "MEDIUM" -> Color(0xFFFF9800).copy(alpha = 0.2f)
                                    "HARD" -> Color(0xFFF44336).copy(alpha = 0.2f)
                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                },
                                shape = MaterialTheme.shapes.small
                            ) {
                                Text(
                                    text = question.difficulty.name,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = when (question.difficulty.name) {
                                        "EASY" -> Color(0xFF2E7D32)
                                        "MEDIUM" -> Color(0xFFE65100)
                                        "HARD" -> Color(0xFFC62828)
                                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        
                        // Question Text
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                            )
                        ) {
                            Text(
                                text = question.questionText,
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        // Answer Section
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF4CAF50).copy(alpha = 0.1f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Answer",
                                        tint = Color(0xFF2E7D32),
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Text(
                                        text = "Answer",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF2E7D32)
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                Text(
                                    text = question.correctAnswer,
                                    style = MaterialTheme.typography.bodyLarge,
                                    lineHeight = 26.sp
                                )
                            }
                        }
                        
                        // Code Example Section
                        question.codeExample?.let { codeExample ->
                            if (codeExample.isNotBlank()) {
                                var showCodeDialog by remember { mutableStateOf(false) }
                                
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color(0xFF263238)
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Code,
                                                    contentDescription = "Code",
                                                    tint = Color(0xFF4CAF50),
                                                    modifier = Modifier.size(24.dp)
                                                )
                                                Text(
                                                    text = "Code Example",
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF4CAF50)
                                                )
                                            }
                                            
                                            IconButton(onClick = { showCodeDialog = true }) {
                                                Icon(
                                                    imageVector = Icons.Default.Fullscreen,
                                                    contentDescription = "View Fullscreen",
                                                    tint = Color(0xFF4CAF50)
                                                )
                                            }
                                        }
                                        
                                        Spacer(modifier = Modifier.height(12.dp))
                                        
                                        // Preview (limited height)
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .heightIn(max = 150.dp)
                                                .horizontalScroll(rememberScrollState())
                                                .verticalScroll(rememberScrollState())
                                        ) {
                                            Text(
                                                text = codeExample,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontFamily = FontFamily.Monospace,
                                                color = Color(0xFFE0E0E0),
                                                lineHeight = 22.sp
                                            )
                                        }
                                        
                                        TextButton(
                                            onClick = { showCodeDialog = true },
                                            modifier = Modifier.align(Alignment.End)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ZoomIn,
                                                contentDescription = null,
                                                tint = Color(0xFF4CAF50),
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "View Large",
                                                color = Color(0xFF4CAF50)
                                            )
                                        }
                                    }
                                }
                                
                                // Fullscreen Code Dialog
                                if (showCodeDialog) {
                                    FullscreenCodeDialog(
                                        code = codeExample,
                                        onDismiss = { showCodeDialog = false }
                                    )
                                }
                            }
                        }
                        
                        // Image Section
                        question.imageUrl?.let { imageUrl ->
                            if (imageUrl.isNotBlank()) {
                                var showImageDialog by remember { mutableStateOf(false) }
                                
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Image,
                                                    contentDescription = "Image",
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                                Text(
                                                    text = "Visual Diagram",
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                            
                                            IconButton(onClick = { showImageDialog = true }) {
                                                Icon(
                                                    imageVector = Icons.Default.Fullscreen,
                                                    contentDescription = "View Fullscreen",
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                        
                                        Spacer(modifier = Modifier.height(12.dp))
                                        
                                        // Preview (smaller)
                                        AsyncImage(
                                            model = imageUrl,
                                            contentDescription = "Diagram Preview",
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .heightIn(max = 200.dp)
                                                .clip(MaterialTheme.shapes.medium)
                                                .clickable { showImageDialog = true }
                                        )
                                        
                                        TextButton(
                                            onClick = { showImageDialog = true },
                                            modifier = Modifier.align(Alignment.End)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ZoomIn,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("View Large")
                                        }
                                    }
                                }
                                
                                // Fullscreen Image Dialog
                                if (showImageDialog) {
                                    FullscreenImageDialog(
                                        imageUrl = imageUrl,
                                        onDismiss = { showImageDialog = false }
                                    )
                                }
                            }
                        }
                        
                        // Explanation Section
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "💡 Detailed Explanation",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                Text(
                                    text = question.explanation,
                                    style = MaterialTheme.typography.bodyLarge,
                                    lineHeight = 26.sp
                                )
                            }
                        }
                    }
                    
                    BannerAdView()
                }
            }
            
            is QuestionDetailUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = (uiState as QuestionDetailUiState.Error).message,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun MCQOption(
    option: String,
    index: Int,
    isSelected: Boolean,
    isCorrect: Boolean,
    showAnswer: Boolean,
    onClick: () -> Unit
) {
    val optionLabel = ('A' + index).toString()
    
    val backgroundColor = when {
        showAnswer && isCorrect -> Color(0xFF4CAF50).copy(alpha = 0.2f)
        showAnswer && isSelected && !isCorrect -> Color(0xFFF44336).copy(alpha = 0.2f)
        isSelected -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        else -> MaterialTheme.colorScheme.surface
    }
    
    val borderColor = when {
        showAnswer && isCorrect -> Color(0xFF2E7D32)
        showAnswer && isSelected && !isCorrect -> Color(0xFFC62828)
        isSelected -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 2.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            )
            .clip(RoundedCornerShape(12.dp))
            .clickable(enabled = !showAnswer, onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Option Label (A, B, C, D)
            Surface(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(8.dp),
                color = when {
                    showAnswer && isCorrect -> Color(0xFF2E7D32)
                    showAnswer && isSelected && !isCorrect -> Color(0xFFC62828)
                    isSelected -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.primaryContainer
                }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = optionLabel,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (showAnswer && (isCorrect || (isSelected && !isCorrect))) {
                            Color.White
                        } else if (isSelected) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        }
                    )
                }
            }
            
            // Option Text
            Text(
                text = option,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
            )
            
            // Show check/cross icon when answer is revealed
            if (showAnswer) {
                Icon(
                    imageVector = if (isCorrect) Icons.Default.Check else {
                        if (isSelected) Icons.Default.Close else Icons.Default.Check
                    },
                    contentDescription = null,
                    tint = when {
                        isCorrect -> Color(0xFF2E7D32)
                        isSelected && !isCorrect -> Color(0xFFC62828)
                        else -> Color.Transparent
                    }
                )
            }
        }
    }
}

@Composable
fun FullscreenCodeDialog(
    code: String,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF263238))
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header
                Surface(
                    color = Color(0xFF1E272E),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Code,
                                contentDescription = "Code",
                                tint = Color(0xFF4CAF50)
                            )
                            Text(
                                text = "Code Example",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF4CAF50)
                            )
                        }
                        
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color.White
                            )
                        }
                    }
                }
                
                // Code Content
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                        .horizontalScroll(rememberScrollState())
                ) {
                    Text(
                        text = code,
                        style = MaterialTheme.typography.bodyLarge,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFFE0E0E0),
                        lineHeight = 24.sp
                    )
                }
            }
        }
    }
}

@Composable
fun FullscreenImageDialog(
    imageUrl: String,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .clickable { onDismiss() }
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = "Image",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Visual Diagram",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
                
                // Image Content with white background
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = "Diagram Fullscreen",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    )
                }
            }
        }
    }
}
