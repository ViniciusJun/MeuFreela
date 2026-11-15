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
import com.meufreela.app.models.response.SolicitacaoKycResponse;
import com.meufreela.app.network.ApiService;
import com.meufreela.app.network.RetrofitClient;
import com.meufreela.app.storage.TokenManager;

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

    private static final String TAG = "PERFIL_UPLOAD";

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

        pickImage = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    Log.d(TAG, "Picker retornou URI: " + uri);
                    if (uri != null) enviarFoto(uri);
                    else Log.w(TAG, "Picker retornou null (usuário cancelou?)");
                });

        binding.botaoEscolherFoto.setOnClickListener(v -> {
            Log.d(TAG, "FAB clicado — abrindo picker");
            try {
                pickImage.launch("image/*");
            } catch (Exception e) {
                Log.e(TAG, "Erro ao abrir picker", e);
                Toast.makeText(this, "Erro ao abrir seletor: " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });

        // Ação do botão de KYC
        binding.botaoVerificarKyc.setOnClickListener(v -> {
            startActivity(new Intent(this, EnviarKycActivity.class));
        });

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
            public void onResponse(Call<PerfilUsuario> call, Response<PerfilUsuario> response) {
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

                    // --- CORREÇÃO DO KYC AQUI ---
                    if (!ehFreelancer || u.isVerificado()) {
                        // Se é cliente, ou se já é verificado, o card NUNCA deve aparecer
                        binding.cardKyc.setVisibility(View.GONE);
                    } else {
                        // Só busca o status se for freelancer não verificado
                        carregarStatusKyc();
                    }

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
                Log.e(TAG, "Falha ao carregar perfil", t);
                Toast.makeText(PerfilActivity.this,
                        "Erro: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void carregarStatusKyc() {
        api.statusKyc().enqueue(new Callback<SolicitacaoKycResponse>() {
            @Override
            public void onResponse(Call<SolicitacaoKycResponse> call, Response<SolicitacaoKycResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String status = response.body().getStatus();

                    // TRAVA DE SEGURANÇA: Se o status for APROVADO, esconde o card!
                    if ("APROVADO".equals(status)) {
                        binding.cardKyc.setVisibility(View.GONE);
                        binding.textVerificado.setVisibility(View.VISIBLE);
                        return;
                    }

                    // Se chegou aqui, o usuário NÃO é verificado. Mostra o card.
                    binding.cardKyc.setVisibility(View.VISIBLE);

                    switch (status) {
                        case "NAO_ENVIADO":
                            binding.textKycStatus.setText("Status: Não enviado");
                            binding.botaoVerificarKyc.setVisibility(View.VISIBLE);
                            binding.botaoVerificarKyc.setText("Enviar Documentos");
                            binding.botaoVerificarKyc.setEnabled(true);
                            break;
                        case "EM_ANALISE":
                            binding.textKycStatus.setText("Status: Em análise");
                            binding.botaoVerificarKyc.setVisibility(View.GONE);
                            break;
                        case "REJEITADO":
                            String motivo = response.body().getMotivoRejeicao();
                            binding.textKycStatus.setText("Status: Recusado \nMotivo: " + (motivo != null ? motivo : "Não informado"));
                            binding.botaoVerificarKyc.setVisibility(View.VISIBLE);
                            binding.botaoVerificarKyc.setText("Reenviar Documentos");
                            binding.botaoVerificarKyc.setEnabled(true);
                            break;
                    }
                }
            }

            @Override
            public void onFailure(Call<SolicitacaoKycResponse> call, Throwable t) {
                Log.e(TAG, "Falha ao carregar status KYC", t);
                // Em caso de erro de rede, por segurança, esconde o card para não confundir
                binding.cardKyc.setVisibility(View.GONE);
            }
        });
    }

    private void enviarFoto(Uri uri) {
        Log.d(TAG, "enviarFoto iniciado, URI=" + uri);
        binding.progressFoto.setVisibility(View.VISIBLE);

        try {
            File tempFile = new File(getCacheDir(), "avatar_upload.jpg");

            try (InputStream is = getContentResolver().openInputStream(uri);
                 FileOutputStream fos = new FileOutputStream(tempFile)) {

                if (is == null) throw new RuntimeException("openInputStream retornou null");

                byte[] buffer = new byte[8192];
                int n;
                long total = 0;
                while ((n = is.read(buffer)) > 0) {
                    fos.write(buffer, 0, n);
                    total += n;
                }
                Log.d(TAG, "Arquivo copiado: " + total + " bytes");
            }

            if (tempFile.length() == 0) {
                throw new RuntimeException("Arquivo vazio após cópia");
            }

            MediaType mediaType = MediaType.parse("image/jpeg");
            RequestBody rb = RequestBody.create(tempFile, mediaType);
            MultipartBody.Part part = MultipartBody.Part
                    .createFormData("file", "avatar.jpg", rb);

            Log.d(TAG, "Chamando POST /api/usuarios/foto");

            api.uploadFoto(part).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call,
                                       Response<ResponseBody> response) {
                    binding.progressFoto.setVisibility(View.GONE);
                    Log.d(TAG, "Resposta upload: HTTP " + response.code());

                    if (response.isSuccessful()) {
                        Glide.with(PerfilActivity.this).clear(binding.imageFoto);
                        Toast.makeText(PerfilActivity.this,
                                "Foto atualizada!", Toast.LENGTH_SHORT).show();
                        carregarPerfil();
                    } else {
                        try {
                            String erro = response.errorBody().string();
                            Log.e(TAG, "Erro backend: " + erro);
                            Toast.makeText(PerfilActivity.this,
                                    "Erro: " + erro, Toast.LENGTH_LONG).show();
                        } catch (Exception e) {
                            Toast.makeText(PerfilActivity.this,
                                    "Erro HTTP " + response.code(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    binding.progressFoto.setVisibility(View.GONE);
                    Log.e(TAG, "Falha de rede", t);
                    Toast.makeText(PerfilActivity.this,
                            "Erro de conexão: " + t.getMessage(),
                            Toast.LENGTH_LONG).show();
                }
            });

        } catch (Exception e) {
            binding.progressFoto.setVisibility(View.GONE);
            Log.e(TAG, "Exceção antes do upload", e);
            Toast.makeText(this,
                    "Erro: " + e.getMessage(), Toast.LENGTH_LONG).show();
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