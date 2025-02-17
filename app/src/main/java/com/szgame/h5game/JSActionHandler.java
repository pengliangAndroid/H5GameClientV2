package com.szgame.h5game;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.webkit.ValueCallback;
import android.webkit.WebView;

import com.szgame.h5game.util.LogUtil;

/**
 * @author pengl
 */
public class JSActionHandler {
    public static final String ACTION_LOGIN = "doLogin";
    public static final String ACTION_PAY = "doPay";
    public static final String ACTION_UPDATE_ROLE_INFO = "updateRoleInfo";

    //TrackEventHelper trackEventHelper;

    private Context context;

    private GameCallBack gameCallBack;

    public interface GameCallBack{
        void onLogin();

        void onPay(String data);

        void onUpdateRoleInfo(String data);
    }

    public JSActionHandler(Context context,GameCallBack callBack){
        this.context = context;
        this.gameCallBack =  callBack;
    }

    public void doJSAction(WebView webView,String uriStr){
        Uri uri = Uri.parse(uriStr);

        String path = uri.getPath();
        String authority = uri.getAuthority();

        LogUtil.d("uri:"+uri.toString());
        LogUtil.d("authority:"+authority);
        LogUtil.d("path:"+path);

        switch (authority){
            case ACTION_LOGIN:
                String accountId = uri.getQueryParameter("accountId");
                LogUtil.d("accountId:"+accountId);

                if(gameCallBack != null)
                    gameCallBack.onLogin();

                break;
            case ACTION_PAY:
                String data = uri.getQueryParameter("data");
                LogUtil.d("data:"+data);
                if(gameCallBack != null)
                    gameCallBack.onPay(data);
                break;
            case ACTION_UPDATE_ROLE_INFO:
                data = uri.getQueryParameter("data");
                LogUtil.d("data:"+data);
                if(gameCallBack != null)
                    gameCallBack.onUpdateRoleInfo(data);
                break;
        }
    }

    public void onLoginSuccess(WebView webView,String uid,String token){
        execJavaScript(webView,"javascript:loginSuccess('"+uid+"')");
    }

    public void onLogout(WebView webView){
        execJavaScript(webView,"javascript:logout()");
    }

    /*public void initTrack(String appKey){
        LogUtil.d("initTrack");
    }*/

    private void execJavaScript(WebView webView,String script){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            webView.evaluateJavascript(script, new ValueCallback<String>() {
                @Override
                public void onReceiveValue(String value) {
                    //此处为 js 返回的结果
                    LogUtil.d("onReceiveValue:"+value);
                }
            });
        }else{
            webView.loadUrl(script);
        }
    }


}
