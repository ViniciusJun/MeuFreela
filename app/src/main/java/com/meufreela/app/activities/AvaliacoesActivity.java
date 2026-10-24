package com.meufreela.app.activities;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.meufreela.app.adapters.AvaliacaoAdapter;
import com.meufreela.app.databinding.ActivityAvaliacoesBinding;
import com.meufreela.app.models.response.Avaliacao;
import com.meufreela.app.network.ApiService;
import com.meufreela.app.network.RetrofitClient;
import com.meufreela.app.storage.TokenManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AvaliacoesActivity extends AppCompatActivity {

    public static final String EXTRA_USUARIO_ID = "usuario_id";

    private ActivityAvaliacoesBinding binding;
    private AvaliacaoAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAvaliacoesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        TokenManager tm = new TokenManager(this);
        ApiService api = RetrofitClient.getApiService(tm);

        String id = getIntent().getStringExtra(EXTRA_USUARIO_ID);
        if (id == null) id = tm.getUserId(); // se não veio, mostra as próprias

        adapter = new AvaliacaoAdapter();
        binding.recyclerAvaliacoes.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerAvaliacoes.setAdapter(adapter);

        binding.progressBar.setVisibility(View.VISIBLE);
        api.avaliacoesDoUsuario(id).enqueue(new Callback<List<Avaliacao>>() {
            @Override
            public void onResponse(Call<List<Avaliacao>> call,
                                   Response<List<Avaliacao>> response) {
                binding.progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    List<Avaliacao> lista = response.body();
                    adapter.setItens(lista);
                    binding.textVazio.setVisibility(lista.isEmpty() ? View.VISIBLE : View.GONE);
                    binding.recyclerAvaliacoes.setVisibility(
                            lista.isEmpty() ? View.GONE : View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<List<Avaliacao>> call, Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
            }
        });
    }
}