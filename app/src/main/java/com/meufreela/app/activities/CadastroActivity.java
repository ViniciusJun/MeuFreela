package com.meufreela.app.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.meufreela.app.databinding.ActivityCadastroBinding;
import com.meufreela.app.models.request.CadastroRequest;
import com.meufreela.app.models.response.LoginResponse;
import com.meufreela.app.network.ApiService;
import com.meufreela.app.network.RetrofitClient;
import com.meufreela.app.storage.TokenManager;
import com.meufreela.app.utils.ValidacaoUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CadastroActivity extends AppCompatActivity {

    private ActivityCadastroBinding binding;
    private TokenManager tokenManager;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCadastroBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        tokenManager = new TokenManager(this);
        apiService = RetrofitClient.getApiService(tokenManager);

        binding.botaoCadastrar.setOnClickListener(v -> realizarCadastro());
        binding.textoVoltarLogin.setOnClickListener(v -> finish());
    }

    private void realizarCadastro() {
        String nome = texto(binding.editNome);
        String email = texto(binding.editEmail);
        String telefone = texto(binding.editTelefone);
        String cpf = texto(binding.editCpf);
        String senha = texto(binding.editSenha);
        String confirmarSenha = texto(binding.editConfirmarSenha);
        String tipo = binding.radioFreelancer.isChecked() ? "FREELANCER" : "CLIENTE";

        // ----- Validações locais -----
        if (nome.length() < 3) {
            binding.editNome.setError("Informe seu nome completo");
            binding.editNome.requestFocus();
            return;
        }
        if (!ValidacaoUtils.emailValido(email)) {
            binding.editEmail.setError("E-mail inválido");
            binding.editEmail.requestFocus();
            return;
        }
        if (!ValidacaoUtils.telefoneValido(telefone)) {
            binding.editTelefone.setError("Telefone inválido");
            binding.editTelefone.requestFocus();
            return;
        }
        if (!ValidacaoUtils.cpfValido(cpf)) {
            binding.editCpf.setError("CPF inválido");
            binding.editCpf.requestFocus();
            return;
        }
        if (!ValidacaoUtils.senhaForte(senha)) {
            binding.editSenha.setError("Mín. 8 caracteres, com letras e números");
            binding.editSenha.requestFocus();
            return;
        }
        if (!senha.equals(confirmarSenha)) {
            binding.editConfirmarSenha.setError("As senhas não coincidem");
            binding.editConfirmarSenha.requestFocus();
            return;
        }
        if (!binding.checkTermos.isChecked()) {
            Toast.makeText(this,
                    "Você precisa aceitar os termos para continuar",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        mostrarLoading(true);

        CadastroRequest request = new CadastroRequest(
                nome, email, senha, telefone, cpf.replaceAll("\\D", ""), tipo);

        apiService.cadastrar(request).enqueue(new Callback<LoginResponse>() {
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
                    Toast.makeText(CadastroActivity.this,
                            "Conta criada com sucesso!", Toast.LENGTH_SHORT).show();
                    irParaHome(dados.getUsuario().getTipo());
                } else {
                    Toast.makeText(CadastroActivity.this,
                            "Erro ao cadastrar. Verifique os dados.",
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                mostrarLoading(false);
                Toast.makeText(CadastroActivity.this,
                        "Erro de conexão: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void irParaHome(String tipo) {
        Class<?> destino = "FREELANCER".equals(tipo)
                ? MainFreelancerActivity.class
                : MainClienteActivity.class;

        android.content.Intent intent = new android.content.Intent(this, destino);
        intent.setFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                | android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private String texto(android.widget.EditText edit) {
        return edit.getText() != null ? edit.getText().toString().trim() : "";
    }

    private void mostrarLoading(boolean ativo) {
        binding.progressBar.setVisibility(ativo ? View.VISIBLE : View.GONE);
        binding.botaoCadastrar.setEnabled(!ativo);
    }
}