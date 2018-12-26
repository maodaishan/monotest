package com.maods.monotest;

import android.support.annotation.NonNull;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by MAODS on 2018/12/12.
 */

public class City implements Comparable{
    private static final String TAG="City";
    public int mCityNum;
    public String mOwner;
    public double mPrice;
    public double mRent;
    public int mLastModified;
    public String mLabel;
    public String mImg;
    public String mUrl;
    public String mName;
    public boolean mAvailable=false;
    public City(JSONObject json){
        try {
            mCityNum = json.getInt("city_num");
            mOwner=json.getString("owner");
            String price=json.getString("price");
            price=price.substring(0,price.length()-4);
            mPrice=Double.parseDouble(price);
            String rent=json.getString("rent");
            rent=rent.substring(0,rent.length()-4);
            mRent=Double.parseDouble(rent);
            mLastModified=json.getInt("last_modified");
            mLabel=json.getString("label");
            mImg=json.getString("img");
            mUrl=json.getString("url");
            mName=Utils.getCityName(mCityNum);
            mAvailable=true;
        }catch(JSONException e){
            Log.e(TAG,e.toString());
        }
    }
    //just for not bourght city,default values
    public City(int i){
        mCityNum=i;
        mOwner="monopolygame";
        mPrice=1.0000;
        mRent=0;
        mLastModified=-1;
        mLabel="";
        mImg="";
        mUrl="";
        mName=Utils.getCityName(i);
        mAvailable=true;
    }
    @Override
    public String toString(){
        StringBuilder sb=new StringBuilder();
        sb.append("城市编号："+mCityNum+"\n");
        sb.append("城市名称："+mName+"\n");
        sb.append("城市主人："+mOwner+"\n");
        sb.append("当前价格："+mPrice+"\n");
        sb.append("城市租金："+mRent+"\n");
        Date date = new Date(mLastModified);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String dateStr= format.format(date);
        sb.append("上次交易时间："+dateStr+"\n");
        sb.append("标签："+mLabel+"\n");
        sb.append("Logo："+mImg+"\n");
        sb.append("Url:"+mUrl+"\n");
        return sb.toString();
    }

    @Override
    public int compareTo(@NonNull Object o) {
        City other=(City)o;
        if(mCityNum>other.mCityNum){
            return 1;
        }else{
            return -1;
        }
    }
}
