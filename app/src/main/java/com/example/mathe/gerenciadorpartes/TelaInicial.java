package com.example.mathe.gerenciadorpartes;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Debug;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.mathe.gerenciadorpartes.Controller.Banco;
import com.example.mathe.gerenciadorpartes.Controller.Provider;
import com.example.mathe.gerenciadorpartes.Model.Ponto;
import com.example.mathe.gerenciadorpartes.Model.Publicador;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.drive.CreateFileActivityOptions;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.security.Permission;
import java.security.Permissions;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

//Tutorial TabLayout:
//  https://www.youtube.com/watch?v=bNpWGI_hGGg&t=100s
public class TelaInicial extends BaseDemoActivity implements android.app.LoaderManager.LoaderCallbacks<Cursor>{

    private final int REQUISICAO_ESCRITA = 1;
    public ArrayList<Publicador> publicadores;
    private SectionsPageAdapter sectionAdapter;
    private ViewPager conteudo_abas;
    public ArrayList<Ponto> pontos;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tela_inicial);

        //Inicializa variaveis, relacionando-os aos componentes do layout
        //Ao usar o findViewbyid, temos que dizer em qual view procuramos aquele id
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_main);
        TabLayout abas = (TabLayout) findViewById(R.id.abas);
        conteudo_abas = (ViewPager) findViewById(R.id.container);

        //Define a Toolbar (toolbar_main) como ActionBar (a barra de ferramentas do aplicativo)
        setSupportActionBar(toolbar);

        //Cria os fragmentos (O que mostra o conteúdo das abas)
        criarFragmentos();

        //Define "conteudo_abas" como o viewpager (visualizador de conteúdos das abas) do tablayout "abas"
        abas.setupWithViewPager(conteudo_abas);

        //Obtem os publicadores e os pontos para serem usados no programa
        Banco b = new Banco(this);
        getLoaderManager().restartLoader(0, null, this);
        pontos = b.obterPontos();
    }

    //Cria a view dos fragmentos (new Pendentes() chama onCreateview implementado nessa classe) e os adiciona no tablayout
    //pare que sejam acessíveis ao usuário
    private void criarFragmentos(){
        sectionAdapter = new SectionsPageAdapter(getSupportFragmentManager());
        sectionAdapter.adcFragmento(new Pendentes(), "Pendentes");
        sectionAdapter.adcFragmento(new Historico(), "Historico");

        conteudo_abas.setAdapter(sectionAdapter);
    }

    //Função necessária para carregar menu criado em res/menu
    //Tutoriais:
    //  https://stackoverflow.com/questions/31231609/creating-a-button-in-android-toolbar
    //  https://stackoverflow.com/questions/29047902/how-to-add-an-image-to-the-drawable-folder-in-android-studio
    public boolean onCreateOptionsMenu(Menu menu) {
        //Se a actionbar estiver presente na activity, o menu cujo layout está em res/menu é inflado
        getMenuInflater().inflate(R.menu.menu_tela_inicial, menu);
        return true;
    }

    //Evento disparado quando apertamos um item do menu (indicado pelo MenuItem item)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //Verificamos qual item ele é.
        if(item.getItemId() == R.id.adcpub){        //Se for o item Adicionar publicadores
            //Declara a caixa de dialogo
            final AlertDialog.Builder caixa_dialogo = new AlertDialog.Builder(TelaInicial.this);
            caixa_dialogo.setTitle("Adicionar Publicador");

            //Define a view da caixa de dialogo (disponível no android.support.v7.app)
            caixa_dialogo.setView(R.layout.adicionarpublicador);

            //Definindo a função dos botões da caixa de dialogo
            caixa_dialogo.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //Ao usar o findViewById, definimos de qual view fazemos isso, caso cotrário, ele
                    //não encontra o elemento
                    EditText caixatexto = (EditText) ((AlertDialog) dialog).findViewById(R.id.adcionarpublicador);
                    Spinner sexo = (Spinner) ((AlertDialog) dialog).findViewById(R.id.spinnersexo);

                    //Se a caixa de texto estiver vazia, não adicionamos o publicador
                    if(!caixatexto.getText().toString().isEmpty()) {
                        adcPublicadores(caixatexto.getText().toString(), sexo.getSelectedItemPosition());   //adicionamos publicado
                        AtualizarPublicadores();                                                            //atualizamos publicadores em outras views
                    }
                    else{
                        dialog.dismiss();
                        Toast.makeText(getBaseContext(), "Falha ao incluir publicador", Toast.LENGTH_LONG).show();
                    }
                }
            });
            caixa_dialogo.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            caixa_dialogo.show();
            return true;
        }
        else if(item.getItemId() == R.id.backup){        //Se for o item Backup
            //Inicializa ProgressDialog, definindo também seu título, mensagem, e o estilo do símbolo de carregamento
            //que é um circulo giratório
            progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Backup dos dados");
            progressDialog.setMessage("Passando dados para nuvem...");
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.show();

            //Executa classe AsyncTask Backup
            new Backup().execute();
            return true;
        }
        else if(item.getItemId() == R.id.restore) {        //Se for o item de Recuperação de dados
            //Inicializa ProgressDialog, definindo também seu título, mensagem, e o estilo do símbolo de carregamento
            //que é um circulo giratório
            progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Recuperação dos dados");
            progressDialog.setMessage("Obtendo dados da nuvem...");
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.show();

            //Executa classe AsyncTask Restore
            new Restore().execute();
            return true;
        }
        else if(item.getItemId() == R.id.removePub){        //Se for o item de Remover publicadores
            removePublicadorDialog();
            return true;
        }
        //https://developer.android.com/training/permissions/requesting.html?hl=pt-br#explain
        else if(item.getItemId() == R.id.l_backup){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requerir_permissoes();
            }
            else{
                new exportar_para_texto(this).execute();
            }
            return true;
        }
        //Caso não for nenhuma das opções acima, o código padrão da barra de ferramentas é executado
        else{
            return super.onOptionsItemSelected(item);
        }
    }

    public void removePublicadorDialog(){
        //Variável que será usada para armazenar o id obtido na AutoCompleteTextView
        //Não pode ser um inteiro, pois para ser acessado dentro das classes passadas nos métodos: "OnItemClick" e "TextWatcher"
        //a variável precisa ser "final", mas uma variável "final" pode ser atribuida apenas uma vez, o que não serviria neste caso,
        //cujo id pode mudar dependendo do desejo do usuário, portanto foi usado um objeto da classe Publicador. Dessa forma,
        //podemos declara-la como "final" e mudar o atributo "id" diversas vezes, já que assim não mudaremos o objeto em sí, apenas
        //um atributo. Esse 'truque' também pode ser feito com vetores.
        final Publicador pub = new Publicador(0, "", "m");

        //Infla view que será usada na caixa de dialogo
        View view_dialog = getLayoutInflater().inflate(R.layout.remover_publicador, null, false);

        //Procura o item "removepublic" dentro da "view_dialog"
        final AutoCompleteTextView actv_nome_public = (AutoCompleteTextView) view_dialog.findViewById(R.id.removepublic);

        //Inicializa arrayadapter do AutoCompleteTextView
        //Seus argumento são: contexto, layout da lista de sugestões, vetor de dados
        ArrayAdapter<Publicador> aa = new ArrayAdapter<Publicador>(this, android.R.layout.simple_list_item_1, publicadores);
        actv_nome_public.setAdapter(aa);

        //Quando clica numa sugestão do AutoCompleteTextView, o id dele é obtido, mas se o texto do actv_nome_public for alterado
        //o id volta para 0. Isso impede que, após clicar em uma sugestão, a pessoa altere o nome manualmente (até para o nome
        // de outra pessoa) e que o id permaneça inalterado. Dessa forma, o usuário só pode apagar uma pessoa que foi selecionada
        // no AutoCompleteTextView e não modificar manualmente
        //OBS: Quando uma sugestão é escolhida e ela aparece no AutoCompleTextView, ela não dispara o evento de textWatcher, por
        //isso o método descrito acima funciona
        actv_nome_public.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Publicador p = (Publicador) parent.getItemAtPosition(position);
                pub.setId(p.getId());
            }
        });
        actv_nome_public.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                pub.setId(0);
            }
        });

        //Inicializando caixa de dialogo
        AlertDialog.Builder caixa_dialogo = new AlertDialog.Builder(this);
        caixa_dialogo.setView(view_dialog);
        caixa_dialogo.setTitle("Remover publicador");
        caixa_dialogo.setPositiveButton("Excluir", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Se o AutoCompleteTextView não estiver vazio e o id do publicador for diferete de 0, a remoção é realizada
                if(!actv_nome_public.getText().toString().isEmpty() && pub.getId() != 0) {
                    new RemovePublicador().execute(pub.getId());
                }
                else{
                    Toast.makeText(TelaInicial.this, "Remoção mal-sucedida", Toast.LENGTH_LONG).show();
                }
            }
        }).setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        caixa_dialogo.show();
    }

    public void adcPublicadores(String nome, int sexo){
        //O id do publicador no banco é inserido automaticamente e é autoincrementado
        //A classe Publicador converte o sexo de inteiro para char (forma como é armazenado no banco), por isso é usado
        //um objeto dessa classe para fazer a conversão.
        //Foi tentado atualizar manualmente a lista de publicadores e depois o banco, sem ser necessário pegar de volta
        // o Loader, somando o id do último publicador para colocar no novo publicador
        Publicador pub = new Publicador(0, nome, sexo);

        ContentValues cv = new ContentValues();
        cv.put("nome", pub.getNome());
        cv.put("ativo", 1);
        cv.put("sexo", pub.getSexo_banco().toString());

        new AdicionaPublicador().execute(cv);
    }

    //Enviar broadcast message para view ativas atualizarem os publicadores (neste caso, o AutoCompleteTexteView do
    // fragmento historico)
    public void AtualizarPublicadores(){
        Intent intent = new Intent("atualizarPublicadores");
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    protected void onDriveClientReady() {
        //Faz nada quando termina de carregar o Drive client
    }

    //Loader para carregar os publicadores de forma assíncrona
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri uri = Uri.parse("content://com.example.mathe.gerenciadorpartes.provider/dados/ObterPublicadores");
        CursorLoader consulta = new CursorLoader(TelaInicial.this, uri, null, null, null, null);
        return  consulta;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        //Reinicia Lista de publicadores e preenche com os publicadores atualizados
        publicadores = new ArrayList<>();
        while (!data.isAfterLast()){
            publicadores.add(new Publicador(data.getInt(0), data.getString(1), data.getString(3)));
            data.moveToNext();
        }

        //Como a lista de publicadores é usada para arrayadapter em Historico, o método abaixo é chamado para atualizar
        //esse vetor na view de lá
        AtualizarPublicadores();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        loader.cancelLoad();
    }

    class Backup extends AsyncTask<Void, Void, Void>{

        @Override
        protected Void doInBackground(Void... voids) {
            //Fonte:
            //  https://github.com/googledrive/android-demos/blob/master/app/src/main/java/com/google/android/gms/drive/sample/demo/CreateFileInAppFolderActivity.java

            //Uma task é uma API para fazer chamadas assíncrona, de forma que não precisa ser chamada dentro de uma
            //AsyncTask para ser assícrona
            //Obtem appfolder criada para este app no GoogleDrive
            final Task<DriveFolder> appFolderTask = getDriveResourceClient().getAppFolder();
            //Cria referência para conteúdo que irá ser criado
            final Task<DriveContents> createContentsTask = getDriveResourceClient().createContents();
            Tasks.whenAll(appFolderTask, createContentsTask)
                    .continueWithTask(new Continuation<Void, Task<DriveFile>>() {
                        @Override
                        public Task<DriveFile> then(@NonNull Task<Void> task) throws Exception {
                            DriveFolder parent = appFolderTask.getResult();
                            DriveContents contents = createContentsTask.getResult();

                            //Faz a copia do arquivo do input para o output
                            //Tutorial
                            //  https://stackoverflow.com/questions/9292954/how-to-make-a-copy-of-a-file-in-android
                            //A estrutura abaixo é denominada try with resources e ela fecha os recursos inicializados
                            //dentro dos parênteses após execução do bloco
                            try (InputStream entrada = new FileInputStream(getDatabasePath("dados.db"))) {
                                try (OutputStream saida = contents.getOutputStream()) {
                                    // Transfere bytes from in to out
                                    byte[] buf = new byte[1024];
                                    int len;
                                    while ((len = entrada.read(buf)) > 0) {
                                        saida.write(buf, 0, len);
                                    }
                                }
                            }

                            MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                                    .setTitle("dados.db")
                                    .setMimeType("application/x-sqlite3")
                                    .setStarred(true)
                                    .build();

                            return getDriveResourceClient().createFile(parent, changeSet, contents);
                        }
                    })
                    .addOnSuccessListener(new OnSuccessListener<DriveFile>() {
                        @Override
                        public void onSuccess(DriveFile driveFile) {
                            Toast.makeText(getBaseContext(), "Backup feito com sucesso", Toast.LENGTH_LONG).show();
                            progressDialog.dismiss();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getBaseContext(), "Falha no backup", Toast.LENGTH_LONG).show();
                            progressDialog.dismiss();
                        }
                    });
            return null;
        }
    }

    class Restore extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            //Fonte
            //  https://developers.google.com/android/guides/tasks
            //  https://developers.google.com/drive/android/files#lifecycle_of_a_drive_file

            //Apaga banco de dados
            //Fonte
            //  https://stackoverflow.com/questions/4406067/how-to-delete-sqlite-database-from-android-programmatically
            deleteDatabase("dados.db");

            //Tarefa que obtem appfolder criada para este app no GoogleDrive
            final Task<DriveFolder> obterPastaApp = getDriveResourceClient().getAppFolder();

            //O continuewithtask serve para continuar uma tarefa com outra tarefa.
            //Neste caso, quando a tarefa pastaApp acabar, começa a queryTask
            //Fonte:
            //  https://stackoverflow.com/questions/40161333/google-play-services-task-api-continuewith-vs-continuewithtask
            obterPastaApp.continueWithTask(new Continuation<DriveFolder, Task<MetadataBuffer>>() {
                @Override
                public Task<MetadataBuffer> then(@NonNull Task<DriveFolder> task) throws Exception {
                    //Obtem resultado da tarefa anterior
                    DriveFolder pastaApp = obterPastaApp.getResult();

                    //Monta a query procurando o arquivo de nome "dados.db"
                    //Fonte:
                    //  https://developers.google.com/drive/android/queries
                    final Query buscaArquivoBanco = new Query.Builder()
                            .addFilter(Filters.eq(SearchableField.TITLE, "dados.db"))
                            .build();
                    //Tarefa para executar a query dentro da subpasta "resultado" (appfolder)
                    final Task<MetadataBuffer> obtemArquivoBanco = getDriveResourceClient().queryChildren(pastaApp, buscaArquivoBanco);
                    obtemArquivoBanco.continueWithTask(new Continuation<MetadataBuffer, Task<DriveContents>>() {
                        @Override
                        public Task<DriveContents> then(@NonNull Task<MetadataBuffer> task) throws Exception {
                            //Obtem resultado da tarefa acima (lista de metadatas)
                            MetadataBuffer metadata = obtemArquivoBanco.getResult();
                            DriveFile arquivo = metadata.get(0).getDriveId().asDriveFile();

                            //Tarefa para abrir arquivo no modo leitura
                            final Task<DriveContents> abrirArquivo = getDriveResourceClient().openFile(arquivo, DriveFile.MODE_READ_ONLY);
                            abrirArquivo.continueWithTask(new Continuation<DriveContents, Task<Void>>() {
                                @Override
                                public Task<Void> then(@NonNull Task<DriveContents> task) throws Exception {
                                    //Fonte:
                                    //  https://developers.google.com/drive/android/files
                                    //Obtem resultado da tarefa anterior (conteúdo do arquivo aberto)
                                    DriveContents conteudoArquivo = abrirArquivo.getResult();

                                    //Define entrada dos dados da tarefa de cópia
                                    try (InputStream entrada = conteudoArquivo.getInputStream()) {
                                        //Obtem caminho do banco de daoos no celular para saída dos dados
                                        String destPath = getFilesDir().getPath();
                                        destPath = destPath.substring(0, destPath.lastIndexOf("/")) + "/databases/dados.db";
                                        File caminho = new File(destPath);

                                        //Define saída dos dados da tarefa de cópia
                                        try (OutputStream saida = new FileOutputStream(caminho)) {
                                            // Transfer bytes from in to out
                                            byte[] buf = new byte[1024];
                                            int len;
                                            while ((len = entrada.read(buf)) > 0) {
                                                saida.write(buf, 0, len);
                                            }
                                        }
                                        Toast.makeText(getBaseContext(), "Recuperação realizada com sucesso", Toast.LENGTH_LONG).show();
                                    }

                                    //Descarta alterações (já que o arquivo é apenas para leitura), fechando o DriveContents
                                    Task<Void> discardTask = getDriveResourceClient().discardContents(conteudoArquivo);
                                    return discardTask;
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {  //mesma função do onPostExecute do assynctask, só que aqui só executa quando HÁ falhas na execução
                                    Toast.makeText(getBaseContext(), "Arquivo não apode ser aberto", Toast.LENGTH_LONG).show();
                                    progressDialog.dismiss();
                                }
                            });

                            return abrirArquivo;
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) { //mesma função do onPostExecute do assynctask, só que aqui só executa quando não há falhas na execução
                            Toast.makeText(getBaseContext(), "Arquivo não achado na nuvem", Toast.LENGTH_LONG).show();
                            progressDialog.dismiss();
                        }
                    });
                    return obtemArquivoBanco;
                }
            }).addOnSuccessListener(new OnSuccessListener<MetadataBuffer>() { //mesma função do onPostExecute do assynctask, só que aqui só executa quando não há falhas na execução
                @Override
                public void onSuccess(MetadataBuffer metadata) {
                    progressDialog.setMessage("Aplicando mudanças...");

                    //Obtem os publicadores do arquivo restaurado
                    getLoaderManager().restartLoader(0, null, TelaInicial.this);

                    //Atualiza as informações dos fragmentos da TelaInicial
                    Intent intent = new Intent("atualizarHistorico");
                    LocalBroadcastManager.getInstance(TelaInicial.this).sendBroadcast(intent);

                    intent = new Intent("atualizarPublicadores");
                    LocalBroadcastManager.getInstance(TelaInicial.this).sendBroadcast(intent);

                    intent = new Intent("atualizarPendentes");
                    LocalBroadcastManager.getInstance(TelaInicial.this).sendBroadcast(intent);
                    progressDialog.dismiss();
                }
            });
            //O progressDialog.dismiss(); não é colocado aqui, pois como Tasks são assíncronas, esse método
            //seria executado antes da tarefa acabar
            return null;
        }
    }

    class AdicionaPublicador extends AsyncTask<ContentValues, Void, Void>{

        @Override
        protected Void doInBackground(ContentValues... publicador) {
            Uri u = Uri.parse("content://com.example.mathe.gerenciadorpartes.provider/dados/InserirPublicador");
            getContentResolver().insert(u, publicador[0]);
            return null;
        }
    }

    class RemovePublicador extends AsyncTask<Integer, Void, Void>{

        @Override
        protected Void doInBackground(Integer... integers) {
            Uri u = Uri.parse("content://com.example.mathe.gerenciadorpartes.provider/dados/RemoverPublicador");
            ContentValues valor = new ContentValues();
            valor.put("ativo", 0);
            getContentResolver().update(u, valor, null, new String[]{String.valueOf(integers[0])});
            return null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode){
            case REQUISICAO_ESCRITA:
                new exportar_para_texto(this).execute();
        }
    }

    @RequiresApi(23)
    public void requerir_permissoes(){
        int permissionCheck = getBaseContext().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if(permissionCheck != PermissionChecker.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUISICAO_ESCRITA);
        }
    }

    class exportar_para_texto extends AsyncTask<Void, Void, Integer>{

        private Context contexto;

        public exportar_para_texto(Context c){
            contexto = c;
        }

        @Override
        protected Integer doInBackground(Void... voids) {
            try (InputStream entrada = new FileInputStream(getDatabasePath("dados.db"))) {

                String state = Environment.getExternalStorageState();
                if (Environment.MEDIA_MOUNTED.equals(state)) {
                    File pasta = new File(Environment.getExternalStorageDirectory().toString() + "/dados.db");
                    try (OutputStream saida = new FileOutputStream(pasta)) {
                        // Transfere bytes from in to out
                        byte[] buf = new byte[1024];
                        int len;
                        while ((len = entrada.read(buf)) > 0) {
                            saida.write(buf, 0, len);
                        }

                        saida.flush();

                        return 0;
                    }
                }
            }
            catch (IOException e){
                return 1;
            }

            return null;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            if(integer==0){
                Toast.makeText(contexto, "Exportação feita com sucesso", Toast.LENGTH_LONG).show();
            }
            else{
                Toast.makeText(contexto, "Falaha na exportação", Toast.LENGTH_LONG).show();
            }
        }
    }
}