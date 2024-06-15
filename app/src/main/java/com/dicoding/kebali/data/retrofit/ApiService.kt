package com.dicoding.kebali.data.retrofit

import com.google.gson.annotations.JsonAdapter
import retrofit2.http.Field
import retrofit2.http.Header
import retrofit2.http.POST


//I don't know
interface ApiService {
    @POST("translate-endpoint-ydhrwqtw7a-et.a.run.app")
    suspend fun translate(
        @Field("src_lan") src_lan: String,
        @Field("dst_lan") dst_lan: String,
        @Field("input_text") input_text: String
    )
}