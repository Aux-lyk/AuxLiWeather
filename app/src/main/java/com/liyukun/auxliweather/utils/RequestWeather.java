package com.liyukun.auxliweather.utils;

import com.liyukun.auxliweather.gson.Weather;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class RequestWeather {
    static String responseText;
    public static String requestWeatherInfo(final String weatherId){
        String weatherUrl = "http://guolin.tech/api/weather?cityid=" + weatherId + "&key=01aeaf65459e48b583a4aeab0e39cdba";
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                responseText = "";
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                responseText = response.body().string();
            }
        });
        return responseText;
    }
}
