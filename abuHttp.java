package com.abu.healthandroidpad.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * @author 阿布
 * @date 2016-4-13 下午2:12:06
 * 子线程应该在空闲时对cup资源让步
 */
public class abuHttp {

	private static ExecutorService mExecutorService;

	//服务器响应的回调接口
	public static interface OnResponseListener{
		public void onResponse(String responseStr);
		public void onError(IOException e, int responseCode);
	}

	private static ExecutorService getThreadPool(){
		if(mExecutorService == null)
			mExecutorService = Executors.newFixedThreadPool(5);//线程池里最多5个线程处理任务,多余的任务会阻塞
		return mExecutorService;
	}

	public static Runnable LoadImage(final Activity activity, final String strUrl, final ImageView imageView){
		Runnable r = new Runnable(){
			private boolean tryStop = false;
			private boolean tryDestroy = false;

			public void tryStop(){
				tryStop = true;
			}

			public void tryStart(){
				tryStop = false;
			}

			public void tryDestroy(){
				tryDestroy = true;
			}

			@Override
			public void run() {
				try {
					URL url = new URL(strUrl);
					HttpURLConnection conn = (HttpURLConnection) url.openConnection();
					InputStream inputStream = conn.getInputStream();
					final Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
					if(!tryStop && !tryDestroy && activity != null)
						activity.runOnUiThread(new Runnable() {
							@Override
							public void run() {
								imageView.setImageBitmap(bitmap);
							}
						});

				} catch (IOException e) {
					e.printStackTrace();
				}

			}

		};
		getThreadPool().execute(r);
		return r;
	}

	/**
	 *@author   abu   2016/4/19   1:05
	 * 简单图片加载
	 */
	public static class LoadImageThread extends Thread{

		private boolean tryStop = false;
		private boolean tryDestroy = false;
		private Activity activity;
		private String strUrl;
		private ImageView imageView;

		public LoadImageThread(Activity activity, String strUrl, ImageView imageView){

			this.activity = activity;
			this.strUrl = strUrl;
			this.imageView = imageView;
		}
		public void tryStop(){
			tryStop = true;
		}

		public void tryStart(){
			tryStop = false;
		}

		public void tryDestroy(){
			tryDestroy = true;
		}
		@Override
		public void run() {
			try {
				URL url = new URL(strUrl);
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				InputStream inputStream = conn.getInputStream();
				final Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
				if(!tryStop && !tryDestroy && activity != null)
					activity.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							imageView.setImageBitmap(bitmap);
						}
					});

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 *@author   abu   2016/4/19   1:05
	 * 简单单个文件上传
	 */
	public static class UpLoadThread extends Thread{

		private boolean tryStop = false;
		private boolean tryDestroy = false;
		private Activity activity;
		private OnResponseListener onResponseListener;
		private String strUrl;
		private Map<String, String> params;
		private String fileKey ;
		private File file;
		private String encode = "utf-8";//默认编码
		private int readTimeOut = 10 * 1000;
		private int connectTimeOut = 10 * 1000;
		private String BOUNDARY =  UUID.randomUUID().toString();

		public UpLoadThread(Activity activity, String strUrl, Map<String, String> params, String fileKey, File file, String encode, OnResponseListener onResponseListener){
			this.activity = activity;
			this.strUrl = strUrl;
			this.params = params;
			this.fileKey = fileKey;
			this.file = file;
			if(encode != null)
				this.encode = encode;
			this.onResponseListener = onResponseListener;
		}

		public void tryStop(){
			tryStop = true;
		}

		public void tryStart(){
			tryStop = false;
		}

		public void tryDestroy(){
			tryDestroy = true;
		}

		private void onResponse(final String str){
			if(activity == null) return;
			activity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if(!tryStop && !tryDestroy)
						onResponseListener.onResponse(str);
				}
			});
		}

		private void onError(final IOException e, final int responseCode){
			if(activity == null) return;
			activity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if(!tryStop && !tryDestroy)
						onResponseListener.onError(e, responseCode);
				}
			});
		}

		@Override
		public void run() {
			try {
				URL url = new URL(strUrl);
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				conn.setReadTimeout(readTimeOut);
				conn.setConnectTimeout(connectTimeOut);
				conn.setDoInput(true); // 允许输入流
				conn.setDoOutput(true); // 允许输出流
				conn.setUseCaches(false); // 不允许使用缓存
				conn.setRequestMethod("POST"); // 请求方式
				conn.setRequestProperty("Charset", encode); // 设置编码
				conn.setRequestProperty("connection", "keep-alive");
				conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)");
				conn.setRequestProperty("Content-Type", "multipart/form-data" + ";boundary=" + BOUNDARY);

				/**
				 * 当文件不为空，把文件包装并且上传
				 */
				DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
				StringBuffer sb ;

				/***
				 * 以下是用于上传参数
				 */
				if (params != null && params.size() > 0) {
					Iterator<String> it = params.keySet().iterator();
					while (it.hasNext()) {
						sb = new StringBuffer();
						String key = it.next();
						String value = params.get(key);
						sb.append("--").append(BOUNDARY).append("\r\n");
						sb.append("Content-Disposition: form-data; name=\"").append(key).append("\"").append("\r\n").append("\r\n");
						sb.append(value).append("\r\n");

						dos.write(sb.toString().getBytes());
					}
				}

				sb = new StringBuffer();
				/**
				 * 这里重点注意： name里面的值为服务器端需要key 只有这个key 才可以得到对应的文件
				 * filename是文件的名字，包含后缀名的 比如:abc.png
				 */
				sb.append("--").append(BOUNDARY).append("\r\n");
				sb.append("Content-Disposition:form-data; name=\"" + fileKey
						+ "\"; filename=\"" + file.getName() + "\"\r\n");
				sb.append("Content-Type:image/pjpeg" + "\r\n"); // 这里配置的Content-type很重要的 ，用于服务器端辨别文件的类型的
				sb.append("\r\n");
				dos.write( sb.toString().getBytes());
				sb = null;

				/**上传文件*/
				InputStream is = new FileInputStream(file);

				byte[] bytes = new byte[1024];//1kb
				int len = 0;
				int curLen = 0;
				while ((len = is.read(bytes)) != -1) {
					curLen += len;
					dos.write(bytes, 0, len);
					System.out.println("::file.length:"+file.length()+"\n::curLen:"+curLen);
				}
				is.close();

				dos.write("\r\n".getBytes());

				dos.write(("--" + BOUNDARY + "--" + "\r\n").getBytes());
				dos.flush();

				/**
				 * 获取响应码 200=成功 当响应成功，获取响应的流
				 */
				int responseCode = conn.getResponseCode();


				System.out.println("responseCode:"+responseCode);
				if (responseCode == 200) {
					final InputStream input = conn.getInputStream();


					if(!tryStop && !tryDestroy)
						onResponse(dealResponseResult(input));
				} else {
					if(!tryStop && !tryDestroy)
						onError(null, responseCode);
				}
			} catch (MalformedURLException e) {
				if(!tryStop && !tryDestroy)
					onError(e, 0);
			} catch (IOException e) {
				if(!tryStop && !tryDestroy)
					onError(e, 0);
			}
		}
	}



	/**
	 *@author   abu   2016/4/18   22:54
	 * 简单表单请求
	 */
	public static class PostThread extends Thread{

		private boolean tryStop = false;
		private boolean tryDestroy = false;
		private OnResponseListener onResponseListener;
		private String strUrl;
		private Map<String, String> params;
		private String encode = "utf-8";//默认编码
		private Activity activity;

		public PostThread(Activity activity, String strUrl, Map<String, String> params, String encode, OnResponseListener onResponseListener){
			this.activity = activity;
			this.strUrl = strUrl;
			this.params = params;
			if(encode != null)
				this.encode = encode;
			this.onResponseListener = onResponseListener;

		}

		//jvm对线程的释放不完善,自己控制,run里有循环体控制while(runable),或者简单的控制run后的结果操作
		public void tryStop(){
			tryStop = true;
		}

		public void tryStart(){
			tryStop = false;
		}

		public void tryDestroy(){
			tryDestroy = true;
		}

		private void onResponse(final String str){
			if(activity == null) return;
			activity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if(!tryStop && !tryDestroy)
						onResponseListener.onResponse(str);
				}
			});
		}

		private void onError(final IOException e, final int responseCode){
			if(activity == null) return;
			activity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if(!tryStop && !tryDestroy)
						onResponseListener.onError(e, responseCode);
				}
			});
		}





		@Override
		public void run() {


			byte[] data = getRequestData(params, encode).toString().getBytes();//获得请求体的二进制
			try {
				URL url = new URL(strUrl);
				HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
				httpURLConnection.setReadTimeout(6000);
				httpURLConnection.setConnectTimeout(6000);           //设置连接超时时间
				httpURLConnection.setDoInput(true);                  //打开输入流，以便从服务器获取数据
				httpURLConnection.setDoOutput(true);                 //打开输出流，以便向服务器提交数据
				httpURLConnection.setRequestMethod("POST");     	 //设置以Post方式提交数据
				httpURLConnection.setUseCaches(false);               //不使用缓存

//				httpURLConnection.setRequestProperty("Charset", encode); // 设置编码
//				httpURLConnection.setRequestProperty("connection", "keep-alive");
//				httpURLConnection.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)");
//				httpURLConnection.setRequestProperty("Content-Type", "multipart/form-data" + ";boundary=" + UUID.randomUUID().toString());//内容类型;边界标识符随机生成(多种数据传输使能)

				//设置请求体的类型是文本类型
//		            //text/xml -> xml数据       application/json -> json对象       application/x-www-form-urlencoded -> 表单数据       multipart/form-data -> 多种数据
//		            httpURLConnection.setRequestProperty("Content-Type", "application/x-javascript; charset="+ encode);
				httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
				//设置请求体的长度
				httpURLConnection.setRequestProperty("Content-Length", String.valueOf(data.length));
				//获得输出流，向服务器写入数据
				OutputStream outputStream = httpURLConnection.getOutputStream();
				outputStream.write(data);
				int responseCode = httpURLConnection.getResponseCode();            //获得服务器的响应码
				if(responseCode == HttpURLConnection.HTTP_OK) {
					final InputStream inptStream = httpURLConnection.getInputStream();
					if(!tryStop && !tryDestroy)
						onResponse(dealResponseResult(inptStream));
				} else {
					if(!tryStop && !tryDestroy)
						onError(null, responseCode);
				}
			} catch (MalformedURLException e) {
				if(!tryStop && !tryDestroy)
					onError(e, 0);
			} catch (IOException e) {
				if(!tryStop && !tryDestroy)
					onError(e, 0);
			}
		}


	}






	//inputStream2String
	public final static String dealResponseResult(InputStream inptStream) {
		String resultData = null;      //存储处理结果
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		byte[] data = new byte[1024];
		int len = 0;
		try {
			while((len = inptStream.read(data)) != -1) {
				byteArrayOutputStream.write(data, 0, len);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		resultData = new String(byteArrayOutputStream.toByteArray());
		return resultData;
	}

	//参数转StringBuilder
	public final static StringBuilder getRequestData(Map<String, String> params,String encode) {
		StringBuilder sb = new StringBuilder();//非线程安全
		try {
			for(Map.Entry<String, String> entry : params.entrySet()) {
				sb.append(entry.getKey())
						.append("=")
						.append(URLEncoder.encode(entry.getValue(), encode))
						.append("&");
			}
			sb.deleteCharAt(sb.length() - 1);    //删除最后的一个"&"
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sb;
	}


	//判断是否有网
	public static boolean isNetWorkConnected(Context context) {
		boolean bisConnFlag = false;
		ConnectivityManager conManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo network = conManager.getActiveNetworkInfo();
		if(network!=null){
			bisConnFlag = conManager.getActiveNetworkInfo().isAvailable();
		}
//		if(!bisConnFlag)
//			XUtil.tipDialogNetWork(context, null, null);//没网则进入网络设置页面
		return bisConnFlag;
	}

}

