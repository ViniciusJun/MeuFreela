package com.meufreela.app.activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.meufreela.app.databinding.ActivityNovaSolicitacaoBinding;
import com.meufreela.app.models.request.CriarSolicitacaoRequest;
import com.meufreela.app.models.response.Solicitacao;
import com.meufreela.app.network.ApiService;
import com.meufreela.app.network.RetrofitClient;
import com.meufreela.app.storage.TokenManager;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NovaSolicitacaoActivity extends AppCompatActivity {

    public static final String EXTRA_FREELANCER_ID = "fid";
    public static final String EXTRA_FREELANCER_NOME = "fnome";
    public static final String EXTRA_FREELANCER_PRECO = "fpreco";

    private ActivityNovaSolicitacaoBinding binding;
    private ApiService api;
    private double precoHora;
    private Calendar dataSelecionada;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNovaSolicitacaoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        TokenManager tm = new TokenManager(this);
        api = RetrofitClient.getApiService(tm);

        String id = getIntent().getStringExtra(EXTRA_FREELANCER_ID);
        String nome = getIntent().getStringExtra(EXTRA_FREELANCER_NOME);
        precoHora = getIntent().getDoubleExtra(EXTRA_FREELANCER_PRECO, 0);

        binding.textFreelancer.setText("Contratar: " + nome);
        atualizarValor();

        binding.editDuracao.setOnFocusChangeListener((v, has) -> atualizarValor());
        binding.editData.setOnClickListener(v -> abrirSeletorDataHora());
        binding.botaoEnviar.setOnClickListener(v -> enviar(id));
    }

    private void abrirSeletorDataHora() {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(this, (dp, ano, mes, dia) -> {
            Calendar temp = Calendar.getInstance();
            temp.set(ano, mes, dia);
            new TimePickerDialog(this, (tp, hora, min) -> {
                temp.set(Calendar.HOUR_OF_DAY, hora);
                temp.set(Calendar.MINUTE, min);
                dataSelecionada = temp;
                SimpleDateFormat fmt = new SimpleDateFormat(
                        "dd/MM/yyyy HH:mm", Locale.getDefault());
                binding.editData.setText(fmt.format(temp.getTime()));
            }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show();
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void atualizarValor() {
        try {
            int horas = Integer.parseInt(binding.editDuracao.getText().toString());
            binding.textValorEstimado.setText(String.format(Locale.getDefault(),
                    "Valor estimado: R$ %.2f", precoHora * horas));
        } catch (Exception e) {
            binding.textValorEstimado.setText("Valor estimado: R$ 0,00");
        }
    }

    private void enviar(String freelancerId) {
        String descricao = texto(binding.editDescricao);
        String endereco = texto(binding.editEndereco);
        String horasStr = texto(binding.editDuracao);

        if (descricao.length() < 10) {
            binding.editDescricao.setError("Descreva com pelo menos 10 caracteres");
            return;
        }
        if (endereco.length() < 5) {
            binding.editEndereco.setError("Informe o endereço");
            return;
        }
        if (dataSelecionada == null) {
            binding.editData.setError("Escolha data e hora");
            return;
        }
        int horas;
        try {
            horas = Integer.parseInt(horasStr);
            if (horas < 1 || horas > 24) throw new NumberFormatException();
        } catch (Exception e) {
            binding.editDuracao.setError("Entre 1 e 24 horas");
            return;
        }

        // Formata para ISO-8601, sem timezone (compatível com LocalDateTime)
        SimpleDateFormat iso = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
        String dataIso = iso.format(dataSelecionada.getTime());

        mostrarLoading(true);

        CriarSolicitacaoRequest req = new CriarSolicitacaoRequest(
                freelancerId, descricao, endereco, dataIso, horas);

        api.criarSolicitacao(req).enqueue(new Callback<Solicitacao>() {
            @Override
            public void onResponse(Call<Solicitacao> call, Response<Solicitacao> response) {
                mostrarLoading(false);
                if (response.isSuccessful()) {
                    Toast.makeText(NovaSolicitacaoActivity.this,
                            "Solicitação enviada! Aguarde a resposta.",
                            Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    try {
                        JSONObject obj = new JSONObject(response.errorBody().string());
                        Toast.makeText(NovaSolicitacaoActivity.this,
                                obj.getString("mensagem"), Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(NovaSolicitacaoActivity.this,
                                "Erro ao enviar", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<Solicitacao> call, Throwable t) {
                mostrarLoading(false);
                Toast.makeText(NovaSolicitacaoActivity.this,
                        "Erro de conexão: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private String texto(android.widget.EditText e) {
        return e.getText() != null ? e.getText().toString().trim() : "";
    }

    private void mostrarLoading(boolean ativo) {
        binding.progressBar.setVisibility(ativo ? View.VISIBLE : View.GONE);
        binding.botaoEnviar.setEnabled(!ativo);
    }
}