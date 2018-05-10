package com.example.mathe.gerenciadorpartes.Controller;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by mathe on 11/01/2018.
 */

//Tutorial
//  https://developer.android.com/guide/topics/providers/content-providers.html?hl=pt-br
//  https://www.youtube.com/watch?v=4w63OdDvbIw
//Necessário para implementar o Cursorloader, que automaticamente se atualiza ao inserir/remover/atualizar dados
public class Provider extends ContentProvider {

    //AUTORIDADE = nomedopacote + ".provider"
    public static final String AUTORIDADE = "com.example.mathe.gerenciadorpartes.provider";
    //CONTENT_URI = content://AUTORIDADE/nome do banco
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTORIDADE + "/dados");
    //Números relativos a cada operação realizada pelo provider
    public static final int INSERIR_PUBLICADOR = 1;
    public static final int INSERIR_PARTE = 2;
    public static final int OBTER_PONTOS = 3;
    public static final int OBTER_SUGERIDOS = 4;
    public static final int OBTER_PENDENTES = 5;
    public static final int REMOVER_PARTE = 6;
    public static final int CONCLUIR_PARTE = 7;
    public static final int SUBSTITUIR_PARTE = 8;
    public static final int REMOVER_PUBLICADOR = 9;
    public static final int RECARREGAR_BANCO = 10;
    public static final int NOTIFICADO = 11;
    public static final int OBTER_PUBLICADORES = 12;
    private static UriMatcher uriDados;

    private Banco banco;

    //Inicia a variável uriDados de forma estática
    static {
        uriDados = new UriMatcher(UriMatcher.NO_MATCH);
        uriDados.addURI(AUTORIDADE, "dados/InserirPublicador", INSERIR_PUBLICADOR);
        uriDados.addURI(AUTORIDADE, "dados/InserirParte", INSERIR_PARTE);
        uriDados.addURI(AUTORIDADE, "dados/", OBTER_PONTOS);
        uriDados.addURI(AUTORIDADE, "dados/ObterSugeridos", OBTER_SUGERIDOS);
        uriDados.addURI(AUTORIDADE, "dados/ObterPendentes", OBTER_PENDENTES);
        uriDados.addURI(AUTORIDADE, "dados/RemoverParte/#", REMOVER_PARTE);
        uriDados.addURI(AUTORIDADE, "dados/ConcluirParte", CONCLUIR_PARTE);
        uriDados.addURI(AUTORIDADE, "dados/SubstituirParte", SUBSTITUIR_PARTE);
        uriDados.addURI(AUTORIDADE, "dados/RemoverPublicador", REMOVER_PUBLICADOR);
        uriDados.addURI(AUTORIDADE, "dados/RecarregarBanco", RECARREGAR_BANCO);
        uriDados.addURI(AUTORIDADE, "dados/Notificar", NOTIFICADO);
        uriDados.addURI(AUTORIDADE, "dados/ObterPublicadores", OBTER_PUBLICADORES);
    }

    @Override
    public boolean onCreate() {
        banco = new Banco(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        Cursor cursor = null;
        switch (uriDados.match(uri)){
            case OBTER_PONTOS:
                break;
            case OBTER_SUGERIDOS:
                //Efetua a obtenção dos dados
                cursor = banco.obterSugeridos();
                //Implementa um "rastreador" neste cursor que é ativado quando um notifyChange com a uri a seguir é chamado:
                //content://com.example.mathe.gerenciadorpartes.provider/dados/ObterSugeridos
                //Portanto, quando o método "notifyChange" é chamado com essa uri, a consulta acima é refeita.
                //O cursor retornado deve ser o mesmo que recebeu o .setNotificationUri() para que a atualização funcione
                cursor.setNotificationUri(getContext().getContentResolver(), uri);
                break;
            case OBTER_PENDENTES:
                cursor = banco.partesPendentes();
                cursor.setNotificationUri(getContext().getContentResolver(), uri);
                break;
            case OBTER_PUBLICADORES:
                cursor = banco.obterPublicadores();
                cursor.setNotificationUri(getContext().getContentResolver(), uri);
                break;
            default:
                throw new IllegalArgumentException("Consulta não disponivel");
        }
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        switch (uriDados.match(uri)){
            case INSERIR_PUBLICADOR:
                banco.InserirPublicador(values);
                getContext().getContentResolver().notifyChange(Uri.parse("content://com.example.mathe.gerenciadorpartes.provider/dados/ObterPublicadores"), null);
                break;
            case INSERIR_PARTE:
                banco.InserirPartes(values);
                //Notifica a mudança para o cursor cuja uri é:
                //"content://com.example.mathe.gerenciadorpartes.provider/dados/ObterPendentes"
                //Dessa forma, logo depois da mudança feita no banco, a consulta é feita para atualizar a lista.
                //Para isso dar certo, devemos colocar .setNotificationUri() no cursor cuja consulta desejams atualizar
                getContext().getContentResolver().notifyChange(Uri.parse("content://com.example.mathe.gerenciadorpartes.provider/dados/ObterPendentes"), null);
                break;
            default:
                throw new IllegalArgumentException("Insert não disponível");
        }
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        switch (uriDados.match(uri)){
            case REMOVER_PARTE:
                banco.removerParte(Integer.parseInt(uri.getLastPathSegment()));
                getContext().getContentResolver().notifyChange(Uri.parse("content://com.example.mathe.gerenciadorpartes.provider/dados/ObterPendentes"), null);
                break;
            case RECARREGAR_BANCO:
                //Quando o banco é instanciado no onCreate, a referência dele permanece mesmo que o arquivo do banco seja
                //apagado, por isso, quando restauramos o banco do backup, essa reinicializaçao faz com que
                //o novo banco seja carregado pelo programa
                banco = new Banco(getContext());
                break;
            default:
                throw new IllegalArgumentException("Delete não disponivel");
        }
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        switch (uriDados.match(uri)){
            case CONCLUIR_PARTE:
                banco.concluirParte(values, selectionArgs[0]);
                getContext().getContentResolver().notifyChange(Uri.parse("content://com.example.mathe.gerenciadorpartes.provider/dados/ObterPendentes"), null);
                break;
            case SUBSTITUIR_PARTE:
                banco.substituirParte(values, selectionArgs[0]);
                getContext().getContentResolver().notifyChange(Uri.parse("content://com.example.mathe.gerenciadorpartes.provider/dados/ObterPendentes"), null);
                break;
            case REMOVER_PUBLICADOR:
                banco.desativarPublicador(selectionArgs[0]);
                getContext().getContentResolver().notifyChange(Uri.parse("content://com.example.mathe.gerenciadorpartes.provider/dados/ObterPendentes"), null);
                getContext().getContentResolver().notifyChange(Uri.parse("content://com.example.mathe.gerenciadorpartes.provider/dados/ObterPublicadores"), null);
                break;
            case NOTIFICADO:
                banco.setNotificado(selectionArgs[0]);
                getContext().getContentResolver().notifyChange(Uri.parse("content://com.example.mathe.gerenciadorpartes.provider/dados/ObterPendentes"), null);
                break;
            default:
                throw new IllegalArgumentException("Update não disponível");
        }
        return 0;
    }
}