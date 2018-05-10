package com.example.mathe.gerenciadorpartes.Controller;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import java.util.ArrayList;

import com.example.mathe.gerenciadorpartes.Model.Ponto;
import com.example.mathe.gerenciadorpartes.Model.Publicador;

/**
 * Created by mathe on 17/12/2017.
 */

public class Banco{

    private SQLiteDatabase db;
    private CriaBanco banco;

    public Banco(Context contexto) {
        banco = new CriaBanco(contexto);
    }


    public Cursor partesPendentes(){

        Cursor dados = null;
        db=banco.getReadableDatabase();

        try {
            //A rawquery é usado quando queremos uma queery personalizada (inner joins, etc)
            //A consulta abaixo obtem:
            //      pontos_publicadores.id, nome do publicador, ponto, nome do ajudante, data de conclusão, tipo da parte (leitura, ...), nome do ponto e se ele foi notificado
            //se pontos_publicadores.passou for nulo, o que indica que a parte ainda não foi feita, ordenando em ordem decrescente
            //pela data de conclusão da parte
            //Para fazer dois inner join com a mesma tabela, precisamos dar nomes diferentes para cada inner join
            dados = db.rawQuery("SELECT ppub._id, public.nome as publicador, ppub.ponto_id, ajuda.nome as ajudante, ppub.data_conc, ppub.tipo_parte, pontos.nome, ppub.notificado " +
                    "from pontos_publicadores as ppub " +
                    "left join publicadores as public on " +
                    "public._id = ppub.public_id " +
                    "left join publicadores as ajuda on " +
                    "ajuda._id = ppub.comp_id " +
                    "inner join pontos on " +
                    "pontos._id = ppub.ponto_id " +
                    "where ppub.passou is null " +
                    "ORDER BY ppub.data_conc DESC", null);
            if (dados != null) {
                dados.moveToFirst();
            }
        }
        finally {
            return dados;
            //dados.close() db.close() não são chamados pois o cursor apenas está disponível enquanto o banco de dados
            //e o cursor estão abertos
        }
    }

    public Cursor obterPublicadores(){
        Cursor consulta = null;

        try{
            db = banco.getReadableDatabase();
            //Consulta na tabela publicadores os que forem ativos (ativo = 1)
            //Para cada variável do selection, colocamos "?". Eles serão subtituidos em ordem pelos
            //valores contidos no vetor "selectionArgs"
            consulta = db.query("publicadores", null, "ativo = ?", new String[] {"1"}, null, null, null);
            if(consulta != null){
                //Tem que ser chamado para mover o ponteiro para a primeira linha da consulta
                consulta.moveToFirst();
            }
        }
        finally {
            return consulta;
        }
    }

    public Cursor obterSugeridos(){
        Cursor consulta = null;
        db = banco.getReadableDatabase();
        try {
            //https://stackoverflow.com/questions/19432913/select-info-from-table-where-row-has-max-date
            //Nesta consulta, fazemos uso de Derived table (inner join com uma tabela personalizada) e subquery (query feita dentro do select).
            //Nesta consulta, queremos obter a última parte feita pelo publicador, bem como suas informações.
            //      publicador.id, o nome do publicador, o número de partes (incluindo as que ele fará), a maior data de conclusão, o sexo e
            //      o ponto sugerido em sua ultima parte
            //Para isso, a primeira Derived table "pub_info" obtem da tabela pontos_publicadores os dados abaixo agrupados pelo public.id:
            //      public_id, data de conclusão máxima e a contagem de partes
            //Já em psug, obtemos os pontos sugeridos de cada ponto_publicador
            //Por fim, com a tabela pub tem prioridade, para cada publicador, são buscadas as informações nas derived tables se o
            //publicador for ativo.
            consulta = db.rawQuery("select pub._id, pub.nome, pub_info.npartes, pub_info.datamax, pub.sexo, psug.ponto_sugerido " +
                    "                    from publicadores as pub " +
                    "                    left join (select ppub.public_id, max(ppub.data_conc) as datamax, count(ppub.public_id) as npartes " +
                    "                               from pontos_publicadores as ppub " +
                    "                               where ppub.ponto_id != 0 " +
                    "                               group by ppub.public_id) as pub_info on " +
                    "                               pub._id = pub_info.public_id " +
                    "                    left join (select ppub._id, ppub.ponto_sugerido " +
                    "                               from pontos_publicadores as ppub) as psug on " +
                    "                               psug._id = pub._id " +
                    "                    where pub.ativo = 1 " +
                    "                    order by pub_info.datamax asc", null);
            if (consulta != null) {
                consulta.moveToFirst();
            }
        }
        finally {
            return consulta;
        }
    }

    public void InserirPublicador(ContentValues valores){
        try {
            db = banco.getWritableDatabase();
            db.insert("publicadores", null, valores);
        }
        finally {
            if(db.isOpen()) {
                db.close();
            }
        }
    }

    public void InserirPartes(ContentValues valores){
        try {
            db = banco.getWritableDatabase();
            db.insert("pontos_publicadores", null, valores);
        }
        finally {
            if(db.isOpen()) {
                db.close();
            }
        }
    }

    public Cursor obterHistorico(String arg, int tipo){
        Cursor consulta = null;
        try {
            db = banco.getReadableDatabase();
            //Nas consultas abaixo, obtemos:
            //      id do publicador, nome do publicador, nome do ajudante, ponto, data de conclusão da parte,
            //      tipo da parte, se passou ou não e o ponto sugerido para a próxima parte.
            //quando passou != null (indicando que a parte ainda não foi finalizada), divergindo, em cada caso,
            //na seleção dos dados, de forma que:
            switch(tipo) {
                case 2: //Consulta data ou nome publicador
                    //Neste caso, quando o nome do publicador ou a data de conclusão contem "arg"
                    consulta = db.rawQuery("SELECT public._id, public.nome, comp.nome, ppub.ponto_id, ppub.data_conc, ppub.tipo_parte, ppub.passou, ppub.ponto_sugerido " +
                            "from pontos_publicadores as ppub " +
                            "left join publicadores as public on " +
                            "public._id = ppub.public_id " +
                            "left join publicadores as comp on " +
                            "comp._id = ppub.comp_id " +
                            "WHERE (public.nome like ? or ppub.data_conc like ?) and (ppub.passou is not null) " +
                            "ORDER BY ppub.data_conc DESC", new String[]{"%"+arg+"%", "%"+arg+"%"});
                    break;
                case 3: //Consulta publicador id
                    //Neste caso, quando o id do publicador = arg
                    consulta = db.rawQuery("SELECT public._id, public.nome, comp.nome, ppub.ponto_id, ppub.data_conc, ppub.tipo_parte, ppub.passou, ppub.ponto_sugerido " +
                            "from pontos_publicadores as ppub " +
                            "left join publicadores as public on " +
                            "public._id = ppub.public_id " +
                            "left join publicadores as comp on " +
                            "comp._id = ppub.comp_id " +
                            "WHERE ppub.public_id = ? and ppub.passou is not null " +
                            "ORDER BY ppub.data_conc DESC", new String[]{arg});
                    break;
                default: //Consulta padrão
                    //Neste caso, todos os registros são retornados
                    consulta = db.rawQuery("SELECT public._id, public.nome, comp.nome, ppub.ponto_id, ppub.data_conc, ppub.tipo_parte, ppub.passou, ppub.ponto_sugerido " +
                            "from pontos_publicadores as ppub " +
                            "left join publicadores as public on " +
                            "public._id = ppub.public_id " +
                            "left join publicadores as comp on " +
                            "comp._id = ppub.comp_id " +
                            "where ppub.passou IS NOT NULL " +
                            "ORDER BY ppub.data_conc DESC", null);
                    break;
            }
            if(consulta != null){
                consulta.moveToFirst();
            }
        }
        finally {
            return consulta;
            //dados.close() db.close() não são chamados pois o cursor apenas está disponível enquanto o banco de dados
            //e o cursor estão abertos
        }
    }

    public void closeBanco(){
        db.close();
    }

    public ArrayList<Ponto> obterPontos(){
        Cursor consulta = null;
        ArrayList<Ponto> pontos = new ArrayList<>();
        try{
            db = banco.getReadableDatabase();
            consulta = db.query("pontos", null, null, null, null, null, null);
            consulta.moveToFirst();
            //O método [nome do cursor].get[Int, String, ...](x) obtém aquela variável do tipo especificado na linha em que
            //o cursor está apontando na coluna de índice x, começando do 0
            while(!consulta.isAfterLast()){
                pontos.add(new Ponto(consulta.getInt(0), consulta.getString(1), consulta.getInt(2)));
                //move o ponteiro para a próxima linha
                consulta.moveToNext();
            }
            consulta.close();;
        }
        finally {
            if(db.isOpen()){
                db.close();
            }
            return pontos;
        }
    }

    public void removerParte(int id){
        try {
            db = banco.getWritableDatabase();
            db.delete("pontos_publicadores", "_id = ?", new String[]{String.valueOf(id)});
        }
        finally {
            if(db.isOpen()){
                db.close();
            }
        }
    }

    //todo verificar se "pontosugerido" retorna um int/verificar o que é colocado no contentvalues no metodo concluir parte (testar por debug)
    public void concluirParte(ContentValues valores, String id){
        try {
            //Se ponto sugerido for 0, consideramos que esse valor não foi informado pelo usuário, portanto ele é removido
            //do contentvalues "valores" para que no banco assuma o valor NULL
            if(valores.get("ponto_sugerido").equals(0)) {
                valores.remove("ponto_sugerido");
            }

            db = banco.getWritableDatabase();
            db.update("pontos_publicadores", valores, "_id = ?", new String[]{id});
        }
        finally {
            if(db.isOpen()){
                db.close();
            }
        }
    }

    public void substituirParte(ContentValues valores, String id){
        try {
            db = banco.getWritableDatabase();
            db.update("pontos_publicadores", valores, "_id = ?", new String[]{id});
        }
        finally {
            if(db.isOpen()){
                db.close();
            }
        }
    }

    //Desativamos um publicador por tornar ativo = 0
    //Além disso, deletamos as partes pendentes do publicador a ser desativado (passou = null)
    public void desativarPublicador(String arg){
        ContentValues valores = new ContentValues();
        valores.put("ativo", 0);

        try{
            db = banco.getWritableDatabase();
            db.update("publicadores", valores, "_id = ?", new String[]{arg});
            db.delete("pontos_publicadores", "public_id = ? and passou is null", new String[]{arg});
        }
        finally {
            if(db.isOpen()){
                db.close();
            }
        }
    }

    public void setNotificado(String arg){
        ContentValues valores = new ContentValues();
        valores.put("notificado", 1);

        try{
            db = banco.getWritableDatabase();
            db.update("pontos_publicadores", valores, "_id = ?", new String[]{arg});
        }
        finally {
            if(db.isOpen()){
                db.close();
            }
        }
    }
}