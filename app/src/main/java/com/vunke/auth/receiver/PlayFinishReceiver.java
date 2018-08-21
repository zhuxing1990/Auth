package com.vunke.auth.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.vunke.auth.activity.AuthSuccessActivity;
import com.vunke.auth.util.LogUtil;

/**
 * Created by zhuxi on 2017/10/19.
 */
public class PlayFinishReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        LogUtil.i("tv_launcher", "PlayFinishReceiver: tv_launcher play finish,start AuthSuccessActivity");
        Intent mintent = new Intent(context, AuthSuccessActivity.class);
        mintent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(mintent);
    }
}
