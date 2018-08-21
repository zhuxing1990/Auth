package com.vunke.auth.base;

import android.app.Application;

import com.lzy.okgo.OkGo;
import com.vunke.auth.util.LogUtil;
import com.vunke.auth.util.LogcatHelper;
import com.vunke.auth.util.UIUtil;


public class BaseApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		OkGo.getInstance().init(this);
		LogcatHelper.getInstance(this).start();
		LogUtil.i("tv_launcher", "Auth versionCode:"+ UIUtil.getVersionCode(this));
		LogUtil.i("tv_launcher", "Auth versionName:"+ UIUtil.getVersionName(this));
	}

	@Override
	public void onTerminate() {
		super.onTerminate();
		LogcatHelper.getInstance(this).stop();
	}
}
