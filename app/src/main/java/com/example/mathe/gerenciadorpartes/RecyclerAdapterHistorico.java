package com.example.mathe.gerenciadorpartes;


import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.example.mathe.gerenciadorpartes.Model.PontosPublicadores;

import java.util.ArrayList;

/**
 * Created by mathe on 24/12/2017.
 */
//todo: lazyloader carregar por blocos (i.e. de 20 em 20 registros)

//Tutorial:
//  http://www.luiztools.com.br/post/tutorial-crud-em-android-com-sqlite-e-recyclerview-2/
//Para implementar itens clicáveis no RecyclerView (já que não pode ser implementado da mesma forma que a ListView):
//  https://gist.github.com/riyazMuhammad/1c7b1f9fa3065aa5a46f
public class RecyclerAdapterHistorico extends RecyclerView.Adapter<HolderHistorico> {

    private ArrayList<PontosPublicadores> publicadores;

    public RecyclerAdapterHistorico(Cursor dados){
        //Copia todos os dados do cursor em uma array
        publicadores = new ArrayList<>();
        while(!dados.isAfterLast()) {
            publicadores.add(new PontosPublicadores(0, dados.getString(1), dados.getInt(3), dados.getString(2), dados.getString(4), dados.getInt(5), dados.getInt(6), dados.getInt(7), dados.getInt(0)));
            dados.moveToNext();
        }
    }

    //Este método é responsável por criar a view da linha (holder)
    @Override
    public HolderHistorico onCreateViewHolder(ViewGroup parent, int viewType) {
        HolderHistorico hh = new HolderHistorico(LayoutInflater.from(parent.getContext()).inflate(R.layout.linhah, parent, false));
        return hh;
    }

    //Neste método, o holder (view da linha) é associado aos dados na posição requeriada pelo Recyclerview
    @Override
    public void onBindViewHolder(HolderHistorico holder, int position) {
        PontosPublicadores publicador = publicadores.get(position);

        //É definido o dados que preencherá cada componente de uma linha da linha
        holder.setNome_pub(publicador.getPublic_nome());
        holder.setNome_ajud(publicador.getComp_nome());
        holder.setNumero_ponto(String.valueOf(publicador.getPonto_id()));
        holder.setPonto_suger(String.valueOf(publicador.getPonto_sug()));
        holder.setPassou(publicador.getPassou(), publicador.getPonto_id());
        holder.setTipo_parte(publicador.getTipo_partenome());
        holder.setData_parte(publicador.getData_concl());
    }

    @Override
    public int getItemCount() {
        return publicadores.size();
    }
}
