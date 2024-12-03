package com.example.volunteerkim;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;

public interface NaverSearchAPI {
    @Headers({
            "X-Naver-Client-Id: IFGDQpfj72GnLDrnrcOI",
            "X-Naver-Client-Secret: NfJj8V6SCv"
    })
    @GET("v1/search/local.json")
    Call<SearchResult> searchPlace(
            @Query("query") String query,
            @Query("display") int display);
}

