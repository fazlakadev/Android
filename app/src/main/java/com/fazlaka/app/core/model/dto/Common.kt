package com.fazlaka.app.core.model.dto

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement

/**
 * Global API envelope produced by the backend TransformInterceptor:
 * { success, timestamp, data, meta }
 */
@Serializable
data class ApiEnvelope<T>(
    val success: Boolean = true,
    val timestamp: String? = null,
    val data: T? = null,
    val meta: Meta? = null,
)

@Serializable
data class Meta(
    val page: Int = 1,
    val limit: Int = 20,
    val total: Int = 0,
    val totalPages: Int = 0,
    val hasNextPage: Boolean = false,
    val hasPreviousPage: Boolean = false,
)

/**
 * Payload for paginated endpoints.
 *
 * The backend TransformInterceptor returns the list directly in the envelope's
 * `data` with the pagination `meta` as a sibling:
 * `{ success, timestamp, data: [...], meta: {...} }`.
 * Some endpoints nest them instead (`data: { data: [...], meta: {...} }`);
 * both forms are decoded.
 */
@Serializable(with = PaginatedSerializer::class)
data class Paginated<T>(
    val data: List<T> = emptyList(),
    val meta: Meta = Meta(),
)

class PaginatedSerializer<T>(private val elementSerializer: KSerializer<T>) :
    KSerializer<Paginated<T>> {

    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("Paginated") {
        element("data", ListSerializer(elementSerializer).descriptor)
        element("meta", Meta.serializer().descriptor)
    }

    override fun deserialize(decoder: Decoder): Paginated<T> {
        val json = decoder as? JsonDecoder
            ?: throw SerializationException("Paginated requires a JsonDecoder")
        return when (val element = json.decodeJsonElement()) {
            is JsonArray -> Paginated(
                data = json.json.decodeFromJsonElement(ListSerializer(elementSerializer), element),
            )
            is JsonObject -> Paginated(
                data = element["data"]
                    ?.let { json.json.decodeFromJsonElement(ListSerializer(elementSerializer), it) }
                    ?: emptyList(),
                meta = element["meta"]
                    ?.let { json.json.decodeFromJsonElement(Meta.serializer(), it) }
                    ?: Meta(),
            )
            else -> Paginated()
        }
    }

    override fun serialize(encoder: Encoder, value: Paginated<T>) {
        val json = encoder as? JsonEncoder
            ?: throw SerializationException("Paginated requires a JsonEncoder")
        json.encodeJsonElement(
            json.json.encodeToJsonElement(ListSerializer(elementSerializer), value.data),
        )
    }
}

@Serializable
data class SuccessDto(
    val success: Boolean = true,
)

@Serializable
data class SimpleMessageDto(
    val message: String? = null,
)

/** Error body produced by AllExceptionsFilter. */
@Serializable
data class ApiErrorBody(
    val statusCode: Int = 0,
    val message: String? = null,
    val error: String? = null,
    val path: String? = null,
    val timestamp: String? = null,
    val attemptsLeft: Int? = null,
    val errors: Map<String, List<String>>? = null,
)

/** Compact user reference used inside comments/ratings/playlists/content. */
@Serializable
data class AuthorDto(
    val id: String = "",
    val name: String = "",
    val username: String = "",
    val avatarUrl: String? = null,
    @SerialName("publicId")
    val publicId: String? = null,
    val bio: String? = null,
)
