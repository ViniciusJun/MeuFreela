package com.meufreela.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.meufreela.app.databinding.ActivityDetalheFreelancerBinding;
import com.meufreela.app.models.response.Freelancer;
import com.meufreela.app.network.ApiService;
import com.meufreela.app.network.RetrofitClient;
import com.meufreela.app.storage.TokenManager;
import com.meufreela.app.utils.MenuUtils;

import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetalheFreelancerActivity extends AppCompatActivity {

    public static final String EXTRA_ID = "freelancer_id";
    public static final String EXTRA_NOME = "freelancer_nome";
    public static final String EXTRA_PRECO = "freelancer_preco";

    private ActivityDetalheFreelancerBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDetalheFreelancerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        TokenManager tm = new TokenManager(this);
        ApiService api = RetrofitClient.getApiService(tm);

        String id = getIntent().getStringExtra(EXTRA_ID);
        String nome = getIntent().getStringExtra(EXTRA_NOME);
        double preco = getIntent().getDoubleExtra(EXTRA_PRECO, 0);

        binding.textNome.setText(nome);
        binding.textPreco.setText(String.format(Locale.getDefault(), "R$ %.0f/h", preco));

        api.detalheFreelancer(id).enqueue(new Callback<Freelancer>() {
            @Override
            public void onResponse(Call<Freelancer> call, Response<Freelancer> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Freelancer f = response.body();
                    binding.textCategoria.setText(rotuloCategoria(f.getCategoria()));
                    binding.textDescricao.setText(f.getDescricao());

                    double av = f.getAvaliacaoMedia() != null ? f.getAvaliacaoMedia() : 0;
                    int tot = f.getTotalServicos() != null ? f.getTotalServicos() : 0;
                    binding.textAvaliacao.setText(String.format(Locale.getDefault(),
                            "★ %.1f · %d serviços", av, tot));

                    binding.textVerificado.setVisibility(
                            f.isVerificado() ? View.VISIBLE : View.GONE);
                }
            }

            @Override
            public void onFailure(Call<Freelancer> call, Throwable t) {
                Toast.makeText(DetalheFreelancerActivity.this,
                        "Erro: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        binding.botaoContratar.setOnClickListener(v -> {
            Intent i = new Intent(this, NovaSolicitacaoActivity.class);
            i.putExtra(NovaSolicitacaoActivity.EXTRA_FREELANCER_ID, id);
            i.putExtra(NovaSolicitacaoActivity.EXTRA_FREELANCER_NOME, nome);
            i.putExtra(NovaSolicitacaoActivity.EXTRA_FREELANCER_PRECO, preco);
            startActivity(i);
        });

        binding.botaoMenu.setOnClickListener(v -> MenuUtils.mostrar(this, v));
    }

    private String rotuloCategoria(String c) {
        if (c == null) return "";
        switch (c) {
            case "REFORMAS_REPAROS": return "Reformas e Reparos";
            case "SERVICOS_DOMESTICOS": return "Serviços Domésticos";
            case "TECNOLOGIA": return "Tecnologia";
            case "EVENTOS": return "Eventos";
            case "BELEZA": return "Beleza";
            default: return c;
        }
    }
}