package com.meufreela.app.network;

import androidx.annotation.NonNull;

import com.meufreela.app.storage.TokenManager;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {

    private final TokenManager tokenManager;

    public AuthInterceptor(TokenManager tokenManager) {
        this.tokenManager = tokenManager;
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request original = chain.request();
        String token = tokenManager.getAccessToken();

        Request.Builder builder = original.newBuilder()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("X-App-Version", "1.0.0")
                .header("X-Platform", "android");

        if (token != null && !token.isEmpty()) {
            builder.header("Authorization", "Bearer " + token);
        }

        return chain.proceed(builder.build());
    }
}