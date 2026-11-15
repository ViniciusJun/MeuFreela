package com.meufreela.app.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.meufreela.app.config.ApiConfig;
import com.meufreela.app.databinding.ActivityPerfilBinding;
import com.meufreela.app.models.response.PerfilUsuario;
import com.meufreela.app.network.ApiService;
import com.meufreela.app.network.RetrofitClient;
import com.meufreela.app.storage.TokenManager;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PerfilActivity extends AppCompatActivity {

    private ActivityPerfilBinding binding;
    private TokenManager tokenManager;
    private ApiService api;
    private ActivityResultLauncher<String> pickImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPerfilBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        tokenManager = new TokenManager(this);
        api = RetrofitClient.getApiService(tokenManager);

        // Registra o picker de imagem
        pickImage = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> { if (uri != null) enviarFoto(uri); });

        binding.botaoEscolherFoto.setOnClickListener(v ->
                pickImage.launch("image/*"));

        binding.botaoSair.setOnClickListener(v -> confirmarLogout());

        binding.botaoTornarFreelancer.setOnClickListener(v ->
                startActivity(new Intent(this, TornarFreelancerActivity.class)));

        binding.botaoTornarCliente.setOnClickListener(v -> tornarCliente());
    }

    @Override
    protected void onResume() {
        super.onResume();
        carregarPerfil();
    }

    private void carregarPerfil() {
        api.meuPerfil().enqueue(new Callback<PerfilUsuario>() {
            @Override
            public void onResponse(Call<PerfilUsuario> call,
                                   Response<PerfilUsuario> response) {
                if (response.isSuccessful() && response.body() != null) {
                    PerfilUsuario u = response.body();

                    binding.editNome.setText(u.getNome());
                    binding.editEmail.setText(u.getEmail());
                    binding.textTipo.setText("Tipo de conta: "
                            + ("FREELANCER".equals(u.getTipo()) ? "Freelancer" : "Cliente"));
                    binding.textVerificado.setVisibility(
                            u.isVerificado() ? View.VISIBLE : View.GONE);

                    boolean ehFreelancer = u.temPapel("FREELANCER")
                            || "FREELANCER".equals(u.getTipo());
                    boolean ehCliente = u.temPapel("CLIENTE")
                            || "CLIENTE".equals(u.getTipo());
                    binding.botaoTornarFreelancer.setVisibility(
                            ehFreelancer ? View.GONE : View.VISIBLE);
                    binding.botaoTornarCliente.setVisibility(
                            ehCliente ? View.GONE : View.VISIBLE);

                    // Carrega foto (com Glide cacheado)
                    String fotoUrl = u.getFotoUrl();
                    if (fotoUrl != null && !fotoUrl.isEmpty()) {
                        String urlCompleta = ApiConfig.SERVER_URL_DEV + fotoUrl;
                        Glide.with(PerfilActivity.this)
                                .load(urlCompleta)
                                .placeholder(android.R.drawable.ic_menu_myplaces)
                                .error(android.R.drawable.ic_menu_myplaces)
                                .circleCrop()
                                .into(binding.imageFoto);
                    }
                }
            }

            @Override
            public void onFailure(Call<PerfilUsuario> call, Throwable t) {
                Toast.makeText(PerfilActivity.this,
                        "Erro: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void enviarFoto(Uri uri) {
        binding.progressFoto.setVisibility(View.VISIBLE);

        try {
            // Copia a URI para um arquivo temporário
            File tempFile = new File(getCacheDir(), "avatar_upload.jpg");
            try (InputStream is = getContentResolver().openInputStream(uri);
                 FileOutputStream fos = new FileOutputStream(tempFile)) {
                if (is == null) throw new RuntimeException("Não foi possível abrir");
                byte[] buffer = new byte[8192];
                int n;
                while ((n = is.read(buffer)) > 0) fos.write(buffer, 0, n);
            }

            MediaType mediaType = MediaType.parse("image/jpeg");
            RequestBody rb = RequestBody.create(tempFile, mediaType);
            MultipartBody.Part part = MultipartBody.Part
                    .createFormData("file", "avatar.jpg", rb);

            api.uploadFoto(part).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call,
                                       Response<ResponseBody> response) {
                    binding.progressFoto.setVisibility(View.GONE);
                    if (response.isSuccessful()) {
                        Toast.makeText(PerfilActivity.this,
                                "Foto atualizada!", Toast.LENGTH_SHORT).show();
                        carregarPerfil();
                    } else {
                        try {
                            JSONObject obj = new JSONObject(
                                    response.errorBody().string());
                            Toast.makeText(PerfilActivity.this,
                                    obj.getString("mensagem"),
                                    Toast.LENGTH_LONG).show();
                        } catch (Exception e) {
                            Toast.makeText(PerfilActivity.this,
                                    "Erro ao subir imagem", Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    binding.progressFoto.setVisibility(View.GONE);
                    Log.e("PERFIL", "Falha no upload", t);
                    Toast.makeText(PerfilActivity.this,
                            "Erro de conexão: " + t.getMessage(),
                            Toast.LENGTH_LONG).show();
                }
            });

        } catch (Exception e) {
            binding.progressFoto.setVisibility(View.GONE);
            Toast.makeText(this,
                    "Não foi possível ler a imagem", Toast.LENGTH_LONG).show();
        }
    }

    private void tornarCliente() {
        new AlertDialog.Builder(this)
                .setTitle("Ativar perfil de cliente")
                .setMessage("Você poderá contratar serviços. Deseja ativar?")
                .setPositiveButton("Sim", (d, w) -> {
                    api.tornarCliente().enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call,
                                               Response<ResponseBody> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(PerfilActivity.this,
                                        "Você agora também pode contratar! 🎉",
                                        Toast.LENGTH_LONG).show();
                                carregarPerfil();
                            }
                        }
                        @Override public void onFailure(Call<ResponseBody> c, Throwable t) {}
                    });
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void confirmarLogout() {
        new AlertDialog.Builder(this)
                .setTitle("Sair da conta")
                .setMessage("Deseja realmente sair?")
                .setPositiveButton("Sim", (d, w) -> fazerLogout())
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void fazerLogout() {
        tokenManager.limparSessao();
        Intent i = new Intent(this, LoginActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }
}