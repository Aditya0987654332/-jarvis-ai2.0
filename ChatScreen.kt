package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.AiEngineStatus
import com.example.data.AppScreen
import com.example.data.AssistantLanguage
import com.example.data.ChatMessage
import com.example.data.Sender
import com.example.ui.components.HudHeader
import com.example.ui.components.VoiceInputBar
import com.example.ui.components.hudBorder
import com.example.ui.theme.*
import com.example.viewmodel.JarvisViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChatScreen(
  viewModel: JarvisViewModel,
  onStartVoiceInput: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val messages by viewModel.messages.collectAsState()
  val language by viewModel.language.collectAsState()
  val isTtsEnabled by viewModel.isTtsEnabled.collectAsState()
  val inputText by viewModel.inputText.collectAsState()
  val isListening by viewModel.isListening.collectAsState()
  val aiEngineStatus by viewModel.aiEngineStatus.collectAsState()

  val isHindi = language == AssistantLanguage.HINDI
  var showClearConfirmDialog by remember { mutableStateOf(false) }
  var showConfigBanner by remember { mutableStateOf(aiEngineStatus != AiEngineStatus.GEMINI_ONLINE) }

  val listState = rememberLazyListState()

  LaunchedEffect(messages.size) {
    if (messages.isNotEmpty()) {
      listState.animateScrollToItem(messages.size - 1)
    }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(VoidBlack)
  ) {
    // Top Bar with Back and Clear Buttons
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .background(CyberDark)
        .padding(end = 8.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Box(modifier = Modifier.weight(1f)) {
        HudHeader(
          title = if (isHindi) stringResource(R.string.ai_chat_hi) else stringResource(R.string.ai_chat),
          subtitle = if (aiEngineStatus == AiEngineStatus.GEMINI_ONLINE) "GEMINI NEURAL MESH ONLINE" else "CORE PROTOCOL [LOCAL MODE]",
          language = language,
          isTtsEnabled = isTtsEnabled,
          onLanguageToggle = { viewModel.toggleLanguage() },
          onTtsToggle = { viewModel.toggleTts() },
          showBackButton = true,
          onBackClick = { viewModel.setScreen(AppScreen.DASHBOARD) }
        )
      }

      // Clear Chat History Button
      IconButton(
        onClick = { showClearConfirmDialog = true },
        modifier = Modifier
          .size(36.dp)
          .clip(CircleShape)
          .background(CyberSlate)
          .border(1.dp, CyberBorder, CircleShape)
          .testTag("clear_chat_button")
      ) {
        Icon(
          imageVector = Icons.Default.DeleteSweep,
          contentDescription = "Clear History",
          tint = StatusWarning,
          modifier = Modifier.size(20.dp)
        )
      }
    }

    // AI Configuration Transparency Banner (when Gemini Key is not set)
    AnimatedVisibility(visible = showConfigBanner && aiEngineStatus != AiEngineStatus.GEMINI_ONLINE) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .background(CyberPlate.copy(alpha = 0.9f))
          .border(1.dp, StatusWarning.copy(alpha = 0.5f))
          .padding(horizontal = 14.dp, vertical = 8.dp)
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.Info,
            contentDescription = "AI Status Info",
            tint = StatusWarning,
            modifier = Modifier.size(20.dp)
          )
          Spacer(modifier = Modifier.width(10.dp))
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = if (isHindi) "जेमिनी एआई अनकॉन्फ़िगर है (लोकल मोड सक्रिय)" else "Gemini API Not Configured (Running Core Protocol)",
              fontFamily = FontFamily.Monospace,
              fontWeight = FontWeight.Bold,
              fontSize = 11.sp,
              color = StatusWarning
            )
            Text(
              text = if (isHindi)
                "बिना एपीआई की के ऑन-डिवाइस कमांड्स उपलब्ध हैं। पूर्ण एआई के लिए Secrets में GEMINI_API_KEY जोड़ें।"
              else
                "Core device protocols active. Responses are handled locally. Set GEMINI_API_KEY in Secrets panel to unlock full LLM reasoning.",
              fontFamily = FontFamily.SansSerif,
              fontSize = 10.sp,
              color = TextCyanDim
            )
          }
          IconButton(
            onClick = { showConfigBanner = false },
            modifier = Modifier.size(28.dp)
          ) {
            Text("✕", color = TextMuted, fontSize = 12.sp)
          }
        }
      }
    }

    // Suggestion Prompt Chips
    val suggestions = if (isHindi) {
      listOf("सिस्टम स्थिति", "वर्तमान समय", "आज का मौसम", "बैटरी स्तर", "25 * 14 की गणना")
    } else {
      listOf("System Status", "What time is it?", "Weather in Tokyo", "Battery Level", "Calculate 150 * 8")
    }

    FlowRow(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 6.dp),
      horizontalArrangement = Arrangement.spacedBy(6.dp),
      verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
      suggestions.forEach { chipText ->
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(CyberSlate)
            .border(1.dp, CyberBorder, RoundedCornerShape(12.dp))
            .clickable { viewModel.sendMessage(chipText) }
            .padding(horizontal = 10.dp, vertical = 4.dp)
            .testTag("prompt_chip_${chipText.take(6)}")
        ) {
          Text(
            text = chipText,
            color = NeonCyan,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace
          )
        }
      }
    }

    // Chat Message History List
    LazyColumn(
      state = listState,
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth()
        .padding(horizontal = 12.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      items(items = messages, key = { it.id }) { msg ->
        ChatMessageBubble(
          message = msg,
          onCopy = {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("JARVIS Message", msg.text))
            Toast.makeText(context, if (isHindi) "संदेश कॉपी किया गया" else "Copied to clipboard", Toast.LENGTH_SHORT).show()
          },
          onSpeak = {
            viewModel.speakOut(msg.text)
          }
        )
      }
      item {
        Spacer(modifier = Modifier.height(8.dp))
      }
    }

    // Bottom Input Bar
    VoiceInputBar(
      text = inputText,
      onTextChange = { viewModel.setInputText(it) },
      onSend = { query -> viewModel.sendMessage(query) },
      onMicClick = onStartVoiceInput,
      isListening = isListening,
      placeholder = if (isHindi) stringResource(R.string.input_placeholder_hi) else stringResource(R.string.input_placeholder),
      modifier = Modifier
        .background(CyberDark)
        .border(1.dp, CyberBorder, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
        .padding(bottom = 6.dp)
    )
  }

  // Clear Chat History Confirmation Dialog
  if (showClearConfirmDialog) {
    AlertDialog(
      onDismissRequest = { showClearConfirmDialog = false },
      containerColor = CyberSlate,
      titleContentColor = NeonCyan,
      textContentColor = TextCyan,
      title = {
        Text(
          text = if (isHindi) "चैट साफ़ करें?" else "Clear Chat History?",
          fontFamily = FontFamily.Monospace,
          fontWeight = FontWeight.Bold
        )
      },
      text = {
        Text(
          text = if (isHindi) "क्या आप सभी पिछले संदेशों को हटाना चाहते हैं?" else "Are you sure you want to purge all conversation logs from memory?",
          fontFamily = FontFamily.SansSerif,
          fontSize = 13.sp
        )
      },
      confirmButton = {
        Button(
          onClick = {
            viewModel.clearChatHistory()
            showClearConfirmDialog = false
          },
          colors = ButtonDefaults.buttonColors(containerColor = StatusWarning),
          modifier = Modifier.testTag("confirm_clear_button")
        ) {
          Text(text = if (isHindi) "साफ़ करें" else "Purge Logs", color = VoidBlack, fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        OutlinedButton(
          onClick = { showClearConfirmDialog = false },
          colors = ButtonDefaults.outlinedButtonColors(contentColor = TextCyanDim)
        ) {
          Text(text = if (isHindi) "रद्द करें" else "Cancel")
        }
      }
    )
  }
}

@Composable
private fun ChatMessageBubble(
  message: ChatMessage,
  onCopy: () -> Unit,
  onSpeak: () -> Unit
) {
  val isUser = message.sender == Sender.USER
  val timeFormatted = remember(message.timestamp) {
    SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(message.timestamp))
  }

  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth(0.88f)
        .clip(
          RoundedCornerShape(
            topStart = 14.dp,
            topEnd = 14.dp,
            bottomStart = if (isUser) 14.dp else 2.dp,
            bottomEnd = if (isUser) 2.dp else 14.dp
          )
        )
        .background(
          if (isUser) {
            Brush.verticalGradient(listOf(NeonBlue.copy(alpha = 0.25f), CyberSlate))
          } else {
            Brush.verticalGradient(listOf(CyberPlate, CyberDark))
          }
        )
        .border(
          width = 1.dp,
          color = if (isUser) ElectricBlue.copy(alpha = 0.5f) else CyberBorderBright,
          shape = RoundedCornerShape(
            topStart = 14.dp,
            topEnd = 14.dp,
            bottomStart = if (isUser) 14.dp else 2.dp,
            bottomEnd = if (isUser) 2.dp else 14.dp
          )
        )
        .hudBorder(
          bracketColor = if (isUser) ElectricBlue else NeonCyan,
          bracketLength = 6.dp
        )
        .padding(12.dp)
    ) {
      // Header: Sender Label, Model/Core tag, Timestamp
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = if (isUser) "USER // PROTOCOL" else "JARVIS // ASSISTANT",
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
            letterSpacing = 1.sp,
            color = if (isUser) ElectricBlue else NeonCyanBright
          )
          if (!isUser && message.modelTag.isNotBlank()) {
            Spacer(modifier = Modifier.width(6.dp))
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(CyberSlate)
                .padding(horizontal = 4.dp, vertical = 1.dp)
            ) {
              Text(
                text = message.modelTag,
                fontSize = 8.sp,
                fontFamily = FontFamily.Monospace,
                color = if (message.isCoreProtocol) StatusWarning else StatusGreen
              )
            }
          }
        }
        Text(
          text = timeFormatted,
          fontFamily = FontFamily.Monospace,
          fontSize = 9.sp,
          color = TextMuted
        )
      }

      Spacer(modifier = Modifier.height(6.dp))

      // Message Content Body
      Text(
        text = message.text,
        fontFamily = FontFamily.SansSerif,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        color = if (isUser) TextWhite else TextCyan
      )

      Spacer(modifier = Modifier.height(6.dp))

      // Quick action icons: Copy & Speak Out
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(
          onClick = onCopy,
          modifier = Modifier.size(24.dp)
        ) {
          Icon(
            imageVector = Icons.Default.ContentCopy,
            contentDescription = "Copy message",
            tint = TextMuted,
            modifier = Modifier.size(14.dp)
          )
        }
        if (!isUser) {
          Spacer(modifier = Modifier.width(8.dp))
          IconButton(
            onClick = onSpeak,
            modifier = Modifier.size(24.dp)
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.VolumeUp,
              contentDescription = "Speak aloud",
              tint = NeonCyanDim,
              modifier = Modifier.size(16.dp)
            )
          }
        }
      }
    }
  }
}
