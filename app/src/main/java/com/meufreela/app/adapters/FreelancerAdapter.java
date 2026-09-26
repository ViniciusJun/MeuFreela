package com.meufreela.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.meufreela.app.databinding.ItemFreelancerBinding;
import com.meufreela.app.models.response.Freelancer;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FreelancerAdapter extends RecyclerView.Adapter<FreelancerAdapter.ViewHolder> {

    public interface OnItemClick {
        void onClick(Freelancer f);
    }

    private final List<Freelancer> itens = new ArrayList<>();
    private final OnItemClick listener;

    public FreelancerAdapter(OnItemClick listener) {
        this.listener = listener;
    }

    public void setItens(List<Freelancer> novos) {
        itens.clear();
        itens.addAll(novos);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemFreelancerBinding binding = ItemFreelancerBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(itens.get(position));
    }

    @Override
    public int getItemCount() { return itens.size(); }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemFreelancerBinding binding;

        ViewHolder(ItemFreelancerBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Freelancer f) {
            binding.textNome.setText(f.getNome());
            binding.textCategoria.setText(rotuloCategoria(f.getCategoria()));
            binding.textDescricao.setText(f.getDescricao());

            double avaliacao = f.getAvaliacaoMedia() != null ? f.getAvaliacaoMedia() : 0;
            int total = f.getTotalServicos() != null ? f.getTotalServicos() : 0;
            binding.textAvaliacao.setText(String.format(Locale.getDefault(),
                    "★ %.1f (%d serviços)", avaliacao, total));

            double preco = f.getPrecoHora() != null ? f.getPrecoHora() : 0;
            binding.textPreco.setText(String.format(Locale.getDefault(),
                    "R$ %.0f/h", preco));

            binding.textVerificado.setVisibility(f.isVerificado() ? View.VISIBLE : View.GONE);

            binding.getRoot().setOnClickListener(v -> listener.onClick(f));
        }

        private String rotuloCategoria(String c) {
            if (c == null) return "";
            switch (c) {
                case "REFORMAS_REPAROS": return "Reformas e Reparos";
                case "SERVICOS_DOMESTICOS": return "Serviços Domésticos";
                case "TECNOLOGIA": return "Tecnologia";
                case "EVENTOS": return "Eventos";
                case "BELEZA": return "Beleza";
                default: return c;
            }
        }
    }
}