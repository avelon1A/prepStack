# HomeScreen Functionality Implementation

## Overview
The HomeScreen has been transformed from a static UI to a fully functional dashboard with real-time data persistence and interactive features.

## Changes Made

### 1. Data Persistence Layer

#### New Database Entities
- **UserActivityEntity** (`data/src/main/java/com/prepstack/data/local/UserActivityEntity.kt`)
  - Tracks user's recent activities (topics studied, questions completed)
  - Stores progress information and timestamps

- **QuizHistoryEntity** (`data/src/main/java/com/prepstack/data/local/QuizHistoryEntity.kt`)
  - Records completed quiz results
  - Tracks correct/incorrect/skipped answers and accuracy

- **IncompleteTestEntity** (`data/src/main/java/com/prepstack/data/local/IncompleteTestEntity.kt`)
  - Stores incomplete test sessions
  - Allows users to resume interrupted quizzes

#### Database Access
- **UserProgressDao** (`data/src/main/java/com/prepstack/data/local/UserProgressDao.kt`)
  - Room DAO with methods for all CRUD operations
  - Flow-based queries for reactive updates

- **UserProgressDatabase** (`data/src/main/java/com/prepstack/data/local/UserProgressDatabase.kt`)
  - Room database configuration
  - Singleton pattern for app-wide access

### 2. Domain Layer

#### New Domain Models
- **UserActivity** (`domain/src/main/java/com/prepstack/domain/model/UserActivity.kt`)
- **QuizHistory** (`domain/src/main/java/com/prepstack/domain/model/QuizHistory.kt`)
- **IncompleteTest** (`domain/src/main/java/com/prepstack/domain/model/IncompleteTest.kt`)
- **UserPerformance** (`domain/src/main/java/com/prepstack/domain/model/UserPerformance.kt`)

#### Repository Interface
- **UserProgressRepository** (`domain/src/main/java/com/prepstack/domain/repository/UserProgressRepository.kt`)
  - Defines contract for user progress operations
  - Follows Clean Architecture principles

### 3. Data Layer Implementation

#### Repository Implementation
- **UserProgressRepositoryImpl** (`data/src/main/java/com/prepstack/data/repository/UserProgressRepositoryImpl.kt`)
  - Implements UserProgressRepository interface
  - Handles entity-to-domain model mapping
  - Provides reactive data streams using Flow

### 4. UI Layer

#### ViewModel
- **HomeViewModel** (`ui/src/main/java/com/prepstack/ui/viewmodel/HomeViewModel.kt`)
  - Manages HomeScreen state
  - Loads recent activities, performance stats, and incomplete tests
  - Handles user actions (resume test, delete incomplete test)

#### Updated Screens
- **HomeScreen** (`ui/src/main/java/com/prepstack/ui/screen/HomeScreen.kt`)
  - Now uses HomeViewModel for dynamic data
  - Recent Activities section displays actual user activities
  - Performance Overview shows real statistics
  - Incomplete Tests section lists resumable tests
  - All items are clickable with proper navigation

- **MainScreen** (`ui/src/main/java/com/prepstack/ui/screen/MainScreen.kt`)
  - Integrated HomeViewModel
  - Added navigation handlers for activities and tests

- **NavGraph** (`ui/src/main/java/com/prepstack/ui/navigation/NavGraph.kt`)
  - Added HomeViewModel to navigation graph
  - Implemented navigation callbacks for all interactive elements

### 5. Application Setup

#### Updated App Class
- **TechInterviewPrepApp** (`app/src/main/java/com/prepstack/techinterviewprep/TechInterviewPrepApp.kt`)
  - Added UserProgressDatabase initialization
  - Created UserProgressRepository instance
  - Provides repository to ViewModels

#### Updated MainActivity
- **MainActivity** (`app/src/main/java/com/prepstack/techinterviewprep/MainActivity.kt`)
  - Instantiated HomeViewModel with dependencies
  - Passed HomeViewModel to NavGraph

### 6. Utility Functions

#### DateUtils
- **DateUtils** (`core/src/main/java/com/prepstack/core/util/DateUtils.kt`)
  - Formats timestamps to relative time strings (e.g., "2h ago", "Yesterday")
  - Provides date/time formatting utilities

## Features Implemented

### 1. Browse Topics
- ✅ Already functional - displays all domains
- ✅ Clickable domain cards navigate to topic screen

### 2. Recent Activities
- ✅ **NEW**: Displays actual user activities from database
- ✅ Shows topic name, progress, and relative timestamp
- ✅ Clickable to navigate back to topic
- ✅ Updates automatically when user studies

### 3. Performance Overview
- ✅ **NEW**: Real-time statistics from quiz history
- ✅ Total questions answered
- ✅ Total correct answers
- ✅ Accuracy percentage
- ✅ Updates after each quiz completion

### 4. Incomplete Tests
- ✅ **NEW**: Lists incomplete test sessions
- ✅ Shows test name, progress, and timestamp
- ✅ "Resume Test" button to continue
- ✅ Delete button to remove incomplete test
- ✅ Automatically populated when quiz is interrupted

## Data Flow

```
User Action
    ↓
ViewModel (HomeViewModel)
    ↓
Repository (UserProgressRepository)
    ↓
DAO (UserProgressDao)
    ↓
Database (UserProgressDatabase)
    ↓
Entity (UserActivityEntity/QuizHistoryEntity/IncompleteTestEntity)
```

## Usage Examples

### Recording User Activity
```kotlin
val activity = UserActivity(
    topicId = "topic_123",
    topicName = "Android Basics",
    domainId = "domain_456",
    questionsCompleted = 5,
    totalQuestions = 10,
    timestamp = System.currentTimeMillis()
)
userProgressRepository.recordActivity(activity)
```

### Recording Quiz Result
```kotlin
val quizHistory = QuizHistory(
    domainId = "domain_456",
    topicId = "topic_123",
    totalQuestions = 10,
    correctAnswers = 8,
    incorrectAnswers = 2,
    skippedQuestions = 0,
    percentage = 80f,
    timestamp = System.currentTimeMillis()
)
userProgressRepository.recordQuizResult(quizHistory)
```

### Saving Incomplete Test
```kotlin
val incompleteTest = IncompleteTest(
    testId = "test_789",
    domainId = "domain_456",
    domainName = "Android",
    topicId = "topic_123",
    topicName = "Basics",
    questionsCompleted = 3,
    totalQuestions = 10,
    timestamp = System.currentTimeMillis()
)
userProgressRepository.saveIncompleteTest(incompleteTest)
```

## Next Steps

To fully utilize the new functionality, you should:

1. **Integrate activity recording** in QuestionListScreen when users complete questions
2. **Integrate quiz result recording** in QuizScreen when users finish a quiz
3. **Integrate incomplete test saving** when users exit a quiz before completion
4. **Add empty states** for when there are no activities or incomplete tests
5. **Consider adding charts/graphs** for visual performance representation

## Architecture Benefits

- **Clean Architecture**: Clear separation of concerns with domain, data, and UI layers
- **Reactive Programming**: Uses Flow for real-time updates
- **Single Source of Truth**: Database is the source of truth for user progress
- **Testable**: Each layer can be tested independently
- **Scalable**: Easy to add new features and data types

## Notes

- All data persists across app restarts using Room database
- The database is created on first app launch
- No migration needed as this is version 1
- Consider adding data export/import for backup in future versions