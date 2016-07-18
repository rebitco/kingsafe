package com.king.kingsafe.activity;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

import com.king.kingsafe.R;
import com.king.kingsafe.utils.StreamUtil;
import com.king.kingsafe.utils.ToastUtil;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.animation.AlphaAnimation;
import android.widget.RelativeLayout;
import android.widget.TextView;


/**
 * @author Administrator
 *
 */
public class SplashActivity extends Activity {

	private RelativeLayout rl_root;
	private TextView tv_version;
	private PackageInfo packageInfo;
	private int mLocalVersionCode;
	protected String mVersionDesp;
	protected String mDownloadUrl;
	private static final String tag = "SplashActivity";//打log方便查看
	
	protected static final int UPDATE_VERSION = 100;
	protected static final int ENTER_HOME = 101;
	protected static final int URL_ERROR = 102;//方便调试错误
	protected static final int IO_ERROR = 103;
	protected static final int JSON_ERROR = 104;

	private Handler mhHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case UPDATE_VERSION:
				//弹框提醒更新版本
				updateDialog();
				break;
			case ENTER_HOME:
				enterHome();
				break;
			case URL_ERROR:
				//TODO 吐司是调试用,以后打包必须得删除或注销
				ToastUtil.show(getApplicationContext(), "URL异常");
				enterHome();
				break;
			case IO_ERROR:
				ToastUtil.show(getApplicationContext(), "IO流异常");
				enterHome();
				break;
			case JSON_ERROR:
				ToastUtil.show(getApplicationContext(), "JSON解析异常");
				enterHome();
				break;
			}
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);
		//初始化控件
		initUI();
		//初始化数据
		initData();
		//初始化动画
		initAnomation();
	}
	
	
	/**
	 * 界面淡入淡出动画
	 */
	private void initAnomation() {
		AlphaAnimation alphaAnimation = new AlphaAnimation(0, 1);
		alphaAnimation.setDuration(3000);
		rl_root.startAnimation(alphaAnimation);
	}

	/**
	 * 弹框提醒更新
	 */
	protected void updateDialog() {
		Builder builder = new AlertDialog.Builder(SplashActivity.this);
		builder.setTitle("更新版本");
		builder.setIcon(R.drawable.ic_launcher);
		builder.setMessage(mVersionDesp);
		builder.setPositiveButton("立即更新", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				//下载apk
				downLoadApk();
			}
		});
		builder.setNegativeButton("稍后再说", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				//取消后直接进入主页面
				enterHome();
			}
		});
		builder.show();
	}

	protected void downLoadApk() {
		//1.判断sd卡是否可用 是否挂载上
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			//2.获取SD卡路径
			String path = Environment.getExternalStorageDirectory().getAbsolutePath()+
							File.separator+"kingsafe.apk";
			//3.发送请求 下载apk
			HttpUtils httpUtils = new HttpUtils();
			//4.发送请求 传递参数
			httpUtils.download(mDownloadUrl, path, new RequestCallBack<File>() {
				@Override
				public void onSuccess(ResponseInfo<File> responseInfo) {
					//下载成功
					File file = responseInfo.result;
					//下载成功后,安装新版本的APK包
					installApk(file);
				}
				@Override
				public void onFailure(HttpException e, String arg1) {
					//下载失败
				}
				//下载中的方法
				@Override
				public void onLoading(long total, long current,
						boolean isUploading) {
					Log.i(tag, "total-->"+total);
					Log.i(tag, "current-->"+current);
					Log.i(tag, "total-->"+total);
					super.onLoading(total, current, isUploading);
				}
				//刚刚开始下载
				@Override
				public void onStart() {
					super.onStart();
				}
			});
		}
	}

	/**
	 * 安装下载好新版本apk 弹出系统的dialog提示和安装界面
	 * 	<activity android:name=".PackageInstallerActivity"
     *           android:configChanges="orientation|keyboardHidden"
     *           android:theme="@style/Theme.Transparent">
     *       <intent-filter>
     *           <action android:name="android.intent.action.VIEW" />
     *           <category android:name="android.intent.category.DEFAULT" />
     *           <data android:scheme="content" />
     *           <data android:scheme="file" />
     *           <data android:mimeType="application/vnd.android.package-archive" />
     *       </intent-filter>
     *   </activity>
	 * @param file
	 */
	protected void installApk(File file) {
		Intent intent = new Intent("android.intent.action.VIEW");
		intent.addCategory("android.intent.category.DEFAULT");
		intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
		//跳转到系统的安装应用页面
		//startActivity(intent);
		//如果到了安装提示弹框 , 但用户不想安装了 , 然后点取消
		startActivityForResult(intent, 0);
	}
	//如果到了安装提示弹框 , 但用户不想安装了 , 然后点取消, 应该是跳转到主页面
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		enterHome();
		super.onActivityResult(requestCode, resultCode, data);
	}

	/**
	 * 进入主页面
	 */
	protected void enterHome() {
		Intent intent = new Intent(this, HomeActivity.class);
		startActivity(intent);
		//进入主页面后 , 销毁欢迎页
		finish();
	}

	/**
	 * 初始化数据
	 */
	private void initData() {
		//1.获取版本名
		tv_version.setText("版本: v"+getVersionName());
		//2.新版本提示更新需求 : 请求服务器,读取json数据 , 将拿到的版本号跟本地版本号做对比
		mLocalVersionCode = getVersionCode();
		//3.请求服务器 耗时操作放到子线程
		checkVersion();
	}

	/**
	 * 检测版本号
	 */
	private void checkVersion() {
		//子线程
		new Thread(){
			/* (non-Javadoc)
			 * @see java.lang.Thread#run()
			 */
			public void run() {
				//获取message实例
				Message msg = Message.obtain();
				//获取执行run方法的起始时间
				long startTime = System.currentTimeMillis();
				try {
					//1.封装url
					URL url = new URL("http://haoyear.cn/update.json");
					//2.开启连接
					HttpURLConnection connection = (HttpURLConnection) url.openConnection();
					//3.设置请求头
					connection.setConnectTimeout(2000);
					connection.setReadTimeout(2000);
					connection.setRequestMethod("GET");//默认为GET,可不用写
					//4.请求码 200代表成功
					if (connection.getResponseCode() == 200) {
						//5.请求成功拿到一个输入流
						InputStream is = connection.getInputStream();
						//6.将流转换成字符串
						String json = StreamUtil.stream2String(is);
						Log.i(tag, json);
						JSONObject jsonObject = new JSONObject(json);
						String versionName = jsonObject.getString("versionName");
						String versionCode = jsonObject.getString("versionCode");
						mVersionDesp = jsonObject.getString("versionDesp");
						mDownloadUrl = jsonObject.getString("downloadUrl");
						
						//7.将本地版本号与服务器版本号做对比
						if (mLocalVersionCode < Integer.parseInt(versionCode)) {
							//7.1 弹框提示更新新版本
							msg.what = UPDATE_VERSION;
						} else {
							//7.2 进入主界面
							msg.what = ENTER_HOME;
						}
					}
				} catch (MalformedURLException e) {
					e.printStackTrace();
					msg.what = URL_ERROR;
				} catch (IOException e) {
					e.printStackTrace();
					msg.what = IO_ERROR;
				} catch (JSONException e) {
					e.printStackTrace();
					msg.what = JSON_ERROR;
				} finally {
					//获取执行run方法的结束时间
					long endTime = System.currentTimeMillis();
					//如果请求网络时长超过4秒即不做处理,否则强行睡眠够4秒
					if(endTime-startTime < 4000){
						try {
							Thread.sleep(4000-(endTime-startTime));
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					mhHandler.sendMessage(msg);
				}
			};
		}.start();
	}

	/**
	 * 获取版本号 注册清单下
	 * @return 正常返回版本号 否则返回0
	 */
	private int getVersionCode() {
		try {
			return packageInfo.versionCode;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	/**
	 * 获取版本名 注册清单下
	 * @return 正常返回版本名 异常返回null
	 */
	private String getVersionName() {
		//包管理者
		PackageManager pm = getPackageManager();
		try {
			packageInfo = pm.getPackageInfo(getPackageName(), 0);
			return packageInfo.versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 初始化控件
	 */
	private void initUI() {
		tv_version = (TextView) findViewById(R.id.tv_version);
		rl_root = (RelativeLayout) findViewById(R.id.rl_root);
	}

}
