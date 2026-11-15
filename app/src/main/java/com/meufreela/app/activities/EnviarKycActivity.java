package com.meufreela.app.activities;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.meufreela.app.databinding.ActivityEnviarKycBinding;
import com.meufreela.app.network.ApiService;
import com.meufreela.app.network.RetrofitClient;
import com.meufreela.app.storage.TokenManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EnviarKycActivity extends AppCompatActivity {

    private ActivityEnviarKycBinding binding;
    private ApiService api;
    private Uri uriDocumento, uriSelfie;

    private ActivityResultLauncher<String> pickDocumento;
    private ActivityResultLauncher<String> pickSelfie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEnviarKycBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        TokenManager tm = new TokenManager(this);
        api = RetrofitClient.getApiService(tm);

        pickDocumento = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                uriDocumento = uri;
                binding.botaoDocumento.setText("✅ Documento anexado");
                checarBotoes();
            }
        });

        pickSelfie = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                uriSelfie = uri;
                binding.botaoSelfie.setText("✅ Selfie anexada");
                checarBotoes();
            }
        });

        binding.botaoDocumento.setOnClickListener(v -> pickDocumento.launch("image/*"));
        binding.botaoSelfie.setOnClickListener(v -> pickSelfie.launch("image/*"));
        binding.botaoEnviar.setOnClickListener(v -> enviarKyc());
    }

    private void checarBotoes() {
        binding.botaoEnviar.setEnabled(uriDocumento != null && uriSelfie != null);
    }

    private void enviarKyc() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.botaoEnviar.setEnabled(false);

        try {
            MultipartBody.Part docPart = criarParte(uriDocumento, "documento");
            MultipartBody.Part selfiePart = criarParte(uriSelfie, "selfie");

            api.enviarDocumentosKyc(docPart, selfiePart).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    binding.progressBar.setVisibility(View.GONE);
                    if (response.isSuccessful()) {
                        Toast.makeText(EnviarKycActivity.this, "Documentos enviados! Em análise.", Toast.LENGTH_LONG).show();
                        finish();
                    } else {
                        Toast.makeText(EnviarKycActivity.this, "Erro ao enviar. Verifique os arquivos.", Toast.LENGTH_LONG).show();
                        binding.botaoEnviar.setEnabled(true);
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(EnviarKycActivity.this, "Erro de conexão: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    binding.botaoEnviar.setEnabled(true);
                }
            });
        } catch (Exception e) {
            binding.progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "Erro: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            binding.botaoEnviar.setEnabled(true);
        }
    }

    private MultipartBody.Part criarParte(Uri uri, String campo) throws Exception {
        String mimeType = getContentResolver().getType(uri);
        if (mimeType == null) mimeType = "image/jpeg";

        File temp = new File(getCacheDir(), campo + ".jpg");
        try (InputStream is = getContentResolver().openInputStream(uri);
             FileOutputStream fos = new FileOutputStream(temp)) {
            byte[] buf = new byte[8192];
            int n;
            while ((n = is.read(buf)) > 0) fos.write(buf, 0, n);
        }

        RequestBody rb = RequestBody.create(temp, MediaType.parse(mimeType));
        return MultipartBody.Part.createFormData(campo, temp.getName(), rb);
    }
}