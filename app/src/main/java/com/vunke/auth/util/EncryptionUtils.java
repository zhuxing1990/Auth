package com.vunke.auth.util;

/**
 * Created by zhuxi on 2017/10/24.
 */
public class EncryptionUtils {

    public static String EncryptionData(String data,int startLength,int endLength){
        if (data==null&& data.length()==0){
            return data;
        }
        try {
            String xing = "*";
            int dataLength = data.length();
            int encryotionLength = startLength+endLength;
            if (dataLength>encryotionLength){
                int contentLength = dataLength-encryotionLength;
                for (int i = 0; i < contentLength; i++) {
                    xing +="*";
                }
                data = data.replaceAll("(\\w{"+startLength+"})\\w+{"+contentLength+"}(\\w{"+endLength+"})", "$1"+xing+"$2");
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return data;
    }
}
