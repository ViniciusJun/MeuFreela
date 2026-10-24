package com.meufreela.app.services;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.meufreela.app.R;
import com.meufreela.app.activities.ChatActivity;
import com.meufreela.app.activities.SplashActivity;

public class MeuFirebaseMessagingService extends FirebaseMessagingService {

    private static final String CANAL_ID = "meufreela_default";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage msg) {
        criarCanalSeNecessario();

        String titulo = msg.getNotification() != null
                ? msg.getNotification().getTitle() : "Meu Freela";
        String corpo = msg.getNotification() != null
                ? msg.getNotification().getBody() : "";

        String tipo = msg.getData().get("tipo");
        String solicitacaoId = msg.getData().get("solicitacaoId");

        Intent intent;
        if ("NOVA_MENSAGEM".equals(tipo) && solicitacaoId != null) {
            intent = new Intent(this, ChatActivity.class);
            intent.putExtra(ChatActivity.EXTRA_SOLICITACAO_ID, solicitacaoId);
            intent.putExtra(ChatActivity.EXTRA_OUTRO_NOME,
                    msg.getData().get("remetenteNome"));
        } else {
            intent = new Intent(this, SplashActivity.class);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pi = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        if (Build.VERSION.SDK_INT >= 33
                && ContextCompat.checkSelfPermission(this,
                Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        NotificationCompat.Builder n = new NotificationCompat.Builder(this, CANAL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(titulo)
                .setContentText(corpo)
                .setAutoCancel(true)
                .setContentIntent(pi)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManager nm =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify((int) System.currentTimeMillis(), n.build());
    }

    private void criarCanalSeNecessario() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel canal = new NotificationChannel(
                    CANAL_ID, "Meu Freela",
                    NotificationManager.IMPORTANCE_HIGH);
            NotificationManager nm =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            nm.createNotificationChannel(canal);
        }
    }
}