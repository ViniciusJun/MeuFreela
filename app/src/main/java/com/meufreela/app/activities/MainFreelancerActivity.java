package com.meufreela.app.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.meufreela.app.adapters.SolicitacaoAdapter;
import com.meufreela.app.databinding.ActivityMainFreelancerBinding;
import com.meufreela.app.models.response.Solicitacao;
import com.meufreela.app.network.ApiService;
import com.meufreela.app.network.RetrofitClient;
import com.meufreela.app.storage.TokenManager;
import com.meufreela.app.utils.MenuUtils;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainFreelancerActivity extends AppCompatActivity
        implements SolicitacaoAdapter.Acoes {

    private ActivityMainFreelancerBinding binding;
    private ApiService api;
    private SolicitacaoAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainFreelancerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        TokenManager tm = new TokenManager(this);
        api = RetrofitClient.getApiService(tm);

        adapter = new SolicitacaoAdapter(this, true);
        binding.recyclerSolicitacoes.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerSolicitacoes.setAdapter(adapter);

        //Botão de menu
        binding.botaoMenu.setOnClickListener(v -> MenuUtils.mostrar(this, v, true));
    }

    @Override
    protected void onResume() {
        super.onResume();
        carregar();
    }

    private void carregar() {
        binding.progressBar.setVisibility(View.VISIBLE);
        api.solicitacoesComoFreelancer().enqueue(new Callback<List<Solicitacao>>() {
            @Override
            public void onResponse(Call<List<Solicitacao>> call,
                                   Response<List<Solicitacao>> response) {
                binding.progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    List<Solicitacao> lista = response.body();
                    adapter.setItens(lista);
                    atualizarContadores(lista);
                    binding.textVazio.setVisibility(lista.isEmpty() ? View.VISIBLE : View.GONE);
                    binding.recyclerSolicitacoes.setVisibility(lista.isEmpty() ? View.GONE : View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<List<Solicitacao>> call, Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(MainFreelancerActivity.this,
                        "Erro: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void atualizarContadores(List<Solicitacao> lista) {
        long pendentes = lista.stream()
                .filter(s -> "PENDENTE".equals(s.getStatus())).count();
        long aceitas = lista.stream()
                .filter(s -> "ACEITA".equals(s.getStatus())).count();
        binding.textResumo.setText(pendentes + " pendente(s) · " + aceitas + " aceita(s)");
    }

    @Override
    public void aceitar(Solicitacao s) {
        atualizarStatus(s.getId(), "ACEITA");
    }

    @Override
    public void recusar(Solicitacao s) {
        atualizarStatus(s.getId(), "RECUSADA");
    }

    private void atualizarStatus(String id, String status) {
        binding.progressBar.setVisibility(View.VISIBLE);
        api.atualizarStatus(id, status).enqueue(new Callback<Solicitacao>() {
            @Override
            public void onResponse(Call<Solicitacao> call, Response<Solicitacao> response) {
                binding.progressBar.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    Toast.makeText(MainFreelancerActivity.this,
                            "Solicitação " + status.toLowerCase() + "!",
                            Toast.LENGTH_SHORT).show();
                    carregar();
                } else {
                    Toast.makeText(MainFreelancerActivity.this,
                            "Erro ao atualizar", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Solicitacao> call, Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(MainFreelancerActivity.this,
                        "Erro: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}