package com.nexus7.kryptonplayer.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeInput
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.glass.GlassStyle
import dev.chrisbanes.haze.glass.hazeGlass

@Composable
fun GlassSurface(
    hazeState: HazeState,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 26.dp,
    content: @Composable BoxScope.() -> Unit,
) {
    val surfaceShape = RoundedCornerShape(cornerRadius)
    val glassStyle = remember(cornerRadius) {
        GlassStyle.regular.then { shape(surfaceShape) }
    }
    Box(
        modifier = modifier
            .clip(surfaceShape)
            .hazeGlass(
                input = HazeInput.Sources(hazeState),
                style = glassStyle,
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    listOf(
                        Color.White.copy(alpha = 0.42f),
                        Color.White.copy(alpha = 0.10f),
                        Color.White.copy(alpha = 0.04f),
                    ),
                ),
                shape = surfaceShape,
            ),
        content = content,
    )
}

@Composable
fun GlassPill(
    hazeState: HazeState,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    GlassSurface(
        hazeState = hazeState,
        modifier = modifier.fillMaxWidth(),
        cornerRadius = 100.dp,
        content = content,
    )
}
