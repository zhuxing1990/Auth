package com.vunke.auth.modle;

/**
 * Created by zhuxi on 2016/10/14.
 */
public class AuthInfo {
    public String AccessMethod;
    public String AuthServer ="http://222.246.132.231:8298/auth";
//    public String AuthServer ="http://124.232.135.227:8298/auth";
    public String IpAddr;
    public String MacAddr;
    public String Password;
    public String StbId;
    public String UserId;
    public String EncryToken;

    public String toString()
    {
        return "AuthInfo {AuthServer='" + this.AuthServer + '\'' + ", MacAddr='" + this.MacAddr + '\'' + ", IpAddr='" + this.IpAddr + '\'' + ", StbId='" + this.StbId + '\'' + ", UserId='" + this.UserId + '\'' + ", Password='" + this.Password + '\'' + ", AccessMethod='" + this.AccessMethod + '\'' + '}';
    }
}
