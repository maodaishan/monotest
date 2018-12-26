package com.maods.bctest.EOS;

import android.app.Notification;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.maods.bctest.ChainCommonOperations;
import com.maods.bctest.GlobalUtils;
import com.maods.monotest.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.PublicKey;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.plactal.eoscommander.crypto.digest.Sha256;
import io.plactal.eoscommander.crypto.ec.EcDsa;
import io.plactal.eoscommander.crypto.ec.EcSignature;
import io.plactal.eoscommander.crypto.ec.EosPrivateKey;
import io.plactal.eoscommander.crypto.ec.EosPublicKey;
import io.plactal.eoscommander.data.remote.model.api.EosChainInfo;
import io.plactal.eoscommander.data.remote.model.chain.SignedTransaction;
import io.plactal.eoscommander.data.remote.model.types.TypeChainId;
import io.plactal.eoscommander.data.remote.model.types.TypePermissionLevel;
import io.plactal.eoscommander.data.wallet.EosWalletManager;
//import io.plactal.eoscommander.data.remote.model.chain.Action;
/**
 * Created by MAODS on 2018/7/19.
 */

public class EOSOperations implements ChainCommonOperations {
    private static final String TAG="EOSOperations";

    public static final String ACTION_GET_INFO="get_info";
    public static final String ACTION_GET_ACCOUNT="get_account";
    public static final String ACTION_GET_BLOCK="get_block";
    public static final String ACTION_GET_ABI="get_abi";
    public static final String ACTION_GET_CODE="get_code";
    public static final String ACTION_GET_TABLE_ROWS="get_table_rows";
    public static final String ACTION_GET_RAM_PRICE="get_ram_price";        //actually this's not HTTP API, just for easy use
    public static final String ACTION_GET_PRODUCERS="get_producers";
    public static final String ACTION_JSON_TO_BIN="abi_json_to_bin";
    public static final String ACTION_GET_REQUIRED_KEYS="get_required_keys";
    public static final String ACTION_TRANSFER="transfer";
    public static final String ACTION_PUSH_TRANSACTION="push_transaction";
    public static final String ACTION_BUYRAMBYTES="buyrambytes";
    public static final String ACTION_BUYRAMEOS="buyram";
    public static final String ACTION_SELLRAM="sellram";
    public static final String ACTION_DELEGATEBW="delegatebw";
    public static final String ACTION_UNDELEGATEBW="undelegatebw";
    public static final String ACTION_GET_ACTIONS="get_actions";
    public static final String ACTION_GET_TRANSACTION="get_transaction";
    public static final String ACTION_BIN_TO_JSON="abi_bin_to_json";
    public static final String FUNCTION_BROWSER="browser";
    public static final String FUNCTION_GET_AVAILABLE_BP_API_SERVER="get_available_api_server";
    public static final String FUNCTION_CREATE_WALLET="create_wallet";
    public static final String FUNCTION_LIST_WALLETS="list_wallets";
    public static final String FUNCTION_GET_PRICE="get_price(neet climb over the great wall in China)";
    public static final String FUNCTION_MY_PROPERTY="check_my_property (neet climb over the great wall in China)";
    public static final String FUNCTION_RAM_TRADE_DEFINE_PRICE="ram trade with designated price";

    public static final String ACTOR="actor";
    public static final String PERMISSION="permission";

    private static final String PARAM_ACCOUNT_NAME="account_name";
    private static final String PARAM_ACCOUNT="account";
    private static final String PARAM_BLOCK_NUMBER_OR_ID="block_num_or_id";
    private static final String PARAM_CODE_AS_WASM="code_as_wasm";
    private static final String CODE_AS_WASM="false";
    private static final String PARAM_SCOPE="scope";
    private static final String PARAM_TABLE="table";
    private static final String PARAM_CODE="code";
    private static final String PARAM_JSON="json";
    private static final String PARAM_ACTION="action";
    private static final String PARAM_ARGS="args";
    private static final String PARAM_FROM="from";
    private static final String PARAM_TO="to";
    private static final String PARAM_QUANTITY="quantity";
    private static final String PARAM_MEMO="memo";
    private static final String PARAM_TRANSACTION="transaction";
    private static final String PARAM_AVAILABLE_KEYS="available_keys";
    private static final String PARAM_PAYER="payer";
    private static final String PARAM_RECEIVER="receiver";
    private static final String PARAM_BYTES="bytes";
    private static final String PARAM_OFFSET="offset";
    private static final String PARAM_POS="pos";
    private static final String PARAM_BINARGS="binargs";
    private static final String ACTIVE="active";
    private static final String RESULT_AS_JSON="true";
    private static final String ACCOUNT_EOSIO="eosio";
    private static final String TABLE_RAMMARKET="rammarket";
    private static final String EOSIO_TOKEN="eosio.token";
    private static final String EOSIO="eosio";
    private static final int TX_EXPIRATION_IN_MILSEC = 30000;

    //trade related
    private static final String HUOBI="huobi";
    private static final String HUOBI_URL_MARKET = "https://api.huobipro.com/market/";
    private static final String EOSUSDT="eosusdt";

    private static Object sSync=new Object();
    private static int sRpcUrlTestCount=0;
    private static ArrayList<String> sRpcUrlList=new ArrayList<String>();
    @Override
    public List<String> getServerNode(){
        return EOSUtils.getAvailableServers();
    }

    /**
     * Can't be called in UI thread
     * may return null or empty String if failed
     * will call "http://127.0.0.1:8888/v1/chain/get_info"
     */
    public static String getInfo(){
        List<String>servers=EOSUtils.getAvailableServers();
        if(servers.size()==0){
            return null;
        }
        for(int i=0;i<servers.size();i++){
            String server=servers.get(i);
            StringBuilder sb=new StringBuilder(server);
            sb.append("/"+EOSUtils.VERSION+"/"+EOSUtils.API_CHAIN+"/"+ACTION_GET_INFO);
            String url=sb.toString();
            String content=GlobalUtils.getContentFromUrl(url);
            Log.i(TAG,"geting info from:"+url+",content:"+content);
            if(!TextUtils.isEmpty(content)){
                return content;
            }
        }
        return null;
    }

    /**
     * Can't be called in UI thread
     * may return null or empty String if failed
     * curl http://mainnet.eoscanada.com/v1/chain/get_producers -X POST -d {\"json\":\"true\"}
     */
    public static String getProducers(){
        List<String>servers=EOSUtils.getAvailableServers();
        if(servers.size()==0){
            return null;
        }
        for(int i=0;i<servers.size();i++){
            String server=servers.get(i);
            StringBuilder sb=new StringBuilder(server);
            sb.append("/"+EOSUtils.VERSION+"/"+EOSUtils.API_CHAIN+"/"+ACTION_GET_PRODUCERS);
            String url=sb.toString();
            HashMap<String,String> params=new HashMap<String,String>();
            params.put(PARAM_JSON,RESULT_AS_JSON);
            String content=GlobalUtils.postToServer(url,params);
            Log.i(TAG,"geting getProducers from:"+url+",result:"+content);
            if(!TextUtils.isEmpty(content)){
                return content;
            }
        }
        return null;
    }
    /**
     * Can't be called in UI thread.
     * Get Account info.
     * ex. curl http://jungle.cryptolions.io/v1/chain/get_account -X POST -d {"account_name":"xxxxx"}
     */
    public static String getAccount(String accountName){
        List<String>servers=EOSUtils.getAvailableServers();
        if(servers.size()==0){
            return null;
        }
        for(int i=0;i<servers.size();i++){
            String server=servers.get(i);
            StringBuilder sb=new StringBuilder(server);
            sb.append("/"+EOSUtils.VERSION+"/"+EOSUtils.API_CHAIN+"/"+ACTION_GET_ACCOUNT);
            String url=sb.toString();
            HashMap<String,String> params=new HashMap<String,String>();
            params.put(PARAM_ACCOUNT_NAME,accountName);
            String content=GlobalUtils.postToServer(url,params);
            Log.i(TAG,"geting account from:"+url+" for account:"+accountName+",result:"+content);
            if(!TextUtils.isEmpty(content)){
                return content;
            }
        }
        return null;
    }

    /**
     * Can't be called in UI thread.
     * Get Block Num.
     * ex. curl https://mainnet.eoscannon.io/v1/chain/get_block -X POST -d {"block_num_or_id":"5"}
     */
    public static String getBlock(String blockNum){
        List<String>servers=EOSUtils.getAvailableServers();
        if(servers.size()==0){
            return null;
        }
        for(int i=0;i<servers.size();i++){
            String server=servers.get(i);
            StringBuilder sb=new StringBuilder(server);
            sb.append("/"+EOSUtils.VERSION+"/"+EOSUtils.API_CHAIN+"/"+ACTION_GET_BLOCK);
            String url=sb.toString();
            HashMap<String,String> params=new HashMap<String,String>();
            params.put(PARAM_BLOCK_NUMBER_OR_ID,blockNum);
            String content=GlobalUtils.postToServer(url,params);
            Log.i(TAG,"geting block from:"+url+"for block "+blockNum+",result:"+content);
            if(!TextUtils.isEmpty(content)){
                return content;
            }
        }
        return null;
    }

    /**
     * Can't be called in UI thread.
     * Get ABI
     * ex. curl https://mainnet.eoscannon.io/v1/chain/get_abi -X POST -d {"account_name":"eosio"}
     */
    public static String getABI(String accountName){
        List<String>servers=EOSUtils.getAvailableServers();
        if(servers.size()==0){
            return null;
        }
        for(int i=0;i<servers.size();i++){
            String server=servers.get(i);
            StringBuilder sb=new StringBuilder(server);
            sb.append("/"+EOSUtils.VERSION+"/"+EOSUtils.API_CHAIN+"/"+ACTION_GET_ABI);
            String url=sb.toString();
            HashMap<String,String> params=new HashMap<String,String>();
            params.put(PARAM_ACCOUNT_NAME,accountName);
            String content=GlobalUtils.postToServer(url,params);
            Log.i(TAG,"geting ABI from:"+url+"for account: "+accountName+",result:"+content);
            if(!TextUtils.isEmpty(content)){
                return content;
            }
        }
        return null;
    }

    /**
     * Can't be called in UI thread.
     * Get Code
     * ex. curl https://mainnet.eoscannon.io/v1/chain/get_abi -X POST -d {"account_name":"eosio"}
     */
    public static String getCode(String accountName){
        List<String>servers=EOSUtils.getAvailableServers();
        if(servers.size()==0){
            return null;
        }
        for(int i=0;i<servers.size();i++){
            String server=servers.get(i);
            StringBuilder sb=new StringBuilder(server);
            sb.append("/"+EOSUtils.VERSION+"/"+EOSUtils.API_CHAIN+"/"+ACTION_GET_CODE);
            String url=sb.toString();
            HashMap<String,String> params=new HashMap<String,String>();
            params.put(PARAM_ACCOUNT_NAME,accountName);
            params.put(PARAM_CODE_AS_WASM,CODE_AS_WASM);
            String content=GlobalUtils.postToServer(url,params);
            Log.i(TAG,"geting Code from:"+url+"for account: "+accountName+",result:"+content);
            if(!TextUtils.isEmpty(content)){
                return content;
            }
        }
        return null;
    }

    /**
     * Can't be called in UI thread.
     * Get Table rows
     * ex. curl http://mainnet.eoscanada.com/v1/chain/get_table_rows -X POST -d {\"code\":\"eosio\",\"scope\":\"eosio\",\"table\":\"rammarket\",\"json\":\"true\"}
     * if lower_bound,upper_bound,limit is -1, means don't input them.
     */
    public static String getTableRows(String scope,String code,String tableName,int lower_bound,int upper_bound,int limit){
        List<String>servers=EOSUtils.getAvailableServers();
        if(servers.size()==0){
            return null;
        }
        for(int i=0;i<servers.size();i++){
            String server=servers.get(i);
            StringBuilder sb=new StringBuilder(server);
            sb.append("/"+EOSUtils.VERSION+"/"+EOSUtils.API_CHAIN+"/"+ACTION_GET_TABLE_ROWS);
            String url=sb.toString();
            HashMap<String,String> params=new HashMap<String,String>();
            params.put(PARAM_SCOPE,scope);
            params.put(PARAM_CODE,code);
            params.put(PARAM_TABLE,tableName);
            params.put(PARAM_JSON,RESULT_AS_JSON);
            if(lower_bound!=-1){
                params.put("lower_bound",String.valueOf(lower_bound));
            }
            if(upper_bound!=-1){
                params.put("upper_bound",String.valueOf(upper_bound));
            }
            if(limit!=-1){
                params.put("limit",String.valueOf(upper_bound));
            }
            String content=GlobalUtils.postToServer(url,params);
            Log.i(TAG,"geting Table rows from:"+url+"for scope: "+scope+",code:"+code+",table:"+tableName+",result:"+content);
            if(!TextUtils.isEmpty(content)){
                return content;
            }
        }
        return null;
    }

    /**
     * Can't be called in UI thread.
     * Get RamPrice
     * the result is for every kB
     * Actually use get_table_rows get needed info, then calculate it according bancor
     * use: curl http://mainnet.eoscanada.com/v1/chain/get_table_rows -X POST -d {\"code\":\"eosio\",\"scope\":\"eosio\",\"table\":\"rammarket\",\"json\":\"true\"}
     *                           EOS balance
     * then: RAM price (/k)= -----------------------
     *                           RAM left  * 1024
     */
    public static String getRamPrice(){
        StringBuilder result=new StringBuilder();
        String jsonStr=getTableRows(ACCOUNT_EOSIO,ACCOUNT_EOSIO,TABLE_RAMMARKET,-1,-1,-1);
        if(TextUtils.isEmpty(jsonStr)){
            return null;
        }
        try {
            JSONObject json=new JSONObject(jsonStr);
            JSONArray rows=json.getJSONArray("rows");
            JSONObject data=rows.getJSONObject(0);
            JSONObject base=data.getJSONObject("base");
            String ramBalance=base.getString("balance");
            JSONObject quote = data.getJSONObject("quote");
            String eosBalance=quote.getString("balance");
            float ram=0;
            float eos=0;
            try {
                //remove " RAM" and " EOS" from the string.
                ramBalance=ramBalance.substring(0,ramBalance.length()-4);
                eosBalance=eosBalance.substring(0,eosBalance.length()-4);
                ram = Float.parseFloat(ramBalance);
                eos = Float.parseFloat(eosBalance);
                float ramPrice=eos/ram;//price for every Byte.
                ramPrice=ramPrice*1024;//return value is for every KB.
                result.append("RAM price:"+ramPrice);
                result.append("\nOriginal data:"+jsonStr);
            }catch(NumberFormatException e){
                Log.e(TAG,"number format error，ram:"+ram+",eos:"+eos);
                return jsonStr;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result.toString();
    }

    /**
     * Can't be called in UI thread
     * get available bp api server, results will be separated by ","
     * 1st: curl http://peer1.eoshuobipool.com:8181/v1/chain/get_producers -X POST -d {\"json\":\"true\"}, get list of bp
     * for bp url, add "bp.json",POST to it, get result.
     * The result is json containing the detail info, extract "api_endpoint" from it.
     * TODO: we need a seed server, how to get it? may need centralized way now.
     */
    public static String getAvailableAPIServer(){
        List<String>servers=EOSUtils.getAvailableServers();
        if(servers.size()==0){
            return null;
        }
        for(int i=0;i<servers.size();i++){
            String server=servers.get(i);
            StringBuilder sb=new StringBuilder(server);
            sb.append("/"+EOSUtils.VERSION+"/"+EOSUtils.API_CHAIN+"/"+ACTION_GET_PRODUCERS);
            String url=sb.toString();
            HashMap<String,String> params=new HashMap<String,String>();
            params.put(PARAM_JSON,RESULT_AS_JSON);
            String content=GlobalUtils.postToServer(url,params);
            Log.i(TAG,"geting producers from:"+url+",result:"+content);
            if(TextUtils.isEmpty(content)){
                continue;
            }
            ArrayList<String>bpUrl=new ArrayList<String>();
            try {
                //get url for each BP
                JSONObject rawBP=new JSONObject(content);
                JSONArray bps=rawBP.getJSONArray("rows");
                int size=bps.length();
                for(int bpIndex=0;bpIndex<size;bpIndex++){
                    JSONObject bp=(JSONObject)bps.getJSONObject(bpIndex);
                    String urlOfBp=bp.getString("url");
                    if(!TextUtils.isEmpty(urlOfBp)){
                        bpUrl.add(urlOfBp);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Log.i(TAG,"bpUrl.size:"+bpUrl.size());
            if(bpUrl.size()>0){
                for (int j=0;j<bpUrl.size();j++){
                    Log.i(TAG,"bpUrl("+j+")="+bpUrl.get(j).toString());
                }
            }
            if(bpUrl.size()==0){
                return null;
            }
            //get api server address from each bp,and test them, for each available, add to the result.
            final int serverCount=bpUrl.size();
            sRpcUrlTestCount=0;
            sRpcUrlList.clear();
            for(int index=0;index<serverCount;index++){
                final String bp=bpUrl.get(index);
                Log.i(TAG,"getting bp,index="+index);
                Thread t=new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String bpInfo=bp+"/bp.json";
                        String infoStr=GlobalUtils.getContentFromUrl(bpInfo);
                        Log.i(TAG,"json from "+bp+" is:"+infoStr );
                        if(!TextUtils.isEmpty(infoStr)) {
                            try {
                                JSONObject infoJson = new JSONObject(infoStr);
                                JSONArray nodes = infoJson.getJSONArray("nodes");
                                Log.i(TAG, "nodes:" + nodes);
                                if (nodes != null) {
                                    JSONObject node = nodes.getJSONObject(0);
                                    String apiUrl = node.optString("api_endpoint");
                                    Log.i(TAG, "api_endpoint:" + apiUrl);
                                    if (!TextUtils.isEmpty(apiUrl)) {
                                        long before=System.currentTimeMillis();
                                        if (testAPIServerAvailable(apiUrl)) {
                                            long after=System.currentTimeMillis();
                                            synchronized(sRpcUrlList){
                                                sRpcUrlList.add(apiUrl+"   "+(after-before)+" ms");
                                            }
                                        }
                                    }
                                }
                            } catch (JSONException e) {
                                Log.e(TAG, e.toString());
                            }
                        }
                        synchronized (sSync){
                            sRpcUrlTestCount++;
                            Log.i(TAG,"sRpcUrlTestCount:"+sRpcUrlTestCount);
                            if(sRpcUrlTestCount>=serverCount){
                                sSync.notify();
                            }
                        }
                    }
                });
                t.start();
            }
            try {
                synchronized (sSync) {
                    sSync.wait();
                }
            }catch(InterruptedException e){
                Log.e(TAG,e.toString());
            }
            StringBuilder result=new StringBuilder();
            for(String urlResult:sRpcUrlList){
                result.append(urlResult+",");
            }
            return result.toString();
        }
        return null;
    }

    /**
     * test available servers.
     * this will call get_actions, to prevent getting nodes not suppling history apis.
     * @param server
     * @return
     */
    private static boolean testAPIServerAvailable(String server){
        String testResult=getActions(server,EOSIO,-1,-1);
        if(!TextUtils.isEmpty(testResult)){
            return true;
        }
        return false;
        /*URL url=null;
        try {
            StringBuilder sb=new StringBuilder(server);
            sb.append("/"+EOSUtils.VERSION+"/"+EOSUtils.API_CHAIN+"/"+EOSOperations.ACTION_GET_ACTIONS);
            url=new URL(sb.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        HttpURLConnection httpURLConnection=null;
        try {
            httpURLConnection = (HttpURLConnection) url.openConnection();
            int responseCode=httpURLConnection.getResponseCode();
            Log.i(TAG,"testAPIServerAvailable,server:"+server+",responseCode:"+responseCode);
            if(responseCode == HttpURLConnection.HTTP_OK
                    ||responseCode==HttpURLConnection.HTTP_ACCEPTED ) {
                return true;
            } else {
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            httpURLConnection.disconnect();
        }
        return false;*/
    }

    public static String createWallet(Context context,String walletName){
        EosWalletManager manager= EosWalletManager.getInstance(context);
        String pswd=null;
        try {
            pswd= manager.create(walletName);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return pswd;
    }

    private static List<String> getServers(boolean mainNet){
        List<String>servers;
        mainNet=false;//remember to remove, always for test now now.
        if(mainNet){
            servers=EOSUtils.getAvailableServers();
        }else{
            servers=EOSUtils.getTestNetServers();
        }
        return servers;
    }

    public static List<String> getRequiredKeys(boolean mainNet,String transaction,List<String>available_keys){
        if(available_keys.size()==0){
            return null;
        }
        HashMap<String,String> params=new HashMap<String,String>();
        params.put(PARAM_TRANSACTION,transaction);
        StringBuilder keys=new StringBuilder();
        keys.append("[");
        for(int j=0;j<available_keys.size();j++){
            keys.append("\""+available_keys.get(j)+"\",");
        }
        //remove last ","
        keys.delete(keys.length()-1,keys.length());
        keys.append("]");
        params.put(PARAM_AVAILABLE_KEYS,keys.toString());
        List<String>servers=getServers(mainNet);
        if(servers.size()==0 || available_keys==null || available_keys.size()==0){
            return null;
        }
        for(int i=0;i<servers.size();i++){
            String server=servers.get(i);
            StringBuilder sb=new StringBuilder(server);
            sb.append("/"+EOSUtils.VERSION+"/"+EOSUtils.API_CHAIN+"/"+ACTION_GET_REQUIRED_KEYS);
            String url=sb.toString();
            String content=GlobalUtils.postToServer(url,params);
            Log.i(TAG,"getRequiredKeys,result:"+content);
            if(TextUtils.isEmpty(content)){
                continue;
            }
            try {
                ArrayList<String>result=new ArrayList<>();
                JSONObject json=new JSONObject(content);
                JSONArray keysResult=json.getJSONArray("required_keys");
                for(int k=0;k<keysResult.length();k++){
                    result.add(keysResult.get(k).toString());
                }
                return result;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static String jsonToBin(boolean mainNet,String contract,String action,String args){
        List<String>servers=getServers(mainNet);
        if(servers.size()==0){
            return null;
        }
        for(int i=0;i<servers.size();i++){
            String server=servers.get(i);
            StringBuilder sb=new StringBuilder(server);
            sb.append("/"+EOSUtils.VERSION+"/"+EOSUtils.API_CHAIN+"/"+ACTION_JSON_TO_BIN);
            String url=sb.toString();
            Log.i(TAG,"params in abi_json_to_bin:"+args);
            HashMap<String,String> params=new HashMap<String,String>();
            params.put(PARAM_CODE,contract);
            params.put(PARAM_ACTION,action);
            params.put(PARAM_ARGS,args);
            String content=GlobalUtils.postToServer(url,params);
            Log.i(TAG,"abi_json_to_bin from:"+url+"for code: "+contract+",action:"+action+",args:"+args+",result:"+content);
            if(!TextUtils.isEmpty(content)){
                return content;
            }
        }
        return null;
    }
    public static String jsonToBin(boolean mainNet,String contract,String action,Map<String,String>args){
        String actionData=packActionData(args);
        return jsonToBin(mainNet,contract,action,actionData);
    }

    /**
     * pack arguments in an action into Json format String
     * @param args
     * @return
     */
    private static String packActionData(Map<String,String>args){
        StringBuilder sb=new StringBuilder();
        sb.append("{");
        Set<Map.Entry<String,String>> argsEntry=args.entrySet();
        for(Map.Entry<String,String> entry:argsEntry){
            String key=entry.getKey();
            String value=entry.getValue();
            sb.append("\""+key+"\":");
            if(value.equalsIgnoreCase("false") || value.equalsIgnoreCase("true")){
                sb.append(value);
            }else{
                sb.append("\""+value+"\",");
            }
        }
        if(args.size()>0){
            sb.delete(sb.length()-1,sb.length());
        }
        sb.append("}");
        return sb.toString();
    }
    /**
     * Transfer, can't be called from UI thread.
     * eosio.token <= eosio.token::transfer        {"from":"useraaaaaaaa","to":"useraaaaaaac","quantity":"1.0000 SYS","memo":"hello world"}
     * @param from
     * @param to
     * @param amount
     * @param memo
     * @return
     */
    public static String transfer(Context context,String from,String to,String amount,String memo){
        HashMap<String,String>params=new HashMap<String,String>();
        params.put(PARAM_FROM,from);
        params.put(PARAM_TO,to);
        params.put(PARAM_QUANTITY,amount);
        params.put(PARAM_MEMO,memo);
        String bin=jsonToBin(true,EOSIO_TOKEN,ACTION_TRANSFER,params);
        if(TextUtils.isEmpty(bin)){
            return null;
        }
        try {
            JSONObject binJson=new JSONObject(bin);
            bin=binJson.getString("binargs");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ArrayList<Map<String,String>>auths=new ArrayList<Map<String,String>>();
        HashMap<String,String>auth=new HashMap<String,String>();
        auth.put(ACTOR,from);
        auth.put(PERMISSION,ACTIVE);
        auths.add(auth);
        //Action action=new Action(EOSIO_TOKEN,ACTION_TRANSFER);
        Action action=new Action(EOSIO_TOKEN,ACTION_TRANSFER,/*packActionData(params),*/bin,auths);
        ArrayList<Action> actions=new ArrayList<Action>();
        actions.add(action);
        String result=pushTransaction(context,true,actions,null);
        Log.i(TAG,"result of transfer,from:"+from+",to:"+to+",amount:"+amount+",memo:"+memo+",result:"+result);
        return result;
    }

    public static String executeAction(Context context,boolean mainNet,String account,String actionIn,Map<String,String>args,List<Map<String,String>>authsInput){
        String bin=jsonToBin(mainNet,account,actionIn,args);
        if(TextUtils.isEmpty(bin)){
            return null;
        }
        try {
            JSONObject binJson=new JSONObject(bin);
            bin=binJson.getString("binargs");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Action action=new Action(account,actionIn,/*packActionData(params),*/bin,authsInput);
        ArrayList<Action> actions=new ArrayList<Action>();
        actions.add(action);
        String result=pushTransaction(context,true,actions,null);
        Log.i(TAG,"result of executeAction,account:"+account+",actionIn:"+actionIn+",args:"+args+",authsInput:"+authsInput+",result:"+result);
        return result;
    }
    /**
     * Can't be called in UI thread.
     * This's trying to push_transaction.
     * @param mainNet, whether execute on mainnet.
     * @return just the result.
     */
    public static String pushTransaction(Context context,boolean mainNet,List<Action>actions, String[]pubKeysInput){
        EosWalletManager walletManager=EosWalletManager.getInstance(context);
        List<String>pubKeysAvailable;
        if(pubKeysInput!=null && pubKeysInput.length>0){
            pubKeysAvailable=new ArrayList<String>();
            for(int i=0;i<pubKeysInput.length;i++){
                pubKeysAvailable.add(pubKeysInput[i]);
            }
        }else{
            pubKeysAvailable=walletManager.listPubKeys();
        }
        if(pubKeysAvailable.size()==0){
            Log.i(TAG,"no unlocked wallet, please check your wallet.");
            return null;
        }
        //call get_info,to get last_irreversible_block_num
        String info=getInfo();
        if(TextUtils.isEmpty(info)){
            return null;
        }
        try {
            JSONObject infoJson = new JSONObject(info);
            int blockNo = infoJson.getInt("last_irreversible_block_num");
            if (blockNo<=0) {
                return null;
            }
            String blockTime = infoJson.getString("head_block_time");
            String chainId=infoJson.getString("chain_id");
            String expiration = getTimeAfterHeadBlockTime(blockTime, TX_EXPIRATION_IN_MILSEC);
            String block = getBlock(String.valueOf(blockNo));
            if (TextUtils.isEmpty(block)) {
                return null;
            }
            JSONObject blockJson = new JSONObject(block);
            String refBlockPref = blockJson.getString("ref_block_prefix");
            if (TextUtils.isEmpty(refBlockPref)) {
                return null;
            }
            //Attention: this transaction is just for collect informations, not for send. we'll use eoscommander's SignedTransaction class for push.
            Transaction transaction = new Transaction(expiration, blockNo, refBlockPref,actions);

            List<String>requiredPubKeys=getRequiredKeys(true,transaction.toString(),pubKeysAvailable);
            if(requiredPubKeys==null){
                Log.i(TAG,"can't get requiredPubKeys, please check whether wallet is unlock, or input keys are valid");
                return null;
            }
            List<EosPublicKey>pubKeys=new ArrayList<>();
            for(String pubKeyStr:requiredPubKeys){
                EosPublicKey pubKey=new EosPublicKey(pubKeyStr);
                pubKeys.add(pubKey);
            }

            /*to Sign transaction, need to pack it in required way.
            EosCommander supplied many classes and way to do it.
            Below operations will totally use EosCommander supplided classes to sign it.
            will create actions and transactions to do it.
            it's duplicated with some of above operations. But I don't wish to waste time rewrite those basic classes*/
            //1st:generate Actions and SignedTransaction.
            List<io.plactal.eoscommander.data.remote.model.chain.Action> actionsSign=new ArrayList<>();
            for(int i=0;i<actions.size();i++){
                Action actionOri=actions.get(i);
                io.plactal.eoscommander.data.remote.model.chain.Action actionSign=new io.plactal.eoscommander.data.remote.model.chain.Action();
                actionSign.setAccount(actionOri.mAccount);
                actionSign.setName(actionOri.mName);
                actionSign.setData(actionOri.mData);
                List<TypePermissionLevel>permissionLevels=new ArrayList<TypePermissionLevel>();
                for(Action.Authorization authOri:actionOri.mAuth){
                    TypePermissionLevel permissionLv=new TypePermissionLevel(authOri.mActor,authOri.mPermission);
                    permissionLevels.add(permissionLv);
                }
                actionSign.setAuthorization(permissionLevels);
                actionsSign.add(actionSign);
            }
            SignedTransaction signedTransaction=new SignedTransaction();
            signedTransaction.setActions(actionsSign);
            signedTransaction.putKcpuUsage(transaction.mMaxCpuUsageMS);
            signedTransaction.putNetUsageWords(transaction.mMaxNetUsageWords);
            signedTransaction.setExpiration(transaction.mExpiration);
            signedTransaction.setRefBlockNum(transaction.mRefBlockNum);
            signedTransaction.setRefBlockPrefix(Long.parseLong(transaction.mRefBlockPrefix));
            //signed the transaction.
            signedTransaction=EosWalletManager.getInstance(context).signTransaction(signedTransaction,pubKeys,new TypeChainId(chainId));
            //pack transaction
            /*Notice: the format is:
            {"compression": "none",
            "transaction": {
            ....
            }
            "signatures": ["SIGxxxxx"]}
             */
            JSONObject transactionJson=new JSONObject();
            transactionJson.put("expiration",signedTransaction.getExpiration());
            transactionJson.put("ref_block_num",signedTransaction.getRefBlockNum());
            transactionJson.put("ref_block_prefix",signedTransaction.getRefBlockPrefix());
            JSONArray contextFreeActions=new JSONArray();
            transactionJson.put("context_free_actions",contextFreeActions);//not support contextFreeActions now.
            JSONArray actionArray=new JSONArray();
            for(Action actionToPack:actions){
                actionArray.put(actionToPack.toJson());
            }
            transactionJson.put("actions",actionArray);
            JSONArray extensions=new JSONArray();
            transactionJson.put("transaction_extensions",extensions);//not support now.

            HashMap<String,String>params=new HashMap<>();
            params.put("compression","none");
            params.put("transaction",transactionJson.toString());
            StringBuilder sbSig=new StringBuilder();
            sbSig.append("[");
            for(String sig:signedTransaction.getSignatures()){
                sbSig.append("\""+sig+"\",");
            }
            sbSig.delete(sbSig.length()-1,sbSig.length());
            sbSig.append("]");
            params.put("signatures",sbSig.toString());
            List<String>servers=getServers(mainNet);
            for(String server:servers){
                StringBuilder sb=new StringBuilder(server);
                sb.append("/"+EOSUtils.VERSION+"/"+EOSUtils.API_CHAIN+"/"+ACTION_PUSH_TRANSACTION);
                String url=sb.toString();
                String result=GlobalUtils.postToServer(url,params);
                if(!TextUtils.isEmpty(result)){
                    Log.i(TAG,"result of pushTransaction:"+result);
                    return result;
                }
            }
        }catch (JSONException e){
            e.printStackTrace();
        }
        return null;
    }

    /**
     * format: 2018-08-15T03:19:48.500
     * @param diffInMilSec
     * @return
     */
    private static String getTimeAfterHeadBlockTime(String headBlockTime,int diffInMilSec) {
        DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        try {
            Date date = sdf.parse( headBlockTime);
            Calendar c = Calendar.getInstance();
            c.setTime(date);
            c.add( Calendar.MILLISECOND, diffInMilSec);
            date = c.getTime();
            return sdf.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return headBlockTime;
        }
    }

    /**
     * Don't call from UI thread.
     * @param payer
     * @param receiver
     * @param bytes
     * @return
     */
    public static String buyRamBytes(Context context,String payer,String receiver,int bytes){
        HashMap<String,String>params=new HashMap<String,String>();
        params.put(PARAM_PAYER,payer);
        params.put(PARAM_RECEIVER,receiver);
        params.put(PARAM_BYTES,String.valueOf(bytes));
        String bin=jsonToBin(true,EOSIO,ACTION_BUYRAMBYTES,params);
        if(TextUtils.isEmpty(bin)){
            return null;
        }
        try {
            JSONObject binJson=new JSONObject(bin);
            bin=binJson.getString("binargs");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ArrayList<Map<String,String>>auths=new ArrayList<Map<String,String>>();
        HashMap<String,String>auth=new HashMap<String,String>();
        auth.put(ACTOR,payer);
        auth.put(PERMISSION,ACTIVE);
        auths.add(auth);
        Action action=new Action(EOSIO,ACTION_BUYRAMBYTES,bin,auths);
        ArrayList<Action> actions=new ArrayList<Action>();
        actions.add(action);
        String result=pushTransaction(context,true,actions,null);
        Log.i(TAG,"result of buyrambytes,payer:"+payer+",receiver:"+receiver+",bytes:"+bytes+",result:"+result);
        return result;
    }

    /**
     * Don't call from UI thread.
     * @param payer
     * @param receiver
     * @param eos
     * @return
     */
    public static String buyRamEos(Context context,String payer,String receiver,String eos){
        HashMap<String,String>params=new HashMap<String,String>();
        params.put(PARAM_PAYER,payer);
        params.put(PARAM_RECEIVER,receiver);
        params.put("quant",eos+" EOS");
        String bin=jsonToBin(true,EOSIO,ACTION_BUYRAMEOS,params);
        if(TextUtils.isEmpty(bin)){
            return null;
        }
        try {
            JSONObject binJson=new JSONObject(bin);
            bin=binJson.getString("binargs");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ArrayList<Map<String,String>>auths=new ArrayList<Map<String,String>>();
        HashMap<String,String>auth=new HashMap<String,String>();
        auth.put(ACTOR,payer);
        auth.put(PERMISSION,ACTIVE);
        auths.add(auth);
        Action action=new Action(EOSIO,ACTION_BUYRAMEOS,bin,auths);
        ArrayList<Action> actions=new ArrayList<Action>();
        actions.add(action);
        String result=pushTransaction(context,true,actions,null);
        Log.i(TAG,"result of buyram,payer:"+payer+",receiver:"+receiver+",quant:"+eos+",result:"+result);
        return result;
    }

    /**
     * Don't call from UI thread
     */
    public static String sellRam(Context context,String account,int bytes){
        HashMap<String,String>params=new HashMap<String,String>();
        params.put(PARAM_ACCOUNT,account);
        params.put(PARAM_BYTES,String.valueOf(bytes));
        String bin=jsonToBin(true,EOSIO,ACTION_SELLRAM,params);
        if(TextUtils.isEmpty(bin)){
            return null;
        }
        try {
            JSONObject binJson=new JSONObject(bin);
            bin=binJson.getString("binargs");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ArrayList<Map<String,String>>auths=new ArrayList<Map<String,String>>();
        HashMap<String,String>auth=new HashMap<String,String>();
        auth.put(ACTOR,account);
        auth.put(PERMISSION,ACTIVE);
        auths.add(auth);
        Action action=new Action(EOSIO,ACTION_SELLRAM,bin,auths);
        ArrayList<Action> actions=new ArrayList<Action>();
        actions.add(action);
        String result=pushTransaction(context,true,actions,null);
        Log.i(TAG,"result of sellram,account:"+account+",bytes:"+bytes+",result:"+result);
        return result;
    }

    public static String delegatebw(Context context,String payer,String receiver,int cpu,int net){
        HashMap<String,String>params=new HashMap<String,String>();
        params.put(PARAM_FROM,payer);
        params.put(PARAM_RECEIVER,receiver);
        params.put("transfer","false");
        params.put("stake_cpu_quantity",String.valueOf(cpu)+".0000 EOS");
        params.put("stake_net_quantity",String.valueOf(net)+".0000 EOS");
        String bin=jsonToBin(true,EOSIO,ACTION_DELEGATEBW,params);
        if(TextUtils.isEmpty(bin)){
            return null;
        }
        try {
            JSONObject binJson=new JSONObject(bin);
            bin=binJson.getString("binargs");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ArrayList<Map<String,String>>auths=new ArrayList<Map<String,String>>();
        HashMap<String,String>auth=new HashMap<String,String>();
        auth.put(ACTOR,payer);
        auth.put(PERMISSION,ACTIVE);
        auths.add(auth);
        Action action=new Action(EOSIO,ACTION_DELEGATEBW,bin,auths);
        ArrayList<Action> actions=new ArrayList<Action>();
        actions.add(action);
        String result=pushTransaction(context,true,actions,null);
        Log.i(TAG,"result of delegatebw,payer:"+payer+"receiver:"+receiver+"cpu:"+cpu+",net:"+net+",result:"+result);
        return result;
    }

    public static String undelegatebw(Context context,String payer,String receiver,int cpu,int net){
        HashMap<String,String>params=new HashMap<String,String>();
        params.put(PARAM_FROM,payer);
        params.put(PARAM_RECEIVER,receiver);
        params.put("unstake_cpu_quantity",String.valueOf(cpu)+".0000 EOS");
        params.put("unstake_net_quantity",String.valueOf(net)+".0000 EOS");
        String bin=jsonToBin(true,EOSIO,ACTION_UNDELEGATEBW,params);
        if(TextUtils.isEmpty(bin)){
            return null;
        }
        try {
            JSONObject binJson=new JSONObject(bin);
            bin=binJson.getString("binargs");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ArrayList<Map<String,String>>auths=new ArrayList<Map<String,String>>();
        HashMap<String,String>auth=new HashMap<String,String>();
        auth.put(ACTOR,payer);
        auth.put(PERMISSION,ACTIVE);
        auths.add(auth);
        Action action=new Action(EOSIO,ACTION_UNDELEGATEBW,bin,auths);
        ArrayList<Action> actions=new ArrayList<Action>();
        actions.add(action);
        String result=pushTransaction(context,true,actions,null);
        Log.i(TAG,"result of undelegatebw,payer:"+payer+"receiver:"+receiver+"cpu:"+cpu+",net:"+net+",result:"+result);
        return result;
    }

    /**
     * don't call in UI thread
     * @param accountName
     * @return
     */
    public static String getActions(String server,String accountName,int pos,int offset){
        StringBuilder sb=new StringBuilder(server);
        sb.append("/"+EOSUtils.VERSION+"/"+EOSUtils.API_HISTORY+"/"+ACTION_GET_ACTIONS);
        String url=sb.toString();
        HashMap<String,String> params=new HashMap<String,String>();
        params.put(PARAM_ACCOUNT_NAME,accountName);
        params.put(PARAM_OFFSET,String.valueOf(offset));
        params.put(PARAM_POS,String.valueOf(pos));
        String content=GlobalUtils.postToServer(url,params);
        Log.i(TAG,"geting action from:"+url+"for account: "+accountName+",pos:"+pos+",offset:"+offset+",result:"+content);
        if(!TextUtils.isEmpty(content) && !content.startsWith("err:")){
            return content;
        }
        return null;
    }

    public static String getActions(String accountName,int pos,int offset){
        List<String>servers=EOSUtils.getAvailableServers();
        if(servers.size()==0){
            return null;
        }
        for(int i=0;i<servers.size();i++) {
            String server = servers.get(i);
            String content=getActions(server,accountName,pos,offset);
            if(!TextUtils.isEmpty(content)){
                return content;
            }
        }
        return null;
    }

    /**
     * Don't call from UI thread
     * @return
     */
    public static String getTransaction(String id){
        List<String>servers=EOSUtils.getAvailableServers();
        if(servers.size()==0){
            return null;
        }
        for(int i=0;i<servers.size();i++){
            String server=servers.get(i);
            StringBuilder sb=new StringBuilder(server);
            sb.append("/"+EOSUtils.VERSION+"/"+EOSUtils.API_HISTORY+"/"+ACTION_GET_TRANSACTION);
            String url=sb.toString();
            HashMap<String,String> params=new HashMap<String,String>();
            params.put("id",id);
            String content=GlobalUtils.postToServer(url,params);
            Log.i(TAG,"getTransaction from:"+url+",id:"+id+",result:"+content);
            if(!TextUtils.isEmpty(content)){
                return content;
            }
        }
        return null;
    }

    public static String binToJson(String code,String action,String bin){
        List<String>servers=EOSUtils.getAvailableServers();
        if(servers.size()==0){
            return null;
        }
        for(int i=0;i<servers.size();i++){
            String server=servers.get(i);
            StringBuilder sb=new StringBuilder(server);
            sb.append("/"+EOSUtils.VERSION+"/"+EOSUtils.API_CHAIN+"/"+ACTION_BIN_TO_JSON);
            String url=sb.toString();
            HashMap<String,String> params=new HashMap<String,String>();
            params.put(PARAM_CODE,code);
            params.put(PARAM_ACTION,action);
            params.put(PARAM_BINARGS,bin);
            String content=GlobalUtils.postToServer(url,params);
            Log.i(TAG,"binToJson from:"+url+",code:"+code+",action+"+action+",result:"+content);
            if(!TextUtils.isEmpty(content)){
                return content;
            }
        }
        return null;
    }

    /**
     * Get price, and other informations.
     * Info are get from Huobi.
     * Reference:https://github.com/huobiapi/API_Docs/wiki/REST_api_reference
     * Return:
     * Get price and information from :huobi
     *
     */
    public static String getPriceInfo(Context context){
        StringBuilder sbUrl=new StringBuilder();
        //StringBuilder sbResult=new StringBuilder();
        sbUrl.append(HUOBI_URL_MARKET);
        sbUrl.append("detail?symbol=");
        sbUrl.append(EOSUSDT);
        String priceJsonStr=GlobalUtils.getContentFromUrl(sbUrl.toString());
        if(TextUtils.isEmpty(priceJsonStr)){
            return null;
        }
        try {
            JSONObject tradePrice=new JSONObject(priceJsonStr);
            JSONObject tick=tradePrice.getJSONObject("tick");
            if(tick==null){
                return null;
            }
            //all amount traded
            String amount=tick.getString("amount");
            //price of 24h before
            String open=tick.getString("open");
            //price now
            String close=tick.getString("close");
            //highest price
            String high=tick.getString("high");
            //lowest price
            String low=tick.getString("low");
            //total usdt volume
            String volume=tick.getString("vol");
            String result=context.getString(R.string.price_info_detail,HUOBI,open,close,high,low,amount,volume);
            return result;
            //below are parsing result from curl https://api.huobipro.com/market/trade?symbol=eosusdt
            /*JSONArray data=tick.getJSONArray("data");
            if(data==null || data.length()==0){
                return null;
            }
            JSONObject tradeItem=data.getJSONObject(0);
            if(tradeItem==null){
                return null;
            }
            String amount=tradeItem.getString("amount");
            String price=tradeItem.getString("price");
            String priceStr=context.getString(R.string.price_info,HUOBI,amount,price);
            sbResult.append(priceStr);
            return sbResult.toString();*/
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getAccountProperty(Context context,String account){
        String accountInfo=getAccount(account);
        if(TextUtils.isEmpty(accountInfo)){
            return null;
        }
        try {
            JSONObject accountJson=new JSONObject(accountInfo);
            if(accountJson==null){
                return null;
            }
            String liquidBalanceStrRaw=accountJson.getString("core_liquid_balance");
            String liquidBalanceStr=liquidBalanceStrRaw.substring(0,liquidBalanceStrRaw.length()-4);
            double liquidBalance=Double.parseDouble(liquidBalanceStr);
            int ramRaw=accountJson.getInt("ram_quota");
            double netWeight=accountJson.getDouble("net_weight");
            double cpuWeight=accountJson.getDouble("cpu_weight");
            netWeight=netWeight/10000;
            cpuWeight=cpuWeight/10000;
            float ramPrice=getRawRamPrice();
            double ramEos=ramPrice*ramRaw;
            double eos=liquidBalance+netWeight+cpuWeight+ramEos;
            String priceStr=getPriceInfo(context);
            if(TextUtils.isEmpty(priceStr)){
                return context.getString(R.string.get_price_fail_info);
            }
            float price=getRawEosPrice(priceStr);
            double balance= eos*price;
            Log.i(TAG,"getAccountProperty,account:"+account+",eos:"+eos+",price:"+price+",balance:"+balance);
            /*
            Balance of %1$s:%2$s\n
        Details:\n
            Balance of %1$s:%2$f\n
        Details:\n
            liquid eos:%3$f\n
            ram:%4$f Byte\n
            cpu:%5$f\n
            net:%6$f\n
            price:%7$f
             */
            String result=context.getString(R.string.account_balance_info,account,balance,liquidBalance,ramRaw,cpuWeight,netWeight,eos,price);
            return result;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    /*result is by byte*/
    public static float getRawRamPrice(){
        /*It's like:
        RAM price:xxxx\n\nOriginal data:"+jsonStr
         */
        String priceStr=getRamPrice();
        priceStr=priceStr.substring(10);
        int pos=priceStr.indexOf("\n");
        priceStr=priceStr.substring(0,pos);
        float price=Float.parseFloat(priceStr);
        price=price/1024;
        return price;
    }

    private static float getRawEosPrice(String input){
        /*input is from getPriceInfo, it's like:
        Get latest trade info from %1$s.\n
        price of 24h before:%2$s.\n
        price of now:%3$s. \n
        highest price:%4$s.\n
        lowest price:%5$s.\n
        amount:%6$s.\n
        volumn:%7$s.
         */
        int pos=input.indexOf("price of now:");
        input=input.substring(pos+"price of now:".length());
        pos=input.indexOf(". \n");
        input=input.substring(0,pos);
        float price=Float.parseFloat(input);
        return price;
    }
}
