package com.prepstack.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.prepstack.ads.BannerAdView

/**
 * Progress Screen - Shows user's learning progress and statistics
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressScreen() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Your Progress") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Overall Progress Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            AsyncImage(
                                model = "https://img.icons8.com/color/96/trophy.png",
                                contentDescription = null,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Overall Progress",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            LinearProgressIndicator(
                                progress = 0.65f,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(12.dp),
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "65% Complete",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
                
                // Domain-wise Progress
                item {
                    Text(
                        text = "Domain-wise Progress",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                item {
                    DomainProgressCard(
                        domainName = "Android",
                        iconUrl = "https://img.icons8.com/color/96/android-os.png",
                        progress = 0.8f,
                        completed = 24,
                        total = 30
                    )
                }
                
                item {
                    DomainProgressCard(
                        domainName = "Kotlin",
                        iconUrl = "https://img.icons8.com/color/96/kotlin.png",
                        progress = 0.6f,
                        completed = 12,
                        total = 20
                    )
                }
                
                item {
                    DomainProgressCard(
                        domainName = "DSA",
                        iconUrl = "https://img.icons8.com/color/96/data-structures.png",
                        progress = 0.4f,
                        completed = 8,
                        total = 20
                    )
                }
                
                // Quiz History
                item {
                    Text(
                        text = "Recent Quiz Results",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                item {
                    QuizResultCard(
                        title = "Android Basics Quiz",
                        score = "8/10",
                        accuracy = "80%",
                        passed = true
                    )
                }
                
                item {
                    QuizResultCard(
                        title = "Kotlin Fundamentals Quiz",
                        score = "6/10",
                        accuracy = "60%",
                        passed = true
                    )
                }
            }
            
            BannerAdView()
        }
    }
}

@Composable
fun DomainProgressCard(
    domainName: String,
    iconUrl: String,
    progress: Float,
    completed: Int,
    total: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AsyncImage(
                model = iconUrl,
                contentDescription = domainName,
                modifier = Modifier.size(48.dp)
            )
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = domainName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$completed/$total questions completed",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun QuizResultCard(
    title: String,
    score: String,
    accuracy: String,
    passed: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (passed) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            } else {
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Score: $score • Accuracy: $accuracy",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            AsyncImage(
                model = if (passed) {
                    "https://img.icons8.com/color/96/checkmark.png"
                } else {
                    "https://img.icons8.com/color/96/close-window.png"
                },
                contentDescription = if (passed) "Passed" else "Failed",
                modifier = Modifier.size(32.dp)
            )
        }
    }
}
