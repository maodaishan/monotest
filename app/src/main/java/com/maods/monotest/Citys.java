package com.maods.monotest;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by MAODS on 2018/12/12.
 */

public class Citys{
    private static final String TAG="citys";
    public ArrayList<City> mCitys=new ArrayList<City>();
    public boolean mAvailable=false;

    public Citys(String input){
        if(TextUtils.isEmpty(input)){
            return;
        }
        for(int i=0;i<100;i++){
            City cityDefault=new City(i);
            mCitys.add(cityDefault);
        }
        try{
            JSONObject jsonInput=new JSONObject(input);
            if(jsonInput==null){
                return;
            }
            JSONArray citys=jsonInput.getJSONArray("rows");
            if(citys.length()>0){
                for(int i=0;i<citys.length();i++){
                    JSONObject cityJson=citys.getJSONObject(i);
                    City city=new City(cityJson);
                    if(city.mAvailable){
                        mCitys.remove(city.mCityNum);
                        mCitys.add(city.mCityNum,city);
                    }
                }
            }
            mAvailable=true;
            //Collections.sort(mCitys);
        }catch(JSONException e){
            Log.e(TAG,e.toString());
        }
    }

    public City getCity(int num){
        if(!mAvailable){
            return null;
        }
        return mCitys.get(num);
    }

    public int getCityCount(){
        if(!mAvailable){
            return 0;
        }
        return mCitys.size();
    }
}
