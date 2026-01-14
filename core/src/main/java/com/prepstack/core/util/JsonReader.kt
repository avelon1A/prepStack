package com.prepstack.core.util

import android.content.Context
import java.io.IOException

/**
 * Utility class for reading JSON files from assets
 */
object JsonReader {
    
    /**
     * Reads a JSON file from the assets folder
     * @param context Android context
     * @param fileName Name of the JSON file in assets
     * @return JSON string or null if error occurs
     */
    fun readJsonFromAssets(context: Context, fileName: String): String? {
        return try {
            context.assets.open(fileName).bufferedReader().use { it.readText() }
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }
}
