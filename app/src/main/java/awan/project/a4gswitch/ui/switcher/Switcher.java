package awan.project.a4gswitch.ui.switcher;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import awan.project.a4gswitch.ui.home.MainActivity;

public class Switcher extends AppCompatActivity {

    @SuppressLint("MissingSuperCall")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try{
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                Intent intent = new Intent("android.intent.action.MAIN");
                intent.setClassName("com.android.phone", "com.android.phone.settings.RadioInfo");
                intent.setData(Uri.parse("package:awan.project.a4gswitch"));
                startActivity(intent);
                finish();
            }else{
                Intent RadioInfo = new Intent("android.intent.action.MAIN");
                RadioInfo.setClassName("com.android.settings", "com.android.settings.RadioInfo");
                RadioInfo.setData(Uri.parse("package:awan.project.a4gswitch"));
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
                    Uninstall.setData(Uri.parse("package:awan.project.a4gswitch"));
                    startActivity(Uninstall);
                    finish();
                }
            });
            ErrorMessage.show();
        }
    }

    public void onBackPressed(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
