package com.example.mathe.gerenciadorpartes;

import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.example.mathe.gerenciadorpartes.Model.Publicador;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by mathe on 09/01/2018.
 */

public class AdapterExpandableSugeridos extends BaseExpandableListAdapter {

    //O HashMap "publicadores" armazenará em 2 chaves
    //0 - sexo masculino
    //1 - sexo feminino
    private HashMap<Integer, ArrayList<Publicador>> publicadores;
    private ArrayList<String> titulos;                      //título de cada grupo, em que titulos.get(0) = titulo de publicadores.get(0)

    //Quando a view da ExpandableList é recriada, seja por atualização/ordenacao de valores
    //ou mesmo girar a tela, os grupos encolhem. Para isso não acontecer nessas
    //ocasiões, foi criado uma lista para memorizar o estado de cada grupo
    //recuperando-o para o estado anterior caso necessário
    private ArrayList<Boolean> expandido;                   //ArrayList que indica quais grupos estão expandidos, em que expandido.get(0) = titulo de publicadores.get(0)

    public AdapterExpandableSugeridos(Cursor consulta, ArrayList<String> titulos){

        //Inicializa o HashMap "publicadores" de forma que
        //0 - sexo masculino
        //1 - sexo feminino
        publicadores = new HashMap<>();
        publicadores.put(0, new ArrayList<Publicador>());
        publicadores.put(1, new ArrayList<Publicador>());
        this.titulos = titulos;

        //Inicializa a Lista "expandido" assumindo que no início, todos os grupos estão encolhidos
        expandido = new ArrayList<>();
        for(int i = 0; i < titulos.size(); i++){
            expandido.add(false);
        }

        while(!consulta.isAfterLast()){
            //Obtem o campo que armazena o sexo do cursor, incluindo o publicador em seu respectivo grupo
            String s = consulta.getString(4);
            if(s.equals("m")) {
                publicadores.get(0).add(new Publicador(consulta.getInt(0), consulta.getString(1), consulta.getInt(2), consulta.getString(3), consulta.getString(4), consulta.getInt(5)));
            }
            else{
                publicadores.get(1).add(new Publicador(consulta.getInt(0), consulta.getString(1), consulta.getInt(2), consulta.getString(3), consulta.getString(4), consulta.getInt(5)));
            }
            consulta.moveToNext();
        }

        consulta.close();
    }

    public void setPublicadores(HashMap<Integer, ArrayList<Publicador>> publicadores){
        this.publicadores = publicadores;
    }

    public HashMap<Integer, ArrayList<Publicador>> obterPublicadores(){
        return this.publicadores;
    }

    //############# A sobrescrição dos 2 metodos abaixo não é obrigatória ##########

    //Os dois metodos abaixo atualizam o vetor que indica quais grupos estão expandidos ou não
    @Override
    public void onGroupExpanded(int groupPosition) {
        super.onGroupExpanded(groupPosition);
        expandido.set(groupPosition, true);
    }

    @Override
    public void onGroupCollapsed(int groupPosition) {
        super.onGroupCollapsed(groupPosition);
        expandido.set(groupPosition, false);
    }

    public ArrayList<Boolean> getExpandidos(){
        return expandido;
    }

    //################################################################

    @Override
    public int getGroupCount() {
        return publicadores.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return publicadores.get(groupPosition).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return publicadores.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return publicadores.get(groupPosition).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return publicadores.get(groupPosition).get(childPosition).getId();
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    //Monta a view do grupo
    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        //Se "convertView" == null, a View do grupo "groupPosition" ainda não foi criada
        //Assim sendo, é inflado a view do grupo. Isso é feito para que o programa possa
        //reaproveitar as view e economizar recursos
        if(convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.titulos_expansablelist, null, false);
        }

        //As linhas abaixo devem ficar fora do if, senão os campos dos grupos somente serão atualizados se convertView = null
        //o que causa problemas ao expandir/contrair os grupos

        //Os componentes são obtidos da view inflada e recebem seus valores dependendo do "groupPosition"
        TextView titulo_grupo = (TextView) convertView.findViewById(R.id.titulo_sugeridos);
        TextView contagem_grupo = (TextView) convertView.findViewById(R.id.itens_grupo);

        //Preenche os componentes da view
        titulo_grupo.setText(titulos.get(groupPosition));
        contagem_grupo.setText(String.valueOf(publicadores.get(groupPosition).size()));

        return convertView;
    }

    //Monta a view dos itens de cada grupo
    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        //Se "convertView" == null, a View do item "childPosition" do grupo "groupPosition" ainda não foi criada
        //Assim sendo, é inflado a view do item. Isso é feito para que o programa possa
        //reaproveitar as view e economizar recursos
        if(convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.linhas, null, false);
        }

        //Essa parte deve ficar fora do if acima, senão os campos dos grupos somente serão atualizados se convertView = null
        //fazendo repetir o elementos de um grupo em outros ao fechar e abrir os grupos sucessivamente

        //Os componentes são obtidos da view inflada e recebem seus valores dependendo do "groupPosition" e "groupChild"
        TextView nome_publicador = (TextView) convertView.findViewById(R.id.nomep);;
        TextView num_partes = (TextView) convertView.findViewById(R.id.countp);
        TextView data_ultima_parte = (TextView) convertView.findViewById(R.id.datap);
        TextView ponto_sugerido = (TextView) convertView.findViewById(R.id.sugeridop);

        //Preenche os componentes da view
        nome_publicador.setText(publicadores.get(groupPosition).get(childPosition).getNome());
        num_partes.setText(String.valueOf(publicadores.get(groupPosition).get(childPosition).getNpartes()) + " partes");
        data_ultima_parte.setText(publicadores.get(groupPosition).get(childPosition).getData());

        //Se o ponto sugerido for 0, então nenhum ponto foi sugerido na ultima parte desse publicador
        if(publicadores.get(groupPosition).get(childPosition).getSugerido() == 0) {
            ponto_sugerido.setText("Sem ponto sugerido");
        }
        else{
            ponto_sugerido.setText("Ponto " + String.valueOf(publicadores.get(groupPosition).get(childPosition).getSugerido()));
        }

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
