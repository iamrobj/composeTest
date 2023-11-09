package com.robj.deblocktest.di.modules

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.robj.deblocktest.networking.ApiClient
import com.robj.deblocktest.BuildConfig
import com.robj.deblocktest.networking.EthereumApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class ApiModule @Inject constructor() {

    @Provides
    @Singleton
    fun provideOkHttp(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = if (BuildConfig.DEBUG) {
                        HttpLoggingInterceptor.Level.BODY
                    } else {
                        HttpLoggingInterceptor.Level.NONE
                    }
                }
            )
            .callTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .connectTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    @OptIn(ExperimentalSerializationApi::class)
    @Provides
    @Singleton
    fun provideRetrofitBuilder(okHttpClient: OkHttpClient, json: Json): Retrofit.Builder {
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .client(okHttpClient)
            .addConverterFactory(
                json.asConverterFactory(contentType)
            )
    }

    @Provides
    @Singleton
    fun provideJson() = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    @Provides
    @Singleton
    fun provideEthereumApiService(retrofitBuilder: Retrofit.Builder): EthereumApiService =
        retrofitBuilder
            .baseUrl(BuildConfig.BASE_API_URL)
            .build()
            .create(EthereumApiService::class.java)

    @Provides
    @Singleton
    fun provideApiClient(
        ethereumApiService: EthereumApiService
    ) = ApiClient(
        ethereumApiService = ethereumApiService,
    )

}