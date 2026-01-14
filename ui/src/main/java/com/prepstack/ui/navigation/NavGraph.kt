package com.prepstack.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.prepstack.domain.model.QuizDescriptor
import com.prepstack.domain.model.QuizHistory
import com.prepstack.domain.repository.UserProgressRepository
import com.prepstack.ui.screen.*
import com.prepstack.ui.viewmodel.DomainViewModel
import com.prepstack.ui.viewmodel.HomeViewModel
import com.prepstack.ui.viewmodel.QuestionDetailViewModel
import com.prepstack.ui.viewmodel.QuestionListViewModel
import com.prepstack.ui.viewmodel.QuizViewModel
import com.prepstack.ui.viewmodel.TopicViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Main Navigation Graph using Jetpack Compose Navigation
 */
@Composable
fun NavGraph(
    navController: NavHostController,
    domainViewModel: DomainViewModel,
    homeViewModel: HomeViewModel,
    topicViewModel: TopicViewModel,
    questionListViewModel: QuestionListViewModel,
    questionDetailViewModel: QuestionDetailViewModel,
    quizViewModel: QuizViewModel,
    userProgressRepository: UserProgressRepository,
    startDestination: String = Screen.Splash.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Splash Screen
        composable(route = Screen.Splash.route) {
            SplashScreen(
                onNavigateToDomain = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }
        
        // Main Screen with Bottom Navigation
        composable(route = Screen.Main.route) {
            MainScreen(
                domainViewModel = domainViewModel,
                homeViewModel = homeViewModel,
                onDomainClick = { domainId ->
                    navController.navigate(Screen.Topic.createRoute(domainId))
                },
                onTopicClick = { topicId ->
                    navController.navigate(Screen.QuestionList.createRoute(topicId))
                },
                onResumeTest = { testId ->
                    // Navigate to quiz screen with testId
                    navController.navigate(Screen.Quiz.createRoute(testId))
                },
                onBookmarkNavigate = {
                    navController.navigate(Screen.Bookmark.route)
                }
            )
        }
        
        // Domain Screen (Keep for direct navigation)
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
        
        // Topic Screen
        composable(
            route = Screen.Topic.route,
            arguments = listOf(
                navArgument("domainId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val domainId = backStackEntry.arguments?.getString("domainId") ?: return@composable
            
            TopicScreen(
                domainId = domainId,
                viewModel = topicViewModel,
                onTopicClick = { topicId ->
                    navController.navigate(Screen.QuestionList.createRoute(topicId))
                },
                onQuizClick = { quiz: QuizDescriptor ->
                    navController.navigate(Screen.Quiz.createRoute(quiz.id, quiz.topicId))
                },
                onBackClick = { navController.navigateUp() }
            )
        }
        
        // Question List Screen
        composable(
            route = Screen.QuestionList.route,
            arguments = listOf(
                navArgument("topicId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val topicId = backStackEntry.arguments?.getString("topicId") ?: return@composable
            
            QuestionListScreen(
                topicId = topicId,
                viewModel = questionListViewModel,
                onQuestionClick = { questionId ->
                    navController.navigate(Screen.QuestionDetail.createRoute(questionId))
                },
                onBackClick = { navController.navigateUp() }
            )
        }
        
        // Quiz Screen
        composable(
            route = Screen.Quiz.route,
            arguments = listOf(
                navArgument("domainId") { type = NavType.StringType },
                navArgument("topicId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val domainId = backStackEntry.arguments?.getString("domainId") ?: return@composable
            val topicId = backStackEntry.arguments?.getString("topicId")
            
            QuizScreen(
                viewModel = quizViewModel,
                domainId = domainId,
                topicId = topicId,
                onBackClick = { navController.navigateUp() },
                onQuizComplete = { correctAnswers, totalQuestions, timeTaken ->
                    // Navigate to result screen
                    navController.navigate(
                        Screen.Result.createRoute(correctAnswers, totalQuestions, timeTaken)
                    ) {
                        popUpTo(Screen.Main.route) { inclusive = false }
                    }
                }
            )
        }
        
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
                    domainId = "quiz", // You might want to pass actual domainId
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
                    // Navigate back to quiz (you might want to reload the same quiz)
                    navController.popBackStack()
                },
                onHomeClick = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Main.route) { inclusive = true }
                    }
                }
            )
        }
        
        // Question Detail Screen
        composable(
            route = Screen.QuestionDetail.route,
            arguments = listOf(
                navArgument("questionId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val questionId = backStackEntry.arguments?.getString("questionId") ?: return@composable
            
            QuestionDetailScreen(
                questionId = questionId,
                viewModel = questionDetailViewModel,
                onBackClick = { navController.navigateUp() }
            )
        }
        
        // Bookmark Screen
        composable(route = Screen.Bookmark.route) {
            BookmarkScreen(
                onBackClick = { navController.navigateUp() }
            )
        }
    }
}
