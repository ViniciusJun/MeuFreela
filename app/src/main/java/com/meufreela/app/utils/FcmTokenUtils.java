package com.meufreela.app.utils;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.messaging.FirebaseMessaging;
import com.meufreela.app.models.request.FcmTokenRequest;
import com.meufreela.app.network.ApiService;
import com.meufreela.app.network.RetrofitClient;
import com.meufreela.app.storage.TokenManager;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FcmTokenUtils {

    public static void solicitarPermissao(Activity activity) {
        if (Build.VERSION.SDK_INT >= 33) {
            if (ContextCompat.checkSelfPermission(activity,
                    Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1001);
            }
        }
    }

    public static void registrarTokenNoBackend(Activity activity) {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (!task.isSuccessful() || task.getResult() == null) return;

            String token = task.getResult();
            TokenManager tm = new TokenManager(activity);
            ApiService api = RetrofitClient.getApiService(tm);

            api.salvarFcmToken(new FcmTokenRequest(token))
                    .enqueue(new Callback<ResponseBody>() {
                        @Override public void onResponse(Call<ResponseBody> call,
                                                         Response<ResponseBody> r) { }
                        @Override public void onFailure(Call<ResponseBody> call,
                                                        Throwable t) { }
                    });
        });
    }
}