package online.buzzzz.security.textencryptor;

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
        LinearLayout layout = (LinearLayout)findViewById(R.id.masterLayout);
        ViewGroup.LayoutParams params = layout.getLayoutParams();
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        layout.setLayoutParams(params);
    }

    protected void doEncrypt(View v){
        doWork(1);
    }

    protected void doDecrypt(View v){
        doWork(2);
    }

    private void doWork(int iMode){
        EditText pass = (EditText)findViewById(R.id.txtPass);
        EditText sourceData = (EditText)findViewById(R.id.txtData);
        String key = pass.getText().toString();
        String data = sourceData.getText().toString();
        String res;
        if(iMode==1){
            res = AESCrypto.encrypt(key,data);
        }else{
            res = AESCrypto.decrypt(key,data);
        }
        sourceData.setText(res);
    }


}
