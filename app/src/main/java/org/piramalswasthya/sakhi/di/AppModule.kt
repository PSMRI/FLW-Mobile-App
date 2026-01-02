package org.piramalswasthya.sakhi.di

import android.content.Context
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.piramalswasthya.sakhi.BuildConfig
import org.piramalswasthya.sakhi.database.room.InAppDb
import org.piramalswasthya.sakhi.database.room.NcdReferalDao
import org.piramalswasthya.sakhi.database.room.dao.ABHAGenratedDao
import org.piramalswasthya.sakhi.database.room.dao.AdolescentHealthDao
import org.piramalswasthya.sakhi.database.room.dao.AesDao
import org.piramalswasthya.sakhi.database.room.dao.BenDao
import org.piramalswasthya.sakhi.database.room.dao.BeneficiaryIdsAvailDao
import org.piramalswasthya.sakhi.database.room.dao.CbacDao
import org.piramalswasthya.sakhi.database.room.dao.CdrDao
import org.piramalswasthya.sakhi.database.room.dao.ChildRegistrationDao
import org.piramalswasthya.sakhi.database.room.dao.DeliveryOutcomeDao
import org.piramalswasthya.sakhi.database.room.dao.FilariaDao
import org.piramalswasthya.sakhi.database.room.dao.GeneralOpdDao
import org.piramalswasthya.sakhi.database.room.dao.HbncDao
import org.piramalswasthya.sakhi.database.room.dao.HbycDao
import org.piramalswasthya.sakhi.database.room.dao.HouseholdDao
import org.piramalswasthya.sakhi.database.room.dao.ImmunizationDao
import org.piramalswasthya.sakhi.database.room.dao.IncentiveDao
import org.piramalswasthya.sakhi.database.room.dao.InfantRegDao
import org.piramalswasthya.sakhi.database.room.dao.KalaAzarDao
import org.piramalswasthya.sakhi.database.room.dao.LeprosyDao
import org.piramalswasthya.sakhi.database.room.dao.MaaMeetingDao
import org.piramalswasthya.sakhi.database.room.dao.MalariaDao
import org.piramalswasthya.sakhi.database.room.dao.MaternalHealthDao
import org.piramalswasthya.sakhi.database.room.dao.MdsrDao
import org.piramalswasthya.sakhi.database.room.dao.MosquitoNetFormResponseDao
import org.piramalswasthya.sakhi.database.room.dao.PmsmaDao
import org.piramalswasthya.sakhi.database.room.dao.PncDao
import org.piramalswasthya.sakhi.database.room.dao.ProfileDao
import org.piramalswasthya.sakhi.database.room.dao.SaasBahuSammelanDao
import org.piramalswasthya.sakhi.database.room.dao.SyncDao
import org.piramalswasthya.sakhi.database.room.dao.TBDao
import org.piramalswasthya.sakhi.database.room.dao.UwinDao
import org.piramalswasthya.sakhi.database.room.dao.VLFDao
import org.piramalswasthya.sakhi.database.room.dao.dynamicSchemaDao.BenIfaFormResponseJsonDao
import org.piramalswasthya.sakhi.database.room.dao.dynamicSchemaDao.CUFYFormResponseJsonDao
import org.piramalswasthya.sakhi.database.room.dao.dynamicSchemaDao.EyeSurgeryFormResponseJsonDao
import org.piramalswasthya.sakhi.database.room.dao.dynamicSchemaDao.FilariaMDAFormResponseJsonDao
import org.piramalswasthya.sakhi.database.room.dao.dynamicSchemaDao.FormResponseANCJsonDao
import org.piramalswasthya.sakhi.database.room.dao.dynamicSchemaDao.FormResponseJsonDao
import org.piramalswasthya.sakhi.database.room.dao.dynamicSchemaDao.FormResponseJsonDaoHBYC
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.AnalyticsHelper
import org.piramalswasthya.sakhi.helpers.ApiAnalyticsInterceptor
import org.piramalswasthya.sakhi.network.AbhaApiService
import org.piramalswasthya.sakhi.network.AmritApiService
import org.piramalswasthya.sakhi.network.interceptors.ContentTypeInterceptor
import org.piramalswasthya.sakhi.network.interceptors.LoggingInterceptor
import org.piramalswasthya.sakhi.network.interceptors.TokenAuthenticator
import org.piramalswasthya.sakhi.network.interceptors.TokenInsertAbhaInterceptor
import org.piramalswasthya.sakhi.network.interceptors.TokenInsertTmcInterceptor
import org.piramalswasthya.sakhi.utils.KeyUtils
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // Named qualifiers
    const val AUTH_CLIENT = "authClient"
    const val AUTH_API = "authApi"
    const val UAT_CLIENT = "uatClient"
    const val ABHA_CLIENT = "abhaClient"

    // AUTH client (NO interceptors, for refresh calls only)
    @Singleton
    @Provides
    @Named(AUTH_CLIENT)
    fun provideAuthClient(loggingInterceptor: HttpLoggingInterceptor): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(loggingInterceptor)
            .build()
    }

    @Singleton
    @Provides
    @Named(AUTH_API)
    fun provideAuthApiService(
        moshi: Moshi,
        @Named(AUTH_CLIENT) httpClient: OkHttpClient
    ): AmritApiService {
        return Retrofit.Builder()
            .baseUrl(KeyUtils.baseTMCUrl())
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .client(httpClient)
            .build()
            .create(AmritApiService::class.java)
    }

    // Main UAT client (with interceptors + authenticator)
    @Singleton
    @Provides
    @Named(UAT_CLIENT)
    fun provideUatHttpClient(
        apiAnalyticsInterceptor: ApiAnalyticsInterceptor,
        tokenInsertTmcInterceptor: TokenInsertTmcInterceptor,
        tokenAuthenticator: TokenAuthenticator,
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient {
        return baseClient
            .newBuilder()
            .addInterceptor(tokenInsertTmcInterceptor)
            .addInterceptor(apiAnalyticsInterceptor)
            .addInterceptor(loggingInterceptor)
            .authenticator(tokenAuthenticator) // attach authenticator for 401 handling
            .build()
    }

    @Singleton
    @Provides
    fun provideAmritApiService(
        moshi: Moshi,
        @Named(UAT_CLIENT) httpClient: OkHttpClient
    ): AmritApiService {
        return Retrofit.Builder()
            .baseUrl(KeyUtils.baseTMCUrl())
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .client(httpClient)
            .build()
            .create(AmritApiService::class.java)
    }

    @Singleton
    @Provides
    fun provideTokenInsertTmcInterceptor(): TokenInsertTmcInterceptor {
        return TokenInsertTmcInterceptor()
    }

    @Singleton
    @Provides
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor {
        val loggingInterceptor = HttpLoggingInterceptor(LoggingInterceptor()).apply {
            level =
                if (BuildConfig.DEBUG)
                    HttpLoggingInterceptor.Level.BODY
                else
                    HttpLoggingInterceptor.Level.NONE
        }
        return loggingInterceptor
    }

    // TokenAuthenticator provider
    @Singleton
    @Provides
    fun provideTokenAuthenticator(
        pref: PreferenceDao,
        @Named(AUTH_API) authApi: AmritApiService
    ): TokenAuthenticator {
        return TokenAuthenticator(pref, authApi)
    }

    @Singleton
    @Provides
    @Named(ABHA_CLIENT)
    fun provideAbhaHttpClient(loggingInterceptor: HttpLoggingInterceptor): OkHttpClient {
        return baseClient
            .newBuilder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .addInterceptor(TokenInsertAbhaInterceptor())
            .addInterceptor(loggingInterceptor)
            .build()
    }

    @Singleton
    @Provides
    fun provideAbhaApiService(
        moshi: Moshi,
        @Named(ABHA_CLIENT) httpClient: OkHttpClient
    ): AbhaApiService {
        return Retrofit.Builder()
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            //.addConverterFactory(GsonConverterFactory.create())
            .baseUrl(KeyUtils.baseAbhaUrl())
            .client(httpClient)
            .build()
            .create(AbhaApiService::class.java)
    }

    private val baseClient =
        OkHttpClient.Builder()
            .addInterceptor(ContentTypeInterceptor())
            .build()

    @Singleton
    @Provides
    fun provideMoshiInstance(): Moshi {
        return Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    // for dynamic data
    @Singleton
    @Provides
    @Named("gsonAmritApi")
    fun provideGsonBasedAmritApiService(
        @Named("uatClient") httpClient: OkHttpClient
    ): AmritApiService {
        return Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create()) // ✅ Only for this API
            .baseUrl(KeyUtils.baseTMCUrl())
            .client(httpClient)
            .build()
            .create(AmritApiService::class.java)
    }

    @Singleton
    @Provides
    fun provideRoomDatabase(@ApplicationContext context: Context) = InAppDb.getInstance(context)

    @Provides
    @Singleton
    fun provideAnalyticsHelper(
        @ApplicationContext context: Context
    ): AnalyticsHelper {
        return AnalyticsHelper(context)
    }

    @Provides
    @Singleton
    fun provideApiAnalyticsInterceptor(
        @ApplicationContext context: Context
    ): ApiAnalyticsInterceptor {
        return ApiAnalyticsInterceptor(context)
    }

    @Singleton
    @Provides
    fun provideHouseholdDao(database: InAppDb): HouseholdDao = database.householdDao

    @Singleton
    @Provides
    fun provideBenDao(database: InAppDb): BenDao = database.benDao

    @Singleton
    @Provides
    fun provideAdolescentHealthDao(database: InAppDb): AdolescentHealthDao = database.adolescentHealthDao


    @Singleton
    @Provides
    fun provideBenIdDao(database: InAppDb): BeneficiaryIdsAvailDao = database.benIdGenDao

    @Singleton
    @Provides
    fun provideCbacDao(database: InAppDb): CbacDao = database.cbacDao

    @Singleton
    @Provides
    fun provideVaccineDao(database: InAppDb): ImmunizationDao = database.vaccineDao

    @Singleton
    @Provides
    fun provideMaternalHealthDao(database: InAppDb): MaternalHealthDao = database.maternalHealthDao

    @Singleton
    @Provides
    fun providePncDao(database: InAppDb): PncDao = database.pncDao

    @Singleton
    @Provides
    fun provideTBDao(database: InAppDb): TBDao = database.tbDao

    @Singleton
    @Provides
    fun provideDeliveryOutcomeDao(database: InAppDb): DeliveryOutcomeDao =
        database.deliveryOutcomeDao

    @Singleton
    @Provides
    fun provideInfantRegDao(database: InAppDb): InfantRegDao = database.infantRegDao

    @Singleton
    @Provides
    fun provideChildRegDao(database: InAppDb): ChildRegistrationDao = database.childRegistrationDao

    @Singleton
    @Provides
    fun provideSyncDao(database: InAppDb): SyncDao = database.syncDao

    @Singleton
    @Provides
    fun providePreferenceDao(@ApplicationContext context: Context) = PreferenceDao(context)

    @Singleton
    @Provides
    fun providePmsmaDao(database: InAppDb): PmsmaDao = database.pmsmaDao

    @Singleton
    @Provides
    fun provideMdsrDao(database: InAppDb): MdsrDao = database.mdsrDao

    @Singleton
    @Provides
    fun provideCdrDao(database: InAppDb): CdrDao = database.cdrDao

    @Singleton
    @Provides
    fun provideIncentiveDao(database: InAppDb): IncentiveDao = database.incentiveDao

    @Singleton
    @Provides
    fun provideMalariaDao(database: InAppDb): MalariaDao = database.malariaDao

    @Singleton
    @Provides
    fun provideKalaAzarDao(database: InAppDb): KalaAzarDao = database.kalaAzarDao

    @Singleton
    @Provides
    fun provideFilariaDao(database: InAppDb): FilariaDao = database.filariaDao

    @Singleton
    @Provides
    fun provideLeprosyDao(database: InAppDb): LeprosyDao = database.leprosyDao

    @Singleton
    @Provides
    fun provideAESDao(database: InAppDb): AesDao = database.aesDao

    @Singleton
    @Provides
    fun provideHBNCDao(database: InAppDb): HbncDao = database.hbncDao

    @Singleton
    @Provides
    fun provideHBYCDao(database: InAppDb): HbycDao = database.hbycDao

    @Singleton
    @Provides
    fun provideVlfDao(database: InAppDb): VLFDao = database.vlfDao

    @Singleton
    @Provides
    fun provideAshaProfileDao(database: InAppDb): ProfileDao = database.profileDao

    @Singleton
    @Provides
    fun provideABHAGenDao(database: InAppDb): ABHAGenratedDao = database.abhaGenratedDao

    @Singleton
    @Provides
    fun provideFormResponseJsonDao(database: InAppDb): FormResponseJsonDao = database.formResponseJsonDao()

    @Singleton
    @Provides
    fun provideFormSaasBahuSamelanDao(database: InAppDb): SaasBahuSammelanDao = database.saasBahuSammelanDao

    @Singleton
    @Provides
    fun formResponseJsonDaoHBYC(database: InAppDb): FormResponseJsonDaoHBYC = database.formResponseJsonDaoHBYC()

    @Singleton
    @Provides
    fun formResponseJsonDaoANC(database: InAppDb): FormResponseANCJsonDao = database.formResponseJsonDaoANC()

    @Singleton
    @Provides
    fun provideUwinDao(database: InAppDb) : UwinDao = database.uwinDao

    @Singleton
    @Provides
    fun provideGenOPDDao(database: InAppDb): GeneralOpdDao = database.generalOpdDao


    @Singleton
    @Provides
    fun provideMaaMeetingDao(database: InAppDb): MaaMeetingDao = database.maaMeetingDao

    @Singleton
    @Provides
    fun provideCUFYFormResponseJsonDao(database: InAppDb): CUFYFormResponseJsonDao = database.CUFYFormResponseJsonDao()

    @Singleton
    @Provides
    fun provideEyeSurgeryFormResponseJsonDao(database: InAppDb): EyeSurgeryFormResponseJsonDao = database.formResponseJsonDaoEyeSurgery()

    @Singleton
    @Provides
    fun provideBenIfaFormResponseJsonDao(database: InAppDb): BenIfaFormResponseJsonDao = database.formResponseJsonDaoBenIfa()
    @Singleton
    @Provides
    fun provideMosquitoNetFormResponseDao(database: InAppDb): MosquitoNetFormResponseDao = database.formResponseMosquitoNetJsonDao()
    @Singleton
    @Provides
    fun provideFilariaMDAFormResponseDao(database: InAppDb): FilariaMDAFormResponseJsonDao = database.formResponseFilariaMDAJsonDao()

    @Singleton
    @Provides
    fun provideNcdReferDao(database: InAppDb): NcdReferalDao = database.referalDao

}