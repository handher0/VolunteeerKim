package com.example.volunteerkim;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class SearchResult {
    @SerializedName("addresses")
    public List<SearchAddress> SearchAddress;

    @SerializedName("status")
    public String status;

    @SerializedName("errorMessage")
    public String errorMessage;
}