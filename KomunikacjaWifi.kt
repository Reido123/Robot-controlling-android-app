package com.example.aplikacja_final

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class KomunikacjaWifi {

    private val client = OkHttpClient()

    fun sendJson_wifi(json: String) {
        try {
            println("sendJson() - start")

            val url = "http://192.168.4.1:80/sterowanie?instrukcja=$json"
            println("URL: $url")

            var request = Request.Builder()
                .url(url)
                .get()
                .build()
            CoroutineScope(Dispatchers.Main).launch {
                var response = withContext(Dispatchers.IO) { client.newCall(request).execute() }
                handleResponse(response)
            }

        } catch (e: Exception) {
            println("Exception: ${e.toString()}")
            e.printStackTrace()
        }
    }

    fun handleResponse(response: Response) {
        try {
            val responseCode = response.code
            println("Response Code: $responseCode")

            if (response.isSuccessful) {
                val resp = response.body?.string()
                if (resp != null) {
                    println("Uzyskany JSON z serwera: $resp")
                } else {
                    println("Brak danych w odpowiedzi serwera")
                }
            } else {
                println("Błąd: ${response.message}")
                val errorResp = response.body?.string()
                if (errorResp != null) {
                    println("Uzyskany JSON z serwera: $errorResp")
                    try {
                        val jsonObject = JSONObject(errorResp)
                        println("JSON: $jsonObject")
                    } catch (err: JSONException) {
                        println("Error: ${err.toString()}")
                    }
                } else {
                    println("Brak danych w odpowiedzi serwera")
                }
            }
        } catch (e: IOException) {
            println("IOException: ${e.toString()}")
        }
    }

    suspend fun getJson(): JSONObject {
        var jsonObject = JSONObject()
        val client = OkHttpClient()

        try {
            // Tworzenie zapytania
            val request = Request.Builder()
                .url("http://192.168.4.1:80/synch?obecnaPozycja=")
                .header("Content-Type", "application/json")
                .build()

            // Wykonanie zapytania
            val response = withContext(Dispatchers.IO) { client.newCall(request).execute() }

            // Sprawdzenie odpowiedzi
            if (response.isSuccessful) {
                val json = response.body!!.string()
                println("Uzyskany JSON z serwera: $json")
                jsonObject = JSONObject(json).getJSONObject("wartosci")
            } else {
                println("Błąd: ${response.message}")
                val resp = response.body!!.string()
                println("Uzyskany JSON z serwera: $resp")
                try {
                    jsonObject = JSONObject(resp)
                    println(jsonObject)
                } catch (e: Exception) {
                    println("Błąd parsowania JSON: ${e.message}")
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return jsonObject
    }
}
