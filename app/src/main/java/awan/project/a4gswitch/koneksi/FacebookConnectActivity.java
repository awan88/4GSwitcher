package awan.project.a4gswitch.koneksi;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.facebook.network.connectionclass.ConnectionClassManager;
import com.facebook.network.connectionclass.ConnectionQuality;
import com.facebook.network.connectionclass.DeviceBandwidthSampler;
import com.google.android.material.snackbar.Snackbar;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import awan.project.a4gswitch.R;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class FacebookConnectActivity extends AppCompatActivity {

    private final OkHttpClient client = new OkHttpClient();
    private ConnectionQuality mConnectionClass = ConnectionQuality.UNKNOWN;
    private ConnectionClassManager mConnectionClassManager;
    private DeviceBandwidthSampler mDeviceBandwidthSampler;
    private ConnectionChangedListener mListener;

    private int mTries = 0;
    private TextView mTextView;
    private View mRunningBar;

    private String TAG = this.getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_facebook_connect);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mConnectionClassManager = ConnectionClassManager.getInstance();
        mDeviceBandwidthSampler = DeviceBandwidthSampler.getInstance();
        Button btn = findViewById(R.id.test_btn);
        btn.setOnClickListener(testButtonClicked);
        mRunningBar = findViewById(R.id.runningBar);

    }

    public void checkNetworkQuality(){

        /*Request request = new Request.Builder()
                .url("https://scontent-sin6-2.xx.fbcdn.net/v/t1.0-9/126102317_3355965181167906_5379772951142964292_n.jpg?_nc_cat=103&ccb=2&_nc_sid=8bfeb9&_nc_eui2=AeFEmfTh7kVKTKe4l9tYncLu9bb3_5xy9gD1tvf_nHL2AMJfCSrgTc9Te8QBjAjWY5OueeWKKa5-9fIYLbgY1tyw&_nc_ohc=o-oySoTA8wEAX-jNNzX&_nc_ht=scontent-sin6-2.xx&oh=aef674b25e98a62779dfba83abc90b20&oe=5FF4BDEF") // replace image url
                .build();*/

        /*Request request = new Request.Builder()
                .url("https://i.pinimg.com/originals/4c/c4/75/4cc4754f5caab172e1c9aa32955d1cfd.jpg") // replace image url
                .build();*/

        Request request = new Request.Builder()
                .url("") // replace image url
                .build();

        mRunningBar.setVisibility(View.VISIBLE);
        mDeviceBandwidthSampler.startSampling();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
                mDeviceBandwidthSampler.stopSampling();
                // Retry for up to 10 times until we find a ConnectionClass.
                if (mConnectionClass == ConnectionQuality.UNKNOWN && mTries < 10) {
                    mTries++;
                    checkNetworkQuality();
                }
                if (!mDeviceBandwidthSampler.isSampling()) {
                    mRunningBar.setVisibility(View.GONE);
                }
            }



            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {

                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                Headers responseHeaders = response.headers();
                for (int i = 0, size = responseHeaders.size(); i < size; i++) {
                    Log.d(TAG, responseHeaders.name(i) + ": " + responseHeaders.value(i));
                }

                Log.d(TAG, response.body().string());
                Log.d(TAG, mConnectionClassManager.getCurrentBandwidthQuality().toString());

                mDeviceBandwidthSampler.stopSampling();
            }
        });



        mTextView = (TextView)findViewById(R.id.connection_class);
        mTextView.setText(mConnectionClassManager.getCurrentBandwidthQuality().toString());
        mRunningBar = findViewById(R.id.runningBar);
        mRunningBar.setVisibility(View.GONE);

        mListener = new ConnectionChangedListener();
    }


    @Override
    protected void onPause() {
        super.onPause();
        mConnectionClassManager.remove(mListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mConnectionClassManager.register(mListener);
    }



    // Listener to update the UI upon connectionclass change.
    private class ConnectionChangedListener
            implements ConnectionClassManager.ConnectionClassStateChangeListener {

        @Override
        public void onBandwidthStateChange(ConnectionQuality bandwidthState) {
            mConnectionClass = bandwidthState;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // do something
                    mTextView.setText(mConnectionClass.toString());
                }
            });
        }
    }

    private final View.OnClickListener testButtonClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            if(cm.getActiveNetworkInfo() != null  && cm.getActiveNetworkInfo().isConnected()){

                checkNetworkQuality(); // call downloadInfo to perform the download request

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}