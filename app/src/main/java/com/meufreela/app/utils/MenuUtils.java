package com.meufreela.app.utils;

import android.app.Activity;
import android.content.Intent;
import android.view.View;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.meufreela.app.R;
import com.meufreela.app.activities.MinhasSolicitacoesActivity;
import com.meufreela.app.activities.PerfilActivity;

public class MenuUtils {

    public static void mostrar(Activity activity, View ancora) {
        BottomSheetDialog dialog = new BottomSheetDialog(activity);
        dialog.setContentView(R.layout.menu_bottom_sheet);

        dialog.findViewById(R.id.opcaoPerfil).setOnClickListener(v -> {
            activity.startActivity(new Intent(activity, PerfilActivity.class));
            dialog.dismiss();
        });

        dialog.findViewById(R.id.opcaoSolicitacoes).setOnClickListener(v -> {
            activity.startActivity(new Intent(activity, MinhasSolicitacoesActivity.class));
            dialog.dismiss();
        });

        dialog.show();
    }

    public static void mostrar(Activity activity, View ancora, boolean ehFreelancer) {
        BottomSheetDialog dialog = new BottomSheetDialog(activity);
        dialog.setContentView(R.layout.menu_bottom_sheet);

        dialog.findViewById(R.id.opcaoPerfil).setOnClickListener(v -> {
            activity.startActivity(new Intent(activity, PerfilActivity.class));
            dialog.dismiss();
        });

        View opcaoSolic = dialog.findViewById(R.id.opcaoSolicitacoes);
        if (ehFreelancer) {
            // No painel do freelancer, oculta a opção redundante
            opcaoSolic.setVisibility(View.GONE);
        } else {
            opcaoSolic.setOnClickListener(v -> {
                activity.startActivity(new Intent(activity, MinhasSolicitacoesActivity.class));
                dialog.dismiss();
            });
        }

        dialog.show();
    }
}