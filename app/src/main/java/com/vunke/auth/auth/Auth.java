package com.vunke.auth.auth;

import android.content.ComponentName;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.HttpParams;
import com.lzy.okgo.request.GetRequest;
import com.lzy.okgo.request.PostRequest;
import com.vunke.auth.modle.AuthInfo;
import com.vunke.auth.modle.AuthenticationBean;
import com.vunke.auth.modle.GroupInfo;
import com.vunke.auth.modle.GroupStrategy;
import com.vunke.auth.util.LogUtil;
import com.vunke.auth.util.SharedPreferencesUtil;
import com.vunke.auth.util.UIUtil;

import org.json.JSONObject;

import java.net.CookieStore;
import java.net.HttpCookie;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;

public class Auth {
    private static final String TAG = "Auth";

    public static final int AUTH_CODE_AUTH_NOT_AUTH = 0x98000;
    public static final int AUTH_CODE_AUTH_ERROR = 0x98002;
    public static final int AUTH_CODE_AUTH_SUCCESS = 0x98001;
    public static final int AUTH_CODE_AUTH_RE_AUTH = 0x98003;

    /**
     * 通过查询数据库获取认证信息
     *
     * @param paramContext  上下文
     * @param paramAuthInfo JavaBean
     */
    public static void queryDeviceInfo(Context paramContext,
                                       AuthInfo paramAuthInfo) {
        Uri localUri = Uri
                .parse("content://com.starcor.mango.hndx.provider/deviceinfo");
        Cursor localCursor = paramContext.getContentResolver().query(localUri,
                null, null, null, null);
        try {
            if (localCursor.moveToFirst()) {
                paramAuthInfo.AuthServer = localCursor.getString(localCursor
                        .getColumnIndex("auth_url"));
//                paramAuthInfo.StbId = localCursor.getString(localCursor
//                        .getColumnIndex("stb_id"));
//                paramAuthInfo.UserId = localCursor.getString(localCursor
//                        .getColumnIndex("user_id"));
//                paramAuthInfo.Password = localCursor.getString(localCursor
//                        .getColumnIndex("user_password"));
//                paramAuthInfo.Password = DecodePassword
//                        .decode(paramAuthInfo.Password);
                paramAuthInfo.AccessMethod = localCursor.getString(localCursor
                        .getColumnIndex("access_method"));
            }
        } finally {
            if (localCursor != null)
                localCursor.close();
        }
    }
    /**
     * 通过查询数据库获取认证信息
     *
     * @param context  上下文
     *
     */
    public static void queryUserId(Context context) {
        LogUtil.i("tv_launcher", "queryUserId: ");
        Uri localUri = Uri
                .parse("content://com.starcor.mango.hndx.provider/deviceinfo");
        Cursor localCursor = context.getContentResolver().query(localUri,
                null, null, null, null);
        try {
            if (localCursor.moveToFirst()) {
                String userId= localCursor.getString(localCursor
                        .getColumnIndex("user_id"));
                SharedPreferencesUtil.setStringValue(context,SharedPreferencesUtil.USER_ID,userId);
            }
        } finally {
            if (localCursor != null)
                localCursor.close();
        }
    }



    private static String Action = "Login";

    /**
     * 获取 UserToken
     *
     * @param authInfo
     */
    public static void GetUserToken(AuthInfo authInfo, Context context, StringCallback callback) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("userId", authInfo.UserId);
            jsonObject.put("action", Action);
            LogUtil.i("tv_launcher", jsonObject.toString());
            OkGo.<String>post(Config.BASE_WS_URL2 + Config.AUTH).tag(Auth.TAG)
                    .params("json", jsonObject.toString())
                    .execute(callback);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void GetUserToken2(AuthInfo authInfo, StringCallback callback) {
        try {
            GetRequest<String> getRequest = OkGo.<String>get(Config.BASE_WS_URL+"auth").tag(Auth.TAG)
                    .params("UserID",authInfo.UserId)
                    .params("Action", Action);
                    getRequest.execute(callback);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * 获取认证信息
     *
     * @param authInfo
     */
    public static void GetAuthInfo(AuthInfo authInfo, Context context, StringCallback callback) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("userId", authInfo.UserId);
            jsonObject.put("accessMethod", authInfo.AccessMethod);
            jsonObject.put("encryToken", authInfo.EncryToken);
            jsonObject.put("stbId", authInfo.StbId);
            jsonObject.put("mac", authInfo.MacAddr.trim());
            jsonObject.put("passWord", authInfo.Password);
            LogUtil.i("tv_laucnher", jsonObject.toString());
            PostRequest<String> postRequest = OkGo.<String>post(Config.BASE_WS_URL2 + Config.UPLOAD_AUTH_INFO)
                    .tag(Auth.TAG).params("json", jsonObject.toString());
            HttpParams params = postRequest.getParams();
            LogUtil.i(TAG, "GetAuthInfo: params:"+params.toString());
            postRequest .execute(callback);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * 插入用户信息到数据库，内容提供者 ，方便应用商城查询
     *
     * @param context
     * @param authinfo
     */
    public static void INSERT(Context context, AuthInfo authinfo, GroupInfo groupInfo) {
        LogUtil.i("tv_launcher", "insert");
        Uri uri = Uri
                .parse("content://com.huawei.hunandx.auth.provider/authinfo");//content://com.vunke.auth.auth/groupinfo
        ContentResolver resolver = context.getContentResolver();
        try {
            resolver.delete(uri, null, null);
            ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
            ops.add(ContentProviderOperation.newInsert(uri)
                    .withValue("name", "user_token")
                    .withValue("value", groupInfo.getUserToken())
                    .build());
            ops.add(ContentProviderOperation.newInsert(uri)
                    .withValue("name", "epg_platform")
                    .withValue("value", (!TextUtils.isEmpty(groupInfo.getUserToken()))?"2":"")
                    .build());
            ops.add(ContentProviderOperation.newInsert(uri)
                    .withValue("name", "family_id")
                    .withValue("value", "")
                    .build());
            ops.add(ContentProviderOperation.newInsert(uri)
                    .withValue("name", "user_group_nmb")
                    .withValue("value", groupInfo.getUserGroupNmb())
                    .build());
            ops.add(ContentProviderOperation.newInsert(uri)
                    .withValue("name", "auth_url")
                    .withValue("value", "http://10.27.40.138:8082/EDS/jsp/index.jsp")
                    .build());
            ops.add(ContentProviderOperation.newInsert(uri)
                    .withValue("name", "stb_id")
                    .withValue("value", authinfo.StbId)
                    .build());
            ops.add(ContentProviderOperation.newInsert(uri)
                    .withValue("name", "user_id")
                    .withValue("value", authinfo.UserId)
                    .build());
            ops.add(ContentProviderOperation.newInsert(uri)
                    .withValue("name", "user_password")
                    .withValue("value", authinfo.Password)
                    .build());
            ops.add(ContentProviderOperation.newInsert(uri)
                    .withValue("name", "access_method")
                    .withValue("value", authinfo.AccessMethod)
                    .build());
            ops.add(ContentProviderOperation.newInsert(uri)
                    .withValue("name", "epg_domain")
                    .withValue("value", groupInfo.getEpgDomain())
                    .build());
            ops.add(ContentProviderOperation.newInsert(uri)
                    .withValue("name", "platform_flag")
                    .withValue("value", groupInfo.getPlatFlag())
                    .build());
            ops.add(ContentProviderOperation.newInsert(uri)
                    .withValue("name", "auth_type")
                    .withValue("value", groupInfo.getAuth_type())
                    .build());
            resolver.applyBatch("com.huawei.hunandx.auth.provider", ops);
        } catch (RemoteException e) {
            LogUtil.i("tv_launcher", "insert failed");
            e.printStackTrace();
        } catch (OperationApplicationException e) {
            LogUtil.i("tv_launcher", "insert failed");
            e.printStackTrace();
        }
    }

    /**
     * 插入用户分组策略到数据库，内容提供者
     *
     * @param context
     * @param bean
     */
    public static void InsertGroupStrategy(Context context, GroupStrategy.GroupStrategyBean bean) {
        Uri uri = Uri
                .parse("content://com.vunke.auth.auth2/group_strategy");
        ContentResolver resolver = context.getContentResolver();
        ContentValues values = new ContentValues();
        try {
            values.put("epg_code", bean.getEPGcode());
            values.put("epg_package", bean.getEPGpackage());
            values.put("group_address", bean.getGroupAddress());
            values.put("group_name", bean.getGroupName());
            values.put("group_status", bean.getGroupStatus());
            values.put("group_type", bean.getGroupType());
            values.put("group_number", bean.getGrpupNumber());
            values.put("create_time", System.currentTimeMillis());
            values.put("user_id", bean.getUserId());
            values.put("apk_path", bean.getApkPath());
            Uri uri2 = resolver.insert(uri, values); // 内部调用内容提供者的insert方法
            LogUtil.i("tv_launcher", "insert date to group_strategy：" + uri2);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 查询数据库的代码
        // Cursor query = resolver.query(uri, null, null, null, null);
        // while (query.moveToNext()) {
        // // System.err.println(query.getString(query.getColumnIndex("body")));
        // // System.err
        // // .println(query.getString(query.getColumnIndex("user_id")));
        // }
    }
    @NonNull
    public static GroupStrategy.GroupStrategyBean getGroupStrategyBean(Context context, String user_id) {
        String[] strings = new String[]{user_id.trim()};
        Uri localUri = Uri
                .parse("content://com.vunke.auth.auth2/group_strategy");
        Cursor localCursor = context.getContentResolver().query(localUri,
                null, null, strings, null);
        GroupStrategy.GroupStrategyBean bean = new GroupStrategy.GroupStrategyBean();
        try {
            if (localCursor.moveToNext()) {
                bean.setEPGcode(localCursor.getString(localCursor
                        .getColumnIndex("epg_code")));
                bean.setEPGpackage(localCursor.getString(localCursor
                        .getColumnIndex("epg_package")));
                bean.setGroupAddress(localCursor.getString(localCursor
                        .getColumnIndex("group_address")));
                bean.setGroupName(localCursor.getString(localCursor
                        .getColumnIndex("group_name")));
                bean.setGroupStatus(localCursor.getString(localCursor
                        .getColumnIndex("group_status")));
                bean.setGroupType(localCursor.getString(localCursor
                        .getColumnIndex("group_type")));
                bean.setGrpupNumber(localCursor.getString(localCursor
                        .getColumnIndex("group_number")));
                // bean.setCreateTime(localCursor.getString(localCursor.getColumnIndex("create_time")));
                bean.setUserId(localCursor.getString(localCursor
                        .getColumnIndex("user_id")));
                bean.setApkPath(localCursor.getString(localCursor
                        .getColumnIndex("apk_path")));
            }
        } catch (Exception e) {
            LogUtil.e("tv_launcher", "get group_strategy error ,sql select failed");
            bean.setUserId(user_id.trim());
            UIUtil.StartLastEpg(context, bean);
        } finally {
            if (localCursor != null)
                localCursor.close();
        }
        return bean;
    }

    /**
     * 插入用户分组策略到数据库，内容提供者
     *
     * @param context
     * @param
     */
    public static void InsertAuth(Context context,String user_id,int auth_code,String error_code,String error_info) {
        LogUtil.i("tv_launcher", "InsertAuth: user_id:"+user_id);
        LogUtil.i("tv_launcher", "InsertAuth: auth_code:"+auth_code);
        LogUtil.i("tv_launcher", "InsertAuth: error_code:"+error_code);
        LogUtil.i("tv_launcher", "InsertAuth: error_info:"+error_info);
        Uri uri = Uri
                .parse("content://com.vunke.auth.authentication/auth");
        ContentResolver resolver = context.getContentResolver();
        ContentValues values = new ContentValues();
        try {
            values.put("user_id", user_id);
            values.put("auth_code", auth_code);
            if (auth_code!=AUTH_CODE_AUTH_SUCCESS){
                values.put("error_code", error_code);
                values.put("error_info", error_info);
            }
            values.put("create_time",System.currentTimeMillis());
            Uri uri2 = resolver.insert(uri, values); // 内部调用内容提供者的insert方法
            LogUtil.i("tv_launcher", "insert date to group_strategy：" + uri2);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static  AuthenticationBean  queryAuth(Context context, String user_id){
        String[] strings = new String[]{user_id.trim()};
        Uri uri = Uri.parse("content://com.vunke.auth.authentication/auth");
            Cursor cursor = context.getContentResolver().query(uri,
             null, null, strings, null);
        AuthenticationBean authenticationBean = new AuthenticationBean();
        try {
            if (cursor.moveToNext()){
                authenticationBean.setUser_id(cursor.getString(cursor.getColumnIndex("user_id")));
                authenticationBean.setAuth_code(cursor.getInt(cursor.getColumnIndex("auth_code")));
                authenticationBean.setError_code(cursor.getString(cursor.getColumnIndex("error_code")));
                authenticationBean.setError_Info(cursor.getString(cursor.getColumnIndex("error_info")));
                authenticationBean.setCreate_time(cursor.getString(cursor.getColumnIndex("create_time")));
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if (cursor != null)
                cursor.close();
        }
        return authenticationBean;
    }

    @Deprecated
    public static void StartEPG(Context context,GroupInfo groupInfo, AuthInfo authInfo, StringCallback callback) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("userGroupId", groupInfo.getUserGroupNmb());
            OkGo.<String>post(Config.BASE_WS_URL2 + Config.GetStartInfo)
                    .tag(context).params("json", jsonObject.toString())
                    .execute(callback);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static PackageInfo GetPackageInfo(Context context, String packageName) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(packageName, 0);
            if (packageInfo != null) {
                return packageInfo;
            }
        } catch (Exception e) {
//            e.printStackTrace();
            LogUtil.i("tv_launcher", "startEPG: get start packager failed ,没有安装该应用");
        }
        return null;
    }

    public static void GOtoActivity(Context context, String packageName) {
        PackageInfo pi;
        try {
            pi = context.getPackageManager().getPackageInfo(packageName, 0);
            Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
            resolveIntent.setPackage(pi.packageName);
            PackageManager pManager = context.getPackageManager();
            List apps = pManager.queryIntentActivities(resolveIntent, 0);
            ResolveInfo ri = (ResolveInfo) apps.iterator().next();
            if (ri != null) {
                packageName = ri.activityInfo.packageName;
                String className = ri.activityInfo.name;
                Intent intent = new Intent(Intent.ACTION_MAIN);
                ComponentName cn = new ComponentName(packageName, className);
                intent.setComponent(cn);
                context.startActivity(intent);
            }
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
    }


    /**
     * 被弃用
     *
     * @param url
     * @param json
     */
    protected static void GetEpgHomeAuth(String url, String json) {
        LogUtil.e("tv_launcher", "请求数据:" + json);
        // detail_data.append("\n " + userToken);
        OkGo.<String>post(url).tag(Auth.TAG).params("json", json).execute(new StringCallback() {
            @Override
            public void onSuccess(com.lzy.okgo.model.Response<String> response) {
                LogUtil.i("tv_launcher", "onSuccess:" + response.body());
            }

            @Override
            public void onError(com.lzy.okgo.model.Response<String> response) {
                super.onError(response);
                LogUtil.i("tv_launcher", "onError: ");
            }
        });
    }

    /**
     * 调用联创的接口 测试用的 暂不使用
     *
     * @param authInfo
     */
    public static void GetAuthInfo2(final AuthInfo authInfo,StringCallback callback) {
        try {
            String path = authInfo.AuthServer;
            path = path.substring(0, path.lastIndexOf('/')) + "/uploadAuthInfo";
            LogUtil.i("tv_launcher", "request path:" + path);
            // 随机数
            String random = Auth.getRandom();
//            LogUtil.i("tv_launcher", "Random:" + random);
//
//            // 密钥
//            byte[] keyCode = Auth.getKeyCode(authInfo.Password);
//            LogUtil.i("tv_launcher", "KEY:" + new String(keyCode));

//            // Auth 信息
//            String authData = random + "$" + authInfo.EncryToken + "$"
//                    + authInfo.UserId + "$" + authInfo.StbId + "$"
//                    + authInfo.IpAddr + "$" + authInfo.MacAddr.trim() + "$"
//                    + "990070|$CTC";
//
//            LogUtil.i("tv_launcher", "AUTH_DATA:" + authData);
//
//            // Auth 加密信息
//            String authenticator = Auth.DesEncrypt(authData, keyCode)
//                    .toUpperCase();

            String Authenticator = AuthManager.Authenticator(random, authInfo.EncryToken, authInfo.UserId, authInfo.StbId, authInfo.IpAddr, authInfo.MacAddr.trim(), "990070|", authInfo.Password);

            LogUtil.i("tv_launcher","Authenticator:"+Authenticator);
            PostRequest<String> postRequest = OkGo.<String>post(path).tag(Auth.TAG)
                    .params("UserID", authInfo.UserId)
                    .params("Authenticator", Authenticator)
                    .params("AccessMethod", authInfo.AccessMethod);
            postRequest.execute(callback);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static byte[] getKeyCode(String password) {
        byte[] keyCode = new byte[24];
        byte[] arrByte = password.getBytes();
        for (int i = 0; i < keyCode.length; i++) {
            if (i < 24) {
                if (i < arrByte.length) {
                    keyCode[i] = arrByte[i];
                } else {
                    keyCode[i] = 48;
                }
            }
        }
        return keyCode;
    }

    public static String getRandom() {
        // 随机数
        Random localRandom = new Random(Calendar.getInstance()
                .getTimeInMillis());
        Object[] arrayOfObject = new Object[1];
        arrayOfObject[0] = Integer.valueOf(localRandom.nextInt(99999999));
        String random = String.format("%08d", arrayOfObject);

        return random;
    }
    /**
     * 更新本地Cookie信息
     */
    public static void responseUpdateCookieHttpURL(CookieStore store) {
        boolean needUpdate = false;
        List<HttpCookie> cookies = store.getCookies();
        HashMap<String, String> cookieMap = null;
        if (cookieMap == null) {
            cookieMap = new HashMap<String, String>();
        }
        for (HttpCookie cookie : cookies) {
            String key = cookie.getName();
            String value = cookie.getValue();
            if (cookieMap.size() == 0 || !value.equals(cookieMap.get(key))) {
                needUpdate = true;
            }
            cookieMap.put(key, value);
            // BDebug.e(HTTP_COOKIE, cookie.getName() + "---->" +
            // cookie.getDomain() + "------>" + cookie.getPath());
            LogUtil.e("cookie",
                    cookie.getName() + "---->\n" + cookie.getDomain() + "---->"
                            + cookie.getPath());
        }

    }
    /**
     * DESede 加密
     *
     * @param paramString      加密信息
     * @param paramArrayOfByte 加密密钥
     * @return 十六进制代码
     * @throws Exception
     */
    public static String DesEncrypt(String paramString, byte[] paramArrayOfByte)
            throws Exception {
        Cipher localCipher = Cipher.getInstance("DESede/ECB/PKCS5Padding");
        DESedeKeySpec localDESedeKeySpec = new DESedeKeySpec(paramArrayOfByte);
        localCipher.init(1, SecretKeyFactory.getInstance("desede")
                .generateSecret(localDESedeKeySpec));
        String str = "";
        byte[] arrayOfByte = localCipher.doFinal(paramString.getBytes("ASCII"));
        return bytesToHexString(arrayOfByte);
    }

    /**
     * Convert byte[] to hex
     * string.这里我们可以将byte转换成int，然后利用Integer.toHexString(int)来转换成16进制字符串。
     *
     * @param src byte[] data
     * @return hex string
     */
    public static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }




}