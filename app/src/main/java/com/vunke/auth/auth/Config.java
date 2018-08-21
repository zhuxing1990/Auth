package com.vunke.auth.auth;

import android.content.Intent;

public class Config {
	public static Intent intent;
	/**
	 * webservice 正式地址ip
	 */
	public static final String BASE_WS_URL = "http://222.246.132.231:8298/";
	@Deprecated
	public static final String BASE_WS_URL2 = "http://124.232.135.225:8082/tvlauncher/";
//	@Deprecated
//	public static final String BASE_WS_URL2 = "http://124.232.136.239:8080/tvlauncher/";// 测试服务器的
//	public static final String BASE_WS_URL3 = "http://124.232.135.225:8082/AppStoreTV4/";//老版本正式
//	public static final String BASE_WS_URL3 = "http://124.232.136.236:8099/apk/";//老版本正式
//	public static final String BASE_WS_URL3 = "http://124.232.136.236:8099/apk/";//测试地址
	public static final String BASE_WS_URL3 = "http://124.232.135.223:8082/auth/";//新版本正式

	/**
	 *  获取 User_Token
	 */
	public static final String AUTH = "authenticationURL.do";
	/**
	 * 获取  User_Info
	 */
	public static final String UPLOAD_AUTH_INFO = "auth.do";
	/**
	 * 获取分组信息
	 */
	public static final String GROUP_STRATEGY = "GroupStrategy.jsp";//新版本配置文件
//	public static final String GROUP_STRATEGY = "GroupStrategy.html";//老版本配置文件
	/**
	 * 已弃用
	 */
	@Deprecated
	public static final String EPG_HOME_AUTH  = "/epgHomeAuth.do";
	/**
	 * 获取 Start_Info
	 */
	@Deprecated
	public static final String GetStartInfo = "/startInfo.do";



}
