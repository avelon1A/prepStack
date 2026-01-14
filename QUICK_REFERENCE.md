# Quick Reference Card

## 🚀 Quick Commands

```bash
# Build project
./gradlew build

# Run app
./gradlew :app:installDebug

# Run tests
./gradlew test

# Clean build
./gradlew clean build
```

## 📦 Module Overview

| Module | Purpose | Dependencies |
|--------|---------|--------------|
| **core** | Utilities, Constants | None |
| **domain** | Business Logic, Models | core |
| **data** | Data Sources, Repositories | core, domain |
| **bookmarks** | Room Database | core, domain |
| **ads** | AdMob Integration | None |
| **ui** | Compose Screens, ViewModels | core, domain, bookmarks, ads |
| **app** | Entry Point, DI | All |

## 📁 Key Files

```
app/src/main/
├── assets/interview_data.json           # Question database
├── java/.../MainActivity.kt             # App entry
├── java/.../TechInterviewPrepApp.kt     # DI container
└── AndroidManifest.xml                  # App config

core/src/main/java/.../util/
├── Constants.kt                         # Icon URLs, configs
├── Resource.kt                          # State wrapper
└── JsonReader.kt                        # Asset reader

domain/src/main/java/.../
├── model/                               # Entities
├── repository/InterviewRepository.kt    # Contract
└── usecase/                             # Business logic

data/src/main/java/.../
├── dto/InterviewDataDto.kt             # JSON structure
├── mapper/DomainMapper.kt              # DTO → Domain
├── source/LocalDataSource.kt           # JSON loader
└── repository/InterviewRepositoryImpl.kt

ui/src/main/java/.../
├── screen/                             # Compose screens
├── viewmodel/                          # MVVM ViewModels
└── navigation/                         # Nav setup
```

## 🎨 Adding New Screen (Template)

### 1. Create ViewModel
```kotlin
class MyViewModel(
    private val useCase: MyUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<MyUiState>(MyUiState.Loading)
    val uiState: StateFlow<MyUiState> = _uiState.asStateFlow()
    
    fun loadData() {
        viewModelScope.launch {
            useCase().collect { resource ->
                _uiState.value = when (resource) {
                    is Resource.Loading -> MyUiState.Loading
                    is Resource.Success -> MyUiState.Success(resource.data!!)
                    is Resource.Error -> MyUiState.Error(resource.message!!)
                }
            }
        }
    }
}

sealed class MyUiState {
    data object Loading : MyUiState()
    data class Success(val data: MyData) : MyUiState()
    data class Error(val message: String) : MyUiState()
}
```

### 2. Create Screen
```kotlin
@Composable
fun MyScreen(
    viewModel: MyViewModel,
    onNavigate: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = { TopAppBar(title = { Text("My Screen") }) }
    ) { padding ->
        when (uiState) {
            is MyUiState.Loading -> LoadingView()
            is MyUiState.Success -> SuccessView(data)
            is MyUiState.Error -> ErrorView(message)
        }
    }
}
```

### 3. Add to Navigation
```kotlin
// In Screen.kt
data object MyScreen : Screen("my_screen")

// In NavGraph.kt
composable(route = Screen.MyScreen.route) {
    MyScreen(viewModel = myViewModel, onNavigate = { ... })
}
```

## 📝 Adding Questions to JSON

```json
{
  "id": "unique_id",
  "questionText": "Your question?",
  "type": "MCQ",                    // or "THEORY"
  "options": ["A", "B", "C", "D"],  // only for MCQ
  "correctAnswer": "A",
  "explanation": "Detailed explanation...",
  "difficulty": "MEDIUM"            // EASY, MEDIUM, HARD
}
```

## 🎯 Icon URLs (Icons8)

```kotlin
// Use in Constants.kt
const val ICON_URL = "https://img.icons8.com/color/96/your-icon.png"
```

**Popular icons:**
- Android: `android-os.png`
- Kotlin: `kotlin.png`
- Java: `java-coffee-cup-logo.png`
- C++: `c-plus-plus-logo.png`
- Database: `database.png`
- Server: `server.png`

## 🔧 Common Patterns

### StateFlow Collection
```kotlin
val uiState by viewModel.uiState.collectAsState()
```

### LaunchedEffect for Side Effects
```kotlin
LaunchedEffect(key) {
    viewModel.loadData()
}
```

### Navigation with Arguments
```kotlin
navController.navigate("topic/${domainId}")
```

### Bookmark Toggle
```kotlin
viewModel.toggleBookmark(questionId, topicId, domainId)
```

### Ad Tracking
```kotlin
adManager.trackInteraction(activity, threshold = 5) {
    // Continue after ad
}
```

## 🧪 Testing Checklist

```kotlin
// ViewModel Test
@Test
fun `test loads data successfully`() = runTest {
    // Given, When, Then
}

// Compose Test
@Test
fun `screen displays loading state`() {
    composeTestRule.setContent { MyScreen() }
    composeTestRule.onNode(hasProgressBarIndicator()).assertExists()
}
```

## 🐛 Debugging Tips

### Check Data Loading
```kotlin
Log.d("TAG", "Domains loaded: ${domains.size}")
```

### Verify State Updates
```kotlin
viewModel.uiState.value.let { Log.d("TAG", "State: $it") }
```

### Test JSON Parsing
```kotlin
val json = JsonReader.readJsonFromAssets(context, Constants.JSON_FILE_NAME)
Log.d("TAG", "JSON: ${json?.take(100)}")
```

## ⚡ Performance Tips

1. **Use keys in LazyColumn/Grid**
```kotlin
items(items, key = { it.id }) { item -> ... }
```

2. **Avoid recomposition**
```kotlin
val derivedState by remember { derivedStateOf { ... } }
```

3. **Use `remember` for expensive operations**
```kotlin
val filteredList = remember(searchQuery) { 
    list.filter { it.contains(searchQuery) }
}
```

## 🔐 Production Checklist

### Before Release
- [ ] Replace test AdMob IDs
- [ ] Add ProGuard rules
- [ ] Enable R8 shrinking
- [ ] Test on multiple devices
- [ ] Add more questions (100+ per domain)
- [ ] Implement error logging
- [ ] Add analytics
- [ ] Test offline functionality
- [ ] Optimize APK size
- [ ] Add app icon and splash

### ProGuard Rules
```proguard
# Gson
-keepattributes Signature
-keep class com.prepstack.data.dto.** { *; }

# Room
-keep class * extends androidx.room.RoomDatabase
```

## 📚 Documentation Files

- **README.md** - Getting started guide
- **ARCHITECTURE.md** - Detailed architecture
- **IMPLEMENTATION_GUIDE.md** - Code examples
- **PROJECT_SUMMARY.md** - Project overview
- **QUICK_REFERENCE.md** - This file

## 🆘 Common Issues

### Gradle Sync Failed
```bash
./gradlew clean
rm -rf .gradle
./gradlew build
```

### Module Not Found
Check `settings.gradle.kts` includes all modules:
```kotlin
include(":app", ":core", ":domain", ":data", ":ui", ":bookmarks", ":ads")
```

### JSON Not Loading
Verify file is in `app/src/main/assets/interview_data.json`

### Navigation Not Working
Ensure routes match in `Screen.kt` and `NavGraph.kt`

### StateFlow Not Updating UI
Use `collectAsState()` in Composable:
```kotlin
val state by viewModel.state.collectAsState()
```

## 🔗 Useful Links

- [Jetpack Compose Docs](https://developer.android.com/jetpack/compose)
- [Clean Architecture Guide](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)
- [Material 3 Guidelines](https://m3.material.io/)
- [AdMob Integration](https://developers.google.com/admob/android/quick-start)
- [Room Database](https://developer.android.com/training/data-storage/room)
- [Coil Image Loading](https://coil-kt.github.io/coil/)

## 💡 Pro Tips

1. **Use sealed classes** for exhaustive when expressions
2. **Prefer StateFlow** over LiveData in Compose
3. **Keep ViewModels pure** - no Android dependencies
4. **Extract composables** for reusability
5. **Use remember** to avoid unnecessary recompositions
6. **Test business logic** in domain layer
7. **Keep UI dumb** - logic in ViewModels
8. **Document complex logic** with comments

---

**Version**: 1.0  
**Last Updated**: 2025-01-29  
**Author**: Tech Interview Prep Team
