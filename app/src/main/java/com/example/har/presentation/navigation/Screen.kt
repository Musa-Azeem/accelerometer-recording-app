package com.example.har.presentation.navigation

// Navigation Argument for Screens with scrollable types:
const val SCROLL_TYPE_NAV_ARGUMENT = "scrollType"

/**
 * Represent all Screens (Composables) in the app.
 */
sealed class Screen(
    val route: String
) {
    object Landing : Screen("landing")
    object Theme : Screen("theme")
}
