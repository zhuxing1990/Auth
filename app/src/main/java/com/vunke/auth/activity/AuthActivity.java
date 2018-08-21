package com.vunke.auth.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.vunke.auth.R;
import com.vunke.auth.auth.Auth;
import com.vunke.auth.auth.Config;
import com.vunke.auth.base.BaseActivity;
import com.vunke.auth.modle.AuthenticationBean;
import com.vunke.auth.service.AuthService;
import com.vunke.auth.util.LogUtil;
import com.vunke.auth.util.SharedPreferencesUtil;
import com.vunke.auth.util.UIUtil;


public class AuthActivity extends BaseActivity implements View.OnClickListener{
    private Button auth_confirm;
    private TextView auth_errText;
    private TextView auth_version;
    private Handler handler = new Handler() ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtil.i("tv_launcher", "AuthActivity onCreate: ");
        setContentView(R.layout.activity_auth);
        init();
        initListener();
        Auth.queryUserId(mcontext);
//        UserInfoUtil.initUserInfo(getApplicationContext());
//        UserInfoUtil.registerBoradcastReceiver(getApplicationContext(), userinfoBroadcast2);

    }
//    BroadcastReceiver userinfoBroadcast2 = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            if (intent != null) {
//                String action = intent.getAction();
//                if (action.equals(Constants.LOAD_USER_INFO_ACTION)) {
//                    user_id =intent.getStringExtra("userID");
//                    SharedPreferencesUtil.setStringValue(mcontext,SharedPreferencesUtil.USER_ID,user_id);
//                    try {
//                        LogUtil.i("tv_launcher", "onReceive: unregisterReceiver");
//                        unregisterReceiver(this);
//                    }catch (Exception e){
////                       e.printStackTrace();
//                        LogUtil.i("tv_launcher", "onReceive: error");
//                    }
//                }
//            }
//        }
//    };
    private void init(){
        LogUtil.i("tv_launcher","AuthActivity init: ");
        auth_confirm = (Button)findViewById(R.id.auth_confirm);
        auth_errText = (TextView)findViewById(R.id.auth_errText);
        auth_version = (TextView)findViewById(R.id.auth_version);
        String versionName = UIUtil.getVersionName(mcontext);
        auth_version.setText(TextUtils.isEmpty(versionName)?"":"版本号:"+versionName);
        auth_confirm.bringToFront();
        auth_confirm.requestFocus();
    }
    private void initListener(){
        auth_confirm.setOnClickListener(this);
    }
   private String user_id;
    @Override
    protected void onResume() {
        LogUtil.i("tv_launcher", "AuthActivity onResume: ");
        super.onResume();
        try {
            user_id= SharedPreferencesUtil.getStringValue(mcontext,SharedPreferencesUtil.USER_ID,"");
            LogUtil.i("tv_launcher", "init: user_id:"+user_id);
            if (!TextUtils.isEmpty(user_id)){
                AuthenticationBean authenticationBean = Auth.queryAuth(mcontext, user_id);
                int auth_code = authenticationBean.getAuth_code();
                if (auth_code == Auth.AUTH_CODE_AUTH_SUCCESS){
                    LogUtil.i("tv_launcher", "AuthActivity init: get auth is success");
                    Config.intent = new Intent(mcontext,AuthSuccessActivity.class);
                    startActivity(Config.intent);
                    finish();
                    return;
                }
                String ErrorCode =  authenticationBean.getError_code();
                if (!TextUtils.isEmpty(ErrorCode)) {
                    LogUtil.i("tv_launcher", "AuthActivity init: get error_code"+ErrorCode);
                    if (ErrorCode.equals("1002008")) {
                        auth_errText.setText("数据请求超时，请检测网络后再试");
                    } else {
                        auth_errText.setText("用户认证失败 [" + ErrorCode + "]");
                        if (!TextUtils.isEmpty(authenticationBean.getError_Info())){
                            auth_errText.append("\n"+authenticationBean.getError_Info());
                        }
                    }
                }
            }else{
                LogUtil.i("tv_launcher",  "AuthActivity init: get user_id is null");
                auth_errText.setText("数据请求超时，正在重新认证");
                ReAuth();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.auth_confirm){
            LogUtil.i("tv_launcher", "AuthActivity onClick: restart auth");
            ReAuth();
        }
    }

    private void ReAuth() {
        LogUtil.i("tv_launcher", "AuthActivity ReAuth: ");
        Config.intent = new Intent(mcontext, AuthService.class);
        Config.intent.setAction("com.vunke.auth.reauth");
        startService(Config.intent);
        UIUtil.stopClick(auth_confirm,30);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(mcontext,AuthSuccessActivity.class);
                startActivity(intent);
                finish();
            }
        },30000L);
    }

    @Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			return false;
		}
		return super.onKeyDown(keyCode, event);
	}

    @Override
    protected void onDestroy() {
        LogUtil.i("tv_launcher", "AuthActivity onDestroy: ");
        super.onDestroy();
    }
}
