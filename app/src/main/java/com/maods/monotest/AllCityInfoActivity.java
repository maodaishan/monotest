package com.maods.monotest;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.maods.bctest.EOS.EOSOperations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by MAODS on 2018/12/17.
 */

public class AllCityInfoActivity extends Activity {
    private static final String TAG="AllCityInfoActivity";
    private static final String CODE="monopolygame";
    private ListView mList;
    private SimpleAdapter mSimpleAdapter;
    private Citys mCitys;
    public void onCreate(Bundle savedInstance){
        super.onCreate(savedInstance);
        setContentView(R.layout.all_citys);
        mList=findViewById(R.id.list);
        getCitys();
    }

    private void getCitys(){
        Thread t=new Thread(new Runnable() {
            @Override
            public void run() {
                String result=EOSOperations.getTableRows(CODE,CODE,"citys",0,100,100);
                mCitys=new Citys(result);
                if(mCitys.mAvailable){
                    AllCityInfoActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            updateList();
                        }
                    });

                }
            }
        });
        t.start();
    }

    private void updateList(){
        List<Map<String,String>> allCityInfo=new ArrayList<Map<String,String>>();
        for(int i=0;i<mCitys.getCityCount();i++){
            City city=mCitys.getCity(i);
            Map<String,String> info=new HashMap<String,String>();
            info.put("city",city.toString());
            allCityInfo.add(info);
        }
        mSimpleAdapter=new SimpleAdapter(this,allCityInfo,android.R.layout.simple_list_item_1,new String[]{"city"},new int[]{android.R.id.text1});
        mList.setAdapter(mSimpleAdapter);
    }
}
