package awan.project.a4gswitch.ui.history;

import android.app.Activity;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import awan.project.a4gswitch.R;
import awan.project.a4gswitch.model.DataInfo;

/**
 * This class awan.project.a4gswitch.ui.history
 * Created by Awan on 15/01/2022.
 * Github github.com/awan88
 */


public class DataAdapter extends RecyclerView.Adapter<DataAdapter.DataViewHolder> {

    public List<DataInfo> dataList;
    Activity context;

    public DataAdapter (Activity context, List<DataInfo> dataList) {
        this.dataList = dataList;
        this.context = context;
    }

    @NonNull
    @Override
    public DataViewHolder onCreateViewHolder (ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from (parent.getContext ()).inflate (R.layout.card_layout, parent, false);
        return new DataViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder (DataViewHolder holder, int position) {

        DataInfo di = dataList.get (position);
        SimpleDateFormat formatter = new SimpleDateFormat (context.getString (R.string.date_format), Locale.getDefault());
        Calendar calendar = Calendar.getInstance ();
        Timestamp ts = new Timestamp(di.getDate());
        Date date = new Date(ts.getTime());
        calendar.setTimeInMillis (di.getDate ());

        holder.vDate.setText (formatter.format(date));
        holder.vWifi.setText (String.valueOf (di.getPing ()));
        holder.vMobile.setText (String.valueOf (di.getDownload ()));
        holder.vTotal.setText (String.valueOf (di.getUpload ()));
        if (position % 2 == 0) {
            holder.card_view.setBackgroundColor (context.getResources ().getColor (R.color.white_10));
        } else {
            holder.card_view.setBackgroundColor (context.getResources ().getColor (android.R.color.transparent));
        }

        if(position %8 == 0){
            holder.adContainerView.setVisibility(View.VISIBLE);
            loadAds(holder.adContainerView);
        }else {
            holder.adContainerView.setVisibility(View.GONE);
        }
    }

    private void loadAds(FrameLayout adContainerView) {
        MobileAds.initialize(context, initializationStatus -> {});
        adContainerView.post(() -> loadBanner(adContainerView));
    }

    private void loadBanner(FrameLayout adContainerView) {
        AdView adView = new AdView(context);
        adView.setAdUnitId(context.getString(R.string.unitIDBannerAds));
        adContainerView.removeAllViews();
        adContainerView.addView(adView);

        AdSize adSize = getAdSize(adContainerView);
        adView.setAdSize(adSize);

        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
    }

    private AdSize getAdSize(FrameLayout adContainerView) {
        Display display = context.getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        float density = outMetrics.density;

        float adWidthPixels = adContainerView.getWidth();

        // If the ad hasn't been laid out, default to the full screen width.
        if (adWidthPixels == 0) {
            adWidthPixels = outMetrics.widthPixels;
        }

        int adWidth = (int) (adWidthPixels / density);
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, adWidth);
    }

    @Override
    public int getItemCount () {
        return dataList.size ();
    }


    static class DataViewHolder extends RecyclerView.ViewHolder {

        TextView vDate;
        TextView vWifi;
        TextView vMobile;
        TextView vTotal;
        FrameLayout adContainerView;
        ConstraintLayout card_view;

        public DataViewHolder (View itemView) {
            super (itemView);
            vDate = itemView.findViewById (R.id.id_date);
            vWifi = itemView.findViewById (R.id.id_wifi);
            vMobile = itemView.findViewById (R.id.mobile);
            vTotal = itemView.findViewById (R.id.total);
            card_view = itemView.findViewById (R.id.card_view);
            adContainerView = itemView.findViewById(R.id.ad_view_container);
        }
    }
}
