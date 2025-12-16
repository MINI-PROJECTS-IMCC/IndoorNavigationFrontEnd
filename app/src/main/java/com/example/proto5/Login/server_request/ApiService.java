package com.example.proto5.Login.server_request;

import com.example.proto5.Login.models.User;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {
    @POST("api/auth/register")
    public Call<String> registerUser(@Body User user);

    @POST("api/auth/login")
    public Call<String> loginUser(@Body User user);
}
