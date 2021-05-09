package com.example.downloadservice;

import retrofit2.Response;

public class ServiceHelper {
    public static  boolean checkObjectResponseCommon(Response response) {
        return response != null
                && response.body() != null;
    }
}