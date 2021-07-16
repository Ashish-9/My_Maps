package com.example.testapp.network;

import com.example.testapp.model.ListLocationModel;

import retrofit2.Call;
import retrofit2.http.GET;

public interface ApiService {
    @GET("api/posts/myposts")
    Call<ListLocationModel> getAllLocation();
}
