package com.meufreela.app.activities;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.meufreela.app.adapters.MensagemAdapter;
import com.meufreela.app.databinding.ActivityChatBinding;
import com.meufreela.app.models.response.Mensagem;
import com.meufreela.app.network.ApiService;
import com.meufreela.app.network.ChatWebSocketClient;
import com.meufreela.app.network.RetrofitClient;
import com.meufreela.app.storage.TokenManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends AppCompatActivity {

    public static final String EXTRA_SOLICITACAO_ID = "solicitacao_id";
    public static final String EXTRA_OUTRO_NOME = "outro_nome";

    private ActivityChatBinding binding;
    private MensagemAdapter adapter;
    private ChatWebSocketClient ws;
    private String solicitacaoId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Edge-to-edge correto para Android 15+
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Tratamento de insets: status bar no header + teclado no input
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets ime = insets.getInsets(WindowInsetsCompat.Type.ime());

            binding.textTitulo.setPadding(
                    bars.left + dp(20),
                    bars.top + dp(20),
                    bars.right + dp(20),
                    dp(20)
            );

            int bottomInset = Math.max(bars.bottom, ime.bottom);
            binding.layoutInput.setPadding(
                    dp(8),
                    dp(8),
                    dp(8),
                    bottomInset + dp(8)
            );

            if (ime.bottom > 0) {
                binding.recyclerMensagens.post(this::rolarParaFim);
            }

            return insets;
        });

        solicitacaoId = getIntent().getStringExtra(EXTRA_SOLICITACAO_ID);
        String outroNome = getIntent().getStringExtra(EXTRA_OUTRO_NOME);

        binding.textTitulo.setText("Chat com " + outroNome);

        TokenManager tm = new TokenManager(this);
        ApiService api = RetrofitClient.getApiService(tm);

        adapter = new MensagemAdapter(tm.getUserId());
        binding.recyclerMensagens.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerMensagens.setAdapter(adapter);

        api.historicoChat(solicitacaoId).enqueue(new Callback<List<Mensagem>>() {
            @Override
            public void onResponse(Call<List<Mensagem>> call, Response<List<Mensagem>> r) {
                if (r.isSuccessful() && r.body() != null) {
                    adapter.setItens(r.body());
                    rolarParaFim();
                }
            }

            @Override
            public void onFailure(Call<List<Mensagem>> call, Throwable t) {
            }
        });

        ws = new ChatWebSocketClient(new ChatWebSocketClient.Listener() {
            @Override
            public void onMensagem(Mensagem m) {
                runOnUiThread(() -> {
                    adapter.adicionar(m);
                    rolarParaFim();
                });
            }

            @Override
            public void onConectado() {
                runOnUiThread(() -> Toast.makeText(ChatActivity.this,
                        "Conectado", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onErro(String motivo) {
                runOnUiThread(() -> Toast.makeText(ChatActivity.this,
                        "Chat desconectado: " + motivo, Toast.LENGTH_LONG).show());
            }
        });
        ws.conectar(tm.getAccessToken(), solicitacaoId);

        binding.botaoEnviar.setOnClickListener(v -> {
            String texto = binding.editMensagem.getText().toString().trim();
            if (texto.isEmpty()) return;
            ws.enviar(texto);
            binding.editMensagem.setText("");
        });
    }

    private int dp(int valor) {
        return Math.round(valor * getResources().getDisplayMetrics().density);
    }

    private void rolarParaFim() {
        if (adapter.getItemCount() > 0) {
            binding.recyclerMensagens.smoothScrollToPosition(adapter.getItemCount() - 1);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (ws != null) ws.fechar();
    }
}