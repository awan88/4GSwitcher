package awan.project.a4gswitch.koneksi;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.Snackbar;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import awan.project.a4gswitch.R;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class DownloadInfoActivity extends AppCompatActivity {

    private final OkHttpClient client = new OkHttpClient();
    private String TAG = "TagDownload";
    private long startTime;
    private long endTime;
    private long fileSize;
    private View mRunningBar;
    private TextView timeTaken, kbPerSec, downloadSpeed, fSize, bandwidthType;
    String downloadURL = "https://firebasestorage.googleapis.com/v0/b/g-switch-9c4f0.appspot.com/o/7782_f.jpg?alt=media&token=aac67e34-03b4-4edd-bf15-a180252384ae";
    private int POOR_BANDWIDTH = 150;
    private int AVERAGE_BANDWIDTH = 550;
    private int GOOD_BANDWIDTH = 2000;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_info);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        findViewById(R.id.test_download_btn).setOnClickListener(testButtonClicked);
        mRunningBar = findViewById(R.id.download_running_bar);
        timeTaken = findViewById(R.id.time_taken);
        kbPerSec = findViewById(R.id.kilobyte_per_sec);
        downloadSpeed = findViewById(R.id.download_speed);
        fSize = findViewById(R.id.file_size);
        bandwidthType = findViewById(R.id.bandwidth_type);


    }

    private final View.OnClickListener testButtonClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            if(cm.getActiveNetworkInfo().isConnected()) {
                downloadInfo();  // call downloadInfo to perform the download request
                //downloadInfo2();
            } else {
                // display snack bar message
                String msg = getResources().getString(R.string.connection_error);
                Snackbar snack = Snackbar.make(v, msg, Snackbar.LENGTH_LONG).setAction("Action", null);
                ViewGroup group = (ViewGroup) snack.getView();
                group.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.error));
                snack.show();
            }
        }
    };

    private void downloadInfo(){

        Request request = new Request.Builder()
                .url(downloadURL) // replace image url
                .build();

        mRunningBar.setVisibility(View.VISIBLE);
        bandwidthType.setText("");
        startTime = System.currentTimeMillis();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                Headers responseHeaders = response.headers();
                for (int i = 0, size = responseHeaders.size(); i < size; i++) {
                    Log.d(TAG, responseHeaders.name(i) + ": " + responseHeaders.value(i));
                }

                InputStream input = response.body().byteStream();

                try {
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    byte[] buffer = new byte[1024];

                    while (input.read(buffer) != -1) {
                        bos.write(buffer);
                    }
                    byte[] docBuffer = bos.toByteArray();
                    fileSize = bos.size();

                } finally {
                    input.close();
                }

                endTime = System.currentTimeMillis();

                // calculate how long it took by subtracting endtime from starttime

                final double timeTakenMills = Math.floor(endTime - startTime);  // time taken in milliseconds
                final double timeTakenInSecs = timeTakenMills / 1000 % 60;  // divide by 1000 to get time in seconds
                final int kilobytePerSec = (int) Math.round(1024 / timeTakenInSecs);
                final double speed = Math.round(fileSize / timeTakenMills);

                Log.d(TAG, "Time taken in secs: " + timeTakenInSecs);
                Log.d(TAG, "Kb per sec: " + kilobytePerSec);
                Log.d(TAG, "Download Speed: " + speed);
                Log.d(TAG, "File size in kb: " + fileSize);

                runOnUiThread(() -> {
                    mRunningBar.setVisibility(View.GONE);
                    timeTaken.setText("= " + timeTakenInSecs + " detik");
                    kbPerSec.setText("= " + kilobytePerSec + " kb/s");
                    fSize.setText("= " + fileSize / 1024 + " kb");
                    downloadSpeed.setText("= " + speed+ " kb");


                    if(kilobytePerSec <= POOR_BANDWIDTH){
                        // slow connection
                        bandwidthType.setText("= " + getResources().getString(R.string.poor_bandwidth));

                    } else if (kilobytePerSec > POOR_BANDWIDTH && kilobytePerSec <= AVERAGE_BANDWIDTH){
                        // Average connection
                        bandwidthType.setText("= " + getResources().getString(R.string.average_bandwidth));

                    } else if (kilobytePerSec > AVERAGE_BANDWIDTH && kilobytePerSec <= GOOD_BANDWIDTH){
                        // Fast connection
                        bandwidthType.setText("= " + getResources().getString(R.string.good_bandwidth));

                    }

                });



            }
        });



    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}