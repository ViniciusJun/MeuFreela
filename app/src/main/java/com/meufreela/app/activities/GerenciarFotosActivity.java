package com.meufreela.app.activities;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import com.meufreela.app.adapters.FotoServicoAdapter;
import com.meufreela.app.databinding.ActivityGerenciarFotosBinding;
import com.meufreela.app.models.response.Servico;
import com.meufreela.app.network.ApiService;
import com.meufreela.app.network.RetrofitClient;
import com.meufreela.app.storage.TokenManager;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GerenciarFotosActivity extends AppCompatActivity {

    public static final String EXTRA_SERVICO_ID = "servico_id";
    public static final String EXTRA_SERVICO_TITULO = "servico_titulo";
    private static final int MAX_FOTOS = 5;

    private ActivityGerenciarFotosBinding binding;
    private ApiService api;
    private FotoServicoAdapter adapter;
    private String servicoId;
    private ActivityResultLauncher<String> pickImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGerenciarFotosBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        TokenManager tm = new TokenManager(this);
        api = RetrofitClient.getApiService(tm);

        servicoId = getIntent().getStringExtra(EXTRA_SERVICO_ID);
        String titulo = getIntent().getStringExtra(EXTRA_SERVICO_TITULO);
        binding.textTituloServico.setText(titulo != null ? titulo : "Fotos do serviço");

        adapter = new FotoServicoAdapter(this::removerFoto);
        binding.recyclerFotos.setLayoutManager(new GridLayoutManager(this, 2));
        binding.recyclerFotos.setAdapter(adapter);

        pickImage = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> { if (uri != null) uploadFoto(uri); });

        // CORREÇÃO 4: Listener configurado apenas UMA VEZ no onCreate
        binding.botaoAdicionar.setOnClickListener(v -> {
            if (adapter.getItemCount() >= MAX_FOTOS) {
                Toast.makeText(this, "Limite de 5 fotos atingido", Toast.LENGTH_SHORT).show();
                return;
            }
            pickImage.launch("image/*");
        });

        carregar();
    }

    private void carregar() {
        binding.progressBar.setVisibility(View.VISIBLE);
        api.detalheServico(servicoId).enqueue(new Callback<Servico>() {
            @Override
            public void onResponse(Call<Servico> call, Response<Servico> response) {
                binding.progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    Servico s = response.body();
                    adapter.setItens(s.getFotos());

                    int qtd = s.getFotos().size();
                    binding.textContador.setText(qtd + " de " + MAX_FOTOS + " fotos");

                    // CORREÇÃO 4: Desabilita o botão se o limite foi atingido
                    // e remove aquele trecho que abria a galeria sozinho (pickImage.launch)
                    if (qtd >= MAX_FOTOS) {
                        binding.botaoAdicionar.setEnabled(false);
                        binding.textContador.setText("Limite atingido (5 de 5 fotos)");
                    } else {
                        binding.botaoAdicionar.setEnabled(true);
                    }
                }
            }

            @Override
            public void onFailure(Call<Servico> call, Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void uploadFoto(Uri uri) {
        binding.progressBar.setVisibility(View.VISIBLE);

        try {
            // CORREÇÃO 5: Detecta o MimeType real da imagem
            String mimeType = getContentResolver().getType(uri);
            if (mimeType == null) mimeType = "image/jpeg"; // fallback seguro

            File temp = new File(getCacheDir(), "foto_servico.jpg");
            try (InputStream is = getContentResolver().openInputStream(uri);
                 FileOutputStream fos = new FileOutputStream(temp)) {
                if (is == null) throw new RuntimeException("openInputStream null");
                byte[] buf = new byte[8192];
                int n;
                while ((n = is.read(buf)) > 0) fos.write(buf, 0, n);
            }

            // CORREÇÃO 5: Usa o MimeType real em vez de forçar JPG
            RequestBody rb = RequestBody.create(temp, MediaType.parse(mimeType));
            MultipartBody.Part part = MultipartBody.Part
                    .createFormData("file", "foto", rb);

            api.uploadFotoServico(servicoId, part).enqueue(new Callback<Servico>() {
                @Override
                public void onResponse(Call<Servico> call, Response<Servico> response) {
                    binding.progressBar.setVisibility(View.GONE);
                    if (response.isSuccessful()) {
                        Toast.makeText(GerenciarFotosActivity.this,
                                "Foto adicionada!", Toast.LENGTH_SHORT).show();
                        carregar();
                    } else {
                        try {
                            JSONObject o = new JSONObject(response.errorBody().string());
                            Toast.makeText(GerenciarFotosActivity.this,
                                    o.getString("mensagem"), Toast.LENGTH_LONG).show();
                        } catch (Exception e) {
                            Toast.makeText(GerenciarFotosActivity.this,
                                    "Erro ao enviar", Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void onFailure(Call<Servico> call, Throwable t) {
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(GerenciarFotosActivity.this,
                            "Erro de conexão: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        } catch (Exception e) {
            binding.progressBar.setVisibility(View.GONE);
            Log.e("FOTOS", "Erro", e);
            Toast.makeText(this, "Erro: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void removerFoto(String url) {
        new AlertDialog.Builder(this)
                .setTitle("Remover foto")
                .setMessage("Deseja excluir esta foto?")
                .setPositiveButton("Remover", (d, w) -> {
                    binding.progressBar.setVisibility(View.VISIBLE);
                    api.removerFotoServico(servicoId, url).enqueue(new Callback<Servico>() {
                        @Override
                        public void onResponse(Call<Servico> call, Response<Servico> response) {
                            binding.progressBar.setVisibility(View.GONE);
                            if (response.isSuccessful()) carregar();
                        }
                        @Override
                        public void onFailure(Call<Servico> call, Throwable t) {
                            binding.progressBar.setVisibility(View.GONE);
                        }
                    });
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
}