package com.meufreela.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.meufreela.app.databinding.ActivityLoginBinding;
import com.meufreela.app.models.request.LoginRequest;
import com.meufreela.app.models.response.LoginResponse;
import com.meufreela.app.network.ApiService;
import com.meufreela.app.network.RetrofitClient;
import com.meufreela.app.storage.TokenManager;
import com.meufreela.app.utils.ValidacaoUtils;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private TokenManager tokenManager;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        tokenManager = new TokenManager(this);
        apiService = RetrofitClient.getApiService(tokenManager);

        binding.botaoEntrar.setOnClickListener(v -> realizarLogin());
        binding.textoCadastro.setOnClickListener(v ->
                startActivity(new Intent(this, CadastroActivity.class)));
    }

    private void realizarLogin() {
        String email = binding.editEmail.getText().toString().trim();
        String senha = binding.editSenha.getText().toString();

        if (!ValidacaoUtils.emailValido(email)) {
            binding.editEmail.setError("E-mail inválido");
            return;
        }
        if (senha.length() < 6) {
            binding.editSenha.setError("Senha deve ter pelo menos 6 caracteres");
            return;
        }

        mostrarLoading(true);

        apiService.login(new LoginRequest(email, senha))
                .enqueue(new Callback<LoginResponse>() {
                    @Override
                    public void onResponse(Call<LoginResponse> call,
                                           Response<LoginResponse> response) {
                        mostrarLoading(false);
                        if (response.isSuccessful() && response.body() != null) {
                            LoginResponse dados = response.body();
                            tokenManager.salvarSessao(
                                    dados.getToken(),
                                    dados.getRefreshToken(),
                                    dados.getUsuario().getId(),
                                    dados.getUsuario().getTipo()
                            );
                            redirecionarPorTipo(dados.getUsuario().getTipo());
                        } else {
                            try {
                                String erroJson = response.errorBody().string();
                                JSONObject obj = new JSONObject(erroJson);
                                String mensagem = obj.getString("mensagem");
                                Toast.makeText(LoginActivity.this, mensagem,
                                        Toast.LENGTH_LONG).show();
                            } catch (Exception e) {
                                Toast.makeText(LoginActivity.this,
                                        "E-mail ou senha inválidos",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<LoginResponse> call, Throwable t) {
                        mostrarLoading(false);
                        Toast.makeText(LoginActivity.this,
                                "Erro de conexão: " + t.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void redirecionarPorTipo(String tipo) {
        Intent intent;
        if ("FREELANCER".equals(tipo)) {
            intent = new Intent(this, MainFreelancerActivity.class);
        } else {
            intent = new Intent(this, MainClienteActivity.class);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void mostrarLoading(boolean ativo) {
        binding.progressBar.setVisibility(ativo ? View.VISIBLE : View.GONE);
        binding.botaoEntrar.setEnabled(!ativo);
    }
}