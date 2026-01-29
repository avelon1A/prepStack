package com.prepstack.ui.navigation

/**
 * Sealed class representing all navigation destinations
 */
sealed class Screen(val route: String) {
    data object Main : Screen("main")
    data object Domain : Screen("domain")
    data object Topic : Screen("topic/{domainId}") {
        fun createRoute(domainId: String) = "topic/$domainId"
    }
    data object QuestionList : Screen("question_list/{topicId}") {
        fun createRoute(topicId: String) = "question_list/$topicId"
    }
    data object QuestionDetail : Screen("question_detail/{questionId}") {
        fun createRoute(questionId: String) = "question_detail/$questionId"
    }
    data object Quiz : Screen("quiz/{domainId}?topicId={topicId}") {
        fun createRoute(domainId: String, topicId: String? = null): String {
            return if (topicId != null) {
                "quiz/$domainId?topicId=$topicId"
            } else {
                "quiz/$domainId"
            }
        }
    }
    data object Result : Screen("result/{correctAnswers}/{totalQuestions}/{timeTaken}") {
        fun createRoute(correctAnswers: Int, totalQuestions: Int, timeTaken: Long): String {
            return "result/$correctAnswers/$totalQuestions/$timeTaken"
        }
    }
    data object Bookmark : Screen("bookmark")
}
