package com.meufreela.app.utils;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.meufreela.app.R;
import com.meufreela.app.activities.AvaliacoesActivity;
import com.meufreela.app.activities.MainClienteActivity;
import com.meufreela.app.activities.MainFreelancerActivity;
import com.meufreela.app.activities.MinhasSolicitacoesActivity;
import com.meufreela.app.activities.PerfilActivity;
import com.meufreela.app.models.response.PerfilUsuario;
import com.meufreela.app.network.ApiService;
import com.meufreela.app.network.RetrofitClient;
import com.meufreela.app.storage.TokenManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MenuUtils {

    public static void mostrar(Activity activity, View ancora) {
        BottomSheetDialog dialog = new BottomSheetDialog(activity);
        dialog.setContentView(R.layout.menu_bottom_sheet);

        TokenManager tm = new TokenManager(activity);
        ApiService api = RetrofitClient.getApiService(tm);

        TextView opcaoPerfil = dialog.findViewById(R.id.opcaoPerfil);
        TextView opcaoSolicitacoes = dialog.findViewById(R.id.opcaoSolicitacoes);
        TextView opcaoAvaliacoes = dialog.findViewById(R.id.opcaoAvaliacoes);

        // --- Meu perfil ---
        if (opcaoPerfil != null) {
            opcaoPerfil.setOnClickListener(v -> {
                activity.startActivity(new Intent(activity, PerfilActivity.class));
                dialog.dismiss();
            });
        }

        // --- Minhas avaliações ---
        if (opcaoAvaliacoes != null) {
            opcaoAvaliacoes.setOnClickListener(v -> {
                activity.startActivity(new Intent(activity, AvaliacoesActivity.class));
                dialog.dismiss();
            });
        }

        // --- Minhas solicitações / Trocar de modo ---
        if (opcaoSolicitacoes != null) {
            api.meuPerfil().enqueue(new Callback<PerfilUsuario>() {
                @Override
                public void onResponse(Call<PerfilUsuario> call,
                                       Response<PerfilUsuario> response) {
                    if (!response.isSuccessful() || response.body() == null) {
                        configurarMinhasSolicitacoes(activity, dialog, opcaoSolicitacoes);
                        return;
                    }

                    PerfilUsuario u = response.body();
                    boolean temAmbos = u.temPapel("FREELANCER") && u.temPapel("CLIENTE");

                    if (temAmbos) {
                        boolean estaNoModoCliente = activity instanceof MainClienteActivity;
                        opcaoSolicitacoes.setText(estaNoModoCliente
                                ? "🔧  Mudar para modo Freelancer"
                                : "🛒  Mudar para modo Cliente");
                        opcaoSolicitacoes.setOnClickListener(v -> {
                            Class<?> destino = estaNoModoCliente
                                    ? MainFreelancerActivity.class
                                    : MainClienteActivity.class;
                            activity.startActivity(new Intent(activity, destino));
                            dialog.dismiss();
                        });
                    } else {
                        configurarMinhasSolicitacoes(activity, dialog, opcaoSolicitacoes);
                    }
                }

                @Override
                public void onFailure(Call<PerfilUsuario> call, Throwable t) {
                    configurarMinhasSolicitacoes(activity, dialog, opcaoSolicitacoes);
                }
            });
        }

        dialog.show();
    }

    private static void configurarMinhasSolicitacoes(Activity activity,
                                                     BottomSheetDialog dialog,
                                                     TextView opcao) {
        opcao.setText("📋  Minhas solicitações");
        opcao.setOnClickListener(v -> {
            activity.startActivity(new Intent(activity, MinhasSolicitacoesActivity.class));
            dialog.dismiss();
        });
    }
}