package com.vunke.auth.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.FileCallback;
import com.lzy.okgo.model.Response;

import java.io.File;

/**
 * Created by zhuxi on 2017/7/27.
 */
public class SilenceInstallUtils {

    public static void InstallApk(Context context,File ApkPath){
        try {
            LogUtil.i("tv_launcher", "InstallApk: start SilenceInstall Service");
            Intent intent = new Intent("com.android.SilenceInstall.Start");
            intent.setDataAndType(Uri.fromFile(ApkPath),"application/vnd.android.package-archive");
            context.startService(intent);
//            Intent intent = new Intent("com.android.silenceinstaller.InstallService");
//            intent.setPackage("com.android.silenceinstaller");
//            intent.setAction("com.android.SilenceInstall.Start");
//            intent.setDataAndType(Uri.fromFile(ApkPath),"application/vnd.android.package-archive");
//            context.startService(intent);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void DonloadEpgApk(final Context context,String ApkPath){
        LogUtil.i("tv_launcher", "DonloadEpgApk: ApkPath:"+ApkPath);
        OkGo.<File>post(ApkPath).tag(context).execute(new FileCallback() {
            @Override
            public void onSuccess(Response<File> response) {
                LogUtil.i("tv_launcher", "onSuccess: download apk success");
                InstallApk(context,response.body());
            }

            @Override
            public void onError(Response<File> response) {
                super.onError(response);
                LogUtil.i("tv_launcher", "onError: download failed");
            }
            //            @Override
//            public void onResponse(boolean b, File file, Request request, @Nullable Response response) {
//
//            }
//
//            @Override
//            public void onError(boolean isFromCache, Call call, @Nullable Response response, @Nullable Exception e) {
//                super.onError(isFromCache, call, response, e);
//                LogUtil.i("tv_launcher", "onError: download failed");
//            }

        });

    }
}
