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
import org.piramalswasthya.sakhi.database.room.InAppDb
import org.piramalswasthya.sakhi.database.room.dao.BenDao
import org.piramalswasthya.sakhi.database.room.dao.BeneficiaryIdsAvailDao
import org.piramalswasthya.sakhi.database.room.dao.CbacDao
import org.piramalswasthya.sakhi.database.room.dao.CdrDao
import org.piramalswasthya.sakhi.database.room.dao.ChildRegistrationDao
import org.piramalswasthya.sakhi.database.room.dao.DeliveryOutcomeDao
import org.piramalswasthya.sakhi.database.room.dao.FilariaDao
import org.piramalswasthya.sakhi.database.room.dao.HbncDao
import org.piramalswasthya.sakhi.database.room.dao.HbycDao
import org.piramalswasthya.sakhi.database.room.dao.HouseholdDao
import org.piramalswasthya.sakhi.database.room.dao.ImmunizationDao
import org.piramalswasthya.sakhi.database.room.dao.IncentiveDao
import org.piramalswasthya.sakhi.database.room.dao.InfantRegDao
import org.piramalswasthya.sakhi.database.room.dao.KalaAzarDao
import org.piramalswasthya.sakhi.database.room.dao.LeprosyDao
import org.piramalswasthya.sakhi.database.room.dao.MalariaDao
import org.piramalswasthya.sakhi.database.room.dao.MaternalHealthDao
import org.piramalswasthya.sakhi.database.room.dao.MdsrDao
import org.piramalswasthya.sakhi.database.room.dao.PmsmaDao
import org.piramalswasthya.sakhi.database.room.dao.PncDao
import org.piramalswasthya.sakhi.database.room.dao.ProfileDao
import org.piramalswasthya.sakhi.database.room.dao.SyncDao
import org.piramalswasthya.sakhi.database.room.dao.TBDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.network.AbhaApiService
import org.piramalswasthya.sakhi.network.AmritApiService
import org.piramalswasthya.sakhi.network.interceptors.ContentTypeInterceptor
import org.piramalswasthya.sakhi.network.interceptors.TokenInsertAbhaInterceptor
import org.piramalswasthya.sakhi.network.interceptors.TokenInsertTmcInterceptor
import org.piramalswasthya.sakhi.utils.KeyUtils
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    private val baseClient =
        OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            .addInterceptor(ContentTypeInterceptor())
            .build()

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
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            //.addConverterFactory(GsonConverterFactory.create())
            .baseUrl(KeyUtils.baseTMCUrl())
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
            .baseUrl(KeyUtils.baseAbhaUrl())
            .client(httpClient)
            .build()
            .create(AbhaApiService::class.java)
    }

    @Singleton
    @Provides
    fun provideRoomDatabase(@ApplicationContext context: Context) = InAppDb.getInstance(context)

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
    fun provideAshaProfileDao(database: InAppDb): ProfileDao = database.profileDao


}