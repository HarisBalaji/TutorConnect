package com.example.tutormsg

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("signup")
    fun signup(@Body user: User): Call<ResponseBody>

    @POST("signin")
    fun signin(@Body user: User): Call<TokenResponse>
}