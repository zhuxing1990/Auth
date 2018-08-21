package com.vunke.auth.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.vunke.auth.util.LogUtil;
import com.vunke.auth.util.UIUtil;


public class ConnectionChangeReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if (!UIUtil.isNetworkAvailable(context)) {
			LogUtil.i("tv_launcher", "网络未连接 ----------");
		}

		if (!UIUtil.isNetworkConnected(context)) {
			LogUtil.i("tv_launcher", "网络还未连接----------");
		}
		if (UIUtil.isNetConnected(context)) {
			LogUtil.i("tv_launcher", "网络已连接 ----------");
		}
	}

}










