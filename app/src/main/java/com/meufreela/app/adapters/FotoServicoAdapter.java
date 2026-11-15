package com.meufreela.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.meufreela.app.config.ApiConfig;
import com.meufreela.app.databinding.ItemFotoServicoBinding;

import java.util.ArrayList;
import java.util.List;

public class FotoServicoAdapter extends RecyclerView.Adapter<FotoServicoAdapter.VH> {

    public interface Acoes {
        void onRemover(String urlFoto);
    }

    private final List<String> fotos = new ArrayList<>();
    private final Acoes acoes;

    public FotoServicoAdapter(Acoes acoes) {
        this.acoes = acoes;
    }

    public void setItens(List<String> novas) {
        fotos.clear();
        fotos.addAll(novas);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemFotoServicoBinding b = ItemFotoServicoBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new VH(b);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        String url = fotos.get(position);
        Glide.with(h.itemView.getContext())
                .load(ApiConfig.SERVER_URL_DEV + url)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .into(h.binding.imageFoto);

        h.binding.botaoRemover.setOnClickListener(v -> acoes.onRemover(url));
    }

    @Override
    public int getItemCount() { return fotos.size(); }

    static class VH extends RecyclerView.ViewHolder {
        final ItemFotoServicoBinding binding;
        VH(ItemFotoServicoBinding b) { super(b.getRoot()); this.binding = b; }
    }
}