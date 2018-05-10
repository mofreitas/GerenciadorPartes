package com.example.mathe.gerenciadorpartes;


import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mathe.gerenciadorpartes.Model.PontosPublicadores;
import com.example.mathe.gerenciadorpartes.Model.Publicador;

import java.util.ArrayList;

/**
 * Created by mathe on 18/12/2017.
 */

//http://www.theappguruz.com/blog/use-android-cursorloader-example
//https://stackoverflow.com/questions/15517920/how-do-cursorloader-automatically-updates-the-view-even-if-the-app-is-inactive
public class Pendentes extends Fragment implements android.support.v4.app.LoaderManager.LoaderCallbacks<Cursor> {

    private ArrayList<PontosPublicadores> pendentes;
    private LinearLayoutCompat ultimoaberto;

    private ExpandableListView ela;
    private AdapterExpandablePendentes adapterExpList;
    //private SimpleCursorTreeAdapter adapterExpList;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.pendente, container, false);

        //Inicializa os componentes, buscando-os na view inflada do layout "pendente"
        FloatingActionButton fab = (FloatingActionButton) v.findViewById(R.id.fab);
        ela = (ExpandableListView) v.findViewById(R.id.lista_pendentes);

        //Evento quando FloatingActionButton é pressionado
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getContext(), Sugeridos.class);
                i.putExtra("publicadores", ((TelaInicial) getActivity()).publicadores);
                startActivity(i);
            }
        });

        //Evento quando um item do ExpandableListView é selecionado
        ela.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, final int groupPosition, final int childPosition, long id) {

                //Inicializa caixa de diálogo
                AlertDialog.Builder caixa_dialogo = new AlertDialog.Builder(getActivity());
                caixa_dialogo.setTitle("Escolha uma opção:")
                        .setItems(new String[]{"Notificar", "Concluir", "Substituir", "Remover"}, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                final PontosPublicadores p = (PontosPublicadores) adapterExpList.getChild(groupPosition, childPosition);
                                switch (which){
                                    case 0:
                                        notificarWhats(p);
                                        break;
                                    case 1:
                                        concluirDialog(p);
                                        break;
                                    case 2:
                                        boolean temAjudante = p.getComp_nome() == null ? false : true;
                                        substituirDialog(p.getId(), temAjudante);
                                        break;
                                    case 3:
                                        AlertDialog.Builder caixa_dialogo_confirma_remover = new AlertDialog.Builder(getContext());
                                        caixa_dialogo_confirma_remover.setMessage("Deseja excluir parte?").setPositiveButton("Excluir", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                removerParte(p.getId());
                                            }
                                        }).setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                            }
                                        }).show();
                                        break;
                                    default:
                                        throw new IllegalArgumentException("Opcão não existente");
                                }
                            }
                        });
                caixa_dialogo.show();
                return true;
            }
        });


        getLoaderManager().restartLoader(1, null, this);

        LocalBroadcastManager.getInstance(getContext()).registerReceiver(atualizarPendentes,
                new IntentFilter("atualizarPendentes"));

        return v;
    }

    public void concluirDialog(final PontosPublicadores parte){
        //O ponto sugerido da próxima parte pode ser o ponto avaliado da parte atual
        parte.setPonto_sug(parte.getPonto_id());

        //Se for uma substituição (ponto_avaliado == 0), não há o botão que indica "não passou" e nem podem ser sugeridos pontos
        if(parte.getPonto_id()!= 0) {
            View v = getLayoutInflater().inflate(R.layout.concluir_parte, null, false);

            //Declara o EditText que recebe o ponto sugerido e o TextView que contem o nome do ponto sugerido
            final TextView tv_nome_ponto_sug = (TextView) v.findViewById(R.id.cpText);
            final EditText et_ponto = (EditText) v.findViewById(R.id.cpEdit);

            //Define evento que inicia quando texto do EditText "et_ponto" é modificado
            et_ponto.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    //Faz nada
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    //Faz nada
                }

                @Override
                public void afterTextChanged(Editable s) {
                    try {
                        int ponto_aval = Integer.valueOf(s.toString());
                        if (ponto_aval != 0) {
                            tv_nome_ponto_sug.setText(((TelaInicial) getActivity()).pontos.get(ponto_aval).getNome());
                            parte.setPonto_sug(ponto_aval);
                        } else {
                            tv_nome_ponto_sug.setText("Ponto Inexistente");
                            parte.setPonto_sug(0);
                        }
                    } catch (NumberFormatException pe) {
                        //Quando o edittext está vazio há uma falha de oonversão, disparando essa excessão
                        tv_nome_ponto_sug.setText("Sem ponto sugerido");
                        parte.setPonto_sug(0);
                    } catch (IndexOutOfBoundsException pe) {
                        //Quando o valor do edittext é um index fora dos limites da lista, essa excessão é disparada
                        tv_nome_ponto_sug.setText("Ponto Inexistente");
                        parte.setPonto_sug(0);
                    }
                }
            });
            et_ponto.setText(String.valueOf(parte.getPonto_sug()));

            //Declara o dialogo de alerta
            AlertDialog.Builder ad = new AlertDialog.Builder(getContext());
            ad.setTitle("Concluir Parte");
            ad.setView(v);
            ad.setPositiveButton("Passou", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String nome_ponto = tv_nome_ponto_sug.getText().toString();
                    if(!nome_ponto.equals("Ponto Inexistente")) {
                        concluirParte(parte.getId(), parte.getPonto_sug(), true);
                    }
                    else{
                        Toast.makeText(getContext(), "Erro na conclusão", Toast.LENGTH_LONG).show();
                    }
                }
            }).setNeutralButton("Cancelar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            }).setNegativeButton("Não passou", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String nome_ponto = tv_nome_ponto_sug.getText().toString();
                    if(!nome_ponto.equals("Ponto Inexistente")) {
                        concluirParte(parte.getId(), parte.getPonto_sug(), false);
                    }
                    else{
                        Toast.makeText(getContext(), "Erro na conclusão", Toast.LENGTH_LONG).show();
                    }
                }
            });

            ad.show();
        }
        //Caso seja uma substituição, apenas necessita da confimação se foi concluida ou não
        else{
            AlertDialog.Builder ad = new AlertDialog.Builder(getContext());
            ad.setMessage("Deseja concluir esta parte?");
            ad.setPositiveButton("Concluir", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    concluirParte(parte.getId(), 0, true);
                }
            }).setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            ad.show();
        }
    }

    public void notificarWhats(PontosPublicadores pp){
        try {
            Intent wppIntent = new Intent(Intent.ACTION_SEND);
            wppIntent.setType("text/plain");

            String ajudante = pp.getComp_nome();
            if(ajudante==null){
                ajudante = "";
            } else{
                ajudante = "Ajudante: " + pp.getComp_nome() + "\n";
            }

            String ponto;
            if(pp.getPonto_id()==0){
                ponto = "Substituição \n";
            }
            else{
                ponto = "Ponto: " + pp.getPonto_id() + " (" + pp.getPonto_nome() + ") \n";
            }

            String text = "Designação da escola do ministério: \n \n" +
                    "Nome: " + pp.getPublic_nome() + "\n" +
                    ajudante +
                    ponto +
                    "Designação: " + pp.getTipo_partenome() + " \n" +
                    "Data de designação: " + pp.getData_concl() + "\n \n" +
                    "Por favor, confirme o recebimento";

            wppIntent.setPackage("com.whatsapp");

            wppIntent.putExtra(Intent.EXTRA_TEXT, text);
            startActivity(Intent.createChooser(wppIntent, "Mandar para:"));
            new NotificarParte().execute(pp.getId());

        } catch (Exception e) {
            Toast.makeText(getContext(), "WhatsApp não instalado", Toast.LENGTH_SHORT).show();
        }
    }

    public void substituirDialog(final int p, final boolean temAjudante){
        AlertDialog.Builder ad = new AlertDialog.Builder(getContext());
        View v = getLayoutInflater().inflate(R.layout.substituir_parte, null, false);

        ad.setView(v);
        ad.setTitle("Escolha substitutos");

        final Bundle args = new Bundle();

        ArrayAdapter aa = new ArrayAdapter(getContext(), android.R.layout.simple_list_item_1, ((TelaInicial)getActivity()).publicadores);

        //Definindo AutoCompleteText que contem os publicadores
        AutoCompleteTextView actv1 = (AutoCompleteTextView) v.findViewById(R.id.actvsubs1);
        actv1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Publicador p = (Publicador) parent.getItemAtPosition(position);
                args.putInt("public_id", p.getId());
            }
        });
        actv1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                args.remove("public_id");
            }
        });
        actv1.setAdapter(aa);

        //Definindo AutoCompleteText que contem os ajudantes
        AutoCompleteTextView actv2 = (AutoCompleteTextView) v.findViewById(R.id.actvsubs2);
        actv2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Publicador p = (Publicador) parent.getItemAtPosition(position);
                args.putInt("comp_id", p.getId());
            }
        });
        actv1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                args.remove("comp_id");
            }
        });
        actv2.setAdapter(aa);

        if(!temAjudante){
            actv2.setVisibility(View.GONE);
        }

        ad.setPositiveButton("Substituir", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(args.getInt("public_id")==0) {
                    Toast.makeText(getContext(), "Substituição não efetuada: publicador inválido", Toast.LENGTH_LONG).show();
                }
                else if(temAjudante && args.getInt("comp_id")==0){
                    Toast.makeText(getContext(), "Substituição não efetuada: Essa parte tem que ter ajudante", Toast.LENGTH_LONG).show();
                }
                else if(args.getInt("comp_id")==args.getInt("public_id")){
                    Toast.makeText(getContext(), "Substituição não efetuada: nomes iguais", Toast.LENGTH_LONG).show();
                }
                else{
                    substituirParte(p, args);
                }
            }
        }).setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        ad.show();

    }

    public void removerParte(int id){
        Uri u = Uri.parse("content://com.example.mathe.gerenciadorpartes.provider/dados/RemoverParte/"+String.valueOf(id));
        getActivity().getContentResolver().delete(u, null, null);
        getLoaderManager().restartLoader(1, null, this);
    }

    public void concluirParte(int id, int ponto_sugerido, Boolean resultado){
        Intent intent = new Intent("atualizarHistorico");
        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);

        //Inicia o loader passando como argumento o id do parte e se foi aprovado ou não;
        Bundle args = new Bundle();
        args.putBoolean("passou", resultado);
        args.putInt("id", id);
        args.putInt("ponto_sug", ponto_sugerido);
        ConcluirPendentes concluirpendentes = new ConcluirPendentes(getActivity().getContentResolver(), args);
        concluirpendentes.execute();
    }

    public void substituirParte(int id, Bundle args){
        args.putInt("_id", id);
        SubstituirParte substituirparte = new SubstituirParte(getActivity().getContentResolver(), args);
        substituirparte.execute();
        Toast.makeText(getContext(), "Substituição efetuada", Toast.LENGTH_LONG).show();
    }


    @Override
    public android.support.v4.content.Loader onCreateLoader(int id, Bundle args) {
        if(id == 1) {
            //Obtem a lista de pendentes
            Uri uri = Uri.parse("content://com.example.mathe.gerenciadorpartes.provider/dados/ObterPendentes");
            android.support.v4.content.CursorLoader cl = new android.support.v4.content.CursorLoader(getContext(), uri, null, null, null, null);
            return cl;
        }
        else{
            throw new IllegalArgumentException("Operação impossível");
        }
    }

    @Override
    public void onLoadFinished(android.support.v4.content.Loader loader, Cursor data) {
        if(adapterExpList==null) {
            adapterExpList = new AdapterExpandablePendentes(data);
            ela.setAdapter(adapterExpList);
        }
        else {
            ArrayList<Boolean> expandidos = adapterExpList.getExpandidos();
            adapterExpList = new AdapterExpandablePendentes(data);
            ela.setAdapter(adapterExpList);
            for (int i = 0; i < expandidos.size(); i++) {
                if (expandidos.get(i)) {
                    ela.expandGroup(i);
                }
            }
        }

    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader loader) {
        loader.cancelLoad();
    }

    @Override
    public void onStart() {
        getLoaderManager().initLoader(1, null, this);
        super.onStart();
    }

    private BroadcastReceiver atualizarPendentes = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Uri uri = Uri.parse("content://com.example.mathe.gerenciadorpartes.provider/dados/RecarregarBanco");
            getActivity().getContentResolver().delete(uri, null, null);
            getLoaderManager().restartLoader(1, null, Pendentes.this);
        }
    };

    @Override
    public void onDestroy() {
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(atualizarPendentes);
        super.onDestroy();
    }

    class NotificarParte extends AsyncTask<Integer, Void, Void>{

        @Override
        protected Void doInBackground(Integer... integers) {
            Uri uri = Uri.parse("content://com.example.mathe.gerenciadorpartes.provider/dados/Notificar");
            getActivity().getContentResolver().update(uri, new ContentValues(), null, new String[]{String.valueOf(integers[0])});
            return null;
        }
    }
}

class ConcluirPendentes extends AsyncTask<Void, Void, Void>{

    private ContentResolver contentResolver;
    private Bundle args;

    public ConcluirPendentes(ContentResolver contentResolver, Bundle args) {
        this.contentResolver = contentResolver;
        this.args = args;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        //Conclui pendentes
        Uri uri = Uri.parse("content://com.example.mathe.gerenciadorpartes.provider/dados/ConcluirParte");
        ContentValues cv = new ContentValues();
        cv.put("passou", args.getBoolean("passou"));
        cv.put("ponto_sugerido", args.getInt("ponto_sug"));
        contentResolver.update(uri, cv, null, new String[]{String.valueOf(args.getInt("id"))});
        return null;
    }
}

class SubstituirParte extends AsyncTask<Void, Void, Void>{

    private ContentResolver contentResolver;
    private Bundle args;

    public SubstituirParte(ContentResolver contentResolver, Bundle args) {
        this.contentResolver = contentResolver;
        this.args = args;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        Uri uri = Uri.parse("content://com.example.mathe.gerenciadorpartes.provider/dados/SubstituirParte");
        ContentValues valores = new ContentValues();
        valores.put("comp_id", args.getInt("comp_id"));
        valores.put("public_id", args.getInt("public_id"));
        valores.put("ponto_id", 0);
        valores.put("notificado", 0);
        valores.putNull("ponto_sugerido");
        contentResolver.update(uri, valores, null, new String[]{String.valueOf(args.getInt("_id"))});
        return null;
    }
}