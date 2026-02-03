# Voice Interview SDK - Technical Documentation

## Overview

The Voice Interview SDK is an Android library that provides AI-powered voice-based interview functionality. It enables users to practice technical interviews through natural voice conversations, with real-time speech recognition, AI-generated questions, and intelligent feedback.

## Features

- 🎤 **Voice Input**: Real-time speech recognition for answering questions
- 🤖 **AI-Powered**: OpenAI GPT-4 integration for dynamic question generation
- 📊 **Smart Evaluation**: Detailed feedback with scoring (1-10 scale)
- 🎯 **Multi-Topic Support**: Pre-configured topics (Android, React, Java, Python, etc.)
- 🔧 **Customizable**: Create custom topics with adjustable difficulty levels
- ⚡ **Auto-Flow**: Automatic progression between questions
- 🎨 **Modern UI**: Circular wave animations and clean Material Design 3
- 📝 **Fallback Option**: Type answers if voice isn't available

## Architecture

### Module Structure

```
voiceinterview/
├── core/
│   ├── InterviewController.kt      # Main SDK controller
│   ├── InterviewSession.kt         # Session management
│   ├── model/                      # Data models
│   │   └── Models.kt
│   └── service/
│       ├── AIService.kt            # OpenAI integration
│       └── InterviewRepository.kt  # Data management
├── speech/
│   ├── SpeechManager.kt            # Speech coordination
│   ├── SpeechRecognitionManager.kt # Android STT
│   └── TextToSpeechManager.kt      # Android TTS
├── ui/
│   ├── VoiceInterviewScreen.kt     # Main UI composables
│   └── VoiceInterviewViewModel.kt  # UI state management
└── VoiceInterviewSDK.kt            # Public API entry point
```

### Component Diagram

```
┌─────────────────────────────────────────┐
│      VoiceInterviewSDK (Public API)     │
└────────────────┬────────────────────────┘
                 │
    ┌────────────┴────────────┐
    │                         │
┌───▼──────────────┐  ┌──────▼─────────────┐
│Interview         │  │   UI Layer         │
│Controller        │  │   (Composables)    │
└───┬──────────────┘  └────────────────────┘
    │
    ├──► InterviewSession
    │    ├──► SpeechManager
    │    │    ├──► SpeechRecognition
    │    │    └──► TextToSpeech
    │    ├──► AIService (OpenAI)
    │    └──► InterviewRepository
    │
    └──► State Management (Flow)
```

## Installation

### 1. Add Module Dependency

In your app's `build.gradle.kts`:

```kotlin
dependencies {
    implementation(project(":voiceinterview"))
}
```

### 2. Add Required Permissions

In your `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<uses-permission android:name="android.permission.INTERNET" />
```

### 3. Request Runtime Permissions

```kotlin
val permissions = arrayOf(
    Manifest.permission.RECORD_AUDIO
)
ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE)
```

## Usage

### Initialize SDK

```kotlin
val sdk = VoiceInterviewSDK.initialize(
    context = applicationContext,
    apiKey = "your-openai-api-key"
)
```

### Integration with Jetpack Compose

```kotlin
@Composable
fun YourScreen() {
    val viewModel: VoiceInterviewViewModel = viewModel(
        factory = VoiceInterviewViewModelFactory(
            application = LocalContext.current.applicationContext as Application,
            apiKey = "your-openai-api-key"
        )
    )
    
    AppVoiceInterviewScreen(viewModel = viewModel)
}
```

### Get Available Topics

```kotlin
val topics = sdk.getAvailableTopics()
// Returns list of InterviewTopic objects
```

### Start Interview

```kotlin
val config = InterviewConfig(
    topicId = "android",
    difficultyLevel = DifficultyLevel.INTERMEDIATE,
    duration = 15, // minutes
    numberOfQuestions = 5
)

val session = sdk.startInterview(config)
```

### Session Control

```kotlin
// Start listening for answer
session.startListening()

// Stop listening
session.stopListening()

// Submit text answer (alternative to voice)
session.submitTextAnswer("My answer here...")

// Move to next question
session.nextQuestion()

// End interview
val summary = session.end()
```

### Monitor State

```kotlin
session.state.collect { state ->
    when (state.status) {
        InterviewStatus.INITIALIZING -> { /* Show loading */ }
        InterviewStatus.PRESENTING_QUESTION -> { /* Show question */ }
        InterviewStatus.LISTENING -> { /* Show listening indicator */ }
        InterviewStatus.PROCESSING_RESPONSE -> { /* Show processing */ }
        InterviewStatus.PRESENTING_FEEDBACK -> { /* Show feedback */ }
        InterviewStatus.COMPLETED -> { /* Show summary */ }
        InterviewStatus.ERROR -> { /* Handle error */ }
    }
}
```

## Data Models

### InterviewTopic

```kotlin
data class InterviewTopic(
    val id: String,
    val name: String,
    val description: String,
    val category: InterviewCategory,
    val difficultyLevels: List<DifficultyLevel>,
    val estimatedTimeMinutes: Int,
    val iconUrl: String
)
```

### InterviewConfig

```kotlin
data class InterviewConfig(
    val topicId: String,
    val difficultyLevel: DifficultyLevel,
    val duration: Int,
    val numberOfQuestions: Int
)
```

### InterviewState

```kotlin
data class InterviewState(
    val sessionId: String,
    val status: InterviewStatus,
    val currentQuestion: InterviewQuestion?,
    val currentResponse: InterviewResponse?,
    val questionsAnswered: Int,
    val totalQuestions: Int,
    val error: String?
)
```

### InterviewQuestion

```kotlin
data class InterviewQuestion(
    val id: String,
    val text: String,
    val category: String,
    val difficultyLevel: DifficultyLevel,
    val expectedAnswerPoints: List<String>
)
```

### InterviewResponse

```kotlin
data class InterviewResponse(
    val questionId: String,
    val transcribedAnswer: String,
    val evaluation: InterviewEvaluation,
    val nextQuestionId: String?
)
```

### InterviewEvaluation

```kotlin
data class InterviewEvaluation(
    val score: Int, // 1-10
    val feedbackSummary: String,
    val strengths: List<String>,
    val improvements: List<String>,
    val suggestedResources: List<String>
)
```

### InterviewSummary

```kotlin
data class InterviewSummary(
    val sessionId: String,
    val topicName: String,
    val difficultyLevel: DifficultyLevel,
    val totalQuestions: Int,
    val questionsAnswered: Int,
    val averageScore: Double,
    val totalDurationMinutes: Int,
    val overallFeedback: String,
    val strongAreas: List<String>,
    val improvementAreas: List<String>,
    val detailedResults: List<QuestionResult>
)
```

## Configuration

### Dynamic Question Count

The SDK automatically calculates optimal question count based on:
- **Difficulty Level**: Harder questions get more time
- **Interview Duration**: Longer interviews = more questions
- **Time per Question**:
  - Beginner: ~2 minutes/question
  - Intermediate: ~2.5 minutes/question
  - Advanced: ~3.5 minutes/question
  - Expert: ~4.5 minutes/question

Formula:
```kotlin
val avgTimePerQuestion = when (difficulty) {
    BEGINNER -> 2.0
    INTERMEDIATE -> 2.5
    ADVANCED -> 3.5
    EXPERT -> 4.5
}
val questionCount = (durationMinutes / avgTimePerQuestion).toInt()
return questionCount.coerceIn(3, 12) // Min 3, Max 12
```

### Custom Topics

Create custom interview topics on-the-fly:

```kotlin
val customTopic = InterviewTopic(
    id = "custom_flutter",
    name = "Flutter",
    description = "Custom interview topic: Flutter",
    category = InterviewCategory.TECHNICAL,
    difficultyLevels = listOf(DifficultyLevel.INTERMEDIATE),
    estimatedTimeMinutes = 15,
    iconUrl = ""
)
```

### AI Configuration

The SDK uses OpenAI's GPT-4 API. Default settings:

```kotlin
// API Configuration
baseUrl = "https://api.openai.com/v1"
model = "gpt-4"
temperature = 0.7
maxTokens = 500
```

## UI Components

### Main Screen

```kotlin
@Composable
fun VoiceInterviewScreen(
    viewModel: VoiceInterviewViewModel,
    onBackClick: () -> Unit
)
```

### Topic Selection

```kotlin
@Composable
fun TopicSelectionScreen(
    topics: List<InterviewTopic>,
    onTopicSelected: (InterviewTopic) -> Unit
)
```

### Interview Configuration

```kotlin
@Composable
fun InterviewConfigScreen(
    topic: InterviewTopic,
    onStartInterview: (InterviewConfig) -> Unit,
    onBackToTopicSelection: () -> Unit
)
```

### Active Interview

```kotlin
@Composable
fun ActiveInterviewScreen(
    interviewState: InterviewState,
    onStartListening: () -> Unit,
    onStopListening: () -> Unit,
    onSubmitTextAnswer: (String) -> Unit,
    onContinue: () -> Unit,
    onEndInterview: () -> Unit
)
```

### UI Features

#### Circular Wave Animation
```kotlin
@Composable
fun CircularWaveAnimation(isListening: Boolean)
```
- 4 animated concentric rings
- Pulsing center circle
- Color-coded states (Primary/Tertiary)
- Smooth fade-in/fade-out effects

#### Auto-Proceed
- 3-second delay after feedback
- Visual progress indicator
- Skip option available
- Automatic next question or interview end

## Speech Recognition

### Android SpeechRecognizer

The SDK uses Android's built-in speech recognition:

```kotlin
interface SpeechRecognitionManager {
    fun startListening(callback: SpeechRecognitionCallback)
    fun stopListening()
    fun isListening(): Boolean
    fun shutdown()
}
```

### Error Handling

Handles common speech recognition errors:
- `ERROR_AUDIO`: Audio recording error
- `ERROR_CLIENT`: Client side error
- `ERROR_INSUFFICIENT_PERMISSIONS`: Permission denied
- `ERROR_NETWORK`: Network connectivity issues
- `ERROR_NO_MATCH`: No speech detected
- `ERROR_RECOGNIZER_BUSY`: Service busy
- `ERROR_SPEECH_TIMEOUT`: Silence timeout

### Configuration

```kotlin
// Speech recognition settings
EXTRA_LANGUAGE_MODEL = LANGUAGE_MODEL_FREE_FORM
EXTRA_PARTIAL_RESULTS = true
EXTRA_MAX_RESULTS = 3
EXTRA_SPEECH_INPUT_COMPLETE_SILENCE = 3000ms
EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE = 1500ms
```

## Text-to-Speech

### Android TTS

```kotlin
interface TextToSpeechManager {
    fun speak(text: String, onComplete: (() -> Unit)?)
    fun stop()
    fun shutdown()
}
```

### Features
- Automatic question reading
- Completion callbacks
- Queue management
- Pitch and speed control

## OpenAI Integration

### API Endpoints

```kotlin
POST /v1/chat/completions
```

### Request Format

```json
{
  "model": "gpt-4",
  "messages": [
    {
      "role": "system",
      "content": "You are an expert technical interviewer..."
    },
    {
      "role": "user",
      "content": "Generate a question about Android..."
    }
  ],
  "temperature": 0.7,
  "max_tokens": 500
}
```

### Response Processing

The SDK automatically parses AI responses and extracts:
- Question text
- Expected answer points
- Evaluation criteria
- Follow-up suggestions

## State Management

### StateFlow Architecture

```kotlin
// ViewModel
private val _uiState = MutableStateFlow<VoiceInterviewUiState>(Loading)
val uiState: StateFlow<VoiceInterviewUiState> = _uiState.asStateFlow()

// UI observes state
val uiState by viewModel.uiState.collectAsState()
```

### UI States

```kotlin
sealed class VoiceInterviewUiState {
    object Loading : VoiceInterviewUiState()
    data class TopicSelection(val topics: List<InterviewTopic>)
    data class ConfigureInterview(val topic: InterviewTopic)
    data class ActiveInterview(val interviewState: InterviewState)
    data class InterviewSummary(val summary: CoreInterviewSummary)
    data class Error(val errorMessage: String)
}
```

## Error Handling

### Common Errors

1. **Permission Denied**
   ```kotlin
   error: "RECORD_AUDIO permission not granted"
   solution: Request runtime permission
   ```

2. **Network Error**
   ```kotlin
   error: "Network error"
   solution: Check internet connectivity
   ```

3. **API Key Invalid**
   ```kotlin
   error: "Unauthorized"
   solution: Verify OpenAI API key
   ```

4. **Speech Recognition Unavailable**
   ```kotlin
   error: "Speech recognition is not available"
   solution: Ensure device has Google app installed
   ```

### Error Recovery

```kotlin
fun retryAfterError() {
    when (val currentState = uiState.value) {
        is VoiceInterviewUiState.Error -> {
            loadTopics() // Retry loading
        }
        else -> { /* Handle other cases */ }
    }
}
```

## Performance Optimization

### Memory Management
- Automatic cleanup of speech resources
- Proper coroutine scope management
- StateFlow instead of LiveData for efficiency

### Network Optimization
- Timeout configurations (30s connect, 60s read)
- Retry logic for failed requests
- Offline fallback (simulated responses)

### UI Optimization
- Lazy loading of topics
- Debounced user inputs
- Efficient recomposition with `remember`

## Testing

### Unit Tests

```kotlin
@Test
fun `test question count calculation`() {
    val count = calculateQuestionCount(
        difficultyLevel = DifficultyLevel.INTERMEDIATE,
        durationMinutes = 15
    )
    assertEquals(6, count)
}
```

### Integration Tests

```kotlin
@Test
suspend fun `test interview session flow`() {
    val session = sdk.startInterview(config)
    session.startListening()
    session.submitTextAnswer("Test answer")
    val hasNext = session.nextQuestion()
    assertTrue(hasNext)
}
```

### UI Tests

```kotlin
@Test
fun `test topic selection screen displays topics`() {
    composeTestRule.setContent {
        TopicSelectionScreen(
            topics = listOf(testTopic),
            onTopicSelected = {}
        )
    }
    composeTestRule.onNodeWithText("Android").assertExists()
}
```

## Troubleshooting

### Issue: Microphone not working

**Symptoms**: Error immediately after question
**Cause**: Speech recognizer not properly cleaned up
**Solution**: Ensure 500ms delay between questions (already implemented)

### Issue: Questions not generating

**Symptoms**: Stuck on "Waiting for question..."
**Cause**: API key invalid or network issue
**Solution**: 
```kotlin
// Check API key
Log.d("VoiceInterview", "API Key: ${apiKey.take(10)}...")

// Check network
if (!isNetworkAvailable()) {
    // Show error
}
```

### Issue: Auto-proceed not working

**Symptoms**: Stuck on feedback screen
**Cause**: LaunchedEffect not triggered
**Solution**: Ensure proper state updates and delay implementation

### Issue: App crashes on start

**Symptoms**: Crash when initializing SDK
**Cause**: Missing permissions or invalid context
**Solution**: 
```kotlin
// Ensure permissions granted
if (ContextCompat.checkSelfPermission(context, RECORD_AUDIO) 
    != PERMISSION_GRANTED) {
    // Request permission
}
```

## Best Practices

### 1. Resource Management
```kotlin
override fun onCleared() {
    super.onCleared()
    sdk.shutdown() // Always cleanup
}
```

### 2. Permission Handling
```kotlin
// Request permissions before initializing
val permissionLauncher = rememberLauncherForActivityResult(
    ActivityResultContracts.RequestPermission()
) { granted ->
    if (granted) {
        // Initialize SDK
    }
}
```

### 3. Error Boundaries
```kotlin
try {
    session.startListening()
} catch (e: Exception) {
    // Show user-friendly error
    showError("Unable to start microphone: ${e.message}")
}
```

### 4. State Preservation
```kotlin
// Save state for process death
@Composable
fun rememberInterviewState() {
    val savedStateHandle = viewModel.savedStateHandle
    // Use savedStateHandle for persistence
}
```

## API Reference

### VoiceInterviewSDK

```kotlin
class VoiceInterviewSDK {
    companion object {
        fun initialize(
            context: Context,
            apiKey: String,
            baseUrl: String = "https://api.openai.com/v1"
        ): VoiceInterviewSDK
    }
    
    suspend fun getAvailableTopics(): List<InterviewTopic>
    suspend fun startInterview(config: InterviewConfig): InterviewSession
    fun shutdown()
}
```

### InterviewSession

```kotlin
interface InterviewSession {
    val sessionId: String
    val config: InterviewConfig
    val state: StateFlow<InterviewState>
    
    suspend fun startListening(): Boolean
    suspend fun stopListening(): Boolean
    suspend fun submitTextAnswer(textAnswer: String): Boolean
    suspend fun nextQuestion(): Boolean
    suspend fun end(generateSummary: Boolean = true): InterviewSummary?
    suspend fun pause(): Boolean
    suspend fun resume(): Boolean
}
```

## Dependencies

```kotlin
// Kotlin & Coroutines
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

// Compose
implementation(platform("androidx.compose:compose-bom:2024.11.00"))
implementation("androidx.compose.material3:material3")
implementation("androidx.compose.ui:ui-tooling-preview")

// Lifecycle & ViewModel
implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")

// Navigation
implementation("androidx.navigation:navigation-compose:2.8.7")

// Serialization
implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

// HTTP Client
implementation("com.squareup.okhttp3:okhttp:4.12.0")
```

## Version History

### v1.0.0 (Current)
- ✅ Initial release
- ✅ Voice input/output
- ✅ OpenAI GPT-4 integration
- ✅ Multiple difficulty levels
- ✅ Custom topic support
- ✅ Auto-proceed feature
- ✅ Circular wave animations
- ✅ Material Design 3 UI

## License

```
Copyright 2024 PrepStack

Licensed under the Apache License, Version 2.0
```

## Support

For issues, feature requests, or questions:
- GitHub Issues: [Your repo URL]
- Email: support@prepstack.com
- Documentation: [Your docs URL]

## Contributors

- Development Team: PrepStack
- AI Integration: OpenAI GPT-4
- UI/UX Design: Material Design 3

---

**Built with ❤️ for better interview preparation**
