package com.liyukun.auxliweather.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;

import com.liyukun.auxliweather.gson.Weather;
import com.liyukun.auxliweather.utils.RequestWeather;
import com.liyukun.auxliweather.utils.Utiluty;

public class AutoUpdateService extends Service {
    public AutoUpdateService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        updateWeather();
        updateBingPic();
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        int anHour = 8 * 60 * 60 * 1000; //八小时的毫秒数
        long triggerAtTime = SystemClock.elapsedRealtime() + anHour;
        Intent myIntent = new Intent(this, AutoUpdateService.class);
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, myIntent, 0);
        alarmManager.cancel(pendingIntent);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pendingIntent);
        return super.onStartCommand(intent, flags, startId);
    }

    //更新天气信息
    private void updateWeather(){
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("weather_id", Context.MODE_PRIVATE);
        String weatherString = sharedPreferences.getString("weather_id",null);
        if(weatherString != null){
            Weather weather = Utiluty.handleWeatherResponse(weatherString);
            String weatherId = weather.basic.weatherId;
            String responseText = RequestWeather.requestWeatherInfo(weatherId);
            Weather newWeather = Utiluty.handleWeatherResponse(responseText);
            if(newWeather != null && "ok".equals(newWeather.status)){
                SharedPreferences.Editor editor =  getApplicationContext().getSharedPreferences("weather", Context.MODE_PRIVATE).edit();
                editor.putString("weather", responseText);
                editor.apply();
            }
        }
    }

    //更新必应图片
    private void updateBingPic(){

    }
}
