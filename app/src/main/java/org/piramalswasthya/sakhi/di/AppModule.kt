package org.piramalswasthya.sakhi.di

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.piramalswasthya.sakhi.database.room.InAppDb
import org.piramalswasthya.sakhi.database.room.dao.*
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.network.AbhaApiService
import org.piramalswasthya.sakhi.network.AmritApiService
import org.piramalswasthya.sakhi.network.interceptors.ContentTypeInterceptor
import org.piramalswasthya.sakhi.network.interceptors.TokenInsertAbhaInterceptor
import org.piramalswasthya.sakhi.network.interceptors.TokenInsertTmcInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    private const val baseTmcUrl = //"http://uatamrit.piramalswasthya.org:8080/"
    "http://amritdemo.piramalswasthya.org:8080/"
        //"http://192.168.1.94:8081/"
    private const val baseAbhaUrl = "https://healthidsbx.abdm.gov.in/api/"

    private val baseClient =
        OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            .addInterceptor(ContentTypeInterceptor())
            .build()

    val gson: Gson = GsonBuilder()
        .serializeNulls()
        .create()

    @Singleton
    @Provides
    fun provideMoshiInstance(): Moshi {
        return Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    @Singleton
    @Provides
    @Named("uatClient")
    fun provideTmcHttpClient(): OkHttpClient {
        return baseClient
            .newBuilder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .addInterceptor(TokenInsertTmcInterceptor())
            .build()
    }

    @Singleton
    @Provides
    @Named("abhaClient")
    fun provideAbhaHttpClient(): OkHttpClient {
        return baseClient
            .newBuilder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .addInterceptor(TokenInsertAbhaInterceptor())
            .build()
    }


    @Singleton
    @Provides
    fun provideAmritApiService(
        moshi: Moshi,
        @Named("uatClient") httpClient: OkHttpClient
    ): AmritApiService {
        return Retrofit.Builder()
//            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .addConverterFactory(GsonConverterFactory.create(gson))
            .baseUrl(baseTmcUrl)
            .client(httpClient)
            .build()
            .create(AmritApiService::class.java)
    }

    @Singleton
    @Provides
    fun provideAbhaApiService(
        moshi: Moshi,
        @Named("abhaClient") httpClient: OkHttpClient
    ): AbhaApiService {
        return Retrofit.Builder()
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            //.addConverterFactory(GsonConverterFactory.create())
            .baseUrl(baseAbhaUrl)
            .client(httpClient)
            .build()
            .create(AbhaApiService::class.java)
    }

    @Singleton
    @Provides
    fun provideRoomDatabase(@ApplicationContext context: Context) = InAppDb.getInstance(context)

    @Singleton
    @Provides
    fun provideHouseholdDao(database : InAppDb) : HouseholdDao = database.householdDao

    @Singleton
    @Provides
    fun provideBenDao(database : InAppDb) : BenDao = database.benDao


    @Singleton
    @Provides
    fun provideBenIdDao(database : InAppDb) : BeneficiaryIdsAvailDao = database.benIdGenDao

    @Singleton
    @Provides
    fun provideCbacDao(database : InAppDb) : CbacDao = database.cbacDao

    @Singleton
    @Provides
    fun provideVaccineDao(database : InAppDb) : ImmunizationDao = database.vaccineDao

    @Singleton
    @Provides
    fun provideMaternalHealthDao(database : InAppDb) : MaternalHealthDao = database.maternalHealthDao

    @Singleton
    @Provides
    fun provideTBDao(database : InAppDb) : TBDao = database.tbDao

    @Singleton
    @Provides
    fun provideDeliveryOutcomeDao(database : InAppDb) : DeliveryOutcomeDao = database.deliveryOutcomeDao

    @Singleton
    @Provides
    fun provideInfantRegDao(database : InAppDb) : InfantRegDao = database.infantRegDao

    @Singleton
    @Provides
    fun provideChildRegDao(database : InAppDb) : ChildRegistrationDao = database.childRegistrationDao

    @Singleton
    @Provides
    fun provideSyncDao(database : InAppDb) : SyncDao = database.syncDao

    @Singleton
    @Provides
    fun providePreferenceDao(@ApplicationContext context: Context) = PreferenceDao(context)


}