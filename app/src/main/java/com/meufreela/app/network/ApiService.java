package com.meufreela.app.network;

import com.meufreela.app.models.request.CadastroRequest;
import com.meufreela.app.models.request.LoginRequest;
import com.meufreela.app.models.response.LoginResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {
    @POST("auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @POST("auth/cadastro")
    Call<LoginResponse> cadastrar(@Body CadastroRequest request);
}