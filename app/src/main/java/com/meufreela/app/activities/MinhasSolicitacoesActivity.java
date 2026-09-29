package com.meufreela.app.activities;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.meufreela.app.adapters.SolicitacaoAdapter;
import com.meufreela.app.databinding.ActivityMinhasSolicitacoesBinding;
import com.meufreela.app.models.response.Solicitacao;
import com.meufreela.app.network.ApiService;
import com.meufreela.app.network.RetrofitClient;
import com.meufreela.app.storage.TokenManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MinhasSolicitacoesActivity extends AppCompatActivity {

    private ActivityMinhasSolicitacoesBinding binding;
    private ApiService api;
    private SolicitacaoAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMinhasSolicitacoesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        TokenManager tm = new TokenManager(this);
        api = RetrofitClient.getApiService(tm);

        // Cliente não tem botões de aceitar/recusar
        adapter = new SolicitacaoAdapter(new SolicitacaoAdapter.Acoes() {
            @Override public void aceitar(Solicitacao s) {}
            @Override public void recusar(Solicitacao s) {}
        }, false);

        binding.recycler.setLayoutManager(new LinearLayoutManager(this));
        binding.recycler.setAdapter(adapter);

        carregar();
    }

    private void carregar() {
        binding.progressBar.setVisibility(View.VISIBLE);
        api.solicitacoesComoCliente().enqueue(new Callback<List<Solicitacao>>() {
            @Override
            public void onResponse(Call<List<Solicitacao>> call,
                                   Response<List<Solicitacao>> response) {
                binding.progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    List<Solicitacao> lista = response.body();
                    adapter.setItens(lista);
                    binding.textVazio.setVisibility(lista.isEmpty() ? View.VISIBLE : View.GONE);
                    binding.recycler.setVisibility(lista.isEmpty() ? View.GONE : View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<List<Solicitacao>> call, Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
            }
        });
    }
}