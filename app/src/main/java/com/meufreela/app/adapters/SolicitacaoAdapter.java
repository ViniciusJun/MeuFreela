package com.meufreela.app.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.meufreela.app.databinding.ItemSolicitacaoBinding;
import com.meufreela.app.models.response.Solicitacao;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SolicitacaoAdapter extends RecyclerView.Adapter<SolicitacaoAdapter.VH> {

    public interface Acoes {
        void aceitar(Solicitacao s);
        void recusar(Solicitacao s);
    }

    private final List<Solicitacao> itens = new ArrayList<>();
    private final Acoes acoes;
    private final boolean ehFreelancer; // se true, mostra botões Aceitar/Recusar

    public SolicitacaoAdapter(Acoes acoes, boolean ehFreelancer) {
        this.acoes = acoes;
        this.ehFreelancer = ehFreelancer;
    }

    public void setItens(List<Solicitacao> novas) {
        itens.clear();
        itens.addAll(novas);
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
        Solicitacao s = itens.get(position);

        // Nome exibido: para o freelancer, o cliente; para o cliente, o freelancer
        String nome = ehFreelancer ? s.getClienteNome() : s.getFreelancerNome();
        h.binding.textNome.setText(nome);

        h.binding.textDescricao.setText(s.getDescricao());
        h.binding.textEndereco.setText("📍 " + s.getEndereco());

        String dataFormatada = formatarData(s.getDataDesejada());
        h.binding.textData.setText("🗓 " + dataFormatada
                + " · " + (s.getDuracaoHoras() != null ? s.getDuracaoHoras() : 0) + "h");

        double valor = s.getValorTotal() != null ? s.getValorTotal() : 0;
        h.binding.textValor.setText(String.format(Locale.getDefault(), "R$ %.2f", valor));

        aplicarStatus(h.binding, s.getStatus());

        // Botões só para o freelancer e quando PENDENTE
        boolean mostrarBotoes = ehFreelancer && "PENDENTE".equals(s.getStatus());
        h.binding.containerAcoes.setVisibility(mostrarBotoes ? View.VISIBLE : View.GONE);

        h.binding.botaoAceitar.setOnClickListener(v -> acoes.aceitar(s));
        h.binding.botaoRecusar.setOnClickListener(v -> acoes.recusar(s));
    }

    @Override
    public int getItemCount() { return itens.size(); }

    private void aplicarStatus(ItemSolicitacaoBinding b, String status) {
        int cor;
        String rotulo;
        switch (status) {
            case "PENDENTE":
                rotulo = "PENDENTE"; cor = 0xFFFFA000; break;
            case "ACEITA":
                rotulo = "ACEITA"; cor = 0xFF2196F3; break;
            case "EM_ANDAMENTO":
                rotulo = "EM ANDAMENTO"; cor = 0xFF03A9F4; break;
            case "CONCLUIDA":
                rotulo = "CONCLUÍDA"; cor = 0xFF4CAF50; break;
            case "RECUSADA":
                rotulo = "RECUSADA"; cor = 0xFFF44336; break;
            case "CANCELADA":
                rotulo = "CANCELADA"; cor = 0xFF9E9E9E; break;
            case "PAGA":
                rotulo = "PAGA"; cor = 0xFF4CAF50; break;
            default:
                rotulo = status; cor = 0xFF9E9E9E;
        }
        b.textStatus.setText(rotulo);
        b.textStatus.setTextColor(Color.WHITE);
        b.textStatus.setBackgroundColor(cor);
    }

    private String formatarData(String iso) {
        if (iso == null) return "—";
        try {
            // backend envia algo como "2026-10-15T14:00:00"
            SimpleDateFormat entrada = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
            SimpleDateFormat saida = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            return saida.format(entrada.parse(iso));
        } catch (Exception e) {
            return iso;
        }
    }

    static class VH extends RecyclerView.ViewHolder {
        final ItemSolicitacaoBinding binding;
        VH(ItemSolicitacaoBinding b) { super(b.getRoot()); this.binding = b; }
    }
}