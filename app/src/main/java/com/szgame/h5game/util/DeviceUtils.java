package com.szgame.h5game.util;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 设备工具类
 */
public class DeviceUtils {
    private final static String TAG = "DeviceUtils";

    private static final String STATUS_BAR_HEIGHT_RES_NAME = "status_bar_height";

    private static String sSerialNo = null;
    private static String sDeviceId = null;
    private static String sAndroidId = null;
    private static String sSerialNumber = null;

    private static String sGuid = null;


    private static final String MANUFACTURER_XIAOMI = "Xiaomi";
    private static final String MANUFACTURER_MEIZU = "Meizu";
    private static final String PROPERY_MUI_VER = "ro.miui.ui.version.name";

    /**
     * GUID 设备识标示，避免imei号为空
     * **/
    public static String getGuid(Context context){
        if (sGuid!=null){
            return sGuid;
        }
        /*String fileName = FileUtils.getExternalStoragePath()+FileUtils.getHiddenSDKDirectory()+"guid";
        String sGuid = FileUtils.readFile2String(fileName);
        if(!TextUtils.isEmpty(sGuid)){
            return sGuid;
        }*/
//        sGuid = BaseUtils.md5Str(getAndroidId(context)+getSerialNumber()+NetUtils.getMacAddress(context));
        //FileUtils.saveToFile(sGuid, fileName);
        return sGuid;
    }

    /**
     * DEVICE_ID 不同的手机设备的IMEI，MEID或者ESN码
     */
    public static String getDeviceId(Context context) {
        if (sDeviceId != null) {
            return sDeviceId;
        }
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        try {
            sDeviceId = tm.getDeviceId();
        }catch (Exception e){
            //e.printStackTrace();
        }
        return sDeviceId;
    }

    /**
     * ANDROID_ID 设备首次启动时，系统会随机生成一个64位的数字
     */
    public static String getAndroidId(Context context) {
        if (sAndroidId != null) {
            return sAndroidId;
        }
        sAndroidId = Settings.System.getString(context.getContentResolver(), Settings.System.ANDROID_ID);
        return sAndroidId;
    }

    /**
     * Serial Number
     */
    public static String getSerialNumber() {
        if (sSerialNumber != null) {
            return sSerialNumber;
        }
        sSerialNumber = Build.SERIAL;
        return sSerialNumber;
    }

    /**
     * 获取android序列号
     *
     * @return 失败返回空字符串
     */
    public static String getSerialNo() {
        if (sSerialNo != null) {
            return sSerialNo;
        }
        String serialNo = getSystemProperty("ro.serialno");
        if (serialNo == null) {
            sSerialNo = "";
            return "";
        }
        sSerialNo = serialNo;
        return serialNo;
    }


    /**
     * 判断build号是否有效
     *
     * @return
     */
    public static boolean isValidBuild() {
        // 读取系统属性
        String model = Build.MODEL;
        return (!model.equals("sdk") && !model.equals("google_sdk"));
    }

    public static String getMetaInApplication(Context context, String key) {
        String value = "";
        ApplicationInfo info = null;
        try {
            info = context.getPackageManager().getApplicationInfo(context.getApplicationContext().getPackageName(),
                    PackageManager.GET_META_DATA);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (info != null) {
            Bundle bundle = info.metaData;
            try {
                value = bundle.getString(key);
                if (TextUtils.isEmpty(value)) {
                    value = String.valueOf(bundle.getInt(key));
                }
            } catch (Exception e) {
                //e.printStackTrace();
            }
        }

        return value;
    }


    public static int getIntMetaInApplication(Context context, String key) {
        int value = 0;
        ApplicationInfo info = null;
        try {
            info = context.getPackageManager().getApplicationInfo(context.getApplicationContext().getPackageName(),
                    PackageManager.GET_META_DATA);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (info != null) {
            Bundle bundle = info.metaData;
            value = bundle.getInt(key);
        }

        return value;
    }

    public static boolean getBooleanMetaInApplication(Context context, String key) {
        boolean value = true;
        ApplicationInfo info = null;
        try {
            info = context.getPackageManager().getApplicationInfo(context.getApplicationContext().getPackageName(),
                    PackageManager.GET_META_DATA);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (info != null) {
            Bundle bundle = info.metaData;
            try {
                value = bundle.getBoolean(key);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return value;
    }


    /**
     * 获取系统属性
     *
     * @param key 比如“ro.kernel.qemu”
     * @return 返回属性值，如果属性值不存在返回空字符串，如果发送错误，返回null。
     */
    public static String getSystemProperty(String key) {
        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method m = c.getMethod("get", String.class, String.class);
            return (String) m.invoke(c, key, "");
        } catch (ClassNotFoundException e) {
            Log.w(TAG, e.toString());
        } catch (NoSuchMethodException e) {
            Log.w(TAG, e.toString());
        } catch (IllegalArgumentException e) {
            Log.w(TAG, e.toString());
        } catch (IllegalAccessException e) {
            Log.w(TAG, e.toString());
        } catch (InvocationTargetException e) {
            Log.w(TAG, e.toString());
        }
        return null;
    }

    /**
     * 屏幕宽度
     *
     * @param context
     * @return
     */
    public static int getScreenWidth(Context context) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        if (dm.widthPixels < dm.heightPixels) {
            return dm.widthPixels;
        }
        return dm.heightPixels;

    }

    /**
     * 屏幕高度
     *
     * @param context
     * @return
     */
    public static int getScreenHeight(Context context) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        if (dm.heightPixels > dm.widthPixels) {
            return dm.heightPixels;
        }
        return dm.widthPixels;
    }

    public static float getDensity(Context context) {
        return context.getResources().getDisplayMetrics().density;
    }

    public static int dip2px(Context context, float dip) {
        final float scale = getDensity(context);
        return (int) (dip * scale + 0.5f);
    }

    public static float px2dip(Context context, float px) {
        final float scale = getDensity(context);
        return (px / scale + 0.5f);
    }

    /**
     * 将px值转换为sp值，保证文字大小不变
     *
     * @param pxValue
     * @param pxValue （DisplayMetrics类中属性scaledDensity）
     * @return
     */
    public static int px2sp(Context context, float pxValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (pxValue / fontScale + 0.5f);
    }

    /**
     * 将sp值转换为px值，保证文字大小不变
     *
     * @param spValue
     * @return
     */
    public static int sp2px(Context context, float spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    public static int getStatusBarHeight(Context ctx) {
        Class<?> c = null;
        Object obj = null;
        Field field = null;
        int x = 0, sbar = 0;
        try {
            c = Class.forName("com.android.internal.R$dimen");
            obj = c.newInstance();
            field = c.getField("status_bar_height");
            x = Integer.parseInt(field.get(obj).toString());
            sbar = ctx.getResources().getDimensionPixelSize(x);
        } catch (Exception e) {
            Log.w(TAG, e.toString());
        }
        return sbar;
    }

    /**
     * 是否是小米设备
     *
     * @return
     */
    public static boolean isXiaomiDevice() {
        return Build.MANUFACTURER.contains(MANUFACTURER_XIAOMI)
                || Build.MODEL.contains(MANUFACTURER_XIAOMI);
    }

    /**
     * 是否是魅族设备
     *
     * @return
     */
    public static boolean isMeizuDevice() {
        return Build.MANUFACTURER.contains(MANUFACTURER_MEIZU)
                || Build.MODEL.contains(MANUFACTURER_MEIZU);
    }

    /**
     * 获取MUI的版本号
     *
     * @return
     */
    public static int getMuiVersion() {
        String muiVer = getSystemProperty(PROPERY_MUI_VER);
        if (muiVer != null && muiVer.length() > 0) {
            String verNum = muiVer.substring(1, muiVer.length());
            if (verNum != null && verNum.length() > 0) {
                try {
                    int ver = Integer.parseInt(verNum);
                    return ver;
                } catch (Exception e) {

                }

            }
        }
        return 0;
    }

    /**
     * android系统状态栏高度
     *
     * @return
     */
    public static int getStatusBarHeight(Resources res) {
        int result = 0;
        int resourceId = res.getIdentifier(STATUS_BAR_HEIGHT_RES_NAME, "dimen",
                "android");
        if (resourceId > 0) {
            result = res.getDimensionPixelSize(resourceId);
        }
        return result;
    }

    @TargetApi(19)
    public static void setTranslucentStatus(Activity activity, boolean on) {
        Window win = activity.getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        if (on) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }
        win.setAttributes(winParams);
    }
}
