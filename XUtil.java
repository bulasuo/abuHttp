package com.abu.healthandroidpad.util;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.abu.healthandroidpad.R;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class XUtil {


	public static int birth2gae(String dateStr){
		Date date = null;
		final DateFormat fmt =new SimpleDateFormat("yyyy-MM-dd");
		try {
			date = fmt.parse(dateStr);
		} catch (ParseException e1) {
			e1.printStackTrace();
			return -1;
		}
		Calendar cal = Calendar.getInstance();
		final int yearNow = cal.get(Calendar.YEAR) - 1900;
		final int monthNow = cal.get(Calendar.MONTH);
		final int dayOfYear = cal.get(Calendar.DAY_OF_YEAR);

		int age = yearNow - date.getYear() - 1;
		if(age < 0) return 0;
		age = monthNow > date.getMonth()
				? age ++
				: (monthNow == date.getMonth()
					? (dayOfYear >= date.getDay()
						? age ++
						: age)
					: age);
		return age;
	}

	/**
	 *@author   abu   2016/4/15   10:58
	 * 提示Dialog 只有确定键
	 */
	public static void tipDialog(Context context, String title, String mesg){
		final AlertDialog mDialog = new AlertDialog.Builder(context).create();
		mDialog.setCanceledOnTouchOutside(true);
		mDialog.show();
		mDialog.setContentView(R.layout.base_dialog_layout);
		if(title != null) {
			final TextView tv_title = (TextView) mDialog.findViewById(R.id.tv_title);
			tv_title.setText(title);
		}
		final TextView tv_mesg = (TextView) mDialog.findViewById(R.id.tv_mesg);
		tv_mesg.setText(mesg);

		mDialog.findViewById(R.id.btn_confirm).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mDialog.dismiss();
			}
		});

	}

	public static void tipDialogNetWork(final Context context, String title, String mesg){
		final AlertDialog mDialog = new AlertDialog.Builder(context).create();
		mDialog.setCanceledOnTouchOutside(true);
		mDialog.show();
		mDialog.setContentView(R.layout.network_tip_layout);
		if(title != null) {
			final TextView tv_title = (TextView) mDialog.findViewById(R.id.tv_title);
			tv_title.setText(title);
		}
		if(mesg != null) {
			final TextView tv_mesg = (TextView) mDialog.findViewById(R.id.tv_mesg);
			tv_mesg.setText(mesg);
		}
		mDialog.findViewById(R.id.btn_confirm).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				context.startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
				mDialog.dismiss();
			}
		});
		mDialog.findViewById(R.id.btn_cancel).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mDialog.dismiss();
			}
		});
	}

	/**
	 *@author   abu   2016/4/7   11:26
	 * 判断网络是否可用(2G/3G/4G/Wifi)
	 */
	public boolean isNetWorkConnected(Context context) {
		if(context != null) {
			ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo networkInfo = manager.getActiveNetworkInfo();
			if(networkInfo != null) {
				boolean b = networkInfo.isAvailable();
				return b;
			}
		}
		return false;
	}

	/**
	 *@author   abu   2016/4/26   10:36
	 * 获取网络类型
	 */
	public static int getInternetType(Context context){
		ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		return manager.getActiveNetworkInfo().getType();
	}

	/**
	 *@author   abu   2016/4/6   10:19
	 * 隐藏键盘 且 有光标
	 */
	public static void disableShowSoftInput(EditText editText)
	{
		if (android.os.Build.VERSION.SDK_INT <= 10)
		{
			editText.setInputType(InputType.TYPE_NULL);
		}
		else {
			Class<EditText> cls = EditText.class;
			Method method;
			try {
				method = cls.getMethod("setShowSoftInputOnFocus",boolean.class);
				method.setAccessible(true);
				method.invoke(editText, false);
			}catch (Exception e) {
				// TODO: handle exception
			}

			try {
				method = cls.getMethod("setSoftInputShownOnFocus",boolean.class);
				method.setAccessible(true);
				method.invoke(editText, false);
			}catch (Exception e) {
				// TODO: handle exception
			}
		}
	}

	/**
	 *@author   abu   2016/3/28   17:28
	 * 判断当前系统中指定的service是否在运行
	 * @param context 	APP上下文
	 * @param className		service类的名称
	 * @return 如果存在，返回true，否则返回false
	 */
	public static boolean isServiceExisted(Context context, String className)
	{
		ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		List<ActivityManager.RunningServiceInfo> serviceList = activityManager.getRunningServices(Integer.MAX_VALUE);

		if(serviceList.size()<= 0) {
			return false;
		}

		for(int i = 0; i < serviceList.size(); i++) {
			ActivityManager.RunningServiceInfo serviceInfo = serviceList.get(i);
			ComponentName serviceName = serviceInfo.service;

			if(serviceName.getClassName().equals(className)) {
				return true;
			}
		}
		return false;
	}

	/**
	 *@author   abu   2016/3/22   11:01
	 */
	public static String getCurrentDate(){
		Calendar cal = Calendar.getInstance();
		return cal.get(Calendar.YEAR)+"-"+(cal.get(Calendar.MONTH) + 1)+"-"+cal.get(Calendar.DAY_OF_MONTH);
	}

	/**
	 *@author   abu   2016/3/22   11:00
	 */
	public static String changeDay(int action, Date date){
		Calendar cal = Calendar.getInstance();
		cal.clear();
		cal.setTime(date);
		cal.add(cal.DATE, action);
		return cal.get(Calendar.YEAR)+"-"+(cal.get(Calendar.MONTH) + 1)+"-"+cal.get(Calendar.DAY_OF_MONTH);
	}

	/**
	 *@author   abu   2016/3/22   11:00
	 */
	public static boolean isPhone(String phone){
		if(phone.matches("1[0-9]{10}")){
			return true;
		}
		return false;
	}

	/**
	 *@author   abu   2016/4/7   11:27
	 */
	public static boolean isMobileNo(String mobile) {
		Pattern p  = Pattern.compile("^((13[0-9])|(15[^4,\\D])|(18[0,5-9]))\\d{8}$");
		Matcher m = p.matcher(mobile);
		return m.matches();
	}

	/**
	 *@author   abu   2016/3/22   10:58
	 * X:表示以十六进制输出
	 * 02:表示不足2位补0
	 */
	public static String byte2Str(byte[] b){
		if (b != null && b.length > 0) {
			final StringBuilder stringBuilder = new StringBuilder(b.length);
			for(byte byteChar : b)
				stringBuilder.append(String.format("%02X ", byteChar));
			return stringBuilder.toString();
		}
		return null;
	}

    /**
     *@author   abu   2016/3/28   17:41
     */
    public static byte[] str2Bytes(String s){
        String ss = s.replace(" ", "");
        int string_len = ss.length();
        int len = string_len / 2;
        if(string_len % 2 == 1){
            ss = "0" + ss;
            string_len ++;
            len ++;
        }
        byte[] a = new byte[len];
        for(int i=0;i<len;i++){
            a[i] = (byte)Integer.parseInt(ss.substring(2*i,2*i+2), 16);
        }
        return a;
    }

	/**
	 *@author   abu   2016/3/22   11:15
	 * CRC8校验表
	 */
	static char crc_table[]={
			0x00,0x5e,0xbc,0xe2,0x61,0x3f,0xdd,0x83,0xc2,0x9c,0x7e,0x20,0xa3,0xfd,0x1f,0x41,
			0x9d,0xc3,0x21,0x7f,0xfc,0xa2,0x40,0x1e,0x5f,0x01,0xe3,0xbd,0x3e,0x60,0x82,0xdc,
			0x23,0x7d,0x9f,0xc1,0x42,0x1c,0xfe,0xa0,0xe1,0xbf,0x5d,0x03,0x80,0xde,0x3c,0x62,
			0xbe,0xe0,0x02,0x5c,0xdf,0x81,0x63,0x3d,0x7c,0x22,0xc0,0x9e,0x1d,0x43,0xa1,0xff,
			0x46,0x18,0xfa,0xa4,0x27,0x79,0x9b,0xc5,0x84,0xda,0x38,0x66,0xe5,0xbb,0x59,0x07,
			0xdb,0x85,0x67,0x39,0xba,0xe4,0x06,0x58,0x19,0x47,0xa5,0xfb,0x78,0x26,0xc4,0x9a,
			0x65,0x3b,0xd9,0x87,0x04,0x5a,0xb8,0xe6,0xa7,0xf9,0x1b,0x45,0xc6,0x98,0x7a,0x24,
			0xf8,0xa6,0x44,0x1a,0x99,0xc7,0x25,0x7b,0x3a,0x64,0x86,0xd8,0x5b,0x05,0xe7,0xb9,
			0x8c,0xd2,0x30,0x6e,0xed,0xb3,0x51,0x0f,0x4e,0x10,0xf2,0xac,0x2f,0x71,0x93,0xcd,
			0x11,0x4f,0xad,0xf3,0x70,0x2e,0xcc,0x92,0xd3,0x8d,0x6f,0x31,0xb2,0xec,0x0e,0x50,
			0xaf,0xf1,0x13,0x4d,0xce,0x90,0x72,0x2c,0x6d,0x33,0xd1,0x8f,0x0c,0x52,0xb0,0xee,
			0x32,0x6c,0x8e,0xd0,0x53,0x0d,0xef,0xb1,0xf0,0xae,0x4c,0x12,0x91,0xcf,0x2d,0x73,
			0xca,0x94,0x76,0x28,0xab,0xf5,0x17,0x49,0x08,0x56,0xb4,0xea,0x69,0x37,0xd5,0x8b,
			0x57,0x09,0xeb,0xb5,0x36,0x68,0x8a,0xd4,0x95,0xcb,0x29,0x77,0xf4,0xaa,0x48,0x16,
			0xe9,0xb7,0x55,0x0b,0x88,0xd6,0x34,0x6a,0x2b,0x75,0x97,0xc9,0x4a,0x14,0xf6,0xa8,
			0x74,0x2a,0xc8,0x96,0x15,0x4b,0xa9,0xf7,0xb6,0xe8,0x0a,0x54,0xd7,0x89,0x6b,0x35
	};

	/**
	 *@author   abu   2016/3/22   11:15
	 */
	public static byte getCRC8(byte[] b){
		char crc = 0;
		for(int i = 0; i < b.length; i++){
			crc =  crc_table[crc ^ (char)(b[i] & 0xff)];
		}
		return (byte)crc;
	}

    /**
     *@author   abu   2016/3/28   17:49
     * 获取本地ip地址
     */
    private String getLocalIpAddress() throws UnknownHostException {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface
                    .getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf
                        .getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()
                            && !inetAddress.isLinkLocalAddress()) {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (SocketException ex) {
            System.out.println("WifiPreference IpAddress:"+ ex.toString());
        }
        return null;
    }

	public static int getViewWidth(View v){
		int w = View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED);
		int h = View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED);
		v.measure(w, h);
		return v.getMeasuredWidth();
	}
	public static int getViewHeight(View v){
		int w = View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED);
		int h = View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED);
		v.measure(w, h);
		return v.getMeasuredHeight();
	}



}
