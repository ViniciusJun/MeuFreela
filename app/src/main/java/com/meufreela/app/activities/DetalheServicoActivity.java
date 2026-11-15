package com.meufreela.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.meufreela.app.config.ApiConfig;
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

            Intent i = new Intent(this, NovaSolicitacaoActivity.class);

            // Dados do freelancer (para exibição e como fallback)
            i.putExtra(NovaSolicitacaoActivity.EXTRA_FREELANCER_ID,
                    servicoAtual.getFreelancerId());
            i.putExtra(NovaSolicitacaoActivity.EXTRA_FREELANCER_NOME,
                    servicoAtual.getFreelancerNome());

            // Dados do serviço (fonte de verdade do preço)
            i.putExtra(NovaSolicitacaoActivity.EXTRA_SERVICO_ID,
                    servicoAtual.getId());
            i.putExtra(NovaSolicitacaoActivity.EXTRA_SERVICO_TITULO,
                    servicoAtual.getTitulo());
            // CORREÇÃO: envia o preço do serviço no campo CORRETO.
            // Antes ia no EXTRA_FREELANCER_PRECO, o que era semanticamente
            // errado (nome diz "freelancer", valor era do "serviço").
            i.putExtra(NovaSolicitacaoActivity.EXTRA_SERVICO_PRECO,
                    servicoAtual.getPreco() != null ? servicoAtual.getPreco() : 0.0);

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

        String fotoPath = s.getFreelancerFotoUrl();
        if (fotoPath != null && !fotoPath.isEmpty()) {
            String urlCompleta = com.meufreela.app.config.ApiConfig.SERVER_URL_DEV + fotoPath;
            com.bumptech.glide.Glide.with(this)
                    .load(urlCompleta)
                    .placeholder(android.R.drawable.ic_menu_myplaces)
                    .circleCrop()
                    .into(binding.imageFreelancer);
        }

        if (s.getFotos() != null && !s.getFotos().isEmpty()) {
            binding.containerFotos.setVisibility(View.VISIBLE);
            binding.linearFotos.removeAllViews();

            for (String url : s.getFotos()) {
                android.widget.ImageView iv = new android.widget.ImageView(this);
                android.widget.LinearLayout.LayoutParams lp =
                        new android.widget.LinearLayout.LayoutParams(dp(180), dp(180));
                lp.setMargins(0, 0, dp(8), 0);
                iv.setLayoutParams(lp);
                iv.setScaleType(android.widget.ImageView.ScaleType.CENTER_CROP);
                iv.setBackgroundColor(0xFFE0E0E0);

                com.bumptech.glide.Glide.with(this)
                        .load(ApiConfig.SERVER_URL_DEV + url)
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .into(iv);

                binding.linearFotos.addView(iv);
            }
        }
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

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }
}