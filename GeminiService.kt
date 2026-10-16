package com.example.data

import com.example.BuildConfig
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

sealed class GeminiResult {
  data class Success(val text: String, val model: String = "gemini-3.5-flash") : GeminiResult()
  data class NotConfigured(val reason: String) : GeminiResult()
  data class Failure(val error: String) : GeminiResult()
}

@JsonClass(generateAdapter = true)
data class GeminiApiRequest(
  val contents: List<GeminiContent>,
  val systemInstruction: GeminiContent? = null
)

@JsonClass(generateAdapter = true)
data class GeminiContent(
  val role: String = "user",
  val parts: List<GeminiPart>
)

@JsonClass(generateAdapter = true)
data class GeminiPart(
  val text: String? = null
)

@JsonClass(generateAdapter = true)
data class GeminiApiResponse(
  val candidates: List<GeminiCandidate>? = null,
  val error: GeminiApiError? = null
)

@JsonClass(generateAdapter = true)
data class GeminiCandidate(
  val content: GeminiContent? = null
)

@JsonClass(generateAdapter = true)
data class GeminiApiError(
  val code: Int? = null,
  val message: String? = null,
  val status: String? = null
)

class GeminiService {

  private val okHttpClient = OkHttpClient.Builder()
    .connectTimeout(30, TimeUnit.SECONDS)
    .readTimeout(60, TimeUnit.SECONDS)
    .writeTimeout(60, TimeUnit.SECONDS)
    .build()

  private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

  private val requestAdapter = moshi.adapter(GeminiApiRequest::class.java)
  private val responseAdapter = moshi.adapter(GeminiApiResponse::class.java)

  fun isKeyConfigured(): Boolean {
    val key = BuildConfig.GEMINI_API_KEY
    return !key.isNullOrBlank() &&
      key != "MY_GEMINI_API_KEY" &&
      key != "null" &&
      !key.startsWith("YOUR_")
  }

  suspend fun generateResponse(
    prompt: String,
    language: AssistantLanguage,
    conversationHistory: List<ChatMessage>
  ): GeminiResult = withContext(Dispatchers.IO) {
    if (!isKeyConfigured()) {
      return@withContext GeminiResult.NotConfigured(
        "Gemini API key is not configured in the AI Studio Secrets panel. Add GEMINI_API_KEY to activate cloud neural intelligence."
      )
    }

    val apiKey = BuildConfig.GEMINI_API_KEY

    val systemPromptText = if (language == AssistantLanguage.HINDI) {
      "You are J.A.R.V.I.S., a sophisticated, witty, and highly intelligent AI assistant created by Tony Stark. Respond in Hindi (Devanagari script), respectfully referring to the user as 'Sir' (सर) or 'Boss'. Keep answers concise, articulate, and futuristic."
    } else {
      "You are J.A.R.V.I.S., an exceptionally intelligent, polite, and witty AI system created by Tony Stark. Speak with British elegance and futuristic composure, addressing the user as 'Sir'. Keep responses concise, articulate, and actionable."
    }

    val historyContents = conversationHistory.takeLast(6).map { msg ->
      GeminiContent(
        role = if (msg.sender == Sender.USER) "user" else "model",
        parts = listOf(GeminiPart(text = msg.text))
      )
    }

    val finalContents = if (historyContents.isEmpty() || historyContents.last().role != "user" || historyContents.last().parts.firstOrNull()?.text != prompt) {
      historyContents + GeminiContent(
        role = "user",
        parts = listOf(GeminiPart(text = prompt))
      )
    } else {
      historyContents
    }

    val requestPayload = GeminiApiRequest(
      contents = finalContents,
      systemInstruction = GeminiContent(
        role = "system",
        parts = listOf(GeminiPart(text = systemPromptText))
      )
    )

    try {
      val jsonBody = requestAdapter.toJson(requestPayload)
      val mediaType = "application/json; charset=utf-8".toMediaType()
      val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

      val httpRequest = Request.Builder()
        .url(url)
        .post(jsonBody.toRequestBody(mediaType))
        .build()

      okHttpClient.newCall(httpRequest).execute().use { response ->
        val responseBody = response.body?.string() ?: ""
        if (!response.isSuccessful) {
          return@withContext GeminiResult.Failure(
            "API Request Failed [HTTP ${response.code}]: ${responseBody.take(200)}"
          )
        }

        val apiResponse = responseAdapter.fromJson(responseBody)
        if (apiResponse?.error != null) {
          return@withContext GeminiResult.Failure(apiResponse.error.message ?: "Unknown API error")
        }

        val text = apiResponse?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
        if (text != null) {
          GeminiResult.Success(text = text.trim())
        } else {
          GeminiResult.Failure("Empty response received from Gemini engine.")
        }
      }
    } catch (e: Exception) {
      GeminiResult.Failure("Network error communicating with Gemini API: ${e.localizedMessage}")
    }
  }
}
