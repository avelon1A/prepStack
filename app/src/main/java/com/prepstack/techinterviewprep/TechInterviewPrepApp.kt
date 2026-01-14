package com.prepstack.techinterviewprep

import android.app.Application
import android.os.Build
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.prepstack.ads.AdManager
import com.prepstack.bookmarks.data.BookmarkDatabase
import com.prepstack.bookmarks.repository.BookmarkRepository
import com.prepstack.core.util.AnalyticsLogger
import com.prepstack.core.util.CrashlyticsLogger
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
        
        // Initialize Analytics and Crashlytics
        setupAnalytics()
        setupCrashlytics()
        
        // Load data at startup
        applicationScope.launch {
            interviewRepository.loadData()
            
            // Preload ads
            adManager.loadInterstitialAd(AdManager.getInterstitialAdId())
            adManager.loadRewardedAd(AdManager.getRewardedAdId())
        }
    }
    
    private fun setupAnalytics() {
        // Initialize the Analytics Logger
        AnalyticsLogger.initialize(applicationContext)
        
        // Set up user properties
        val versionName = packageManager.getPackageInfo(packageName, 0).versionName ?: "unknown"
        AnalyticsLogger.setUserProperty("app_version", versionName)
        AnalyticsLogger.setUserProperty("device_model", Build.MODEL)
        AnalyticsLogger.setUserProperty("android_version", Build.VERSION.RELEASE)
        
        // Log app start event
        val params = android.os.Bundle().apply {
            putString("startup_type", "cold_start")
        }
        AnalyticsLogger.logEvent("app_start", params)
    }
    
    private fun setupCrashlytics() {
        // Set user identifiers (non-PII)
        val installId = applicationContext.getSharedPreferences("app_prefs", 0)
            .getString("install_id", null) ?: java.util.UUID.randomUUID().toString().also {
                applicationContext.getSharedPreferences("app_prefs", 0).edit().putString("install_id", it).apply()
            }
        CrashlyticsLogger.setUserId(installId)
        
        // Log device info
        CrashlyticsLogger.setCustomKey("device_manufacturer", Build.MANUFACTURER)
        CrashlyticsLogger.setCustomKey("device_model", Build.MODEL)
        CrashlyticsLogger.setCustomKey("android_version", Build.VERSION.SDK_INT.toString())
        val versionName = packageManager.getPackageInfo(packageName, 0).versionName ?: "unknown"
        CrashlyticsLogger.setCustomKey("app_version", versionName)
        CrashlyticsLogger.log("App initialized", "INFO")
        
        // Enable Crashlytics in debug builds only if explicitly enabled
        // This is controlled by the manifest placeholder we set in build.gradle.kts
        
        // Set up a global exception handler
        setupGlobalExceptionHandler()
    }
    
    private fun setupGlobalExceptionHandler() {
        val defaultExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()
        
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            CrashlyticsLogger.log("Fatal Exception: ${throwable.message}", "FATAL")
            CrashlyticsLogger.logException(throwable, "Uncaught exception in thread: ${thread.name}")
            
            // Call the default exception handler after logging
            defaultExceptionHandler?.uncaughtException(thread, throwable)
        }
    }
}
