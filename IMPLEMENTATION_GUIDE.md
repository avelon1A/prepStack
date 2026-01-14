# Implementation Guide - Code Examples

This document provides code samples and implementation patterns for key components of the Tech Interview Prep application.

## Table of Contents
1. [Sample ViewModel Implementation](#sample-viewmodel-implementation)
2. [Sample JSON Loading](#sample-json-loading)
3. [Sample Jetpack Compose Screen](#sample-jetpack-compose-screen)
4. [Navigation Setup](#navigation-setup)
5. [AdMob Integration Example](#admob-integration-example)
6. [Adding New Use Cases](#adding-new-use-cases)
7. [Testing Examples](#testing-examples)

---

## Sample ViewModel Implementation

### DomainViewModel.kt

This demonstrates the MVVM pattern with StateFlow for reactive UI updates:

```kotlin
package com.prepstack.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prepstack.core.util.Resource
import com.prepstack.domain.model.Domain
import com.prepstack.domain.usecase.GetDomainsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DomainViewModel(
    private val getDomainsUseCase: GetDomainsUseCase
) : ViewModel() {
    
    // Private mutable state - only ViewModel can modify
    private val _uiState = MutableStateFlow<DomainUiState>(DomainUiState.Loading)
    
    // Public immutable state - UI observes this
    val uiState: StateFlow<DomainUiState> = _uiState.asStateFlow()
    
    init {
        loadDomains()
    }
    
    fun loadDomains() {
        viewModelScope.launch {
            // Use case returns Flow<Resource<T>>
            getDomainsUseCase().collect { resource ->
                _uiState.value = when (resource) {
                    is Resource.Loading -> DomainUiState.Loading
                    is Resource.Success -> {
                        resource.data?.let {
                            DomainUiState.Success(it)
                        } ?: DomainUiState.Error("No data available")
                    }
                    is Resource.Error -> DomainUiState.Error(
                        resource.message ?: "An unexpected error occurred"
                    )
                }
            }
        }
    }
}

// Sealed class for type-safe UI states
sealed class DomainUiState {
    data object Loading : DomainUiState()
    data class Success(val domains: List<Domain>) : DomainUiState()
    data class Error(val message: String) : DomainUiState()
}
```

**Key Patterns:**
- ✅ Single source of truth with StateFlow
- ✅ Sealed classes for exhaustive state handling
- ✅ Separation of mutable and immutable state
- ✅ Coroutine scope tied to ViewModel lifecycle
- ✅ Resource wrapper for data states

---

## Sample JSON Loading

### LocalDataSource.kt - JSON Loading and Caching

```kotlin
class LocalDataSource(private val context: Context) {
    
    private val gson = Gson()
    
    // In-memory cache for performance
    private var domains: List<Domain> = emptyList()
    private var topics: List<Topic> = emptyList()
    private var questions: List<Question> = emptyList()
    
    private var isDataLoaded = false
    
    suspend fun loadData(): Result<Unit> {
        return try {
            // Avoid reloading if already cached
            if (isDataLoaded) {
                return Result.success(Unit)
            }
            
            // Read JSON from assets using utility
            val jsonString = JsonReader.readJsonFromAssets(
                context, 
                Constants.JSON_FILE_NAME
            ) ?: return Result.failure(Exception("Failed to read JSON"))
            
            // Parse JSON with Gson
            val dataDto = gson.fromJson(jsonString, InterviewDataDto::class.java)
            
            // Transform DTOs to Domain models
            val domainsList = mutableListOf<Domain>()
            val topicsList = mutableListOf<Topic>()
            val questionsList = mutableListOf<Question>()
            
            dataDto.domains.forEach { domainDto ->
                // Map domain
                domainsList.add(domainDto.toDomain())
                
                domainDto.topics.forEach { topicDto ->
                    // Map topic with parent domain ID
                    topicsList.add(topicDto.toTopic(domainDto.id))
                    
                    topicDto.questions.forEach { questionDto ->
                        // Map question with parent IDs
                        questionsList.add(
                            questionDto.toQuestion(topicDto.id, domainDto.id)
                        )
                    }
                }
            }
            
            // Cache in memory
            domains = domainsList
            topics = topicsList
            questions = questionsList
            isDataLoaded = true
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Fast in-memory retrieval
    fun getDomains(): List<Domain> = domains
    
    fun getTopicsByDomain(domainId: String): List<Topic> {
        return topics.filter { it.domainId == domainId }
    }
}
```

**Performance Optimizations:**
- ✅ Load once at app startup
- ✅ Cache all data in memory
- ✅ Filter operations on cached data (O(n) vs file I/O)
- ✅ Suspend function for non-blocking load

---

## Sample Jetpack Compose Screen

### DomainScreen.kt - Full Implementation

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DomainScreen(
    viewModel: DomainViewModel,
    onDomainClick: (String) -> Unit,
    onBookmarkClick: () -> Unit
) {
    // Collect state as Compose State
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tech Interview Prep") },
                actions = {
                    IconButton(onClick = onBookmarkClick) {
                        Icon(
                            imageVector = Icons.Default.Bookmark,
                            contentDescription = "Bookmarks"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            // Pattern: When expression for exhaustive state handling
            when (uiState) {
                is DomainUiState.Loading -> {
                    LoadingView()
                }
                
                is DomainUiState.Success -> {
                    val domains = (uiState as DomainUiState.Success).domains
                    
                    // Grid layout for domains
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(domains) { domain ->
                            DomainCard(
                                domain = domain,
                                onClick = { onDomainClick(domain.id) }
                            )
                        }
                    }
                    
                    // Banner ad at bottom
                    BannerAdView()
                }
                
                is DomainUiState.Error -> {
                    ErrorView(message = (uiState as DomainUiState.Error).message)
                }
            }
        }
    }
}

@Composable
fun DomainCard(domain: Domain, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Coil AsyncImage for URL loading
            AsyncImage(
                model = domain.iconUrl,
                contentDescription = domain.name,
                modifier = Modifier.size(64.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = domain.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = "${domain.topicCount} topics",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
```

**Compose Best Practices:**
- ✅ State hoisting (callbacks for events)
- ✅ Reusable components (DomainCard)
- ✅ Material 3 theming
- ✅ Proper modifier usage
- ✅ Accessibility (contentDescription)

---

## Navigation Setup

### Using Jetpack Compose Navigation

```kotlin
// 1. Define routes with sealed class
sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Domain : Screen("domain")
    data object Topic : Screen("topic/{domainId}") {
        fun createRoute(domainId: String) = "topic/$domainId"
    }
}

// 2. Create NavGraph
@Composable
fun NavGraph(
    navController: NavHostController,
    domainViewModel: DomainViewModel,
    startDestination: String = Screen.Splash.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Simple screen
        composable(route = Screen.Domain.route) {
            DomainScreen(
                viewModel = domainViewModel,
                onDomainClick = { domainId ->
                    navController.navigate(Screen.Topic.createRoute(domainId))
                },
                onBookmarkClick = {
                    navController.navigate(Screen.Bookmark.route)
                }
            )
        }
        
        // Screen with arguments
        composable(
            route = Screen.Topic.route,
            arguments = listOf(
                navArgument("domainId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val domainId = backStackEntry.arguments?.getString("domainId") 
                ?: return@composable
            
            TopicScreen(
                domainId = domainId,
                onTopicClick = { topicId ->
                    navController.navigate(Screen.QuestionList.createRoute(topicId))
                },
                onBackClick = { navController.navigateUp() }
            )
        }
    }
}

// 3. Use in MainActivity
setContent {
    TechInterviewPrepTheme {
        val navController = rememberNavController()
        NavGraph(navController = navController, domainViewModel = viewModel)
    }
}
```

---

## AdMob Integration Example

### AdManager.kt - Safe Ad Handling

```kotlin
class AdManager(private val context: Context) {
    
    private var interstitialAd: InterstitialAd? = null
    private var interactionCount = 0
    
    init {
        MobileAds.initialize(context) {}
    }
    
    // Track user interactions and show ad after threshold
    fun trackInteraction(
        activity: Activity, 
        threshold: Int = 5,
        onAdClosed: () -> Unit = {}
    ) {
        interactionCount++
        
        if (interactionCount >= threshold) {
            interactionCount = 0
            showInterstitialAd(activity, onAdClosed)
        } else {
            onAdClosed() // Continue without ad
        }
    }
    
    fun showInterstitialAd(activity: Activity, onAdClosed: () -> Unit = {}) {
        interstitialAd?.let { ad ->
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    interstitialAd = null
                    loadInterstitialAd() // Preload next
                    onAdClosed()
                }
                
                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    interstitialAd = null
                    onAdClosed() // Continue even if ad fails
                }
            }
            ad.show(activity)
        } ?: run {
            onAdClosed() // No ad available, continue
        }
    }
}

// Usage in Screen
@Composable
fun QuestionListScreen(
    onQuestionClick: (String) -> Unit,
    adManager: AdManager
) {
    val activity = LocalContext.current as Activity
    
    LazyColumn {
        items(questions) { question ->
            QuestionItem(
                question = question,
                onClick = {
                    // Track interaction before navigation
                    adManager.trackInteraction(activity) {
                        onQuestionClick(question.id)
                    }
                }
            )
        }
    }
}
```

**Ad Integration Patterns:**
- ✅ Non-blocking - app continues if ad fails
- ✅ Frequency control - not every interaction
- ✅ Preloading - next ad loads after showing
- ✅ Isolated - no ad logic in domain/data layers

---

## Adding New Use Cases

### Pattern for Creating Use Cases

```kotlin
// 1. Define in domain module
class GetQuestionByIdUseCase(
    private val repository: InterviewRepository
) {
    operator fun invoke(questionId: String): Flow<Resource<Question>> {
        return repository.getQuestionById(questionId)
    }
}

// 2. Add to repository interface
interface InterviewRepository {
    fun getQuestionById(questionId: String): Flow<Resource<Question>>
}

// 3. Implement in data module
class InterviewRepositoryImpl(
    private val localDataSource: LocalDataSource
) : InterviewRepository {
    
    override fun getQuestionById(questionId: String): Flow<Resource<Question>> = flow {
        try {
            emit(Resource.Loading())
            val question = localDataSource.getQuestionById(questionId)
            if (question != null) {
                emit(Resource.Success(question))
            } else {
                emit(Resource.Error("Question not found"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "An error occurred"))
        }
    }
}

// 4. Use in ViewModel
class QuestionDetailViewModel(
    private val getQuestionByIdUseCase: GetQuestionByIdUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<QuestionDetailUiState>(
        QuestionDetailUiState.Loading
    )
    val uiState: StateFlow<QuestionDetailUiState> = _uiState.asStateFlow()
    
    fun loadQuestion(questionId: String) {
        viewModelScope.launch {
            getQuestionByIdUseCase(questionId).collect { resource ->
                _uiState.value = when (resource) {
                    is Resource.Loading -> QuestionDetailUiState.Loading
                    is Resource.Success -> resource.data?.let {
                        QuestionDetailUiState.Success(it)
                    } ?: QuestionDetailUiState.Error("Question not found")
                    is Resource.Error -> QuestionDetailUiState.Error(
                        resource.message ?: "An error occurred"
                    )
                }
            }
        }
    }
}
```

---

## Testing Examples

### Unit Testing a Use Case

```kotlin
class GetDomainsUseCaseTest {
    
    private lateinit var getDomainsUseCase: GetDomainsUseCase
    private lateinit var mockRepository: InterviewRepository
    
    @Before
    fun setup() {
        mockRepository = mock()
        getDomainsUseCase = GetDomainsUseCase(mockRepository)
    }
    
    @Test
    fun `invoke returns flow from repository`() = runTest {
        // Given
        val expectedDomains = listOf(
            Domain("1", "Android", "Desc", "url", 5)
        )
        val expectedResource = Resource.Success(expectedDomains)
        
        whenever(mockRepository.getDomains())
            .thenReturn(flowOf(expectedResource))
        
        // When
        val result = getDomainsUseCase().first()
        
        // Then
        assertThat(result).isInstanceOf(Resource.Success::class.java)
        assertThat((result as Resource.Success).data).isEqualTo(expectedDomains)
    }
}
```

### Testing a ViewModel

```kotlin
class DomainViewModelTest {
    
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()
    
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    
    private lateinit var viewModel: DomainViewModel
    private lateinit var mockGetDomainsUseCase: GetDomainsUseCase
    
    @Before
    fun setup() {
        mockGetDomainsUseCase = mock()
        viewModel = DomainViewModel(mockGetDomainsUseCase)
    }
    
    @Test
    fun `loadDomains updates state to Success when use case succeeds`() = runTest {
        // Given
        val domains = listOf(Domain("1", "Android", "Desc", "url", 5))
        whenever(mockGetDomainsUseCase())
            .thenReturn(flowOf(Resource.Success(domains)))
        
        // When
        viewModel.loadDomains()
        advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.value
        assertThat(state).isInstanceOf(DomainUiState.Success::class.java)
        assertThat((state as DomainUiState.Success).domains).isEqualTo(domains)
    }
}
```

### Compose UI Testing

```kotlin
class DomainScreenTest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun domainScreen_displaysLoadingState() {
        // Given
        val viewModel = mock<DomainViewModel>()
        whenever(viewModel.uiState).thenReturn(
            MutableStateFlow(DomainUiState.Loading)
        )
        
        // When
        composeTestRule.setContent {
            DomainScreen(
                viewModel = viewModel,
                onDomainClick = {},
                onBookmarkClick = {}
            )
        }
        
        // Then
        composeTestRule
            .onNode(hasProgressBarIndicator())
            .assertExists()
    }
    
    @Test
    fun domainScreen_displaysDomainsInGrid() {
        // Given
        val domains = listOf(
            Domain("1", "Android", "Desc", "url", 5),
            Domain("2", "Kotlin", "Desc", "url", 3)
        )
        val viewModel = mock<DomainViewModel>()
        whenever(viewModel.uiState).thenReturn(
            MutableStateFlow(DomainUiState.Success(domains))
        )
        
        // When
        composeTestRule.setContent {
            DomainScreen(
                viewModel = viewModel,
                onDomainClick = {},
                onBookmarkClick = {}
            )
        }
        
        // Then
        composeTestRule.onNodeWithText("Android").assertExists()
        composeTestRule.onNodeWithText("Kotlin").assertExists()
    }
}
```

---

## Summary

This guide demonstrates:

1. ✅ **Clean Architecture** with clear layer separation
2. ✅ **MVVM Pattern** with StateFlow for reactive UI
3. ✅ **Repository Pattern** with use cases
4. ✅ **Jetpack Compose** best practices
5. ✅ **Navigation** with type-safe routes
6. ✅ **Ad Integration** isolated from business logic
7. ✅ **Testing** at all layers

Follow these patterns when extending the application with new features!
