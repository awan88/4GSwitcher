package awan.project.a4gswitch;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.multidex.MultiDexApplication;

import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.tasks.OnCompleteListener;

public class BaseApp extends Application {

    @Override
    public void onCreate( ) {
        super.onCreate( );
    }
}
