package com.meufreela.app.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.meufreela.app.databinding.ItemAvaliacaoBinding;
import com.meufreela.app.models.response.Avaliacao;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AvaliacaoAdapter extends RecyclerView.Adapter<AvaliacaoAdapter.VH> {

    private final List<Avaliacao> itens = new ArrayList<>();

    public void setItens(List<Avaliacao> novas) {
        itens.clear();
        itens.addAll(novas);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAvaliacaoBinding b = ItemAvaliacaoBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new VH(b);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Avaliacao a = itens.get(position);
        h.binding.textAutor.setText(a.getAutorNome());

        int nota = a.getNota() != null ? a.getNota() : 0;
        StringBuilder estrelas = new StringBuilder();
        for (int i = 0; i < 5; i++) estrelas.append(i < nota ? "★" : "☆");
        h.binding.textNota.setText(estrelas.toString());

        String comentario = a.getComentario();
        h.binding.textComentario.setText(
                comentario != null && !comentario.isEmpty()
                        ? comentario : "(sem comentário)");

        h.binding.textData.setText(formatarData(a.getCriadoEm()));
    }

    @Override
    public int getItemCount() { return itens.size(); }

    private String formatarData(String iso) {
        if (iso == null) return "";
        String[] formatos = {"yyyy-MM-dd'T'HH:mm:ss", "yyyy-MM-dd'T'HH:mm"};
        for (String f : formatos) {
            try {
                SimpleDateFormat entrada = new SimpleDateFormat(f, Locale.US);
                SimpleDateFormat saida = new SimpleDateFormat(
                        "dd/MM/yyyy", Locale.getDefault());
                return saida.format(entrada.parse(iso));
            } catch (Exception ignored) {}
        }
        return iso;
    }

    static class VH extends RecyclerView.ViewHolder {
        final ItemAvaliacaoBinding binding;
        VH(ItemAvaliacaoBinding b) { super(b.getRoot()); this.binding = b; }
    }
}