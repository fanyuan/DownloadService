package com.example.downloadservice;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

/**
 * 载相关服务接口
 * 飞星
 */
public interface DownloadInterface {

    /**
     * 下载服务器音频文件
     * 飞星
     * @param fileUrl
     * @return
     */
    @GET
    @Streaming
    Call<ResponseBody> downLoad(@Url String fileUrl);
}