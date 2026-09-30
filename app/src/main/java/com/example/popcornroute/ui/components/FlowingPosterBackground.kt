package com.example.popcornroute.ui.components

import android.content.Context
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray

private const val POSTER_ASSET = "tmdb_posters_2026.json"
private const val TMDB_IMAGE_BASE_URL = "https://image.tmdb.org/t/p/w500"
private const val DEFAULT_ANIMATION_DURATION_MILLIS = 20_000
private const val UPWARD_COLUMN_PHASE = 0.35f
private const val DOWNWARD_COLUMN_PHASE = 0.75f
private val POSTER_GAP = 6.dp

/**
 * A full-screen backdrop of movie posters flowing in opposite directions.
 *
 * The bundled TMDB poster list is used by default. Pass [posterUrls] to display a
 * different set of images. [content] is drawn above the configurable dark overlay.
 */
@Composable
fun FlowingPosterBackground(
    modifier: Modifier = Modifier,
    posterUrls: List<String>? = null,
    overlayColor: Color = Color.Black.copy(alpha = 0.68f),
    animationDurationMillis: Int = DEFAULT_ANIMATION_DURATION_MILLIS,
    content: @Composable BoxScope.() -> Unit = {},
) {
    val bundledPosterUrls by rememberBundledPosterUrls()
    val posters = posterUrls ?: bundledPosterUrls
    val leftPosters = remember(posters) { posters.filterIndexed { index, _ -> index % 2 == 0 } }
    val rightPosters = remember(posters) { posters.filterIndexed { index, _ -> index % 2 != 0 } }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .clipToBounds(),
    ) {
        if (posters.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                FlowingPosterColumn(
                    posterUrls = leftPosters,
                    flowsUp = true,
                    animationDurationMillis = animationDurationMillis,
                    modifier = Modifier.weight(1f),
                )
                FlowingPosterColumn(
                    posterUrls = rightPosters.ifEmpty { leftPosters },
                    flowsUp = false,
                    animationDurationMillis = animationDurationMillis,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(overlayColor),
        )

        content()
    }
}

@Composable
private fun FlowingPosterColumn(
    posterUrls: List<String>,
    flowsUp: Boolean,
    animationDurationMillis: Int,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .clipToBounds(),
    ) {
        val density = LocalDensity.current
        val posterStride = with(density) {
            (maxWidth * 1.5f + POSTER_GAP).toPx()
        }
        val stripBoundary = posterStride * posterUrls.size
        val phaseOffset = posterStride * if (flowsUp) {
            UPWARD_COLUMN_PHASE
        } else {
            DOWNWARD_COLUMN_PHASE
        }
        val startOffset = if (flowsUp) {
            -phaseOffset
        } else {
            -stripBoundary - phaseOffset
        }
        val endOffset = if (flowsUp) {
            -stripBoundary - phaseOffset
        } else {
            -phaseOffset
        }
        val verticalOffset = remember(posterUrls, flowsUp, startOffset) {
            Animatable(startOffset)
        }

        LaunchedEffect(verticalOffset, endOffset, animationDurationMillis) {
            verticalOffset.snapTo(startOffset)
            while (true) {
                verticalOffset.animateTo(
                    targetValue = endOffset,
                    animationSpec = tween(
                        durationMillis = animationDurationMillis.coerceAtLeast(1),
                        easing = LinearEasing,
                    ),
                )
                verticalOffset.snapTo(startOffset)
            }
        }

        Column(
            modifier = Modifier
                .wrapContentHeight(
                    align = Alignment.Top,
                    unbounded = true,
                )
                .graphicsLayer { translationY = verticalOffset.value },
        ) {
            PosterStrip(posterUrls = posterUrls)
            PosterStrip(posterUrls = posterUrls)
        }
    }
}

@Composable
private fun PosterStrip(
    posterUrls: List<String>,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        posterUrls.forEach { posterUrl ->
            AsyncImage(
                model = posterUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(2f / 3f)
                    .clip(RoundedCornerShape(14.dp)),
            )
            Spacer(modifier = Modifier.height(POSTER_GAP))
        }
    }
}

@Composable
private fun rememberBundledPosterUrls(): State<List<String>> {
    val context = LocalContext.current.applicationContext
    return produceState(
        initialValue = emptyList(),
        key1 = context,
    ) {
        value = withContext(Dispatchers.IO) {
            loadBundledPosterUrls(context)
        }
    }
}

internal fun loadBundledPosterUrls(context: Context): List<String> = runCatching {
    val json = context.assets.open(POSTER_ASSET).bufferedReader().use { it.readText() }
    val posters = JSONArray(json)
    buildList {
        for (index in 0 until posters.length()) {
            val path = posters.getJSONObject(index).optString("tmdbPosterPath")
            if (path.startsWith('/')) add("$TMDB_IMAGE_BASE_URL$path")
        }
    }
}.getOrDefault(emptyList())