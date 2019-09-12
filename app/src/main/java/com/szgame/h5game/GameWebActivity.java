package com.szgame.h5game;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.szgame.h5game.util.DeviceUtils;
import com.szgame.h5game.util.HttpUtils;
import com.szgame.h5game.util.LogUtil;
import com.szgame.sdk.SZGameSDK;
import com.szgame.sdk.base.callback.SZExitCallback;
import com.szgame.sdk.base.callback.SZPaymentCallback;
import com.szgame.sdk.base.callback.SZSDKCallback;
import com.szgame.sdk.base.model.SZOrderInfo;
import com.szgame.sdk.base.model.SZRoleInfo;
import com.szgame.sdk.base.model.SZSDKEventName;
import com.szgame.sdk.base.model.SZUserInfo;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class GameWebActivity extends BaseActivity {
    private static final int REQUEST_PERMISSION = 924;

    private static final String BUILD_SDK_PROPERTIES = "sz_webgame_config.txt";
    private static final String GAME_URL = "game_url";
    private static final String GET_CONFIG_URL = "https://passport.best91yx.com/wx/changeGameLink";

    private static final String SZ_SDK_PREFIX = "szsdk://";

    public static final String INTENT_URL = "intent_url";

    private WebView webView;
    private ImageView splashView;
    //private LinearLayout rootLayout;

    private String gameUrl /*= "http://mohuan.xiaozigame.com/index.html?x_channel=uc"*/;

    private AudioManager audioManager;
    private AudioManager.OnAudioFocusChangeListener audioListener;

    private JSActionHandler jsActionHandler;

    private SZGameSDK sdkInstance;

    protected void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        /*this.rootLayout = new LinearLayout(this);
        this.rootLayout.setOrientation(LinearLayout.VERTICAL);
        this.rootLayout.setLayoutParams(new FrameLayout.LayoutParams(-1, -1));
        this.rootLayout.setBackgroundColor(Color.WHITE);*/
        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.webView);
        splashView = findViewById(R.id.imageView);

        sdkInstance = SZGameSDK.getInstance();


        jsActionHandler = new JSActionHandler(this, new JSActionHandler.GameCallBack() {
            @Override
            public void onLogin() {
                sdkInstance.login(GameWebActivity.this);
            }

            @Override
            public void onLogout() {
                sdkInstance.logout(GameWebActivity.this);
            }

            @Override
            public void onPay(String data) {
                doPay(data);
            }

            @Override
            public void onUpdateRoleInfo(String data) {
                updateRoleInfo(data);
            }

            @Override
            public void onCreateRoleInfo(String data) {
                createRoleInfo(data);
            }

            @Override
            public void onSubmitRoleInfo(String data) {
                submitRoleInfo(data);
            }
        });

        setFinishOnTouchOutside(false);

//        Intent localIntent = getIntent();
//        if (localIntent != null) {
//            this.gameUrl = localIntent.getStringExtra("intent_url");
//            this.gameUrl = getFileGameUrl();
//        }

//        LogUtil.d("Game web url:"+gameUrl);
        this.audioManager = ((AudioManager)getSystemService(Context.AUDIO_SERVICE));
        this.audioListener = new AudioManager.OnAudioFocusChangeListener() {
            public void onAudioFocusChange(int paramInt) {

            }
        };

        initWebView();

        //getNetGameUrl();
        //loadH5Game();

        //特殊渠道检查权限
//        checkPermission(this);
        sdkInstance.onCreate(this);
        doInit();

//        splashView.setVisibility(View.GONE);
    }

    private void updateRoleInfo(String data){
        LogUtil.i("updateRoleInfo");
        try {
            JSONObject jsonObj = new JSONObject(data);

            Map<String,Object> values = new HashMap<>();
            values.put(SZSDKEventName.ParameterName.GAME_SERVER_ID,jsonObj.getInt("serverId")+"");
            values.put(SZSDKEventName.ParameterName.GAME_SERVER_NAME,jsonObj.getString("serverName"));
            values.put(SZSDKEventName.ParameterName.GAME_ROLE_ID,jsonObj.getInt("roleId")+"");
            values.put(SZSDKEventName.ParameterName.GAME_ROLE_NAME,jsonObj.getString("roleName"));

            values.put(SZSDKEventName.ParameterName.LEVEL_COUNT,jsonObj.getInt("roleLv")+"");
            values.put(SZSDKEventName.ParameterName.LEVEL_ADD_VALUE,"1");

            sdkInstance.trackEvent(SZSDKEventName.EVENT_LEVEL_ACHIEVED,values);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void submitRoleInfo(String data){
        LogUtil.i("submitRoleInfo");
        try {
            JSONObject jsonObj = new JSONObject(data);

            Map<String,Object> values = new HashMap<>();
            values.put(SZSDKEventName.ParameterName.GAME_SERVER_ID,jsonObj.getInt("serverId")+"");
            values.put(SZSDKEventName.ParameterName.GAME_SERVER_NAME,jsonObj.getString("serverName"));
            values.put(SZSDKEventName.ParameterName.GAME_ROLE_ID,jsonObj.getInt("roleId")+"");
            values.put(SZSDKEventName.ParameterName.GAME_ROLE_NAME,jsonObj.getString("roleName"));

            values.put(SZSDKEventName.ParameterName.LEVEL_COUNT,jsonObj.getInt("roleLv")+"");

            sdkInstance.trackEvent(SZSDKEventName.EVENT_ENTER_GAME,values);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void createRoleInfo(String data){
        LogUtil.i("createRoleInfo");
        SZRoleInfo roleInfo = new SZRoleInfo();
        try {
            JSONObject jsonObj = new JSONObject(data);
            roleInfo.setRoleServerId(jsonObj.getInt("serverId"));
            roleInfo.setRoleId(jsonObj.getInt("roleId")+"");
            roleInfo.setServerName(jsonObj.getString("serverName"));
            roleInfo.setRoleName(jsonObj.getString("roleName"));

            roleInfo.setVipLevel(jsonObj.getInt("vip")+"");
            roleInfo.setRoleLevel(jsonObj.getInt("roleLv")+"");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        sdkInstance.updateRoleInfo(true,roleInfo);
    }

    private int curLoginNumber;

    private void doInit() {
        sdkInstance.init(this, new SZSDKCallback() {
            @Override
            public void onInitSuccess() {
                LogUtil.i("onInitSuccess");

                //loadH5Game();
                getNetGameUrl();
            }

            @Override
            public void onInitFail(int errorCode,String message) {
                LogUtil.i("onInitFail:" + message);
                //doInit();
                onLoadGameFail();
            }

            @Override
            public void onLoginSuccess(SZUserInfo userInfo, boolean isSwitchAccount) {
                LogUtil.i("onLoginSuccess:" + userInfo.toString());
                LogUtil.i("isSwitchAccount:" + isSwitchAccount);
                jsActionHandler.onLoginSuccess(webView,userInfo.getUid(),userInfo.getToken());
                curLoginNumber = 0;
                splashView.setVisibility(View.GONE);
            }

            @Override
            public void onLoginFail(int errorCode, String message) {
                LogUtil.i("onLoginFail:" + message);

                //sdkInstance.login(GameWebActivity.this);
                /*curLoginNumber++;
                if(curLoginNumber < 3){
                    sdkInstance.login(GameWebActivity.this);
                }*/
                onLoginGameFail();
            }

            @Override
            public void onLogoutSuccess() {
                LogUtil.i("onLogoutSuccess:");

                jsActionHandler.onLogout(webView);

                sdkInstance.login(GameWebActivity.this);
            }

            @Override
            public void onLogoutFail(int errorCode, String message) {
                LogUtil.i("onLogoutFail:" + message);
                //tvTip.setText("SDK用户登出失败，请重新登出");
            }
        });
    }

    /*private String getFileGameUrl(){
        String url = "";
        JSONObject properties = FileUtils.getJSONObjectForFile(this, BUILD_SDK_PROPERTIES);
        if(properties != null){
             url = JSONUtils.getString(properties, GAME_URL);
        }else{
            LogUtil.e("read file failed:" + BUILD_SDK_PROPERTIES);
        }
        return url;
    }*/

    private boolean filterUrl(String paramString) {
        LogUtil.i("filterUrl:"+paramString);
        try {
            if ((paramString.startsWith("weixin://")) || (paramString.startsWith("alipays:")) || (paramString.startsWith("alipay"))) {
                startActivity(new Intent("android.intent.action.VIEW", Uri.parse(paramString)));
                return true;
            }else if(paramString.startsWith(SZ_SDK_PREFIX)){

                try {
                    jsActionHandler.doJSAction(webView,paramString);
                }catch (Exception e){
                    e.printStackTrace();
                }

                return true;
            }
        } catch (Exception localException) {
            localException.printStackTrace();
            return true;
        }
        return false;
    }


    private void initWebView() {
       /* this.webView = new WebView(this);
        this.webView.setBackgroundColor(Color.WHITE);*/
        this.webView.setVerticalScrollBarEnabled(false);
        this.webView.setHorizontalScrollBarEnabled(false);
        FrameLayout.LayoutParams localLayoutParams = new FrameLayout.LayoutParams(-1, -1);
        this.webView.setLayoutParams(localLayoutParams);
//        this.rootLayout.addView(this.webView);
        WebSettings localWebSettings = this.webView.getSettings();
        localWebSettings.setAllowFileAccess(true);
        if (Build.VERSION.SDK_INT >= 16) {
            localWebSettings.setAllowFileAccessFromFileURLs(true);
            localWebSettings.setAllowUniversalAccessFromFileURLs(true);
        }

        localWebSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
        localWebSettings.setSupportZoom(false);
        localWebSettings.setBuiltInZoomControls(false);
        localWebSettings.setUseWideViewPort(true);
        localWebSettings.setSupportMultipleWindows(false);
        localWebSettings.setLoadWithOverviewMode(true);
        int densityDpi = getResources().getDisplayMetrics().densityDpi;
        LogUtil.e("TermsView", "densityDpi = " + densityDpi);
        if (densityDpi == 240) {
            localWebSettings.setDefaultZoom(WebSettings.ZoomDensity.FAR);
        }else if (densityDpi == 160) {
            localWebSettings.setDefaultZoom(WebSettings.ZoomDensity.MEDIUM);
        }else if (densityDpi == 120) {
            localWebSettings.setDefaultZoom(WebSettings.ZoomDensity.CLOSE);
        }else if (densityDpi == 320) {
            localWebSettings.setDefaultZoom(WebSettings.ZoomDensity.FAR);
        } else if (densityDpi == 213) {
            localWebSettings.setDefaultZoom(WebSettings.ZoomDensity.FAR);
        }else {
            localWebSettings.setDefaultZoom(WebSettings.ZoomDensity.MEDIUM);
        }

        localWebSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        localWebSettings.setDefaultTextEncodingName("UTF-8");
//        localWebSettings.setDatabaseEnabled(true);
        localWebSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        localWebSettings.setDomStorageEnabled(true);
        localWebSettings.setJavaScriptEnabled(true);
        localWebSettings.setGeolocationEnabled(true);
        if (Build.VERSION.SDK_INT >= 21)
            localWebSettings.setMixedContentMode(0);
        //this.b.addJavascriptInterface(new a(), "android");
        localWebSettings.setPluginState(WebSettings.PluginState.ON_DEMAND);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            localWebSettings.setMediaPlaybackRequiresUserGesture(false);
        }

        this.webView.setWebChromeClient(new WebChromeClient() {
            public boolean onJsAlert(WebView paramWebView, String paramString1, String paramString2, JsResult paramJsResult) {
                return super.onJsAlert(paramWebView, paramString1, paramString2, paramJsResult);
            }

            public void onProgressChanged(WebView paramWebView, int paramInt) {
                super.onProgressChanged(paramWebView, paramInt);

                /*if(paramInt >= 80){
                    splashView.setVisibility(View.GONE);
                }*/
            }

            public void onReceivedTitle(WebView paramWebView, String paramString) {
                super.onReceivedTitle(paramWebView, paramString);
            }
        });

        clearWebViewCache();
        //loadH5Game();

    }

    private void loadH5Game() {
        if (!TextUtils.isEmpty(this.gameUrl)) {
            this.webView.loadUrl(this.gameUrl);
            this.webView.setWebViewClient(new WebViewClient() {
                public void onReceivedSslError(WebView paramWebView, SslErrorHandler paramSslErrorHandler, SslError paramSslError) {
                    final String packageName = GameWebActivity.this.getPackageName();
                    final PackageManager pm = GameWebActivity.this.getPackageManager();

                    ApplicationInfo appInfo;
                    try {
                        appInfo = pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
                        if ((appInfo.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
                            // debug = true
                            //handler.proceed(); 这里是问题代码
                            paramSslErrorHandler.cancel();
                            return;
                        } else {
                            // debug = false
                            super.onReceivedSslError(paramWebView, paramSslErrorHandler, paramSslError);
                        }
                    } catch (PackageManager.NameNotFoundException e) {
                        // When it doubt, lock it out!
                        super.onReceivedSslError(paramWebView, paramSslErrorHandler, paramSslError);
                    }
                }

                public boolean shouldOverrideUrlLoading(WebView paramWebView, String paramString) {
                    if (paramString == null)
                        return false;
                    return filterUrl(paramString);
                }


                @Override
                public void onPageFinished(WebView view, String url) {
                    super.onPageFinished(view, url);
//                    splashView.setVisibility(View.GONE);

                }
            });
        }
    }

    private void doPay(String data){
        LogUtil.i("onPay:"+data);
        ItemInfo itemInfo = buildItemInfo(data);
        //onPay(itemInfo);

        final SZOrderInfo orderInfo = new SZOrderInfo();
        //orderInfo.setItemName("100个元宝");
        orderInfo.setItemName(itemInfo.getGoodName());
        orderInfo.setItemId(itemInfo.getGoodId()+"");
        //orderInfo.setItemDetail("充值1.00获得100个元宝");
        orderInfo.setItemPrice(itemInfo.getAmount());
        //orderInfo.setItemPrice("0.01");
        orderInfo.setItemOrderId(itemInfo.getOutTradeNo());
        //orderInfo.setExtraParams("xxx");
        orderInfo.setGameServerId(itemInfo.getServerId());

        sdkInstance.pay(this, orderInfo, new SZPaymentCallback() {
            @Override
            public void onCreateOrderSuccess(String itemOrderId) {
                LogUtil.i("onCreateOrderSuccess:" + itemOrderId);
                //showToast("创建订单成功，订单号为" + itemOrderId);
            }

            @Override
            public void onPaySuccess(String itemOrderId) {
                LogUtil.i("onPaySuccess:" + itemOrderId);
                //showToast("支付成功，订单号为" + itemOrderId);
            }

            @Override
            public void onPayFailed(String itemOrderId, String errorMsg) {
                LogUtil.i("onPayFailed:" + itemOrderId + ",errorMsg:" + errorMsg);
                //showToast("支付失败，订单号为" + itemOrderId);
            }

        });
    }

    private ItemInfo buildItemInfo(String json){
        //{"amount":1,"paytime":"1558575868","goodId":1,"goodName":"钻石","roleId":131074,
        // "roleName":"天空媚儿","serverId":2,"serverName":"2服","notifyUrl":"https://mzqysdk.xiaozigame.com/gmweb_entrance/uc/pay","extension":"1-13-131074-2-uc_gs_nc7le74ucb8cpydi-1558575868","outTradeNo":"1-13-131074-2-uc_gs_nc7le74ucb8cpydi-1558575868","rolelv":13}

        ItemInfo itemInfo = new ItemInfo();
        try {
            JSONObject jsonObj = new JSONObject(json);
            itemInfo.setExtension(jsonObj.getString("extension"));
            itemInfo.setGoodId(jsonObj.getInt("goodId"));

            int price = jsonObj.getInt("amount");
            /*BigDecimal decimal = new BigDecimal(itemInfo.getGame_price());
            String price = decimal.setScale(2, RoundingMode.HALF_UP).toString();*/
            Log.d("price",price+"");
            itemInfo.setAmount(Double.valueOf(price)+"");
            itemInfo.setRoleId(jsonObj.getInt("roleId"));
            itemInfo.setNotifyUrl(jsonObj.getString("notifyUrl"));
            itemInfo.setOutTradeNo(jsonObj.getString("outTradeNo"));
            itemInfo.setPaytime(jsonObj.getString("paytime"));
            itemInfo.setRoleName(jsonObj.getString("roleName"));
            itemInfo.setRolelv(jsonObj.getInt("rolelv"));
            itemInfo.setServerName(jsonObj.getString("serverName"));
            itemInfo.setServerId(jsonObj.getInt("serverId"));
            itemInfo.setGoodName(jsonObj.getString("goodName"));

        } catch (JSONException e) {
            e.printStackTrace();
        }


        return itemInfo;
    }

    private ItemInfo buildSZItemInfo(String json){
        //{"game_id":"147","cpOrderId":"1-10-1245189-5-shangzaoSwpc_-1560314521","userId":"","gameServerId":5,"totalFee":10,"coinName":"钻石","ratio":10,"productName":"钻石"}

        ItemInfo itemInfo = new ItemInfo();
        try {
            JSONObject jsonObj = new JSONObject(json);
//            itemInfo.setExtension(jsonObj.getString("extension"));
//            itemInfo.setGoodId(jsonObj.getInt("goodId"));

            int price = jsonObj.getInt("totalFee");
            /*BigDecimal decimal = new BigDecimal(itemInfo.getGame_price());
            String price = decimal.setScale(2, RoundingMode.HALF_UP).toString();*/
            Log.d("price",price+"");
            itemInfo.setAmount(Double.valueOf(price)+"");
//            itemInfo.setRoleId(jsonObj.getInt("roleId"));
//            itemInfo.setNotifyUrl(jsonObj.getString("notifyUrl"));
            itemInfo.setOutTradeNo(jsonObj.getString("cpOrderId"));
//            itemInfo.setPaytime(jsonObj.getString("paytime"));
//            itemInfo.setRoleName(jsonObj.getString("roleName"));
//            itemInfo.setRolelv(jsonObj.getInt("rolelv"));
//            itemInfo.setServerName(jsonObj.getString("serverName"));
            itemInfo.setServerId(jsonObj.getInt("gameServerId"));
            itemInfo.setGoodName(jsonObj.getString("productName"));

        } catch (JSONException e) {
            e.printStackTrace();
        }


        return itemInfo;
    }

    public void onConfigurationChanged(Configuration paramConfiguration) {
        super.onConfigurationChanged(paramConfiguration);
        sdkInstance.onConfigurationChanged(paramConfiguration);
    }


    /*@Override
    public void onBackPressed() {
        //super.onBackPressed();
        new AlertDialog.Builder(this,AlertDialog.THEME_HOLO_DARK)
                .setTitle(null)
                .setMessage("确定退出游戏？")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                        android.os.Process.killProcess(android.os.Process.myPid());
                    }
                })
                .setNegativeButton("取消",null)
                .setCancelable(true)
                .show();
    }
*/

    private void checkPermission(Activity activity){
        //if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
        //当targetVersion 较大时  动态申请读写权限和读取手机状态等权限(一般此方法在onCreate执行)  具体权限 具体而定
//        try {
//            //check权限
//            if ((ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED)
//                    || (ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {
//                //没有,申请权限权限数组
//                ActivityCompat.requestPermissions(activity, new String[] { Manifest.permission.READ_PHONE_STATE ,Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION);
//            } else {
//                // 有 则执行初始化
//                //startMain();
//                sdkInstance.onCreate(this);
//                doInit();
//            }
//        } catch (Exception e) {
//            //异常  继续申请
//            ActivityCompat.requestPermissions(activity, new String[] { Manifest.permission.READ_PHONE_STATE ,Manifest.permission.WRITE_EXTERNAL_STORAGE }, REQUEST_PERMISSION);
//        }
        //}
    }

    /*public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        SGameLog.i("onRequestPermissionsResult:"+requestCode);
        if(requestCode != REQUEST_PERMISSION)
            return;

        if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
            SGameLog.i("onRequestPermissionsResult PERMISSION_GRANTED");
            //申请成功
            //startMain();
            sdkInstance.onCreate(this);
            doInit();
        } else {
            SGameLog.i("onRequestPermissionsResult not PERMISSION_GRANTED");
            //失败  这里逻辑以游戏为准 这里只是模拟申请失败 cp方可改为继续申请权限 或者退出游戏 或者其他逻辑
            checkPermission(this);
        }
    }*/


    @Override
    protected void onStart() {
        super.onStart();
        sdkInstance.onStart(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        LogUtil.i("onPause");
        sdkInstance.onPause(this);
        
        for (int i = 0; i < 10; i++) {
            int result = this.audioManager.requestAudioFocus(this.audioListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
            LogUtil.i("requestAudioFocus");
            if(result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED){
                break;
            }
        }

        /*if (this.webView != null)
            this.webView.onPause();*/
    }

    @Override
    protected void onResume() {
        super.onResume();
        sdkInstance.onResume(this);

        if (this.audioManager != null)
            this.audioManager.abandonAudioFocus(audioListener);

        if (this.webView != null)
            this.webView.resumeTimers();

        if (this.webView != null)
            this.webView.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        sdkInstance.onStop(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sdkInstance.onDestroy(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        sdkInstance.onNewIntent(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        sdkInstance.onActivityResult(requestCode, resultCode, data);
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            sdkInstance.exit(this, new SZExitCallback() {
                @Override
                public void onExitSuccess() {
                    finish();
                    android.os.Process.killProcess(android.os.Process.myPid());
                }

                @Override
                public void onExitFail(int errorCode, String message) {

                }
            });
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    protected void onRestart() {
        super.onRestart();
    }


    public void getNetGameUrl() {
        LogUtil.d("getNetGameUrl:"+GET_CONFIG_URL);
        String gameId = DeviceUtils.getIntMetaInApplication(this, "SZ_GAME_ID")+"";
        String packageId = DeviceUtils.getIntMetaInApplication(this, "SZ_PACKAGE_ID")+"";
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("game_id",gameId);
            jsonObject.put("package_id",packageId);
            //Android容器
            jsonObject.put("web_type",2);

            LogUtil.d("data:"+jsonObject.toString());

            HttpUtils.doPostJsonAsyn(GET_CONFIG_URL, jsonObject.toString(), new HttpUtils.CallBack() {
                @Override
                public void onRequestComplete(String result) {
                    try {
                        LogUtil.d("result:"+result);
                        JSONObject jsonObj = new JSONObject(result);
                        int code = jsonObj.getInt("code");

                        if(code == 0){
                            JSONObject data = jsonObj.getJSONObject("data");
                            String link = data.getString("game_link");
                            //
//                            final String appKey = data.getString("app_key");
                            LogUtil.d("gameUrl:"+link);
//                            link = "https://h5.best91yx.com/v7web/play.html?game_id=1007&web_type=0&packet_id=1007000003";
//                            LogUtil.d("appKey:"+appKey);

                            if(TextUtils.isEmpty(link)){
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        onLoadGameFail();
                                    }
                                });
                                return;
                            }

                            gameUrl = link;

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    loadH5Game();
                                }
                            });

                        }else{
                            onLoadGameFail();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        onLoadGameFail();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void onLoadGameFail(){
        //Toast.makeText(this,"加载游戏失败",Toast.LENGTH_SHORT).show();
        new AlertDialog.Builder(this,AlertDialog.THEME_HOLO_DARK)
                .setTitle(null)
                .setMessage("加载游戏失败,确定重新加载游戏？")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        //getNetGameUrl();
                        doInit();
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        finish();
                        android.os.Process.killProcess(android.os.Process.myPid());
                    }
                })
                .setCancelable(true)
                .show();
    }

    private void onLoginGameFail(){
        new AlertDialog.Builder(this/*,AlertDialog.THEME_HOLO_DARK*/)
                .setTitle(null)
                .setMessage("登录游戏失败,确定重新登录？")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        sdkInstance.login(GameWebActivity.this);
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                       /* finish();
                        android.os.Process.killProcess(android.os.Process.myPid());*/
                    }
                })
                .setCancelable(true)
                .show();
    }


    public void clearWebViewCache(){
        //清理Webview缓存数据库
        try {
            deleteDatabase("webview.db");
            deleteDatabase("webviewCache.db");
        } catch (Exception e) {
            e.printStackTrace();
        }

        //WebView 缓存文件

        File webviewCacheDir = new File(getCacheDir().getAbsolutePath()+"/webviewCache");
        LogUtil.i("webviewCacheDir path="+webviewCacheDir.getAbsolutePath());

        //删除webview 缓存目录
        if(webviewCacheDir.exists()){
            deleteFile(webviewCacheDir);
        }

//        webView.clearCache(true);
    }

    /**
     * 递归删除 文件/文件夹
     *
     * @param file
     */
    public void deleteFile(File file) {

        LogUtil.i("delete file path=" + file.getAbsolutePath());

        if (file.exists()) {
            if (file.isFile()) {
                file.delete();
            } else if (file.isDirectory()) {
                File files[] = file.listFiles();
                for (int i = 0; i < files.length; i++) {
                    deleteFile(files[i]);
                }
            }
            file.delete();
        } else {
            LogUtil.e("delete file no exists " + file.getAbsolutePath());
        }
    }
}
