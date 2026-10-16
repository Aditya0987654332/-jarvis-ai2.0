package com.example.data

import java.util.Locale
import java.util.UUID

enum class AppScreen {
  DASHBOARD,
  CHAT,
  VOICE,
  IMAGE_GEN,
  WEATHER,
  TOOLS
}

enum class AssistantLanguage(
  val code: String,
  val label: String,
  val nativeLabel: String,
  val locale: Locale
) {
  ENGLISH("en", "EN", "English", Locale.US),
  HINDI("hi", "HI", "हिन्दी", Locale.forLanguageTag("hi-IN"))
}

enum class CoreState {
  IDLE,
  LISTENING,
  PROCESSING,
  SPEAKING
}

enum class AiEngineStatus {
  GEMINI_ONLINE,
  CORE_PROTOCOL_OFFLINE,
  ERROR
}

enum class Sender {
  USER,
  JARVIS,
  SYSTEM
}

data class ChatMessage(
  val id: String = UUID.randomUUID().toString(),
  val sender: Sender,
  val text: String,
  val timestamp: Long = System.currentTimeMillis(),
  val isCoreProtocol: Boolean = false,
  val modelTag: String = ""
)

data class TelemetryData(
  val cpuPercent: Int = 14,
  val ramUsedMb: Long = 412,
  val ramTotalMb: Long = 1024,
  val batteryPercent: Int = 92,
  val batteryTempC: Float = 31.4f,
  val isCharging: Boolean = true,
  val arcReactorEnergy: Int = 98,
  val protocolVersion: String = "MARK VII.4",
  val securityLevel: String = "MAXIMUM",
  val neuralMesh: String = "SYNCHRONIZED"
)

data class WeatherData(
  val cityName: String,
  val countryCode: String,
  val tempC: Int,
  val condition: String,
  val conditionHi: String,
  val humidity: Int,
  val windKmh: Int,
  val pressureHpa: Int,
  val uvIndex: Int,
  val satelliteOrbitalStatus: String = "ORBIT LOCK (GEO-8)"
)

enum class ImageGenStyle(val title: String, val promptSuffix: String) {
  HOLOGRAPHIC("Hologram", "3D cyan holographic neon wireframe glowing HUD schematic"),
  BLUEPRINT("Blueprint", "technical blueprint CAD architectural schematic with cyan vectors"),
  CYBERPUNK("Cyberpunk", "futuristic cyberpunk neon cityscape night rain high tech"),
  NANOTECH("Nano-Core", "molecular nanotech honeycomb energy lattice arc reactor"),
  ARC_REACTOR("Arc Reactor", "concentric glowing cyan arc reactor core technical schematic"),
  SUIT_ARMOR("Mark Armor", "Tony Stark high-tech titanium alloy armor schematic wireframe")
}

data class GeneratedSchematic(
  val id: String = UUID.randomUUID().toString(),
  val prompt: String,
  val style: ImageGenStyle,
  val timestamp: Long = System.currentTimeMillis(),
  val wireframePattern: Int = 0
)
