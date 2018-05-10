package com.example.mathe.gerenciadorpartes;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveClient;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.DriveResourceClient;
import com.google.android.gms.drive.OpenFileActivityOptions;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;

/**
 * An abstract activity that handles authorization and connection to the Drive
 * services.
 */

//Configurando DriveAuth
    //https://developers.google.com/identity/sign-in/android/start-integrating
    //https://stackoverflow.com/questions/35561657/sign-in-with-google-for-android-not-working-in-release-apk
    //Comando:
    //keytool -exportcert -keystore C:\Users\mathe\Documents\chaveGerenciadorPartes.jks(caminho da chave criada no "build signed apk") -list -v
    //https://console.developers.google.com (Site quqe dá permissão para o aplicativo usar o google)
//Fonte:
//  https://github.com/googledrive/android-demos/blob/master/app/src/main/java/com/google/android/gms/drive/sample/demo/BaseDemoActivity.java
public abstract class BaseDemoActivity extends AppCompatActivity{
    private static final String TAG = "BaseDriveActivity";

    /**
     * Request code for google sign-in
     */
    protected static final int REQUEST_CODE_SIGN_IN = 0;

    /**
     * Request code for the Drive picker
     */
    protected static final int REQUEST_CODE_OPEN_ITEM = 1;

    /**
     * Handles high-level drive functions like sync
     */
    private DriveClient mDriveClient;

    /**
     * Handle access to Drive resources/files.
     */
    private DriveResourceClient mDriveResourceClient;

    /**
     * Tracks completion of the drive picker
     */
    private TaskCompletionSource<DriveId> mOpenItemTaskSource;

    @Override
    protected void onStart() {
        super.onStart();
        signIn();
    }

    /**
     * Handles resolution callbacks.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_SIGN_IN:
                if (resultCode != RESULT_OK) {
                    Toast.makeText(this, "Backup desativado", Toast.LENGTH_LONG).show();
                }

                Task<GoogleSignInAccount> getAccountTask =
                        GoogleSignIn.getSignedInAccountFromIntent(data);
                if (getAccountTask.isSuccessful()) {
                    initializeDriveClient(getAccountTask.getResult());
                } else {
                    Log.e(TAG, "Sign-in failed.");
                    finish();
                }
                break;
            case REQUEST_CODE_OPEN_ITEM:
                if (resultCode == RESULT_OK) {
                    DriveId driveId = data.getParcelableExtra(
                            OpenFileActivityOptions.EXTRA_RESPONSE_DRIVE_ID);
                    mOpenItemTaskSource.setResult(driveId);
                } else {
                    mOpenItemTaskSource.setException(new RuntimeException("Unable to open file"));
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Starts the sign-in process and initializes the Drive client.
     */
    protected void signIn() {
        Scope requiredScopes = Drive.SCOPE_APPFOLDER;
        GoogleSignInAccount signInAccount = GoogleSignIn.getLastSignedInAccount(this);
        //Se o acesso ja etiver sido permitido, inicializa o cliente do drive
        if (signInAccount != null && signInAccount.getGrantedScopes().contains(requiredScopes)) {
            initializeDriveClient(signInAccount);
        } else {
            GoogleSignInOptions signInOptions =
                    new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestScopes(Drive.SCOPE_APPFOLDER)
                            .build();
            GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, signInOptions);
            startActivityForResult(googleSignInClient.getSignInIntent(), REQUEST_CODE_SIGN_IN);
        }
    }

    /**
     * Continues the sign-in process, initializing the Drive clients with the current
     * user's account.
     */
    private void initializeDriveClient(GoogleSignInAccount signInAccount) {
        mDriveClient = Drive.getDriveClient(getApplicationContext(), signInAccount);
        mDriveResourceClient = Drive.getDriveResourceClient(getApplicationContext(), signInAccount);
        onDriveClientReady();
    }


    /**
     * Called after the user has signed in and the Drive client has been initialized.
     * um metodo abstrato força seu override em  uma classe filho
     */
    protected abstract void onDriveClientReady();

    protected DriveClient getDriveClient() {
        return mDriveClient;
    }

    protected DriveResourceClient getDriveResourceClient() {
        return mDriveResourceClient;
    }
}