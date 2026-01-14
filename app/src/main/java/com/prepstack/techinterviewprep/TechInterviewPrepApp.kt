package com.prepstack.techinterviewprep

import android.app.Application
import com.prepstack.ads.AdManager
import com.prepstack.bookmarks.data.BookmarkDatabase
import com.prepstack.bookmarks.repository.BookmarkRepository
import com.prepstack.data.local.UserProgressDatabase
import com.prepstack.data.repository.InterviewRepositoryImpl
import com.prepstack.data.repository.UserProgressRepositoryImpl
import com.prepstack.data.source.LocalDataSource
import com.prepstack.domain.repository.InterviewRepository
import com.prepstack.domain.repository.UserProgressRepository
import com.prepstack.domain.usecase.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Application class for dependency injection and initialization
 * In production, use Hilt or Koin for DI
 */
class TechInterviewPrepApp : Application() {
    
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    // Data Layer
    private val localDataSource by lazy { LocalDataSource(applicationContext) }
    val interviewRepository: InterviewRepository by lazy {
        InterviewRepositoryImpl(localDataSource)
    }
    
    // Domain Layer - Use Cases
    val getDomainsUseCase by lazy { GetDomainsUseCase(interviewRepository) }
    val getTopicsUseCase by lazy { GetTopicsUseCase(interviewRepository) }
    val getQuestionsUseCase by lazy { GetQuestionsUseCase(interviewRepository) }
    val getRandomQuizQuestionsUseCase by lazy {
        GetRandomQuizQuestionsUseCase(interviewRepository)
    }
    
    // Bookmarks
    private val bookmarkDatabase by lazy {
        BookmarkDatabase.getDatabase(applicationContext)
    }
    val bookmarkRepository by lazy {
        BookmarkRepository(bookmarkDatabase.bookmarkDao())
    }
    
    // User Progress
    private val userProgressDatabase by lazy {
        UserProgressDatabase.getDatabase(applicationContext)
    }
    val userProgressRepository: UserProgressRepository by lazy {
        UserProgressRepositoryImpl(userProgressDatabase.userProgressDao())
    }
    
    // Ads
    val adManager by lazy { AdManager(applicationContext) }
    
    override fun onCreate() {
        super.onCreate()
        
        // Load data at startup
        applicationScope.launch {
            interviewRepository.loadData()
            
            // Preload ads
            adManager.loadInterstitialAd()
            adManager.loadRewardedAd()
        }
    }
}
