package com.prepstack.voiceinterview.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment as ComposeAlignment
import androidx.compose.ui.Modifier
import app.rive.Fit
import app.rive.Rive
import app.rive.Result
import app.rive.RiveFileSource
import app.rive.rememberRiveFile
import app.rive.rememberRiveWorker
import app.rive.rememberViewModelInstance

/**
 * Rive animation using the Compose API for Rive 11.1.1
 * 
 * @param animationResId Resource ID (e.g., R.raw.takingbear)
 * @param modifier Modifier
 */
@Composable
fun RiveAnimation(
    animationResId: Int,
    modifier: Modifier = Modifier
) {
    // Create Rive worker
    val riveWorker = rememberRiveWorker()

    // Load Rive file
    val riveFile = rememberRiveFile(
        RiveFileSource.RawRes.from(animationResId),
        riveWorker
    )

    // Display based on loading state
    when (riveFile) {
        is Result.Loading -> {
            Box(
                modifier = modifier,
                contentAlignment = ComposeAlignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        is Result.Error -> {
            Box(
                modifier = modifier,
                contentAlignment = ComposeAlignment.Center
            ) {
                Text("Animation load error")
            }
        }

        is Result.Success -> {
            Rive(
                riveFile.value,
                modifier = modifier
            )
        }
    }
}

/**
 * Voice animation using takingbear.riv with Talk and Hear boolean inputs
 * 
 * IMPORTANT: Make sure your Rive file's state machine has:
 * 1. Boolean input named "Talk"
 * 2. Boolean input named "Hear"
 * 3. Transitions configured to respond to these inputs
 * 
 * @param animationResId Resource ID (R.raw.takingbear)
 * @param isListening Whether user is speaking (true = Hear active, false = Talk active)
 * @param modifier Modifier
 */
@Composable
fun VoiceRiveAnimation(
    animationResId: Int,
    isListening: Boolean,
    modifier: Modifier = Modifier
) {
    val riveWorker = rememberRiveWorker()
    val riveFileResult = rememberRiveFile(
        RiveFileSource.RawRes.from(animationResId),
        riveWorker
    )

    when (riveFileResult) {
        is Result.Loading -> {
            Box(
                modifier = modifier,
                contentAlignment = ComposeAlignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        is Result.Error -> {
            Box(
                modifier = modifier,
                contentAlignment = ComposeAlignment.Center
            ) {
                Text("Animation load error: ${riveFileResult.throwable.message}")
            }
        }

        is Result.Success -> {
            val riveFile = riveFileResult.value
            val vmi = rememberViewModelInstance(riveFile)

            // Control Talk and Hear inputs based on isListening state
            LaunchedEffect(isListening) {
                try {
                    // Set boolean inputs for state machine
                    // The inputs must exist in the Rive file's state machine
                    vmi.setBoolean("Talk", !isListening)
                    vmi.setBoolean("Hear", isListening)
                    
                    android.util.Log.d(
                        "VoiceRiveAnimation",
                        "✅ Inputs set successfully - Talk: ${!isListening}, Hear: $isListening"
                    )
                } catch (e: Exception) {
                    android.util.Log.e(
                        "VoiceRiveAnimation",
                        "❌ Failed to set inputs: ${e.message}. " +
                        "Make sure 'Talk' and 'Hear' boolean inputs exist in your Rive state machine.",
                        e
                    )
                }
            }

            Rive(
                file = riveFile,
                viewModelInstance = vmi,
                fit = Fit.Contain(),
                modifier = modifier
            )
        }
    }
}
