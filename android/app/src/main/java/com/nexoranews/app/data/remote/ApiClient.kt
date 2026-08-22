package com.nexoranews.app.data.remote

import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

object ApiClient {

    private const val BASE_URL =
        "https://hlghyzvspodrmjvmslur.supabase.co/rest/v1"

    private const val SUPABASE_KEY =
        "sb_publishable_rm2iAd195USLfN1z7VQpgA_t4zbSuwd"

    private const val TIMEOUT_MS = 15000

    fun get(endpoint: String): Result<String> {
        return try {
            val urlString =
                if (endpoint.startsWith("http")) endpoint
                else "$BASE_URL$endpoint"

            val connection =
                (URL(urlString).openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = TIMEOUT_MS
                    readTimeout = TIMEOUT_MS

                    setRequestProperty("apikey", SUPABASE_KEY)
                    setRequestProperty("Authorization", "Bearer $SUPABASE_KEY")
                    setRequestProperty("Accept", "application/json")
                }

            val responseCode = connection.responseCode

            if (responseCode in 200..299) {
                val reader = BufferedReader(
                    InputStreamReader(connection.inputStream)
                )
                val response = reader.use { it.readText() }
                connection.disconnect()
                Result.success(response)
            } else {
                connection.disconnect()
                Result.failure(Exception("Supabase HTTP Error $responseCode"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
