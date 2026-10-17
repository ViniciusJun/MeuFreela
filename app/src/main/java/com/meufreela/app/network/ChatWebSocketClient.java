package com.meufreela.app.network;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.meufreela.app.config.ApiConfig;
import com.meufreela.app.models.request.EnviarMensagemRequest;
import com.meufreela.app.models.response.Mensagem;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class ChatWebSocketClient {

    public interface Listener {
        void onMensagem(Mensagem m);
        void onConectado();
        void onErro(String motivo);
    }

    private final OkHttpClient client;
    private final Gson gson = new Gson();
    private WebSocket webSocket;
    private final Listener listener;

    public ChatWebSocketClient(Listener listener) {
        this.listener = listener;
        this.client = new OkHttpClient.Builder()
                .readTimeout(0, TimeUnit.MILLISECONDS) // sem timeout: WS fica aberto
                .build();
    }

    public void conectar(String token, String solicitacaoId) {
        String url = ApiConfig.WS_URL_DEV
                + "?token=" + token
                + "&solicitacao=" + solicitacaoId;

        Request req = new Request.Builder().url(url).build();

        webSocket = client.newWebSocket(req, new WebSocketListener() {
            @Override
            public void onOpen(@NonNull WebSocket ws, @NonNull Response response) {
                listener.onConectado();
            }

            @Override
            public void onMessage(@NonNull WebSocket ws, @NonNull String text) {
                try {
                    Mensagem m = gson.fromJson(text, Mensagem.class);
                    if (m != null && m.getId() != null) {
                        listener.onMensagem(m);
                    }
                } catch (Exception ignored) {}
            }

            @Override
            public void onFailure(@NonNull WebSocket ws, @NonNull Throwable t,
                                  Response response) {
                listener.onErro(t.getMessage());
            }

            @Override
            public void onClosed(@NonNull WebSocket ws, int code, @NonNull String reason) {
                listener.onErro("Conexão fechada");
            }
        });
    }

    public void enviar(String conteudo) {
        if (webSocket != null) {
            String json = gson.toJson(new EnviarMensagemRequest(conteudo));
            webSocket.send(json);
        }
    }

    public void fechar() {
        if (webSocket != null) webSocket.close(1000, "saindo");
    }
}