package org.betterseqta.betterseqtateachandroid.data.remote

sealed class SeqtaApiException(message: String) : Exception(message) {
    class InvalidUrl(message: String = "Invalid URL") : SeqtaApiException(message)
    class InvalidBody(message: String = "Invalid request body") : SeqtaApiException(message)
    class InvalidResponse(message: String = "Invalid response") : SeqtaApiException(message)
}
