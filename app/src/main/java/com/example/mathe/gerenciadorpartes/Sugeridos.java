package com.example.mathe.gerenciadorpartes;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ExpandableListView;
import android.widget.Spinner;

import com.example.mathe.gerenciadorpartes.Model.Publicador;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class Sugeridos extends AppCompatActivity {

    //Adapter dos componentes da tela (AutoCompleteTextView e ExpandableList)
    private AdapterExpandableSugeridos aes;
    private ArrayAdapter<Publicador> actv_pub;

    //Lista de dados
    private HashMap<Integer, ArrayList<Publicador>> sugeridos;
    private ArrayList<Publicador> publicadores;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sugeridos);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.minhatoolbar);
        Spinner selecionaOrdenacao =(Spinner) findViewById(R.id.spinner_sugeridos);
        //São "final" pois são usados em implementações de interfaces de outros componentes
        final ExpandableListView lista_sugeridos = (ExpandableListView) findViewById(R.id.lista_sugeridos);
        final AutoCompleteTextView actv = (AutoCompleteTextView) findViewById(R.id.actv_sugeridos);

        //Define a toolbar contida no layout da cativity como a Actionbar (barra de ferramenta da activity)
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //Ativa botão de voltar
        getSupportActionBar().setDisplayShowHomeEnabled(true);  //Ativa botão de voltar
        getSupportActionBar().setDisplayShowTitleEnabled(false); //Desativa o título

        //Obtem lista de publicadores enviados por meio de intent da activity TelaInicial
        Intent i = getIntent();
        publicadores = i.getParcelableArrayListExtra("publicadores");

        //Inclui no ArrayAdapter do AutoCompleteTextView.
        //Só é possivel definir um objeto em um ArrayAdaper se tiver implementado o método toString, sendo isso o que
        //irá aparecer como sugerstão.
        actv_pub = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, publicadores);
        actv.setAdapter(actv_pub);

        //Obtem do banco de dados a lista de sugeridos
        Uri u = Uri.parse("content://com.example.mathe.gerenciadorpartes.provider/dados/ObterSugeridos");
        Cursor c = getContentResolver().query(u, null, null, null, null);

        //Lista que contém os títulos dos grupos da ExpandableListView
        final ArrayList<String> titulos = new ArrayList<>();
        titulos.add("Homens");
        titulos.add("Mulheres");

        //Insere os dados no AdapterExpandableSugeridos e obtem o HashMap dos publicadores para ordenação
        aes = new AdapterExpandableSugeridos(c, titulos);
        lista_sugeridos.setAdapter(aes);
        this.sugeridos = aes.obterPublicadores();

        //Cria Evento que é disparado quando item da ExpandableList é selecionada (a escolha do publicador é feita)
        lista_sugeridos.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                //A activity Adicionar parte é chamada, passando a lista de publicadores e o publicador selecionado
                //por meio de intent
                Intent i = new Intent(Sugeridos.this, AdicionarParte.class);
                i.putExtra("publicador", sugeridos.get(groupPosition).get(childPosition));
                i.putExtra("publicadores", publicadores);
                startActivity(i);
                return true;
            }
        });

        //Evento quando item do spinner é selecionado (Selecionado a ordenação da lista)
        selecionaOrdenacao.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //Dependendo do item escolhido, os publicadores são ordenados separadamente (homens e mulheres)
                switch(position){
                    case 0:
                        Collections.sort(sugeridos.get(0), new ComparaData());
                        Collections.sort(sugeridos.get(1), new ComparaData());
                        break;
                    case 1:
                        Collections.sort(sugeridos.get(0), new ComparaDataReverso());
                        Collections.sort(sugeridos.get(1), new ComparaDataReverso());
                        break;
                    case 2:
                        Collections.sort(sugeridos.get(0), new ComparaNpartes());
                        Collections.sort(sugeridos.get(1), new ComparaNpartes());
                        break;
                    case 3:
                        Collections.sort(sugeridos.get(0), new ComparaNpartesReverso());
                        Collections.sort(sugeridos.get(1), new ComparaNpartesReverso());
                        break;
                    case 4:
                        Collections.sort(sugeridos.get(0), new ComparaNome());
                        Collections.sort(sugeridos.get(1), new ComparaNome());
                        break;
                    case 5:
                        Collections.sort(sugeridos.get(0), new ComparaNomeReverso());
                        Collections.sort(sugeridos.get(1), new ComparaNomeReverso());
                        break;
                    default:
                        throw new IllegalArgumentException("Opção de ordenação incorreta");
                }

                //Define os publicadores ordenados
                aes.setPublicadores(sugeridos);
                lista_sugeridos.setAdapter(aes);

                //Expande grupos que estavam expandidos antes de atualizar os valores, já que o setAdapter encolhe todos
                //os grupos
                ArrayList<Boolean> b = aes.getExpandidos();
                for(int i = 0; i < b.size(); i++){
                    if(b.get(i)){
                        lista_sugeridos.expandGroup(i);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //Nada a fazer neste caso
            }
        });

        //Evento quando nome sugerido no AutoCompleteTextView é selecionado
        actv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //É criado um HashMap teporário para mostrar os resultados da busca, pois se reescrevêssemos "sugeridos"
                //teríamos que ler novamente o banco em busca dos sugeridos, o que gastaria mais recursos
                HashMap<Integer, ArrayList<Publicador>> resultadoBusca = new HashMap<>();
                ArrayList<Publicador> hom = new ArrayList<>();
                ArrayList<Publicador> mul = new ArrayList<>();
                Publicador pub = (Publicador) parent.getItemAtPosition(position);   //O publicador selecionado é obtido

                for(int i = 0; i < sugeridos.size(); i++){
                    for(int j = 0; j < sugeridos.get(i).size(); j++) {
                        //Quando o nome do "pub" for igual a um dos "sugeridos", o publicador é inserido na sua
                        //respectiva lista (homem (hom) ou mulher (mul))
                        if (pub.getNome().equals(sugeridos.get(i).get(j).getNome()) && i == 0) {
                            hom.add(sugeridos.get(i).get(j));
                        }
                        else if (pub.getNome().equals(sugeridos.get(i).get(j).getNome()) && i == 1) {
                            mul.add(sugeridos.get(i).get(j));
                        }
                    }
                }

                resultadoBusca.put(0, hom);
                resultadoBusca.put(1, mul);

                aes.setPublicadores(resultadoBusca);
                lista_sugeridos.setAdapter(aes);

                //Expande os grupos que foram achados os publicadores, já que o setAdapter encolhe todos
                //os grupos
                for(int i = 0; i< resultadoBusca.size(); i++){
                    if(!resultadoBusca.get(i).isEmpty()){
                        lista_sugeridos.expandGroup(i);
                    }
                }
            }
        });

        //Evento quando o AutoCompleteTextView é modificado manualmente
        actv.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //Nada a fazer neste caso
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //Nada a fazer neste caso
            }

            @Override
            public void afterTextChanged(Editable s) {
                //Quando estiver ACTV estiver vazio, todos os sugeridos reaparecem na lista
                String nome_procurado = s.toString();
                if(nome_procurado.isEmpty()) {
                    aes.setPublicadores(sugeridos);
                    lista_sugeridos.setAdapter(aes);

                    //Expande os grupos anteriormente expandidos, já que o setAdapter() encolhe todos
                    //os grupos
                    ArrayList<Boolean> b = aes.getExpandidos();
                    for(int i = 0; i < b.size(); i++){
                        if(b.get(i)){
                            lista_sugeridos.expandGroup(i);
                        }
                    }
                }
            }
        });
    }
}

//Implementação dos comparadores para que possamos usar o Collections.sort();
//Fonte:
//  https://www.tutorialspoint.com/java/util/calendar_compareto.htm
//Quando comparamos a com b, se:
//a < b, retorna < 0
//a = b, retorna 0
//a > b, retorna > 1
class ComparaData implements Comparator<Publicador>{

    @Override
    public int compare(Publicador o1, Publicador o2) {
        //A classe Calendar já implementa o compareTo, porém temos lidar com a excessão que é disparada
        //quando um ou os dois valores são nulos, de forma que:
        //se a = null, a < b, retornando < 0
        //se b = null, a > b, retornando > 0
        //se a =b = null, a = b, retornando 0
        try {
            return o1.getData_Banco().compareTo(o2.getData_Banco());
        }catch (NullPointerException ne){
            if(o1.getData_Banco() == null){
                return(-1);
            } else if (o2.getData_Banco() == null){
                return(1);
            }else {
                return 0;
            }
        }
    }
}

//Nome nunca deverá ser null, mais excessão é tratada para que não trave ao ocorrer um erro desse
class ComparaNome implements Comparator<Publicador>{

    @Override
    public int compare(Publicador o1, Publicador o2) {
        try{
            return o1.getNome().compareTo(o2.getNome());
        }catch (NullPointerException ne){
            if(o1.getNome() == null){
                return(-1);
            } else if (o2.getNome() == null){
                return(1);
            }else {
                return 0;
            }
        }
    }
}

//Npartes nunca será null, já que é um inteiro, não havendo necessidade de lidar com excessão NullPointerException
class ComparaNpartes implements Comparator<Publicador>{

    @Override
    public int compare(Publicador o1, Publicador o2) {
        return o1.getNpartes() - o2.getNpartes();
    }
}

class ComparaDataReverso implements Comparator<Publicador>{

    @Override
    public int compare(Publicador o1, Publicador o2) {
        try{
            return o2.getData_Banco().compareTo(o1.getData_Banco());
        }catch (NullPointerException ne){
            if(o2.getData_Banco() == null){
                return(-1);
            } else if (o1.getData_Banco() == null){
                return(1);
            }else {
                return 0;
            }
        }
    }
}

//Nome nunca deverá ser null, mais excessão é tratada para que não trave ao ocorrer um erro desse
class ComparaNomeReverso implements Comparator<Publicador>{

    @Override
    public int compare(Publicador o1, Publicador o2) {
        try{
            return o2.getNome().compareTo(o1.getNome());
        }catch (NullPointerException ne){
            if(o2.getNome() == null){
                return(-1);
            } else if (o1.getNome() == null){
                return(1);
            }else {
                return 0;
            }
        }
    }
}

//Npartes nunca será null, já que é um inteiro, não havendo necessidade de lidar com excessão NullPointerException
class ComparaNpartesReverso implements Comparator<Publicador>{

    @Override
    public int compare(Publicador o1, Publicador o2) {
        return o2.getNpartes() - o1.getNpartes();
    }
}
