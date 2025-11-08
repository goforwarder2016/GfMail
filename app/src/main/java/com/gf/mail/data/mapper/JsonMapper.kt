package com.gf.mail.data.mapper

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer

/**
 * JSON mapper utility for converting between JSON and objects
 */
object JsonMapper {
    
    val json: Json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        isLenient = true
    }
    
    /**
     * Convert any object to JSON string
     */
    fun toJsonString(obj: Any): String {
        return when (obj) {
            is String -> obj
            is Number -> obj.toString()
            is Boolean -> obj.toString()
            is List<*> -> json.encodeToString(ListSerializer(String.serializer()), obj.map { it.toString() })
            is Map<*, *> -> {
                val jsonObject = buildJsonObject {
                    obj.forEach { (key, value) ->
                        put(key.toString(), JsonPrimitive(value.toString()))
                    }
                }
                jsonObject.toString()
            }
            else -> obj.toString()
        }
    }
    
    /**
     * Parse JSON string to JsonElement
     */
    fun parseJson(jsonString: String): JsonElement? {
        return try {
            json.parseToJsonElement(jsonString)
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Convert JsonElement to Map
     */
    fun jsonToMap(jsonElement: JsonElement): Map<String, Any> {
        return when (jsonElement) {
            is JsonObject -> {
                jsonElement.jsonObject.mapValues { (_, value) ->
                    when (value) {
                        is JsonPrimitive -> {
                            // Convert JsonPrimitive to string content
                            value.content
                        }
                        is JsonObject -> jsonToMap(value)
                        else -> value.toString()
                    }
                }
            }
            else -> emptyMap()
        }
    }
}