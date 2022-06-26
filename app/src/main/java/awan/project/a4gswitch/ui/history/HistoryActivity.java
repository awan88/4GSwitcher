package awan.project.a4gswitch.ui.history;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.ads.AdView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import awan.project.a4gswitch.R;
import awan.project.a4gswitch.model.DataInfo;

public class HistoryActivity extends AppCompatActivity {


    List<DataInfo> monthData;

    private DataAdapter dataAdapter;
    private RecyclerView recList;
    private TextView tvDate;
    private TextView tvPing;
    private TextView tvDownload;
    private TextView tvUpload;


    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Riwayat Kecepatan Internet");
        tvDate = findViewById (R.id.tv_date);
        tvPing = findViewById (R.id.tv_ping);
        tvDownload = findViewById (R.id.tv_download);
        tvUpload = findViewById (R.id.tv_upload);


        init ();
        Random random = new Random ();
        int l = random.nextInt (2);
        Log.e ("newactvitiy", "newactivity" + l);

//        MobileAds.initialize(this, initializationStatus -> { });
//        AdRequest adRequest = new AdRequest.Builder().build();
//        mAdView.loadAd(adRequest);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void init () {
        tvDate.post (() -> {
            int length = tvDate.getMeasuredWidth ();
            float angle = 45;
            Shader textShader = new LinearGradient(0, 0, (int) (Math.sin (Math.PI * angle / 180) * length),
                    (int) (Math.cos (Math.PI * angle / 180) * length),
                    new int[] {0xFF30E3CA, 0xFFa5dee5},
                    null,
                    Shader.TileMode.CLAMP);
            tvDate.getPaint ().setShader (textShader);
            tvDate.invalidate ();
        });
        tvPing.post (() -> {
            int length = tvPing.getMeasuredWidth ();
            float angle = 45;
            Shader textShader = new LinearGradient (0, 0, (int) (Math.sin (Math.PI * angle / 180) * length),
                    (int) (Math.cos (Math.PI * angle / 180) * length),
                    new int[] {0xFF30E3CA, 0xFFa5dee5},
                    null,
                    Shader.TileMode.CLAMP);
            tvPing.getPaint ().setShader (textShader);
            tvPing.invalidate ();
        });
        tvDownload.post (() -> {
            int length = tvDownload.getMeasuredWidth ();
            float angle = 45;
            Shader textShader = new LinearGradient (0, 0, (int) (Math.sin (Math.PI * angle / 180) * length),
                    (int) (Math.cos (Math.PI * angle / 180) * length),
                    new int[] {0xFF30E3CA, 0xFFa5dee5},
                    null,
                    Shader.TileMode.CLAMP);
            tvDownload.getPaint ().setShader (textShader);
            tvDownload.invalidate ();
        });
        tvUpload.post (() -> {
            int length = tvUpload.getMeasuredWidth ();
            float angle = 45;
            Shader textShader = new LinearGradient (0, 0, (int) (Math.sin (Math.PI * angle / 180) * length),
                    (int) (Math.cos (Math.PI * angle / 180) * length),
                    new int[] {0xFF30E3CA, 0xFFa5dee5},
                    null,
                    Shader.TileMode.CLAMP);
            tvUpload.getPaint ().setShader (textShader);
            tvUpload.invalidate ();
        });
        recList = findViewById (R.id.cardList);
        recList.setHasFixedSize (true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this.getApplicationContext());
        recList.setLayoutManager (layoutManager);
        monthData = createList ();
        dataAdapter = new DataAdapter (this, monthData);
        recList.setAdapter (dataAdapter);
        dataAdapter.notifyDataSetChanged();
    }

    private List<DataInfo> createList() {
        List<DataInfo> result = new ArrayList<>();
        SharedPreferences sharedPref = this.getSharedPreferences ("historydata", Context.MODE_PRIVATE);
        String _data = sharedPref.getString ("DATA", "");
        if (!_data.equals ("")) {
            Log.i  ("TAG", "1");
            JSONObject js;
            try {
                js = new JSONObject (_data);
                JSONArray array = js.getJSONArray ("History");
                if (array.length () <= 30) {
                    Log.i  ("TAG", "2");
                    for (int i = array.length () - 1 ; i >= 0 ; i--) {
                        JSONObject jo = array.getJSONObject (i);
                        Log.i("jesonCek", jo.getString("date"));
                        result.add (new DataInfo (jo.getLong ("date"), jo.getString ("ping"), jo.getString ("download"), jo.getString ("upload")));
                    }
                    if (array.length () != 30) {
                        for (int i = 0; i < (30 - array.length ()) ; i++) {
                            result.add (new DataInfo ());
                        }
                    }
                } else {
                    Log.i  ("TAG", "3");
                    int count = 0;
                    while (count <= 30) {
                        JSONObject jo = array.getJSONObject ((array.length () - 1) - count);
                        result.add (new DataInfo (jo.getLong ("date"), jo.getString ("ping"), jo.getString ("download"), jo.getString ("upload")));
                        count++;
                    }
                }
            } catch (JSONException e) {
                Log.i  ("TAG", "ERROR" + e.getMessage ());
            }
        } else {
            Log.i  ("TAG", "4");
            for (int i = 0; i < 30; i++) {
                result.add (new DataInfo ());
            }
        }
        Log.i  ("TAG", "list added");
        return result;
    }

    @Override
    public void onPause () {
        super.onPause ();
    }

    @Override
    public void onResume () {
        super.onResume ();
        Log.i ("TAG", "resume");
        monthData = createList ();
        dataAdapter = new DataAdapter (this, monthData);
        recList.setAdapter (dataAdapter);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onDestroy () {
        super.onDestroy ();
    }

    @Override
    public void onStart () {
        super.onStart ();
    }

    @Override
    public void onStop () {
        super.onStop ();
    }
}