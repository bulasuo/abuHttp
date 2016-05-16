# abuHttp
abuHttp,集成web请求,文件上传,图片加载,文件下载,文件硬盘缓存(DiskLruCache)和运存缓存(LruCache),任务可控
集成DiskLruCache(硬盘缓存) 和 LruCache(运存缓存) 

关于DiskLruCache: http://www.mobile-open.com/2014/3104.html;http://www.mobile-open.com/2015/77513.html
DiskLruCache-gitHub  :  https://github.com/JakeWharton/DiskLruCache
可以下载jar包 也可以AS关联 Gradle:compile 'com.jakewharton:disklrucache:2.0.2'
我这边直接用源码了方便随时看源码^-^

/**---abuHttp.java
 * @author 阿布
 * @date 2016-4-13 下午2:12:06
 * 子线程应该在空闲时对cup资源让步
 * CacheManager.getDiskLruCache(activity.getApplicationContext(), "bitmap").flush();//不应该常用,应该在activity.onPause()里用
 * 图片加载逻辑步骤: 运存里有资源否 - 硬盘有资源否 ; 加载到硬盘 - 显示到UI - 保存到运存
 */

/**---CacheManager.java
 * Created by abu on 2016/5/16 20:36.
 * 运存保存资源的key就是 url_str;硬盘则是url_str的MD5值
 * CacheManager.getDiskLruCache(activity.getApplicationContext(), "bitmap").flush();//不应该常用,应该在activity.onPause()里用
 */
