package com.szgame.h5game;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import com.szgame.h5game.util.LogUtil;

public class BaseActivity extends Activity {

  protected void onCreate(Bundle paramBundle) {
      super.onCreate(paramBundle);
      getWindow().requestFeature(Window.FEATURE_NO_TITLE);
      getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

      LogUtil.e("base activity onCreate:" + getResources().getConfiguration().orientation);
  }
}