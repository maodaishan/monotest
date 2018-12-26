package com.maods.monotest;

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by MAODS on 2018/12/12.
 */

public class Account {
    private static final String TAG="Account";
    public String mAccountName;
    public int mPos;
    public double mRent;
    public String mRentOwner;
    public double mReveal;
    public boolean mAvailable=false;

    //input should be result of get_table_rows
    public Account(String tableContent,String accountInput){
        if(TextUtils.isEmpty(tableContent)){
            return;
        }
        try {
            JSONObject tableJson=new JSONObject(tableContent);
            JSONArray rows=tableJson.getJSONArray("rows");
            if(rows.length()==0){
                mAccountName=accountInput;
                mPos=-1;
                mRent=0;
                mRentOwner="monopolygame";
                mReveal=0;
                mAvailable=true;
                return;
            }
            JSONObject account=rows.getJSONObject(0);
            mAccountName=account.getString("account");
            mPos=account.getInt("pos");
            String rent_quantity=account.getString("rent_quantity");
            mRentOwner=account.getString("rent_owner");
            String balance=account.getString("to_reveal");
            mRent=Double.parseDouble(rent_quantity.substring(0,rent_quantity.length()-4));
            mReveal=Double.parseDouble(balance.substring(0,balance.length()-4));
            mAvailable=true;
        }catch(JSONException e){
            Log.e(TAG,e.toString());
        }
    }
}
