package com.meufreela.app.storage;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class TokenManager {

    private static final String PREFS_NAME = "meufreela_secure_prefs";
    private static final String KEY_ACCESS_TOKEN = "access_token";
    private static final String KEY_REFRESH_TOKEN = "refresh_token";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_TYPE = "user_type";

    private final SharedPreferences prefs;

    public TokenManager(Context context) {
        try {
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            prefs = EncryptedSharedPreferences.create(
                    context,
                    PREFS_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException("Falha ao inicializar armazenamento seguro", e);
        }
    }

    public void salvarSessao(String accessToken, String refreshToken,
                             String userId, String userType) {
        prefs.edit()
                .putString(KEY_ACCESS_TOKEN, accessToken)
                .putString(KEY_REFRESH_TOKEN, refreshToken)
                .putString(KEY_USER_ID, userId)
                .putString(KEY_USER_TYPE, userType)
                .apply();
    }

    public String getAccessToken() { return prefs.getString(KEY_ACCESS_TOKEN, null); }
    public String getRefreshToken() { return prefs.getString(KEY_REFRESH_TOKEN, null); }
    public String getUserId() { return prefs.getString(KEY_USER_ID, null); }
    public String getUserType() { return prefs.getString(KEY_USER_TYPE, null); }
    public boolean isLogado() { return getAccessToken() != null; }

    public void limparSessao() { prefs.edit().clear().apply(); }
}