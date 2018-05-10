package com.example.mathe.gerenciadorpartes.Controller;

import android.content.Context;
import android.database.sqlite.*;


/**
 * Created by mathe on 17/12/2017.
 */

//Extends (Herança) SQLiteOpenHelper implementa métodos dessa classe.
//Dessa forma, não é necessário criar metodos para criação de banco
//Tutoriais/Perguntas de ajuda:
//  https://www.devmedia.com.br/criando-um-crud-com-android-studio-e-sqlite/32815
//  https://stackoverflow.com/questions/8147440/android-database-transaction
public class CriaBanco extends SQLiteOpenHelper{

    private final String PUBLICADORES = "publicadores";
    private final String PONTOS = "pontos";
    private final String PONTOS_PUBLICADORES = "pontos_publicadores";
    private final String PARTES_PENDENTES = "partes_pendentes";

    //Cria banco em uma pasta cujo acesso é exclusivo para o app
    public CriaBanco(Context context) {
        super(context, "dados.db", null, 31);
    }

    //Quando o arquivo é criado no construtor, o método abaixo é chamado para a criação
    //do banco
    @Override
    public void onCreate(SQLiteDatabase db) {

        //Para cada execSQL/insert/update/delete é criada uma transaction. Por criar manualmente as transactions,
        //podemos reduzir o tempo de execução, pois em vez de criar uma transação para cada linha, é criada uma
        //para todas, sendo útil para várias inserções sucessivas (i.e. insert dentro de um laço for).
        db.beginTransaction();

        try {
            db.execSQL("CREATE TABLE IF NOT EXISTS pontos( " +
                    "_id INTEGER PRIMARY KEY, " +
                    "nome VARCHAR(55), " +
                    "tipo INTEGER);");

            db.execSQL("CREATE TABLE IF NOT EXISTS publicadores( " +
                    "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "nome VARCHAR(100), " +
                    "ativo BOOL, " +
                    "sexo VARCHAR(1));");

            db.execSQL("CREATE TABLE IF NOT EXISTS pontos_publicadores( " +
                    "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "public_id INTEGER, " +
                    "comp_id INTEGER, " +
                    "ponto_id INTEGER, " +
                    "data_conc DATETIME, " +
                    "tipo_parte INTEGER, " +
                    "passou BOOLEAN, " +
                    "ponto_sugerido INTEGER, " +
                    "notificado BOOLEAN, " +
                    "FOREIGN KEY (public_id) REFERENCES publicadores(_id), " +
                    "FOREIGN KEY (comp_id) REFERENCES publicadores(_id), " +
                    "FOREIGN KEY (ponto_id) REFERENCES pontos(_id), " +
                    "FOREIGN KEY (ponto_sugerido) REFERENCES pontos(_id));");

            db.execSQL("INSERT INTO pontos VALUES " +
                    "(0, 'Substituição', 5), " +
                    "(1, 'Leitura Exata', 5), " +
                    "(2, 'Articulação Clara', 5), " +
                    "(3, 'Pronúncia Correta', 5), " +
                    "(4, 'Fluência', 5), " +
                    "(5, 'Uso correto de pausas', 5), " +
                    "(6, 'Ênfase segundo o sentido', 5), " +
                    "(7, 'Ênfase nas ideias principais', 1), " +
                    "(8, 'Volume apropriado', 5), " +
                    "(9, 'Modulação', 5), " +
                    "(10, 'Entuasiasmo', 5), " +
                    "(11, 'Cordialidade e sentimento', 5), " +
                    "(12, 'Gestos e expressões faciais', 5), " +
                    "(13, 'Contato Visual', 5), " +
                    "(14, 'Naturalidade', 5), " +
                    "(15, 'Boa aparência', 5), " +
                    "(16, 'Equilíbrio', 5), " +
                    "(17, 'Uso do microfone', 5), " +
                    "(18, 'Uso da bíblia ao responder a perguntas', 2), " +
                    "(19, 'Incentivo ao uso da bíblia', 4), " +
                    "(20, 'Introdução eficaz de textos bíblicos', 4), " +
                    "(21, 'Leitura de textos com ênfase adequada', 4), " +
                    "(22, 'Aplicação correta dos textos', 4), " +
                    "(23, 'Esclarecer o valor prático da matéria', 4), " +
                    "(24, 'Escolha de palavras', 4), " +
                    "(25, 'Uso de esboço', 4), " +
                    "(26, 'Apresentação lógica da matéria', 4), " +
                    "(27, 'Proferimento espontâneo', 4), " +
                    "(28, 'Estilo conversante', 4), " +
                    "(29, 'Qualidade da voz', 4), " +
                    "(30, 'Mostrar interesse nos outros', 2), " +
                    "(31, 'Respeito pelos outros', 4), " +
                    "(32, 'Falar com convicção', 4), " +
                    "(33, 'Falar com tato, mas de modo firme', 4), " +
                    "(34, 'Ser edificante e positivo', 4), " +
                    "(35, 'Repetição para dar ênfase', 4), " +
                    "(36, 'Desenvolvimento do tema', 4), " +
                    "(37, 'Destacar os pontos principais', 4), " +
                    "(38, 'Introdução que desperta interesse', 4), " +
                    "(39, 'Conclusão eficaz', 4), " +
                    "(40, 'Exatidão das declarações', 4), " +
                    "(41, 'Clareza', 4), " +
                    "(42, 'Apresentação instrutiva', 4), " +
                    "(43, 'Usar a matéria designada', 4), " +
                    "(44, 'Uso eficaz de perguntas', 4), " +
                    "(45, 'Ilustrações instrutivas', 4), " +
                    "(46, 'Ilustrações baseadas em situações conhecidas', 4), " +
                    "(47, 'Uso eficaz de recursos visuais', 4), " +
                    "(48, 'Argumentação que estimula o raciocínio', 4), " +
                    "(49, 'Argumentos convincentes', 4), " +
                    "(50, 'Tocar o coração', 4), " +
                    "(51, 'Controle e boa distribuição do tempo', 4), " +
                    "(52, 'Exortação eficaz', 3), " +
                    "(53, 'Encorajar e fortalecer os ouvintes', 3);");

            /*db.execSQL("INSERT INTO publicadores VALUES " +
                    "(1, 'Matheus Oliveira', 1, 'm'), " +
                    "(2, 'João Batista', 1, 'm'), " +
                    "(3, 'Mattia Penazzi', 1, 'm');");

            db.execSQL("INSERT INTO pontos_publicadores VALUES " +
                    "(1, 1, NULL, 1, '2017-12-12', 3, NULL, 2), " +
                    "(2, 2, 1, 3, '2017-12-14', 2, 1, NULL), " +
                    "(3, 1, 2, 0, '2015-08-12', 3, 0, 3), " +
                    "(4, 3, 1, 4, '2016-03-24', 1, 1, NULL), " +
                    "(5, 2, 3, 2, '2016-07-23', 2, 1, NULL);");*/
            //As alterações acima só são aceitas se definirmos a transação como bem sucedida
            db.setTransactionSuccessful();
        }
        finally {
            //Finaliza transação
            db.endTransaction();
        }
    }

    //Quando o app já estava instalado e, consequentemente, já havia um banco instalado, o
    //método abaixo é chamado. Neste caso, quando a versão do novo banco é maior, apagamos o
    //banco antigo e chamamos novamento o onCreate;
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if(db.getVersion()<newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + PONTOS_PUBLICADORES);
            db.execSQL("DROP TABLE IF EXISTS " + PARTES_PENDENTES);
            db.execSQL("DROP TABLE IF EXISTS " + PUBLICADORES);
            db.execSQL("DROP TABLE IF EXISTS " + PONTOS);
            onCreate(db);
        }
    }
}

































 /*SQLiteDatabase con;
    private String url;

    public Conexao(String caminho){
        con = null;
        url = caminho+"/GP/dados.db";
    }

    public Boolean Open(){
        try{
            if(con == null) {
                con = SQLiteDatabase.openOrCreateDatabase(url, null);
                if(con.isOpen()) {
                    return true;
                }
            }
        }
        catch(SQLiteException E){
            return false;
        }
        return false;
    }

    public Boolean Close(){
        try{
            con.close();
            if(!con.isOpen()){
                return true;
            }
        }
        catch(SQLiteException E){
            return false;
        }
        return false;
    }*/