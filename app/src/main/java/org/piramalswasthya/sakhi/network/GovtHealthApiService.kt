package org.piramalswasthya.sakhi.network

import com.google.gson.annotations.SerializedName
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface GovtHealthApiService {

    @POST("sna/dkbssyapipool/api/UserRgistration/GetUserDetailsByAyushmanCardNo")
    suspend fun getUserDetailsByAyushmanCardNo(
        @Body request: AyushmanCardRequest
    ): Response<ResponseBody>

    companion object {
        const val BASE_URL = "https://govthealth.cg.gov.in/"
    }
}

data class AyushmanCardRequest(
    val userId: String,
    val password: String,
    val cardNo: String

)