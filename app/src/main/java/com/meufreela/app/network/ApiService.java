package com.meufreela.app.network;

import com.meufreela.app.models.request.CadastroRequest;
import com.meufreela.app.models.request.LoginRequest;
import com.meufreela.app.models.response.Freelancer;
import com.meufreela.app.models.response.LoginResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiService {
    @POST("auth/login")     // → http://10.0.2.2:8080/api/auth/login
    Call<LoginResponse> login(@Body LoginRequest request);

    @POST("auth/cadastro")  // → http://10.0.2.2:8080/api/auth/cadastro
    Call<LoginResponse> cadastrar(@Body CadastroRequest request);

    @GET("freelancers")
    Call<List<Freelancer>> listarFreelancers(@Query("categoria") String categoria);
}