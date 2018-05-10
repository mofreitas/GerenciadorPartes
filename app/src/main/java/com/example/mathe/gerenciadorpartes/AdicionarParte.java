package com.example.mathe.gerenciadorpartes;

import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.mathe.gerenciadorpartes.Controller.Banco;
import com.example.mathe.gerenciadorpartes.Model.Ponto;
import com.example.mathe.gerenciadorpartes.Model.PontosPublicadores;
import com.example.mathe.gerenciadorpartes.Model.Publicador;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class AdicionarParte extends AppCompatActivity {

    //Listas de dados
    private ArrayList<Publicador> publicadores;
    private ArrayList<Ponto> pontos;
    private ArrayAdapter<Publicador> adapteractv;

    //Componentes
    private View viewactivity;
    private TextView publicad;
    private AutoCompleteTextView ajudante;
    private EditText data_parte;
    private EditText ponto;
    private Spinner tipo_parte;
    private TextView nome_ponto;

    //Variáveis temporárias
    Publicador publicador_selecionado;
    private int comp_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adicionar_parte);

        getSupportActionBar().setTitle("Adicionar Parte");          //Define o título da barra de menu

        //Obtendo informações provenientes da Activity Sugeridos por meio de intent
        Intent recebe = getIntent();
        publicadores = recebe.getParcelableArrayListExtra("publicadores");
        publicador_selecionado = recebe.getParcelableExtra("publicador");

        //Inicialização dos componentes
        viewactivity = findViewById(R.id.adcParte);
        publicad = (TextView) findViewById(R.id.tv_adcparte);
        ajudante = (AutoCompleteTextView) findViewById(R.id.actv2_adcparte);
        ponto = (EditText) findViewById(R.id.ponto_adc);
        nome_ponto = (TextView) findViewById(R.id.nomeponto);
        tipo_parte = (Spinner) findViewById(R.id.spinner);

        //Exibe o nome do publicador escolhido em um TextView
        publicad.setText(publicador_selecionado.getNome());

        //Inicializa o AutoCompleteTextView ajudante, define o seu Array adapter, configura o
        //onItemClickListener e textChangedListener
        adapteractv = new ArrayAdapter<Publicador>(this, android.R.layout.simple_list_item_1, publicadores);
        ajudante.setAdapter(adapteractv);
        ajudante.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Publicador p = (Publicador) parent.getItemAtPosition(position);
                comp_id = p.getId();
            }
        });
        ajudante.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                comp_id = 0;
            }
        });

        //Obtendo Pontos do banco de dados
        Banco b = new Banco(this);
        pontos = b.obterPontos();

        //Declara o edittext que recebe o numero do ponto e o textview que mostra o numero do ponto

        //o evento Onitemclick não funciona com spinner
        tipo_parte.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String parte = tipo_parte.getItemAtPosition(position).toString();
                //https://stackoverflow.com/questions/4297763/disabling-of-edittext-in-android/34367434
                if((parte.equals("Discurso") || parte.equals("Leitura"))){
                    ajudante.setEnabled(false);
                }
                else{
                    ajudante.setEnabled(true);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //Nada a ser feito
            }
        });

        //Lança evento quando o texto do edittext ponto muda
        ponto.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //Nada a ser feito
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //Nada a ser feito
            }

            @Override
            public void afterTextChanged(Editable s) {
                //Se a caixa de texto NÂO estiver vazia, procura o respectivo nome do ponto e exibe no textview
                //Se não tiver nada em ponto, a textview nome_ponto mostra nada
                if(!s.toString().equals("")) {
                    //Se o ponto colocado for 0 ou não existir, o textview ponto exibe "Ponto inexistente"
                    if (Integer.parseInt(s.toString()) != 0) {
                        try {
                            nome_ponto.setText(pontos.get(Integer.parseInt(s.toString())).getNome());
                        } catch (IndexOutOfBoundsException E) {
                            nome_ponto.setText("Ponto inexistente");
                        }
                    }else {
                        nome_ponto.setText("Ponto inexistente");
                    }
                }
                else {
                    nome_ponto.setText("");
                }
            }
        });

        //Preenche o campo que contem o nome do ponto e o ponto se p.getsugerido for diferente de 0 (null)
        if(publicador_selecionado.getSugerido() != 0) {
            //Define o ponto baseado no getsugerido obtido da activity anterior
            //Após isso, o evento de TextChangeListener é disparado, modificando o texto do textView ponto conforme vemos acima
            ponto.setText(String.valueOf(publicador_selecionado.getSugerido()));
        }

        //Declara edittext data, desativa a sua edição por parte do usuário
        data_parte = (EditText) findViewById(R.id.data_adc);
        data_parte.setKeyListener(null);
        //Esse eveto dispara tanto quando foca como quando desfoca
        data_parte.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                //Se o evento foi numa "focalização"
                if(hasFocus){
                    data_parte.setText(obtemData(data_parte));
                }
            }
        });

        //Evento que dispara quando o objeto é "clicado"
        data_parte.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                data_parte.setText(obtemData(data_parte));
            }
        });

        ArrayAdapter<Publicador> publicadorArrayAdapter = new ArrayAdapter<Publicador>(this, android.R.layout.simple_list_item_1, publicadores);
        ajudante.setAdapter(publicadorArrayAdapter);
    }

    //Método que lança o diálogo de obter datas e retorna uma string com a data escolhida
    public String obtemData(final EditText data){
        //Inicia objeto Calendário com data atual e Inicia o Datepickerdialog
        final Calendar dataatual = Calendar.getInstance();
        DatePickerDialog escolhedata = new DatePickerDialog(AdicionarParte.this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                //Cria um formato de data e inicia o calendario "temp" com o ano, mes e dia obtidos do datepicker
                SimpleDateFormat formatodata = new SimpleDateFormat("dd/MM/yyyy");
                Calendar temp = Calendar.getInstance();
                temp.set(year, month, dayOfMonth);

                //Define a data no Edittext covertida
                data.setText(formatodata.format(temp.getTime()));
            }
        }, dataatual.get(Calendar.YEAR), dataatual.get(Calendar.MONTH), dataatual.get(Calendar.DAY_OF_MONTH));

        escolhedata.show();
        return data.getText().toString();
    }

    //Cria o menu a partir do layout personalizado contido em res/layout/menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_adicionar_publicador, menu);
        return true;
    }

    //Para definir o que faremos quando algum botão do menu for "clicado"
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==R.id.adc){

            //São obtidas duas datas: Uma para a data de hoje e outra para a data da parte
            //Para verificar a data informada já passou nos if abaixo
            Calendar hoje = Calendar.getInstance();
            Calendar c = Calendar.getInstance();

            try {
                SimpleDateFormat formatar = new SimpleDateFormat("dd/MM/yyyy");
                c.setTime(formatar.parse(data_parte.getText().toString()));
                hoje.setTime(formatar.parse(hoje.get(Calendar.DAY_OF_MONTH)+"/"+(hoje.get(Calendar.MONTH)+1)+"/"+hoje.get(Calendar.YEAR)));
            }
            catch(ParseException pe){
                Snackbar.make(viewactivity, "Defina uma data", Snackbar.LENGTH_LONG).show();
                return super.onOptionsItemSelected(item);
            }

            if(publicador_selecionado.getNome().equals(ajudante.getText().toString())){
                Snackbar.make(viewactivity, "O publicador e o ajudante não podem ser iguais", Snackbar.LENGTH_LONG).show();
            }
            else  if(ajudante.isEnabled() && comp_id == 0) {
                Snackbar.make(viewactivity, "Escolha um ajudante válido", Snackbar.LENGTH_LONG).show();
                ajudante.setText("");
            }
            else if(nome_ponto.getText().equals("Ponto inexistente") || nome_ponto.getText().toString().isEmpty()){
                Snackbar.make(viewactivity, "Defina um ponto existente", Snackbar.LENGTH_LONG).show();
            }
            else if(c.compareTo(hoje)<=-1){
                Snackbar.make(viewactivity, "Data já passou", Snackbar.LENGTH_LONG).show();
            }
            else if(ponto.getText().toString().isEmpty() || data_parte.getText().toString().isEmpty()){
                Snackbar.make(viewactivity, "Preencha os campos obrigatórios", Snackbar.LENGTH_LONG).show();
            }
            else {
                Intent i = new Intent(this, TelaInicial.class);
                adicionarParte();
                startActivity(i);
            }
            return super.onOptionsItemSelected(item);
        }
        else {
            return super.onOptionsItemSelected(item);
        }
    }

    public void adicionarParte(){
        //Se for uma leitura ou discurso, comp.isEnabled = false, colocando null no banco no campo comp_id
        PontosPublicadores p;
        if(ajudante.isEnabled()) {
            p = new PontosPublicadores(0, publicador_selecionado.getId(), Integer.parseInt(ponto.getText().toString()), comp_id, null, tipo_parte.getSelectedItemPosition());
        }
        else{
            p = new PontosPublicadores(0, publicador_selecionado.getId(), Integer.parseInt(ponto.getText().toString()), 0, null, tipo_parte.getSelectedItemPosition());
        }
        p.setData_conc(data_parte.getText().toString());
        Uri u = Uri.parse("content://com.example.mathe.gerenciadorpartes.provider/dados/InserirParte");
        getContentResolver().insert(u, contentValues_Parte(p));
    }

    //Insere uma parte em um ContenValues
    public ContentValues contentValues_Parte(PontosPublicadores p){
        ContentValues valores = new ContentValues();
        valores.put("public_id", p.getPublic_id());
        //Se comp_id for 0, ou seja, não tiver sido adicionado um ajudante, no banco essa coluna assumira um valor nulo
        if(p.getComp_id()!=0) {
            valores.put("comp_id", p.getComp_id());
        }
        valores.put("ponto_id", p.getPonto_id());
        valores.put("data_conc", p.getData_conc_banco());
        valores.put("tipo_parte", p.getTipo_parte());
        valores.put("ponto_id", p.getPonto_id());
        valores.put("notificado", p.getNotificado());
        return valores;
    }
}
