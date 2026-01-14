package com.prepstack.ads

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.prepstack.core.util.AnalyticsLogger

/**
 * Composable Banner Ad View
 * Safe abstraction for displaying banner ads in Compose
 * With analytics tracking for load events and impressions
 */
@Composable
fun BannerAdView(
    modifier: Modifier = Modifier,
    adUnitId: String = AdManager.getBannerAdId(), // Use helper method to get the correct ID based on build type
    placement: String = "default"
) {
    val context = LocalContext.current
    
    // Log banner ad request
    DisposableEffect(adUnitId) {
        AnalyticsLogger.logEvent("banner_ad_requested", android.os.Bundle().apply {
            putString("ad_unit_id", adUnitId)
            putString("placement", placement)
        })
        
        onDispose { }
    }
    
    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { context ->
            AdView(context).apply {
                setAdSize(AdSize.BANNER)
                this.adUnitId = adUnitId
                
                adListener = object : AdListener() {
                    override fun onAdLoaded() {
                        // Log ad loaded event
                        AnalyticsLogger.logEvent("banner_ad_loaded", android.os.Bundle().apply {
                            putString("ad_unit_id", adUnitId)
                            putString("placement", placement)
                        })
                    }
                    
                    override fun onAdFailedToLoad(error: LoadAdError) {
                        // Log ad failed event
                        AnalyticsLogger.logEvent("banner_ad_failed", android.os.Bundle().apply {
                            putString("ad_unit_id", adUnitId)
                            putString("placement", placement)
                            putString("error_code", error.code.toString())
                            putString("error_message", error.message)
                        })
                    }
                    
                    override fun onAdImpression() {
                        // Log ad impression
                        AnalyticsLogger.logAdImpression("banner", placement)
                    }
                    
                    override fun onAdClicked() {
                        // Log ad click
                        AnalyticsLogger.logEvent("banner_ad_clicked", android.os.Bundle().apply {
                            putString("ad_unit_id", adUnitId)
                            putString("placement", placement)
                        })
                    }
                }
                
                loadAd(AdRequest.Builder().build())
            }
        }
    )
}
