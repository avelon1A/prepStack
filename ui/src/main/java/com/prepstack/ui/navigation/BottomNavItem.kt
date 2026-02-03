package com.prepstack.ui.navigation

import com.prepstack.core.util.Constants

/**
 * Bottom Navigation Items
 */
sealed class BottomNavItem(
    val route: String,
    val title: String,
    val iconUrl: String
) {
    data object Home : BottomNavItem(
        route = "home",
        title = "Home",
        iconUrl = "https://img.icons8.com/color/96/home.png"
    )
    
    data object Progress : BottomNavItem(
        route = "progress",
        title = "Progress",
        iconUrl = "https://img.icons8.com/color/96/graph.png"
    )
    
    data object Bookmarks : BottomNavItem(
        route = "bookmarks",
        title = "Bookmarks",
        iconUrl = Constants.IconUrls.BOOKMARK
    )
    
    data object VoiceInterview : BottomNavItem(
        route = "voice_interview",
        title = "Voice Interview",
        iconUrl = "https://img.icons8.com/color/96/microphone.png"
    )
    
    data object Profile : BottomNavItem(
        route = "profile",
        title = "Profile",
        iconUrl = "https://img.icons8.com/color/96/user.png"
    )
}

fun getAllBottomNavItems() = listOf(
    BottomNavItem.Home,
//    BottomNavItem.Progress,
    BottomNavItem.Bookmarks,
    BottomNavItem.VoiceInterview,
//    BottomNavItem.Profile
)