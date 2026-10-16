package com.example.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.AppScreen
import com.example.data.AssistantLanguage
import com.example.data.CoreState
import com.example.ui.components.HudHeader
import com.example.ui.components.JarvisWaveform
import com.example.ui.components.hudBorder
import com.example.ui.theme.*
import com.example.viewmodel.JarvisViewModel

@Composable
fun VoiceAssistantScreen(
  viewModel: JarvisViewModel,
  onStartVoiceInput: () -> Unit,
  modifier: Modifier = Modifier
) {
  val language by viewModel.language.collectAsState()
  val coreState by viewModel.coreState.collectAsState()
  val isListening by viewModel.isListening.collectAsState()
  val speechTranscript by viewModel.speechTranscript.collectAsState()
  val isTtsEnabled by viewModel.isTtsEnabled.collectAsState()

  val isHindi = language == AssistantLanguage.HINDI

  val infiniteTransition = rememberInfiniteTransition(label = "voice_screen_pulse")
  val pulseRing by infiniteTransition.animateFloat(
    initialValue = 0.8f,
    targetValue = 1.25f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 1000, easing = LinearEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "pulse_ring"
  )

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(
        Brush.verticalGradient(
          listOf(VoidBlack, CyberDark, VoidBlack)
        )
      )
  ) {
    HudHeader(
      title = if (isHindi) stringResource(R.string.voice_assistant_hi) else stringResource(R.string.voice_assistant),
      subtitle = "VOCAL TELEMETRY INTERFACE",
      language = language,
      isTtsEnabled = isTtsEnabled,
      onLanguageToggle = { viewModel.toggleLanguage() },
      onTtsToggle = { viewModel.toggleTts() },
      showBackButton = true,
      onBackClick = { viewModel.setScreen(AppScreen.DASHBOARD) }
    )

    Column(
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 20.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      Spacer(modifier = Modifier.height(10.dp))

      Text(
        text = if (isListening) {
          if (isHindi) "सुन रहा हूँ, सर... बोलिए" else "Listening, Sir... Speak now"
        } else if (coreState == CoreState.PROCESSING) {
          if (isHindi) "संसाधित हो रहा है..." else "Processing audio telemetry..."
        } else if (coreState == CoreState.SPEAKING) {
          if (isHindi) "जार्विस बोल रहा है..." else "JARVIS Vocalizing..."
        } else {
          if (isHindi) "माइक टैप करें और बोलें" else "Tap Mic Below to Speak"
        },
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        letterSpacing = 1.sp,
        color = if (isListening) NeonCyanBright else TextCyan,
        textAlign = TextAlign.Center
      )

      Spacer(modifier = Modifier.height(24.dp))

      // Extra Large Animated Waveform
      JarvisWaveform(
        state = coreState,
        waveformSize = 280.dp,
        onClick = onStartVoiceInput
      )

      Spacer(modifier = Modifier.height(24.dp))

      // Realtime Transcript Card
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(12.dp))
          .background(CyberSlate.copy(alpha = 0.9f))
          .border(1.dp, CyberBorderBright, RoundedCornerShape(12.dp))
          .hudBorder(bracketColor = NeonCyan, bracketLength = 10.dp)
          .padding(16.dp)
      ) {
        Column {
          Text(
            text = "// SPEECH TRANSCRIPT",
            fontFamily = FontFamily.Monospace,
            fontSize = 10.sp,
            color = NeonCyanDim,
            letterSpacing = 1.sp
          )
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = if (speechTranscript.isNotBlank()) {
              "\"$speechTranscript\""
            } else if (isListening) {
              if (isHindi) "[ध्वनि कैप्चर हो रही है...]" else "[Capturing audio stream...]"
            } else {
              if (isHindi) "कोई ध्वनि अभी दर्ज नहीं हुई। नीचे दिए माइक बटन पर टैप करें।" else "No voice input registered yet. Press the mic button below."
            },
            fontFamily = FontFamily.SansSerif,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            color = if (speechTranscript.isNotBlank()) NeonCyanBright else TextMuted
          )
        }
      }

      Spacer(modifier = Modifier.height(24.dp))

      // Giant Pulsing Mic Action Button
      Box(
        modifier = Modifier
          .size(76.dp)
          .clip(CircleShape)
          .background(
            if (isListening) NeonCyanBright.copy(alpha = 0.3f * pulseRing) else CyberPlate
          )
          .border(
            width = if (isListening) 3.dp else 2.dp,
            color = if (isListening) NeonCyanBright else NeonCyan,
            shape = CircleShape
          )
          .clickable(onClick = onStartVoiceInput)
          .testTag("voice_assistant_giant_mic"),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
          contentDescription = "Activate Voice Listening",
          tint = if (isListening) NeonCyanBright else NeonCyan,
          modifier = Modifier.size(36.dp)
        )
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Suggested vocal commands
      Text(
        text = if (isHindi) "आज़माएं: \"सिस्टम स्थिति बताओ\" या \"आज का मौसम क्या है\"" else "Try saying: \"System status\" or \"What is the time?\"",
        fontFamily = FontFamily.Monospace,
        fontSize = 10.sp,
        color = TextMuted,
        textAlign = TextAlign.Center
      )

      Spacer(modifier = Modifier.height(20.dp))
    }
  }
}
