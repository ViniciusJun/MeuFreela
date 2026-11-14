package com.meufreela.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.meufreela.app.adapters.ServicoAdapter;
import com.meufreela.app.databinding.ActivityMainClienteBinding;
import com.meufreela.app.models.response.Servico;
import com.meufreela.app.network.ApiService;
import com.meufreela.app.network.RetrofitClient;
import com.meufreela.app.storage.TokenManager;
import com.meufreela.app.utils.MenuUtils;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainClienteActivity extends AppCompatActivity {

    private static final String[][] CATEGORIAS = {
            {"Todos", ""},
            {"Reformas", "REFORMAS_REPAROS"},
            {"Domésticos", "SERVICOS_DOMESTICOS"},
            {"Tecnologia", "TECNOLOGIA"},
            {"Eventos", "EVENTOS"},
            {"Beleza", "BELEZA"}
    };

    private ActivityMainClienteBinding binding;
    private ApiService api;
    private ServicoAdapter adapter;
    private String categoriaAtual = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainClienteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        TokenManager tm = new TokenManager(this);
        api = RetrofitClient.getApiService(tm);

        adapter = new ServicoAdapter(new ServicoAdapter.Acoes() {
            @Override
            public void onServicoClick(Servico s) {
                Intent i = new Intent(MainClienteActivity.this, DetalheServicoActivity.class);
                i.putExtra(DetalheServicoActivity.EXTRA_ID, s.getId());
                startActivity(i);
            }
            @Override public void onPausar(Servico s) {}
            @Override public void onExcluir(Servico s) {}
        }, false);

        binding.recyclerServicos.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerServicos.setAdapter(adapter);

        binding.botaoMenu.setOnClickListener(v -> MenuUtils.mostrar(this, v));

        montarChipsCategorias();
        carregarServicos("");
    }

    private void montarChipsCategorias() {
        binding.containerCategorias.removeAllViews();

        for (String[] cat : CATEGORIAS) {
            TextView chip = new TextView(this);
            chip.setText(cat[0]);
            chip.setPadding(48, 24, 48, 24);
            chip.setTextSize(14);

            boolean selecionado = cat[1].equals(categoriaAtual);
            chip.setTextColor(selecionado ? 0xFFFFFFFF : 0xFF333333);
            chip.setBackgroundColor(selecionado ? 0xFF6200EE : 0xFFEEEEEE);

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(8, 0, 8, 0);
            chip.setLayoutParams(lp);

            chip.setOnClickListener(v -> {
                categoriaAtual = cat[1];
                montarChipsCategorias();
                carregarServicos(categoriaAtual);
            });

            binding.containerCategorias.addView(chip);
        }
    }

    private void carregarServicos(String categoria) {
        binding.progressBar.setVisibility(View.VISIBLE);
        String filtro = categoria.isEmpty() ? null : categoria;

        api.listarServicos(filtro).enqueue(new Callback<List<Servico>>() {
            @Override
            public void onResponse(Call<List<Servico>> call,
                                   Response<List<Servico>> response) {
                binding.progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    adapter.setItens(response.body());
                    if (response.body().isEmpty()) {
                        Toast.makeText(MainClienteActivity.this,
                                "Nenhum serviço nesta categoria ainda.",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Servico>> call, Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(MainClienteActivity.this,
                        "Erro: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}