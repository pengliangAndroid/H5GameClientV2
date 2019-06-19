package com.szgame.h5game;

import android.app.Application;

import com.szgame.sdk.SZGameSDKApplication;

/**
 * @author pengl
 */
public class GameApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        SZGameSDKApplication.attach(this);
    }

}
