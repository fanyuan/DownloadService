package com.example.downloadservice;

import android.content.Context;
import android.content.Intent;

import java.io.File;

/**
 * 朗诵下载帮助类
 */
public class DownloadHelper {
    public static final String SUFFIX_MP3 = ".mp3";

    /**
     * 下载的功能方法
     * @param context
     * @param downloadUrl 下载链接的url
     * @param outFilePath 下载完成后保存本地的本地路径
     * @param callback  下载过程回调
     */
    public static void download(Context context, String downloadUrl, String outFilePath, DownloadService.DownloadCallback callback){
        Intent workIntent = new Intent();
        workIntent.putExtra(DownloadService.WORK_URL, downloadUrl);
        workIntent.putExtra(DownloadService.TARGET_FILE_PATH_URL,outFilePath);
        DownloadService.enqueueWork(context, workIntent, callback);
        Utils.log(context,"DownloadHelper  download outFilePath = " + outFilePath +" downloadUrl =  " + downloadUrl);
    }
}
