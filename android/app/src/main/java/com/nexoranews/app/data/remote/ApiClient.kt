package com.nexoranews.app.data.remote

import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

object ApiClient {

    // Primary and fallback endpoints for the API
    private const val BASE_URL = "https://nexora-news.onrender.com/api"
    private const val TIMEOUT_MS = 10000

    fun postJson(endpoint: String, jsonBody: String): Result<String> {
        return try {
            val urlString = if (endpoint.startsWith("http")) endpoint else "$BASE_URL$endpoint"
            val url = URL(urlString)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = TIMEOUT_MS
                readTimeout = TIMEOUT_MS
                doOutput = true
                setRequestProperty("Accept", "application/json")
                setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                setRequestProperty("User-Agent", "NexoraNews-Android/1.0")
            }

            connection.outputStream.use { output ->
                output.write(jsonBody.toByteArray(Charsets.UTF_8))
            }

            val responseCode = connection.responseCode
            val stream = if (responseCode in 200..299) {
                connection.inputStream
            } else {
                connection.errorStream
            }

            val response = stream?.let {
                BufferedReader(InputStreamReader(it)).use { reader -> reader.readText() }
            } ?: ""

            connection.disconnect()

            if (responseCode in 200..299) {
                Result.success(response)
            } else {
                Result.failure(Exception("HTTP Error $responseCode: $response"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun get(endpoint: String): Result<String> {
        return try {
            val urlString = if (endpoint.startsWith("http")) endpoint else "$BASE_URL$endpoint"
            val url = URL(urlString)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = TIMEOUT_MS
                readTimeout = TIMEOUT_MS
                setRequestProperty("Accept", "application/json")
                setRequestProperty("User-Agent", "NexoraNews-Android/1.0")
            }

            val responseCode = connection.responseCode
            if (responseCode in 200..299) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val response = reader.use { it.readText() }
                connection.disconnect()
                Result.success(response)
            } else {
                connection.disconnect()
                Result.failure(Exception("HTTP Error $responseCode"))
            }
        } catch (e: Exception) {
            // Gracefully catch any network/DNS errors (like UnknownHostException)
            Result.failure(e)
        }
    }
}
