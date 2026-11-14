package com.meufreela.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.meufreela.app.databinding.ActivityPerfilBinding;
import com.meufreela.app.models.response.PerfilUsuario;
import com.meufreela.app.network.ApiService;
import com.meufreela.app.network.RetrofitClient;
import com.meufreela.app.storage.TokenManager;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PerfilActivity extends AppCompatActivity {

    private ActivityPerfilBinding binding;
    private TokenManager tokenManager;
    private ApiService api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPerfilBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        tokenManager = new TokenManager(this);
        api = RetrofitClient.getApiService(tokenManager);

        binding.botaoSair.setOnClickListener(v -> confirmarLogout());

        binding.botaoTornarFreelancer.setOnClickListener(v ->
                startActivity(new Intent(this, TornarFreelancerActivity.class)));

        // ✅ Botão Tornar-se Cliente
        binding.botaoTornarCliente.setOnClickListener(v -> tornarCliente());

        carregarPerfil();
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

                    String tipo = "FREELANCER".equals(u.getTipo())
                            ? "Freelancer" : "Cliente";
                    binding.textTipo.setText("Tipo de conta: " + tipo);

                    binding.textVerificado.setVisibility(
                            u.isVerificado() ? View.VISIBLE : View.GONE);

                    boolean ehFreelancer = u.temPapel("FREELANCER")
                            || "FREELANCER".equals(u.getTipo());
                    boolean ehCliente = u.temPapel("CLIENTE")
                            || "CLIENTE".equals(u.getTipo());

                    // Mostra o botão de virar freelancer se ainda não for
                    binding.botaoTornarFreelancer.setVisibility(
                            ehFreelancer ? View.GONE : View.VISIBLE);

                    // Mostra o botão de virar cliente se ainda não for
                    binding.botaoTornarCliente.setVisibility(
                            ehCliente ? View.GONE : View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<PerfilUsuario> call, Throwable t) {
                Toast.makeText(PerfilActivity.this,
                        "Erro: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void tornarCliente() {
        new AlertDialog.Builder(this)
                .setTitle("Ativar perfil de cliente")
                .setMessage("Você poderá contratar serviços de outros freelancers. Deseja ativar?")
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
                            } else {
                                try {
                                    org.json.JSONObject obj = new org.json.JSONObject(
                                            response.errorBody().string());
                                    Toast.makeText(PerfilActivity.this,
                                            obj.getString("mensagem"),
                                            Toast.LENGTH_LONG).show();
                                } catch (Exception e) {
                                    Toast.makeText(PerfilActivity.this,
                                            "Erro ao ativar", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            Toast.makeText(PerfilActivity.this,
                                    "Erro de conexão: " + t.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
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