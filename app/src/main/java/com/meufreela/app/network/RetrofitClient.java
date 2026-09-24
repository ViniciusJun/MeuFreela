package com.meufreela.app.network;

import com.meufreela.app.config.ApiConfig;
import com.meufreela.app.storage.TokenManager;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static Retrofit retrofit = null;
    private static ApiService apiService = null;

    public static ApiService getApiService(TokenManager tokenManager) {
        if (apiService == null) {
            // Log das requisições (só em DEBUG!)
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(new AuthInterceptor(tokenManager))
                    .addInterceptor(logging)
                    .connectTimeout(ApiConfig.TIMEOUT_SEGUNDOS, TimeUnit.SECONDS)
                    .readTimeout(ApiConfig.TIMEOUT_SEGUNDOS, TimeUnit.SECONDS)
                    .writeTimeout(ApiConfig.TIMEOUT_SEGUNDOS, TimeUnit.SECONDS)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(ApiConfig.BASE_URL_DEV) // trocar para BASE_URL em produção
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            apiService = retrofit.create(ApiService.class);
        }
        return apiService;
    }
}