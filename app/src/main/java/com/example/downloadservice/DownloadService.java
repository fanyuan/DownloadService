package com.example.downloadservice;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;



import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class DownloadService extends JobIntentService {
    /**
     * 下载url
     */
    public static final String WORK_URL = "work_url";
    /**
     *下载完成后的本地文件存放路径
     */
    public static final String TARGET_FILE_PATH_URL = "target_file_path_url";
    /**
     * 这个Service 唯一的id
     */
    static final int JOB_ID = 10110;
    private static final String TAG = "ddebug";

    private static ConcurrentHashMap<String,DownloadCallback> callbacks = new ConcurrentHashMap<>();

    static void enqueueWork(Context context, Intent work) {
        Utils.log(context,"enqueueWork --- " +  Thread.currentThread().getName());
        enqueueWork(context, DownloadInterface.class, JOB_ID, work);
    }
    /**
     * Convenience method for enqueuing work in to this service.
     */
    static void enqueueWork(Context context, Intent work, DownloadCallback notifyCallback) {
        Utils.log(context,"enqueueWork --- " +  Thread.currentThread().getName());

        String url = work.getStringExtra(WORK_URL);
        //Apputils.log(this,"enqueueWork --- url = " + url + "---notifyCallback == null ---" +(notifyCallback==null));
        if(TextUtils.isEmpty(url) && notifyCallback != null){
            notifyCallback.fail("下载url不能为空");
            return;
        }

        callbacks.put(url,notifyCallback);
        enqueueWork(context, DownloadService.class, JOB_ID, work);
    }
    Retrofit retrofit2 = new Retrofit.Builder().baseUrl("https://www.baidu.com/").addConverterFactory(GsonConverterFactory.create()).build();
    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        String url = intent.getStringExtra(WORK_URL);
        Utils.log(this,"DownloadService mp3 url = " + url);
        if(TextUtils.isEmpty(url)){
            DownloadCallback callback = callbacks.get(url);
            if(callback != null){
                callback.fail("url不能为空");
            }
            callbacks.remove(url);
            return;
        }

        DownloadInterface versionService = retrofit2.create(DownloadInterface.class);


        Call<ResponseBody> call = versionService.downLoad(url);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                if(!ServiceHelper.checkObjectResponseCommon(response)){
                    DownloadCallback callback = callbacks.get(url);
                    if(callback !=null){
                        callback.fail(response.raw().networkResponse().toString().toString());
                        callbacks.remove(url);
                    }
                    return;
                }
                String path = intent.getStringExtra(TARGET_FILE_PATH_URL);
                Utils.log(getApplicationContext(),"Environment.getDownloadCacheDirectory().getAbsolutePath() = " + Environment.getDownloadCacheDirectory().getAbsolutePath());
                if(TextUtils.isEmpty(path)){
                    path = getExternalCacheDir().getAbsolutePath() + File.separator + "tmp" + File.separator + "temp.mp3";
                }
                Utils.log(getApplicationContext(),"path = " + path);


                //path = getExternalFilesDir(null).getAbsolutePath()+ File.separator + "tmp";//Environment.getDownloadCacheDirectory().getAbsolutePath() + File.separator + "tmp";//Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "tmp";//getExternalFilesDir(null).getAbsolutePath();
                File file = new File(path);
                if(!file.exists()){
                    boolean b = new File(file.getParent()).mkdirs();
                }

                String finalPath = path;
                Executors.newSingleThreadExecutor().execute(new Runnable() {
                    @Override
                    public void run() {
                        writeResponseBodyToDisk(url,response.body(), finalPath);
                    }
                });

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                String url = call.request().url().toString();
                DownloadCallback callback = callbacks.get(url);
                if(callback !=null){
                    callback.fail("文件下载失败，请检查网络连接状态");
                    callbacks.remove(url);
                }
            }
        });
//            Apputils.log(this,"onHandleWork");
//            if(!ServiceHelper.checkObjectResponseCommon(response)){
//                if(callback !=null){
//                    callback.fail(response.raw().networkResponse().toString().toString());
//                }
//                return;
//            }
//            Utils.log(this,"onHandleWork111111");
//            String path = getExternalFilesDir(null).getAbsolutePath()+ File.separator + "tmp";//Environment.getDownloadCacheDirectory().getAbsolutePath() + File.separator + "tmp";//Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "tmp";//getExternalFilesDir(null).getAbsolutePath();
//            File file = new File(path);
//            if(!file.exists()){
//                boolean b = file.mkdirs();
//            }
//
//            String apkPath = path + File.separator + "update.apk";
//            Utils.log(this,"apkPath = " + apkPath);
//            writeResponseBodyToDisk(response.body(),apkPath);

    }
    private boolean writeResponseBodyToDisk(String url,ResponseBody body,String path) {

        Utils.log(this,"0writeResponseBodyToDisk --- path---" + path);
        try {
            // todo change the file location/name according to your needs
            File targetFile = new File(path);//new File(getExternalFilesDir(null) + File.separator + "Future Studio Icon.png");

            //Apputils.log(this,"targetFile.exists() = " +targetFile.exists() +  "---1writeResponseBodyToDisk --- path---" + targetFile.getAbsolutePath());
            boolean delete = false;
            if(targetFile.exists()){
                delete = targetFile.delete();
            }
            //Apputils.log(this,"targetFile.delete() = " + delete);
            InputStream inputStream = null;
            OutputStream outputStream = null;

            try {
                byte[] fileReader = new byte[4096];

                long fileSize = body.contentLength();
                long fileSizeDownloaded = 0;

                int currentProgress = 0;
                Double totalSize = Double.valueOf(Long.toString(fileSize));
                Double currentSize = 0d;


                inputStream = body.byteStream();
                outputStream = new FileOutputStream(targetFile);

                while (true) {
                    int read = inputStream.read(fileReader);

                    if (read == -1) {
                        break;
                    }

                    outputStream.write(fileReader, 0, read);

                    fileSizeDownloaded += read;

                    currentSize = Double.valueOf(Long.toString(fileSizeDownloaded));
                    int progress = (int) ((currentSize/totalSize)*100);
                    if(currentProgress != progress){
                        currentProgress = progress;
                        DownloadCallback callback = callbacks.get(url);
                        if(callback != null ){//&& callback.isNeedNotify()
                            callback.progressNotify(progress);
                        }
                    }
                    if(fileSizeDownloaded == fileSize){
                        DownloadCallback callback = callbacks.get(url);
                        if(callback !=null){
                            callback.finish(targetFile.getAbsolutePath());
                            callbacks.remove(url);
                        }
                    }
                    //Log.d(TAG, "file download: " + fileSizeDownloaded + " of " + fileSize + "---" +progress);
                }

                outputStream.flush();

                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }

                if (outputStream != null) {
                    outputStream.close();
                }
            }

        } catch (IOException e) {
            return false;
        }
    }

    /**
     * 下载进度通知回调，默认实现
     */
    public static class SimpleDownloadCallback implements DownloadCallback{
        public Object tag = null;
        @Override
        public void progressNotify(int progress) {

        }

        @Override
        public void finish(String filePath) {

        }

        @Override
        public void fail(String msg) {

        }
    }

    /**
     * 下载进度通知回调
     */
    public interface DownloadCallback {
        //boolean isNeedNotify();

        /**
         * 下载进度通知
         * @param progress
         */
        void progressNotify(int progress);

        /**
         * 工作已完成
         */
        void finish(String filePath);

        /**
         * 网络下载失败
         * @param msg
         */
        void fail(String msg);
    }

}
