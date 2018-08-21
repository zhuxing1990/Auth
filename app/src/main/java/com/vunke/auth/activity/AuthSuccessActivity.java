package com.vunke.auth.activity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.widget.TextView;

import com.vunke.auth.R;
import com.vunke.auth.auth.Auth;
import com.vunke.auth.base.BaseActivity;
import com.vunke.auth.modle.AuthenticationBean;
import com.vunke.auth.modle.GroupStrategy;
import com.vunke.auth.util.Constants;
import com.vunke.auth.util.LogUtil;
import com.vunke.auth.util.SharedPreferencesUtil;
import com.vunke.auth.util.SilenceInstallUtils;
import com.vunke.auth.util.UIUtil;

import java.util.Date;

public class AuthSuccessActivity extends BaseActivity {
    private TextView authsuccess_version;
    private String user_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth_success);
        LogUtil.i("tv_launcher", "AuthSuccessActivity: onCreate ");
        initView();
//		UserInfoUtil.initUserInfo(getApplicationContext());
//		UserInfoUtil.registerBoradcastReceiver(getApplicationContext(), userinfoBroadcast1);
    }

    private void initView() {
        authsuccess_version = (TextView) findViewById(R.id.authsuccess_version);
        String versionName = UIUtil.getVersionName(mcontext);
        authsuccess_version.setText(TextUtils.isEmpty(versionName) ? "" : "版本号:" + versionName);
    }
//	BroadcastReceiver userinfoBroadcast1 = new BroadcastReceiver() {
//		@Override
//		public void onReceive(Context context, Intent intent) {
//			if (intent != null) {
//				String action = intent.getAction();
//				if (action.equals(Constants.LOAD_USER_INFO_ACTION)) {
//					user_id = intent.getStringExtra("userID");
//					SharedPreferencesUtil.setStringValue(mcontext,SharedPreferencesUtil.USER_ID,user_id);
//					try {
//						LogUtil.i("tv_launcher", "onReceive: unregisterReceiver");
//						unregisterReceiver(this);
//					}catch (Exception e){
////						e.printStackTrace();
//						LogUtil.i("tv_launcher", "onReceive: error");
//					}
//				}
//			}
//		}
//	};

    @Override
    protected void onResume() {
        LogUtil.i("tv_launcher", "AuthSuccessActivity: onResume ");
        super.onResume();
        Auth.queryUserId(mcontext);
        user_id = SharedPreferencesUtil.getStringValue(mcontext, SharedPreferencesUtil.USER_ID, "");
        AuthenticationBean authenticationBean = Auth.queryAuth(mcontext, user_id);
        if (authenticationBean.getAuth_code() != Auth.AUTH_CODE_AUTH_SUCCESS) {
            LogUtil.i("tv_launcher", "onResume:  auth code is not auth success,finish activity");
            Intent intent = new Intent(mcontext, AuthActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        boolean isFirst = SharedPreferencesUtil.getBooleanValue(mcontext, user_id + "isFirst", false);
        LogUtil.i("tv_launcher", "isFirst: " + isFirst);
        if (isFirst) {
            startEPG(mcontext, user_id);
        } else {
            SharedPreferencesUtil.setBooleanValue(mcontext, user_id + "isFirst", true);
            UIUtil.StartEPG("com.hunantv.operator", mcontext);
            GroupStrategy.GroupStrategyBean bean = Auth.getGroupStrategyBean(mcontext, user_id);
            if (!TextUtils.isEmpty(bean.getEPGpackage())) {
                LogUtil.i("tv_launcher", "get package: :" + bean.getEPGpackage().toString());
                try {
                    PackageInfo getPackageInfo = Auth.GetPackageInfo(mcontext, bean.getEPGpackage());
                    if (getPackageInfo == null) {
                        if (!TextUtils.isEmpty(bean.getApkPath())) {
                            LogUtil.i("tv_launcher", "not installed apk,start Download apk");
                            SilenceInstallUtils.DonloadEpgApk(mcontext, bean.getApkPath());
                        }
                    }
                } catch (Exception e) {
                    LogUtil.i("tv_launcher", "startEPG: get start packager failed ");
                }
            }
        }
    }

    public void startEPG(Context context, String user_id) {
        LogUtil.i("tv_launcher", "AuthSuccessActivity get AuthCode :AUTH_CODE_AUTH_SUCCESS");
        if (TextUtils.isEmpty(user_id)) {
            LogUtil.e("tv_launcher", "get user_id is null ,start epg error");
            UIUtil.sendBroadCast(context, Constants.ADVERTISING_ACTION,
                    new Intent());// 节目播放业务
            SharedPreferencesUtil.setBooleanValue(context,
                    SharedPreferencesUtil.IS_PALYED_ADVERT, true);
            // finish();
            LogUtil.i("tv_launcher",
                    "send BroadCast to play iptv,start time:" + new Date());
            return;
        }
        GroupStrategy.GroupStrategyBean bean = Auth.getGroupStrategyBean(context, user_id);
        if (!TextUtils.isEmpty(bean.getEPGpackage())) {
            LogUtil.i("tv_launcher", "get package: :" + bean.getEPGpackage().toString());
            try {
                PackageInfo getPackageInfo = Auth.GetPackageInfo(context,
                        bean.getEPGpackage());
                if (getPackageInfo != null) {
                    LogUtil.e(
                            "tv_launcher",
                            "get epg_package info success ,start epg :"
                                    + bean.getEPGpackage());
                    UIUtil.StartEPG(bean.getEPGpackage(), context);
                    UIUtil.setPackageName(context, bean.getUserId(),
                            bean.getEPGpackage());
                } else {
                    if (!TextUtils.isEmpty(bean.getApkPath())) {
                        LogUtil.i("tv_launcher", "not installed apk,start Download apk");
                        SilenceInstallUtils.DonloadEpgApk(mcontext, bean.getApkPath());
                    }
                    UIUtil.StartLastEpg(context, bean);
                }
            } catch (Exception e) {
                LogUtil.i("tv_launcher", "startEPG: get start packager failed ");
            }
        } else {
            UIUtil.StartLastEpg(context, bean);
        }
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }
}
