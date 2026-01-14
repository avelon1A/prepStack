package com.prepstack.ads

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

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
     */
    fun loadInterstitialAd(adUnitId: String = TEST_INTERSTITIAL_AD_ID) {
        val adRequest = AdRequest.Builder().build()
        
        InterstitialAd.load(
            context,
            adUnitId,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                }
                
                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    interstitialAd = null
                }
            }
        )
    }
    
    /**
     * Show interstitial ad if loaded
     */
    fun showInterstitialAd(activity: Activity, onAdClosed: () -> Unit = {}) {
        interstitialAd?.let { ad ->
            ad.fullScreenContentCallback = object : com.google.android.gms.ads.FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    interstitialAd = null
                    loadInterstitialAd() // Preload next ad
                    onAdClosed()
                }
                
                override fun onAdFailedToShowFullScreenContent(error: com.google.android.gms.ads.AdError) {
                    interstitialAd = null
                    onAdClosed()
                }
            }
            ad.show(activity)
        } ?: run {
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
            showInterstitialAd(activity, onAdClosed)
        } else {
            onAdClosed()
        }
    }
    
    /**
     * Load rewarded ad with callbacks for tracking load status
     */
    fun loadRewardedAd(
        adUnitId: String = TEST_REWARDED_AD_ID,
        onAdLoaded: () -> Unit = {},
        onAdFailedToLoad: (String) -> Unit = {}
    ) {
        val adRequest = AdRequest.Builder().build()
        
        // Debug output
        println("🔄 Loading rewarded ad: $adUnitId")
        
        RewardedAd.load(
            context,
            adUnitId,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    println("✓ Rewarded ad loaded successfully")
                    rewardedAd = ad
                    onAdLoaded()
                }
                
                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    println("✗ Rewarded ad failed to load: ${loadAdError.message}")
                    rewardedAd = null
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
        onRewarded: () -> Unit,
        onAdClosed: () -> Unit = {}
    ) {
        println("🎬 Attempting to show rewarded ad, isLoaded: ${rewardedAd != null}")
        
        rewardedAd?.let { ad ->
            ad.fullScreenContentCallback = object : com.google.android.gms.ads.FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    println("👋 Rewarded ad dismissed")
                    rewardedAd = null
                    // Preload next ad with callbacks
                    loadRewardedAd(
                        onAdLoaded = { println("✓ Next rewarded ad preloaded") },
                        onAdFailedToLoad = { println("✗ Failed to preload next ad: $it") }
                    )
                    onAdClosed()
                }
                
                override fun onAdFailedToShowFullScreenContent(error: com.google.android.gms.ads.AdError) {
                    println("❌ Failed to show ad: ${error.message}")
                    rewardedAd = null
                    onAdClosed()
                }
                
                override fun onAdShowedFullScreenContent() {
                    println("👁️ Rewarded ad shown full screen")
                }
            }
            
            try {
                ad.show(activity) { rewardItem ->
                    // User earned reward
                    println("🎁 User earned reward: ${rewardItem.amount} ${rewardItem.type}")
                    onRewarded()
                }
            } catch (e: Exception) {
                println("💥 Exception showing ad: ${e.message}")
                onAdClosed()
            }
        } ?: run {
            println("⚠️ Rewarded ad not loaded, skipping")
            // Try to load for next time
            loadRewardedAd(
                onAdLoaded = { println("✓ Rewarded ad loaded for next time") },
                onAdFailedToLoad = { println("✗ Still failed to load ad: $it") }
            )
            onAdClosed()
        }
    }
    
    companion object {
        // Test Ad Unit IDs - Replace with real IDs in production
        const val TEST_BANNER_AD_ID = "ca-app-pub-3940256099942544/6300978111"
        const val TEST_INTERSTITIAL_AD_ID = "ca-app-pub-3940256099942544/1033173712"
        const val TEST_REWARDED_AD_ID = "ca-app-pub-3940256099942544/5224354917"
    }
}
