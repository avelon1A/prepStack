package com.prepstack.ui.screen

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.prepstack.ui.navigation.BottomNavItem
import com.prepstack.ui.navigation.getAllBottomNavItems
import com.prepstack.ui.viewmodel.BookmarkViewModel
import com.prepstack.ui.viewmodel.DomainUiState
import com.prepstack.ui.viewmodel.DomainViewModel
import com.prepstack.ui.viewmodel.HomeViewModel

/**
 * Main Screen with Bottom Navigation
 */
@Composable
fun MainScreen(
    domainViewModel: DomainViewModel,
    homeViewModel: HomeViewModel,
    bookmarkViewModel: BookmarkViewModel,
    onDomainClick: (String) -> Unit,
    onTopicClick: (String) -> Unit,
    onResumeTest: (String) -> Unit,
    onBookmarkNavigate: () -> Unit,
    onQuestionClick: (String) -> Unit
) {
    val navController = rememberNavController()
    val domainUiState by domainViewModel.uiState.collectAsState()
    
    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController)
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(BottomNavItem.Home.route) {
                when (domainUiState) {
                    is DomainUiState.Success -> {
                        val domains = (domainUiState as DomainUiState.Success).domains
                        HomeScreen(
                            domains = domains,
                            homeViewModel = homeViewModel,
                            onDomainClick = onDomainClick,
                            onTopicClick = onTopicClick,
                            onResumeTest = onResumeTest,
                            onSearchClick = { /* TODO */ },
                            onBookmarkClick = onBookmarkNavigate
                        )
                    }
                    else -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
            
//            composable(BottomNavItem.Progress.route) {
//                ProgressScreen()
//            }
            
            composable(BottomNavItem.Bookmarks.route) {
                BookmarkScreen(
                    viewModel = bookmarkViewModel,
                    onQuestionClick = onQuestionClick,
                    onBackClick = { /* In tab navigation, back is handled by tab switching */ }
                )
            }
            
            composable(BottomNavItem.VoiceInterview.route) {
                AppVoiceInterviewScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }
            
            composable(BottomNavItem.Profile.route) {
                ProfileScreen()
            }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val items = getAllBottomNavItems()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
        shape = RectangleShape
    ) {
        NavigationBar(
            modifier = Modifier.fillMaxWidth(),
            containerColor = Color.Transparent,
            tonalElevation = 0.dp
        ) {
            items.forEach { item ->
                AddBottomNavItem(
                    item = item,
                    currentDestination = currentDestination,
                    navController = navController
                )
            }
        }
    }
}

@Composable
fun RowScope.AddBottomNavItem(
    item: BottomNavItem,
    currentDestination: NavDestination?,
    navController: NavHostController
) {
    val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.onSurfaceVariant
    
    NavigationBarItem(
        modifier = Modifier.align(Alignment.CenterVertically),
        icon = {
            AsyncImage(
                model = item.iconUrl,
                contentDescription = item.title,
                modifier = Modifier
                    .size(32.dp)
                    .drawBehind {
                        if (selected) {
                            val glowRadius = size.minDimension / 2 + 16.dp.toPx()
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        primaryColor.copy(alpha = 0.25f),
                                        primaryColor.copy(alpha = 0.15f),
                                        primaryColor.copy(alpha = 0.05f),
                                        Color.Transparent
                                    ),
                                    center = center,
                                    radius = glowRadius
                                ),
                                radius = glowRadius,
                                center = center
                            )
                        }
                    }
            )
        },
        selected = selected,
        onClick = {
            navController.navigate(item.route) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        },
        interactionSource = remember { MutableInteractionSource() },
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = primaryColor,
            selectedTextColor = primaryColor,
            indicatorColor = Color.Transparent,
            unselectedIconColor = secondaryColor,
            unselectedTextColor = secondaryColor
        ),
        label = {
            Text(
                text = item.title,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
            )
        }
    )
}
