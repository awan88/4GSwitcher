package awan.project.a4gswitch.model;

import android.util.Log;

/**
 * This class awan.project.a4gswitch.model
 * Created by Awan on 15/01/2022.
 * Github github.com/awan88
 */


public class DataInfo{
    private long date;
    private String ping;
    private String download;
    private String upload;

    public DataInfo () {
        this.date = 1577833200000L;
        this.ping = "0";
        this.download = "0";
        this.upload = "0";
    }

    public DataInfo (long date, String ping, String download, String upload) {
        this.date = date;
        this.ping = ping;
        this.download = download;
        this.upload = upload;
        Log.i ("TAG", "value added");
    }

    public long getDate () {
        return date;
    }
    public void setDate (long date) {
        this.date = date;
    }
    public String getPing () {
        return ping;
    }
    public void setPing (String ping) {
        this.ping = ping;
    }
    public String getDownload () {
        return download;
    }
    public void setDownload (String download) {
        this.download = download;
    }
    public String getUpload () {
        return upload;
    }
    public void setUpload (String upload) {
        this.upload = upload;
    }
}
