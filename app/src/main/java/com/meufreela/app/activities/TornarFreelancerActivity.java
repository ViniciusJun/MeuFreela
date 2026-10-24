package com.meufreela.app.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.meufreela.app.databinding.ActivityTornarFreelancerBinding;
import com.meufreela.app.models.request.TornarFreelancerRequest;
import com.meufreela.app.network.ApiService;
import com.meufreela.app.network.RetrofitClient;
import com.meufreela.app.storage.TokenManager;

import org.json.JSONObject;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TornarFreelancerActivity extends AppCompatActivity {

    private static final String[] CATEGORIAS = {
            "REFORMAS_REPAROS", "SERVICOS_DOMESTICOS",
            "TECNOLOGIA", "EVENTOS", "BELEZA"
    };
    private static final String[] ROTULOS = {
            "Reformas e Reparos", "Serviços Domésticos",
            "Tecnologia", "Eventos", "Beleza"
    };

    private ActivityTornarFreelancerBinding binding;
    private ApiService api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTornarFreelancerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        TokenManager tm = new TokenManager(this);
        api = RetrofitClient.getApiService(tm);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, ROTULOS);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerCategoria.setAdapter(adapter);

        binding.botaoAtivar.setOnClickListener(v -> ativar());
    }

    private void ativar() {
        String descricao = binding.editDescricao.getText() != null
                ? binding.editDescricao.getText().toString().trim() : "";
        String precoStr = binding.editPreco.getText() != null
                ? binding.editPreco.getText().toString().trim() : "";

        if (descricao.length() < 10) {
            binding.editDescricao.setError("Mínimo 10 caracteres");
            return;
        }

        double preco;
        try {
            preco = Double.parseDouble(precoStr.replace(",", "."));
            if (preco < 10 || preco > 1000) throw new NumberFormatException();
        } catch (Exception e) {
            binding.editPreco.setError("Entre R$ 10 e R$ 1000");
            return;
        }

        String categoria = CATEGORIAS[binding.spinnerCategoria.getSelectedItemPosition()];

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.botaoAtivar.setEnabled(false);

        api.tornarFreelancer(new TornarFreelancerRequest(categoria, descricao, preco))
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call,
                                           Response<ResponseBody> response) {
                        binding.progressBar.setVisibility(View.GONE);
                        binding.botaoAtivar.setEnabled(true);
                        if (response.isSuccessful()) {
                            Toast.makeText(TornarFreelancerActivity.this,
                                    "Você agora é freelancer! 🎉",
                                    Toast.LENGTH_LONG).show();
                            finish();
                        } else {
                            try {
                                JSONObject obj = new JSONObject(
                                        response.errorBody().string());
                                Toast.makeText(TornarFreelancerActivity.this,
                                        obj.getString("mensagem"),
                                        Toast.LENGTH_LONG).show();
                            } catch (Exception e) {
                                Toast.makeText(TornarFreelancerActivity.this,
                                        "Erro ao ativar", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        binding.progressBar.setVisibility(View.GONE);
                        binding.botaoAtivar.setEnabled(true);
                        Toast.makeText(TornarFreelancerActivity.this,
                                "Erro de conexão", Toast.LENGTH_LONG).show();
                    }
                });
    }
}