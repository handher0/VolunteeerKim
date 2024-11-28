package com.example.volunteerkim;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;

public interface NaverMapAPI {
    @Headers({
            "X-NCP-APIGW-API-KEY-ID: cbxzt68nzc",
            "X-NCP-APIGW-API-KEY: SGB2TtRf2y4cW4cqCs15h5dQbsn2GVZGMbL4UgF2"
    })
    @GET("/map-geocode/v2/geocode")
    Call<SearchResult> searchLocation(@Query("query") String query);
}
