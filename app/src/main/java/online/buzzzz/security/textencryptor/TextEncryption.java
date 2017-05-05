package online.buzzzz.security.textencryptor;

import android.app.AlertDialog;
import android.content.DialogInterface;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_encryption);
        ConstraintLayout mainLayout = (ConstraintLayout)findViewById(R.id.mainLayout);
        ViewGroup.LayoutParams params = mainLayout.getLayoutParams();
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        params.height = ViewGroup.LayoutParams.MATCH_PARENT;
        mainLayout.setLayoutParams(params);

        LinearLayout layout = (LinearLayout)findViewById(R.id.masterLayout);
        params = layout.getLayoutParams();
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        params.height = ViewGroup.LayoutParams.MATCH_PARENT;
        layout.setLayoutParams(params);
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
        try{
            if(iMode==1){
                res = AESCrypto.encrypt(key,data);
            }else{
                res = AESCrypto.decrypt(key,data);
            }
            sourceData.setText(res);
        }catch (Exception ex){
            new AlertDialog.Builder(TextEncryption.this)
                .setTitle("Error!")
                .setMessage("Oops! Something went wrong with "+(iMode==1?"Encryption":"Decryption"))
                .setCancelable(true)
                .setNeutralButton("Ok!", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .show();
    }
    }


}
