package online.buzzzz.security;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public class AndroidHelper {
    public static void showMessage(Context context, String title, String sMessage, String button_text){
        new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(sMessage)
                .setCancelable(true)
                .setNeutralButton(button_text, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .show();
    }
}
