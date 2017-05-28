package online.buzzzz.security.textencryptor;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
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

import online.buzzzz.security.AESCrypto;
import online.buzzzz.security.GoogleDriveHelper;
import online.buzzzz.security.GoogleDriveHelperListener;

@SuppressWarnings("ALL")
public class TextEncryption extends AppCompatActivity implements GoogleDriveHelperListener{

    private String encrypt_err_msg;
    private String decrypt_err_msg;
    private String error_title;
    private String ok_button;
    private final String PREFS_NAME = "TextEncryptor";
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

    }

    private void showMessage(String sMessage){
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

    private void readFile(){
        gh.readFile("TextEncryptor");
    }

    private void writeFile(){
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
        Toast.makeText(getApplicationContext(),"Unable to open file!",Toast.LENGTH_SHORT).show();
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


}
