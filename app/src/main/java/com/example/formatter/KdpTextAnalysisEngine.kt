package com.example.formatter

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

enum class IssueSeverity {
    PASSIVE_VOICE,
    RUN_ON_SENTENCE,
    KDP_PITFALL
}

data class ReadabilityIssue(
    val id: String = java.util.UUID.randomUUID().toString(),
    val type: IssueSeverity,
    val title: String,
    val originalText: String,
    val suggestionText: String,
    val explanation: String
)

data class TextAnalysisResult(
    val readabilityScore: Int,
    val gradeLevel: String,
    val passiveVoiceCount: Int,
    val runOnSentenceCount: Int,
    val kdpPitfallCount: Int,
    val issues: List<ReadabilityIssue>,
    val analyzedByGemini: Boolean = false
)

object KdpTextAnalysisEngine {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    // Main entry point: Performs Gemini AI analysis if available, otherwise rule-based analysis
    suspend fun analyzeText(text: String): TextAnalysisResult = withContext(Dispatchers.IO) {
        if (text.isBlank()) {
            return@withContext TextAnalysisResult(
                readabilityScore = 100,
                gradeLevel = "N/A",
                passiveVoiceCount = 0,
                runOnSentenceCount = 0,
                kdpPitfallCount = 0,
                issues = emptyList(),
                analyzedByGemini = false
            )
        }

        val localResult = performLocalRuleAnalysis(text)

        // Try calling Gemini 3.5 Flash REST API if key exists
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Throwable) {
            ""
        }

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext localResult
        }

        try {
            val geminiResult = callGemini35FlashAnalysis(text, apiKey)
            if (geminiResult != null && geminiResult.issues.isNotEmpty()) {
                return@withContext geminiResult
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return@withContext localResult
    }

    // Fast heuristic rule-based analyzer
    fun performLocalRuleAnalysis(text: String): TextAnalysisResult {
        val sentences = text.split("(?<=[.!?])\\s+".toRegex()).filter { it.isNotBlank() }
        val words = text.trim().split("\\s+".toRegex()).filter { it.isNotBlank() }

        val wordCount = words.size.coerceAtLeast(1)
        val sentenceCount = sentences.size.coerceAtLeast(1)

        val issues = mutableListOf<ReadabilityIssue>()

        // 1. Passive Voice Detection
        val passivePattern = Pattern.compile(
            "\\b(am|is|are|was|were|been|being|be)\\s+([a-z]+ed|[a-z]+en|made|written|done|given|taken|seen|known|found|built|told|brought)\\b",
            Pattern.CASE_INSENSITIVE
        )

        sentences.forEach { sentence ->
            val matcher = passivePattern.matcher(sentence)
            if (matcher.find()) {
                val match = matcher.group()
                issues.add(
                    ReadabilityIssue(
                        type = IssueSeverity.PASSIVE_VOICE,
                        title = "Passive Voice Detected",
                        originalText = sentence.trim(),
                        suggestionText = "Rephrase '$match' to active voice (e.g., place the actor/subject first).",
                        explanation = "Passive voice can reduce reader engagement in KDP fiction & non-fiction."
                    )
                )
            }
        }

        // 2. Run-On Sentences Detection (>28 words)
        sentences.forEach { sentence ->
            val sWords = sentence.trim().split("\\s+".toRegex())
            if (sWords.size > 28) {
                issues.add(
                    ReadabilityIssue(
                        type = IssueSeverity.RUN_ON_SENTENCE,
                        title = "Run-On / Overly Long Sentence",
                        originalText = sentence.trim(),
                        suggestionText = "Split into two concise sentences to improve flow and reader stamina.",
                        explanation = "Sentences over 28 words disrupt reading cadence on Kindle e-readers & mobile screens."
                    )
                )
            }
        }

        // 3. Common KDP Readability Pitfalls
        val filterWordPatterns = listOf(
            "noticed that", "realized that", "felt that", "saw that", "seemed to",
            "started to", "began to", "suddenly", "very", "really", "basically", "actually"
        )

        sentences.forEach { sentence ->
            val lower = sentence.lowercase()
            for (filterWord in filterWordPatterns) {
                if (lower.contains(filterWord)) {
                    issues.add(
                        ReadabilityIssue(
                            type = IssueSeverity.KDP_PITFALL,
                            title = "Filter Word / Weak Adverb '$filterWord'",
                            originalText = sentence.trim(),
                            suggestionText = "Remove '$filterWord' or replace with vivid action verbs.",
                            explanation = "Filter words create distance between the reader and the narrative."
                        )
                    )
                    break
                }
            }
        }

        val passiveCount = issues.count { it.type == IssueSeverity.PASSIVE_VOICE }
        val runOnCount = issues.count { it.type == IssueSeverity.RUN_ON_SENTENCE }
        val pitfallCount = issues.count { it.type == IssueSeverity.KDP_PITFALL }

        // Flesch Reading Ease approximation
        val avgWordsPerSentence = wordCount.toFloat() / sentenceCount
        val approxSyllablesPerWord = 1.45f
        val score = (206.835f - (1.015f * avgWordsPerSentence) - (84.6f * approxSyllablesPerWord)).roundToInt().coerceIn(30, 100)

        val gradeLevel = when {
            score >= 80 -> "6th-7th Grade (Easy / Fluid KDP Readability)"
            score >= 60 -> "8th-9th Grade (Optimal KDP Standard)"
            score >= 45 -> "10th-12th Grade (Moderate Complexity)"
            else -> "College Level (Dense Readability)"
        }

        return TextAnalysisResult(
            readabilityScore = score,
            gradeLevel = gradeLevel,
            passiveVoiceCount = passiveCount,
            runOnSentenceCount = runOnCount,
            kdpPitfallCount = pitfallCount,
            issues = issues,
            analyzedByGemini = false
        )
    }

    // Call Gemini 3.5 Flash REST API
    private fun callGemini35FlashAnalysis(text: String, apiKey: String): TextAnalysisResult? {
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

        val prompt = """
            Analyze the following book manuscript chapter for KDP (Kindle Direct Publishing) editorial standards.
            Identify:
            1. Passive voice sentences.
            2. Run-on or overly long sentences.
            3. Common KDP readability pitfalls (filter words, overused adverbs, awkward phrasing).

            Provide output in strict JSON format matching this schema:
            {
              "readabilityScore": 85,
              "gradeLevel": "7th Grade (Optimal)",
              "issues": [
                {
                  "type": "PASSIVE_VOICE" or "RUN_ON_SENTENCE" or "KDP_PITFALL",
                  "title": "Short title",
                  "originalText": "exact text snippet from input",
                  "suggestionText": "revised active sentence or fix",
                  "explanation": "why this fix improves KDP publication quality"
                }
              ]
            }

            Manuscript snippet:
            \"\"\"
            $text
            \"\"\"
        """.trimIndent()

        val jsonRequest = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", prompt)
                        })
                    })
                })
            })
            put("generationConfig", JSONObject().apply {
                put("responseMimeType", "application/json")
            })
        }

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = jsonRequest.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return null
            val responseStr = response.body?.string() ?: return null

            val root = JSONObject(responseStr)
            val candidates = root.optJSONArray("candidates") ?: return null
            if (candidates.length() == 0) return null

            val candidate = candidates.getJSONObject(0)
            val content = candidate.optJSONObject("content") ?: return null
            val parts = content.optJSONArray("parts") ?: return null
            if (parts.length() == 0) return null

            val replyText = parts.getJSONObject(0).optString("text", "")
            if (replyText.isBlank()) return null

            val jsonReply = JSONObject(replyText)
            val readabilityScore = jsonReply.optInt("readabilityScore", 82)
            val gradeLevel = jsonReply.optString("gradeLevel", "8th Grade Standard")
            val issuesArray = jsonReply.optJSONArray("issues") ?: JSONArray()

            val issues = mutableListOf<ReadabilityIssue>()
            for (i in 0 until issuesArray.length()) {
                val item = issuesArray.getJSONObject(i)
                val typeStr = item.optString("type", "KDP_PITFALL")
                val type = when (typeStr) {
                    "PASSIVE_VOICE" -> IssueSeverity.PASSIVE_VOICE
                    "RUN_ON_SENTENCE" -> IssueSeverity.RUN_ON_SENTENCE
                    else -> IssueSeverity.KDP_PITFALL
                }

                issues.add(
                    ReadabilityIssue(
                        type = type,
                        title = item.optString("title", "Style Improvement"),
                        originalText = item.optString("originalText", ""),
                        suggestionText = item.optString("suggestionText", ""),
                        explanation = item.optString("explanation", "")
                    )
                )
            }

            return TextAnalysisResult(
                readabilityScore = readabilityScore,
                gradeLevel = gradeLevel,
                passiveVoiceCount = issues.count { it.type == IssueSeverity.PASSIVE_VOICE },
                runOnSentenceCount = issues.count { it.type == IssueSeverity.RUN_ON_SENTENCE },
                kdpPitfallCount = issues.count { it.type == IssueSeverity.KDP_PITFALL },
                issues = issues,
                analyzedByGemini = true
            )
        }
    }

    private fun Double.roundToInt(): Int = Math.round(this).toInt()
    private fun Float.roundToInt(): Int = Math.round(this).toInt()
}
