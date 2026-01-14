package com.prepstack.techinterviewprep

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.rememberNavController
import com.prepstack.techinterviewprep.ui.theme.TechInterviewPrepTheme
import com.prepstack.ui.navigation.NavGraph
import com.prepstack.ui.viewmodel.DomainViewModel
import com.prepstack.ui.viewmodel.HomeViewModel
import com.prepstack.ui.viewmodel.QuestionDetailViewModel
import com.prepstack.ui.viewmodel.QuestionListViewModel
import com.prepstack.ui.viewmodel.QuizViewModel
import com.prepstack.ui.viewmodel.TopicViewModel

/**
 * Main Activity - Entry point of the application
 * Sets up navigation and dependency injection
 */
class MainActivity : ComponentActivity() {
    
    private lateinit var app: TechInterviewPrepApp
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        app = application as TechInterviewPrepApp
        
        // Create ViewModels with dependencies
        val domainViewModel = DomainViewModel(app.getDomainsUseCase)
        val homeViewModel = HomeViewModel(app.userProgressRepository)
        val topicViewModel = TopicViewModel(app.getTopicsUseCase, app.interviewRepository)
        val questionListViewModel = QuestionListViewModel(
            app.getQuestionsUseCase,
            app.bookmarkRepository
        )
        val questionDetailViewModel = QuestionDetailViewModel(
            app.interviewRepository,
            app.bookmarkRepository
        )
        val quizViewModel = QuizViewModel(app.interviewRepository)
        
        setContent {
            TechInterviewPrepTheme {
                val navController = rememberNavController()
                
                NavGraph(
                    navController = navController,
                    domainViewModel = domainViewModel,
                    homeViewModel = homeViewModel,
                    topicViewModel = topicViewModel,
                    questionListViewModel = questionListViewModel,
                    questionDetailViewModel = questionDetailViewModel,
                    quizViewModel = quizViewModel,
                    userProgressRepository = app.userProgressRepository
                )
            }
        }
    }
}