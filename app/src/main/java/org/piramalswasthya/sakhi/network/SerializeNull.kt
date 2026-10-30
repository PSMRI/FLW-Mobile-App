package org.piramalswasthya.sakhi.network

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonQualifier
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import java.lang.reflect.Type

/**
 * Moshi drops null fields by default. A field marked with this is sent as an explicit `"field": null`
 * instead, without turning on null output for every other request.
 */
@Retention(AnnotationRetention.RUNTIME)
@JsonQualifier
annotation class SerializeNull

object SerializeNullAdapterFactory : JsonAdapter.Factory {
    override fun create(type: Type, annotations: Set<Annotation>, moshi: Moshi): JsonAdapter<*>? {
        val otherAnnotations = Types.nextAnnotations(annotations, SerializeNull::class.java) ?: return null
        val delegate = moshi.adapter<Any?>(type, otherAnnotations)
        return object : JsonAdapter<Any?>() {
            override fun fromJson(reader: JsonReader): Any? = delegate.fromJson(reader)

            override fun toJson(writer: JsonWriter, value: Any?) {
                if (value != null) {
                    delegate.toJson(writer, value)
                    return
                }
                val serializeNulls = writer.serializeNulls
                writer.serializeNulls = true
                try {
                    writer.nullValue()
                } finally {
                    writer.serializeNulls = serializeNulls
                }
            }
        }
    }
}
