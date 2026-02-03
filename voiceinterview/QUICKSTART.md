# Voice Interview SDK - Quick Start Guide

## 5-Minute Integration

### Step 1: Add Permissions (1 min)

Add to `AndroidManifest.xml`:
```xml
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<uses-permission android:name="android.permission.INTERNET" />
```

### Step 2: Add Dependency (1 min)

In `app/build.gradle.kts`:
```kotlin
dependencies {
    implementation(project(":voiceinterview"))
}
```

### Step 3: Initialize SDK (1 min)

```kotlin
val sdk = VoiceInterviewSDK.initialize(
    context = applicationContext,
    apiKey = "YOUR_OPENAI_API_KEY"
)
```

### Step 4: Add to Navigation (1 min)

```kotlin
// In NavGraph.kt
composable(route = "voice_interview") {
    AppVoiceInterviewScreen()
}
```

### Step 5: Request Permissions (1 min)

```kotlin
val permissionLauncher = rememberLauncherForActivityResult(
    ActivityResultContracts.RequestPermission()
) { granted ->
    if (granted) {
        navController.navigate("voice_interview")
    }
}

Button(onClick = {
    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
}) {
    Text("Start Interview")
}
```

## That's It! 🎉

The SDK handles:
- ✅ Speech recognition
- ✅ AI question generation  
- ✅ Intelligent evaluation
- ✅ Beautiful UI
- ✅ Auto-progression
- ✅ Error handling

## Test It

1. Run your app
2. Click "Start Interview"
3. Grant microphone permission
4. Select a topic
5. Configure difficulty & duration
6. Start speaking!

## Common Patterns

### Get Topics
```kotlin
val topics = sdk.getAvailableTopics()
```

### Start Interview
```kotlin
val config = InterviewConfig(
    topicId = "android",
    difficultyLevel = DifficultyLevel.INTERMEDIATE,
    duration = 15,
    numberOfQuestions = 5
)
val session = sdk.startInterview(config)
```

### Monitor Progress
```kotlin
session.state.collect { state ->
    when (state.status) {
        InterviewStatus.LISTENING -> showMicIndicator()
        InterviewStatus.PRESENTING_FEEDBACK -> showScore(state.currentResponse)
        InterviewStatus.COMPLETED -> showSummary()
    }
}
```

## Need More?

Check out [README.md](./README.md) for complete documentation.

## Troubleshooting

**Microphone not working?**
- Check permission is granted
- Ensure device has Google app installed

**Questions not generating?**
- Verify OpenAI API key is valid
- Check internet connection

**App crashes?**
- Ensure permissions requested before SDK init
- Check Logcat for error messages

## Examples

See the `ui` module for complete implementation example in `VoiceInterviewScreen.kt`.

---

Happy interviewing! 🎤
