# Tech Interview Prep 🚀

A comprehensive Android application for technical interview preparation covering multiple domains: Android, Backend, Java, Kotlin, C++, OOPS, DSA, SQL, and HR.

## 📱 Features

- **Multi-Domain Coverage**: 9 technical domains with extensive question banks
- **Two Question Types**: MCQ and Theory questions with detailed explanations
- **Quiz Mode**: Random quiz generation from domains or specific topics
- **Bookmarks**: Save important questions for quick review
- **Ad-Supported**: Free app with non-intrusive ads (Banner, Interstitial, Rewarded)
- **Modern UI**: Clean, minimal design using Jetpack Compose and Material 3
- **Offline-First**: All content available locally (no internet required except for ads)

## 🏗️ Architecture

This project follows **Clean Architecture** principles with a multi-module structure:

```
├── app/           # Main application module
├── core/          # Common utilities and constants
├── domain/        # Business logic and domain models
├── data/          # Data sources and repositories
├── ui/            # Jetpack Compose screens and ViewModels
├── bookmarks/     # Bookmark feature with Room database
└── ads/           # AdMob integration (Android-only)
```

### Key Architectural Principles

- ✅ **Clean Architecture** with clear separation of concerns
- ✅ **MVVM Pattern** with StateFlow for reactive UI
- ✅ **Multi-Module** structure for scalability and testability
- ✅ **Dependency Inversion** - domain layer has no dependencies on outer layers
- ✅ **Single Responsibility** - each module has a clear purpose
- ✅ **Future-Ready** for Kotlin Multiplatform migration

For detailed architecture documentation, see [ARCHITECTURE.md](ARCHITECTURE.md)

## 🛠️ Tech Stack

### Core Technologies
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture**: Clean Architecture + MVVM
- **Concurrency**: Coroutines + Flow
- **Navigation**: Jetpack Navigation Compose

### Libraries
- **Coil**: Async image loading from URLs
- **Room**: Local database for bookmarks
- **Gson**: JSON parsing
- **Google AdMob**: Monetization
- **Material 3**: Modern Material Design components

### Build System
- Gradle with Kotlin DSL
- Version Catalog for dependency management
- KSP for annotation processing

## 📋 Prerequisites

- Android Studio Hedgehog (2023.1.1) or later
- JDK 11 or higher
- Android SDK with minimum API 24 (Android 7.0)
- Gradle 8.0+

## 🚀 Getting Started

### 1. Clone the Repository

```bash
git clone https://github.com/yourusername/tech-interview-prep.git
cd tech-interview-prep
```

### 2. Open in Android Studio

- Open Android Studio
- Select "Open an Existing Project"
- Navigate to the cloned directory
- Wait for Gradle sync to complete

### 3. Configure AdMob (Optional for testing)

The app uses test AdMob IDs by default. For production:

1. Create an AdMob account at [AdMob Console](https://admob.google.com)
2. Create a new app and ad units
3. Replace test IDs in:
   - `app/src/main/AndroidManifest.xml` - App ID
   - `ads/src/main/java/com/prepstack/ads/AdManager.kt` - Ad unit IDs

### 4. Build and Run

```bash
./gradlew :app:assembleDebug
```

Or use Android Studio's run button (▶️)

## 📂 Project Structure

```
TechInterviewPrep/
│
├── app/
│   ├── src/main/
│   │   ├── assets/
│   │   │   └── interview_data.json     # All questions data
│   │   ├── java/com/prepstack/techinterviewprep/
│   │   │   ├── MainActivity.kt
│   │   │   └── TechInterviewPrepApp.kt # Application class
│   │   └── AndroidManifest.xml
│   └── build.gradle.kts
│
├── core/
│   └── src/main/java/com/prepstack/core/
│       └── util/
│           ├── Constants.kt
│           ├── Resource.kt
│           └── JsonReader.kt
│
├── domain/
│   └── src/main/java/com/prepstack/domain/
│       ├── model/                      # Domain entities
│       │   ├── Domain.kt
│       │   ├── Topic.kt
│       │   ├── Question.kt
│       │   └── QuizResult.kt
│       ├── repository/
│       │   └── InterviewRepository.kt  # Repository interface
│       └── usecase/                    # Business logic
│           ├── GetDomainsUseCase.kt
│           ├── GetTopicsUseCase.kt
│           ├── GetQuestionsUseCase.kt
│           └── GetRandomQuizQuestionsUseCase.kt
│
├── data/
│   └── src/main/java/com/prepstack/data/
│       ├── dto/                        # Data Transfer Objects
│       │   └── InterviewDataDto.kt
│       ├── mapper/                     # DTO to Domain mappers
│       │   └── DomainMapper.kt
│       ├── source/
│       │   └── LocalDataSource.kt     # JSON data source
│       └── repository/
│           └── InterviewRepositoryImpl.kt
│
├── bookmarks/
│   └── src/main/java/com/prepstack/bookmarks/
│       ├── data/
│       │   ├── BookmarkEntity.kt
│       │   ├── BookmarkDao.kt
│       │   └── BookmarkDatabase.kt
│       └── repository/
│           └── BookmarkRepository.kt
│
├── ads/
│   └── src/main/java/com/prepstack/ads/
│       ├── AdManager.kt               # Ad management logic
│       └── BannerAdView.kt           # Composable banner ad
│
├── ui/
│   └── src/main/java/com/prepstack/ui/
│       ├── screen/                    # Compose screens
│       │   ├── SplashScreen.kt
│       │   ├── DomainScreen.kt
│       │   └── TopicScreen.kt
│       ├── viewmodel/                 # ViewModels
│       │   ├── DomainViewModel.kt
│       │   └── TopicViewModel.kt
│       └── navigation/
│           ├── Screen.kt              # Navigation routes
│           └── NavGraph.kt            # Navigation graph
│
├── gradle/
│   └── libs.versions.toml            # Version catalog
│
├── ARCHITECTURE.md                    # Detailed architecture docs
└── README.md                         # This file
```

## 🎨 UI Screens

### Implemented Screens

1. **Splash Screen** - App initialization and branding
2. **Domain Screen** - Grid view of all interview domains
3. **Topic Screen** - List of topics within a domain

### Screens to Implement (Following same pattern)

4. **Question List Screen** - List of all questions in a topic
5. **Question Detail Screen** - Detailed view with explanation
6. **Quiz Screen** - Interactive quiz with timer
7. **Result Screen** - Quiz results with statistics
8. **Bookmark Screen** - Saved questions

## 📝 Adding New Content

### To Add New Questions

Edit `app/src/main/assets/interview_data.json`:

```json
{
  "domains": [
    {
      "id": "new_domain",
      "name": "New Domain",
      "description": "Description",
      "iconUrl": "https://img.icons8.com/color/96/icon.png",
      "topics": [
        {
          "id": "topic_id",
          "name": "Topic Name",
          "description": "Topic description",
          "iconUrl": "https://img.icons8.com/color/96/icon.png",
          "questions": [
            {
              "id": "unique_question_id",
              "questionText": "Your question here?",
              "type": "MCQ",
              "options": ["Option 1", "Option 2", "Option 3", "Option 4"],
              "correctAnswer": "Option 1",
              "explanation": "Detailed explanation",
              "difficulty": "MEDIUM"
            }
          ]
        }
      ]
    }
  ]
}
```

### Question Types

- **MCQ**: Multiple choice questions with 4 options
- **THEORY**: Open-ended questions with text answers

### Difficulty Levels

- **EASY**: Fundamental concepts
- **MEDIUM**: Intermediate topics
- **HARD**: Advanced concepts

## 🔌 Dependency Injection

Currently using **manual DI** in `TechInterviewPrepApp.kt` for simplicity.

### Migration to Hilt (Recommended for Production)

```kotlin
// Add to app/build.gradle.kts
plugins {
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
}

// Application class
@HiltAndroidApp
class TechInterviewPrepApp : Application()

// ViewModels
@HiltViewModel
class DomainViewModel @Inject constructor(
    private val getDomainsUseCase: GetDomainsUseCase
) : ViewModel()
```

## 🧪 Testing

### Unit Tests
Located in each module's `src/test/` directory:

```bash
./gradlew test
```

### Instrumentation Tests
Located in `src/androidTest/`:

```bash
./gradlew connectedAndroidTest
```

### Test Coverage

- [ ] Domain models and use cases
- [ ] Repository implementations
- [ ] ViewModels
- [ ] Compose UI tests

## 🔐 ProGuard Rules

For release builds, ensure proper ProGuard rules for:

```proguard
# Gson
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.prepstack.data.dto.** { *; }

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
```

## 📱 APK Size Optimization

- Use R8 code shrinking
- Enable resource shrinking
- Optimize images (use WebP)
- Split APKs by ABI if needed

## 🌐 Internet Permissions

The app requires internet permission **only for ads**. All content works offline.

## 🎯 Roadmap

### Phase 1 (Current)
- ✅ Core architecture setup
- ✅ Domain, Topic, and basic screens
- ✅ JSON data loading
- ✅ Bookmark functionality
- ✅ Ad integration

### Phase 2 (Next)
- [ ] Complete remaining screens (Quiz, Result, Question Detail)
- [ ] Search functionality
- [ ] Progress tracking
- [ ] Analytics integration

### Phase 3 (Future)
- [ ] Kotlin Multiplatform migration
- [ ] iOS app
- [ ] Backend sync (optional)
- [ ] User accounts
- [ ] Leaderboards

## 🤝 Contributing

Contributions are welcome! Please:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit changes (`git commit -m 'Add AmazingFeature'`)
4. Push to branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👨‍💻 Author

**Your Name**
- GitHub: [@yourusername](https://github.com/yourusername)

## 🙏 Acknowledgments

- Icons from [Icons8](https://icons8.com)
- Material Design guidelines
- Android Jetpack libraries
- Kotlin Coroutines team

---

**Made with ❤️ using Jetpack Compose**
