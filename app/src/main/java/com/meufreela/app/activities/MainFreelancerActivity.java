package com.meufreela.app.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.meufreela.app.adapters.SolicitacaoAdapter;
import com.meufreela.app.databinding.ActivityMainFreelancerBinding;
import com.meufreela.app.models.response.Solicitacao;
import com.meufreela.app.network.ApiService;
import com.meufreela.app.network.RetrofitClient;
import com.meufreela.app.storage.TokenManager;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainFreelancerActivity extends AppCompatActivity {

    private static final String[][] FILTROS = {
            {"Todas", ""},
            {"Pendentes", "PENDENTE"},
            {"Aceitas", "ACEITA"},
            {"Em andamento", "EM_ANDAMENTO"},
            {"Concluídas", "CONCLUIDA"}
    };

    private ActivityMainFreelancerBinding binding;
    private ApiService api;
    private SolicitacaoAdapter adapter;
    private List<Solicitacao> todas = new ArrayList<>();
    private String filtroAtual = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainFreelancerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        TokenManager tm = new TokenManager(this);
        api = RetrofitClient.getApiService(tm);

        adapter = new SolicitacaoAdapter(this::executarAcao);
        binding.recyclerSolicitacoes.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerSolicitacoes.setAdapter(adapter);

        montarChipsFiltros();
        carregarSolicitacoes();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recarrega sempre que volta à tela (ex: após aceitar uma solicitação)
        if (adapter != null) carregarSolicitacoes();
    }

    private void montarChipsFiltros() {
        binding.containerFiltros.removeAllViews();
        for (String[] f : FILTROS) {
            TextView chip = new TextView(this);
            chip.setText(f[0]);
            chip.setPadding(48, 24, 48, 24);
            chip.setTextSize(14);

            boolean selecionado = f[1].equals(filtroAtual);
            chip.setTextColor(selecionado ? 0xFFFFFFFF : 0xFF333333);
            chip.setBackgroundColor(selecionado ? 0xFF6200EE : 0xFFEEEEEE);

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(8, 0, 8, 0);
            chip.setLayoutParams(lp);

            chip.setOnClickListener(v -> {
                filtroAtual = f[1];
                montarChipsFiltros();
                aplicarFiltro();
            });

            binding.containerFiltros.addView(chip);
        }
    }

    private void carregarSolicitacoes() {
        binding.progressBar.setVisibility(View.VISIBLE);

        api.solicitacoesComoFreelancer().enqueue(new Callback<List<Solicitacao>>() {
            @Override
            public void onResponse(Call<List<Solicitacao>> call,
                                   Response<List<Solicitacao>> response) {
                binding.progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    todas = response.body();
                    atualizarResumo();
                    aplicarFiltro();
                } else {
                    Toast.makeText(MainFreelancerActivity.this,
                            "Erro ao carregar solicitações", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Solicitacao>> call, Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(MainFreelancerActivity.this,
                        "Erro de conexão: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void aplicarFiltro() {
        List<Solicitacao> filtradas = new ArrayList<>();
        for (Solicitacao s : todas) {
            if (filtroAtual.isEmpty() || filtroAtual.equals(s.getStatus())) {
                filtradas.add(s);
            }
        }
        adapter.setItens(filtradas);

        boolean vazio = filtradas.isEmpty();
        binding.textVazio.setVisibility(vazio ? View.VISIBLE : View.GONE);
        binding.recyclerSolicitacoes.setVisibility(vazio ? View.GONE : View.VISIBLE);

        if (vazio) {
            binding.textVazio.setText(filtroAtual.isEmpty()
                    ? "Nenhuma solicitação por aqui ainda"
                    : "Nenhuma solicitação com este status");
        }
    }

    private void atualizarResumo() {
        long pendentes = todas.stream()
                .filter(s -> "PENDENTE".equals(s.getStatus())).count();
        long andamento = todas.stream()
                .filter(s -> "EM_ANDAMENTO".equals(s.getStatus())).count();

        if (todas.isEmpty()) {
            binding.textResumo.setText("Aguardando sua primeira solicitação");
        } else {
            binding.textResumo.setText(
                    pendentes + " pendente(s) · " + andamento + " em andamento");
        }
    }

    private void executarAcao(Solicitacao s, String novoStatus) {
        binding.progressBar.setVisibility(View.VISIBLE);

        api.atualizarStatus(s.getId(), novoStatus)
                .enqueue(new Callback<Solicitacao>() {
                    @Override
                    public void onResponse(Call<Solicitacao> call,
                                           Response<Solicitacao> response) {
                        binding.progressBar.setVisibility(View.GONE);
                        if (response.isSuccessful()) {
                            Toast.makeText(MainFreelancerActivity.this,
                                    "Status atualizado para " + rotulo(novoStatus),
                                    Toast.LENGTH_SHORT).show();
                            carregarSolicitacoes();
                        } else {
                            try {
                                JSONObject obj = new JSONObject(
                                        response.errorBody().string());
                                Toast.makeText(MainFreelancerActivity.this,
                                        obj.getString("mensagem"),
                                        Toast.LENGTH_LONG).show();
                            } catch (Exception e) {
                                Toast.makeText(MainFreelancerActivity.this,
                                        "Erro ao atualizar", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<Solicitacao> call, Throwable t) {
                        binding.progressBar.setVisibility(View.GONE);
                        Toast.makeText(MainFreelancerActivity.this,
                                "Erro de conexão: " + t.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private String rotulo(String status) {
        switch (status) {
            case "ACEITA": return "aceita";
            case "RECUSADA": return "recusada";
            case "EM_ANDAMENTO": return "em andamento";
            case "CONCLUIDA": return "concluída";
            default: return status;
        }
    }
}