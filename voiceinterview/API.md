# Voice Interview SDK - API Reference

## Table of Contents
- [VoiceInterviewSDK](#voiceinterviewsdk)
- [InterviewSession](#interviewsession)
- [Data Models](#data-models)
- [Enums](#enums)
- [Callbacks](#callbacks)

---

## VoiceInterviewSDK

Main entry point for the Voice Interview SDK.

### Methods

#### `initialize()`
Initialize the SDK instance.

```kotlin
fun initialize(
    context: Context,
    apiKey: String,
    baseUrl: String = "https://api.openai.com/v1"
): VoiceInterviewSDK
```

**Parameters:**
- `context`: Application context
- `apiKey`: OpenAI API key
- `baseUrl`: (Optional) Custom API endpoint

**Returns:** `VoiceInterviewSDK` instance

**Example:**
```kotlin
val sdk = VoiceInterviewSDK.initialize(
    context = applicationContext,
    apiKey = "sk-..."
)
```

---

#### `getAvailableTopics()`
Retrieve list of available interview topics.

```kotlin
suspend fun getAvailableTopics(): List<InterviewTopic>
```

**Returns:** List of `InterviewTopic` objects

**Example:**
```kotlin
val topics = sdk.getAvailableTopics()
topics.forEach { topic ->
    println("${topic.name}: ${topic.description}")
}
```

---

#### `startInterview()`
Start a new interview session.

```kotlin
suspend fun startInterview(
    config: InterviewConfig
): InterviewSession
```

**Parameters:**
- `config`: Interview configuration

**Returns:** `InterviewSession` instance

**Throws:** `Exception` if session cannot be created

**Example:**
```kotlin
val config = InterviewConfig(
    topicId = "android",
    difficultyLevel = DifficultyLevel.INTERMEDIATE,
    duration = 15,
    numberOfQuestions = 5
)
val session = sdk.startInterview(config)
```

---

#### `shutdown()`
Release all resources and cleanup.

```kotlin
fun shutdown()
```

**Example:**
```kotlin
override fun onDestroy() {
    super.onDestroy()
    sdk.shutdown()
}
```

---

## InterviewSession

Represents an active interview session.

### Properties

#### `sessionId`
Unique identifier for this session.

```kotlin
val sessionId: String
```

---

#### `config`
Configuration used for this interview.

```kotlin
val config: InterviewConfig
```

---

#### `state`
Current state of the interview (Flow).

```kotlin
val state: StateFlow<InterviewState>
```

**Example:**
```kotlin
session.state.collect { state ->
    println("Status: ${state.status}")
    println("Question: ${state.currentQuestion?.text}")
}
```

---

### Methods

#### `startListening()`
Begin listening for user's voice answer.

```kotlin
suspend fun startListening(): Boolean
```

**Returns:** `true` if listening started successfully

**Example:**
```kotlin
if (session.startListening()) {
    showMicrophoneIndicator()
}
```

---

#### `stopListening()`
Stop listening and process captured audio.

```kotlin
suspend fun stopListening(): Boolean
```

**Returns:** `true` if stopped successfully

**Example:**
```kotlin
session.stopListening()
```

---

#### `submitTextAnswer()`
Submit a text answer instead of voice.

```kotlin
suspend fun submitTextAnswer(
    textAnswer: String
): Boolean
```

**Parameters:**
- `textAnswer`: The text answer to submit

**Returns:** `true` if submitted successfully

**Example:**
```kotlin
session.submitTextAnswer("Activities have lifecycle callbacks...")
```

---

#### `nextQuestion()`
Move to the next question.

```kotlin
suspend fun nextQuestion(): Boolean
```

**Returns:** `true` if there is a next question

**Example:**
```kotlin
if (session.nextQuestion()) {
    println("Moved to next question")
} else {
    println("No more questions")
}
```

---

#### `end()`
End the interview session.

```kotlin
suspend fun end(
    generateSummary: Boolean = true
): InterviewSummary?
```

**Parameters:**
- `generateSummary`: Whether to generate summary report

**Returns:** `InterviewSummary` if requested, `null` otherwise

**Example:**
```kotlin
val summary = session.end(generateSummary = true)
println("Average Score: ${summary?.averageScore}")
```

---

#### `pause()`
Pause the current session.

```kotlin
suspend fun pause(): Boolean
```

**Returns:** `true` if paused successfully

---

#### `resume()`
Resume a paused session.

```kotlin
suspend fun resume(): Boolean
```

**Returns:** `true` if resumed successfully

---

## Data Models

### InterviewTopic

Represents an interview topic.

```kotlin
data class InterviewTopic(
    val id: String,                              // Unique identifier
    val name: String,                            // Display name
    val description: String,                     // Topic description
    val category: InterviewCategory,             // Category type
    val difficultyLevels: List<DifficultyLevel>, // Available levels
    val estimatedTimeMinutes: Int,               // Estimated time
    val iconUrl: String                          // Icon URL
)
```

**Example:**
```kotlin
val androidTopic = InterviewTopic(
    id = "android",
    name = "Android Development",
    description = "Android app development interview",
    category = InterviewCategory.TECHNICAL,
    difficultyLevels = listOf(
        DifficultyLevel.BEGINNER,
        DifficultyLevel.INTERMEDIATE,
        DifficultyLevel.ADVANCED
    ),
    estimatedTimeMinutes = 15,
    iconUrl = "https://..."
)
```

---

### InterviewConfig

Configuration for starting an interview.

```kotlin
data class InterviewConfig(
    val topicId: String,              // Topic identifier
    val difficultyLevel: DifficultyLevel, // Difficulty level
    val duration: Int,                 // Duration in minutes
    val numberOfQuestions: Int         // Number of questions
)
```

**Example:**
```kotlin
val config = InterviewConfig(
    topicId = "android",
    difficultyLevel = DifficultyLevel.INTERMEDIATE,
    duration = 15,
    numberOfQuestions = 5
)
```

---

### InterviewState

Current state of an interview session.

```kotlin
data class InterviewState(
    val sessionId: String,                    // Session ID
    val status: InterviewStatus,              // Current status
    val currentQuestion: InterviewQuestion?,  // Current question
    val currentResponse: InterviewResponse?,  // Latest response
    val questionsAnswered: Int,               // Questions completed
    val totalQuestions: Int,                  // Total questions
    val error: String?                        // Error message
)
```

---

### InterviewQuestion

A question in the interview.

```kotlin
data class InterviewQuestion(
    val id: String,                           // Question ID
    val text: String,                         // Question text
    val category: String,                     // Category
    val difficultyLevel: DifficultyLevel,     // Difficulty
    val expectedAnswerPoints: List<String>    // Expected points
)
```

---

### InterviewResponse

User's response to a question.

```kotlin
data class InterviewResponse(
    val questionId: String,                   // Question ID
    val transcribedAnswer: String,            // User's answer
    val evaluation: InterviewEvaluation,      // Evaluation
    val nextQuestionId: String?               // Next question ID
)
```

---

### InterviewEvaluation

Evaluation of user's response.

```kotlin
data class InterviewEvaluation(
    val score: Int,                           // Score (1-10)
    val feedbackSummary: String,              // Summary
    val strengths: List<String>,              // What went well
    val improvements: List<String>,           // Areas to improve
    val suggestedResources: List<String>      // Learning resources
)
```

**Example:**
```kotlin
val evaluation = InterviewEvaluation(
    score = 8,
    feedbackSummary = "Good understanding of Activity lifecycle",
    strengths = listOf(
        "Mentioned onCreate and onDestroy",
        "Explained configuration changes"
    ),
    improvements = listOf(
        "Could mention onSaveInstanceState"
    ),
    suggestedResources = listOf(
        "Android Lifecycle Documentation"
    )
)
```

---

### InterviewSummary

Summary of completed interview.

```kotlin
data class InterviewSummary(
    val sessionId: String,                    // Session ID
    val topicName: String,                    // Topic name
    val difficultyLevel: DifficultyLevel,     // Difficulty
    val totalQuestions: Int,                  // Total questions
    val questionsAnswered: Int,               // Answered count
    val averageScore: Double,                 // Average score
    val totalDurationMinutes: Int,            // Total time
    val overallFeedback: String,              // Overall feedback
    val strongAreas: List<String>,            // Strong areas
    val improvementAreas: List<String>,       // Weak areas
    val detailedResults: List<QuestionResult> // Detailed results
)
```

---

### QuestionResult

Detailed result for a single question.

```kotlin
data class QuestionResult(
    val questionText: String,                 // Question
    val userAnswer: String,                   // Answer
    val score: Int,                           // Score (1-10)
    val feedback: String                      // Feedback
)
```

---

## Enums

### InterviewStatus

Current status of interview session.

```kotlin
enum class InterviewStatus {
    INITIALIZING,           // Setting up
    WAITING_FOR_QUESTION,   // Loading question
    PRESENTING_QUESTION,    // Speaking question
    LISTENING,              // Recording answer
    PROCESSING_RESPONSE,    // Evaluating answer
    PRESENTING_FEEDBACK,    // Showing feedback
    COMPLETED,              // Interview done
    ERROR                   // Error occurred
}
```

---

### DifficultyLevel

Interview difficulty levels.

```kotlin
enum class DifficultyLevel {
    BEGINNER,      // Entry-level questions
    INTERMEDIATE,  // Mid-level questions
    ADVANCED,      // Senior-level questions
    EXPERT         // Principal/Architect level
}
```

---

### InterviewCategory

Topic categories.

```kotlin
enum class InterviewCategory {
    TECHNICAL,     // Technical/Coding
    BEHAVIORAL,    // Behavioral questions
    SYSTEM_DESIGN, // System design
    MIXED          // Mixed types
}
```

---

## Callbacks

### SpeechRecognitionCallback

Callback for speech recognition events.

```kotlin
interface SpeechRecognitionCallback {
    fun onResult(text: String, isFinal: Boolean)
    fun onError(error: String)
}
```

**Example:**
```kotlin
object : SpeechRecognitionCallback {
    override fun onResult(text: String, isFinal: Boolean) {
        if (isFinal) {
            println("Final answer: $text")
        } else {
            println("Partial: $text")
        }
    }
    
    override fun onError(error: String) {
        println("Error: $error")
    }
}
```

---

## UI State Models

### VoiceInterviewUiState

UI state for the interview screen.

```kotlin
sealed class VoiceInterviewUiState {
    object Loading : VoiceInterviewUiState()
    
    data class TopicSelection(
        val topics: List<InterviewTopic>
    ) : VoiceInterviewUiState()
    
    data class ConfigureInterview(
        val topic: InterviewTopic
    ) : VoiceInterviewUiState()
    
    data class ActiveInterview(
        val interviewState: InterviewState
    ) : VoiceInterviewUiState()
    
    data class InterviewSummary(
        val summary: InterviewSummary
    ) : VoiceInterviewUiState()
    
    data class Error(
        val errorMessage: String
    ) : VoiceInterviewUiState()
}
```

---

## Extension Functions

### calculateQuestionCount()

Calculate optimal question count for interview.

```kotlin
fun calculateQuestionCount(
    difficultyLevel: DifficultyLevel,
    durationMinutes: Int
): Int
```

**Parameters:**
- `difficultyLevel`: Interview difficulty
- `durationMinutes`: Interview duration

**Returns:** Optimal question count (3-12)

**Example:**
```kotlin
val count = calculateQuestionCount(
    DifficultyLevel.INTERMEDIATE,
    15
)
println("Questions: $count") // Output: 6
```

---

## Constants

```kotlin
// Time per question (minutes)
const val BEGINNER_TIME_PER_QUESTION = 2.0
const val INTERMEDIATE_TIME_PER_QUESTION = 2.5
const val ADVANCED_TIME_PER_QUESTION = 3.5
const val EXPERT_TIME_PER_QUESTION = 4.5

// Question count bounds
const val MIN_QUESTIONS = 3
const val MAX_QUESTIONS = 12

// Timeouts
const val SPEECH_RECOGNITION_TIMEOUT = 60_000L // 60s
const val API_CONNECT_TIMEOUT = 30_000L        // 30s
const val API_READ_TIMEOUT = 60_000L           // 60s

// Auto-proceed delay
const val AUTO_PROCEED_DELAY = 3000L           // 3s
```

---

## Error Codes

Common error messages:

```kotlin
// Permission errors
"RECORD_AUDIO permission not granted"

// Speech recognition errors
"Speech recognition is not available on this device"
"Audio recording error"
"No speech input"
"Recognition service is busy"

// Network errors
"Network error"
"Network timeout"

// API errors
"Unauthorized" // Invalid API key
"Failed to fetch next question"
"Failed to evaluate response"

// Session errors
"No active session"
"Session already ended"
```

---

## Version

Current API Version: **1.0.0**

---

## See Also

- [README.md](./README.md) - Complete documentation
- [QUICKSTART.md](./QUICKSTART.md) - Quick start guide
- [Examples](../ui/src/main/java/com/prepstack/ui/screen/) - Implementation examples

---

**Last Updated:** 2024
