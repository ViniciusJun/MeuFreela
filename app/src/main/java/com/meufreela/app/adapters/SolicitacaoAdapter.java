package com.meufreela.app.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.meufreela.app.databinding.ItemSolicitacaoBinding;
import com.meufreela.app.models.response.Solicitacao;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class SolicitacaoAdapter extends RecyclerView.Adapter<SolicitacaoAdapter.VH> {

    public interface AcaoListener {
        void onAcao(Solicitacao s, String novoStatus);
    }

    private final List<Solicitacao> itens = new ArrayList<>();
    private final AcaoListener listener;

    public SolicitacaoAdapter(AcaoListener listener) {
        this.listener = listener;
    }

    public void setItens(List<Solicitacao> novos) {
        itens.clear();
        itens.addAll(novos);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemSolicitacaoBinding b = ItemSolicitacaoBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new VH(b);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        h.bind(itens.get(position));
    }

    @Override
    public int getItemCount() { return itens.size(); }

    class VH extends RecyclerView.ViewHolder {
        private final ItemSolicitacaoBinding b;

        VH(ItemSolicitacaoBinding b) {
            super(b.getRoot());
            this.b = b;
        }

        void bind(Solicitacao s) {
            b.textCliente.setText(s.getClienteNome());
            b.textDescricao.setText(s.getDescricao());
            b.textEndereco.setText(s.getEndereco());

            // Data formatada (backend envia ISO-8601 sem timezone)
            String data = formatarData(s.getDataDesejada());
            Integer dur = s.getDuracaoHoras() != null ? s.getDuracaoHoras() : 0;
            b.textData.setText("📅 " + data + "  ·  ⏱ " + dur + "h");

            double valor = s.getValorTotal() != null ? s.getValorTotal() : 0;
            b.textValor.setText(String.format(Locale.getDefault(), "R$ %.2f", valor));

            aplicarEstiloStatus(s.getStatus());
            montarBotoes(s);
        }

        private void aplicarEstiloStatus(String status) {
            b.textStatus.setText(rotuloStatus(status));
            b.textStatus.setBackgroundColor(corStatus(status));
        }

        private String rotuloStatus(String s) {
            if (s == null) return "";
            switch (s) {
                case "PENDENTE": return "PENDENTE";
                case "ACEITA": return "ACEITA";
                case "RECUSADA": return "RECUSADA";
                case "EM_ANDAMENTO": return "EM ANDAMENTO";
                case "CONCLUIDA": return "CONCLUÍDA";
                case "CANCELADA": return "CANCELADA";
                case "PAGA": return "PAGA";
                default: return s;
            }
        }

        private int corStatus(String s) {
            if (s == null) return Color.GRAY;
            switch (s) {
                case "PENDENTE": return Color.parseColor("#FFA000"); // laranja
                case "ACEITA": return Color.parseColor("#2196F3");   // azul
                case "EM_ANDAMENTO": return Color.parseColor("#6200EE"); // roxo
                case "CONCLUIDA":
                case "PAGA": return Color.parseColor("#4CAF50");      // verde
                case "RECUSADA":
                case "CANCELADA": return Color.parseColor("#E53935"); // vermelho
                default: return Color.GRAY;
            }
        }

        private void montarBotoes(Solicitacao s) {
            b.containerAcoes.removeAllViews();
            String st = s.getStatus();
            if (st == null) return;

            switch (st) {
                case "PENDENTE":
                    adicionarBotao("Aceitar", "#4CAF50",
                            () -> listener.onAcao(s, "ACEITA"));
                    adicionarBotao("Recusar", "#E53935",
                            () -> listener.onAcao(s, "RECUSADA"));
                    break;
                case "ACEITA":
                    adicionarBotao("Iniciar serviço", "#6200EE",
                            () -> listener.onAcao(s, "EM_ANDAMENTO"));
                    break;
                case "EM_ANDAMENTO":
                    adicionarBotao("Marcar como concluído", "#4CAF50",
                            () -> listener.onAcao(s, "CONCLUIDA"));
                    break;
                // CONCLUIDA, RECUSADA, CANCELADA, PAGA não têm ação
            }
        }

        private void adicionarBotao(String texto, String corHex, Runnable onClick) {
            Button btn = new Button(b.getRoot().getContext());
            btn.setText(texto);
            btn.setAllCaps(false);
            btn.setTextColor(Color.WHITE);
            btn.setBackgroundColor(Color.parseColor(corHex));

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            lp.setMargins(4, 0, 4, 0);
            btn.setLayoutParams(lp);
            btn.setOnClickListener(v -> onClick.run());
            b.containerAcoes.addView(btn);
        }

        private String formatarData(String iso) {
            if (iso == null || iso.isEmpty()) return "";
            try {
                SimpleDateFormat entrada = new SimpleDateFormat(
                        "yyyy-MM-dd'T'HH:mm:ss", Locale.US);
                entrada.setTimeZone(TimeZone.getTimeZone("UTC"));
                SimpleDateFormat saida = new SimpleDateFormat(
                        "dd/MM 'às' HH:mm", Locale.getDefault());
                return saida.format(entrada.parse(iso));
            } catch (ParseException e) {
                return iso;
            }
        }
    }
}