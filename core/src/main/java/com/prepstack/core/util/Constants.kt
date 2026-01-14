package com.prepstack.core.util

object Constants {
    const val JSON_FILE_NAME = "domains.json"
    
    // Icon URLs for domains (using free icons from icons8 or similar sources)
    object IconUrls {
        const val ANDROID = "https://img.icons8.com/color/96/android-os.png"
        const val KOTLIN = "https://img.icons8.com/color/96/kotlin.png"
        const val JAVA = "https://img.icons8.com/color/96/java-coffee-cup-logo.png"
        const val CPP = "https://img.icons8.com/color/96/c-plus-plus-logo.png"
        const val BACKEND = "https://img.icons8.com/color/96/server.png"
        const val DSA = "https://img.icons8.com/color/96/data-structures.png"
        const val OOPS = "https://img.icons8.com/color/96/object.png"
        const val SQL = "https://img.icons8.com/color/96/database.png"
        const val HR = "https://img.icons8.com/color/96/user.png"
        
        // Topic icons
        const val QUIZ = "https://img.icons8.com/color/96/quiz.png"
        const val BOOKMARK = "https://img.icons8.com/color/96/bookmark.png"
        const val TROPHY = "https://img.icons8.com/color/96/trophy.png"
        const val CHECKMARK = "https://img.icons8.com/color/96/checkmark.png"
        const val CLOSE = "https://img.icons8.com/color/96/close-window.png"
    }
    
    // Ad Configuration
    object Ads {
        const val INTERSTITIAL_AD_FREQUENCY = 5 // Show after every 5 interactions
        const val REWARDED_AD_UNLOCK_EXPLANATION = true
    }
    
    // Quiz Configuration
    object Quiz {
        const val DEFAULT_QUIZ_SIZE = 10
        const val QUIZ_TIME_LIMIT_SECONDS = 600 // 10 minutes
    }
}
