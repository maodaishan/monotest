package com.maods.monotest;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.maods.bctest.EOS.Action;
import com.maods.bctest.EOS.EOSOperations;
import com.maods.bctest.EOS.EOSUtils;
import com.maods.bctest.GlobalConstants;
import com.maods.bctest.GlobalUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import io.plactal.eoscommander.data.wallet.EosWallet;
import io.plactal.eoscommander.data.wallet.EosWalletManager;

public class MainActivity extends AppCompatActivity {
    private static final String TAG="monopoly";

    private static final int REQUEST_WRITE_EXTERNAL_STORAGE=1;
    private static final String CODE="monopolygame";
    private static final String TABLE_ACCOUNTS="accounts";
    private static final String TABLE_CITYS="citys";
    private static final String TABLE_STATUS="status";

    private static final String MEMO_PAY_RENT="pay_rent";
    private static final String MEMO_BUY_CITY="buy_city";
    private static final String MEMO_REVEAL="reveal";
    private static final int TEST_TIME_LINE=3*24*60*60*1000;    //3 days

    private static final int MSG_UPDATE=1;
    private static final int MSG_MOVE=2;
    private static final int MSG_PAY_RENT=3;
    private static final int MSG_BUY_CITY=4;
    private static final int MSG_REVEAL=5;
    private static final int MSG_TRY_TERMINATE=6;
    private static final int MSG_RESET=7;
    private static final int MSG_SET_LOGO=8;
    private static final int MSG_UI_HANDLE_ACCOUNT=100;
    private static final int MSG_UI_HANDLE_CITY=101;
    private static final int MSG_UI_HANDLE_STATUS=102;

    private static final String PREF_MONOPOLY="pref_monopoly";
    private static final String PREF_KEY_SHOW_DESC="show_desc";


    private Button mWallet;
    private EditText mAccountView;
    private Button mStartView;

    private TextView mPosView;
    private TextView mBalanceView;
    private TextView mRentView;

    private Button mBtnMove;
    private EditText mEditStep;
    private Button mPayRent;
    private Button mBuyCity;
    private Button mReveal;
    private Button mSetLogo;

    private TextView mPoolView;
    private TextView mLastModifiedView;
    private TextView mWhetherStopView;
    private Button mTryTerminate;
    private Button mReset;
    private TextView mCityInfoView;
    private TextView mActionResultView;
    private Button mAllCityInfoView;

    private Account mAccount;
    private Citys mCitys;
    private Stats mStats;

    private HandlerThread mWorkThread;
    private Handler mWorkHandler;
    private Handler mUIHandler;

    private AlertDialog mGetAccountDialog;
    private AlertDialog mGetCityDialog;
    private AlertDialog mGetStatsDialog;
    private AlertDialog mMoveDialog;
    private AlertDialog mPayRentDialog;
    private AlertDialog mBuyCityDialog;
    private AlertDialog mRevealDialog;
    private AlertDialog mTryTerminateDialog;
    private AlertDialog mResetDialog;
    private AlertDialog mSetLogoDialog;
    private ArrayList<EosWallet.Status> mWalletStatus;
    private SharedPreferences mPref;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mWallet=findViewById(R.id.wallet);
        mAccountView=findViewById(R.id.account);
        mStartView=findViewById(R.id.start);
        mPosView=findViewById(R.id.info_pos);
        mBalanceView=findViewById(R.id.info_balance);
        mRentView=findViewById(R.id.info_rent);
        mBtnMove=findViewById(R.id.action_move);
        mEditStep=findViewById(R.id.step);
        mPayRent=findViewById(R.id.action_pay_rent);
        mBuyCity=findViewById(R.id.action_buy_city);
        mReveal=findViewById(R.id.action_reveal);
        mSetLogo=findViewById(R.id.setlogo);
        mPoolView=findViewById(R.id.pool);
        mLastModifiedView=findViewById(R.id.last_modified);
        mWhetherStopView=findViewById(R.id.whether_stop);
        mTryTerminate=findViewById(R.id.try_terminate);
        mReset=findViewById(R.id.reset);
        mCityInfoView=findViewById(R.id.city_info);
        mActionResultView=findViewById(R.id.operation_result);
        mAllCityInfoView=findViewById(R.id.all_city_info);
        mBtnMove.setEnabled(false);
        mPayRent.setEnabled(false);
        mBuyCity.setEnabled(false);
        mReveal.setEnabled(false);
        mAllCityInfoView.setEnabled(false);

        mWallet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent();
                intent.setComponent(new ComponentName("com.maods.monotest","com.maods.monotest.EOSListActivity"));
                intent.putExtra("action","list_wallets");
                startActivity(intent);
            }
        });
        mStartView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                update();
            }
        });

        mBtnMove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleMove();
            }
        });

        mPayRent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handlePayRent();
            }
        });

        mBuyCity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleBuyCity();
            }
        });

        mReveal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleReveal();
            }
        });

        mTryTerminate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleTryTerminate();
            }
        });

        mAllCityInfoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleViewAllCitys();
            }
        });

        mReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleReset();
            }
        });

        mSetLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleSetLogo();
            }
        });

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_EXTERNAL_STORAGE);
            return;
        }

        mWorkThread=new HandlerThread("monopoly");
        mWorkThread.start();
        mWorkHandler=new Handler(mWorkThread.getLooper()){
            @Override
            public void handleMessage(Message msg){
                handleWorkThreadMsg(msg);
            }
        };
        mUIHandler=new Handler(){
            @Override
            public void handleMessage(Message msg){
                handleUIThreadMsg(msg);
            }
        };

        checkAndLoadWallets();

        mPref=this.getSharedPreferences(PREF_MONOPOLY, Context.MODE_PRIVATE);
        boolean can_show=mPref.getBoolean(PREF_KEY_SHOW_DESC,true);
        if(can_show){
            showGameDescDialog();
        }

    }

    private void showGameDescDialog(){
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        View view=View.inflate(this,R.layout.desc_alert,null);
        final CheckBox not_show_again=view.findViewById(R.id.not_show_again);
        not_show_again.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                mPref.edit().putBoolean(PREF_KEY_SHOW_DESC,!b).commit();
            }
        });
        builder.setView(view);
        builder.setPositiveButton(android.R.string.ok,null);
        builder.create().show();
        mPref.edit().putBoolean(PREF_KEY_SHOW_DESC,!not_show_again.isChecked()).commit();
    }
    private void checkAndLoadWallets(){
        EosWalletManager manager=EosWalletManager.getInstance(this);
        //openAllWallets();
        mWalletStatus= manager.listWallets(null);
        int size=mWalletStatus.size();
        if(size==0){
            GlobalUtils.showAlertMsg(this,R.string.no_wallet_alert);
        }else{
            SharedPreferences pref= PreferenceManager.getDefaultSharedPreferences(this);
            boolean rememberPswd=pref.getBoolean(EOSUtils.REMEMBER_WALLET_PSWD,false);
            if(rememberPswd){
                for(int i=0;i<mWalletStatus.size();i++){
                    EosWallet.Status status=mWalletStatus.get(i);
                    if(status.locked){
                        String name=status.walletName;
                        String pswd=pref.getString(name,null);
                        if(!TextUtils.isEmpty(pswd)){
                            manager.getWallet(name).unlock(pswd);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults){
        return;
    }
    public void onResume(){
        super.onResume();
        if(mAccount!=null && mAccount.mAvailable){
            update();
        }
    }

    private void update(){
        mWorkHandler.sendEmptyMessage(MSG_UPDATE);
        if(mGetAccountDialog!=null){
            mGetAccountDialog.dismiss();
            mGetAccountDialog=null;
        }
        if(mGetCityDialog!=null){
            mGetCityDialog.dismiss();
            mGetCityDialog=null;
        }
        if(mGetStatsDialog!=null){
            mGetStatsDialog.dismiss();
            mGetStatsDialog=null;
        }
        if(mMoveDialog!=null){
            mMoveDialog.dismiss();
            mMoveDialog=null;
        }
        if(mPayRentDialog!=null){
            mPayRentDialog.dismiss();
            mPayRentDialog=null;
        }
        if(mBuyCityDialog!=null){
            mBuyCityDialog.dismiss();
            mBuyCityDialog=null;
        }
        if(mRevealDialog!=null){
            mRevealDialog.dismiss();
            mRevealDialog=null;
        }
        if(mTryTerminateDialog!=null){
            mTryTerminateDialog.dismiss();
            mTryTerminateDialog=null;
        }
        if(mResetDialog!=null){
            mResetDialog.dismiss();
            mResetDialog=null;
        }
        if(mSetLogoDialog!=null){
            mSetLogoDialog.dismiss();
            mSetLogoDialog=null;
        }

        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setMessage("正在获取账号信息");
        mGetAccountDialog=builder.create();
        mGetAccountDialog.show();

        builder=new AlertDialog.Builder(this);
        builder.setMessage("正在获取城市信息");
        mGetCityDialog=builder.create();
        mGetCityDialog.show();

        builder=new AlertDialog.Builder(this);
        builder.setMessage("正在获取奖池信息");
        mGetStatsDialog=builder.create();
        mGetStatsDialog.show();
    }

    private void showExecuteResultAndUpdateinUIThread(final String input){
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String result=input;
                if(TextUtils.isEmpty(result)){
                    result="Error";
                }
                mActionResultView.setText(result);
                update();
            }
        });
    }

    private void handleMove(){
        if(!mAccount.mAvailable || mAccount.mRent>0){
            return;
        }
        mWorkHandler.sendEmptyMessage(MSG_MOVE);
        AlertDialog.Builder builder=new AlertDialog.Builder(MainActivity.this);
        builder.setMessage("正在移动...");
        mMoveDialog=builder.create();
        mMoveDialog.show();
    }

    private void handlePayRent(){
        if(!mAccount.mAvailable){
            return;
        }
        if(mAccount.mRent==0){
            Toast.makeText(this,"当前租金为0，不需要付租金",Toast.LENGTH_LONG).show();
            return;
        }
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setMessage("支付租金 "+mAccount.mRent+" EOS"+" 给您落脚时的城市主人 "+mAccount.mRentOwner);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mWorkHandler.sendEmptyMessage(MSG_PAY_RENT);
                AlertDialog.Builder builder=new AlertDialog.Builder(MainActivity.this);
                builder.setMessage("正在支付租金...");
                mPayRentDialog=builder.create();
                mPayRentDialog.show();
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.create().show();
    }

    private void handleBuyCity(){
        if(!mAccount.mAvailable){
            return;
        }
        City city=mCitys.getCity(mAccount.mPos);
        if(city.mOwner.equals(mAccount.mAccountName)){
            Toast.makeText(this,"这个城市已经是你的了，不需要购买",Toast.LENGTH_LONG).show();
            return;
        }
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setMessage("从 "+city.mOwner+" 处购买城市 "+mAccount.mPos+" ，价格："+city.mPrice);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mWorkHandler.sendEmptyMessage(MSG_BUY_CITY);
                AlertDialog.Builder builder=new AlertDialog.Builder(MainActivity.this);
                builder.setMessage("正在支付买城市费用");
                mBuyCityDialog=builder.create();
                mBuyCityDialog.show();
            }
        });
        builder.setNegativeButton(android.R.string.cancel,null);
        builder.create().show();
    }

    private void handleReveal(){
        if(!mAccount.mAvailable){
            return;
        }
        if(mAccount.mReveal<=0){
            Toast.makeText(this,"没有可以提取的余额",Toast.LENGTH_LONG).show();
            return;
        }
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setMessage("将要提取余额 "+mAccount.mReveal+" EOS"+",是否继续？");
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mWorkHandler.sendEmptyMessage(MSG_REVEAL);
                AlertDialog.Builder builder=new AlertDialog.Builder(MainActivity.this);
                builder.setMessage("正在提取余额");
                mRevealDialog=builder.create();
                mRevealDialog.show();
            }
        });
        builder.setNegativeButton(android.R.string.cancel,null);
        builder.create().show();
    }

    private void handleTryTerminate(){
        mWorkHandler.sendEmptyMessage(MSG_TRY_TERMINATE);
        AlertDialog.Builder builder=new AlertDialog.Builder(MainActivity.this);
        builder.setMessage("正在尝试结束游戏");
        mTryTerminateDialog=builder.create();
        mTryTerminateDialog.show();
    }

    private void handleReset(){
        mWorkHandler.sendEmptyMessage(MSG_RESET);
        AlertDialog.Builder builder=new AlertDialog.Builder(MainActivity.this);
        builder.setMessage("正在尝试重置游戏");
        mResetDialog=builder.create();
        mResetDialog.show();
    }

    private void handleSetLogo(){
        if(mCitys==null || !mCitys.mAvailable
                || mAccount==null || !mAccount.mAvailable){
            Toast.makeText(this,"信息不全，无法操作",Toast.LENGTH_LONG).show();
            return;
        }
        City city=mCitys.getCity(mAccount.mPos);
        if(!city.mAvailable){
            Toast.makeText(this,"城市信息获取错误",Toast.LENGTH_LONG).show();
            return;
        }
        if(!city.mOwner.equals(mAccount.mAccountName)){
            Toast.makeText(this,"只能操作自己的城市",Toast.LENGTH_LONG).show();
            return;
        }
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        View view=View.inflate(this,R.layout.setlogo,null);
        builder.setView(view);
        final EditText labelView=view.findViewById(R.id.label);
        final EditText imgView=view.findViewById(R.id.img);
        final EditText urlView=view.findViewById(R.id.url);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                dialog.dismiss();
                String label=labelView.getText().toString();
                String img=imgView.getText().toString();
                String url=urlView.getText().toString();
                if(TextUtils.isEmpty(label) && TextUtils.isEmpty(img) && TextUtils.isEmpty(url)){
                    return;
                }
                if(label.length()>32){
                    label=label.substring(0,32);
                }
                if(img.length()>64){
                    img=img.substring(0,64);
                }
                if(url.length()>64){
                    url=url.substring(0,64);
                }
                HashMap<String,String>args=new HashMap<String,String>();
                args.put("user",mAccount.mAccountName);
                args.put("city_num",String.valueOf(mAccount.mPos));
                args.put("label",label);
                args.put("img",img);
                args.put("url",url);
                Message msg=mWorkHandler.obtainMessage();
                msg.what=MSG_SET_LOGO;
                msg.obj=args;
                mWorkHandler.sendMessage(msg);
                AlertDialog.Builder builder=new AlertDialog.Builder(MainActivity.this);
                builder.setMessage("正在设置logo");
                mSetLogoDialog=builder.create();
                mSetLogoDialog.show();
            }
        });
        builder.setNegativeButton(android.R.string.cancel,null);
        builder.create().show();
    }

    private void handleViewAllCitys(){
        Intent intent=new Intent();
        intent.setComponent(new ComponentName("com.maods.monotest","com.maods.monotest.AllCityInfoActivity"));
        startActivity(intent);
    }

    private boolean handleAccountInfo(String tableContent){
        mAccount=new Account(tableContent,mAccountView.getText().toString());
        if(!mAccount.mAvailable){
            Log.e(TAG,"can't get account info"+tableContent);
            return false;
        }
        if(mAccount.mPos==-1) {
            mPosView.setText("您尚未开始游戏，请通过“前进”开始游戏");
            mRentView.setText("");
            mBalanceView.setText("");
        }else{
            String cityName="";
            if(mCitys!=null && mCitys.mAvailable){
                City city=mCitys.getCity(mAccount.mPos);
                cityName=city.mName;
            }
            mPosView.setText("您所在城市，号码："+mAccount.mPos+",名字:"+cityName);
            mRentView.setText("您欠"+mAccount.mRentOwner+" 租金："+mAccount.mRent);
            mBalanceView.setText("您可提现金额："+mAccount.mReveal);
        }


        //handle btns
        if(mAccount.mRent==0){
            mBtnMove.setEnabled(true);
            mPayRent.setEnabled(false);
            //mBuyCity.setEnabled(true);
        }else{
            mBtnMove.setEnabled(false);
            mPayRent.setEnabled(true);
            mBuyCity.setEnabled(false);
        }
        if(mAccount.mReveal>0){
            mReveal.setEnabled(true);
        }else{
            mReveal.setEnabled(false);
        }
        Thread t=new Thread(new Runnable() {
            @Override
            public void run() {
                String cityTable=EOSOperations.getTableRows(CODE,CODE,TABLE_CITYS,0,100,100);
                Message msgAccount=mUIHandler.obtainMessage();
                msgAccount.what=MSG_UI_HANDLE_CITY;
                msgAccount.obj=cityTable;
                mUIHandler.sendMessage(msgAccount);
            }
        });
        t.start();
        return true;
    }

    private void handleCityInfo(String input){
        mCitys=new Citys(input);
        if(!mCitys.mAvailable){
            Log.e(TAG,"get city info err,input:"+input);
            mAllCityInfoView.setEnabled(false);
            return;
        }
        if(mAccount.mPos==-1){
            mCityInfoView.setText("尚未开始游戏，不在任何城市");
            mBuyCity.setEnabled(false);
        }else {
            City city = mCitys.getCity(mAccount.mPos);
            if (city == null) {
                return;
            }
            mCityInfoView.setText(city.toString());
            boolean canBuy = false;
            if (city.mAvailable) {
                if (city.mOwner != mAccount.mAccountName) {
                    canBuy = true;
                }
            }
            if (canBuy) {
                mBuyCity.setEnabled(true);
            } else {
                mBuyCity.setEnabled(false);
            }
            //update Account info, mPosView
            mPosView.setText("您所在城市，号码："+mAccount.mPos+",名字:"+city.mName);
        }
        mAllCityInfoView.setEnabled(true);
    }

    private void handleStatsInfo(String input){
        mStats=new Stats(input);
        if(!mStats.mAvailable){
            return;
        }
        mPoolView.setText("当前奖池："+String.valueOf(mStats.mPool));
        mLastModifiedView.setText("最后城市买卖时间："+mStats.mFormatedLastModified);

        long currTime=System.currentTimeMillis();
        long timeGap=currTime-mStats.mLastModified;
        if(mStats.mLastModified==-1){
            mWhetherStopView.setText("尚无城市买卖");
        }else if(timeGap>TEST_TIME_LINE){
            mWhetherStopView.setText("游戏已结束 "+String.valueOf((timeGap-TEST_TIME_LINE)/(60*60*1000))+" 小时");
        }else{
            mWhetherStopView.setText(("游戏尚未结束，上次城市买卖距今："+String.valueOf((-timeGap)/(60*60*1000)))+" 小时");
        }
    }

    private void handleUpdate(){
        String account=mAccountView.getText().toString();
        if(!EOSUtils.isAccountNameLeagle(account)){
            this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this,"请检查账户名是否合法",Toast.LENGTH_SHORT).show();
                }
            });
            return;
        }

        String accountTable=EOSOperations.getTableRows(account,CODE,TABLE_ACCOUNTS,-1,-1,-1);

        Message msgAccount=mUIHandler.obtainMessage();
        msgAccount.what=MSG_UI_HANDLE_ACCOUNT;
        msgAccount.obj=accountTable;
        mUIHandler.sendMessage(msgAccount);

        Thread t2=new Thread(new Runnable() {
            @Override
            public void run() {
                String statsTable=EOSOperations.getTableRows(CODE,CODE,TABLE_STATUS,-1,-1,-1);
                Message msgAccount=mUIHandler.obtainMessage();
                msgAccount.what=MSG_UI_HANDLE_STATUS;
                msgAccount.obj=statsTable;
                mUIHandler.sendMessage(msgAccount);
            }
        });
        t2.start();
    }
    private void handleWorkThreadMsg(Message msg){
        String result;
        switch(msg.what){
            case MSG_UPDATE:
                handleUpdate();
                break;
            case MSG_MOVE:
                Random r=new Random();
                int pos=0;
                String inputStep=mEditStep.getText().toString();
                if(!TextUtils.isEmpty(inputStep)) {
                    pos = Integer.parseInt(inputStep);
                }
                if(pos<2 || pos>12) {
                    do {
                        int random = r.nextInt();
                        pos = random & 12;
                        pos += 1;
                    } while (pos < 2 || pos > 12);
                }
                HashMap<String,String>params=new HashMap<String,String>();
                params.put("user",mAccount.mAccountName);
                params.put("step",String.valueOf(pos));
                List<Map<String,String>> auths=new ArrayList<Map<String,String>>();
                Map<String,String> auth=new HashMap<String,String>();
                auth.put(Action.ACTOR,mAccount.mAccountName);
                auth.put(Action.PERMISSION,"active");
                auths.add(auth);
                result=EOSOperations.executeAction(this,true,CODE,"move",params,auths);
                showExecuteResultAndUpdateinUIThread(result);
                break;
            case MSG_PAY_RENT:
                //String rent=String.valueOf(mAccount.mRent);
                String rent= String.format("%.4f",mAccount.mRent);
                rent=rent+" EOS";
                result=EOSOperations.transfer(MainActivity.this,mAccount.mAccountName,CODE,rent,MEMO_PAY_RENT);
                showExecuteResultAndUpdateinUIThread(result);
                break;
            case MSG_BUY_CITY:
                City city=mCitys.getCity(mAccount.mPos);
                //String price=String.valueOf(city.mPrice);
                String price= String.format("%.4f",city.mPrice);
                price=price+" EOS";
                result=EOSOperations.transfer(MainActivity.this,mAccount.mAccountName,CODE,price,MEMO_BUY_CITY);
                showExecuteResultAndUpdateinUIThread(result);
                break;
            case MSG_REVEAL:
                /*Map<String,String>args=new HashMap<String,String>();
                args.put("user",String.valueOf(mAccount.mReveal));
                List<Map<String,String>>authsReveal=new ArrayList<Map<String,String>>();
                Map<String,String>authReveal=new HashMap<String,String>();
                authReveal.put(Action.ACTOR,mAccount.mAccountName);
                authReveal.put(Action.PERMISSION,"active");
                authsReveal.add(authReveal);
                result=EOSOperations.executeAction(this,true,CODE,"reveal",args,authsReveal);*/
                String reveal= String.format("%.4f",mAccount.mReveal);
                reveal=reveal+" EOS";
                result=EOSOperations.transfer(MainActivity.this,CODE,mAccount.mAccountName,reveal,MEMO_REVEAL);
                showExecuteResultAndUpdateinUIThread(result);
                break;
            case MSG_TRY_TERMINATE:
                List<Map<String,String>>authsTry=new ArrayList<Map<String,String>>();
                Map<String,String>authTry=new HashMap<String,String>();
                authTry.put(Action.ACTOR,CODE);
                authTry.put(Action.PERMISSION,"active");
                authsTry.add(authTry);
                result=EOSOperations.executeAction(this,true,CODE,"testterminate",new HashMap<String,String>(),authsTry);
                showExecuteResultAndUpdateinUIThread(result);
                break;
            case MSG_RESET:
                List<Map<String,String>>authsReset=new ArrayList<Map<String,String>>();
                Map<String,String>authReset=new HashMap<String,String>();
                authReset.put(Action.ACTOR,CODE);
                authReset.put(Action.PERMISSION,"active");
                authsReset.add(authReset);
                result=EOSOperations.executeAction(this,true,CODE,"reset",new HashMap<String,String>(),authsReset);
                showExecuteResultAndUpdateinUIThread(result);
                break;
            case MSG_SET_LOGO:
                List<Map<String,String>>authsSetLogo=new ArrayList<Map<String,String>>();
                Map<String,String>authSetLogo=new HashMap<String,String>();
                authSetLogo.put(Action.ACTOR,mAccount.mAccountName);
                authSetLogo.put(Action.PERMISSION,"active");
                authsSetLogo.add(authSetLogo);
                HashMap<String,String>args=(HashMap<String,String>)msg.obj;
                result=EOSOperations.executeAction(this,true,CODE,"setlogo",args,authsSetLogo);
                showExecuteResultAndUpdateinUIThread(result);
                break;
        }
    }

    private void handleUIThreadMsg(Message msg){
        switch(msg.what){
            case MSG_UI_HANDLE_ACCOUNT:
                String accountTable=(String)msg.obj;
                Log.i(TAG,"accountTable:"+accountTable);
                handleAccountInfo(accountTable);
                if(mGetAccountDialog!=null) {
                    mGetAccountDialog.dismiss();
                    mGetAccountDialog = null;
                }
                break;
            case MSG_UI_HANDLE_CITY:
                String cityTable=(String)msg.obj;
                Log.i(TAG,"cityTable:"+cityTable);
                handleCityInfo(cityTable);
                if(mGetCityDialog!=null) {
                    mGetCityDialog.dismiss();
                    mGetCityDialog = null;
                }
                break;
            case MSG_UI_HANDLE_STATUS:
                String statsTable=(String)msg.obj;
                Log.i(TAG,"statsTable:"+statsTable);
                handleStatsInfo(statsTable);
                if(mGetStatsDialog!=null) {
                    mGetStatsDialog.dismiss();
                    mGetStatsDialog = null;
                }
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        new MenuInflater(this).inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_desc:
                showGameDescDialog();
                break;
        }
        return true;
    }
}
