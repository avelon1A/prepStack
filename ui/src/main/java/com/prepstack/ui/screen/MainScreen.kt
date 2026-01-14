package com.prepstack.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
    onDomainClick: (String) -> Unit,
    onTopicClick: (String) -> Unit,
    onResumeTest: (String) -> Unit,
    onBookmarkNavigate: () -> Unit
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
                BookmarkScreen(onBackClick = { })
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
    
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        items.forEach { item ->
            val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
            
            NavigationBarItem(
                icon = {
                    AsyncImage(
                        model = item.iconUrl,
                        contentDescription = item.title,
                        modifier = Modifier.size(26.dp),
                        alpha = if (selected) 1f else 0.5f
                    )
                },
                label = { 
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
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
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}
