package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CoreState
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.NeonBlue
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonCyanBright
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.TextCyan
import com.example.ui.theme.TextCyanDim
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun JarvisWaveform(
  modifier: Modifier = Modifier,
  state: CoreState = CoreState.IDLE,
  waveformSize: Dp = 260.dp,
  onClick: () -> Unit = {}
) {
  val infiniteTransition = rememberInfiniteTransition(label = "jarvis_reactor")

  // Outer ring clockwise rotation
  val outerRotation by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 360f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 24000, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "outer_rot"
  )

  // Middle ring counter-clockwise rotation
  val innerRotation by infiniteTransition.animateFloat(
    initialValue = 360f,
    targetValue = 0f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 14000, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "inner_rot"
  )

  // Core breathing pulse
  val pulseScale by infiniteTransition.animateFloat(
    initialValue = 0.92f,
    targetValue = 1.08f,
    animationSpec = infiniteRepeatable(
      animation = tween(
        durationMillis = if (state == CoreState.LISTENING || state == CoreState.SPEAKING) 700 else 1800,
        easing = FastOutSlowInEasing
      ),
      repeatMode = RepeatMode.Reverse
    ),
    label = "pulse_scale"
  )

  // Audio wave frequency progression
  val wavePhase by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = (2 * PI).toFloat(),
    animationSpec = infiniteRepeatable(
      animation = tween(
        durationMillis = if (state == CoreState.LISTENING) 1200 else if (state == CoreState.SPEAKING) 1500 else 4000,
        easing = LinearEasing
      ),
      repeatMode = RepeatMode.Restart
    ),
    label = "wave_phase"
  )

  val stateGlowColor = when (state) {
    CoreState.IDLE -> NeonCyan
    CoreState.LISTENING -> NeonCyanBright
    CoreState.PROCESSING -> NeonBlue
    CoreState.SPEAKING -> ElectricBlue
  }

  Box(
    modifier = modifier
      .size(waveformSize)
      .testTag("jarvis_waveform_center")
      .clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        onClick = onClick
      ),
    contentAlignment = Alignment.Center
  ) {
    Canvas(modifier = Modifier.fillMaxSize()) {
      val center = Offset(size.width / 2f, size.height / 2f)
      val radius = size.minDimension / 2f - 12.dp.toPx()

      // 1. Background Ambient Radial Glow
      drawCircle(
        brush = Brush.radialGradient(
          colors = listOf(
            stateGlowColor.copy(alpha = 0.28f * pulseScale),
            NeonBlue.copy(alpha = 0.12f),
            Color.Transparent
          ),
          center = center,
          radius = radius * 1.15f
        ),
        radius = radius * 1.15f,
        center = center
      )

      // 2. Outer Rotating Segmented HUD Ring
      rotate(outerRotation, pivot = center) {
        val outerRadius = radius * 0.96f
        // Base thin ring
        drawCircle(
          color = NeonCyan.copy(alpha = 0.35f),
          radius = outerRadius,
          center = center,
          style = Stroke(width = 1.5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 16f), 0f))
        )

        // 4 Segmented thicker HUD brackets
        for (i in 0 until 4) {
          val startAngle = i * 90f + 15f
          drawArc(
            color = NeonCyanBright,
            startAngle = startAngle,
            sweepAngle = 45f,
            useCenter = false,
            topLeft = Offset(center.x - outerRadius, center.y - outerRadius),
            size = androidx.compose.ui.geometry.Size(outerRadius * 2, outerRadius * 2),
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
          )
        }

        // Cardinal Tick Marks
        val tickCount = 24
        for (i in 0 until tickCount) {
          val angle = (i * (360f / tickCount)) * (PI / 180f)
          val tickLen = if (i % 6 == 0) 8.dp.toPx() else 4.dp.toPx()
          val tickStroke = if (i % 6 == 0) 2.dp.toPx() else 1.dp.toPx()
          val p1 = Offset(
            x = center.x + (outerRadius - tickLen) * cos(angle).toFloat(),
            y = center.y + (outerRadius - tickLen) * sin(angle).toFloat()
          )
          val p2 = Offset(
            x = center.x + outerRadius * cos(angle).toFloat(),
            y = center.y + outerRadius * sin(angle).toFloat()
          )
          drawLine(
            color = if (i % 6 == 0) NeonCyanBright else NeonCyan.copy(alpha = 0.4f),
            start = p1,
            end = p2,
            strokeWidth = tickStroke
          )
        }
      }

      // 3. Middle Counter-Rotating Arc Reactor Segments
      rotate(innerRotation, pivot = center) {
        val midRadius = radius * 0.78f
        drawCircle(
          color = NeonBlue.copy(alpha = 0.4f),
          radius = midRadius,
          center = center,
          style = Stroke(width = 1.5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 10f), 0f))
        )

        // Triple notched power arcs
        for (i in 0 until 3) {
          val startAngle = i * 120f + 10f
          drawArc(
            color = stateGlowColor.copy(alpha = 0.85f),
            startAngle = startAngle,
            sweepAngle = 60f,
            useCenter = false,
            topLeft = Offset(center.x - midRadius, center.y - midRadius),
            size = androidx.compose.ui.geometry.Size(midRadius * 2, midRadius * 2),
            style = Stroke(width = 2.5.dp.toPx())
          )
        }
      }

      // 4. Large Circular Animated Waveform Bars (Radiating Audio Spectrum)
      val barCount = 48
      val innerWaveRadius = radius * 0.44f
      val maxWaveHeight = radius * 0.28f

      for (i in 0 until barCount) {
        val angleRad = (i * (360f / barCount)) * (PI / 180f)
        // Multi-frequency harmonic calculation
        val harmonic = sin(angleRad * 4 + wavePhase) * cos(angleRad * 2 - wavePhase * 0.5)
        val activityMultiplier = when (state) {
          CoreState.IDLE -> 0.35f + 0.15f * pulseScale
          CoreState.LISTENING -> 0.75f + 0.35f * kotlin.math.abs(sin(i * 0.8f + wavePhase * 2)).toFloat()
          CoreState.PROCESSING -> 0.60f + 0.25f * cos(i * 1.5f + wavePhase).toFloat()
          CoreState.SPEAKING -> 0.85f + 0.45f * kotlin.math.abs(sin(i * 1.2f + wavePhase * 3)).toFloat()
        }

        val barLength = (maxWaveHeight * (0.25f + 0.75f * kotlin.math.abs(harmonic).toFloat()) * activityMultiplier)
          .coerceIn(3.dp.toPx(), maxWaveHeight)

        val startX = center.x + innerWaveRadius * cos(angleRad).toFloat()
        val startY = center.y + innerWaveRadius * sin(angleRad).toFloat()
        val endX = center.x + (innerWaveRadius + barLength) * cos(angleRad).toFloat()
        val endY = center.y + (innerWaveRadius + barLength) * sin(angleRad).toFloat()

        val barColor = when {
          i % 4 == 0 -> NeonCyanBright
          i % 2 == 0 -> NeonCyan
          else -> NeonBlue
        }.copy(alpha = (0.45f + 0.55f * (barLength / maxWaveHeight)).coerceIn(0.3f, 1f))

        drawLine(
          color = barColor,
          start = Offset(startX, startY),
          end = Offset(endX, endY),
          strokeWidth = 2.4.dp.toPx(),
          cap = StrokeCap.Round
        )
      }

      // 5. Inner Core High Energy Ring
      val coreRadius = radius * 0.38f * pulseScale
      drawCircle(
        brush = Brush.radialGradient(
          colors = listOf(
            Color.White.copy(alpha = 0.95f),
            stateGlowColor.copy(alpha = 0.8f),
            NeonBlue.copy(alpha = 0.5f),
            Color.Transparent
          ),
          center = center,
          radius = coreRadius
        ),
        radius = coreRadius,
        center = center
      )

      drawCircle(
        color = NeonCyanBright,
        radius = coreRadius,
        center = center,
        style = Stroke(width = 2.dp.toPx())
      )

      // Inner Arc Reactor Center Dot & Ring
      drawCircle(
        color = Color.White,
        radius = 5.dp.toPx(),
        center = center
      )
    }

    // Central Status Overlay Text
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      Text(
        text = "JARVIS",
        color = TextCyan,
        fontSize = 15.sp,
        fontWeight = FontWeight.ExtraBold,
        fontFamily = FontFamily.Monospace,
        letterSpacing = 2.sp
      )
      Spacer(modifier = Modifier.height(2.dp))
      val stateText = when (state) {
        CoreState.IDLE -> "ONLINE"
        CoreState.LISTENING -> "LISTENING"
        CoreState.PROCESSING -> "PROCESSING"
        CoreState.SPEAKING -> "SPEAKING"
      }
      val stateColor = when (state) {
        CoreState.IDLE -> StatusGreen
        CoreState.LISTENING -> NeonCyanBright
        CoreState.PROCESSING -> NeonBlue
        CoreState.SPEAKING -> ElectricBlue
      }
      Text(
        text = stateText,
        color = stateColor,
        fontSize = 9.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace,
        letterSpacing = 1.sp
      )
    }
  }
}
