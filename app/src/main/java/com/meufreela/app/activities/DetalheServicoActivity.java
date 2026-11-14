package com.meufreela.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.meufreela.app.databinding.ActivityDetalheServicoBinding;
import com.meufreela.app.models.response.Servico;
import com.meufreela.app.network.ApiService;
import com.meufreela.app.network.RetrofitClient;
import com.meufreela.app.storage.TokenManager;

import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetalheServicoActivity extends AppCompatActivity {

    public static final String EXTRA_ID = "servico_id";

    private ActivityDetalheServicoBinding binding;
    private Servico servicoAtual;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDetalheServicoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        TokenManager tm = new TokenManager(this);
        ApiService api = RetrofitClient.getApiService(tm);

        String id = getIntent().getStringExtra(EXTRA_ID);
        if (id == null) { finish(); return; }

        api.detalheServico(id).enqueue(new Callback<Servico>() {
            @Override
            public void onResponse(Call<Servico> call, Response<Servico> response) {
                if (response.isSuccessful() && response.body() != null) {
                    servicoAtual = response.body();
                    preencher(servicoAtual);
                } else {
                    Toast.makeText(DetalheServicoActivity.this,
                            "Erro ao carregar serviço", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Servico> call, Throwable t) {
                Toast.makeText(DetalheServicoActivity.this,
                        "Erro: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

        binding.botaoContratar.setOnClickListener(v -> {
            if (servicoAtual == null) {
                Toast.makeText(this, "Aguarde o serviço carregar...",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            //Todos os extras, incluindo SERVICO_ID
            Intent i = new Intent(this, NovaSolicitacaoActivity.class);
            i.putExtra(NovaSolicitacaoActivity.EXTRA_FREELANCER_ID,
                    servicoAtual.getFreelancerId());
            i.putExtra(NovaSolicitacaoActivity.EXTRA_FREELANCER_NOME,
                    servicoAtual.getFreelancerNome());
            i.putExtra(NovaSolicitacaoActivity.EXTRA_FREELANCER_PRECO,
                    servicoAtual.getPreco() != null ? servicoAtual.getPreco() : 0.0);
            i.putExtra(NovaSolicitacaoActivity.EXTRA_SERVICO_ID,
                    servicoAtual.getId());              // ← ESSENCIAL
            i.putExtra(NovaSolicitacaoActivity.EXTRA_SERVICO_TITULO,
                    servicoAtual.getTitulo());
            startActivity(i);
        });
    }

    private void preencher(Servico s) {
        binding.textTitulo.setText(s.getTitulo());
        binding.textFreelancer.setText("por " + s.getFreelancerNome()
                + (s.isFreelancerVerificado() ? " ✓" : ""));
        binding.textDescricao.setText(s.getDescricao());
        binding.textCategoria.setText(rotuloCategoria(s.getCategoria()));
        double preco = s.getPreco() != null ? s.getPreco() : 0;
        binding.textPreco.setText(String.format(Locale.getDefault(), "R$ %.0f/h", preco));
    }

    private String rotuloCategoria(String c) {
        if (c == null) return "";
        switch (c) {
            case "REFORMAS_REPAROS": return "Reformas";
            case "SERVICOS_DOMESTICOS": return "Domésticos";
            case "TECNOLOGIA": return "Tecnologia";
            case "EVENTOS": return "Eventos";
            case "BELEZA": return "Beleza";
            default: return c;
        }
    }
}