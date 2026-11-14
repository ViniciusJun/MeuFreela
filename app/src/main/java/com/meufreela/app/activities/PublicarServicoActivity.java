package com.meufreela.app.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.meufreela.app.databinding.ActivityPublicarServicoBinding;
import com.meufreela.app.models.request.CriarServicoRequest;
import com.meufreela.app.models.response.Servico;
import com.meufreela.app.network.ApiService;
import com.meufreela.app.network.RetrofitClient;
import com.meufreela.app.storage.TokenManager;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PublicarServicoActivity extends AppCompatActivity {

    public static final String EXTRA_ID = "servico_id"; // para edição

    private static final String[] CATEGORIAS = {
            "REFORMAS_REPAROS", "SERVICOS_DOMESTICOS",
            "TECNOLOGIA", "EVENTOS", "BELEZA"
    };
    private static final String[] ROTULOS = {
            "Reformas e Reparos", "Serviços Domésticos",
            "Tecnologia", "Eventos", "Beleza"
    };

    private ActivityPublicarServicoBinding binding;
    private ApiService api;
    private String servicoId; // null = criar, não-null = editar

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPublicarServicoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        TokenManager tm = new TokenManager(this);
        api = RetrofitClient.getApiService(tm);

        servicoId = getIntent().getStringExtra(EXTRA_ID);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, ROTULOS);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerCategoria.setAdapter(adapter);

        if (servicoId != null) {
            binding.textTitulo.setText("Editar serviço");
            binding.botaoSalvar.setText("Salvar alterações");
            carregarServico();
        }

        binding.botaoSalvar.setOnClickListener(v -> salvar());
    }

    private void carregarServico() {
        api.detalheServico(servicoId).enqueue(new Callback<Servico>() {
            @Override
            public void onResponse(Call<Servico> call, Response<Servico> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Servico s = response.body();
                    binding.editTitulo.setText(s.getTitulo());
                    binding.editDescricao.setText(s.getDescricao());
                    binding.editPreco.setText(String.valueOf(s.getPreco()));
                    for (int i = 0; i < CATEGORIAS.length; i++) {
                        if (CATEGORIAS[i].equals(s.getCategoria())) {
                            binding.spinnerCategoria.setSelection(i);
                            break;
                        }
                    }
                }
            }
            @Override public void onFailure(Call<Servico> call, Throwable t) {}
        });
    }

    private void salvar() {
        String titulo = binding.editTitulo.getText().toString().trim();
        String descricao = binding.editDescricao.getText().toString().trim();
        String precoStr = binding.editPreco.getText().toString().trim();

        if (titulo.length() < 5) {
            binding.editTitulo.setError("Mínimo 5 caracteres");
            return;
        }
        if (descricao.length() < 10) {
            binding.editDescricao.setError("Mínimo 10 caracteres");
            return;
        }

        double preco;
        try {
            preco = Double.parseDouble(precoStr.replace(",", "."));
            if (preco < 10 || preco > 2000) throw new NumberFormatException();
        } catch (Exception e) {
            binding.editPreco.setError("Entre R$ 10 e R$ 2000");
            return;
        }

        String categoria = CATEGORIAS[binding.spinnerCategoria.getSelectedItemPosition()];
        CriarServicoRequest req = new CriarServicoRequest(titulo, descricao, categoria, preco);

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.botaoSalvar.setEnabled(false);

        Call<Servico> call = servicoId != null
                ? api.atualizarServico(servicoId, req)
                : api.criarServico(req);

        call.enqueue(new Callback<Servico>() {
            @Override
            public void onResponse(Call<Servico> call, Response<Servico> response) {
                binding.progressBar.setVisibility(View.GONE);
                binding.botaoSalvar.setEnabled(true);
                if (response.isSuccessful()) {
                    Toast.makeText(PublicarServicoActivity.this,
                            servicoId != null ? "Serviço atualizado!" : "Serviço publicado!",
                            Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    try {
                        JSONObject obj = new JSONObject(response.errorBody().string());
                        Toast.makeText(PublicarServicoActivity.this,
                                obj.getString("mensagem"), Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(PublicarServicoActivity.this,
                                "Erro ao salvar", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<Servico> call, Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                binding.botaoSalvar.setEnabled(true);
                Toast.makeText(PublicarServicoActivity.this,
                        "Erro de conexão: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}