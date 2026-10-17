package com.meufreela.app.utils;

import android.app.Activity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.textfield.TextInputEditText;
import com.meufreela.app.R;
import com.meufreela.app.models.request.CriarAvaliacaoRequest;
import com.meufreela.app.models.response.Avaliacao;
import com.meufreela.app.network.ApiService;
import com.meufreela.app.network.RetrofitClient;
import com.meufreela.app.storage.TokenManager;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AvaliacaoDialog {

    public interface OnAvaliado {
        void concluido();
    }

    public static void mostrar(Activity activity, String solicitacaoId,
                               String nomeAvaliado, OnAvaliado callback) {

        View view = activity.getLayoutInflater()
                .inflate(R.layout.dialog_avaliacao, null);

        TextView subtitulo = view.findViewById(R.id.textSubtitulo);
        subtitulo.setText("Avalie " + nomeAvaliado);

        LinearLayout container = view.findViewById(R.id.containerEstrelas);
        TextInputEditText editComentario = view.findViewById(R.id.editComentario);

        final int[] notaSelecionada = {5};
        final TextView[] estrelas = new TextView[5];

        for (int i = 0; i < 5; i++) {
            final int idx = i;
            TextView estrela = new TextView(activity);
            estrela.setText("★");
            estrela.setTextSize(40);
            estrela.setPadding(12, 0, 12, 0);
            estrela.setTextColor(0xFFFFC107);

            estrela.setOnClickListener(v -> {
                notaSelecionada[0] = idx + 1;
                for (int j = 0; j < 5; j++) {
                    estrelas[j].setTextColor(j <= idx ? 0xFFFFC107 : 0xFFCCCCCC);
                }
            });
            estrelas[i] = estrela;
            container.addView(estrela);
        }

        new AlertDialog.Builder(activity)
                .setView(view)
                .setPositiveButton("Enviar", (d, w) -> {
                    String comentario = editComentario.getText() != null
                            ? editComentario.getText().toString().trim() : "";
                    enviar(activity, solicitacaoId, notaSelecionada[0], comentario, callback);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private static void enviar(Activity activity, String solicitacaoId,
                               int nota, String comentario, OnAvaliado cb) {
        TokenManager tm = new TokenManager(activity);
        ApiService api = RetrofitClient.getApiService(tm);

        api.criarAvaliacao(new CriarAvaliacaoRequest(solicitacaoId, nota, comentario))
                .enqueue(new Callback<Avaliacao>() {
                    @Override
                    public void onResponse(Call<Avaliacao> call,
                                           Response<Avaliacao> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(activity,
                                    "Avaliação enviada. Obrigado!",
                                    Toast.LENGTH_SHORT).show();
                            cb.concluido();
                        } else {
                            try {
                                JSONObject obj = new JSONObject(
                                        response.errorBody().string());
                                Toast.makeText(activity,
                                        obj.getString("mensagem"),
                                        Toast.LENGTH_LONG).show();
                            } catch (Exception e) {
                                Toast.makeText(activity,
                                        "Erro ao avaliar", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<Avaliacao> call, Throwable t) {
                        Toast.makeText(activity,
                                "Erro de conexão: " + t.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }
}