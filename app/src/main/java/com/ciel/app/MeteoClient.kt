package com.ciel.app

import java.net.HttpURLConnection
import java.net.URL

object MeteoClient {
    // Simple mock or real implementation. Given context, I will implement a basic HTTP GET.
    // URL for OpenWeatherMap or similar free service would require an API key, 
    // which I don't have. I will mock the return for now or simulate a call.
    
    fun getMeteo(ville: String): String {
        return "Il fait 20°C à $ville, ciel dégagé."
        /* 
        // Example implementation:
        val url = URL("https://api.weather.com/v1/...")
        with(url.openConnection() as HttpURLConnection) {
            requestMethod = "GET"
            return inputStream.bufferedReader().use { it.readText() }
        }
        */
    }
}
