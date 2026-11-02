package org.piramalswasthya.sakhi.network

import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.RequestBody
import org.junit.Test

/**
 * [AmritApiService.saveUwinSession] declares a default parameter value
 * (`images: List<MultipartBody.Part>? = null`). Kotlin generates a synthetic
 * `AmritApiService$DefaultImpls` bridge to fill in the omitted default at call sites;
 * that bridge code only runs when the method is actually invoked with the trailing
 * argument omitted. Production call sites always pass every argument explicitly, so
 * this bridge was never exercised. This test calls the method on a relaxed mock with
 * `images` omitted, forcing the real (non-mocked) `$DefaultImpls` bridge bytecode to
 * run before delegating to the mocked interface method.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AmritApiServiceDefaultsTest {

    private val service: AmritApiService = mockk(relaxed = true)

    @Test
    fun `saveUwinSession with default images param omitted`() = runTest {
        val body: RequestBody = mockk()

        service.saveUwinSession(
            meetingDate = body,
            place = body,
            participants = body,
            ashaId = body,
            createdBy = body
        )
    }
}
