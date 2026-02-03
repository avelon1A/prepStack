package com.prepstack.ui.screen

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import com.prepstack.voiceinterview.ui.VoiceInterviewScreen
import com.prepstack.voiceinterview.ui.VoiceInterviewViewModel

/**
 * Wrapper for the Voice Interview Screen in the main app.
 * This screen integrates the Voice Interview SDK into the app.
 */
@Composable
fun AppVoiceInterviewScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    var showRationale by remember { mutableStateOf(false) }
    
    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasPermission = isGranted
        if (!isGranted) {
            showRationale = true
        }
    }
    
    // Check and request permission on first composition
    LaunchedEffect(Unit) {
        if (!hasPermission) {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }
    
    if (!hasPermission) {
        // Show permission request screen
        PermissionRequestScreen(
            showRationale = showRationale,
            onRequestPermission = {
                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            },
            onOpenSettings = {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                }
                context.startActivity(intent)
            },
            onBack = onBackClick
        )
    } else {
        // Permission granted, show interview screen
        // TODO: Replace with your own OpenAI API key
        val apiKey = "YOUR_OPENAI_API_KEY_HERE"

        val viewModel = remember {
            VoiceInterviewViewModel(
                application = context.applicationContext as android.app.Application,
                apiKey = apiKey
            )
        }
        
        VoiceInterviewScreen(
            viewModel = viewModel,
            onBackClick = onBackClick
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PermissionRequestScreen(
    showRationale: Boolean,
    onRequestPermission: () -> Unit,
    onOpenSettings: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Voice Interview") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Mic,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Microphone Permission Required",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = if (showRationale) {
                    "Voice Interview needs microphone access to listen to your answers. " +
                    "Please grant the permission in app settings to use this feature."
                } else {
                    "Voice Interview uses your microphone to record and evaluate your spoken answers. " +
                    "This helps you practice technical interviews naturally through conversation."
                },
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            if (showRationale) {
                Button(
                    onClick = onOpenSettings,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text("Open Settings")
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text("Go Back")
                }
            } else {
                Button(
                    onClick = onRequestPermission,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text("Grant Permission")
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                TextButton(onClick = onBack) {
                    Text("Maybe Later")
                }
            }
        }
    }
}
