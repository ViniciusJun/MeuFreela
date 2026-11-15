package com.meufreela.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.meufreela.app.adapters.ServicoAdapter;
import com.meufreela.app.databinding.ActivityMeusServicosBinding;
import com.meufreela.app.models.response.Servico;
import com.meufreela.app.network.ApiService;
import com.meufreela.app.network.RetrofitClient;
import com.meufreela.app.storage.TokenManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MeusServicosActivity extends AppCompatActivity
        implements ServicoAdapter.Acoes {

    private ActivityMeusServicosBinding binding;
    private ApiService api;
    private ServicoAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMeusServicosBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        TokenManager tm = new TokenManager(this);
        api = RetrofitClient.getApiService(tm);

        adapter = new ServicoAdapter(this, true);
        binding.recycler.setLayoutManager(new LinearLayoutManager(this));
        binding.recycler.setAdapter(adapter);

        binding.botaoNovo.setOnClickListener(v ->
                startActivity(new Intent(this, PublicarServicoActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        carregar();
    }

    private void carregar() {
        binding.progressBar.setVisibility(View.VISIBLE);
        api.meusServicos().enqueue(new Callback<List<Servico>>() {
            @Override
            public void onResponse(Call<List<Servico>> call,
                                   Response<List<Servico>> response) {
                binding.progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    List<Servico> lista = response.body();
                    adapter.setItens(lista);
                    binding.textVazio.setVisibility(lista.isEmpty() ? View.VISIBLE : View.GONE);
                    binding.recycler.setVisibility(lista.isEmpty() ? View.GONE : View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<List<Servico>> call, Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(MeusServicosActivity.this,
                        "Erro: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onServicoClick(Servico s) {
        Intent i = new Intent(this, DetalheServicoActivity.class);
        i.putExtra(DetalheServicoActivity.EXTRA_ID, s.getId());
        startActivity(i);
    }

    @Override
    public void onPausar(Servico s) {
        api.alternarServicoAtivo(s.getId()).enqueue(new Callback<Servico>() {
            @Override
            public void onResponse(Call<Servico> call, Response<Servico> response) {
                if (response.isSuccessful()) carregar();
            }
            @Override
            public void onFailure(Call<Servico> call, Throwable t) {
                Toast.makeText(MeusServicosActivity.this,
                        "Erro: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onExcluir(Servico s) {
        new AlertDialog.Builder(this)
                .setTitle("Excluir serviço")
                .setMessage("Deseja excluir \"" + s.getTitulo() + "\"?")
                .setPositiveButton("Excluir", (d, w) -> {
                    api.excluirServico(s.getId()).enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            if (response.isSuccessful()) carregar();
                        }
                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            Toast.makeText(MeusServicosActivity.this,
                                    "Erro: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    @Override
    public void onGerenciarFotos(Servico s) {
        Intent i = new Intent(this, GerenciarFotosActivity.class);
        i.putExtra(GerenciarFotosActivity.EXTRA_SERVICO_ID, s.getId());
        i.putExtra(GerenciarFotosActivity.EXTRA_SERVICO_TITULO, s.getTitulo());
        startActivity(i);
    }
}