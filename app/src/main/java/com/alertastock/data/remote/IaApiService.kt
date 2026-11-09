package com.alertastock.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface IaApiService {
    @GET("health")
    suspend fun health(): Map<String, String>

    @POST("predecir")
    suspend fun predecir(@Body request: PrediccionIaRequest): PrediccionIaResponse
}
