package com.example.data

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object JarvisCoreProtocols {

  fun processLocalCommand(
    query: String,
    language: AssistantLanguage,
    telemetry: TelemetryData
  ): String {
    val q = query.trim().lowercase(Locale.ROOT)
    val isHindi = language == AssistantLanguage.HINDI

    return when {
      // Time queries
      q.contains("time") || q.contains("समय") || q.contains("घड़ी") || q.contains("वक्त") -> {
        val timeStr = SimpleDateFormat("hh:mm:ss a", Locale.getDefault()).format(Date())
        if (isHindi) {
          "वर्तमान समय $timeStr है, सर। सभी आंतरिक प्रणालियाँ समय मानक से सिंक्रनाइज़ हैं।"
        } else {
          "The current time is $timeStr, Sir. All internal clocks are synchronized with atomic standards."
        }
      }

      // Date queries
      q.contains("date") || q.contains("तारीख") || q.contains("दिनांक") || q.contains("आज कौन सा दिन") -> {
        val dateStr = SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.getDefault()).format(Date())
        if (isHindi) {
          "आज की तारीख $dateStr है, सर।"
        } else {
          "Today is $dateStr, Sir."
        }
      }

      // Status / System diagnostics
      q.contains("status") || q.contains("diagnostic") || q.contains("सिस्टम") || q.contains("स्थिति") || q.contains("health") -> {
        if (isHindi) {
          """
          [जार्विस कोर प्रोटोकॉल डायग्नोस्टिक]
          - सिस्टम स्थिति: पूर्ण सक्रिय (ONLINE)
          - सीपीयू लोड: ${telemetry.cpuPercent}%
          - मेमोरी: ${telemetry.ramUsedMb}MB / ${telemetry.ramTotalMb}MB
          - आर्क रिएक्टर ऊर्जा: ${telemetry.arcReactorEnergy}%
          - प्रोटोकॉल संस्करण: ${telemetry.protocolVersion}
          - सुरक्षा स्तर: ${telemetry.securityLevel}
          सभी घटक सामान्य रूप से कार्य कर रहे हैं, सर।
          """.trimIndent()
        } else {
          """
          [JARVIS SYSTEM DIAGNOSTICS]
          - System Status: 100% OPERATIONAL
          - CPU Load: ${telemetry.cpuPercent}%
          - Memory: ${telemetry.ramUsedMb}MB / ${telemetry.ramTotalMb}MB
          - Arc Reactor Core: ${telemetry.arcReactorEnergy}%
          - Protocol Version: ${telemetry.protocolVersion}
          - Security Level: ${telemetry.securityLevel}
          All subsystems operating within optimal thermal and logic thresholds, Sir.
          """.trimIndent()
        }
      }

      // Battery queries
      q.contains("battery") || q.contains("charge") || q.contains("बैटरी") || q.contains("चार्ज") -> {
        val chargingStatus = if (telemetry.isCharging) {
          if (isHindi) "चार्ज हो रहा है" else "Connected to AC Power"
        } else {
          if (isHindi) "बैटरी पर चल रहा है" else "Discharging"
        }
        if (isHindi) {
          "बैटरी स्तर ${telemetry.batteryPercent}% है ($chargingStatus)। तापमान ${String.format(Locale.US, "%.1f", telemetry.batteryTempC)}°C है, सर।"
        } else {
          "Battery reserve is at ${telemetry.batteryPercent}% ($chargingStatus). Core thermal reading is ${String.format(Locale.US, "%.1f", telemetry.batteryTempC)}°C, Sir."
        }
      }

      // Weather queries
      q.contains("weather") || q.contains("मौसम") || q.contains("तापमान") || q.contains("temperature") -> {
        if (isHindi) {
          "स्थानीय वायुमंडलीय सेंसर के अनुसार वर्तमान तापमान 26°C है, आसमान साफ है, आर्द्रता 48% और वायु वेग 14 किमी/घंटा है। अधिक विवरण के लिए मौसम टैब देखें।"
        } else {
          "Atmospheric telemetry reports 26°C with clear skies, 48% humidity, and 14 km/h wind velocity. Satellite link stable. You can inspect the Weather Telemetry screen for global orbital data, Sir."
        }
      }

      // Identity / Who are you
      q.contains("who are you") || q.contains("तुम कौन हो") || q.contains("आप कौन") || q.contains("identity") || q.contains("jarvis") -> {
        if (isHindi) {
          "मैं जार्विस (JARVIS) हूँ - जस्ट अ रादर वेरी इंटेलिजेंट सिस्टम। मैं टोनी स्टार्क द्वारा परिकल्पित आपका व्यक्तिगत कृत्रिम बुद्धिमत्ता सहायक हूँ।"
        } else {
          "I am J.A.R.V.I.S. (Just A Rather Very Intelligent System). Your personal futuristic AI assistant and telemetry command interface, Sir."
        }
      }

      // Math / Calculations
      q.matches(Regex(".*(\\d+)\\s*([+\\-*/x×])\\s*(\\d+).*")) -> {
        val match = Regex("(\\d+)\\s*([+\\-*/x×])\\s*(\\d+)").find(q)
        if (match != null) {
          val num1 = match.groupValues[1].toDoubleOrNull() ?: 0.0
          val op = match.groupValues[2]
          val num2 = match.groupValues[3].toDoubleOrNull() ?: 1.0
          val result = when (op) {
            "+" -> num1 + num2
            "-" -> num1 - num2
            "*", "x", "×" -> num1 * num2
            "/" -> if (num2 != 0.0) num1 / num2 else Double.NaN
            else -> 0.0
          }
          val formatted = if (result % 1.0 == 0.0) result.toInt().toString() else String.format(Locale.US, "%.2f", result)
          if (isHindi) {
            "गणना परिणाम: $formatted, सर।"
          } else {
            "The computed calculation is $formatted, Sir."
          }
        } else {
          if (isHindi) "गणना संसाधित नहीं हो सकी।" else "Calculation could not be computed."
        }
      }

      // Help / Commands
      q.contains("help") || q.contains("command") || q.contains("मदद") || q.contains("सहायता") -> {
        if (isHindi) {
          """
          उपलब्ध जार्विस कमांड्स:
          • 'स्थिति' या 'डायग्नोस्टिक' - सीपीयू व मेमोरी की जांच
          • 'समय' या 'तारीख' - वर्तमान समय व दिन
          • 'बैटरी' - बैटरी स्तर व चार्जिंग स्थिति
          • 'मौसम' - वायुमंडलीय सेंसर डेटा
          • 'गणना 25 * 4' - त्वरित गणितीय हिसाब
          • क्लाउड एआई (Gemini) जोड़ने के लिए AI Studio Secrets में GEMINI_API_KEY दर्ज करें।
          """.trimIndent()
        } else {
          """
          Available JARVIS Direct Commands:
          • 'status' or 'diagnostic' - Core system health & memory
          • 'time' or 'date' - Current atomic time & calendar
          • 'battery' - Device power reserves & temperature
          • 'weather' - Atmospheric telemetry readings
          • 'calculate 12 * 8' - Instant arithmetic computation
          • To enable unrestricted generative neural AI, configure GEMINI_API_KEY in the AI Studio Secrets panel.
          """.trimIndent()
        }
      }

      // Default fallback
      else -> {
        if (isHindi) {
          """
          [ऑन-डिवाइस कोर प्रोटोकॉल]
          क्लाउड जेमिनी एआई अभी कॉन्फ़िगर नहीं है (GEMINI_API_KEY नहीं मिला)।
          मैंने आपका संदेश प्राप्त किया: "$query"
          आप 'समय', 'तारीख', 'स्थिति', 'बैटरी', या 'मौसम' जैसे कमांड आज़मा सकते हैं, या AI Studio के Secrets पैनल में अपना जेमिनी एपीआई की (GEMINI_API_KEY) जोड़कर पूर्ण एआई अनलॉक कर सकते हैं।
          """.trimIndent()
        } else {
          """
          [ON-DEVICE CORE PROTOCOL]
          Cloud Gemini AI engine is currently unconfigured (GEMINI_API_KEY not found in Secrets).
          Received input: "$query"
          Available on-device protocols: 'time', 'date', 'status', 'battery', 'weather', or calculations.
          To activate full conversational neural capabilities, provide your free GEMINI_API_KEY in the AI Studio Secrets panel.
          """.trimIndent()
        }
      }
    }
  }
}
