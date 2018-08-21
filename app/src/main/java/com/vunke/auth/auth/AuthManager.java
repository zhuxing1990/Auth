package com.vunke.auth.auth;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.vunke.auth.util.LogUtil;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: 陈庚
 * Description:
 * Date: 2016/10/13
 * Time: 9:51
 */

public class AuthManager {

    public  static String Authenticator(String str_random,String token, String userId, String stbId, String ip, String mac, String reserved,String password) throws Exception {
        LogUtil.i("tv_launcher", "get Authenticator:");
        String str1 = str_random+"$";
        String str2 = str1+token+"$";
        String str3 = str2+userId+"$";
        String str4 = str3+stbId+"$";
        String str5 = str4+ip+"$";
        String str6 = str5+mac+"$";
        String str7 = str6+reserved+"$";
        String string = str7 + "CTC";//加密的内容
        LogUtil.i("tv_launcher", "Authenticator:密码 "+password);
        LogUtil.i("tv_launcher", "Authenticator:加密内容 "+string);
        byte[] arrayOfByte3 = new byte[24];
        byte[] arrayOfByte4 = password.getBytes();
        for (int m = 0; ; m++) {
            if (m < 24) {
                if (m < arrayOfByte4.length)
                    arrayOfByte3[m] = arrayOfByte4[m];
                else
                    arrayOfByte3[m] = 48;//48表示ASCII 字符“0”
            } else {

                String str8 = DesEncrypt(string, arrayOfByte3).toUpperCase();
                return str8;
            }
        }


    }

    private  static String DesEncrypt(String paramString, byte[] paramArrayOfByte)
            throws Exception {
        Cipher localCipher = Cipher.getInstance("DESede/ECB/PKCS5Padding");
        DESedeKeySpec localDESedeKeySpec = new DESedeKeySpec(paramArrayOfByte);
        localCipher.init(Cipher.ENCRYPT_MODE, SecretKeyFactory.getInstance("DESede").generateSecret(localDESedeKeySpec));
        String str = "";
        byte[] arrayOfByte = localCipher.doFinal(paramString.getBytes("ASCII"));
        str =  bytesToHexString(arrayOfByte);

        return str;
    }

    private static String bytesToHexString(byte[] src) {
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
    public  static String getOperate(String result,String operate) {
        operate ="'"+operate+"','";
        try {
            String[] split = result.split(operate);
            String oper = split[1].substring(0, split[1].indexOf("');"));
            return oper;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    public static String getMacAddr(){
        String mac = "00:00:00:00:00:00";
        String LocalMac= getLocalMacAddressFromBusybox();
        return  isMacAddr(LocalMac)?LocalMac:mac;
    }
    /**
     * 推荐使用 获取MAC正确 根据busybox获取本地Mac
     *
     * @return
     */
    public static String getLocalMacAddressFromBusybox() {
        String result = "";
        String Mac = "";
        result = callCmd("busybox ifconfig", "HWaddr");

        // 如果返回的result == null，则说明网络不可取
        if (result == null) {
            return "网络出错，请检查网络";
        }

        // 对该行数据进行解析
        // 例如：eth0 Link encap:Ethernet HWaddr 00:16:E8:3E:DF:67
        if (result.length() > 0 && result.contains("HWaddr") == true) {
            Mac = result.substring(result.indexOf("HWaddr") + 6, result.length() - 1);
            LogUtil.i("tv_launcher", "Mac:" + Mac + " Mac.length: " + Mac.length());
//
			/*
             * if(Mac.length()>1){ Mac = Mac.replaceAll(" ", ""); result = "";
			 * String[] tmp = Mac.split(":"); for(int i = 0;i<tmp.length;++i){
			 * result +=tmp[i]; } }
			 */
            result = Mac;
            LogUtil.i("tv_launcher", result + " result.length: " + result.length());
        }
        return result.trim();
    }
    private static String callCmd(String cmd, String filter) {
        String result = "";
        String line = "";
        try {
            Process proc = Runtime.getRuntime().exec(cmd);
            InputStreamReader is = new InputStreamReader(proc.getInputStream());
            BufferedReader br = new BufferedReader(is);

            // 执行命令cmd，只取结果中含有filter的这一行
            while ((line = br.readLine()) != null && line.contains(filter) == false) {
                // result += line;
                LogUtil.i("tv_launcher", "line: " + line);
            }

            result = line;
            LogUtil.i("tv_launcher", "result: " + result);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static String getSTB_ID(){
        String value2 = "00000000000000000000000000000000";
        try {
            Class<?> c = null;
            c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("get", String.class, String.class);
            value2 = (String)(get.invoke(c, "ro.product.stb.stbid", "unknown" ));
            LogUtil.i("tv_launcher", "getSTB_ID:"+value2);
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.i("tv_launcher","getStbId failed");
            return value2;
        }
        return value2;
    }

    /**
     * 判断MAC地址是否正确
     * @param mac
     * @return
     */
    public static boolean isMacAddr(@Nullable String mac){
        if (TextUtils.isEmpty(mac)){
            return false;
        }
        return mac.matches("([A-Fa-f0-9]{2}:){5}[A-Fa-f0-9]{2}");
    }
    /**
     * 芒果 内部 获取 10段开头的IP地址
     *
     * @return
     */
    public static String getIpAddr() {

        String str = getIpNetcfg("ppp[0-9]+");
        if ((TextUtils.isEmpty(str)) || ("0.0.0.0".equals(str))) {
            str = getIpNetcfg("eth[0-9]+");
        }
        return str;
    }

    public static String getIpNetcfg(String paramString) {
        try {
            Process process = Runtime.getRuntime().exec("netcfg");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            process.waitFor();
            Pattern pattern = Pattern.compile("^([a-z0-9]+)\\s+(UP|DOWN)\\s+([0-9./]+)\\s+.+\\s+([0-9a-f:]+)$", Pattern.CASE_INSENSITIVE);
            String str5;
            Matcher matcher = null;
            String ready ;
            while ((ready = bufferedReader.readLine()) != null) {
//                Log.i("tv_launcher", "getIpNetcfg: ready:"+ready);
                matcher = pattern.matcher(ready);
                if (matcher!=null&&matcher.matches()){
                    String str1 = matcher.group(1).toLowerCase(Locale.CHINA);
//                    String str2 = matcher.group(2);
                    String str3 = matcher.group(3);
//                    String str4 = matcher.group(4).toUpperCase(Locale.CHINA).replace(':', '-');
//                    LogUtil.i("tv_launcher", "match success:" + str1 + " " + str2 + " " + str3 + " " + str4);
                    LogUtil.i("tv_launcher", "match success:str1:"+str1+"\t str3:" + str3);
                    if(str1.matches(paramString)){
                        str5 = str3.substring(0, str3.indexOf("/"));
                        LogUtil.i("tv_launcher", "addr:" + str5);
                        return str5;
                    }
                }
            }
        } catch (java.io.IOException IOException) {
            LogUtil.i("tv_launcher", "Exception: IOException.");
            IOException.printStackTrace();
        } catch (java.lang.InterruptedException InterruptedException) {
            LogUtil.i("tv_launcher", "Exception: InterruptedException.");
            InterruptedException.printStackTrace();
        }
        return "";
    }
    public static String getAccessMethod(Context context) {
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivityManager != null) {
                NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                String typeName = networkInfo.getTypeName().toLowerCase();
                typeName = "pppoe2";
                if (!typeName.equals("pppon")&&!typeName.equals("lan")&&!typeName.equals("dhcp")){
                    return "pppoe";
                }
                return typeName;
            }
        } catch (Exception e) {
            e.printStackTrace();
            ;
        }
        return "";
    }
}
