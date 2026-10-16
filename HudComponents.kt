package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AssistantLanguage
import com.example.ui.theme.*

/**
 * Draws high-tech HUD corner brackets and glowing lines
 */
fun Modifier.hudBorder(
  bracketColor: Color = NeonCyan,
  bracketLength: Dp = 10.dp,
  strokeWidth: Dp = 1.5.dp
): Modifier = this.drawBehind {
  val strokePx = strokeWidth.toPx()
  val bracketPx = bracketLength.toPx()
  val w = size.width
  val h = size.height

  // Top-Left bracket
  drawLine(bracketColor, Offset(0f, 0f), Offset(bracketPx, 0f), strokePx)
  drawLine(bracketColor, Offset(0f, 0f), Offset(0f, bracketPx), strokePx)

  // Top-Right bracket
  drawLine(bracketColor, Offset(w - bracketPx, 0f), Offset(w, 0f), strokePx)
  drawLine(bracketColor, Offset(w, 0f), Offset(w, bracketPx), strokePx)

  // Bottom-Left bracket
  drawLine(bracketColor, Offset(0f, h), Offset(bracketPx, h), strokePx)
  drawLine(bracketColor, Offset(0f, h - bracketPx), Offset(0f, h), strokePx)

  // Bottom-Right bracket
  drawLine(bracketColor, Offset(w - bracketPx, h), Offset(w, h), strokePx)
  drawLine(bracketColor, Offset(w, h - bracketPx), Offset(w, h), strokePx)
}

@Composable
fun HudContainer(
  modifier: Modifier = Modifier,
  content: @Composable () -> Unit
) {
  Box(
    modifier = modifier
      .background(
        brush = Brush.verticalGradient(listOf(CyberPlate.copy(alpha = 0.85f), CyberDark.copy(alpha = 0.95f))),
        shape = RoundedCornerShape(8.dp)
      )
      .border(1.dp, CyberBorder, RoundedCornerShape(8.dp))
      .hudBorder(bracketColor = NeonCyan.copy(alpha = 0.7f), bracketLength = 12.dp)
      .padding(12.dp)
  ) {
    content()
  }
}

@Composable
fun HudHeader(
  title: String = "JARVIS",
  subtitle: String = "MARK VII PROTOCOL",
  language: AssistantLanguage,
  isTtsEnabled: Boolean,
  onLanguageToggle: () -> Unit,
  onTtsToggle: () -> Unit,
  showBackButton: Boolean = false,
  onBackClick: () -> Unit = {}
) {
  val infiniteTransition = rememberInfiniteTransition(label = "hud_pulse")
  val blinkAlpha by infiniteTransition.animateFloat(
    initialValue = 0.35f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(900, easing = LinearEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "status_blink"
  )

  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 8.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      if (showBackButton) {
        IconButton(
          onClick = onBackClick,
          modifier = Modifier
            .size(36.dp)
            .testTag("hud_back_button")
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
            tint = NeonCyan
          )
        }
        Spacer(modifier = Modifier.width(6.dp))
      }

      Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = title,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 18.sp,
            letterSpacing = 2.sp,
            color = TextCyan
          )
          Spacer(modifier = Modifier.width(8.dp))
          // Pulsing Online LED Indicator
          Box(
            modifier = Modifier
              .size(8.dp)
              .clip(CircleShape)
              .background(StatusGreen.copy(alpha = blinkAlpha))
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = if (language == AssistantLanguage.HINDI) "ऑनलाइन" else "ONLINE",
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 9.sp,
            letterSpacing = 1.sp,
            color = StatusGreen
          )
        }
        Text(
          text = subtitle,
          fontFamily = FontFamily.Monospace,
          fontSize = 9.sp,
          letterSpacing = 1.sp,
          color = NeonCyanDim
        )
      }
    }

    // Right Controls: Language Selector and TTS Mute
    Row(verticalAlignment = Alignment.CenterVertically) {
      // Language Switcher Badge (EN / HI)
      Box(
        modifier = Modifier
          .clip(RoundedCornerShape(4.dp))
          .background(CyberSlate)
          .border(1.dp, CyberBorderBright, RoundedCornerShape(4.dp))
          .clickable(onClick = onLanguageToggle)
          .padding(horizontal = 8.dp, vertical = 4.dp)
          .testTag("language_toggle_button")
      ) {
        Text(
          text = language.label + " | " + language.nativeLabel,
          fontFamily = FontFamily.Monospace,
          fontSize = 10.sp,
          fontWeight = FontWeight.Bold,
          color = NeonCyanBright
        )
      }

      Spacer(modifier = Modifier.width(8.dp))

      // TTS Audio Synthesizer Toggle
      IconButton(
        onClick = onTtsToggle,
        modifier = Modifier
          .size(34.dp)
          .clip(CircleShape)
          .background(CyberSlate)
          .border(1.dp, CyberBorder, CircleShape)
          .testTag("tts_toggle_button")
      ) {
        Icon(
          imageVector = if (isTtsEnabled) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeMute,
          contentDescription = "Text to speech toggle",
          tint = if (isTtsEnabled) NeonCyan else TextMuted,
          modifier = Modifier.size(18.dp)
        )
      }
    }
  }
}

@Composable
fun HudNavButton(
  title: String,
  subtitle: String,
  icon: ImageVector,
  testTag: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .clip(RoundedCornerShape(10.dp))
      .background(
        Brush.verticalGradient(
          listOf(CyberPlate.copy(alpha = 0.9f), CyberDark.copy(alpha = 0.95f))
        )
      )
      .border(1.dp, CyberBorder, RoundedCornerShape(10.dp))
      .hudBorder(bracketColor = NeonCyan.copy(alpha = 0.5f), bracketLength = 8.dp)
      .clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        onClick = onClick
      )
      .padding(12.dp)
      .testTag(testTag)
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Box(
        modifier = Modifier
          .size(38.dp)
          .clip(RoundedCornerShape(6.dp))
          .background(CyberSlate)
          .border(1.dp, CyberBorderBright, RoundedCornerShape(6.dp)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = icon,
          contentDescription = title,
          tint = NeonCyanBright,
          modifier = Modifier.size(22.dp)
        )
      }
      Spacer(modifier = Modifier.width(10.dp))
      Column {
        Text(
          text = title,
          fontFamily = FontFamily.Monospace,
          fontWeight = FontWeight.Bold,
          fontSize = 13.sp,
          letterSpacing = 0.5.sp,
          color = TextCyan
        )
        Text(
          text = subtitle,
          fontFamily = FontFamily.SansSerif,
          fontSize = 10.sp,
          color = TextCyanDim
        )
      }
    }
  }
}

@Composable
fun VoiceInputBar(
  text: String,
  onTextChange: (String) -> Unit,
  onSend: (String) -> Unit,
  onMicClick: () -> Unit,
  isListening: Boolean,
  placeholder: String,
  modifier: Modifier = Modifier
) {
  val infiniteTransition = rememberInfiniteTransition(label = "mic_glow")
  val micGlow by infiniteTransition.animateFloat(
    initialValue = 0.4f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(600, easing = LinearEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "mic_pulse"
  )

  Row(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 12.dp, vertical = 6.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    OutlinedTextField(
      value = text,
      onValueChange = onTextChange,
      modifier = Modifier
        .weight(1f)
        .testTag("chat_input_field"),
      placeholder = {
        Text(
          text = placeholder,
          color = TextMuted,
          fontSize = 13.sp,
          fontFamily = FontFamily.SansSerif
        )
      },
      singleLine = true,
      textStyle = androidx.compose.ui.text.TextStyle(
        color = TextCyan,
        fontFamily = FontFamily.SansSerif,
        fontSize = 14.sp
      ),
      colors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = NeonCyan,
        unfocusedBorderColor = CyberBorder,
        focusedContainerColor = CyberDark.copy(alpha = 0.95f),
        unfocusedContainerColor = CyberDark.copy(alpha = 0.85f),
        cursorColor = NeonCyanBright
      ),
      shape = RoundedCornerShape(12.dp),
      keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
      keyboardActions = KeyboardActions(
        onSend = {
          if (text.isNotBlank()) {
            onSend(text.trim())
          }
        }
      )
    )

    Spacer(modifier = Modifier.width(8.dp))

    // Microphone Voice Button
    Box(
      modifier = Modifier
        .size(46.dp)
        .clip(CircleShape)
        .background(if (isListening) NeonCyanBright.copy(alpha = micGlow * 0.4f) else CyberSlate)
        .border(
          width = if (isListening) 2.dp else 1.dp,
          color = if (isListening) NeonCyanBright else CyberBorderBright,
          shape = CircleShape
        )
        .clickable(onClick = onMicClick)
        .testTag("voice_mic_button"),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
        contentDescription = "Voice Input Microphone",
        tint = if (isListening) VoidBlack else NeonCyanBright,
        modifier = Modifier.size(24.dp)
      )
    }

    // Send Button (if text entered)
    if (text.isNotBlank()) {
      Spacer(modifier = Modifier.width(6.dp))
      Box(
        modifier = Modifier
          .size(46.dp)
          .clip(CircleShape)
          .background(NeonCyan)
          .clickable {
            onSend(text.trim())
          }
          .testTag("chat_send_button"),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.Send,
          contentDescription = "Send Message",
          tint = VoidBlack,
          modifier = Modifier.size(20.dp)
        )
      }
    }
  }
}
