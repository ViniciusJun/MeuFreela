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
import com.meufreela.app.activities.MeusServicosActivity;
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
        TextView opcaoMeusServicos = dialog.findViewById(R.id.opcaoMeusServicos);

        // Ações fixas
        opcaoPerfil.setOnClickListener(v -> {
            activity.startActivity(new Intent(activity, PerfilActivity.class));
            dialog.dismiss();
        });

        // Busca o perfil para decidir o que mostrar
        api.meuPerfil().enqueue(new Callback<PerfilUsuario>() {
            @Override
            public void onResponse(Call<PerfilUsuario> call,
                                   Response<PerfilUsuario> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    // fallback: mostra só o básico
                    configurarSolicitacoesPadrao(activity, dialog, opcaoSolicitacoes);
                    return;
                }

                PerfilUsuario u = response.body();
                boolean ehCliente = u.temPapel("CLIENTE");
                boolean ehFreelancer = u.temPapel("FREELANCER");
                boolean ambos = ehCliente && ehFreelancer;

                // 📋 Minhas solicitações — sempre visível, mas o rótulo muda
                if (ambos) {
                    boolean estaNoModoCliente = activity instanceof MainClienteActivity;
                    if (estaNoModoCliente) {
                        opcaoSolicitacoes.setText("🛠  Mudar para modo Freelancer");
                        opcaoSolicitacoes.setOnClickListener(v -> {
                            activity.startActivity(new Intent(activity,
                                    MainFreelancerActivity.class));
                            dialog.dismiss();
                        });
                    } else {
                        opcaoSolicitacoes.setText("🛒  Mudar para modo Cliente");
                        opcaoSolicitacoes.setOnClickListener(v -> {
                            activity.startActivity(new Intent(activity,
                                    MainClienteActivity.class));
                            dialog.dismiss();
                        });
                    }
                } else {
                    configurarSolicitacoesPadrao(activity, dialog, opcaoSolicitacoes);
                }

                // 🛠 Meus serviços — só para freelancer
                if (ehFreelancer) {
                    opcaoMeusServicos.setVisibility(View.VISIBLE);
                    opcaoMeusServicos.setOnClickListener(v -> {
                        activity.startActivity(new Intent(activity,
                                MeusServicosActivity.class));
                        dialog.dismiss();
                    });
                } else {
                    opcaoMeusServicos.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<PerfilUsuario> call, Throwable t) {
                configurarSolicitacoesPadrao(activity, dialog, opcaoSolicitacoes);
            }
        });

        dialog.show();
    }

    private static void configurarSolicitacoesPadrao(Activity activity,
                                                     BottomSheetDialog dialog,
                                                     TextView opcaoSolicitacoes) {
        opcaoSolicitacoes.setText("📋  Minhas solicitações");
        opcaoSolicitacoes.setOnClickListener(v -> {
            activity.startActivity(new Intent(activity, MinhasSolicitacoesActivity.class));
            dialog.dismiss();
        });
    }
}