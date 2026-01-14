# Tech Interview Prep - Architecture Documentation

## Overview
This Android application follows **Clean Architecture** principles with a multi-module structure for scalability, testability, and maintainability. The architecture is designed to be future-ready for Kotlin Multiplatform (KMP) migration.

## Module Structure

### Module Dependency Diagram

```
┌─────────────────────────────────────────────────────────┐
│                         app                              │
│  (Main Application Module - Integration Layer)          │
│  - MainActivity                                          │
│  - Application class                                     │
│  - Dependency injection setup                            │
└─────────────────┬───────────────────────────────────────┘
                  │
                  ├────► ui (Presentation Layer)
                  │      - Compose screens
                  │      - ViewModels
                  │      - Navigation
                  │      - UI state management
                  │      
                  ├────► domain (Business Logic Layer)
                  │      - Domain models (Domain, Topic, Question)
                  │      - Repository interfaces
                  │      - Use cases
                  │      
                  ├────► data (Data Layer)
                  │      - Repository implementations
                  │      - Data sources (JSON)
                  │      - DTOs and mappers
                  │      
                  ├────► bookmarks (Feature Module)
                  │      - Room database
                  │      - Bookmark repository
                  │      - Bookmark entities
                  │      
                  ├────► ads (Feature Module - Android only)
                  │      - AdMob integration
                  │      - Ad manager
                  │      - Banner/Interstitial/Rewarded ads
                  │      
                  └────► core (Utility Module)
                         - Constants
                         - Resource wrapper
                         - JSON reader
                         - Common utilities

Dependency Flow:
app → ui → domain ← data → core
      ↓      ↓       ↓
  bookmarks  ↓    core
             ↓
          core
```

## Layer Responsibilities

### 1. Core Module
**Purpose**: Shared utilities and common code

**Contains**:
- `Constants.kt` - App-wide constants (icon URLs, ad settings)
- `Resource.kt` - Wrapper for data states (Success, Error, Loading)
- `JsonReader.kt` - Utility for reading JSON from assets

**Dependencies**: None (pure Kotlin/Android utilities)

### 2. Domain Module
**Purpose**: Business logic and domain models

**Contains**:
- **Models**: `Domain`, `Topic`, `Question`, `QuestionType`, `DifficultyLevel`, `QuizResult`
- **Repository Interfaces**: `InterviewRepository`
- **Use Cases**:
  - `GetDomainsUseCase`
  - `GetTopicsUseCase`
  - `GetQuestionsUseCase`
  - `GetRandomQuizQuestionsUseCase`

**Dependencies**: `core`

**Key Principles**:
- No Android dependencies (framework-independent)
- Pure business logic
- Repository interfaces (dependency inversion)

### 3. Data Module
**Purpose**: Data management and external data sources

**Contains**:
- **DTOs**: `InterviewDataDto`, `DomainDto`, `TopicDto`, `QuestionDto`
- **Mappers**: DTO → Domain model converters
- **Data Sources**: `LocalDataSource` (JSON-based)
- **Repository Implementation**: `InterviewRepositoryImpl`

**Dependencies**: `core`, `domain`

**Features**:
- Single JSON file loading at startup
- In-memory caching for performance
- Gson for JSON parsing

### 4. Bookmarks Module
**Purpose**: Local bookmark storage

**Contains**:
- **Room Database**: `BookmarkDatabase`
- **DAO**: `BookmarkDao`
- **Entity**: `BookmarkEntity`
- **Repository**: `BookmarkRepository`

**Dependencies**: `core`, `domain`

**Features**:
- Persistent bookmark storage
- Flow-based reactive queries
- Toggle bookmark functionality

### 5. Ads Module (Android-only)
**Purpose**: Advertisement integration

**Contains**:
- `AdManager` - Centralized ad management
- `BannerAdView` - Composable banner ad
- Ad loading and display logic

**Dependencies**: None (can be used standalone)

**Features**:
- Banner ads (list screens)
- Interstitial ads (interaction-based)
- Rewarded ads (unlock explanations)
- Safe abstraction from UI logic

### 6. UI Module
**Purpose**: User interface and presentation

**Contains**:
- **Screens**:
  - `SplashScreen`
  - `DomainScreen` (Grid layout)
  - `TopicScreen` (List layout)
  - More screens (QuestionList, QuestionDetail, Quiz, Result, Bookmark)
  
- **ViewModels**:
  - `DomainViewModel`
  - `TopicViewModel`
  - `QuestionListViewModel`
  
- **Navigation**:
  - `Screen` (sealed class for routes)
  - `NavGraph` (Compose Navigation setup)

**Dependencies**: `core`, `domain`, `bookmarks`, `ads`

**Features**:
- Jetpack Compose UI
- StateFlow for reactive state
- MVVM pattern
- Coil for async image loading

### 7. App Module
**Purpose**: Application entry point and integration

**Contains**:
- `MainActivity` - Entry activity
- `TechInterviewPrepApp` - Application class
- Manual dependency injection (can be replaced with Hilt)
- Asset files (JSON data)

**Dependencies**: All modules

## Data Flow

### Loading Data Flow
```
Startup → TechInterviewPrepApp.onCreate()
       → InterviewRepository.loadData()
       → LocalDataSource reads JSON from assets
       → Parse with Gson
       → Map DTOs to Domain models
       → Cache in memory
```

### Screen Data Flow (Example: Domain Screen)
```
DomainScreen (Compose)
    ↓
DomainViewModel.loadDomains()
    ↓
GetDomainsUseCase()
    ↓
InterviewRepository.getDomains()
    ↓
LocalDataSource.getDomains() (from cache)
    ↓
Flow<Resource<List<Domain>>>
    ↓
StateFlow in ViewModel
    ↓
UI recomposes with new state
```

## State Management

### UI State Pattern
Each screen has its own sealed UI state class:

```kotlin
sealed class DomainUiState {
    data object Loading : DomainUiState()
    data class Success(val domains: List<Domain>) : DomainUiState()
    data class Error(val message: String) : DomainUiState()
}
```

**Benefits**:
- Type-safe state handling
- Clear state transitions
- Easy to test
- Predictable UI updates

## JSON Data Structure

```json
{
  "domains": [
    {
      "id": "android",
      "name": "Android",
      "iconUrl": "https://...",
      "topics": [
        {
          "id": "android_basics",
          "name": "Android Basics",
          "questions": [
            {
              "id": "q1",
              "type": "MCQ" | "THEORY",
              "questionText": "...",
              "options": [...], // for MCQ
              "correctAnswer": "...",
              "explanation": "...",
              "difficulty": "EASY" | "MEDIUM" | "HARD"
            }
          ]
        }
      ]
    }
  ]
}
```

## Navigation Architecture

### Compose Navigation
Uses Jetpack Navigation Component with type-safe routes:

```kotlin
sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Domain : Screen("domain")
    object Topic : Screen("topic/{domainId}") {
        fun createRoute(domainId: String) = "topic/$domainId"
    }
}
```

### Navigation Flow
```
SplashScreen
    ↓
DomainScreen (Grid of domains)
    ↓
TopicScreen (List of topics)
    ↓
QuestionListScreen OR QuizScreen
    ↓
QuestionDetailScreen OR ResultScreen
```

## Ad Integration Strategy

### Ad Placement
- **Banner Ads**: Bottom of list screens (Domain, Topic, QuestionList)
- **Interstitial Ads**: Every 5 interactions (tracked by AdManager)
- **Rewarded Ads**: Unlock question explanations

### Ad Isolation
Ads are completely isolated in the `ads` module:
- Domain and Data layers have no ad knowledge
- UI module imports ads module only for display
- Easy to remove/replace ad provider

## Testing Strategy

### Unit Tests
- **Domain Layer**: Test use cases and business logic
- **Data Layer**: Test repository implementations and mappers
- **ViewModels**: Test state transformations

### Integration Tests
- Test data flow from repository to ViewModel
- Test navigation flows

### UI Tests
- Compose UI tests for screens
- Navigation tests

## Future KMP Migration Path

### Current Android-Only Modules
- `app` (Android MainActivity)
- `ui` (Jetpack Compose - Android)
- `ads` (AdMob - Android)
- `bookmarks` (Room - Android)

### KMP-Ready Modules
- `core` - Can be common module
- `domain` - Pure Kotlin, can be common
- `data` - Minor changes for platform-specific IO

### Migration Strategy
1. Move `core` and `domain` to common source sets
2. Create platform-specific implementations for:
   - Data sources (AndroidMain, iOSMain)
   - Local storage (Room → SQLDelight)
3. UI remains platform-specific:
   - Android: Jetpack Compose
   - iOS: SwiftUI or Compose Multiplatform

## Technologies Used

### Core
- **Language**: Kotlin
- **Architecture**: Clean Architecture + MVVM
- **Concurrency**: Kotlin Coroutines + Flow
- **Navigation**: Jetpack Compose Navigation

### UI
- **Framework**: Jetpack Compose
- **Image Loading**: Coil
- **Material Design**: Material 3

### Data
- **JSON Parsing**: Gson
- **Local Storage**: Room Database
- **Reactive**: StateFlow

### Ads
- **Provider**: Google AdMob
- **Ad Types**: Banner, Interstitial, Rewarded

## Build Configuration

### Min SDK: 24 (Android 7.0)
### Target SDK: 36 (Android 15)
### Compile SDK: 36
### JVM Target: 11

## Production Checklist

Before production release:

1. **Replace Test Ad IDs** with real AdMob IDs in:
   - `AdManager.kt`
   - `AndroidManifest.xml`

2. **Add ProGuard Rules** for:
   - Gson
   - Room
   - AdMob

3. **Implement Dependency Injection**:
   - Replace manual DI with Hilt
   - Add ViewModel factories

4. **Error Handling**:
   - Add proper error logging
   - Implement crash analytics (Firebase Crashlytics)

5. **Analytics**:
   - Add Firebase Analytics
   - Track user interactions

6. **Testing**:
   - Write comprehensive unit tests
   - Add UI tests
   - Perform manual QA

7. **Performance**:
   - Optimize JSON loading
   - Add LazyColumn item keys
   - Profile memory usage

## Contact & Support

For questions or contributions, refer to the project repository.
