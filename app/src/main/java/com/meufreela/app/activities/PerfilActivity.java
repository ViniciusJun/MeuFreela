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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PerfilActivity extends AppCompatActivity {

    private ActivityPerfilBinding binding;
    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPerfilBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        tokenManager = new TokenManager(this);
        ApiService api = RetrofitClient.getApiService(tokenManager);

        binding.botaoSair.setOnClickListener(v -> confirmarLogout());

        api.meuPerfil().enqueue(new Callback<PerfilUsuario>() {
            @Override
            public void onResponse(Call<PerfilUsuario> call, Response<PerfilUsuario> response) {
                if (response.isSuccessful() && response.body() != null) {
                    PerfilUsuario u = response.body();
                    binding.editNome.setText(u.getNome());
                    binding.editEmail.setText(u.getEmail());

                    String tipo = "FREELANCER".equals(u.getTipo())
                            ? "Freelancer" : "Cliente";
                    binding.textTipo.setText("Tipo de conta: " + tipo);

                    binding.textVerificado.setVisibility(
                            u.isVerificado() ? View.VISIBLE : View.GONE);
                }
            }

            @Override
            public void onFailure(Call<PerfilUsuario> call, Throwable t) {
                Toast.makeText(PerfilActivity.this,
                        "Erro: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
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