package com.meufreela.app.adapters;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.meufreela.app.databinding.ItemMensagemBinding;
import com.meufreela.app.models.response.Mensagem;

import java.util.ArrayList;
import java.util.List;

public class MensagemAdapter extends RecyclerView.Adapter<MensagemAdapter.VH> {

    private final List<Mensagem> itens = new ArrayList<>();
    private final String meuUsuarioId;

    public MensagemAdapter(String meuUsuarioId) {
        this.meuUsuarioId = meuUsuarioId;
    }

    public void adicionar(Mensagem m) {
        itens.add(m);
        notifyItemInserted(itens.size() - 1);
    }

    public void setItens(List<Mensagem> novas) {
        itens.clear();
        itens.addAll(novas);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemMensagemBinding b = ItemMensagemBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new VH(b);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Mensagem m = itens.get(position);
        boolean minha = m.getRemetenteId() != null
                && m.getRemetenteId().equals(meuUsuarioId);

        h.binding.textConteudo.setText(m.getConteudo());
        h.binding.textAutor.setText(minha ? "Você" : m.getRemetenteNome());

        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams)
                h.binding.textConteudo.getLayoutParams();
        lp.gravity = minha ? Gravity.END : Gravity.START;
        h.binding.textConteudo.setLayoutParams(lp);

        h.binding.textConteudo.setBackgroundColor(
                minha ? 0xFFD1C4E9 : 0xFFFFFFFF);
    }

    @Override
    public int getItemCount() { return itens.size(); }

    static class VH extends RecyclerView.ViewHolder {
        final ItemMensagemBinding binding;
        VH(ItemMensagemBinding b) { super(b.getRoot()); this.binding = b; }
    }
}