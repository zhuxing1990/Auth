package com.vunke.auth.base;

import android.app.Activity;
import android.os.Bundle;

import com.vunke.auth.manager.AppManager;
import com.vunke.auth.util.LogUtil;


public class BaseActivity extends Activity {
    protected BaseActivity mcontext;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mcontext = this;
        AppManager.getAppManager().addActivity(this);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 结束Activity&从堆栈中移除
        AppManager.getAppManager().finishActivity(this);
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        switch(level){
            case TRIM_MEMORY_UI_HIDDEN:
                LogUtil.i("tv_launcher", "onTrimMemory: level:TRIM_MEMORY_UI_HIDDEN");
                break;
            case TRIM_MEMORY_RUNNING_MODERATE:
                LogUtil.i("tv_launcher", "onTrimMemory: level:TRIM_MEMORY_RUNNING_MODERATE");
                break;
            case TRIM_MEMORY_RUNNING_LOW:
                LogUtil.i("tv_launcher", "onTrimMemory: level:TRIM_MEMORY_RUNNING_LOW");
                break;
            case TRIM_MEMORY_RUNNING_CRITICAL:
                LogUtil.i("tv_launcher", "onTrimMemory: level:TRIM_MEMORY_RUNNING_CRITICAL");
                break;
        }
        System.gc();
    }
}
