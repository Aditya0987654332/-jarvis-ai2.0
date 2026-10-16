package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.speech.tts.TextToSpeech
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AiEngineStatus
import com.example.data.AppScreen
import com.example.data.AssistantLanguage
import com.example.data.ChatMessage
import com.example.data.CoreState
import com.example.data.GeminiResult
import com.example.data.GeminiService
import com.example.data.GeneratedSchematic
import com.example.data.ImageGenStyle
import com.example.data.JarvisCoreProtocols
import com.example.data.Sender
import com.example.data.TelemetryData
import com.example.data.WeatherData
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale

class JarvisViewModel(application: Application) : AndroidViewModel(application), TextToSpeech.OnInitListener {

  private val geminiService = GeminiService()
  private var tts: TextToSpeech? = null
  private var isTtsInitialized = false

  private val _currentScreen = MutableStateFlow(AppScreen.DASHBOARD)
  val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

  private val _language = MutableStateFlow(AssistantLanguage.ENGLISH)
  val language: StateFlow<AssistantLanguage> = _language.asStateFlow()

  private val _coreState = MutableStateFlow(CoreState.IDLE)
  val coreState: StateFlow<CoreState> = _coreState.asStateFlow()

  private val _aiEngineStatus = MutableStateFlow(
    if (geminiService.isKeyConfigured()) AiEngineStatus.GEMINI_ONLINE else AiEngineStatus.CORE_PROTOCOL_OFFLINE
  )
  val aiEngineStatus: StateFlow<AiEngineStatus> = _aiEngineStatus.asStateFlow()

  private val _isTtsEnabled = MutableStateFlow(true)
  val isTtsEnabled: StateFlow<Boolean> = _isTtsEnabled.asStateFlow()

  private val _inputText = MutableStateFlow("")
  val inputText: StateFlow<String> = _inputText.asStateFlow()

  private val _isListening = MutableStateFlow(false)
  val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

  private val _speechTranscript = MutableStateFlow("")
  val speechTranscript: StateFlow<String> = _speechTranscript.asStateFlow()

  private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
  val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

  private val _telemetry = MutableStateFlow(TelemetryData())
  val telemetry: StateFlow<TelemetryData> = _telemetry.asStateFlow()

  private val _weatherList = MutableStateFlow(
    listOf(
      WeatherData("Stark Tower (Malibu)", "US", 24, "Clear Sky", "साफ़ आसमान", 44, 18, 1014, 6),
      WeatherData("New Delhi", "IN", 29, "Hazy Cyber Sun", "धुंध भरा सूरज", 52, 12, 1011, 8),
      WeatherData("Tokyo", "JP", 19, "Neon Rain", "हल्की बारिश", 76, 22, 1008, 3),
      WeatherData("London", "UK", 15, "Atmospheric Fog", "वायुमंडलीय कोहरा", 82, 16, 1016, 2),
      WeatherData("New York", "US", 21, "Partly Cloudy", "आंशिक बादल", 58, 20, 1013, 5)
    )
  )
  val weatherList: StateFlow<List<WeatherData>> = _weatherList.asStateFlow()

  private val _selectedWeatherIndex = MutableStateFlow(0)
  val selectedWeatherIndex: StateFlow<Int> = _selectedWeatherIndex.asStateFlow()

  private val _schematics = MutableStateFlow<List<GeneratedSchematic>>(emptyList())
  val schematics: StateFlow<List<GeneratedSchematic>> = _schematics.asStateFlow()

  private val _imagePrompt = MutableStateFlow("")
  val imagePrompt: StateFlow<String> = _imagePrompt.asStateFlow()

  private val _selectedImageStyle = MutableStateFlow(ImageGenStyle.HOLOGRAPHIC)
  val selectedImageStyle: StateFlow<ImageGenStyle> = _selectedImageStyle.asStateFlow()

  private val _isGeneratingImage = MutableStateFlow(false)
  val isGeneratingImage: StateFlow<Boolean> = _isGeneratingImage.asStateFlow()

  private val _diagnosticScanProgress = MutableStateFlow<Float?>(null)
  val diagnosticScanProgress: StateFlow<Float?> = _diagnosticScanProgress.asStateFlow()

  private val _isTorchOn = MutableStateFlow(false)
  val isTorchOn: StateFlow<Boolean> = _isTorchOn.asStateFlow()

  init {
    try {
      tts = TextToSpeech(application.applicationContext, this)
    } catch (_: Exception) {
      // Graceful fallback if TTS service is unavailable in environment
    }
    refreshDeviceTelemetry()
    initDefaultWelcomeMessage()
    initSampleSchematics()
  }

  override fun onInit(status: Int) {
    if (status == TextToSpeech.SUCCESS) {
      isTtsInitialized = true
      updateTtsLanguage(_language.value)
    }
  }

  private fun updateTtsLanguage(lang: AssistantLanguage) {
    if (!isTtsInitialized) return
    try {
      val result = tts?.setLanguage(lang.locale)
      if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
        tts?.setLanguage(Locale.US)
      }
    } catch (_: Exception) {}
  }

  fun setScreen(screen: AppScreen) {
    _currentScreen.value = screen
    if (screen == AppScreen.VOICE) {
      _coreState.value = CoreState.LISTENING
    } else if (_coreState.value == CoreState.LISTENING) {
      _coreState.value = CoreState.IDLE
    }
  }

  fun toggleLanguage() {
    val next = if (_language.value == AssistantLanguage.ENGLISH) AssistantLanguage.HINDI else AssistantLanguage.ENGLISH
    _language.value = next
    updateTtsLanguage(next)
  }

  fun toggleTts() {
    _isTtsEnabled.update { !it }
    if (!_isTtsEnabled.value) {
      tts?.stop()
    }
  }

  fun setInputText(text: String) {
    _inputText.value = text
  }

  fun setImagePrompt(prompt: String) {
    _imagePrompt.value = prompt
  }

  fun setImageStyle(style: ImageGenStyle) {
    _selectedImageStyle.value = style
  }

  fun setSelectedWeatherIndex(index: Int) {
    _selectedWeatherIndex.value = index
  }

  fun toggleTorch() {
    _isTorchOn.update { !it }
  }

  private fun initDefaultWelcomeMessage() {
    val isConfigured = geminiService.isKeyConfigured()
    val welcomeText = if (_language.value == AssistantLanguage.HINDI) {
      if (isConfigured) {
        "सिस्टम ऑनलाइन, सर। जेमिनी न्यूरल कोर सक्रिय है। मैं आपकी क्या सेवा करूँ?"
      } else {
        "सिस्टम ऑनलाइन, सर। जार्विस ऑन-डिवाइस कोर प्रोटोकॉल सक्रिय है। क्लाउड एआई (Gemini) जोड़ने के लिए AI Studio Secrets में GEMINI_API_KEY दर्ज करें।"
      }
    } else {
      if (isConfigured) {
        "All systems online, Sir. Gemini neural network synchronized. Standing by for your instructions."
      } else {
        "All systems online, Sir. Running on-device JARVIS Core Protocols. Configure GEMINI_API_KEY in AI Studio Secrets to unlock cloud neural reasoning."
      }
    }

    _messages.value = listOf(
      ChatMessage(
        sender = Sender.JARVIS,
        text = welcomeText,
        isCoreProtocol = !isConfigured,
        modelTag = if (isConfigured) "gemini-3.5-flash" else "JARVIS Core"
      )
    )
  }

  private fun initSampleSchematics() {
    _schematics.value = listOf(
      GeneratedSchematic(
        prompt = "Arc Reactor Core Mark VII",
        style = ImageGenStyle.ARC_REACTOR,
        wireframePattern = 1
      ),
      GeneratedSchematic(
        prompt = "Stark Quantum Telemetry HUD",
        style = ImageGenStyle.HOLOGRAPHIC,
        wireframePattern = 2
      ),
      GeneratedSchematic(
        prompt = "Titanium Armor Exoskeleton",
        style = ImageGenStyle.SUIT_ARMOR,
        wireframePattern = 3
      )
    )
  }

  fun refreshDeviceTelemetry() {
    val context = getApplication<Application>().applicationContext
    val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
    val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, 92) ?: 92
    val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, 100) ?: 100
    val batteryPct = ((level / scale.toFloat()) * 100).toInt()
    val status = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
    val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
    val tempTenths = batteryIntent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 314) ?: 314
    val batteryTemp = tempTenths / 10.0f

    val runtime = Runtime.getRuntime()
    val usedMemoryMb = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024)
    val maxMemoryMb = runtime.maxMemory() / (1024 * 1024)

    _telemetry.value = TelemetryData(
      cpuPercent = (12..18).random(),
      ramUsedMb = usedMemoryMb,
      ramTotalMb = maxMemoryMb,
      batteryPercent = batteryPct,
      batteryTempC = batteryTemp,
      isCharging = isCharging,
      arcReactorEnergy = 98,
      protocolVersion = "MARK VII.4",
      securityLevel = "MAXIMUM"
    )
  }

  fun clearChatHistory() {
    _messages.value = emptyList()
    initDefaultWelcomeMessage()
  }

  fun sendMessage(promptText: String) {
    val text = promptText.trim()
    if (text.isEmpty()) return

    _inputText.value = ""
    _speechTranscript.value = ""

    val userMessage = ChatMessage(
      sender = Sender.USER,
      text = text
    )
    _messages.update { it + userMessage }

    _coreState.value = CoreState.PROCESSING

    viewModelScope.launch {
      if (geminiService.isKeyConfigured()) {
        val result = geminiService.generateResponse(text, _language.value, _messages.value)
        when (result) {
          is GeminiResult.Success -> {
            val jarvisMsg = ChatMessage(
              sender = Sender.JARVIS,
              text = result.text,
              isCoreProtocol = false,
              modelTag = result.model
            )
            _messages.update { it + jarvisMsg }
            _coreState.value = CoreState.SPEAKING
            speakOut(result.text)
          }
          is GeminiResult.NotConfigured -> {
            val localReply = JarvisCoreProtocols.processLocalCommand(text, _language.value, _telemetry.value)
            val jarvisMsg = ChatMessage(
              sender = Sender.JARVIS,
              text = localReply,
              isCoreProtocol = true,
              modelTag = "JARVIS Core [Local]"
            )
            _messages.update { it + jarvisMsg }
            _coreState.value = CoreState.SPEAKING
            speakOut(localReply)
          }
          is GeminiResult.Failure -> {
            val errorMsg = ChatMessage(
              sender = Sender.SYSTEM,
              text = "⚠️ Gemini API Error: ${result.error}\nFalling back to JARVIS Core Protocol.",
              isCoreProtocol = true,
              modelTag = "System Diagnostic"
            )
            val fallbackReply = JarvisCoreProtocols.processLocalCommand(text, _language.value, _telemetry.value)
            val jarvisMsg = ChatMessage(
              sender = Sender.JARVIS,
              text = fallbackReply,
              isCoreProtocol = true,
              modelTag = "JARVIS Core [Fallback]"
            )
            _messages.update { it + errorMsg + jarvisMsg }
            _coreState.value = CoreState.SPEAKING
            speakOut(fallbackReply)
          }
        }
      } else {
        delay(400) // Brief futuristic processing latency
        val localReply = JarvisCoreProtocols.processLocalCommand(text, _language.value, _telemetry.value)
        val jarvisMsg = ChatMessage(
          sender = Sender.JARVIS,
          text = localReply,
          isCoreProtocol = true,
          modelTag = "JARVIS Core [Local]"
        )
        _messages.update { it + jarvisMsg }
        _coreState.value = CoreState.SPEAKING
        speakOut(localReply)
      }

      // Return to idle state after speaking delay
      delay(3000)
      if (_coreState.value == CoreState.SPEAKING) {
        _coreState.value = CoreState.IDLE
      }
    }
  }

  fun handleSpeechRecognitionResult(spokenText: String) {
    _isListening.value = false
    _speechTranscript.value = spokenText
    if (spokenText.isNotBlank()) {
      sendMessage(spokenText)
    } else {
      _coreState.value = CoreState.IDLE
    }
  }

  fun setListening(listening: Boolean) {
    _isListening.value = listening
    _coreState.value = if (listening) CoreState.LISTENING else CoreState.IDLE
  }

  fun generateSchematic() {
    val prompt = _imagePrompt.value.trim().ifEmpty {
      when (_selectedImageStyle.value) {
        ImageGenStyle.HOLOGRAPHIC -> "Holographic Arc Reactor HUD"
        ImageGenStyle.BLUEPRINT -> "Mark VII CAD Vector Blueprint"
        ImageGenStyle.CYBERPUNK -> "Cyberpunk Neon Power Matrix"
        ImageGenStyle.NANOTECH -> "Nano-Particle Energy Conduit"
        ImageGenStyle.ARC_REACTOR -> "Arc Reactor Core Mark VII"
        ImageGenStyle.SUIT_ARMOR -> "Mark VII Exoskeleton Titanium Mesh"
      }
    }

    _isGeneratingImage.value = true
    _coreState.value = CoreState.PROCESSING

    viewModelScope.launch {
      delay(1200) // High-tech computation delay
      val newSchematic = GeneratedSchematic(
        prompt = prompt,
        style = _selectedImageStyle.value,
        wireframePattern = (1..4).random()
      )
      _schematics.update { listOf(newSchematic) + it }
      _isGeneratingImage.value = false
      _coreState.value = CoreState.IDLE
      _imagePrompt.value = ""
    }
  }

  fun runSystemDiagnostics() {
    viewModelScope.launch {
      _diagnosticScanProgress.value = 0f
      _coreState.value = CoreState.PROCESSING
      for (i in 1..10) {
        delay(200)
        _diagnosticScanProgress.value = i / 10f
      }
      refreshDeviceTelemetry()
      _diagnosticScanProgress.value = null
      _coreState.value = CoreState.IDLE

      val report = if (_language.value == AssistantLanguage.HINDI) {
        "डायग्नोस्टिक पूर्ण, सर। सभी आंतरिक प्रणालियाँ 100% कार्यशील हैं।"
      } else {
        "Diagnostics complete, Sir. All operational nodes are at 100% capacity."
      }
      speakOut(report)
    }
  }

  fun speakOut(text: String) {
    if (!_isTtsEnabled.value || !isTtsInitialized) return
    try {
      // Strip markdown brackets/bullets for clean audio narration
      val cleanText = text
        .replace(Regex("\\[.*?\\]"), "")
        .replace("*", "")
        .replace("#", "")
        .replace("•", "")
        .trim()
      tts?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, "jarvis_speech_id")
    } catch (_: Exception) {}
  }

  override fun onCleared() {
    super.onCleared()
    try {
      tts?.stop()
      tts?.shutdown()
    } catch (_: Exception) {}
  }
}
