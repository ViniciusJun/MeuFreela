package com.meufreela.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.meufreela.app.databinding.ItemServicoBinding;
import com.meufreela.app.models.response.Servico;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ServicoAdapter extends RecyclerView.Adapter<ServicoAdapter.VH> {

    public interface Acoes {
        void onServicoClick(Servico s);
        void onPausar(Servico s);
        void onExcluir(Servico s);

        // CORREÇÃO: Adicionado 'default' para não quebrar telas que não usam fotos (ex: MainClienteActivity)
        default void onGerenciarFotos(Servico s) {}
    }

    private final List<Servico> itens = new ArrayList<>();
    private final Acoes acoes;
    private final boolean modoDono;

    public ServicoAdapter(Acoes acoes, boolean modoDono) {
        this.acoes = acoes;
        this.modoDono = modoDono;
    }

    public void setItens(List<Servico> novas) {
        itens.clear();
        itens.addAll(novas);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemServicoBinding b = ItemServicoBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new VH(b);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Servico s = itens.get(position);

        h.binding.textTitulo.setText(s.getTitulo());
        h.binding.textDescricao.setText(s.getDescricao());
        h.binding.textCategoria.setText(rotuloCategoria(s.getCategoria()));

        // O botão de fotos só aparece se estiver no modo dono
        if (modoDono) {
            h.binding.botaoFotos.setVisibility(View.VISIBLE);
            h.binding.botaoFotos.setOnClickListener(v -> acoes.onGerenciarFotos(s));
        } else {
            h.binding.botaoFotos.setVisibility(View.GONE);
        }

        if (!modoDono) {
            h.binding.textFreelancer.setText(
                    "por " + s.getFreelancerNome()
                            + (s.isFreelancerVerificado() ? " ✓" : ""));
        } else {
            h.binding.textFreelancer.setVisibility(View.GONE);
        }

        double preco = s.getPreco() != null ? s.getPreco() : 0;
        h.binding.textPreco.setText(String.format(Locale.getDefault(), "R$ %.0f/h", preco));

        if (modoDono) {
            h.binding.textStatus.setText(s.isAtivo() ? "ATIVO" : "PAUSADO");
            h.binding.textStatus.setTextColor(0xFFFFFFFF);
            h.binding.textStatus.setBackgroundColor(
                    s.isAtivo() ? 0xFF4CAF50 : 0xFF9E9E9E);
            h.binding.containerAcoesDono.setVisibility(View.VISIBLE);
            h.binding.botaoPausar.setText(s.isAtivo() ? "Pausar" : "Reativar");
            h.binding.botaoPausar.setOnClickListener(v -> acoes.onPausar(s));
            h.binding.botaoExcluir.setOnClickListener(v -> acoes.onExcluir(s));
        } else {
            h.binding.textStatus.setVisibility(View.GONE);
            h.binding.containerAcoesDono.setVisibility(View.GONE);
        }

        h.binding.getRoot().setOnClickListener(v -> acoes.onServicoClick(s));

        // Foto do freelancer (se vier)
        String fotoPath = s.getFreelancerFotoUrl();
        if (fotoPath != null && !fotoPath.isEmpty()) {
            String urlCompleta = com.meufreela.app.config.ApiConfig.SERVER_URL_DEV + fotoPath;
            com.bumptech.glide.Glide.with(h.binding.getRoot().getContext())
                    .load(urlCompleta)
                    .placeholder(android.R.drawable.ic_menu_myplaces)
                    .circleCrop()
                    .into(h.binding.imageFreelancerFoto);
        } else {
            h.binding.imageFreelancerFoto.setImageResource(
                    android.R.drawable.ic_menu_myplaces);
        }
    }

    @Override
    public int getItemCount() { return itens.size(); }

    private String rotuloCategoria(String c) {
        if (c == null) return "";
        switch (c) {
            case "REFORMAS_REPAROS": return "Reformas";
            case "SERVICOS_DOMESTICOS": return "Domésticos";
            case "TECNOLOGIA": return "Tecnologia";
            case "EVENTOS": return "Eventos";
            case "BELEZA": return "Beleza";
            default: return c;
        }
    }

    static class VH extends RecyclerView.ViewHolder {
        final ItemServicoBinding binding;
        VH(ItemServicoBinding b) { super(b.getRoot()); this.binding = b; }
    }
}