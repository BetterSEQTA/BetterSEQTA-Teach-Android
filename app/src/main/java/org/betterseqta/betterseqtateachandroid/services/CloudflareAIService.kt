package org.betterseqta.betterseqtateachandroid.services

import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import org.betterseqta.betterseqtateachandroid.config.CloudflareConfig
import javax.inject.Inject
import javax.inject.Singleton

/** Cloudflare Workers AI — Llama rewrites and BART summaries (iOS parity). */
@Singleton
class CloudflareAIService @Inject constructor(
    private val httpClient: HttpClient,
) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun proofread(text: String): String? {
        val prompt = """
            You are a proofreader. Fix spelling, grammar, and punctuation in the user's text.
            Return ONLY the corrected text. Do not add explanations, quotes, or extra formatting.
            Preserve the original meaning and structure.
        """.trimIndent()
        return runLlama(system = prompt, user = text)
    }

    suspend fun changeTone(text: String, tone: String): String? {
        val prompt = """
            You are a writing assistant. Rewrite the user's text to be $tone.
            Return ONLY the rewritten text. Do not add explanations, quotes, or extra formatting.
            Keep the same general meaning and length.
        """.trimIndent()
        return runLlama(system = prompt, user = text)
    }

    suspend fun suggestReplies(messageContent: String): List<String>? {
        val prompt = """
            You are a helpful assistant suggesting short email replies. Given a message, suggest 2–3 brief, professional reply options.
            Return ONLY the replies, one per line. No numbering, bullets, or labels. Each reply should be 1–2 sentences max.
            Be concise and appropriate for a teacher replying to students or colleagues.
        """.trimIndent()
        val user = "Message to reply to:\n\n${messageContent.take(2000)}"
        val result = runLlama(system = prompt, user = user) ?: return null
        return result.lines()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .take(3)
    }

    suspend fun summarize(text: String, maxLength: Int = 130): String? {
        val base = CloudflareConfig.baseUrl() ?: return null
        val auth = CloudflareConfig.authHeader() ?: return null
        val url = "$base/@cf/facebook/bart-large-cnn"
        val body = buildJsonObject {
            put("input_text", text.take(1024))
            put("max_length", maxLength)
        }
        val response = httpClient.post(url) {
            contentType(ContentType.Application.Json)
            header("Authorization", auth)
            setBody(body.toString())
        }
        if (response.status.value !in 200..299) return null
        val root = json.parseToJsonElement(response.bodyAsText()).jsonObject
        return root["result"]?.jsonObject
            ?.get("summary")?.jsonPrimitive?.content
            ?.trim()
    }

    private suspend fun runLlama(system: String, user: String): String? {
        val base = CloudflareConfig.baseUrl() ?: return null
        val auth = CloudflareConfig.authHeader() ?: return null
        val url = "$base/@cf/meta/llama-3.1-8b-instruct-fast"
        val body = buildJsonObject {
            put(
                "messages",
                buildJsonArray {
                    add(messageObject("system", system))
                    add(messageObject("user", user))
                },
            )
        }
        val response = httpClient.post(url) {
            contentType(ContentType.Application.Json)
            header("Authorization", auth)
            setBody(body.toString())
        }
        if (response.status.value !in 200..299) return null
        val root = json.parseToJsonElement(response.bodyAsText()).jsonObject
        return root["result"]?.jsonObject
            ?.get("response")?.jsonPrimitive?.content
            ?.trim()
    }

    private fun messageObject(role: String, content: String): JsonObject =
        buildJsonObject {
            put("role", role)
            put("content", content)
        }
}
