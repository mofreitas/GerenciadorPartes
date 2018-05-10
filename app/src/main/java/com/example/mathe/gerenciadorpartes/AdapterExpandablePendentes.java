package com.example.mathe.gerenciadorpartes;

import android.database.Cursor;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.example.mathe.gerenciadorpartes.Model.PontosPublicadores;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

/**
 * Created by mathe on 04/01/2018.
 */

//Adapter - Ponte entre os dados e os elementos da interface
public class AdapterExpandablePendentes extends BaseExpandableListAdapter {
    //O HashMap "partes" armazenará em tres chaves
    //0 - Partes Passadas (de ontem para frente) não concluídas
    //1 - Partes desta semana;
    //2 - Partes das próximas semanas
    private final HashMap<Integer, ArrayList<PontosPublicadores>> partes;
    private final ArrayList<Boolean> expandidos;

    public AdapterExpandablePendentes(Cursor dados){

        //Inicializa o HashMap conforme a seguir
        //0 - Partes Passadas (de ontem para frente) não concluídas
        //1 - Partes desta semana;
        //2 - Partes das próximas semanas
        partes = new HashMap<>();
        partes.put(0, new ArrayList<PontosPublicadores>());
        partes.put(1, new ArrayList<PontosPublicadores>());
        partes.put(2, new ArrayList<PontosPublicadores>());

        //Declaração de variáveis que serão usadas como base para separar os dados entre os grupos
        Calendar c = Calendar.getInstance();                //Obtem data atual
        int semana = c.get(Calendar.WEEK_OF_YEAR);          //Obtem semana da data atual
        int ano = c.get(Calendar.YEAR);                     //Obtem ano da data atual

        //Declaração de variáveis temporárias que serão usadas ao percorrer o cursor
        PontosPublicadores p;
        int semana_parte;
        int ano_parte;

        //Percorre o cursor
        while(!dados.isAfterLast()){
            p = new PontosPublicadores(dados.getInt(0), dados.getString(1), dados.getInt(2), dados.getString(3), dados.getString(4), dados.getInt(5), dados.getString(6), dados.getInt(7));
            semana_parte = p.getData_conc_Calendar().get(Calendar.WEEK_OF_YEAR);
            ano_parte = p.getData_conc_Calendar().get(Calendar.YEAR);

            //Se o ano da parte for menor que o ano atual (anterior),
            //então essa parte vai para o grupo das partes passadas
            if(ano_parte < ano){
                partes.get(0).add(p);
            }
            else if(ano_parte == ano) {
                //Se os anos forem iguais, o numero da semana irá definir em que
                //grupo será contido a parte
                if (semana_parte < semana) {
                    partes.get(0).add(p);
                } else if (semana_parte == semana) {
                    partes.get(1).add(p);
                } else {
                    partes.get(2).add(p);
                }
            }
            else{
                //Se o ano da parte for maior que o ano atual, então essa parte
                //é incluída no grupo das partes futuras
                partes.get(2).add(p);
            }

            dados.moveToNext();
        }
        //O cursor é fechado pelo cursor loader

        //Inicializa a lista de grupos expandidos considerando-os agrupados
        expandidos = new ArrayList<>();
        for(int i = 0; i<partes.size(); i++){
            expandidos.add(false);
        }
    }

    //################# Sobrescrição de métodos não obrigatória #####################
    //Os dois metodos abaixo atualizam o vetor que indica quais grupos estão expandidos ou não
    @Override
    public void onGroupCollapsed(int groupPosition) {
        super.onGroupCollapsed(groupPosition);
        expandidos.set(groupPosition, false);
    }

    @Override
    public void onGroupExpanded(int groupPosition) {
        super.onGroupExpanded(groupPosition);
        expandidos.set(groupPosition, true);
    }
    //##################################################################################

    public ArrayList<Boolean> getExpandidos(){
        return expandidos;
    }

    @Override
    public int getGroupCount() {
        return partes.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return partes.get(groupPosition).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return partes.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return partes.get(groupPosition).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return partes.get(groupPosition).get(childPosition).getId();
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
        if(convertView == null){
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.titulos_expansablelist, null, false);
        }

        //O título do grupo (exibido em um TextView) é definido com base no groupPosition
        TextView nome = (TextView) convertView.findViewById(R.id.titulo_sugeridos);
        switch(groupPosition) {
            case 0:
                nome.setText("Partes não finalizadas");
                break;
            case 1:
                nome.setText("Partes nesta semana");
                break;
            case 2:
                nome.setText("Partes pendentes");
                break;
            default:
                break;
        }

        //O numero de itens do grupo (exibido em um TextView) é definido com base no tamanho da arraylist na posicao groupPosition
        TextView n_itens = (TextView) convertView.findViewById(R.id.itens_grupo);
        n_itens.setText(String.valueOf(partes.get(groupPosition).size()));

        return convertView;
    }

    //Monta a view de cada item das sublistas
    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        //Se "convertView" == null, a View do item "childPosition" do grupo "groupPosition" ainda não foi criada
        //Assim sendo, é inflado a view do item. Isso é feito para que o programa possa
        //reaproveitar as view e economizar recursos
        if(convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.linha, null, false);
        }

        //Obtem todas as caixas de textos de uma linha
        TextView nome = (TextView) convertView.findViewById(R.id.textolinhap);
        TextView ajudante = (TextView) convertView.findViewById(R.id.textolinhap2);
        TextView tipo_parte = (TextView) convertView.findViewById(R.id.textolinhap3);
        TextView ponto = (TextView) convertView.findViewById(R.id.textolinhap4);
        TextView data = (TextView) convertView.findViewById(R.id.textolinhap5);
        TextView notificado = (TextView) convertView.findViewById(R.id.textolinhap6);

        //Obtem da HashMap "partes" o item indicado pelos "childPosition" e "groupPosition"
        PontosPublicadores pp = partes.get(groupPosition).get(childPosition);

        //Preenche os elementos da tela com seus respectivos valores
        nome.setText(pp.getPublic_nome());
        tipo_parte.setText(pp.getTipo_partenome());
        data.setText(pp.getData_concl());

        //Caso não haja ajudante, o componente é preenchido com nada ""
        if(pp.getComp_nome()==null){
            //Se não houver ajudante, o Textview ajudante fica invisivel (GONE) e o peso do nome fica 3
            //ajudante.setVisibility(View.GONE);
            //nome.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 3));
            ajudante.setText("");
        }
        else{
            ajudante.setText(pp.getComp_nome());
        }

        //Se o pontoId obtido for 0, então é uma substituição, conforme definido no banco de dados
        if(pp.getPonto_id()==0) {
            ponto.setText("Substituição");
        }else{
            ponto.setText("Ponto " + pp.getPonto_id());
        }

        //Se o publicador da parte foi notificado, então aparece "notificado" em cor verde na tela
        //caso contrário, aparece "não notificado" em vermelho na tela
        if(pp.getNotificado()){
            notificado.setText("Notificado");
            notificado.setTextColor(Color.parseColor("#00C716"));
        }
        else{
            notificado.setText("Não notificado");
            notificado.setTextColor(Color.parseColor("#D60010"));
        }

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
