package com.maods.monotest;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.maods.bctest.EOS.Action;
import com.maods.bctest.EOS.EOSOperations;
import com.maods.bctest.EOS.EOSUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by MAODS on 2018/12/27.
 */

public class HacktestActivity extends Activity {
    private static final String TAG="HacktestActivity";
    private ListView mList;
    private EditText mEdit1;
    private EditText mEdit2;
    private EditText mEdit3;
    private EditText mEdit4;
    private EditText mEdit5;
    private EditText mPermissionActor;
    private EditText mPermissionLevel;
    private Button mBtnExec;
    private EditText mTransFrom;
    private EditText mTransTo;
    private EditText mTransQuantity;
    private Spinner mTransMemo;
    //private EditText mTransPermActor;
    //private EditText mTransPermLevel;
    private Button mBtnTrans;
    private TextView mExecResult;

    private SimpleAdapter mListAdapter;
    private List<Map<String,String>>mMonoActionList;
    private String mAction;
    private String mMemo;
    private AlertDialog mExecDialog;
    private HandlerThread mWorkThread;
    private Handler mWorkHandler;
    private Handler mUIHandler;

    private static final String CODE="monopolygame";
    private static final String ACTION="action";
    private static final String ACTION_MOVE="move";
    private static final String ACTION_TRANSFER="transfer";
    private static final String ACTION_SETLOGO="setlogo";
    private static final String ACTION_TESTTERMINATE="testterminate";
    private static final String ACTION_RESET="reset";
    private static final int MSG_MOVE=1;
    private static final int MSG_TRANSFER=2;
    private static final int MSG_SETLOGO=3;
    private static final int MSG_TESTTERMINATE=4;
    private static final int MSG_RESET=5;
    private static final int MSG_EOSIO_TRANSFER=6;
    private static final String[] MONO_ACTIONS=new String[]{
            ACTION_MOVE,
            ACTION_TRANSFER,
            ACTION_SETLOGO,
            ACTION_TESTTERMINATE,
            ACTION_RESET
    };

    private static final String MEMO_PAY_RENT="pay_rent";
    private static final String MEMO_BUY_CITY="buy_city";
    private static final String MEMO_REVEAL="reveal";
    private static final String[] MEMO_LIST=new String[]{
            MEMO_PAY_RENT,
            MEMO_BUY_CITY,
            MEMO_REVEAL
    };

    public void onCreate(Bundle savedInstance){
        super.onCreate(savedInstance);
        setContentView(R.layout.hacktest);
        mList=findViewById(R.id.monopolylist);
        mEdit1=findViewById(R.id.edit1);
        mEdit2=findViewById(R.id.edit2);
        mEdit3=findViewById(R.id.edit3);
        mEdit4=findViewById(R.id.edit4);
        mEdit5=findViewById(R.id.edit5);
        mPermissionActor=findViewById(R.id.permission_actor);
        mPermissionLevel=findViewById(R.id.permission_level);
        mBtnExec=findViewById(R.id.monoexec);
        mTransFrom=findViewById(R.id.transfer_from);
        mTransTo=findViewById(R.id.transfer_to);
        mTransQuantity=findViewById(R.id.transfer_quantity);
        mTransMemo=findViewById(R.id.spinner);
        //mTransPermActor=findViewById(R.id.trans_permission_actor);
        //mTransPermLevel=findViewById(R.id.trans_permission_level);
        mBtnTrans=findViewById(R.id.trans_exec);
        mExecResult=findViewById(R.id.result);

        mBtnExec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleMonoActions();
            }
        });
        mBtnTrans.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleTransfer();
            }
        });

        List<String> spinnerData=new ArrayList<String>();
        for(int i=0;i<MEMO_LIST.length;i++){
            spinnerData.add(MEMO_LIST[i]);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item,spinnerData);
        mTransMemo.setAdapter(adapter);
        mTransMemo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                mMemo=MEMO_LIST[i];
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                mMemo="";
            }
        });

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

        mMonoActionList=new ArrayList<Map<String,String>>();
        for(int i=0;i<MONO_ACTIONS.length;i++){
            Map<String,String>map=new HashMap<String,String>();
            map.put(ACTION,MONO_ACTIONS[i]);
            mMonoActionList.add(map);
        }
        mListAdapter=new SimpleAdapter(this,mMonoActionList,android.R.layout.simple_list_item_1,new String[]{ACTION},new int[]{android.R.id.text1});
        mList.setAdapter(mListAdapter);
        //set listview height
        int height = 0;
        int count = mListAdapter.getCount();
        for(int i=0;i<count;i++){
            View temp = mListAdapter.getView(i,null,mList);
            temp.measure(0,0);
            height += temp.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = mList.getLayoutParams();
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        params.height = height;
        mList.setLayoutParams(params);
        mList.invalidate();
        mList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                mEdit1.setHint("");
                mEdit2.setHint("");
                mEdit3.setHint("");
                mEdit4.setHint("");
                mEdit5.setHint("");
                mEdit1.setEnabled(false);
                mEdit2.setEnabled(false);
                mEdit3.setEnabled(false);
                mEdit4.setEnabled(false);
                mEdit5.setEnabled(false);
                Map<String,String>map=mMonoActionList.get(i);
                String action=map.get(ACTION);
                switch(action){
                    case ACTION_MOVE:
                        mEdit1.setHint("账户名");
                        mEdit2.setHint("输入要移动步数");
                        mEdit1.setEnabled(true);
                        mEdit2.setEnabled(true);
                        mAction=ACTION_MOVE;
                        break;
                    case ACTION_TRANSFER:
                        mEdit1.setHint("转出者");
                        mEdit2.setHint("接收者");
                        mEdit3.setHint("转账数量（xx.xxxx)");
                        mEdit4.setHint("memo");
                        mEdit1.setEnabled(true);
                        mEdit2.setEnabled(true);
                        mEdit3.setEnabled(true);
                        mEdit4.setEnabled(true);
                        mAction=ACTION_TRANSFER;
                        break;
                    case ACTION_SETLOGO:
                        mEdit1.setHint("账户名");
                        mEdit2.setHint("城市号");
                        mEdit3.setHint("标语(最大32字符)");
                        mEdit4.setHint("图片(最大64字符)");
                        mEdit5.setHint("链接(最大64字符）");
                        mEdit1.setEnabled(true);
                        mEdit2.setEnabled(true);
                        mEdit3.setEnabled(true);
                        mEdit4.setEnabled(true);
                        mEdit5.setEnabled(true);
                        mAction=ACTION_SETLOGO;
                        break;
                    case ACTION_TESTTERMINATE:
                        mAction=ACTION_TESTTERMINATE;
                        break;
                    case ACTION_RESET:
                        mAction=ACTION_RESET;
                        break;
                }
            }
        });
    }

    private void showProcessing(String action){
        if(mExecDialog!=null){
            mExecDialog.dismiss();
        }
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setMessage("正在执行 "+action);
        mExecDialog=builder.create();
        mExecDialog.show();
    }
    private void handleMonoActions(){
        Map<String,String>args=new HashMap<String,String>();
        Message msg=mWorkHandler.obtainMessage();
        switch(mAction){
            case ACTION_MOVE:
                String account=mEdit1.getText().toString();
                if(!EOSUtils.isAccountNameLeagle(account)){
                    Toast.makeText(this,"用户名不合法",Toast.LENGTH_LONG).show();
                    break;
                }
                args.put("user",account);
                args.put("step",mEdit2.getText().toString());
                msg.what=MSG_MOVE;
                msg.obj=args;
                mWorkHandler.sendMessage(msg);
                break;
            case ACTION_TRANSFER:
                String from=mEdit1.getText().toString();
                String to=mEdit2.getText().toString();
                String quantity=mEdit3.getText().toString()+" EOS";
                String memo=mEdit4.getText().toString();
                if(!EOSUtils.isAccountNameLeagle(from)
                        || !EOSUtils.isAccountNameLeagle(to)){
                    Toast.makeText(this,"账户名不合法",Toast.LENGTH_LONG).show();
                    break;
                }
                args.put("from",from);
                args.put("to",to);
                args.put("quantity",quantity);
                args.put("memo",memo);
                msg.what=MSG_TRANSFER;
                msg.obj=args;
                mWorkHandler.sendMessage(msg);
                break;
            case ACTION_SETLOGO:
                account=mEdit1.getText().toString();
                String pos=mEdit2.getText().toString();
                String label=mEdit3.getText().toString();
                String img=mEdit4.getText().toString();
                String url=mEdit5.getText().toString();
                if(!EOSUtils.isAccountNameLeagle(account)){
                    Toast.makeText(this,"用户名不合法",Toast.LENGTH_LONG).show();
                    break;
                }
                args.put("user",account);
                args.put("city_num",pos);
                args.put("label",label);
                args.put("img",img);
                args.put("url",url);
                msg.what=MSG_SETLOGO;
                msg.obj=args;
                mWorkHandler.sendMessage(msg);
                break;
            case ACTION_TESTTERMINATE:
                msg.what=MSG_TESTTERMINATE;
                msg.obj=args;
                mWorkHandler.sendMessage(msg);
                break;
            case ACTION_RESET:
                msg.what=MSG_RESET;
                msg.obj=args;
                mWorkHandler.sendMessage(msg);
                break;
        }
        showProcessing(mAction);
    }

    private void handleTransfer(){
        /*Map<String,String>args=new HashMap<String,String>();
        String from=mTransFrom.getText().toString();
        String to=mTransTo.getText().toString();
        String quantity=mTransQuantity.getText().toString();
        if(!EOSUtils.isAccountNameLeagle(from)
                || !EOSUtils.isAccountNameLeagle(to)){
            Toast.makeText(this,"账户名不合法",Toast.LENGTH_LONG).show();
        }
        args.put("from",from);
        args.put("to",to);
        args.put("quantity",quantity);
        args.put("memo",mMemo);*/
        if(TextUtils.isEmpty(mMemo)){
            Toast.makeText(this,"必须选择memo",Toast.LENGTH_LONG).show();
            return;
        }
        Message msg=mWorkHandler.obtainMessage();
        msg.what=MSG_EOSIO_TRANSFER;
        //msg.obj=args;
        mWorkHandler.sendMessage(msg);
        showProcessing(ACTION_TRANSFER);
    }

    private void handleWorkThreadMsg(Message msg) {
        final String result;
        List<Map<String,String>>auths=new ArrayList<Map<String,String>>();
        Map<String,String>auth=new HashMap<String,String>();
        switch (msg.what) {
            case MSG_MOVE:
            case MSG_TRANSFER:
            case MSG_SETLOGO:
            case MSG_TESTTERMINATE:
            case MSG_RESET:
                auth.put(Action.ACTOR,mPermissionActor.getText().toString());
                auth.put(Action.PERMISSION,mPermissionLevel.getText().toString());
                auths.add(auth);
                result=EOSOperations.executeAction(this,false,CODE,monoMsgToAction(msg.what),(Map<String,String>)msg.obj,auths);
                break;
            case MSG_EOSIO_TRANSFER:
                //auth.put(Action.ACTOR,mTransPermActor.getText().toString());
                //auth.put(Action.PERMISSION,mTransPermLevel.getText().toString());
                //auths.add(auth);
                String from=mTransFrom.getText().toString();
                String to=mTransTo.getText().toString();
                String quantity=mTransQuantity.getText().toString()+" EOS";
                result=EOSOperations.transfer(this,from,to,quantity,mMemo);
                break;
            default:
                result="";
        }
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(mExecDialog!=null){
                    mExecDialog.dismiss();
                    mExecDialog=null;
                }
                if(!TextUtils.isEmpty(result)) {
                    mExecResult.setText(result);
                }else{
                    mExecResult.setText("执行出错，请检查是否符合执行条件，如没交租金不能前进等");
                }
                mExecResult.invalidate();
            }
        });
    }
    private void handleUIThreadMsg(Message msg) {
        switch (msg.what) {

        }
    }
    private String monoMsgToAction(int msg){
        switch(msg){
            case MSG_MOVE:
                return ACTION_MOVE;
            case MSG_TRANSFER:
                return ACTION_TRANSFER;
            case MSG_SETLOGO:
                return ACTION_SETLOGO;
            case MSG_TESTTERMINATE:
                return ACTION_TESTTERMINATE;
            case MSG_RESET:
                return ACTION_RESET;
        }
        return null;
    }
}
