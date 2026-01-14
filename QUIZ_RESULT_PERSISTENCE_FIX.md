# Quiz Result Persistence Fix

## Problem
After completing a quiz, the HomeScreen performance statistics showed 0 even though a quiz was completed. The quiz results were not being saved to the database.

## Root Cause
The QuizResultScreen was displaying results but not persisting them to the UserProgressDatabase. The HomeScreen's performance stats were reading from the database, which remained empty.

## Solution Implemented

### 1. Updated NavGraph to Save Quiz Results

**File**: `ui/src/main/java/com/prepstack/ui/navigation/NavGraph.kt`

#### Added UserProgressRepository Parameter
```kotlin
@Composable
fun NavGraph(
    navController: NavHostController,
    domainViewModel: DomainViewModel,
    homeViewModel: HomeViewModel,
    topicViewModel: TopicViewModel,
    questionListViewModel: QuestionListViewModel,
    questionDetailViewModel: QuestionDetailViewModel,
    quizViewModel: QuizViewModel,
    userProgressRepository: UserProgressRepository, // NEW
    startDestination: String = Screen.Splash.route
)
```

#### Added Quiz Result Saving Logic
In the Quiz Result screen composable, added LaunchedEffect to save results:
```kotlin
// Quiz Result Screen
composable(
    route = Screen.Result.route,
    arguments = listOf(
        navArgument("correctAnswers") { type = NavType.IntType },
        navArgument("totalQuestions") { type = NavType.IntType },
        navArgument("timeTaken") { type = NavType.LongType }
    )
) { backStackEntry ->
    val correctAnswers = backStackEntry.arguments?.getInt("correctAnswers") ?: 0
    val totalQuestions = backStackEntry.arguments?.getInt("totalQuestions") ?: 0
    val timeTaken = backStackEntry.arguments?.getLong("timeTaken") ?: 0L
    
    val incorrectAnswers = totalQuestions - correctAnswers
    val percentage = if (totalQuestions > 0) {
        (correctAnswers.toFloat() / totalQuestions * 100)
    } else {
        0f
    }
    
    val quizResult = com.prepstack.domain.model.QuizResult(
        totalQuestions = totalQuestions,
        correctAnswers = correctAnswers,
        incorrectAnswers = incorrectAnswers,
        skippedQuestions = 0,
        percentage = percentage,
        timeTaken = timeTaken
    )
    
    // Save quiz result to database
    LaunchedEffect(quizResult) {
        val quizHistory = QuizHistory(
            domainId = "quiz",
            topicId = null,
            totalQuestions = totalQuestions,
            correctAnswers = correctAnswers,
            incorrectAnswers = incorrectAnswers,
            skippedQuestions = 0,
            percentage = percentage,
            timestamp = System.currentTimeMillis(),
            completed = true
        )
        userProgressRepository.recordQuizResult(quizHistory)
    }
    
    QuizResultScreen(
        quizResult = quizResult,
        domainName = "Quiz",
        topicName = null,
        onBackClick = { navController.navigateUp() },
        onRetryQuiz = {
            navController.popBackStack()
        },
        onHomeClick = {
            navController.navigate(Screen.Main.route) {
                popUpTo(Screen.Main.route) { inclusive = true }
            }
        }
    )
}
```

### 2. Updated MainActivity

**File**: `app/src/main/java/com/prepstack/techinterviewprep/MainActivity.kt`

#### Added UserProgressRepository to NavGraph
```kotlin
NavGraph(
    navController = navController,
    domainViewModel = domainViewModel,
    homeViewModel = homeViewModel,
    topicViewModel = topicViewModel,
    questionListViewModel = questionListViewModel,
    questionDetailViewModel = questionDetailViewModel,
    quizViewModel = quizViewModel,
    userProgressRepository = app.userProgressRepository // NEW
)
```

## How It Works Now

### Data Flow
```
User completes quiz
    ↓
QuizScreen calls onQuizComplete
    ↓
Navigate to QuizResultScreen with parameters
    ↓
QuizResultScreen displays results
    ↓
LaunchedEffect saves QuizHistory to database
    ↓
UserProgressRepository.recordQuizResult()
    ↓
UserProgressDao.insertQuizHistory()
    ↓
Room database stores the result
    ↓
HomeScreen reads from database
    ↓
Performance stats updated automatically
```

### Automatic Updates
- HomeScreen uses Flow to observe database changes
- When quiz result is saved, Flow emits new data
- HomeScreen automatically updates with new stats
- No manual refresh needed

## What Gets Saved

When a quiz is completed, the following data is saved to the database:

### QuizHistory Entity
- **domainId**: Identifier for the domain (currently "quiz")
- **topicId**: Topic identifier (null for domain-level quizzes)
- **totalQuestions**: Total number of questions in quiz
- **correctAnswers**: Number of correct answers
- **incorrectAnswers**: Number of incorrect answers
- **skippedQuestions**: Number of skipped questions (currently 0)
- **percentage**: Score percentage (0-100)
- **timestamp**: When the quiz was completed
- **completed**: Boolean flag (always true for completed quizzes)

## HomeScreen Performance Stats

The HomeScreen performance section now shows:

### Questions Answered
- Sum of all `totalQuestions` from completed quizzes
- Updates automatically after each quiz

### Correct Answers
- Sum of all `correctAnswers` from completed quizzes
- Updates automatically after each quiz

### Accuracy
- Calculated as: `(totalCorrectAnswers / totalQuestionsAnswered) * 100`
- Updates automatically after each quiz

## Testing

To verify the fix works:

1. **Complete a quiz**
   - Start any quiz
   - Answer all questions
   - Click "Finish Quiz"

2. **View results**
   - QuizResultScreen should display with your score
   - Results are saved to database automatically

3. **Check HomeScreen**
   - Navigate back to HomeScreen
   - Performance section should show updated stats:
     - Questions Answered: Should be > 0
     - Correct Answers: Should match your quiz score
     - Accuracy: Should show your percentage

4. **Complete another quiz**
   - Take another quiz
   - HomeScreen stats should update again

## Future Enhancements

To improve the quiz result tracking further:

### 1. Track Domain and Topic
- Pass actual `domainId` and `topicId` from QuizScreen
- Save these in QuizHistory
- Allow filtering by domain/topic

### 2. Track Quiz Session ID
- Generate unique session ID for each quiz
- Allow reviewing specific quiz sessions
- Enable retry of same quiz

### 3. Save Question-by-Question Results
- Track which questions were answered correctly/incorrectly
- Show detailed breakdown in results
- Enable review mode

### 4. Add Quiz History Screen
- Display list of all completed quizzes
- Show performance trends over time
- Allow re-taking previous quizzes

### 5. Add Performance Charts
- Visual representation of progress
- Compare scores across attempts
- Identify strengths and weaknesses

## Notes

- Quiz results are saved using LaunchedEffect when the result screen is displayed
- The save happens automatically - no user action required
- HomeScreen updates automatically using reactive Flow
- Data persists across app restarts using Room database
- Multiple quiz results are accumulated in the database
- Performance stats are calculated from all completed quizzes

## Troubleshooting

If performance stats still show 0:

1. **Check database**
   - Verify UserProgressDatabase is created
   - Check if QuizHistory table exists
   - Use database inspector to view data

2. **Check repository**
   - Verify UserProgressRepository is properly initialized
   - Check if recordQuizResult is being called
   - Add logging to debug

3. **Check HomeScreen**
   - Verify HomeViewModel is observing the correct Flow
   - Check if UI is updating from state
   - Add logging to debug state changes

4. **Clear app data**
   - Uninstall and reinstall the app
   - This ensures clean database state
   - Test again with a fresh quiz