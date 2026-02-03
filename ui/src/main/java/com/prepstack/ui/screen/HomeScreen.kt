package com.prepstack.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.prepstack.ads.BannerAdView
import com.prepstack.core.util.Constants
import com.prepstack.core.util.DateUtils
import com.prepstack.domain.model.Domain
import com.prepstack.domain.model.UserActivity
import com.prepstack.domain.model.IncompleteTest
import com.prepstack.domain.model.UserStreak
import com.prepstack.ui.components.EnhancedTopBar
import com.prepstack.ui.components.StreakCard
import com.prepstack.ui.viewmodel.HomeUiState
import com.prepstack.ui.viewmodel.HomeViewModel

/**
 * Home Screen - Dashboard with recent activities, performance, search, and browse topics
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    domains: List<Domain>,
    homeViewModel: HomeViewModel,
    onDomainClick: (String) -> Unit,
    onTopicClick: (String) -> Unit,
    onResumeTest: (String) -> Unit,
    onSearchClick: () -> Unit,
    onBookmarkClick: () -> Unit
) {
    val homeUiState by homeViewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            EnhancedTopBar(
                title = "Tech Interview Prep",
                gradientColors = listOf(Color(0xFF667eea), Color(0xFF764ba2))
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                item {
                    SectionHeader(
                        title = "Browse Topics",
                        iconUrl = "https://img.icons8.com/color/96/books.png"
                    )
                }
                
                // Domains Grid
                items(domains.chunked(2)) { rowDomains ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        rowDomains.forEach { domain ->
                            DomainCompactCard(
                                domain = domain,
                                onClick = { onDomainClick(domain.id) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        // Fill empty space if odd number
                        if (rowDomains.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
                
                // Streak section (appears at the top if streak exists)
                when (homeUiState) {
                    is HomeUiState.Success -> {
                        val userStreak = (homeUiState as HomeUiState.Success).data.userStreak
                        if (userStreak != null && userStreak.streakCount > 0) {
                            item {
                                StreakCard(userStreak = userStreak)
                                Spacer(modifier = Modifier.height(20.dp))
                            }
                        }
                        
                        // Recent Activities Section (Dynamic)
                        val activities = (homeUiState as HomeUiState.Success).data.recentActivities
                        if (activities.isNotEmpty()) {
                            item {
                                SectionHeader(
                                    title = "Recent Activities",
                                    iconUrl = "https://img.icons8.com/color/96/clock.png"
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        activities.forEachIndexed { index, activity ->
                                            RecentActivityItemClickable(
                                                activity = activity,
                                                onClick = { onTopicClick(activity.topicId) }
                                            )
                                            if (index < activities.size - 1) {
                                                Divider(modifier = Modifier.padding(vertical = 8.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        
                        // Performance Overview (Dynamic)
                        item {
                            val performance = (homeUiState as HomeUiState.Success).data.performance
                            SectionHeader(
                                title = "Your Performance",
                                iconUrl = "https://img.icons8.com/color/96/graph.png"
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                PerformanceCard(
                                    title = "Questions\nAnswered",
                                    value = "${performance.totalQuestionsAnswered}",
                                    modifier = Modifier.weight(1f)
                                )
                                PerformanceCard(
                                    title = "Correct\nAnswers",
                                    value = "${performance.totalCorrectAnswers}",
                                    modifier = Modifier.weight(1f)
                                )
                                PerformanceCard(
                                    title = "Accuracy",
                                    value = "${String.format("%.0f", performance.accuracy)}%",
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                        
                        // Incomplete Tests (Dynamic)
                        val incompleteTests = (homeUiState as HomeUiState.Success).data.incompleteTests
                        if (incompleteTests.isNotEmpty()) {
                            item {
                                SectionHeader(
                                    title = "Incomplete Tests",
                                    iconUrl = "https://img.icons8.com/color/96/test.png"
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                            
                            items(incompleteTests) { test ->
                                IncompleteTestCard(
                                    test = test,
                                    onResumeClick = { onResumeTest(test.testId) },
                                    onDeleteClick = { homeViewModel.deleteIncompleteTest(test.testId) }
                                )
                            }
                        }
                    }
                    is HomeUiState.Loading -> {
                        // Show skeleton loaders or loading indicators
                    }
                    is HomeUiState.Error -> {
                        // Show default empty state
                    }
                }
            }
            
            BannerAdView()
        }
    }
}

@Composable
fun SectionHeader(title: String, iconUrl: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AsyncImage(
            model = iconUrl,
            contentDescription = null,
            modifier = Modifier.size(24.dp)  // Smaller icon
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,  // Smaller than original
            fontWeight = FontWeight.SemiBold  // Less heavy weight
        )
    }
}

@Composable
fun RecentActivityItem(
    title: String,
    subtitle: String,
    progress: Float
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,  // Very small text
            fontWeight = FontWeight.Medium
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.labelSmall,  // Smallest text
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(2.dp))  // Less spacing
        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun RecentActivityItemClickable(
    activity: UserActivity,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = activity.topicName,
                style = MaterialTheme.typography.labelMedium,  // Very small text
                fontWeight = FontWeight.Medium
            )
            Text(
                text = DateUtils.getRelativeTime(activity.timestamp),
                style = MaterialTheme.typography.labelSmall,  // Smallest text
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = "Completed ${activity.questionsCompleted}/${activity.totalQuestions} questions",
            style = MaterialTheme.typography.labelSmall,  // Smallest text
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = activity.progress,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun IncompleteTestCard(
    test: IncompleteTest,
    onResumeClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = test.domainName + if (test.topicName != null) " - ${test.topicName}" else "",
                        style = MaterialTheme.typography.labelMedium,  // Very small
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(2.dp))  // Less space
                    Text(
                        text = "${test.questionsCompleted}/${test.totalQuestions} questions completed",
                        style = MaterialTheme.typography.labelSmall,  // Smallest text
                        fontWeight = FontWeight.Normal
                    )
                    Text(
                        text = DateUtils.getRelativeTime(test.timestamp),
                        style = MaterialTheme.typography.labelSmall,  // Smallest text
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.size(24.dp)  // Smaller button
                ) {
                    Text("✕", style = MaterialTheme.typography.labelMedium)  // Smaller text
                }
            }
            Spacer(modifier = Modifier.height(4.dp))  // Less space
            LinearProgressIndicator(
                progress = test.progress,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(4.dp))  // Less space
            TextButton(
                onClick = onResumeClick,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(
                    "Resume Test", 
                    style = MaterialTheme.typography.labelSmall,  // Smallest text
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun PerformanceCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),  // Smaller padding
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,  // Much smaller than original
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(2.dp))  // Less spacing
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,  // Very small text
                fontWeight = FontWeight.Normal,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center  // Center-aligned text
            )
        }
    }
}

@Composable
fun DomainCompactCard(
    domain: Domain,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(150.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp,
            pressedElevation = 8.dp
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer,
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                        )
                    )
                )
                .padding(16.dp)
        ) {

            // Decorative background circle
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 30.dp, y = (-30).dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        shape = CircleShape
                    )
            )

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {

                // Icon container
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 4.dp,
                    modifier = Modifier.size(56.dp)
                ) {
                    AsyncImage(
                        model = domain.iconUrl,
                        contentDescription = domain.name,
                        modifier = Modifier
                            .padding(10.dp)
                            .fillMaxSize()
                    )
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = domain.name,
                        style = MaterialTheme.typography.labelLarge,  // Smaller than original
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Topic count chip
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            Text(
                                text = "${domain.topicCount} topics",
                                style = MaterialTheme.typography.labelSmall,  // Smallest text
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.padding(
                                    horizontal = 10.dp,
                                    vertical = 4.dp
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
