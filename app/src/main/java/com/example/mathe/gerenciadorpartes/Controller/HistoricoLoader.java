package com.example.mathe.gerenciadorpartes.Controller;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.content.AsyncTaskLoader;

/**
 * Created by mathe on 30/12/2017.
 */

//Tutoriais:
//  https://developer.android.com/reference/android/content/AsyncTaskLoader.html
//  http://www.coderzheaven.com/2016/06/01/custom-loaders-with-sqlite-in-android/
//  https://stackoverflow.com/questions/15517920/how-do-cursorloader-automatically-updates-the-view-even-if-the-app-is-inactive
//Relativo a implementação manual de um cursorloader
public class HistoricoLoader extends AsyncTaskLoader<Cursor> {
    private String argumento;               //Argumento que será usado na busca contido no método "obterHistorico"
    private int tipo;                       //Tipo de busca (nome, data, id publicador) que será realizada na tabela "pontos_publicadores"
    private Cursor mcursor;                 //Cursor que armazena o resultado da consulta
    private Banco bancodados;

    public HistoricoLoader(Context context, String arg, int tipo) {
        super(context);
        this.tipo = tipo;
        this.argumento = arg;
        bancodados = new Banco(getContext());
    }

    @Override
    public Cursor loadInBackground() {
        return bancodados.obterHistorico(argumento, tipo);
    }

    //################# código abaixo oopiado do google (link acima) ####################
    @Override
    public void deliverResult(Cursor data) {
        if (isReset()) {
            // An async query came in while the loader is stopped.  We
            // don't need the result.
            if (data != null) {
                LiberarRecursos(data);
            }
        }

        Cursor oldApps = mcursor;
        mcursor = data;

        if (isStarted()) {
            // If the Loader is currently started, we can immediately
            // deliver its results.
            super.deliverResult(data);
        }

        // At this point we can release the resources associated with
        // 'oldApps' if needed; now that the new result is delivered we
        // know that it is no longer in use.
        if (oldApps != null) {
            LiberarRecursos(oldApps);
        }

    }

    @Override
    protected void onStartLoading() {
        if (mcursor != null) {
            // If we currently have a result available, deliver it
            // immediately.
            deliverResult(mcursor);
        }

        if (takeContentChanged() || mcursor == null) {
            // If the data has changed since the last time it was loaded
            // or is not currently available, start a load.
            forceLoad();
        }

    }

    @Override
    protected void onStopLoading() {
        // Attempt to cancel the current load task if possible.
        cancelLoad();
    }

    @Override
    protected void onReset() {
        super.onReset();

        // Ensure the loader is stopped
        onStopLoading();

        // At this point we can release the resources associated with 'apps'
        // if needed.
        if (mcursor!= null) {
            LiberarRecursos(mcursor);
            mcursor = null;
        }
    }

    //Para liberar os recursos utilizados pelo cursor
    protected void LiberarRecursos(Cursor data){
        data.close();
        bancodados.closeBanco();
    }
}
