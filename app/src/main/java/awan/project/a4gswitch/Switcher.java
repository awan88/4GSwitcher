package awan.project.a4gswitch;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class Switcher extends AppCompatActivity {


    @SuppressLint("MissingSuperCall")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        try{
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                Intent intent = new Intent("android.intent.action.MAIN");
                intent.setClassName("com.android.phone", "com.android.phone.settings.RadioInfo");
                startActivity(intent);
                finish();
            }else{
                super.onCreate(savedInstanceState);
                Intent RadioInfo = new Intent("android.intent.action.MAIN");
                RadioInfo.setClassName("com.android.settings", "com.android.settings.RadioInfo");
                startActivity(RadioInfo);
                finish();
            }
        }

        catch (Exception e) {
            AlertDialog.Builder ErrorMessage = new AlertDialog.Builder(this);
            ErrorMessage.setMessage("Maaf, konfigurasi hp mu menolak fungsi yang dipake sama apk ini. kamu gak bisa menggunakannya.");
            ErrorMessage.setPositiveButton("dah tau", new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int id)
                {
                    dialog.dismiss();
                    Intent Uninstall = new Intent(Intent.ACTION_DELETE);
                    Uninstall.setData(Uri.parse("package:fr.studio.cracky.switcher4g"));
                    startActivity(Uninstall);
                    finish();
                }
            });
            ErrorMessage.show();
        }
    }

    public void onBackPressed(){
        Intent intent = new Intent(Switcher.this, MainActivity.class);
        startActivity(intent);
        this.finish();

    }
}
