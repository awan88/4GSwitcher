package awan.project.a4gswitch.ui.home;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.InstallState;
import com.google.android.play.core.install.InstallStateUpdatedListener;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.InstallStatus;
import com.google.android.play.core.install.model.UpdateAvailability;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

import awan.project.a4gswitch.R;
import awan.project.a4gswitch.helper.TickProgressBar;
import awan.project.a4gswitch.ui.history.HistoryActivity;
import awan.project.a4gswitch.ui.switcher.Switcher;
import awan.project.a4gswitch.ui.tutorial.TutorialActivity;
import awan.project.a4gswitch.util.ConnectionDetector;
import awan.project.a4gswitch.util.GetSpeedTestHostsHandler;
import awan.project.a4gswitch.util.test.HttpDownloadTest;
import awan.project.a4gswitch.util.test.HttpUploadTest;
import awan.project.a4gswitch.util.test.PingTest;

public class MainActivity extends AppCompatActivity {

    private SharedPreferences sharedPref;
    private AppUpdateManager mAppUpdateManager;
    private static final int RC_APP_UPDATE = 2;

    private final DecimalFormat dec = new DecimalFormat ("#.##");
    GetSpeedTestHostsHandler getSpeedTestHostsHandler = null;
    private TickProgressBar tickProgressMeasure;

    private LineDataSet lineDataSet;
    private LineChart lcMeasure;
    private LineData lineData;
    ConnectivityManager cm;
    ConnectionDetector cd;

    private AdView mAdView;
    private InterstitialAd mInterstitialAd;

    TextView tvTypeCon, tvDownloadU, tvUploadU, tvBlink, tvPing, tvDownload, tvDownloadL, tvUpload, tvUploadL, tvPingL;
    ImageView ivPBDownload, ivPBUpload;
    Button btnCekSpeed, btnSwitch4G;

    private float i = 0, j = 0, k = 0;
    double distance;
    int clickCoun = 0;
    int position = 0;
    int lastPosition = 0;

    String uploadAddr, btnClick;
    Boolean isInternetPresent = false;

    HashSet<String> tempBlackList;
    List<String> info;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(false);
        cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        cd = new ConnectionDetector(this);
        sharedPref = this.getSharedPreferences("setting", Context.MODE_PRIVATE);
        isInternetPresent = cd.isConnectingToInternet();

        initView();
        initListenner();
        setTypeNetwork();
    }

    private void initView() {
        mAdView = findViewById(R.id.adView);
        tvDownloadU = findViewById(R.id.tv_download_unit);
        tvUploadU = findViewById(R.id.tv_upload_unit);
        tvBlink = findViewById(R.id.tv_information);
        tvPing = findViewById(R.id.tv_ping_value);
        tvDownload = findViewById(R.id.tv_download_value);
        tvDownloadL = findViewById(R.id.tv_download_label);
        tvUpload = findViewById(R.id.tv_upload_value);
        tvUploadL = findViewById (R.id.tv_upload_label);
        lcMeasure = findViewById(R.id.linechart);
        tickProgressMeasure = findViewById(R.id.tickProgressBar);
        ivPBDownload = findViewById(R.id.iv_download);
        ivPBUpload = findViewById(R.id.iv_upload);
        tvPingL = findViewById(R.id.tv_ping_label);
        tvTypeCon = findViewById(R.id.tvTypeCon);
        btnCekSpeed = findViewById(R.id.btntestDownload);
        btnSwitch4G = findViewById(R.id.btnswitch4G);

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
                loadFullScreenAd();
            }
        });

        btnCekSpeed.setOnClickListener(v -> {
//            Intent intent = new Intent(this, SpeedActivity.class);
//            startActivity(intent);
            btnCekSpeed.setEnabled(false);
            btnCekSpeed.setText("Prosess");
            btnClick = "cekSpeed";
            btnCekSpeed.setBackgroundColor(this.getResources().getColor(R.color.colorAccent));
            if (mInterstitialAd != null && clickCoun % 5 == 0) {
                mInterstitialAd.show(this);
            } else {
                testSpeed();
                Log.d("TAG", "The interstitial ad wasn't ready yet.");
            }
            clickCoun++;
        });

        btnSwitch4G.setOnClickListener(v -> {
            btnClick = "ganti4g";
            if (mInterstitialAd != null && clickCoun % 5 == 0) {
                mInterstitialAd.show(this);
            } else {
                Intent intent = new Intent(this, Switcher.class);
                startActivity(intent);
                Log.d("TAG", "The interstitial ad wasn't ready yet.");
            }
            clickCoun++;
        });
    }

    private void initListenner() {
        tvDownloadU.setText(sharedPref.getString("UNIT", "Mbps"));
        tvUploadU.setText(sharedPref.getString("UNIT", "Mbps"));
        tickProgressMeasure.setMax(100 * 100);
        tvPingL.post(() -> {
            int length = tvPingL.getMeasuredWidth();
            float angle = 45;
            Shader textShader = new LinearGradient(0, 0, (int) (Math.sin(Math.PI * angle / 180) * length),
                    (int) (Math.cos(Math.PI * angle / 180) * length),
                    new int[]{0xFF30E3CA, 0xFFa5dee5},
                    null,
                    Shader.TileMode.CLAMP);
            tvPingL.getPaint().setShader(textShader);
            tvPingL.invalidate();
        });
        tvDownloadL.post(() -> {
            int length = tvDownloadL.getMeasuredWidth();
            float angle = 45;
            Shader textShader = new LinearGradient(0, 0, (int) (Math.sin(Math.PI * angle / 180) * length),
                    (int) (Math.cos(Math.PI * angle / 180) * length),
                    new int[]{0xFF30E3CA, 0xFFa5dee5},
                    null,
                    Shader.TileMode.CLAMP);
            tvDownloadL.getPaint().setShader(textShader);
            tvDownloadL.invalidate();
        });
        tvUploadL.post (() -> {
            int length = tvUploadL.getMeasuredWidth ();
            float angle = 45;
            Shader textShader = new LinearGradient (0, 0, (int) (Math.sin (Math.PI * angle / 180) * length),
                    (int) (Math.cos (Math.PI * angle / 180) * length),
                    new int[] {0xFF30E3CA, 0xFFa5dee5},
                    null,
                    Shader.TileMode.CLAMP);
            tvUploadL.getPaint ().setShader (textShader);
            tvUploadL.invalidate ();
        });
        ivPBUpload.setAlpha (0.5f);
        ivPBDownload.setAlpha (0.5f);
        List<Entry> entryList = new ArrayList<>();
        entryList.add (new Entry (0, 0));
        lineDataSet = new LineDataSet (entryList, "");
        lineDataSet.setMode (LineDataSet.Mode.CUBIC_BEZIER);
        lineDataSet.setDrawValues (false);
        lineDataSet.setDrawCircleHole (false);
        lineDataSet.setColor (Color.rgb (145, 174, 210));
        lineDataSet.setCircleColor (Color.rgb (145, 174, 210));
        lineDataSet.setLineWidth (2f);
        lineDataSet.setDrawFilled (false);
        lineDataSet.setHighlightEnabled (false);
        lineDataSet.setDrawCircles (false);
        lineData = new LineData (lineDataSet);
        lcMeasure.setData (lineData);
        lcMeasure.getAxisRight ().setDrawGridLines (false);
        lcMeasure.getAxisRight ().setDrawLabels (false);
        lcMeasure.getAxisLeft ().setDrawGridLines (false);
        lcMeasure.getAxisLeft ().setDrawLabels (false);
        lcMeasure.fitScreen ();
        lcMeasure.setVisibleXRange (0, 10);
        lcMeasure.setNoDataText ("TAP SCAN");
        lcMeasure.setNoDataTextColor (R.color.cp_0);
        lcMeasure.getXAxis ().setDrawGridLines (false);
        lcMeasure.getXAxis ().setDrawLabels (false);
        lcMeasure.getLegend ().setEnabled (false);
        lcMeasure.getDescription ().setEnabled (false);
        lcMeasure.setScaleEnabled (true);
        lcMeasure.setDrawBorders (false);
        lcMeasure.getAxisLeft ().setEnabled (false);
        lcMeasure.getAxisRight ().setEnabled (false);
        lcMeasure.getXAxis ().setEnabled (false);
        lcMeasure.setViewPortOffsets (10f, 10f, 10f, 10f);
        lcMeasure.animateX (1000);
        tempBlackList = new HashSet<> ();
        getSpeedTestHostsHandler = new GetSpeedTestHostsHandler ();
        getSpeedTestHostsHandler.start ();
        defaultValues ();
    }

    private int getPositionByRate (double rate) {
        if (rate <= 1) {
            return (int) (rate * 30);
        } else if (rate <= 2) {
            return (int) (rate * 3) + 30;
        } else if (rate <= 3) {
            return (int) (rate * 3) + 60;
        } else if (rate <= 4) {
            return (int) (rate * 3) + 90;
        } else if (rate <= 5) {
            return (int) (rate * 3) + 120;
        } else if (rate <= 10) {
            return (int) ((rate - 5) * 6) + 150;
        } else if (rate <= 50) {
            return (int) ((rate - 10) * 1.33) + 180;
        } else if (rate <= 100) {
            return (int) ((rate - 50) * 0.6) + 180;
        }
        return 0;
    }

    private void defaultValues () {
        tickProgressMeasure.setProgress (0);
        tvPing.setText ("0");
        tvDownload.setText ("0");
        tvUpload.setText ("0");
        tvBlink.setText (this.getResources().getString(R.string.information));
    }

    @SuppressLint("MissingPermission")
    private void loadFullScreenAd() {
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        String adUnitId = this.getResources().getString(R.string.intersitialADsID);
        InterstitialAd.load(this, adUnitId, adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        // The mInterstitialAd reference will be null until
                        // an ad is loaded.
                        Log.i("TAG", "onAdLoaded");
                        mInterstitialAd = interstitialAd;
                        loadAdsSucess();
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error
                        Log.i("TAG", loadAdError.getMessage());
                        mInterstitialAd = null;
                        loadAdsFailed();
                    }
                });
    }

    private void loadAdsFailed() {
        new CountDownTimer(30000, 1000) {
            public void onTick(long millisUntilFinished) { }
            public void onFinish() {
                //testSpeed();
            }
        }.start();
    }

    private void loadAdsSucess() {
        mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
            @Override
            public void onAdDismissedFullScreenContent() {
                // Called when fullscreen content is dismissed.
                Log.d("TAG", "The ad was dismissed.");
                if (btnClick == "cekSpeed"){
                    testSpeed();
                }else {
                    Intent intent = new Intent(MainActivity.this, Switcher.class);
                    startActivity(intent);
                }
            }

            @Override
            public void onAdFailedToShowFullScreenContent(AdError adError) {
                // Called when fullscreen content failed to show.
                Log.d("TAG", "The ad failed to show.");
               // testSpeed();
            }

            @Override
            public void onAdShowedFullScreenContent() {
                // Called when fullscreen content is shown.
                mInterstitialAd = null;
                Log.d("TAG", "The ad was shown.");
            }
        });
    }

    private void testSpeed() {

        tvBlink.setVisibility (View.VISIBLE);
        Animation anim = new AlphaAnimation(0.0f, 1.0f);
        anim.setDuration (650);
        anim.setStartOffset (20);
        anim.setRepeatMode (Animation.REVERSE);
        anim.setRepeatCount (Animation.INFINITE);
        tvBlink.startAnimation (anim);
        if (getSpeedTestHostsHandler == null) {
            getSpeedTestHostsHandler = new GetSpeedTestHostsHandler ();
            getSpeedTestHostsHandler.start ();
        }
        new Thread (() -> {
            if (this == null)
                return;
            try {
                this.runOnUiThread (() -> tvBlink.setText ("Mencari Server Terbaik"));
            } catch (Exception e) {
                e.printStackTrace ();
            }
            int timeCount = 600;
            while (!getSpeedTestHostsHandler.isFinished ()) {
                timeCount--;
                try {
                    Thread.sleep (100);
                } catch (InterruptedException e) {
                    e.printStackTrace ();
                    Thread.currentThread ().interrupt ();
                }
                if (timeCount <= 0) {
                    if (this == null)
                        return;
                    try {
                        this.runOnUiThread (() -> {
                            tvBlink.clearAnimation ();
                            tvBlink.setVisibility (View.GONE);
                            tvBlink.setText ("Tidak ada koneksi...");
                            //                        tvBegin.setImageResource (R.drawable.ic_play);
                        });
                        getSpeedTestHostsHandler = null;
                        return;
                    } catch (Exception e) {
                        e.printStackTrace ();
                    }
                }
            }
            HashMap<Integer, String> mapKey = getSpeedTestHostsHandler.getMapKey ();
            HashMap<Integer, List<String>> mapValue = getSpeedTestHostsHandler.getMapValue ();
            double selfLat = getSpeedTestHostsHandler.getSelfLat ();
            double selfLon = getSpeedTestHostsHandler.getSelfLon ();
            double tmp = 19349458;
            double dist = 0.0;
            int findServerIndex = 0;
            for (int index : mapKey.keySet ()) {
                if (tempBlackList.contains (mapValue.get (index).get (5))) {
                    continue;
                }
                Location source = new Location ("Source");
                source.setLatitude (selfLat);
                source.setLongitude (selfLon);
                List<String> ls = mapValue.get (index);
                Location dest = new Location ("Dest");
                dest.setLatitude (Double.parseDouble (ls.get (0)));
                dest.setLongitude (Double.parseDouble (ls.get (1)));
                distance = source.distanceTo (dest);
                if (tmp > distance) {
                    tmp = distance;
                    dist = distance;
                    findServerIndex = index;
                }
            }
            uploadAddr = mapKey.get (findServerIndex);
            info = mapValue.get (findServerIndex);
            distance = dist;
            if (info != null) {
                if (info.size () > 0) {
                    this.runOnUiThread (() -> {
                        tvBlink.clearAnimation ();
                        tvBlink.setVisibility (View.VISIBLE);
                        tvBlink.setText (String.format ("Hosted by %s (%s) [%s km]", info.get (5), info.get (3), new DecimalFormat("#.##").format (distance / 1000)));
                    });
                    this.runOnUiThread (() -> {
                        tvPing.setText ("0");
                        tvDownload.setText ("0");
                        tvUpload.setText ("0");
                        i = 0f;
                        j = 0f;
                        k = 0f;
                        ivPBDownload.setAlpha (0.5f);
                        ivPBUpload.setAlpha (0.5f);
                    });
                    final List<Double> pingRateList = new ArrayList<> ();
                    final List<Double> downloadRateList = new ArrayList<> ();
                    final List<Double> uploadRateList = new ArrayList<> ();
                    Boolean pingTestStarted = false;
                    Boolean pingTestFinished = false;
                    Boolean downloadTestStarted = false;
                    Boolean downloadTestFinished = false;
                    Boolean uploadTestStarted = false;
                    Boolean uploadTestFinished = false;

                    final PingTest pingTest = new PingTest (info.get (6).replace (":8080", ""), 6);
                    final HttpDownloadTest downloadTest = new HttpDownloadTest (uploadAddr.replace (uploadAddr.split ("/")[ uploadAddr.split ("/").length - 1 ], ""));
                    final HttpUploadTest uploadTest = new HttpUploadTest (uploadAddr);
                    while (true) {
                        if (!pingTestStarted) {
                            pingTest.start ();
                            pingTestStarted = true;
                        }
                        if (pingTestFinished && !downloadTestStarted) {
                            downloadTest.start ();
                            downloadTestStarted = true;
                        }
                        if (downloadTestFinished && !uploadTestStarted) {
                            uploadTest.start ();
                            uploadTestStarted = true;
                        }
                        if (pingTestFinished) {
                            if (pingTest.getAvgRtt () == 0) {
                                Log.i ("TAG", "pingTest");
                            } else {
                                if (this == null)
                                    return;
                                try {
                                    this.runOnUiThread (() -> {
                                        tickProgressMeasure.setmPUnit ("ms");
                                        tvPing.setText (dec.format (pingTest.getAvgRtt ()) + "");
                                    });
                                } catch (Exception e) {
                                    e.printStackTrace ();
                                }
                            }
                        } else {
                            pingRateList.add (pingTest.getInstantRtt ());
                            try {
                                this.runOnUiThread (() -> {
                                    Log.i("TAG", "i = " + i);
                                    tickProgressMeasure.setmPUnit ("ms");
                                    tvPing.setText (dec.format (pingTest.getInstantRtt ()) + "");
                                    Log.i("PING", "" + pingTest.getInstantRtt ());
                                    tickProgressMeasure.setProgress ((int) (pingTest.getInstantRtt () * 100));
                                    if (i == 0) {
                                        lcMeasure.clear ();
                                        lineDataSet.clear ();
                                        lineDataSet.setColor (Color.rgb (255, 207, 223));
                                        lineData = new LineData (lineDataSet);
                                        lcMeasure.setData (lineData);
                                        lcMeasure.invalidate ();
                                    }
                                    if (i > 10) {
                                        LineData data = lcMeasure.getData ();
                                        LineDataSet set = (LineDataSet) data.getDataSetByIndex (0);
                                        if (set != null) {
                                            data.addEntry (new Entry (i, (float) (10 * pingTest.getInstantRtt ())), 0);
                                            lcMeasure.notifyDataSetChanged ();
                                            lcMeasure.setVisibleXRange (0, i);
                                            lcMeasure.invalidate ();
                                        }
                                    } else {
                                        lcMeasure.setVisibleXRange (0, 10);
                                        LineData data = lcMeasure.getData ();
                                        LineDataSet set = (LineDataSet) data.getDataSetByIndex (0);
                                        if (set != null) {
                                            data.addEntry (new Entry (i, (float) (10 * pingTest.getInstantRtt ())), 0);
                                            lcMeasure.notifyDataSetChanged ();
                                            lcMeasure.invalidate ();
                                        }
                                    }
                                    i++;
                                });
                            } catch (Exception e) {
                                e.printStackTrace ();
                            }
                        }
                        if (pingTestFinished) {
                            if (downloadTestFinished) {
                                if (downloadTest.getFinalDownloadRate () == 0) {
                                    Log.i ("TAG", "ping");
                                } else {
                                    try {
                                        this.runOnUiThread (() -> {
                                            tickProgressMeasure.setmPUnit (sharedPref.getString ("UNIT", "Mbps"));
                                            switch (sharedPref.getString ("UNIT", "Mbps")) {
                                                case "MBps":
                                                    tvDownload.setText (dec.format (0.125 * downloadTest.getFinalDownloadRate ()) + "");
                                                    break;
                                                case "kBps":
                                                    tvDownload.setText (dec.format (125 * downloadTest.getFinalDownloadRate ()) + "");
                                                    break;
                                                case "Mbps":
                                                    tvDownload.setText (dec.format (downloadTest.getFinalDownloadRate ()) + "");
                                                    break;
                                                case "kbps":
                                                    tvDownload.setText (dec.format (1000 * downloadTest.getFinalDownloadRate ()) + "");
                                                    break;
                                                default:
                                                    break;
                                            }
                                        });
                                    } catch (Exception e) {
                                        e.printStackTrace ();
                                    }
                                }
                            } else {
                                double downloadRate = downloadTest.getInstantDownloadRate ();
                                downloadRateList.add (downloadRate);
                                position = getPositionByRate (downloadRate);
                                try {
                                    this.runOnUiThread (() -> {
                                        Log.i("TAG", "j = " + j);
                                        tickProgressMeasure.setmPUnit (sharedPref.getString ("UNIT", "Mbps"));
                                        switch (sharedPref.getString ("UNIT", "Mbps")) {
                                            case "MBps":
                                                tvDownload.setText (dec.format (0.125 * downloadTest.getInstantDownloadRate ()) + "");
                                                tickProgressMeasure.setProgress ((int) (0.125 * downloadTest.getInstantDownloadRate () * 100));
                                                break;
                                            case "kBps":
                                                tvDownload.setText (dec.format (125 * downloadTest.getInstantDownloadRate ()) + "");
                                                tickProgressMeasure.setProgress ((int) (125 * downloadTest.getInstantDownloadRate () * 100));
                                                break;
                                            case "Mbps":
                                                tvDownload.setText (dec.format (downloadTest.getInstantDownloadRate ()) + "");
                                                tickProgressMeasure.setProgress ((int) (downloadTest.getInstantDownloadRate () * 100));
                                                break;
                                            case "kbps":
                                                tvDownload.setText (dec.format (1000 * downloadTest.getInstantDownloadRate ()) + "");
                                                tickProgressMeasure.setProgress ((int) (1000 * downloadTest.getInstantDownloadRate () * 100));
                                                break;
                                            default:
                                                break;
                                        }
                                        Log.i("DOWNLOAD", "" + downloadTest.getInstantDownloadRate ());
                                        if (j == 0) {
                                            ivPBDownload.setAlpha (1.0f);
                                            ivPBUpload.setAlpha (0.5f);
                                            lcMeasure.clear ();
                                            lineDataSet.clear ();
                                            lineDataSet.setColor (Color.rgb (224, 249, 181));
                                            lineData = new LineData (lineDataSet);
                                            lcMeasure.setData (lineData);
                                            lcMeasure.invalidate ();
                                        }
                                        if (j > 100) {
                                            LineData data = lcMeasure.getData ();
                                            LineDataSet set = (LineDataSet) data.getDataSetByIndex (0);
                                            if (set != null) {
                                                data.addEntry (new Entry (j, (float) (1000 * downloadTest.getInstantDownloadRate ())), 0);
                                                lcMeasure.notifyDataSetChanged ();
                                                lcMeasure.setVisibleXRange (0, j);
                                                lcMeasure.invalidate ();
                                            }
                                        } else {
                                            lcMeasure.setVisibleXRange (0, 100);
                                            LineData data = lcMeasure.getData ();
                                            LineDataSet set = (LineDataSet) data.getDataSetByIndex (0);
                                            if (set != null) {
                                                data.addEntry (new Entry (j, (float) (1000 * downloadTest.getInstantDownloadRate ())), 0);
                                                lcMeasure.notifyDataSetChanged ();
                                                lcMeasure.invalidate ();
                                            }
                                        }
                                        j++;
                                    });
                                    lastPosition = position;
                                } catch (Exception e) {
                                    e.printStackTrace ();
                                }
                            }
                        }
                        if (downloadTestFinished) {
                            if (uploadTestFinished) {
                                if (uploadTest.getFinalUploadRate () == 0) {
                                    Log.i("TAG", "dowload");
                                    //btnCekSpeed.setEnabled(true);
                                } else {
                                    if (this == null)
                                        return;
                                    try {
                                        this.runOnUiThread (() -> {
                                            tickProgressMeasure.setmPUnit (sharedPref.getString ("UNIT", "Mbps"));
                                            switch (sharedPref.getString ("UNIT", "Mbps")) {
                                                case "MBps":
                                                    tvUpload.setText (String.format ("%.1f", dec.format (0.125 * uploadTest.getFinalUploadRate ())));
                                                    break;
                                                case "kBps":
                                                    tvUpload.setText (String.format ("%.1f", dec.format (125 * uploadTest.getFinalUploadRate ())));
                                                    break;
                                                case "Mbps":
                                                    tvUpload.setText (String.format ("%.1f", dec.format (uploadTest.getFinalUploadRate ())));
                                                    break;
                                                case "kbps":
                                                    tvUpload.setText (String.format ("%.1f", dec.format (1000 * uploadTest.getFinalUploadRate ())));
                                                    break;
                                                default:
                                                    Log.i("TAG", "ERROR");
                                                    break;
                                            }
                                        });
                                    } catch (Exception e) {
                                        e.printStackTrace ();
                                    }
                                }
                            } else {
                                double uploadRate = uploadTest.getInstantUploadRate ();
                                uploadRateList.add (uploadRate);
                                position = getPositionByRate (uploadRate);
                                try {
                                    this.runOnUiThread (() -> {
                                        tickProgressMeasure.setmPUnit (sharedPref.getString ("UNIT", "Mbps"));
                                        switch (sharedPref.getString ("UNIT", "Mbps")) {
                                            case "MBps":
                                                tvUpload.setText (dec.format (0.125 * uploadTest.getInstantUploadRate ()) + "");
                                                tickProgressMeasure.setProgress ((int) (0.125 * uploadTest.getInstantUploadRate () * 100));
                                                break;
                                            case "kBps":
                                                tvUpload.setText (dec.format (125 * uploadTest.getInstantUploadRate ()) + "");
                                                tickProgressMeasure.setProgress ((int) (125 * uploadTest.getInstantUploadRate () * 100));
                                                break;
                                            case "Mbps":
                                                tvUpload.setText (dec.format (uploadTest.getInstantUploadRate ()) + "");
                                                tickProgressMeasure.setProgress ((int) (uploadTest.getInstantUploadRate () * 100));
                                                break;
                                            case "kbps":
                                                tvUpload.setText (dec.format (1000 * uploadTest.getInstantUploadRate ()) + "");
                                                tickProgressMeasure.setProgress ((int) (1000 * uploadTest.getInstantUploadRate () * 100));
                                                break;
                                            default:
                                                //LOGE ("TAG", "ERROR");
                                                break;
                                        }
                                        Log.i("TAG", "k = " + k);
                                        Log.i("UPLOAD", "" + uploadTest.getInstantUploadRate ());
                                        if (k == 0) {
                                            ivPBDownload.setAlpha (1.0f);
                                            ivPBUpload.setAlpha (1.0f);
                                            lcMeasure.clear ();
                                            lineDataSet.clear ();
                                            lineDataSet.setColor (Color.rgb (145, 174, 210));
                                            lineData = new LineData (lineDataSet);
                                            lcMeasure.setData (lineData);
                                            lcMeasure.invalidate ();
                                        }
                                        if (k > 100) {
                                            LineData data = lcMeasure.getData ();
                                            LineDataSet set = (LineDataSet) data.getDataSetByIndex (0);
                                            if (set != null) {
                                                data.addEntry (new Entry (k, (float) (1000 * uploadTest.getInstantUploadRate ())), 0);
                                                lcMeasure.notifyDataSetChanged ();
                                                lcMeasure.setVisibleXRange (0, k);
                                                lcMeasure.invalidate ();
                                            }
                                        } else {
                                            lcMeasure.setVisibleXRange (0, 100);
                                            LineData data = lcMeasure.getData ();
                                            LineDataSet set = (LineDataSet) data.getDataSetByIndex (0);
                                            if (set != null) {
                                                data.addEntry (new Entry (k, (float) (1000 * uploadTest.getInstantUploadRate ())), 0);
                                                lcMeasure.notifyDataSetChanged ();
                                                lcMeasure.invalidate ();
                                            }
                                        }
                                        k++;
                                    });
                                } catch (Exception e) {
                                    e.printStackTrace ();
                                }
                                lastPosition = position;
                            }
                        }
                        if (pingTestFinished && downloadTestFinished && uploadTest.isFinished ()) {
                            break;
                        }
                        if (pingTest.isFinished ()) {
                            pingTestFinished = true;
                        }
                        if (downloadTest.isFinished ()) {
                            downloadTestFinished = true;
                        }
                        if (uploadTest.isFinished ()) {
                            btnCekSpeed.setEnabled(true);
                            uploadTestFinished = true;
                        }
                        if (pingTestStarted && !pingTestFinished) {
                            try {
                                Thread.sleep (300);
                            } catch (InterruptedException e) {
                                e.printStackTrace ();
                                Thread.currentThread ().interrupt ();
                            }
                        } else {
                            try {
                                Thread.sleep (100);
                            } catch (InterruptedException e) {
                                e.printStackTrace ();
                                Thread.currentThread ().interrupt ();
                            }
                        }
                    }
                    try {
                        this.runOnUiThread (() -> {
                            btnCekSpeed.setEnabled(true);
                            btnCekSpeed.setText("Cek Speed Lagi");
                            btnCekSpeed.setBackgroundColor(this.getResources().getColor(R.color.ratedialog));
                            Toast.makeText(this, "Cek speed selesai", Toast.LENGTH_SHORT).show();
                            // tvBegin.setImageResource (R.drawable.ic_play);
                            Log.i("TAG", "test1");
                            SharedPreferences sharedPrefHistory = this.getSharedPreferences ("historydata", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPrefHistory.edit ();
                            String _data = sharedPrefHistory.getString ("DATA", "");
                            if (!_data.equals ("")) {
                                 Log.i("TAG", "1");
                                JSONObject jsondata = new JSONObject ();
                                try {
                                    jsondata.put ("date", String.valueOf (System.currentTimeMillis ()));
                                    jsondata.put ("ping", tvPing.getText ());
                                    jsondata.put ("download", tvDownload.getText ());
                                    jsondata.put ("upload", tvUpload.getText ());
                                    Log.i ("TAG", _data);
                                    JSONObject js = new JSONObject (_data);
                                    JSONArray array = js.getJSONArray (getString (R.string.history));
                                    array.put (jsondata);
                                    JSONObject new_data = new JSONObject ();
                                    new_data.put (getString (R.string.history), array);
                                    editor.remove ("DATA");
                                    editor.putString ("DATA", new_data.toString ());
                                    editor.apply ();
                                } catch (JSONException e) {
                                    e.printStackTrace ();
                                }
                            } else {
                                Log.i ("TAG", "2");
                                JSONObject jsondata = new JSONObject ();
                                try {
                                    jsondata.put ("date", String.valueOf (System.currentTimeMillis ()));
                                    jsondata.put ("ping", tvPing.getText ());
                                    jsondata.put ("download", tvDownload.getText ());
                                    jsondata.put ("upload", tvUpload.getText ());
                                    JSONArray array = new JSONArray ();
                                    array.put (jsondata);
                                    JSONObject new_data = new JSONObject ();
                                    new_data.put ("History", array);
                                    editor.putString ("DATA", new_data.toString ());
                                    editor.apply ();
                                    // testing = false;
                                } catch (JSONException e) {
                                    e.printStackTrace ();
                                }
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace ();
                        btnCekSpeed.setEnabled(true);
                        btnCekSpeed.setText("Cek Speed Lagi");
                        btnCekSpeed.setBackgroundColor(this.getResources().getColor(R.color.ratedialog));
                    }

                }
            } else {
                btnCekSpeed.setEnabled(true);
                btnCekSpeed.setText("Cek Speed Lagi");
                btnCekSpeed.setBackgroundColor(this.getResources().getColor(R.color.ratedialog));
                //  tvBegin.setImageResource (R.drawable.ic_play);
                Log.i("TAG", "test22");
            }
        }).start ();

    }

    private void cekForUpdate() {
        mAppUpdateManager = AppUpdateManagerFactory.create(this);
        mAppUpdateManager.registerListener(installStateUpdatedListener);
        mAppUpdateManager.getAppUpdateInfo().addOnSuccessListener(appUpdateInfo -> {

            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                    && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE /*AppUpdateType.IMMEDIATE*/)) {

                try {
                    mAppUpdateManager.startUpdateFlowForResult(
                            appUpdateInfo, AppUpdateType.FLEXIBLE /*AppUpdateType.IMMEDIATE*/, MainActivity.this, RC_APP_UPDATE);

                } catch (IntentSender.SendIntentException e) {
                    e.printStackTrace();
                }

            } else if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                //CHECK THIS if AppUpdateType.FLEXIBLE, otherwise you can skip
                popupSnackbarForCompleteUpdate();
            } else {
                Log.e("TAG", "checkForAppUpdateAvailability: something else");
            }
        });
    }

    private void setTypeNetwork() {
        // ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        @SuppressLint("MissingPermission") NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null) { // connected to the internet
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                tvTypeCon.setText("WIFI");
            } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                tvTypeCon.setText("MOBILE");
            } else {
                tvTypeCon.setText("OFF");
            }
        }
    }

    InstallStateUpdatedListener installStateUpdatedListener = new
            InstallStateUpdatedListener() {
                @Override
                public void onStateUpdate(InstallState state) {
                    if (state.installStatus() == InstallStatus.DOWNLOADED) {
                        //CHECK THIS if AppUpdateType.FLEXIBLE, otherwise you can skip
                        popupSnackbarForCompleteUpdate();
                    } else if (state.installStatus() == InstallStatus.INSTALLED) {
                        if (mAppUpdateManager != null) {
                            mAppUpdateManager.unregisterListener(installStateUpdatedListener);
                        }

                    } else {
                        Log.i("TAG", "InstallStateUpdatedListener: state: " + state.installStatus());
                    }
                }
            };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_APP_UPDATE) {
            if (resultCode != RESULT_OK) {
                Log.e("TAG", "onActivityResult: app download failed");
            }
        }
    }

    private void popupSnackbarForCompleteUpdate() {
        Snackbar snackbar = Snackbar.make(findViewById(R.id.layoutMain),
                "Versi baru!",
                Snackbar.LENGTH_INDEFINITE);

        snackbar.setAction("Install", view -> {
            if (mAppUpdateManager != null) {
                mAppUpdateManager.completeUpdate();
            }
        });
        snackbar.setActionTextColor(getResources().getColor(R.color.design_default_color_secondary));
        snackbar.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.tutorial:
                Intent intent = new Intent(this, TutorialActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                return true;

            case R.id.history:
                Intent history = new Intent(this, HistoryActivity.class);
                startActivity(history);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                return true;

            case R.id.keluar:
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onResume () {
        super.onResume ();
        getSpeedTestHostsHandler = new GetSpeedTestHostsHandler ();
        getSpeedTestHostsHandler.start ();
        if (tvDownloadU != null)
            tvDownloadU.setText (sharedPref.getString ("UNIT", "Mbps"));
        if (tvUploadU != null)
            tvUploadU.setText (sharedPref.getString ("UNIT", "Mbps"));
    }

    @Override
    protected void onStart() {
        cekForUpdate();
        super.onStart();
    }

    @Override
    protected void onStop() {
        if (mAppUpdateManager != null) {
            mAppUpdateManager.unregisterListener(installStateUpdatedListener);
        }
        super.onStop();
    }
}