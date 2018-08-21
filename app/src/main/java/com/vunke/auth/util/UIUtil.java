package com.vunke.auth.util;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.vunke.auth.R;
import com.vunke.auth.activity.AuthActivity;
import com.vunke.auth.auth.Auth;
import com.vunke.auth.modle.GroupStrategy;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;


public class UIUtil {

	public static int getScreenWidth(Context context) {
		return context.getResources().getDisplayMetrics().widthPixels;
	}

	public static int getScreenHeight(Context context) {
		return context.getResources().getDisplayMetrics().heightPixels;
	}

	public static float getScreenDensity(Context context) {
		return context.getResources().getDisplayMetrics().density;
	}
	public static void showToast(Activity aty,String msg) {
		Toast.makeText(aty, msg, Toast.LENGTH_LONG).show();
	}

	public static void showToast(Activity aty,int id) {
		Toast.makeText(aty, id, Toast.LENGTH_LONG).show();
	}

	public static void showShortToast(Activity aty,String msg) {
		Toast.makeText(aty, msg, Toast.LENGTH_SHORT).show();
	}

	public static void showShortToast(Activity aty,int id) {
		Toast.makeText(aty, id, Toast.LENGTH_SHORT).show();
	}

	private static long lastClickTime = 0;
	// 防止按钮重复点击
	public static boolean isFastDoubleClick(float ts) {
		long time = System.currentTimeMillis();
		long timeD = time - lastClickTime;
		lastClickTime = time;
		if (0 < timeD && timeD < ts * 1000) {
			return true;
		}
		return false;
	}

	/**
	 * 隐藏软键盘，只在edittext没有获取焦点时有用
	 * @param aty
	 */
	public static void hideSoftKeyboard(Activity aty) {
		aty.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN); // 隐藏意见反馈残留的软键盘
	}

	/**
	 * 隐藏软键盘
	 */
	public static void hideSoftKeyboard(Context mcontext,EditText v) {
	    InputMethodManager imm = (InputMethodManager) mcontext.getSystemService(Context.INPUT_METHOD_SERVICE);
	    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
	}

	/**
	 * 让activity全屏
	 * @param aty
	 */
	public static void makeFullScreenAty(Activity aty) {
		aty.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
	}

	public static void sendBroadCast(Context mcontext,String Action,Intent intent){
        intent.setAction(Action);
		intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
        mcontext.sendBroadcast(intent);
	}

	/**
	 * 存储单位.
	 */
	private static final int STOREUNIT = 1024;

	/**
	 * 时间毫秒单位.
	 */
	private static final int TIMEMSUNIT = 1000;

	/**
	 * 时间单位.
	 */
	private static final int TIMEUNIT = 60;

	/**
	 * 私有构造函数.
	 */
	private UIUtil() {
	}

	/**
	 * 转化文件单位.
	 *
	 * @param size
	 *            转化前大小(byte)
	 * @return 转化后大小
	 */
	public static String getFormatSize(double size) {
		double kiloByte = size / STOREUNIT;
		if (kiloByte < 1) {
			return size + " Byte";
		}

		double megaByte = kiloByte / STOREUNIT;
		if (megaByte < 1) {
			BigDecimal result = new BigDecimal(Double.toString(kiloByte));
			return result.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString()
					+ " KB";
		}

		double gigaByte = megaByte / STOREUNIT;
		if (gigaByte < 1) {
			BigDecimal result = new BigDecimal(Double.toString(megaByte));
			return result.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString()
					+ " MB";
		}

		double teraBytes = gigaByte / STOREUNIT;
		if (teraBytes < 1) {
			BigDecimal result = new BigDecimal(Double.toString(gigaByte));
			return result.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString()
					+ " GB";
		}
		BigDecimal result = new BigDecimal(teraBytes);
		return result.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString()
				+ " TB";
	}

	/**
	 * 转化时间单位.
	 *
	 * @param time
	 *            转化前大小(MS)
	 * @return 转化后大小
	 */
	public static String getFormatTime(long time) {
		double second = (double) time / TIMEMSUNIT;
		if (second < 1) {
			return time + " MS";
		}

		double minute = second / TIMEUNIT;
		if (minute < 1) {
			BigDecimal result = new BigDecimal(Double.toString(second));
			return result.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString()
					+ " SEC";
		}

		double hour = minute / TIMEUNIT;
		if (hour < 1) {
			BigDecimal result = new BigDecimal(Double.toString(minute));
			return result.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString()
					+ " MIN";
		}

		BigDecimal result = new BigDecimal(Double.toString(hour));
		return result.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString()
				+ " H";
	}


	/**
	 * 根据包名启动APK
	 *
	 * @param packageName
	 * @param context
	 */
	public static void StartEPG(String packageName, Context context) {
		if (TextUtils.isEmpty(packageName)) {
			LogUtil.i("tv_launcher", "包名为空");
			return;
		}
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
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(intent);
			}
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 获取上次启动信息
	 *
	 * @param context
	 * @param key
	 * @param defultValue
	 * @return
	 */
	public static String getPackageName(Context context, String key,
										String defultValue) {
		SharedPreferences sp = context.getSharedPreferences(
				context.getPackageName(), Context.MODE_PRIVATE);
		String result = "";
		if (null != sp) {
			result = sp.getString(key, defultValue);
		}
		return result;
	}
	/**
	 * 设置本次启动信息
	 *
	 * @param context
	 * @param key
	 * @param vaule
	 */
	public static void setPackageName(Context context, String key, String vaule) {
		SharedPreferences sp = context.getSharedPreferences(
				context.getPackageName(), Context.MODE_PRIVATE);
		SharedPreferences.Editor edit = sp.edit();
		edit.putString(key, vaule);
		edit.commit();
	}
	public static void StartMangGuoEPG(Context context) {
		LogUtil.i("tv_launcher", "StartMangGuoEPG: ");
		UIUtil.sendBroadCast(context, Constants.ADVERTISING_ACTION,
				new Intent());// 节目播放业务
		SharedPreferencesUtil.setBooleanValue(context,
				SharedPreferencesUtil.IS_PALYED_ADVERT, true);
		// finish();
		LogUtil.i("tv_launcher", "send BroadCast to play iptv,start time:"
				+ new Date());
	}
//	/**
//	 * 发送 广播启动芒果EPG
//	 *
//	 * @param context
//	 */
//	public static void StartMangGuoEPG(Context context, String user_id) {
//		int AuthCode = Auth.getAuthCode(context, Auth.AUTH_CODE_AUTH_NOT_AUTH);
//		if (AuthCode == Auth.AUTH_CODE_AUTH_ERROR) {
//			LogUtil.i("tv_launcher", "get AuthCode :AUTH_CODE_AUTH_ERROR");
//			StartAuthActivity(context);
//			return;
//		} else if (AuthCode == Auth.AUTH_CODE_AUTH_SUCCESS) {
//			LogUtil.i("tv_launcher", "get AuthCode :AUTH_CODE_AUTH_SUCCESS");
//			Intent intent = new Intent(context,AuthSuccessActivity.class);
//			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//			intent.putExtra("user_id", user_id);
//			context.startActivity(intent);
//		} else if (AuthCode == Auth.AUTH_CODE_AUTH_NOT_AUTH) {
//			LogUtil.i("tv_launcher", "get AuthCode :AUTH_CODE_AUTH_NOT_AUTH");
//			StartAuthActivity(context);
//		} else {
//			LogUtil.i("tv_launcher", "get AuthCode : AUTH_CODE_AUTH_INIT");
//			StartAuthActivity(context);
//		}
////		Auth.RemoveAuthCode(context);
//		//Auth.RemoveAuthErrCode(context);
//	}

	/**
	 * 启动 认证失败的界面
	 *
	 * @param context
	 */
	public static void StartAuthActivity(Context context) {
		LogUtil.i("tv_launcher", "auth failed ,start AuthActivity");
		Intent intent = new Intent(context, AuthActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent);
	}

	public static void StartLastEpg(Context context, GroupStrategy.GroupStrategyBean bean) {
		LogUtil.e("tv_launcher",
				"get epg_package info failed ,start'up last time epg");
		if (TextUtils.isEmpty(bean.getUserId())){
			LogUtil.e("tv_launcher","get user_id is null,start'up mangguo eog");
			StartMangGuoEPG(context);
			return;
		}
		String packageName = getPackageName(context, bean.getUserId(),
				"com.hunantv.operator");
		if (packageName.equals("com.hunantv.operator")){
			LogUtil.i("tv_launcher", "StartLastEpg: get epg_package is mgtv");
			StartMangGuoEPG(context);
			setPackageName(context, bean.getUserId(), packageName);
			return;
		}
		try {
			PackageInfo getPackageInfo2 = Auth.GetPackageInfo(context, packageName);
			if (getPackageInfo2 != null) {
				LogUtil.e("tv_launcher",
						"get epg_package info success ,start last time epg :"
								+ packageName);
				StartEPG(packageName, context);
				setPackageName(context, bean.getUserId(), packageName);
			} else {
				LogUtil.e("tv_launcher",
						"get epg_package info failed ,start'up mangguo EPG ");
				StartMangGuoEPG(context);
			}
		}catch (Exception e){
			e.printStackTrace();
		}
	}

	/**
	 * @param context
	 * @return versionName 版本名字
	 */
	public static String getVersionName(Context context) {
		String versionName = "";
		try {
			String pkName = context.getPackageName();
			versionName = context.getPackageManager().getPackageInfo(
					pkName, 0).versionName;
		} catch (Exception e) {
			e.printStackTrace();
			return "0";
		}
		return versionName;
	}
	/**
	 * 获取版本信息
	 * @param context
	 * @return packageName+versionName+versionCode
	 */
	@Nullable
	public static String getVersionInfo(Context context){
		try {

			String pkName = context.getPackageName();

			String versionName = context.getPackageManager().getPackageInfo(

					pkName, 0).versionName;

			int versionCode = context.getPackageManager()

					.getPackageInfo(pkName, 0).versionCode;

			return pkName + "   " + versionName + "  " + versionCode;

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * @param context
	 * @return versionCode 版本号
	 */
	public static int getVersionCode(Context context) {
		int versionCode = 0;
		try {
			String pkName = context.getPackageName();
			versionCode = context.getPackageManager()
					.getPackageInfo(pkName, 0).versionCode;
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
		return versionCode;
	}
	/**
	 * 转化字符串.
	 *
	 * @param source
	 *            转化前字符串
	 * @param encoding
	 *            编码格式
	 * @return 转化后字符串
	 */
	public static String convertString(String source, String encoding) {
		try {
			byte[] data = source.getBytes("ISO8859-1");
			return new String(data, encoding);
		} catch (UnsupportedEncodingException ex) {
			return source;
		}
	}

	public static boolean isNetworkConnected(Context ct) {
		ConnectivityManager cManager = (ConnectivityManager) ct
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (cManager != null) {
			NetworkInfo localNetworkInfo = cManager.getActiveNetworkInfo();
			if (localNetworkInfo != null)
				return localNetworkInfo.isConnected();
		}
		return false;
	}

	public static boolean isNetworkAvailable(Context context) {
		if (context != null) {
			ConnectivityManager mConnectivityManager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo mNetworkInfo = mConnectivityManager
					.getActiveNetworkInfo();
			if (mNetworkInfo != null) {
				return mNetworkInfo.isAvailable();
			}
		}
		return false;
	}

	/**
	 * 判断当前网络是否连接
	 *
	 * @param context
	 * @return
	 */
	public static boolean isNetConnected(Context context) {
		try {
			ConnectivityManager connectivity = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			if (connectivity != null) {

				NetworkInfo info = connectivity.getActiveNetworkInfo();

				if (info != null) {
					boolean istrue = false;
					istrue = istrue ? info.isConnected() : info.isAvailable();
					return istrue;
				}
			}
		} catch (Exception e) {
			return false;
		}
		return false;
	}
	public static String getDateTime(long dataTime) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SS");
		Date date = new Date(dataTime);
		String time = dateFormat.format(date);
		return time;
	}
	public static String getDayDateTime(long dataTime) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
		Date date = new Date(dataTime);
		String time = dateFormat.format(date);
		return time;
	}
	public static  void stopClick(final Button loginBtn, final long time) {
		loginBtn.setClickable(false);
		Observable.interval(0,1,TimeUnit.SECONDS).filter(new Predicate<Long>() {
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
					  loginBtn.setText("请等待" + aLong + "秒");
				  }else{
					  this.dispose();
					  loginBtn.setClickable(true);
					  loginBtn.setText(R.string.confirm);
				  }
			  }

			  @Override
			  public void onError(@NonNull Throwable e) {
				  loginBtn.setClickable(true);
				  loginBtn.setText(R.string.confirm);
				  this.dispose();
			  }

			  @Override
			  public void onComplete() {

			  }
		  });
//		Observable.interval(0,1, TimeUnit.SECONDS).filter(new Func1<Long, Boolean>() {
//			@Override
//			public Boolean call(Long aLong) {
//				return aLong <= time;
//			}
//		}).map(new Func1<Long, Long>() {
//
//			@Override
//			public Long call(Long aLong) {
//				return -(aLong-time);
//			}
//		}).subscribeOn(Schedulers.io())
//				.observeOn(AndroidSchedulers.mainThread())
//				.subscribe(new Subscriber<Long>() {
//					@Override
//					public void onCompleted() {
//
//					}
//
//					@Override
//					public void onError(Throwable throwable) {
//						loginBtn.setClickable(true);
//						loginBtn.setText(R.string.determin);
//						this.unsubscribe();
//					}
//
//					@Override
//					public void onNext(Long aLong) {
//						if (aLong!=0){
//							loginBtn.setText("请等待" + aLong + "秒");
//						}else{
//							this.unsubscribe();
//							loginBtn.setClickable(true);
//							loginBtn.setText(R.string.determin);
//						}
//					}
//				});
	}
	public static void ShowToast(Context mcontext, String str) {
		if (mcontext == null) {
			LogUtil.i("tv_launcher", "ShowToast()无法获取上下文");
			return;
		}
		if (TextUtils.isEmpty(str)) {
			Toast.makeText(mcontext, "未定义提示内容", Toast.LENGTH_SHORT).show();
			return;
		}
		Toast.makeText(mcontext, str, Toast.LENGTH_SHORT).show();
	}
	/**
	 * 判断服务是否运行
	 *
	 * @param context
	 * @param clazz
	 *            要判断的服务的class
	 * @return
	 */
	public static boolean isServiceRunning(Context context,
										   Class<? extends Service> clazz) {
		try {
			ActivityManager manager = (ActivityManager) context
					.getSystemService(Context.ACTIVITY_SERVICE);

			List<ActivityManager.RunningServiceInfo> services = manager.getRunningServices(100);
			for (int i = 0; i < services.size(); i++) {
				String className = services.get(i).service.getClassName();
				if (className.equals(clazz.getName())) {
					return true;
				}
			}
		} catch (Exception e) {
			return false;
		}
		return false;
	}
	public static RelativeLayout.LayoutParams getViewLayoutParams (int viewWidth, int viewHeight, Context context){
		RelativeLayout.LayoutParams layoutParams;
		int windowsWhdth = getScreenWidth(context);
		int windowsHeight = getScreenHeight(context);
		if (viewWidth>= windowsWhdth && viewHeight>=windowsHeight){
			LogUtil.i("tv_launcher", "getmData: videoWidth and windowsHeight>= windowsWhdth and windowsHeight");
			layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
			return layoutParams;
		}else if(viewWidth>= windowsWhdth && viewHeight< windowsHeight){
			LogUtil.i("tv_launcher", "getmData: videoWidth >= windowsWhdth ");
			layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, viewHeight);
			return layoutParams;
		}else if(viewWidth< windowsWhdth && viewHeight>= windowsHeight){
			LogUtil.i("tv_launcher", "getmData:   windowsHeight>=  windowsHeight");
			layoutParams = new RelativeLayout.LayoutParams(viewWidth, RelativeLayout.LayoutParams.MATCH_PARENT);
			return layoutParams;
		}else{
			LogUtil.i("tv_launcher", "getmData: videoWidth and windowsHeight< windowsWhdth and windowsHeight");
			layoutParams = new RelativeLayout.LayoutParams(viewWidth, viewHeight);
			return layoutParams;
		}
	}
	/**
	 * 根据包名启动服务
	 *
	 * @param packageName
	 * @param context
	 */
	public static void StartServer(String packageName,String className,String Action,Context context) {
		if (TextUtils.isEmpty(packageName)) {
			LogUtil.i("tv_launcher", "包名为空");
			return;
		}
		PackageInfo pi;
		try {
			pi = context.getPackageManager().getPackageInfo(packageName, 0);
			if (pi!=null){
				LogUtil.i("tv_launcher","StartServer packageName:"+packageName);
				LogUtil.i("tv_launcher","StartServer className:"+className);
				LogUtil.i("tv_launcher","StartServer Action:"+Action);
//				Intent intent = new Intent(Intent.ACTION_MAIN);
//				ComponentName cn = new ComponentName(packageName, className);
//				intent.setAction(Action);
//				context.startService(intent);
				Intent intent = new Intent(className);
				intent.setPackage(packageName);
				intent.setAction(Action);
				context.startService(intent);
				LogUtil.i("tv_launcher","StartServer to AuthApk,start time:"+new Date());
				SharedPreferencesUtil.setBooleanValue(context,"IPTV_Service", true);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * 判断应用是否安装
	 * @param context
	 * @param packageName
	 * @return
	 */
	public static boolean isPkgInstalled(Context context, String packageName) {
		LogUtil.i("tv_launcher", "isPkgInstalled: getPackageName:"+packageName);
		if (TextUtils.isEmpty(packageName)) {
			LogUtil.i("tv_launcher", "isPkgInstalled: get packageName is null");
			return false;
		}
		ApplicationInfo info = null;
		try {
			info = context.getPackageManager().getApplicationInfo(packageName, 0);
			return info != null;
		} catch (Exception e) {
//            e.printStackTrace();
			return false;
		}
	}
}
