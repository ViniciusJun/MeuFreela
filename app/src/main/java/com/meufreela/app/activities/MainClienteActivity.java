package com.meufreela.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.meufreela.app.adapters.FreelancerAdapter;
import com.meufreela.app.databinding.ActivityMainClienteBinding;
import com.meufreela.app.models.response.Freelancer;
import com.meufreela.app.network.ApiService;
import com.meufreela.app.network.RetrofitClient;
import com.meufreela.app.storage.TokenManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainClienteActivity extends AppCompatActivity {

    // Rótulos amigáveis e valor enviado para o backend
    private static final String[][] CATEGORIAS = {
            {"Todos", ""},
            {"Reformas", "REFORMAS_REPAROS"},
            {"Domésticos", "SERVICOS_DOMESTICOS"},
            {"Tecnologia", "TECNOLOGIA"},
            {"Eventos", "EVENTOS"},
            {"Beleza", "BELEZA"}
    };

    private ActivityMainClienteBinding binding;
    private ApiService apiService;
    private TokenManager tokenManager;
    private FreelancerAdapter adapter;
    private String categoriaAtual = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainClienteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        tokenManager = new TokenManager(this);
        apiService = RetrofitClient.getApiService(tokenManager);

        adapter = new FreelancerAdapter(this::abrirDetalhes);
        binding.recyclerFreelancers.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerFreelancers.setAdapter(adapter);

        montarChipsCategorias();
        carregarFreelancers("");
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
                carregarFreelancers(categoriaAtual);
            });

            binding.containerCategorias.addView(chip);
        }
    }

    private void carregarFreelancers(String categoria) {
        binding.progressBar.setVisibility(View.VISIBLE);

        String filtro = categoria.isEmpty() ? null : categoria;
        apiService.listarFreelancers(filtro).enqueue(new Callback<List<Freelancer>>() {
            @Override
            public void onResponse(Call<List<Freelancer>> call,
                                   Response<List<Freelancer>> response) {
                binding.progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    adapter.setItens(response.body());
                    if (response.body().isEmpty()) {
                        Toast.makeText(MainClienteActivity.this,
                                "Nenhum profissional nesta categoria ainda.",
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MainClienteActivity.this,
                            "Erro ao carregar profissionais", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Freelancer>> call, Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(MainClienteActivity.this,
                        "Erro de conexão: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void abrirDetalhes(Freelancer f) {
        Intent i = new Intent(this, DetalheFreelancerActivity.class);
        i.putExtra(DetalheFreelancerActivity.EXTRA_ID, f.getId());
        i.putExtra(DetalheFreelancerActivity.EXTRA_NOME, f.getNome());
        i.putExtra(DetalheFreelancerActivity.EXTRA_PRECO,
                f.getPrecoHora() != null ? f.getPrecoHora() : 0.0);
        startActivity(i);
    }
}