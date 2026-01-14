package com.prepstack.ui.screen

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.prepstack.ads.BannerAdView
import com.prepstack.core.util.DateUtils
import com.prepstack.domain.model.QuizResult
import com.prepstack.ui.components.EnhancedTopBar

/**
 * Quiz Result Screen - Shows detailed results after quiz completion
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizResultScreen(
    quizResult: QuizResult,
    domainName: String,
    topicName: String? = null,
    onBackClick: () -> Unit,
    onRetryQuiz: () -> Unit,
    onHomeClick: () -> Unit
) {
    val passed = quizResult.passed
    val percentage = quizResult.percentage
    
    Scaffold(
        topBar = {
            EnhancedTopBar(
                title = "Quiz Results",
                gradientColors = if (passed) {
                    listOf(Color(0xFF4CAF50), Color(0xFF8BC34A))
                } else {
                    listOf(Color(0xFFF44336), Color(0xFFFF9800))
                },
                onBackClick = onBackClick
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Result Header Card
            ResultHeaderCard(
                passed = passed,
                percentage = percentage,
                totalQuestions = quizResult.totalQuestions,
                correctAnswers = quizResult.correctAnswers,
                timeTaken = quizResult.timeTaken
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Detailed Statistics
            DetailedStatisticsCard(
                correctAnswers = quizResult.correctAnswers,
                incorrectAnswers = quizResult.incorrectAnswers,
                skippedQuestions = quizResult.skippedQuestions,
                totalQuestions = quizResult.totalQuestions
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Performance Message
            PerformanceMessageCard(
                passed = passed,
                percentage = percentage
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Action Buttons
            ActionButtons(
                passed = passed,
                onRetryQuiz = onRetryQuiz,
                onHomeClick = onHomeClick
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            BannerAdView()
        }
    }
}

@Composable
fun ResultHeaderCard(
    passed: Boolean,
    percentage: Float,
    totalQuestions: Int,
    correctAnswers: Int,
    timeTaken: Long
) {
    val gradientColors = if (passed) {
        listOf(Color(0xFF4CAF50), Color(0xFF8BC34A))
    } else {
        listOf(Color(0xFFF44336), Color(0xFFFF9800))
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = gradientColors,
                        start = androidx.compose.ui.geometry.Offset(0f, 0f),
                        end = androidx.compose.ui.geometry.Offset(1000f, 1000f)
                    )
                )
                .padding(24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Icon
                Icon(
                    imageVector = if (passed) Icons.Default.EmojiEvents else Icons.Default.SentimentDissatisfied,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(80.dp)
                )
                
                // Percentage Circle
                Box(
                    modifier = Modifier.size(140.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Background circle
                    Box(
                        modifier = Modifier
                            .size(140.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f))
                    )
                    
                    // Percentage text
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${String.format("%.0f", percentage)}%",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = if (passed) "PASSED" else "FAILED",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
                
                // Stats row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(32.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatItem(
                        icon = Icons.Default.CheckCircle,
                        label = "Correct",
                        value = "$correctAnswers",
                        color = Color.White
                    )
                    StatItem(
                        icon = Icons.Default.QuestionAnswer,
                        label = "Total",
                        value = "$totalQuestions",
                        color = Color.White
                    )
                    StatItem(
                        icon = Icons.Default.Timer,
                        label = "Time",
                        value = formatTime(timeTaken),
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun StatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = color.copy(alpha = 0.8f)
        )
    }
}

@Composable
fun DetailedStatisticsCard(
    correctAnswers: Int,
    incorrectAnswers: Int,
    skippedQuestions: Int,
    totalQuestions: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Detailed Statistics",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Divider()
            
            // Correct Answers
            StatRow(
                icon = Icons.Default.CheckCircle,
                label = "Correct Answers",
                value = "$correctAnswers",
                color = Color(0xFF4CAF50),
                percentage = (correctAnswers.toFloat() / totalQuestions * 100)
            )
            
            // Incorrect Answers
            StatRow(
                icon = Icons.Default.Cancel,
                label = "Incorrect Answers",
                value = "$incorrectAnswers",
                color = Color(0xFFF44336),
                percentage = (incorrectAnswers.toFloat() / totalQuestions * 100)
            )
            
            // Skipped Questions
            StatRow(
                icon = Icons.Default.SkipNext,
                label = "Skipped Questions",
                value = "$skippedQuestions",
                color = Color(0xFFFF9800),
                percentage = (skippedQuestions.toFloat() / totalQuestions * 100)
            )
        }
    }
}

@Composable
fun StatRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    color: Color,
    percentage: Float
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
        }
        
        Text(
            text = "$value (${String.format("%.0f", percentage)}%)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
    
    // Progress bar
    LinearProgressIndicator(
        progress = percentage / 100f,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        color = color,
        trackColor = color.copy(alpha = 0.2f)
    )
}

@Composable
fun PerformanceMessageCard(
    passed: Boolean,
    percentage: Float
) {
    val (title, message, icon) = when {
        percentage >= 90 -> Triple(
            "Excellent Performance!",
            "You've demonstrated outstanding knowledge. Keep up the great work!",
            Icons.Default.Star
        )
        percentage >= 70 -> Triple(
            "Good Job!",
            "You have a solid understanding. Review the incorrect answers to improve further.",
            Icons.Default.ThumbUp
        )
        percentage >= 50 -> Triple(
            "Keep Practicing!",
            "You're on the right track. Focus on weak areas and try again.",
            Icons.Default.TrendingUp
        )
        else -> Triple(
            "Don't Give Up!",
            "Review the topics and practice more. You'll improve with time.",
            Icons.Default.School
        )
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (passed) 
                Color(0xFF4CAF50).copy(alpha = 0.1f) 
            else 
                Color(0xFFFF9800).copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(20.dp),
        border = if (passed) {
            androidx.compose.foundation.BorderStroke(
                2.dp,
                Color(0xFF4CAF50).copy(alpha = 0.3f)
            )
        } else {
            androidx.compose.foundation.BorderStroke(
                2.dp,
                Color(0xFFFF9800).copy(alpha = 0.3f)
            )
        }
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (passed) Color(0xFF4CAF50) else Color(0xFFFF9800),
                modifier = Modifier.size(48.dp)
            )
            
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (passed) Color(0xFF4CAF50) else Color(0xFFFF9800)
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun ActionButtons(
    passed: Boolean,
    onRetryQuiz: () -> Unit,
    onHomeClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Retry Quiz Button
        Button(
            onClick = onRetryQuiz,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null
                )
                Text(
                    text = "Retry Quiz",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        // Back to Home Button
        OutlinedButton(
            onClick = onHomeClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = null
                )
                Text(
                    text = "Back to Home",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

fun formatTime(seconds: Long): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return if (minutes > 0) {
        "${minutes}m ${remainingSeconds}s"
    } else {
        "${remainingSeconds}s"
    }
}