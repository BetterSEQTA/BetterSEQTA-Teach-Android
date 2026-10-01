package org.betterseqta.betterseqtateachandroid.data.remote

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.put
import org.betterseqta.betterseqtateachandroid.core.network.SeqtaUrlBuilder
import org.betterseqta.betterseqtateachandroid.domain.model.TeachSession
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TeachUserClient @Inject constructor(
    private val seqtaApi: SeqtaApi,
) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun getStaffId(session: TeachSession): Int {
        val loginBody = loginRequestBody(session.baseUrl)
        runCatching {
            val response = seqtaApi.post(session, "/seqta/ta/login", loginBody)
            if (response.statusCode in 200..299) {
                payloadId(response.body)?.let { return it }
            }
        }

        val fallback = seqtaApi.post(session, "/seqta/ta/json/user/get", buildJsonObject { })
        if (fallback.statusCode !in 200..299) {
            throw SeqtaApiException.InvalidResponse()
        }
        return payloadId(fallback.body)
            ?: throw SeqtaApiException.InvalidResponse()
    }

    suspend fun getUserName(session: TeachSession): String? {
        val loginBody = loginRequestBody(session.baseUrl)
        runCatching {
            val response = seqtaApi.post(session, "/seqta/ta/login", loginBody)
            if (response.statusCode in 200..299) {
                userDesc(response.body)?.let { return it }
            }
        }

        val fallback = seqtaApi.post(session, "/seqta/ta/json/user/get", buildJsonObject { })
        if (fallback.statusCode !in 200..299) return null
        return displayNameFromUserGet(fallback.body)
    }

    suspend fun getUserInfo(session: TeachSession): Pair<String?, String?> {
        val loginBody = loginRequestBody(session.baseUrl)
        runCatching {
            val response = seqtaApi.post(session, "/seqta/ta/login", loginBody)
            if (response.statusCode in 200..299) {
                val payload = json.parseToJsonElement(response.body).jsonObject["payload"]?.jsonObject
                val displayName = payload?.get("userDesc")?.jsonPrimitive?.content
                    ?.trim()
                    ?.takeIf { it.isNotEmpty() }
                val userCode = payload?.get("userCode")?.jsonPrimitive?.content
                    ?.trim()
                    ?.takeIf { it.isNotEmpty() }
                if (displayName != null || userCode != null) {
                    return displayName to userCode
                }
            }
        }
        val fallback = seqtaApi.post(session, "/seqta/ta/json/user/get", buildJsonObject { })
        if (fallback.statusCode !in 200..299) {
            throw SeqtaApiException.InvalidResponse()
        }
        return displayNameFromUserGet(fallback.body) to null
    }

    private fun loginRequestBody(baseUrl: String) = buildJsonObject {
        put("mode", "normal")
        put("query", JsonNull)
        put("redirect_url", SeqtaUrlBuilder.buildWelcomeRedirectUrl(baseUrl))
    }

    private fun payloadId(body: String): Int? {
        val payload = json.parseToJsonElement(body).jsonObject["payload"]?.jsonObject
        val idPrimitive = payload?.get("id")?.jsonPrimitive ?: return null
        return idPrimitive.intOrNull ?: idPrimitive.content.toIntOrNull()
    }

    private fun userDesc(body: String): String? =
        json.parseToJsonElement(body).jsonObject["payload"]?.jsonObject
            ?.get("userDesc")?.jsonPrimitive?.content
            ?.trim()
            ?.takeIf { it.isNotEmpty() }

    private fun displayNameFromUserGet(body: String): String? {
        val payload = json.parseToJsonElement(body).jsonObject["payload"]?.jsonObject
        val candidates = listOf("firstName", "name", "displayName")
        for (key in candidates) {
            val value = payload?.get(key)?.jsonPrimitive?.content?.trim()
            if (!value.isNullOrEmpty()) return value
        }
        return null
    }
}
