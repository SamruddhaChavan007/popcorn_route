package com.example.popcornroute.navigation.auth

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.popcornroute.HiltTestActivity
import com.example.popcornroute.R
import com.example.popcornroute.auth.signin.presentation.SIGN_IN_SCREEN_TAG
import com.example.popcornroute.ui.theme.PopcornRouteTheme
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class AuthNavigationTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<HiltTestActivity>()

    private companion object {
        const val TRANSITION_SETTLE_MS = 1_000L
    }

    @Before
    fun pauseClock() {
        // The sign-in screen's poster background animates forever, so Compose never goes idle
        // on its own; time is advanced manually in settleTransition().
        composeRule.mainClock.autoAdvance = false
    }

    private fun launchAuthNavigation() {
        hiltRule.inject()
        composeRule.setContent {
            PopcornRouteTheme { AuthNavigation() }
        }
    }

    /** Lets NavDisplay's transition finish. */
    private fun settleTransition() {
        composeRule.mainClock.advanceTimeBy(TRANSITION_SETTLE_MS)
    }

    private fun string(id: Int) = composeRule.activity.getString(id)

    @Test
    fun startsOnSignUpScreen() {
        launchAuthNavigation()
        composeRule.onNodeWithText(string(R.string.sign_up_title)).assertIsDisplayed()
        composeRule.onNodeWithTag(SIGN_IN_SCREEN_TAG).assertDoesNotExist()
    }

    @Test
    fun tappingSignInLinkOpensSignInScreen() {
        launchAuthNavigation()
        composeRule.onNodeWithText(string(R.string.sign_up_sign_in_action)).performClick()
        settleTransition()

        composeRule.onNodeWithTag(SIGN_IN_SCREEN_TAG).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.sign_up_title)).assertDoesNotExist()
    }

    @Test
    fun backFromSignInReturnsToSignUp() {
        launchAuthNavigation()
        composeRule.onNodeWithText(string(R.string.sign_up_sign_in_action)).performClick()
        settleTransition()
        composeRule.onNodeWithTag(SIGN_IN_SCREEN_TAG).assertIsDisplayed()

        composeRule.runOnUiThread {
            composeRule.activity.onBackPressedDispatcher.onBackPressed()
        }
        settleTransition()

        composeRule.onNodeWithText(string(R.string.sign_up_title)).assertIsDisplayed()
        composeRule.onNodeWithTag(SIGN_IN_SCREEN_TAG).assertDoesNotExist()
    }

    @Test
    fun signInScreenSurvivesStateRestoration() {
        hiltRule.inject()
        val restorationTester = StateRestorationTester(composeRule)
        restorationTester.setContent {
            PopcornRouteTheme { AuthNavigation() }
        }
        composeRule.onNodeWithText(string(R.string.sign_up_sign_in_action)).performClick()
        settleTransition()
        composeRule.onNodeWithTag(SIGN_IN_SCREEN_TAG).assertIsDisplayed()

        restorationTester.emulateSavedInstanceStateRestore()

        composeRule.onNodeWithTag(SIGN_IN_SCREEN_TAG).assertIsDisplayed()
    }
}