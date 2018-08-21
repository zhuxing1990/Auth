package com.vunke.auth.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;
import com.vunke.auth.activity.AuthSuccessActivity;
import com.vunke.auth.auth.Auth;
import com.vunke.auth.auth.AuthManager;
import com.vunke.auth.auth.Config;
import com.vunke.auth.modle.AuthInfo;
import com.vunke.auth.modle.GroupInfo;
import com.vunke.auth.modle.GroupStrategy;
import com.vunke.auth.util.Constants;
import com.vunke.auth.util.LogUtil;
import com.vunke.auth.util.LogcatHelper;
import com.vunke.auth.util.SharedPreferencesUtil;
import com.vunke.auth.util.UIUtil;
import com.vunke.auth.util.UserInfoUtil;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;


public class AuthService extends Service {
    private static final String actionName = "com.vunke.auth.auth";
    private static final String actionName2 = "com.vunke.auth.reauth";
    private AuthInfo authInfo;
    private GroupStrategy.GroupStrategyBean bean;
    private int AuthNum = 0;
    private boolean RE_AUTH = false;
    private int AuthCode;
    private String Error_Code = "1002008";
    private String Error_Info = "";
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogcatHelper.getInstance(this).start();
//        LogcatHelper.getInstance(getApplicationContext()).start();
        LogUtil.i("tv_launcher", "认证APK 启动onStartCommand: ");
//        Toast.makeText(getApplicationContext(),"认证APK解耦测试",Toast.LENGTH_SHORT).show();
//        Auth.setAuthCode(getApplicationContext(),Auth.AUTH_CODE_AUTH_NOT_AUTH);
        SharedPreferencesUtil.setBooleanValue(getApplicationContext(),"isAuth",true);
        AuthCode = Auth.AUTH_CODE_AUTH_NOT_AUTH;
         if (intent.getAction().equals(actionName2)){
            RE_AUTH = true;
             AuthCode = Auth.AUTH_CODE_AUTH_RE_AUTH;
        }else{
            RE_AUTH = false;
        }
        UserInfoUtil.registerBoradcastReceiver(getApplicationContext(),mBroadcastReceiver);
        UserInfoUtil.initUserInfo(getApplicationContext());
        return super.onStartCommand(intent, flags, startId);
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Constants.LOAD_USER_INFO_ACTION)) {
                String userName = intent.getStringExtra("userName");
                String userID = intent.getStringExtra("userID");
                SharedPreferencesUtil.setStringValue(getApplicationContext(),SharedPreferencesUtil.USER_ID,userID);
                String password = intent.getStringExtra("password");
                LogUtil.i("tv_launcher", "userName:" + userName);
                LogUtil.i("tv_launcher", "userID:" + userID);
                // LogUtil.e("tv_launcher", "password:" + password);
                if (TextUtils.isEmpty(userID)) {
                    LogUtil.e("tv_launcher", "get UserId is null");
                } else {
                    authInfo = new AuthInfo();
                    authInfo.UserId = userID;
                    authInfo.Password = password;
                    LogUtil.i("tv_launcher", "get UserId:" + authInfo.UserId);
                    boolean isAuth = SharedPreferencesUtil.getBooleanValue(getApplicationContext(), "isAuth", true);
                    if (isAuth){
                        setDelayAuth(10);
                        handler.sendEmptyMessageDelayed(0x1211, 3000);
                        LogUtil.i("tv_launcher", "auth request time："+new Date());

                    }else{
                        LogUtil.i("tv_launcher", " auth 5s repeated requests,to retun");
                    }
                }
            }
        }
    };
//    private long requestTime = 0;
    private Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case 0x1211:
                    // 判断网络链接
                    if (UIUtil.isNetConnected(getApplicationContext()) == true) {
//                        GetUserToken();
                        getUserToken2();
                    } else {
                        LogUtil.i("tv_launcher", "network not connect");
                        handler.sendEmptyMessageDelayed(0x1211, 2000);
                    }
                    break;
                case 0x1212:
//                    if (UIUtil.isNetConnected(getApplicationContext()) == true) {
//                        AuthNum++;
//                        if (AuthNum <= 2) {
//                            LogUtil.i("tv_launcher", "restart auth ,authNum:" + AuthNum);
//                            GetUserToken();
//                        } else {
//                            AuthNum = 0;
                LogUtil.i("tv_launcher", "restart auth failed,insert auth data");
                Auth.InsertAuth(getApplicationContext(),authInfo.UserId,AuthCode,Error_Code,Error_Info);
                if (RE_AUTH == true){
                    LogUtil.i("tv_launcher", "is reauth,go AuthSuccessActivity: ");
//                    goAuthSuccessActivity();
                }
                 AuthFinish();
                    break;
                case 0x1213:
                    LogUtil.i("tv_launcherr", "auth success: start AuthSuccessActivity");
//                    goAuthSuccessActivity();
                    AuthFinish();
                    break;
                default:
                    break;
            }
        }

        ;
    };

    private void setDelayAuth(final long time) {
        SharedPreferencesUtil.setBooleanValue(getApplicationContext(),"isAuth",false);
        Observable.interval(0,1, TimeUnit.SECONDS).filter(new Predicate<Long>() {
            @Override
            public boolean test(@NonNull Long aLong) throws Exception {
                return aLong <= time;
            }
        }).map(new Function<Long, Long>() {

            @Override
            public Long apply(@NonNull Long aLong) throws Exception {
                return -(aLong-time);
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableObserver<Long>() {
                    @Override
                    public void onNext(@NonNull Long aLong) {
                        if (aLong!=0){

                        }else{
                            SharedPreferencesUtil.setBooleanValue(getApplicationContext(),"isAuth",true);
                            this.dispose();
                        }
                    }
                    @Override
                    public void onError(@NonNull Throwable e) {
                        SharedPreferencesUtil.setBooleanValue(getApplicationContext(),"isAuth",true);
                        this.dispose();
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    private void goAuthSuccessActivity() {
        Config.intent = new Intent(getApplicationContext(), AuthSuccessActivity.class);
        Config.intent .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(Config.intent );
    }

//    private void goAuthActivity() {
//        Config.intent = new Intent(getApplicationContext(), AuthActivity.class);
//        Config.intent .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        startActivity(Config.intent );
//    }


    /**
     * 获取UserToken
     */
    public void getUserToken2() {
        LogUtil.i("tv_launcher", "getUserToken2: ");
        Auth.GetUserToken2(authInfo, new StringCallback() {
            @Override
            public void onSuccess(Response<String> response) {
                try {
                    String resp = response.body();
                    if (!TextUtils.isEmpty(resp)) {
                        int succes = resp.indexOf("Authentication.CTCGetAuthInfo");
                        int failed = resp.indexOf("Authentication.CTCSetConfig");

                        if (succes > 0) {
                            LogUtil.i("tv_launcher", "get usertoken success: ");
                            String re = resp.split("CTCGetAuthInfo")[1];
                            if (re.startsWith("('")) {
                                String encryToken = re.split("\\(\\'")[1].split("\\'\\)")[0];
                                LogUtil.i("tv_launcher", "encryToken:" + encryToken);
                                authInfo.EncryToken = encryToken;
                                authInfo.MacAddr =AuthManager.getMacAddr();
                                authInfo.IpAddr = AuthManager.getIpAddr();
                                authInfo.StbId = AuthManager.getSTB_ID();
                                authInfo.AccessMethod = AuthManager.getAccessMethod(getApplicationContext());
                                Auth.queryDeviceInfo(getApplicationContext(),authInfo);
                                GetAuthInfo2();
                            }
                        } else if (failed > 0) {
                            LogUtil.i("tv_launcher", "get usertoken failed: ");
                            String re = resp.split("CTCSetConfig")[1];
                            if (re.startsWith("('")) {
                                AuthCode = Auth.AUTH_CODE_AUTH_ERROR;
                                Error_Code = re.split("\\(\\'")[1].split("\\'\\)")[0];
                                LogUtil.e("tv_launcher", "get userToken failed,code:" + Error_Code);
                                handler.sendEmptyMessage(0x1212);
                            }
                        } else {
                            LogUtil.e("tv_launcher", "get userToken failed : s:" + resp);
                            AuthCode = Auth.AUTH_CODE_AUTH_ERROR;
                            handler.sendEmptyMessage(0x1212);
                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    LogUtil.e("tv_launcher", "get userToken error,get data error");
                    AuthCode = Auth.AUTH_CODE_AUTH_ERROR;
                    handler.sendEmptyMessage(0x1212);
                }
            }

            @Override
            public void onError(Response<String> response) {
                super.onError(response);
                AuthCode =Auth.AUTH_CODE_AUTH_ERROR;
                LogUtil.e("tv_launcher", "get userToken request error");
                handler.sendEmptyMessage(0x1212);
            }
        });
    }


    /**
     * 获取用户信息
     */
    private void GetAuthInfo2(){
        LogUtil.i("tv_launcher", "start GetAuthInfo2");
        Auth.GetAuthInfo2(authInfo, new StringCallback() {
            @Override
            public void onSuccess(Response<String> response) {
                String s = response.body();
                try {
                    if (!TextUtils.isEmpty(s)){
                        if (s.contains("UserGroupNMB")){
                            GroupInfo  groupInfo = new GroupInfo();
                            groupInfo.setUserToken(AuthManager.getOperate(s,"UserToken"));
                            groupInfo.setUserGroupNmb(AuthManager.getOperate(s,"UserGroupNMB"));
                            groupInfo.setEpgDomain(AuthManager.getOperate(s,"EPGDomain"));
                            groupInfo.setEpgDomainBackup(AuthManager.getOperate(s,"EPGDomainBackup"));
                            groupInfo.setUserGroupNmb(AuthManager.getOperate(s,"UserGroupNMB"));
                            groupInfo.setPlatFlag(AuthManager.getOperate(s,"PlatFlag"));
                            groupInfo.setNtPDomain(AuthManager.getOperate(s,"NTPDomain"));
                            groupInfo.setAuth_type("1");
                            LogUtil.i("tv_laucner", " groupinfo:"+groupInfo.toString());
                            if (!TextUtils.isEmpty(groupInfo
                                    .getUserGroupNmb())) {
                                AuthCode =Auth.AUTH_CODE_AUTH_SUCCESS;
                                Auth.INSERT(getApplicationContext(),
                                        authInfo, groupInfo);
                                if (RE_AUTH == true){
                                    LogUtil.i("tv_launcher", "auth success,sendMessage");
                                    handler.sendEmptyMessageDelayed(0x1213,30000L);
                                    RE_AUTH = false;
                                }
                            }
                            // 根据不同的userGroupNum 启动不同的EPG
//                            GetGroupInfo(groupInfo);
                            getStartEPGInfo(groupInfo);
                        }else if(s.contains("EPGError")){
                            InsertErrData();
                            AuthCode =Auth.AUTH_CODE_AUTH_ERROR;
                            Error_Code =  AuthManager.getOperate(s,"EPGError");
                            if (s.contains("报错信息")){
                                try {
                                    String[] split = s.split("报错信息：");
                                    Error_Info = split[1].substring(0, split[1].indexOf("<br"));
                                    LogUtil.e("tv_launcher", "onSuccess: oper:"+Error_Info);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            handler.sendEmptyMessage(0x1212);
                        }else{
                            InsertErrData();
                            AuthCode =Auth.AUTH_CODE_AUTH_ERROR;
                            handler.sendEmptyMessage(0x1212);
                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    AuthCode =Auth.AUTH_CODE_AUTH_ERROR;
                    handler.sendEmptyMessage(0x1212);
                }

            }

            @Override
            public void onError(Response<String> response) {
                super.onError(response);
                LogUtil.i("tv_launcher", "onError: get authInfo failed,request error");
                AuthCode =Auth.AUTH_CODE_AUTH_ERROR;
                handler.sendEmptyMessage(0x1212);
            }
        });
    }

    private void InsertErrData() {
        LogUtil.i("tv_launcher", "InsertErrData: ");
        GroupInfo groupInfo = new GroupInfo();
        groupInfo.setUserToken("");
        groupInfo.setUserGroupNmb("");
        groupInfo.setEpgDomain("");
        groupInfo.setEpgDomainBackup("");
        groupInfo.setUserGroupNmb("");
        groupInfo.setPlatFlag("");
        groupInfo.setNtPDomain("");
        groupInfo.setAuth_type("");
        LogUtil.i("tv_laucner", " groupinfo:"+groupInfo.toString());
        Auth.INSERT(getApplicationContext(),
                authInfo, groupInfo);
    }

    private void getStartEPGInfo(final GroupInfo groupInfo) {
//        int requestTimes = SharedPreferencesUtil.getIntValue(getApplicationContext(),"GroupInfoRequestTimes",0);
//        LogUtil.i("tv_launcher", "getStartEPGInfo: requestTimes:"+requestTimes);
//        if (requestTimes > 0){
//            requestTimes --;
//            LogUtil.i("tv_launcher", "update requestTimes:"+requestTimes);
//            SharedPreferencesUtil.setIntValue(getApplicationContext(),"GroupInfoRequestTimes",requestTimes);
//            Auth.InsertAuth(getApplicationContext(),authInfo.UserId,AuthCode,Error_Code);
//            AuthFinish();
//            return;
//        }
        LogUtil.i("tv_launcher", "getStartEPGInfo: ");
        OkGo.<String>post(Config.BASE_WS_URL3 + Config.GROUP_STRATEGY).tag(this)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        String t = response.body();
                        LogUtil.i("tv_launcher", "get groupfile success:");
                        try {
                            // saveFile(t);
                            if (!TextUtils.isEmpty(t)) {
                                Gson gson = new Gson();
                                GroupStrategy strategy = gson.fromJson(t,
                                        GroupStrategy.class);
//                                LogUtil.i("tv_launcher", "get strategy info : "+strategy.getJson().toString());
                                if (!strategy.getJson().isEmpty()
                                        && strategy.getJson() != null) {
                                    FromIterable(strategy,groupInfo);
                                } else {
                                    LogUtil.e("tv_launcher", "get group strategy error,get jsonarray isEmpty ");
                                }

                            } else {
                                LogUtil.e("tv_launcher", "get group strategy error,response data is null ");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            LogUtil.e("tv_launcher",
                                    "get group strategy failed : gson.fronJson");
                        }
                    }
                    @Override
                    public void onError(Response<String> response) {
                        super.onError(response);
                        LogUtil.e("tv_launcher",
                                "get group strategy failed request error");
                        handler.sendEmptyMessage(0x1212);
                    }

                    @Override
                    public void onFinish() {
                        super.onFinish();
                        LogUtil.i("tv_launcher", "auth over");
                        Auth.InsertAuth(getApplicationContext(),authInfo.UserId,AuthCode,Error_Code,Error_Info);
                        AuthFinish();
                    }
                });
    }

    private void AuthFinish() {
        LogUtil.i("tv_launcher", "AuthFinish: ");
        LogcatHelper.getInstance(this).stop();
        stopSelf();
    }

    private void FromIterable(GroupStrategy strategy,final GroupInfo groupInfo) {
        Observable.fromIterable(strategy.getJson()).filter(new Predicate<GroupStrategy.GroupStrategyBean>() {
            @Override
            public boolean test(@NonNull GroupStrategy.GroupStrategyBean groupStrategyBean) throws Exception {
                return groupStrategyBean.getGrpupNumber().equals(groupInfo.getUserGroupNmb());
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableObserver<GroupStrategy.GroupStrategyBean>() {
                    @Override
                    public void onNext(@NonNull GroupStrategy.GroupStrategyBean groupStrategyBean) {
                        LogUtil.i("tv_launcher","get group strategy success :"+ groupStrategyBean.getGrpupNumber());
                        bean = new GroupStrategy.GroupStrategyBean();
                        bean.setGrpupNumber(groupStrategyBean.getGrpupNumber());
                        bean.setApkPath(groupStrategyBean.getApkPath());
                        bean.setEPGcode(groupStrategyBean.getEPGcode());
                        bean.setEPGpackage(groupStrategyBean
                                .getEPGpackage());
                        bean.setGroupAddress(groupStrategyBean
                                .getGroupAddress());
                        bean.setGroupName(groupStrategyBean.getGroupName());
                        bean.setGroupStatus(groupStrategyBean
                                .getGroupStatus());
                        bean.setGroupType(groupStrategyBean.getGroupType());
                        bean.setUserId(authInfo.UserId);
                        LogUtil.e("tv_launcher", bean.toString());
                        Auth.InsertGroupStrategy( getApplicationContext(),bean);
//                        LogUtil.i("tv_launcher", "get group info success , update requestTimes 3");//更新分组信息并且替换本地更新时间
//                        SharedPreferencesUtil.setIntValue(getApplicationContext(),"GroupInfoRequestTimes",3);
                        onComplete();
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        dispose();
                    }

                    @Override
                    public void onComplete() {
                        dispose();
                    }
                });
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtil.i("tv_launcher", "AuthService onDestroy: ");
        try {
            if (mBroadcastReceiver != null) {
                LogUtil.i("tv_launcher", "注销广播:request user info");
                unregisterReceiver(mBroadcastReceiver);
            }
        } catch (IllegalArgumentException e) {
            LogUtil.i("tv_launcher", "不需要注销");
        }
    }


}
