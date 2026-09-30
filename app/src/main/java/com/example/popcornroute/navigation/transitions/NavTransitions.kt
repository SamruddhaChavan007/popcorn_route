package com.example.popcornroute.navigation.transitions

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith

/*
 * Fade-through transitions for screens that share one backdrop: the old screen fades out first
 * and the new one fades in after it, so two transparent screens are never visible together.
 */

private const val EXIT_DURATION_MS = 100
private const val ENTER_DURATION_MS = 250

/** Drift distance as a fraction of the container width (1/10 is ~40dp on a typical phone). */
private const val DRIFT_FRACTION = 10

/** Forward navigation: the old screen exits to the left, the new one drifts in from the right. */
fun forwardTransition(): ContentTransform = driftTransition(direction = 1)

/** Back navigation: the mirror image of [forwardTransition]. */
fun backTransition(): ContentTransform = driftTransition(direction = -1)

private fun driftTransition(direction: Int): ContentTransform {
    val enter = fadeIn(tween(ENTER_DURATION_MS, delayMillis = EXIT_DURATION_MS)) +
        slideInHorizontally(tween(ENTER_DURATION_MS, delayMillis = EXIT_DURATION_MS)) { width ->
            direction * width / DRIFT_FRACTION
        }
    val exit = fadeOut(tween(EXIT_DURATION_MS)) +
        slideOutHorizontally(tween(EXIT_DURATION_MS)) { width ->
            -direction * width / DRIFT_FRACTION
        }
    return enter togetherWith exit
}
