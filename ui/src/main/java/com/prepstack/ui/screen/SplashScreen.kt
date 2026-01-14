package com.prepstack.ui.screen

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * Splash Screen - Professional branding screen with animations
 */
@Composable
fun SplashScreen(
    onNavigateToDomain: () -> Unit
) {
    // Animation states
    var logoScale by remember { mutableStateOf(0f) }
    var titleAlpha by remember { mutableStateOf(0f) }
    var subtitleAlpha by remember { mutableStateOf(0f) }
    var progress by remember { mutableStateOf(0f) }
    
    // Logo scale animation
    LaunchedEffect(Unit) {
        logoScale = 1f
        delay(300)
        titleAlpha = 1f
        delay(300)
        subtitleAlpha = 1f
        delay(300)
        
        // Progress animation
        while (progress < 1f) {
            delay(30)
            progress += 0.02f
        }
        
        delay(500)
        onNavigateToDomain()
    }
    
    // Gradient background
    val gradientColors = listOf(
        Color(0xFF667eea),
        Color(0xFF764ba2),
        Color(0xFFf093fb)
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(gradientColors)
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            // Animated Logo
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .scale(logoScale)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.3f),
                                Color.White.copy(alpha = 0.1f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = "App Logo",
                    tint = Color.White,
                    modifier = Modifier.size(60.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // App Title with fade-in animation
            Text(
                text = "Tech Interview Prep",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(titleAlpha)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Subtitle with fade-in animation
            Text(
                text = "Master Your Interview Skills",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.9f),
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(subtitleAlpha)
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Feature badges
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.alpha(subtitleAlpha)
            ) {
                FeatureBadge(
                    icon = Icons.Default.Code,
                    text = "500+ Questions"
                )
                FeatureBadge(
                    icon = Icons.Default.Topic,
                    text = "10+ Domains"
                )
                FeatureBadge(
                    icon = Icons.Default.EmojiEvents,
                    text = "Track Progress"
                )
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Progress bar
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(200.dp)
            ) {
                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = Color.White,
                    trackColor = Color.White.copy(alpha = 0.3f)
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "Loading...",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Medium
                )
            }
        }
        
        // Bottom branding
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "by PrepStack",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.6f),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun FeatureBadge(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White.copy(alpha = 0.15f),
        modifier = Modifier.padding(4.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = text,
                fontSize = 12.sp,
                color = Color.White,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
