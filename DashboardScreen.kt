package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Mic
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
import com.example.data.AiEngineStatus
import com.example.data.AppScreen
import com.example.data.AssistantLanguage
import com.example.ui.components.HudHeader
import com.example.ui.components.HudNavButton
import com.example.ui.components.JarvisWaveform
import com.example.ui.components.VoiceInputBar
import com.example.ui.theme.*
import com.example.viewmodel.JarvisViewModel

@Composable
fun DashboardScreen(
  viewModel: JarvisViewModel,
  onStartVoiceInput: () -> Unit,
  modifier: Modifier = Modifier
) {
  val language by viewModel.language.collectAsState()
  val coreState by viewModel.coreState.collectAsState()
  val telemetry by viewModel.telemetry.collectAsState()
  val isTtsEnabled by viewModel.isTtsEnabled.collectAsState()
  val inputText by viewModel.inputText.collectAsState()
  val isListening by viewModel.isListening.collectAsState()
  val aiEngineStatus by viewModel.aiEngineStatus.collectAsState()

  val isHindi = language == AssistantLanguage.HINDI

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(
        Brush.verticalGradient(
          listOf(VoidBlack, CyberDark, VoidBlack)
        )
      )
  ) {
    // 1. Top HUD Header
    HudHeader(
      title = "JARVIS",
      subtitle = if (isHindi) "मार्क VII प्रोटोकॉल" else "MARK VII // SYSTEM ONLINE",
      language = language,
      isTtsEnabled = isTtsEnabled,
      onLanguageToggle = { viewModel.toggleLanguage() },
      onTtsToggle = { viewModel.toggleTts() },
      showBackButton = false
    )

    // Scrollable Central HUD Dashboard Content
    Column(
      modifier = Modifier
        .weight(1f)
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 16.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Spacer(modifier = Modifier.height(6.dp))

      // Status Pill: SYSTEM ONLINE
      Row(
        modifier = Modifier
          .clip(RoundedCornerShape(20.dp))
          .background(CyberSlate)
          .border(1.dp, CyberBorderBright, RoundedCornerShape(20.dp))
          .padding(horizontal = 14.dp, vertical = 6.dp)
          .testTag("status_system_online_pill"),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .size(8.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(StatusGreen)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = if (isHindi) stringResource(R.string.system_online_hi) else stringResource(R.string.system_online),
          fontFamily = FontFamily.Monospace,
          fontWeight = FontWeight.Bold,
          fontSize = 11.sp,
          letterSpacing = 1.5.sp,
          color = StatusGreen
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "•",
          color = TextMuted,
          fontSize = 10.sp
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = if (aiEngineStatus == AiEngineStatus.GEMINI_ONLINE) "GEMINI 3.5 FLASH" else "CORE PROTOCOL",
          fontFamily = FontFamily.Monospace,
          fontWeight = FontWeight.SemiBold,
          fontSize = 10.sp,
          color = if (aiEngineStatus == AiEngineStatus.GEMINI_ONLINE) NeonCyanBright else StatusWarning
        )
      }

      Spacer(modifier = Modifier.height(18.dp))

      // 2. Large Animated Circular AI Waveform in Center
      JarvisWaveform(
        state = coreState,
        waveformSize = 240.dp,
        onClick = {
          onStartVoiceInput()
        }
      )

      Spacer(modifier = Modifier.height(10.dp))

      Text(
        text = if (isHindi) "टैप करें या बोलें" else "Tap Arc Reactor or Mic to Speak",
        fontFamily = FontFamily.Monospace,
        fontSize = 10.sp,
        color = TextMuted,
        letterSpacing = 1.sp
      )

      Spacer(modifier = Modifier.height(16.dp))

      // 3. Futuristic Telemetry Ribbon (CPU, Memory, Arc Core, Battery)
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(8.dp))
          .background(CyberPlate.copy(alpha = 0.7f))
          .border(1.dp, CyberBorder, RoundedCornerShape(8.dp))
          .padding(vertical = 8.dp, horizontal = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        TelemetryItem(label = "CPU", value = "${telemetry.cpuPercent}%", color = NeonCyan)
        TelemetryItem(label = "RAM", value = "${telemetry.ramUsedMb}M", color = NeonBlue)
        TelemetryItem(label = "ARC CORE", value = "${telemetry.arcReactorEnergy}%", color = NeonCyanBright)
        TelemetryItem(label = "POWER", value = "${telemetry.batteryPercent}%", color = StatusGreen)
      }

      Spacer(modifier = Modifier.height(20.dp))

      // 4. Primary Feature Grid: AI Chat, Voice Assistant, Image Generation, Weather, Tools
      Text(
        text = if (isHindi) "// मुख्य प्रणालियाँ" else "// CORE INTERFACES",
        fontFamily = FontFamily.Monospace,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.5.sp,
        color = NeonCyanDim,
        modifier = Modifier.fillMaxWidth()
      )

      Spacer(modifier = Modifier.height(10.dp))

      // Row 1: AI Chat & Voice Assistant
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        HudNavButton(
          title = if (isHindi) stringResource(R.string.ai_chat_hi) else stringResource(R.string.ai_chat),
          subtitle = if (isHindi) "संदेश व संवाद" else "Neural Dialogue",
          icon = Icons.AutoMirrored.Filled.Chat,
          testTag = "nav_ai_chat_button",
          onClick = { viewModel.setScreen(AppScreen.CHAT) },
          modifier = Modifier.weight(1f)
        )
        HudNavButton(
          title = if (isHindi) stringResource(R.string.voice_assistant_hi) else stringResource(R.string.voice_assistant),
          subtitle = if (isHindi) "ध्वनि आदेश" else "Vocal Interface",
          icon = Icons.Default.Mic,
          testTag = "nav_voice_assistant_button",
          onClick = { viewModel.setScreen(AppScreen.VOICE) },
          modifier = Modifier.weight(1f)
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Row 2: Image Gen & Weather
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        HudNavButton(
          title = if (isHindi) stringResource(R.string.image_generation_hi) else stringResource(R.string.image_generation),
          subtitle = if (isHindi) "होलोग्राफिक खाका" else "Schematic HUD",
          icon = Icons.Default.AutoAwesome,
          testTag = "nav_image_gen_button",
          onClick = { viewModel.setScreen(AppScreen.IMAGE_GEN) },
          modifier = Modifier.weight(1f)
        )
        HudNavButton(
          title = if (isHindi) stringResource(R.string.weather_hi) else stringResource(R.string.weather),
          subtitle = if (isHindi) "वायुमंडलीय सेंसर" else "Atmosphere Telemetry",
          icon = Icons.Default.Cloud,
          testTag = "nav_weather_button",
          onClick = { viewModel.setScreen(AppScreen.WEATHER) },
          modifier = Modifier.weight(1f)
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Row 3: Tools & Diagnostics
      HudNavButton(
        title = if (isHindi) stringResource(R.string.tools_hi) else stringResource(R.string.tools),
        subtitle = if (isHindi) "डायग्नोस्टिक्स, बैटरी व टॉर्च" else "Hardware Diagnostics & Sensors",
        icon = Icons.Default.Build,
        testTag = "nav_tools_button",
        onClick = { viewModel.setScreen(AppScreen.TOOLS) },
        modifier = Modifier.fillMaxWidth()
      )

      Spacer(modifier = Modifier.height(16.dp))
    }

    // 5. Chat Input Box & Microphone Button
    VoiceInputBar(
      text = inputText,
      onTextChange = { viewModel.setInputText(it) },
      onSend = { query ->
        viewModel.sendMessage(query)
        viewModel.setScreen(AppScreen.CHAT)
      },
      onMicClick = onStartVoiceInput,
      isListening = isListening,
      placeholder = if (isHindi) stringResource(R.string.input_placeholder_hi) else stringResource(R.string.input_placeholder),
      modifier = Modifier
        .background(CyberDark.copy(alpha = 0.98f))
        .border(width = 1.dp, color = CyberBorder, shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
        .padding(bottom = 8.dp)
    )
  }
}

@Composable
private fun TelemetryItem(label: String, value: String, color: androidx.compose.ui.graphics.Color) {
  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Text(
      text = label,
      fontFamily = FontFamily.Monospace,
      fontSize = 9.sp,
      color = TextMuted,
      letterSpacing = 1.sp
    )
    Text(
      text = value,
      fontFamily = FontFamily.Monospace,
      fontWeight = FontWeight.Bold,
      fontSize = 12.sp,
      color = color
    )
  }
}
