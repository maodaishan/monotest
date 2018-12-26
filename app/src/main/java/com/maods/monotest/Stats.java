package com.maods.monotest;

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by MAODS on 2018/12/17.
 */

public class Stats {
    private static final String TAG="stats";
    public double mPool;
    public long mLastModified;
    public String mFormatedLastModified;
    public boolean mAvailable=false;
    public Stats(String input){
        if(TextUtils.isEmpty(input)){
            return;
        }
        try {
            JSONObject jsonInput = new JSONObject(input);
            if (jsonInput == null) {
                return;
            }
            JSONArray statses = jsonInput.getJSONArray("rows");
            if(statses.length()>0){
                JSONObject stats=statses.getJSONObject(0);
                String pool=stats.getString("pool");
                pool=pool.substring(0,pool.length()-4);
                mPool=Double.parseDouble(pool);
                mLastModified=stats.getInt("last_modified");
                mLastModified=mLastModified*1000;// contract is using second
                Date date = new Date(mLastModified);
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                mFormatedLastModified= format.format(date);
                mAvailable=true;
            }
        }catch(JSONException e){
            Log.e(TAG,e.toString());
        }
    }
}
