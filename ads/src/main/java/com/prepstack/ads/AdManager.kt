package com.prepstack.ads

import android.app.Activity
import android.content.Context
import android.os.Bundle
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.prepstack.core.util.AnalyticsLogger

/**
 * AdMob Manager for handling ads across the app
 * Follows Single Responsibility Principle
 */
class AdManager(private val context: Context) {
    
    private var interstitialAd: InterstitialAd? = null
    private var rewardedAd: RewardedAd? = null
    private var interactionCount = 0
    
    init {
        // Initialize Mobile Ads SDK with debug output
        println("📱 Initializing Google Mobile Ads SDK")
        MobileAds.initialize(context) { initStatus ->
            println("🔌 Mobile Ads initialization complete: ${initStatus.adapterStatusMap}")
        }
    }
    
    /**
     * Load interstitial ad
     * In release builds, use production ad IDs by default
     */
    fun loadInterstitialAd(adUnitId: String) {
        val adRequest = AdRequest.Builder().build()
        
        // Log ad request event
        AnalyticsLogger.logEvent("interstitial_ad_requested", Bundle().apply {
            putString("ad_unit_id", adUnitId)
        })
        
        InterstitialAd.load(
            context,
            adUnitId,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    // Log ad loaded success
                    AnalyticsLogger.logEvent("interstitial_ad_loaded", Bundle().apply {
                        putString("ad_unit_id", adUnitId)
                    })
                }
                
                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    interstitialAd = null
                    // Log ad load failure
                    AnalyticsLogger.logEvent("interstitial_ad_failed", Bundle().apply {
                        putString("ad_unit_id", adUnitId)
                        putString("error_code", loadAdError.code.toString())
                        putString("error_message", loadAdError.message)
                    })
                }
            }
        )
    }
    
    /**
     * Show interstitial ad if loaded
     */
    fun showInterstitialAd(activity: Activity, adUnitId: String, onAdClosed: () -> Unit = {}) {
        interstitialAd?.let { ad ->
            // Log ad show attempt
            AnalyticsLogger.logEvent("interstitial_ad_show_attempt")
            
            ad.fullScreenContentCallback = object : com.google.android.gms.ads.FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    interstitialAd = null
                    // Log ad dismissed
                    AnalyticsLogger.logEvent("interstitial_ad_dismissed")
                    loadInterstitialAd(getInterstitialAdId()) // Preload next ad
                    onAdClosed()
                }
                
                override fun onAdFailedToShowFullScreenContent(error: com.google.android.gms.ads.AdError) {
                    interstitialAd = null
                    // Log ad show failure
                    AnalyticsLogger.logEvent("interstitial_ad_show_failed", Bundle().apply {
                        putString("error_code", error.code.toString())
                        putString("error_message", error.message)
                    })
                    onAdClosed()
                }
                
                override fun onAdShowedFullScreenContent() {
                    // Log ad impression
                    AnalyticsLogger.logAdImpression("interstitial", "fullscreen")
                }
            }
            ad.show(activity)
        } ?: run {
            // Log ad not available
            AnalyticsLogger.logEvent("interstitial_ad_not_available")
            onAdClosed()
        }
    }
    
    /**
     * Track interactions and show ad after threshold
     */
    fun trackInteraction(activity: Activity, threshold: Int = 5, onAdClosed: () -> Unit = {}) {
        interactionCount++
        if (interactionCount >= threshold) {
            interactionCount = 0
            showInterstitialAd(activity, getInterstitialAdId(), onAdClosed)
        } else {
            onAdClosed()
        }
    }
    
    /**
     * Load rewarded ad with callbacks for tracking load status
     */
    fun loadRewardedAd(
        adUnitId: String,
        onAdLoaded: () -> Unit = {},
        onAdFailedToLoad: (String) -> Unit = {}
    ) {
        val adRequest = AdRequest.Builder().build()
        
        // Debug output
        println("🔄 Loading rewarded ad: $adUnitId")
        
        // Log ad request event
        AnalyticsLogger.logEvent("rewarded_ad_requested", Bundle().apply {
            putString("ad_unit_id", adUnitId)
        })
        
        RewardedAd.load(
            context,
            adUnitId,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    println("✓ Rewarded ad loaded successfully")
                    rewardedAd = ad
                    
                    // Log ad loaded success
                    AnalyticsLogger.logEvent("rewarded_ad_loaded", Bundle().apply {
                        putString("ad_unit_id", adUnitId)
                    })
                    
                    onAdLoaded()
                }
                
                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    println("✗ Rewarded ad failed to load: ${loadAdError.message}")
                    rewardedAd = null
                    
                    // Log ad load failure
                    AnalyticsLogger.logEvent("rewarded_ad_failed", Bundle().apply {
                        putString("ad_unit_id", adUnitId)
                        putString("error_code", loadAdError.code.toString())
                        putString("error_message", loadAdError.message)
                    })
                    
                    onAdFailedToLoad(loadAdError.message)
                }
            }
        )
    }
    
    /**
     * Show rewarded ad with debug output
     */
    fun showRewardedAd(
        activity: Activity,
        adUnitId: String,
        onRewarded: () -> Unit,
        onAdClosed: () -> Unit = {}
    ) {
        println("🎬 Attempting to show rewarded ad, isLoaded: ${rewardedAd != null}")
        
        // Log ad show attempt
        AnalyticsLogger.logEvent("rewarded_ad_show_attempt", Bundle().apply {
            putString("ad_unit_id", adUnitId)
            putBoolean("is_loaded", rewardedAd != null)
        })
        
        rewardedAd?.let { ad ->
            ad.fullScreenContentCallback = object : com.google.android.gms.ads.FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    println("👋 Rewarded ad dismissed")
                    rewardedAd = null
                    
                    // Log ad dismissed
                    AnalyticsLogger.logEvent("rewarded_ad_dismissed", Bundle().apply {
                        putString("ad_unit_id", adUnitId)
                    })
                    
                    // Preload next ad with callbacks
                    loadRewardedAd(
                        adUnitId = adUnitId,
                        onAdLoaded = { println("✓ Next rewarded ad preloaded") },
                        onAdFailedToLoad = { println("✗ Failed to preload next ad: $it") }
                    )
                    onAdClosed()
                }
                
                override fun onAdFailedToShowFullScreenContent(error: com.google.android.gms.ads.AdError) {
                    println("❌ Failed to show ad: ${error.message}")
                    rewardedAd = null
                    
                    // Log ad show failure
                    AnalyticsLogger.logEvent("rewarded_ad_show_failed", Bundle().apply {
                        putString("ad_unit_id", adUnitId)
                        putString("error_code", error.code.toString())
                        putString("error_message", error.message)
                    })
                    
                    onAdClosed()
                }
                
                override fun onAdShowedFullScreenContent() {
                    println("👁️ Rewarded ad shown full screen")
                    
                    // Log ad impression
                    AnalyticsLogger.logAdImpression("rewarded", "quiz_completion")
                }
            }
            
            try {
                ad.show(activity) { rewardItem ->
                    // User earned reward
                    println("🎁 User earned reward: ${rewardItem.amount} ${rewardItem.type}")
                    
                    // Log reward earned
                    AnalyticsLogger.logRewardedAdEarned("quiz_completion")
                    AnalyticsLogger.logEvent("reward_earned", Bundle().apply {
                        putString("ad_unit_id", adUnitId)
                        putInt("reward_amount", rewardItem.amount)
                        putString("reward_type", rewardItem.type)
                    })
                    
                    onRewarded()
                }
            } catch (e: Exception) {
                println("💥 Exception showing ad: ${e.message}")
                
                // Log exception
                AnalyticsLogger.logEvent("rewarded_ad_exception", Bundle().apply {
                    putString("ad_unit_id", adUnitId)
                    putString("error_message", e.message ?: "Unknown error")
                    putString("error_type", e.javaClass.simpleName)
                })
                
                onAdClosed()
            }
        } ?: run {
            println("⚠️ Rewarded ad not loaded, skipping")
            
            // Log ad not available
            AnalyticsLogger.logEvent("rewarded_ad_not_available", Bundle().apply {
                putString("ad_unit_id", adUnitId)
            })
            
            // Try to load for next time
            loadRewardedAd(
                adUnitId = adUnitId,
                onAdLoaded = { println("✓ Rewarded ad loaded for next time") },
                onAdFailedToLoad = { println("✗ Still failed to load ad: $it") }
            )
            onAdClosed()
        }
    }
    
    companion object {
        // Production Ad Unit IDs
        const val HOME_BANNER_AD_ID = "ca-app-pub-7931408789378206/4179750632"
        const val QUIZ_REWARDED_AD_ID = "ca-app-pub-7931408789378206/4729403210"
        const val INTERSTITIAL_AD_ID = "ca-app-pub-7931408789378206/5105819497" // Replace with your actual interstitial ad ID
        
        // Test Ad Unit IDs (for development only)
        private const val TEST_BANNER_AD_ID = "ca-app-pub-3940256099942544/6300978111"
        private const val TEST_INTERSTITIAL_AD_ID = "ca-app-pub-3940256099942544/1033173712"
        private const val TEST_REWARDED_AD_ID = "ca-app-pub-3940256099942544/5224354917"
        
        /**
         * Get the appropriate banner ad ID based on build type
         */
        fun getBannerAdId(): String {
            return if (BuildConfig.USE_TEST_ADS) TEST_BANNER_AD_ID else HOME_BANNER_AD_ID
        }
        
        /**
         * Get the appropriate interstitial ad ID based on build type
         */
        fun getInterstitialAdId(): String {
            return if (BuildConfig.USE_TEST_ADS) TEST_INTERSTITIAL_AD_ID else INTERSTITIAL_AD_ID
        }
        
        /**
         * Get the appropriate rewarded ad ID based on build type
         */
        fun getRewardedAdId(): String {
            return if (BuildConfig.USE_TEST_ADS) TEST_REWARDED_AD_ID else QUIZ_REWARDED_AD_ID
        }
    }
}
