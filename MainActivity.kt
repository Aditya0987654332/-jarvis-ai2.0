package com.example

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.example.data.AppScreen
import com.example.data.AssistantLanguage
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.ImageGenScreen
import com.example.ui.screens.ToolsScreen
import com.example.ui.screens.VoiceAssistantScreen
import com.example.ui.screens.WeatherScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.VoidBlack
import com.example.viewmodel.JarvisViewModel
import java.util.Locale

class MainActivity : ComponentActivity() {

  private val jarvisViewModel: JarvisViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        JarvisApp(viewModel = jarvisViewModel)
      }
    }
  }
}

@Composable
fun JarvisApp(viewModel: JarvisViewModel) {
  val context = LocalContext.current
  val currentScreen by viewModel.currentScreen.collectAsState()
  val language by viewModel.language.collectAsState()

  // Back button handling: return to dashboard if on sub-screen
  BackHandler(enabled = currentScreen != AppScreen.DASHBOARD) {
    viewModel.setScreen(AppScreen.DASHBOARD)
  }

  // Speech Recognition Activity Launcher
  val speechRecognitionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.StartActivityForResult()
  ) { result ->
    if (result.resultCode == Activity.RESULT_OK && result.data != null) {
      val spokenMatches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
      val recognizedText = spokenMatches?.firstOrNull() ?: ""
      viewModel.handleSpeechRecognitionResult(recognizedText)
    } else {
      viewModel.setListening(false)
    }
  }

  fun triggerSpeechRecognizer() {
    try {
      viewModel.setListening(true)
      val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(
          RecognizerIntent.EXTRA_LANGUAGE_MODEL,
          RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        val langTag = if (language == AssistantLanguage.HINDI) "hi-IN" else "en-US"
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, langTag)
        putExtra(RecognizerIntent.EXTRA_PROMPT, if (language == AssistantLanguage.HINDI) "जार्विस से बोलें..." else "Speak to JARVIS...")
      }
      speechRecognitionLauncher.launch(intent)
    } catch (_: Exception) {
      viewModel.setListening(false)
      Toast.makeText(
        context,
        if (language == AssistantLanguage.HINDI) "आवाज़ पहचान समर्थित नहीं है।" else "Speech recognition not available on this device.",
        Toast.LENGTH_SHORT
      ).show()
    }
  }

  // Audio Permission Request Launcher
  val recordAudioPermissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
  ) { isGranted ->
    if (isGranted) {
      triggerSpeechRecognizer()
    } else {
      viewModel.setListening(false)
      Toast.makeText(
        context,
        if (language == AssistantLanguage.HINDI) "माइक्रोफ़ोन अनुमति आवश्यक है।" else "Microphone permission required for voice input.",
        Toast.LENGTH_SHORT
      ).show()
    }
  }

  fun startVoiceInput() {
    val permissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
    if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
      triggerSpeechRecognizer()
    } else {
      recordAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }
  }

  Scaffold(
    modifier = Modifier
      .fillMaxSize()
      .background(VoidBlack),
    containerColor = VoidBlack
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      when (currentScreen) {
        AppScreen.DASHBOARD -> DashboardScreen(
          viewModel = viewModel,
          onStartVoiceInput = { startVoiceInput() }
        )
        AppScreen.CHAT -> ChatScreen(
          viewModel = viewModel,
          onStartVoiceInput = { startVoiceInput() }
        )
        AppScreen.VOICE -> VoiceAssistantScreen(
          viewModel = viewModel,
          onStartVoiceInput = { startVoiceInput() }
        )
        AppScreen.IMAGE_GEN -> ImageGenScreen(
          viewModel = viewModel
        )
        AppScreen.WEATHER -> WeatherScreen(
          viewModel = viewModel
        )
        AppScreen.TOOLS -> ToolsScreen(
          viewModel = viewModel
        )
      }
    }
  }
}
