package online.buzzzz.security.textencryptor;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.DriveResource;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.events.ChangeListener;

import java.util.Set;

import online.buzzzz.security.AESCrypto;
import online.buzzzz.security.AndroidHelper;
import online.buzzzz.security.GoogleDriveHelper;
import online.buzzzz.security.GoogleDriveHelperListener;

public class TextEncryption extends AppCompatActivity implements GoogleDriveHelperListener{

    private String encrypt_err_msg;
    private String decrypt_err_msg;
    private String error_title;
    private String ok_button;
    private final String PREFS_NAME = "TextEncryptor";
    private AndroidHelper ah;
    private GoogleDriveHelper gh;
    private int googleDriveMode = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_encryption);
        ViewGroup.LayoutParams params;
        LinearLayout layout = (LinearLayout)findViewById(R.id.masterLayout);
        params = layout.getLayoutParams();
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        params.height = ViewGroup.LayoutParams.MATCH_PARENT;
        layout.setLayoutParams(params);

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        boolean savePass = settings.getBoolean("savePass", false);

        Switch sw = (Switch)findViewById(R.id.savePass);
        sw.setChecked(savePass);

        EditText txtPass = (EditText)findViewById(R.id.txtPass);
        if (savePass){
            txtPass.setText(settings.getString("pass",""));
        }
        txtPass.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString("pass",s.toString());

                editor.apply();
            }
        });


        encrypt_err_msg = getString(R.string.encryption_error);
        decrypt_err_msg = getString(R.string.decryption_error);
        error_title = getString(R.string.error_label);
        ok_button = getString(R.string.ok_button);


        /*
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Drive.API)
                .addScope(Drive.SCOPE_FILE)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        */
    }

    public void showMessage(String sMessage){
        new AlertDialog.Builder(TextEncryption.this)
                .setTitle(error_title)
                .setMessage(sMessage)
                .setCancelable(true)
                .setNeutralButton(ok_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .show();
    }


    public void setPassSave(View v){
        Switch sw = (Switch)findViewById(R.id.savePass);
        EditText txtPass = (EditText)findViewById(R.id.txtPass);
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("savePass", sw.isChecked());
        if(sw.isChecked()){
            editor.putString("pass",txtPass.getText().toString());
        }else{
            editor.remove("pass");
        }
        editor.apply();
    }

    public void doEncrypt(View v){
        doWork(1);
    }

    public void doDecrypt(View v){
        doWork(2);
    }

    public void doCopy(View v) { doCopyPaste(1); }

    public void doPaste(View v){ doCopyPaste(2); }

    public void doCopyPaste(int iMode){
        EditText sourceData = (EditText)findViewById(R.id.txtData);
        ClipboardManager clipboard = (ClipboardManager)
                getSystemService(Context.CLIPBOARD_SERVICE);

        sourceData.selectAll();
        if (iMode == 1){
            ClipData clip = ClipData.newPlainText("Data",sourceData.getText());
            clipboard.setPrimaryClip(clip);
        }else{
            ClipData.Item item = clipboard.getPrimaryClip().getItemAt(0);
            sourceData.setText(item.getText());
        }

    }

    private void doWork(int iMode){
        EditText pass = (EditText)findViewById(R.id.txtPass);
        EditText sourceData = (EditText)findViewById(R.id.txtData);
        String key = pass.getText().toString();
        String data = sourceData.getText().toString();
        String res;
        String errMessage="";
        try{
            if(iMode==1){
                errMessage = encrypt_err_msg;
                res = AESCrypto.encrypt(key,data);
            }else{
                errMessage = decrypt_err_msg;
                res = AESCrypto.decrypt(key,data);
            }
            sourceData.setText(res);
        }catch (Exception ex){
            showMessage(errMessage);
        }
    }

    private void setWindowState(boolean windowState){
        if(windowState){
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }else{
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }
    }

    public void loadFromDrive(View v){
        googleDriveMode = GoogleDriveHelper.READ_MODE;
        setWindowState(false);
        if (gh==null){
            Toast.makeText(getApplicationContext(),"Trying to connect to Google Drive..",Toast.LENGTH_SHORT).show();
            gh = new GoogleDriveHelper();
            gh.init(this,this,"TextEncryptor");
        }else{
            if (gh.isClientConnected){
                Toast.makeText(getApplicationContext(),"Already connected",Toast.LENGTH_SHORT).show();
                readFile();
            }else{
                Toast.makeText(getApplicationContext(),"Trying to connect to Google Drive..",Toast.LENGTH_SHORT).show();
                gh.connect();
            }
        }
    }

    public void saveToDrive(View v){
        googleDriveMode = GoogleDriveHelper.WRITE_MODE;
        if (gh==null){
            Toast.makeText(getApplicationContext(),"Trying to connect to Google Drive..",Toast.LENGTH_SHORT).show();
            gh = new GoogleDriveHelper();
            gh.init(this,this,"TextEncryptor");
        }else{
            if (gh.isClientConnected){
                Toast.makeText(getApplicationContext(),"Already connected",Toast.LENGTH_SHORT).show();
                writeFile();
            }else{
                Toast.makeText(getApplicationContext(),"Trying to connect to Google Drive..",Toast.LENGTH_SHORT).show();
                gh.connect();
            }
        }
    }

    protected void readFile(){
        EditText sourceData = (EditText)findViewById(R.id.txtData);
        gh.readFile("TextEncryptor");
    }

    protected void writeFile(){
        EditText sourceData = (EditText)findViewById(R.id.txtData);
        gh.writeFile("TextEncryptor",sourceData.getText().toString());
    }


    @Override
    protected  void onStop(){
        super.onStop();
        if (gh!=null){
            gh.disconnect();
        }
    }

    @Override
    public void onSuccessfulConnection() {
        Toast.makeText(getApplicationContext(),"Connection Successful",Toast.LENGTH_SHORT).show();
        if(googleDriveMode == GoogleDriveHelper.READ_MODE){
            readFile();
        }else {
            writeFile();
        }
    }

    @Override
    public void onSuccessfulFileOpened(String sData) {
        Toast.makeText(getApplicationContext(),"File opened!!",Toast.LENGTH_SHORT).show();
        EditText sourceData = (EditText)findViewById(R.id.txtData);
        sourceData.setText(sData);
        gh.disconnect();
        setWindowState(true);
    }

    @Override
    public void onSuccessfulFileSaved() {
        Toast.makeText(getApplicationContext(),"File saved successfully",Toast.LENGTH_SHORT).show();
        gh.disconnect();
        setWindowState(true);
    }

    @Override
    public void onConnectionFailure() {
        Toast.makeText(getApplicationContext(),"Error connecting to Google Drive..",Toast.LENGTH_SHORT).show();
        setWindowState(true);
    }

    @Override
    public void onFileOpenFailure() {
        Toast.makeText(getApplicationContext(),"Unable to create file!",Toast.LENGTH_SHORT).show();
        gh.disconnect();
        setWindowState(true);
    }

    @Override
    public void onFileSaveFailure() {
        Toast.makeText(getApplicationContext(),"Unable to save file!",Toast.LENGTH_SHORT).show();
        gh.disconnect();
        setWindowState(true);
    }

    @Override
    public void onSuccessfulFileCreated() {
        Toast.makeText(getApplicationContext(),"New file created successfully",Toast.LENGTH_SHORT).show();
        gh.disconnect();
        setWindowState(true);
    }

    @Override
    public void onFileCreateFailure() {
        Toast.makeText(getApplicationContext(),"Unable to create new file!",Toast.LENGTH_SHORT).show();
        gh.disconnect();
        setWindowState(true);
    }


    /*
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        isClientConnected = true;

        DriveFile file = new DriveFile() {
            @Override
            public PendingResult<DriveApi.DriveContentsResult> open(GoogleApiClient googleApiClient, int i, DownloadProgressListener downloadProgressListener) {
                return null;
            }

            @Override
            public PendingResult<MetadataResult> getMetadata(GoogleApiClient googleApiClient) {
                return null;
            }

            @Override
            public PendingResult<MetadataResult> updateMetadata(GoogleApiClient googleApiClient, MetadataChangeSet metadataChangeSet) {
                return null;
            }

            @Override
            public DriveId getDriveId() {
                return null;
            }

            @Override
            public PendingResult<DriveApi.MetadataBufferResult> listParents(GoogleApiClient googleApiClient) {
                return null;
            }

            @Override
            public PendingResult<Status> delete(GoogleApiClient googleApiClient) {
                return null;
            }

            @Override
            public PendingResult<Status> setParents(GoogleApiClient googleApiClient, Set<DriveId> set) {
                return null;
            }

            @Override
            public PendingResult<Status> addChangeListener(GoogleApiClient googleApiClient, ChangeListener changeListener) {
                return null;
            }

            @Override
            public PendingResult<Status> removeChangeListener(GoogleApiClient googleApiClient, ChangeListener changeListener) {
                return null;
            }

            @Override
            public PendingResult<Status> addChangeSubscription(GoogleApiClient googleApiClient) {
                return null;
            }

            @Override
            public PendingResult<Status> removeChangeSubscription(GoogleApiClient googleApiClient) {
                return null;
            }

            @Override
            public PendingResult<Status> trash(GoogleApiClient googleApiClient) {
                return null;
            }

            @Override
            public PendingResult<Status> untrash(GoogleApiClient googleApiClient) {
                return null;
            }
        };
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()){
            try{
                connectionResult.startResolutionForResult(this,1);
            }catch (IntentSender.SendIntentException e){
                showMessage(e.getMessage());
            }

        }else{
            showMessage(getString(R.string.unable_to_connect));
        }

        showMessage(getString(R.string.drive_not_available));
    }
    */
}
