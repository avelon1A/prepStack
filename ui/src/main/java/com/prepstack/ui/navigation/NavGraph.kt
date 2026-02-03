package com.prepstack.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.prepstack.core.util.AnalyticsLogger
import com.prepstack.domain.model.QuizDescriptor
import com.prepstack.domain.model.QuizHistory
import com.prepstack.domain.repository.UserProgressRepository
import com.prepstack.ui.screen.*
import com.prepstack.ui.viewmodel.BookmarkViewModel
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
    bookmarkViewModel: BookmarkViewModel,
    userProgressRepository: UserProgressRepository,
    startDestination: String = Screen.Main.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Main Screen with Bottom Navigation
        composable(route = Screen.Main.route) {
            // Log screen view
            LaunchedEffect(Unit) {
                AnalyticsLogger.logScreenView("main_screen")
            }
            
            MainScreen(
                domainViewModel = domainViewModel,
                homeViewModel = homeViewModel,
                bookmarkViewModel = bookmarkViewModel,
                onDomainClick = { domainId ->
                    // Log domain selection
                    AnalyticsLogger.logDomainSelected(domainId, "domain_from_main")
                    navController.navigate(Screen.Topic.createRoute(domainId))
                },
                onTopicClick = { topicId ->
                    // Log topic selection
                    AnalyticsLogger.logTopicSelected("from_main", topicId, "topic_from_main")
                    navController.navigate(Screen.QuestionList.createRoute(topicId))
                },
                onResumeTest = { testId ->
                    // Log quiz resumed event
                    val params = android.os.Bundle().apply {
                        putString("quiz_id", testId)
                    }
                    AnalyticsLogger.logEvent("quiz_resumed", params)
                    // Navigate to quiz screen with testId
                    navController.navigate(Screen.Quiz.createRoute(testId))
                },
                onBookmarkNavigate = {
                    navController.navigate(Screen.Bookmark.route)
                },
                onQuestionClick = { questionId ->
                    // Log question click from main screen bookmarks tab
                    val params = android.os.Bundle().apply {
                        putString("question_id", questionId)
                        putString("source", "main_bookmarks_tab")
                    }
                    AnalyticsLogger.logEvent("question_clicked", params)
                    navController.navigate(Screen.QuestionDetail.createRoute(questionId))
                }
            )
        }
        
        // Domain Screen (Keep for direct navigation)
        composable(route = Screen.Domain.route) {
            // Log screen view
            LaunchedEffect(Unit) {
                AnalyticsLogger.logScreenView("domain_screen")
            }
            
            DomainScreen(
                viewModel = domainViewModel,
                onDomainClick = { domainId ->
                    // Log domain selection event
                    AnalyticsLogger.logDomainSelected(domainId, "domain_from_domains_screen")
                    navController.navigate(Screen.Topic.createRoute(domainId))
                },
                onBookmarkClick = {
                    // Log bookmark navigation event
                    val params = android.os.Bundle().apply {
                        putString("source", "domain_screen")
                    }
                    AnalyticsLogger.logEvent("bookmark_navigation", params)
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
            
            // Log screen view with domain info
            LaunchedEffect(domainId) {
                AnalyticsLogger.logScreenView("topic_screen")
                // Log additional parameters about the screen
                val params = android.os.Bundle().apply {
                    putString("domain_id", domainId)
                }
                AnalyticsLogger.logEvent("topic_screen_viewed", params)
            }
            
            TopicScreen(
                domainId = domainId,
                viewModel = topicViewModel,
                onTopicClick = { topicId ->
                    // Log topic selection
                    AnalyticsLogger.logTopicSelected(domainId, topicId, "from_topic_screen")
                    navController.navigate(Screen.QuestionList.createRoute(topicId))
                },
                onQuizClick = { quiz: QuizDescriptor ->
                    // Log quiz started event
                    AnalyticsLogger.logEvent("quiz_starting", android.os.Bundle().apply {
                        putString("quiz_type", "topic_quiz")
                        putString("domain_id", domainId)
                        putString("topic_id", quiz.topicId)
                        putInt("question_count", 10) // Hardcoded as property doesn't exist
                    })
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
            
            // Log screen view
            LaunchedEffect(topicId) {
                AnalyticsLogger.logScreenView("question_list_screen")
                val params = android.os.Bundle().apply {
                    putString("topic_id", topicId)
                }
                AnalyticsLogger.logEvent("question_list_viewed", params)
            }
            
            QuestionListScreen(
                topicId = topicId,
                viewModel = questionListViewModel,
                onQuestionClick = { questionId ->
                    // Log question selection
                    val params = android.os.Bundle().apply {
                        putString("question_id", questionId)
                        putString("topic_id", topicId)
                    }
                    AnalyticsLogger.logEvent("question_selected", params)
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
            
            // Log screen view and quiz start
            LaunchedEffect(domainId, topicId) {
                AnalyticsLogger.logScreenView("quiz_screen")
                
                // Log quiz start event with details
                val params = android.os.Bundle().apply {
                    putString("quiz_type", if (topicId != null) "topic_quiz" else "domain_quiz")
                    putString("domain_id", domainId)
                    putString("topic_id", topicId ?: "null")
                    putInt("question_count", 10) // Default or assumed count
                }
                AnalyticsLogger.logEvent("quiz_started", params)
            }
            
            QuizScreen(
                viewModel = quizViewModel,
                domainId = domainId,
                topicId = topicId,
                onBackClick = { 
                    // Log quiz abandoned event
                    val params = android.os.Bundle().apply {
                        putString("domain_id", domainId)
                        putString("topic_id", topicId ?: "null")
                    }
                    AnalyticsLogger.logEvent("quiz_abandoned", params)
                    navController.navigateUp() 
                },
                onQuizComplete = { correctAnswers, totalQuestions, timeTaken ->
                    // Log quiz completed event
                    val params = android.os.Bundle().apply {
                        putString("quiz_type", if (topicId != null) "topic_quiz" else "domain_quiz")
                        putString("domain_id", domainId)
                        putString("topic_id", topicId ?: "null")
                        putInt("score", correctAnswers)
                        putInt("total", totalQuestions)
                        putLong("duration_seconds", timeTaken)
                    }
                    AnalyticsLogger.logEvent("quiz_completed", params)
                    
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
            
            // Log screen view
            LaunchedEffect(quizResult) {
                AnalyticsLogger.logScreenView("quiz_result_screen")
                
                // Log detailed quiz result
                val params = android.os.Bundle().apply {
                    putInt("correct_answers", correctAnswers)
                    putInt("total_questions", totalQuestions)
                    putInt("incorrect_answers", incorrectAnswers)
                    putFloat("percentage", percentage)
                    putLong("time_taken", timeTaken)
                    putString("performance_level", when {
                        percentage >= 80 -> "excellent"
                        percentage >= 60 -> "good"
                        percentage >= 40 -> "average"
                        else -> "needs_improvement"
                    })
                }
                AnalyticsLogger.logEvent("quiz_result_details", params)
                
                // Save quiz result to database
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
                    // Log retry quiz event
                    val params = android.os.Bundle().apply {
                        putInt("previous_score", correctAnswers)
                        putInt("previous_total", totalQuestions)
                    }
                    AnalyticsLogger.logEvent("quiz_retry", params)
                    // Navigate back to quiz (you might want to reload the same quiz)
                    navController.popBackStack()
                },
                onHomeClick = {
                    // Log navigation to home
                    AnalyticsLogger.logEvent("result_to_home_navigation")
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
            
            // Log screen view
            LaunchedEffect(questionId) {
                AnalyticsLogger.logScreenView("question_detail_screen")
                val params = android.os.Bundle().apply {
                    putString("question_id", questionId)
                }
                AnalyticsLogger.logEvent("question_detail_viewed", params)
            }
            
            QuestionDetailScreen(
                questionId = questionId,
                viewModel = questionDetailViewModel,
                onBackClick = { navController.navigateUp() }
            )
        }
        
        // Bookmark Screen
    composable(route = Screen.Bookmark.route) {
        // Log screen view
        LaunchedEffect(Unit) {
            AnalyticsLogger.logScreenView("bookmark_screen")
        }
        
        BookmarkScreen(
            viewModel = bookmarkViewModel,
            onQuestionClick = { questionId ->
                // Log bookmarked question click
                val params = android.os.Bundle().apply {
                    putString("question_id", questionId)
                    putString("source", "bookmark_screen")
                }
                AnalyticsLogger.logEvent("bookmarked_question_clicked", params)
                navController.navigate(Screen.QuestionDetail.createRoute(questionId))
            },
            onBackClick = {
                // Log navigation back from bookmarks
                AnalyticsLogger.logEvent("bookmark_exit")
                // Pop back stack to return to previous screen
                navController.popBackStack()
            }
        )
    }
    
    // Voice Interview Screen
    composable(route = Screen.VoiceInterview.route) {
        // Log screen view
        LaunchedEffect(Unit) {
            AnalyticsLogger.logScreenView("voice_interview_screen")
        }
        
        AppVoiceInterviewScreen(
            onBackClick = {
                // Log navigation back from voice interview
                AnalyticsLogger.logEvent("voice_interview_exit")
                // Pop back stack to return to previous screen
                navController.popBackStack()
            }
        )
    }
    }
}