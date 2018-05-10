package com.example.mathe.gerenciadorpartes;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import com.example.mathe.gerenciadorpartes.Controller.HistoricoLoader;
import com.example.mathe.gerenciadorpartes.Model.Publicador;

import java.util.ArrayList;

/**
 * Created by mathe on 18/12/2017.
 */

public class Historico extends Fragment implements android.support.v4.app.LoaderManager.LoaderCallbacks<Cursor> {

    private RecyclerView recyclerHistorico;
    private RecyclerAdapterHistorico adapterRecyclerHistorico;
    private ArrayAdapter<Publicador> actvAdapter;
    private View view;
    private AutoCompleteTextView actvBuscaHistorico;
    final private int BUSCA_COMPLETA = 1;
    final private int BUSCA_PUBLICADOR_ID = 2;
    final private int BUSCA_STRING_PARCIAL = 3;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.historico, container, false);

        //Inicializa variaveis, relacionando-os aos componentes do layout
        recyclerHistorico = (RecyclerView) view.findViewById(R.id.listahistorico);
        actvBuscaHistorico = (AutoCompleteTextView) view.findViewById(R.id.actv_historico);

        //Inicializa arrayadapter e o define com adapter do AutoCompleteTextView
        //Para colocar uma lista de objetos em um arrayadapter, deve implementar o toString() no objeto utilizado,
        //sendo isso que aparece nas sugestões do AutoCompleteTextView
        actvAdapter  = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, ((TelaInicial)getActivity()).publicadores);
        actvBuscaHistorico.setAdapter(actvAdapter);

        //Evento quando sugestão do AutoCompleteTextView é selecionada
        actvBuscaHistorico.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Nesse caso, não se pode usar o actvBuscaHistorico.getPosition(position) pois ele dará a posição na lista de
                //sugestões gerada, não na lista de publicadores

                //Convertendo de objeto genérico para o objeto pontopublicadores
                Publicador pub = (Publicador) parent.getItemAtPosition(position);

                //Definindo Argumentos usados no LoaderManager para obter o histórico
                //O interessante do bundle é que podemos passar vários argumentos e eles não precisam ser pré-definidos,
                //como em umobjeto de uma classe
                Bundle args = new Bundle();
                args.putInt("tipo", 3);
                args.putString("arg", String.valueOf(pub.getId()));

                //Como podemos pesquisar por vários publicadores, temos que reiniciar loader a acada pesquisa, pois se usarmos
                //initLoader, ele reutilizará a busca anterior. O restart força que ele pesquise novamente.
                getLoaderManager().restartLoader(BUSCA_PUBLICADOR_ID, args, Historico.this);
            }
        });

        //Evento quando texto do AutoCompleteTextView é modificado manualmente
        actvBuscaHistorico.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                //Se estiver vazio, pega lista completa do histórico
                if(s.toString().isEmpty()){
                    Bundle args = new Bundle();
                    args.putInt("tipo", 1);
                    args.putString("arg", "");

                    //Aqui se usa initLoader pois podemos reutilizar uma pesquisa anterior do histórico completo
                    getLoaderManager().initLoader(BUSCA_COMPLETA, args, Historico.this);
                }
            }
        });

        //Evento que dispara quando apertamos um tecla do teclado quando o AutocCompleteTexteView está em foco
        actvBuscaHistorico.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                //Verifica se a tecla pressionada foi o enter.
                if(keyCode==KeyEvent.KEYCODE_ENTER) {
                    actvBuscaHistorico.dismissDropDown();   //As sugestões desaparecem
                    Bundle args = new Bundle();
                    args.putInt("tipo", 2);
                    args.putString("arg", actvBuscaHistorico.getText().toString());

                    //Como podemos fazer esse tipo de pesquisa várias vezes, temos que reiniciar loader a a cada pesquisa,
                    //pois se usarmos initLoader, ele reutilizará a busca anterior. O restart força que ele pesquise novamente.
                    getLoaderManager().restartLoader(BUSCA_STRING_PARCIAL, args, Historico.this);
                    return true;
                }
                return false;
            }
        });

        //Inicia o loader que faza busca completa no histórico
        Bundle args = new Bundle();
        args.putInt("tipo", 1);
        args.putString("arg", "");
        getLoaderManager().initLoader(BUSCA_COMPLETA, args, this);

        //################################################################

        //Fonte:
        //  https://stackoverflow.com/questions/44916104/updating-a-fragment-from-activity-during-runtime-view-variable-not-global
        //Cria um receptor de dados enviados em Broadcast local com a Intent "atualizarPublicadores" , executando a
        //função "atualizarPublicador"
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(atualizarPublicadores,
                new IntentFilter("atualizarPublicadores"));

        //Cria um receptor de dados enviados em Broadcast local com a Intent "atualizarHistorico", executando a
        //função "atualizarHistorico"
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(atualizarHistorico,
                new IntentFilter("atualizarHistorico"));

        //################################################################
        return view;
    }

    //################################################################

    //Fonte:
    //  https://stackoverflow.com/questions/44916104/updating-a-fragment-from-activity-during-runtime-view-variable-not-global
    //Função que é executada quando é disparado um localBroadcast com a Intent registrada na criação do receptor

    private BroadcastReceiver atualizarPublicadores = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            actvAdapter  = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, ((TelaInicial)getActivity()).publicadores);
            actvBuscaHistorico.setAdapter(actvAdapter);
        }
    };

    private BroadcastReceiver atualizarHistorico = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle args = new Bundle();
            args.putInt("tipo", 1);
            args.putString("arg", "");
            getLoaderManager().restartLoader(BUSCA_COMPLETA, args, Historico.this);
        }
    };

    //################################################################

    @Override
    public android.support.v4.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {
        //A função abaixo é executada de forma assíncrona
        return new HistoricoLoader(getContext(), args.getString("arg"), args.getInt("tipo"));
    }

    @Override
    public void onLoadFinished(android.support.v4.content.Loader<Cursor> loader, Cursor data) {
        //Se o cursor for null, nenhum registro foi achado
        if(data!=null) {
            if (data.getCount() != 0) {
                //Confere se cursor não foi fechado
                if (!data.isClosed()) {
                    data.moveToFirst();

                    //Inicializa adapter do recyclerView e o define com adapter do recyclerHistorico
                    adapterRecyclerHistorico = new RecyclerAdapterHistorico(data);
                    recyclerHistorico.setAdapter(adapterRecyclerHistorico);
                    recyclerHistorico.setLayoutManager(new LinearLayoutManager(getActivity()));
                    data.close();       //fecha cursor
                } else {
                    //reinicia busca se durante o ciclo de vida da activity, o cursor foi fechado
                    Bundle args = new Bundle();
                    args.putInt("tipo", 1);
                    args.putString("arg", "");
                    getLoaderManager().restartLoader(BUSCA_COMPLETA, args, this);
                }
            }
            else {
                Toast.makeText(getContext(), "Pesquisa não teve resultados", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Cursor> loader) {
        loader.cancelLoad();
    }

    @Override
    public void onDestroy() {
        //Tem que destruir receptores ao destruir Activity
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(atualizarPublicadores);
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(atualizarHistorico);
        super.onDestroy();
    }
}

