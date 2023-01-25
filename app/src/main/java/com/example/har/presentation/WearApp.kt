package com.example.har.presentation

import androidx.compose.foundation.background
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavHostController
import androidx.wear.compose.navigation.currentBackStackEntryAsState
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.example.har.presentation.navigation.DestinationScrollType
import com.example.har.presentation.navigation.SCROLL_TYPE_NAV_ARGUMENT
import com.example.har.presentation.theme.HarTheme
import com.example.har.presentation.ui.ScalingLazyListStateViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.wear.compose.material.*
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import com.example.har.presentation.navigation.Screen
import com.example.har.presentation.ui.ScrollStateViewModel
import com.example.har.presentation.ui.landing.LandingScreen
import com.google.android.horologist.compose.layout.scrollAway
import java.time.LocalDateTime


@Composable
fun WearApp(
    modifier: Modifier = Modifier,
    swipeDismissableNavController: NavHostController = rememberSwipeDismissableNavController()
) {
    HarTheme {
        // -------------------------------- STATE VARS ---------------------------------------------
        var vignetteVisiblePreference by rememberSaveable { mutableStateOf(true) }

        // Determine/save scroll state
        val currentBackStackEntry by swipeDismissableNavController.currentBackStackEntryAsState()

        val scrollType =
            currentBackStackEntry?.arguments?.getSerializable(SCROLL_TYPE_NAV_ARGUMENT)
                ?: DestinationScrollType.NONE

        var displayValueForUserInput by remember { mutableStateOf(5) }
        var dateTimeForUserInput by remember { mutableStateOf(LocalDateTime.now()) }

        // --------------------- SCAFFOLD - Top level app layout -----------------------------------
        // Scaffold: basic wear os app layout
        Scaffold(
            modifier = modifier,
            timeText = {
                // Place time at top of screen - time is hidden when scrolling
                val timeTextModifier =
                    when (scrollType) {
                        DestinationScrollType.SCALING_LAZY_COLUMN_SCROLLING -> {
                            val scrollViewModel: ScalingLazyListStateViewModel =
                                viewModel(currentBackStackEntry!!)
                            Modifier.scrollAway(scrollViewModel.scrollState)
                        }
                        DestinationScrollType.COLUMN_SCROLLING -> {
                            val viewModel: ScrollStateViewModel =
                                viewModel(currentBackStackEntry!!)
                            Modifier.scrollAway(viewModel.scrollState)
                        }
                        DestinationScrollType.TIME_TEXT_ONLY -> {
                            Modifier
                        }
                        else -> {
                            null
                        }
                    }
            },
            vignette = {
                // Only show vignette for screens with scrollable content
                if (scrollType == DestinationScrollType.SCALING_LAZY_COLUMN_SCROLLING ||
                    scrollType == DestinationScrollType.COLUMN_SCROLLING
                ) {
                    if (vignetteVisiblePreference) {
                        Vignette(vignettePosition = VignettePosition.TopAndBottom)
                    }
                }
            },
            positionIndicator = {
                // Only Display Position indicator for scrollable content
                when (scrollType) {
                    DestinationScrollType.SCALING_LAZY_COLUMN_SCROLLING -> {
                        // Get or create the ViewModel associated with the current back stack entry
                        val scrollViewModel: ScalingLazyListStateViewModel =
                            viewModel(currentBackStackEntry!!)
                        PositionIndicator(scalingLazyListState = scrollViewModel.scrollState)
                    }
                    DestinationScrollType.COLUMN_SCROLLING -> {
                        // Get or create the ViewModel associated with the current back stack entry
                        val viewModel: ScrollStateViewModel = viewModel(currentBackStackEntry!!)
                        PositionIndicator(scrollState = viewModel.scrollState)
                    }
                }
            }
        ) {
            // ----------------------- DEFINE NAVIGATION GRAPH -------------------------------------
            // Define Navigation Graph
            SwipeDismissableNavHost(
                navController = swipeDismissableNavController,
                startDestination = Screen.Landing.route,
                modifier = Modifier.background(MaterialTheme.colors.background),
            ) {
                // Define composable for each route

                // ----------------------- LANDING SCREEN ------------------------------------------
                composable(
                    route = Screen.Landing.route,
                    arguments = listOf(
                        // attach this argument as information for destination
                        navArgument(SCROLL_TYPE_NAV_ARGUMENT) {
                            type = NavType.EnumType(DestinationScrollType::class.java)
                            defaultValue = DestinationScrollType.SCALING_LAZY_COLUMN_SCROLLING
                        }
                    )
                ) {
                    // Content of Landing Screen
                    val scalingLazyListState = scalingLazyListState(it)
                    val focusRequester = remember { FocusRequester() }


                    LandingScreen(
                        scalingLazyListState = scalingLazyListState,
                        focusRequester = focusRequester,
                        onClickButton = {
//                            swipeDismissableNavController.navigate(Screen.SomeOtherRoute.route)
                        }
                    )

                    RequestFocusOnResume(focusRequester)
                }
            }
        }
    }
}

@Composable
private fun scalingLazyListState(it: NavBackStackEntry): ScalingLazyListState {
    val passedScrollType = it.arguments?.getSerializable(SCROLL_TYPE_NAV_ARGUMENT)

    check(
        passedScrollType == DestinationScrollType.SCALING_LAZY_COLUMN_SCROLLING
    ) {
        "Scroll type must be DestinationScrollType.SCALING_LAZY_COLUMN_SCROLLING"
    }

    val scrollViewModel: ScalingLazyListStateViewModel = viewModel(it)

    return scrollViewModel.scrollState
}


@Composable
private fun RequestFocusOnResume(focusRequester: FocusRequester) {
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(Unit) {
        lifecycleOwner.repeatOnLifecycle(state = Lifecycle.State.RESUMED) {
            focusRequester.requestFocus()
        }
    }
}