package com.prepstack.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.prepstack.ads.BannerAdView
import com.prepstack.domain.model.Topic
import com.prepstack.domain.model.QuizDescriptor
import com.prepstack.ui.components.EnhancedTopBar
import com.prepstack.ui.viewmodel.TopicUiState
import com.prepstack.ui.viewmodel.TopicViewModel
import android.util.Log

/**
 * Topic Screen - List of topics in a domain
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopicScreen(
    domainId: String,
    viewModel: TopicViewModel,
    onTopicClick: (String) -> Unit,
    onQuizClick: (QuizDescriptor) -> Unit, 
    onBackClick: () -> Unit
) {
    LaunchedEffect(domainId) {
        viewModel.loadTopics(domainId)
    }
    
    val uiState by viewModel.uiState.collectAsState()
    var showQuizDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            EnhancedTopBar(
                title = "Topics",
                gradientColors = listOf(Color(0xFF396afc), Color(0xFF2948ff)), // Professional blue gradient
                onBackClick = onBackClick,
                actions = {
                    TextButton(onClick = { showQuizDialog = true }) {
                        Text(
                            text = "Start Quiz",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            when (uiState) {
                is TopicUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                
                is TopicUiState.Success -> {
                    val topics = (uiState as TopicUiState.Success).topics
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(topics) { topic ->
                            TopicItem(
                                topic = topic,
                                onClick = { onTopicClick(topic.id) }
                            )
                        }
                    }
                    
                    BannerAdView()
                }
                
                is TopicUiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = (uiState as TopicUiState.Error).message,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            // -- Quiz Selection Dialog --
            if (showQuizDialog) {
                val quizzes: List<QuizDescriptor> = viewModel.quizzes
                AlertDialog(
                    onDismissRequest = { showQuizDialog = false },
                    title = { Text("Choose a Quiz") },
                    text = {
                        Column {
                            quizzes.forEach { quiz ->
                                TextButton(
                                    onClick = {
                                        showQuizDialog = false
                                        onQuizClick(quiz)
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(quiz.title)
                                }
                            }
                        }
                    },
                    confirmButton = {}
                )
            }
        }
    }
}

@Composable
fun TopicItem(
    topic: Topic,
    onClick: () -> Unit
) {
    val gradientColors = listOf(
        MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
        MaterialTheme.colorScheme.secondary.copy(alpha = 0.05f),
        MaterialTheme.colorScheme.surface
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = MaterialTheme.shapes.large
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(gradientColors)
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Icon with vibrant gradient background
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(MaterialTheme.shapes.medium)
                        .background(
                            brush = Brush.verticalGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = topic.iconUrl,
                        contentDescription = topic.name,
                        modifier = Modifier.size(44.dp)
                    )
                }
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = topic.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    Text(
                        text = topic.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        lineHeight = MaterialTheme.typography.bodyMedium.lineHeight.times(1.3f)
                    )
                    
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    // Question count with icon style
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = "${topic.questionCount} questions",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}
