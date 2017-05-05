package online.buzzzz.security.textencryptor;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.view.ViewGroup;
import android.widget.TextView;
import online.buzzzz.security.AESCrypto;

public class TextEncryption extends AppCompatActivity {

    private String encrypt_err_msg;
    private String decrypt_err_msg;
    private String error_title;
    private String ok_button;
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

        encrypt_err_msg = getString(R.string.encryption_error);
        decrypt_err_msg = getString(R.string.decryption_error);
        error_title = getString(R.string.error_label);
        ok_button = getString(R.string.ok_button);
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
            new AlertDialog.Builder(TextEncryption.this)
                .setTitle(error_title)
                .setMessage(errMessage)
                .setCancelable(true)
                .setNeutralButton(ok_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .show();
    }
    }


}
