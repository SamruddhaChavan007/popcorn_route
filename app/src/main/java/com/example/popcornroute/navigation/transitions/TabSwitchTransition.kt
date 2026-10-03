package com.example.popcornroute.navigation.transitions

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.scene.Scene

/*
 * Fade-through for switching between bottom tabs. Tabs are peers, so the motion is not
 * directional: the old tab fades out, then the new one fades in while settling into place
 * (slight scale-up and a small rise). Only the incoming tab moves; the bottom bar lives outside
 * the NavDisplay and stays stationary.
 */

private const val EXIT_DURATION_MS = 90
private const val ENTER_DURATION_MS = 220

private const val ENTER_INITIAL_SCALE = 0.98f
private val ENTER_RISE_OFFSET = 8.dp

/** Transition used when the user taps a different bottom tab. */
fun tabSwitchTransition(density: Density): ContentTransform {
    val risePx = with(density) { ENTER_RISE_OFFSET.roundToPx() }

    val enter = fadeIn(tween(ENTER_DURATION_MS, delayMillis = EXIT_DURATION_MS)) +
        scaleIn(
            animationSpec = tween(ENTER_DURATION_MS, delayMillis = EXIT_DURATION_MS),
            initialScale = ENTER_INITIAL_SCALE,
            transformOrigin = TransformOrigin(pivotFractionX = 0.5f, pivotFractionY = 0f),
        ) +
        slideInVertically(tween(ENTER_DURATION_MS, delayMillis = EXIT_DURATION_MS)) { risePx }
    val exit = fadeOut(tween(EXIT_DURATION_MS))

    return enter togetherWith exit
}
private const val TAB_ROOT_METADATA_KEY = "popcornroute.tabRoot"

/** Metadata to attach to the entry of every bottom-tab root so [isTabSwitch] can recognise it. */
val TabRootMetadata: Map<String, Any> = mapOf(TAB_ROOT_METADATA_KEY to true)

private fun NavEntry<*>.isTabRoot(): Boolean = metadata[TAB_ROOT_METADATA_KEY] == true

/** True only when moving between two bottom-tab roots, never to or from a tab's child screens. */
fun isTabSwitch(from: Scene<*>, to: Scene<*>): Boolean =
    from.entries.lastOrNull()?.isTabRoot() == true && to.entries.lastOrNull()?.isTabRoot() == true
