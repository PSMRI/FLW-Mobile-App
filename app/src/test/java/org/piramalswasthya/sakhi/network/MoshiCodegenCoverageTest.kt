package org.piramalswasthya.sakhi.network

import com.squareup.moshi.Moshi
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.junit.Assert.assertTrue
import org.junit.Test
import org.piramalswasthya.sakhi.di.AppModule
import org.piramalswasthya.sakhi.model.MaaMeetingGetAllResponse
import org.piramalswasthya.sakhi.model.PeerAtFacility
import org.piramalswasthya.sakhi.model.SaasBahuSammelanGetAllResponse
import org.piramalswasthya.sakhi.repositories.UwinRepo
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import java.lang.reflect.GenericArrayType
import java.lang.reflect.Modifier
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.lang.reflect.WildcardType
import kotlin.coroutines.Continuation

/**
 * The app's Moshi has no KotlinJsonAdapterFactory (kotlin-reflect was removed to shrink the release
 * DEX, FLW-1211), so every Kotlin class Moshi converts needs @JsonClass(generateAdapter = true).
 * A missing annotation only fails at runtime, on the first API call that uses the class, so this
 * checks every type the Moshi-based API services send or receive, and every class nested in them.
 */
class MoshiCodegenCoverageTest {

    private val moshi: Moshi = AppModule.provideMoshiInstance()

    // Handled by Retrofit or OkHttp directly, never by Moshi.
    private val notMoshi = setOf<Type>(
        ResponseBody::class.java, RequestBody::class.java, MultipartBody.Part::class.java,
        Unit::class.java, Void::class.java
    )

    private fun bodyType(type: Type): Type {
        var t = type
        while (true) {
            t = when {
                t is WildcardType -> t.lowerBounds.firstOrNull() ?: t.upperBounds[0]
                t is ParameterizedType && (t.rawType == Response::class.java || t.rawType == Call::class.java) ->
                    t.actualTypeArguments[0]
                else -> return t
            }
        }
    }

    private fun apiTypes(service: Class<*>): Map<Type, String> {
        val types = mutableMapOf<Type, String>()
        for (method in service.declaredMethods) {
            val params = method.genericParameterTypes
            method.parameterAnnotations.forEachIndexed { i, annotations ->
                if (annotations.any { it is Body }) types[bodyType(params[i])] = "${service.simpleName}.${method.name} @Body"
            }
            val last = params.lastOrNull()
            val returned = if (last is ParameterizedType && last.rawType == Continuation::class.java) {
                last.actualTypeArguments[0]
            } else {
                method.genericReturnType
            }
            types[bodyType(returned)] = "${service.simpleName}.${method.name} response"
        }
        return types.filterKeys { it !in notMoshi }
    }

    private fun rootTypes(): Map<Type, String> =
        apiTypes(AmritApiService::class.java) + apiTypes(AbhaApiService::class.java) + mapOf(
            UwinRepo.UwinGetAllResponse::class.java to "UwinRepo",
            MaaMeetingGetAllResponse::class.java to "MaaMeetingRepo",
            SaasBahuSammelanGetAllResponse::class.java to "SaasBahuSammelanRepo",
            PeerAtFacility::class.java to "UserRepo",
        )

    private fun hasGeneratedAdapter(clazz: Class<*>): Boolean = runCatching {
        Class.forName(clazz.name.replace('$', '_') + "JsonAdapter", false, clazz.classLoader)
    }.isSuccess

    /** App Kotlin classes reachable from [type] (through fields, lists, maps, arrays) without a generated adapter. */
    private fun missingCodegen(type: Type, seen: MutableSet<Class<*>>, missing: MutableSet<String>) {
        when (type) {
            is ParameterizedType -> {
                missingCodegen(type.rawType, seen, missing)
                type.actualTypeArguments.forEach { missingCodegen(it, seen, missing) }
            }
            is WildcardType -> (type.upperBounds + type.lowerBounds).forEach { missingCodegen(it, seen, missing) }
            is GenericArrayType -> missingCodegen(type.genericComponentType, seen, missing)
            is Class<*> -> {
                if (type.isArray) return missingCodegen(type.componentType, seen, missing)
                if (!type.name.startsWith("org.piramalswasthya.") || type.isEnum || !seen.add(type)) return
                if (type.isAnnotationPresent(Metadata::class.java) && !hasGeneratedAdapter(type)) missing += type.name
                type.declaredFields
                    .filter { !Modifier.isStatic(it.modifiers) && !Modifier.isTransient(it.modifiers) }
                    .forEach { missingCodegen(it.genericType, seen, missing) }
            }
        }
    }

    @Test
    fun `every type Moshi converts has a generated adapter`() {
        val roots = rootTypes()
        val missing = sortedSetOf<String>()
        val seen = mutableSetOf<Class<*>>()
        roots.keys.forEach { missingCodegen(it, seen, missing) }
        assertTrue(
            "${missing.size} class(es) need @JsonClass(generateAdapter = true):\n" + missing.joinToString("\n"),
            missing.isEmpty()
        )
    }

    /** True if [type] holds Gson types (JsonObject, JsonElement); those calls go through the "gsonAmritApi" Retrofit. */
    private fun containsGsonTypes(type: Type, seen: MutableSet<Class<*>> = mutableSetOf()): Boolean = when (type) {
        is ParameterizedType -> containsGsonTypes(type.rawType, seen) || type.actualTypeArguments.any { containsGsonTypes(it, seen) }
        is WildcardType -> (type.upperBounds + type.lowerBounds).any { containsGsonTypes(it, seen) }
        is GenericArrayType -> containsGsonTypes(type.genericComponentType, seen)
        is Class<*> -> when {
            type.isArray -> containsGsonTypes(type.componentType, seen)
            type.name.startsWith("com.google.gson.") -> true
            !type.name.startsWith("org.piramalswasthya.") || !seen.add(type) -> false
            else -> type.declaredFields.filter { !Modifier.isStatic(it.modifiers) }.any { containsGsonTypes(it.genericType, seen) }
        }
        else -> false
    }

    @Test
    fun `moshi can build an adapter for every API type`() {
        val failures = rootTypes().filterKeys { !containsGsonTypes(it) }.mapNotNull { (type, usedBy) ->
            runCatching { moshi.adapter<Any>(type) }.exceptionOrNull()?.let { "$usedBy -> ${it.message}" }
        }
        assertTrue("${failures.size} type(s) failed:\n" + failures.joinToString("\n"), failures.isEmpty())
    }
}
