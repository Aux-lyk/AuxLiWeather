package com.liyukun.auxliweather.utils;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.liyukun.auxliweather.db.City;
import com.liyukun.auxliweather.db.County;
import com.liyukun.auxliweather.db.Province;
import com.liyukun.auxliweather.gson.Weather;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Utiluty {
    //解析 省 数据
    public static boolean handleProvinceResponse(String response){
        if(!TextUtils.isEmpty(response)){
            try {
                JSONArray allProvinces = new JSONArray(response);
                for(int i = 0;i < allProvinces.length(); i++){
                    JSONObject provinceObject = allProvinces.getJSONObject(i);
                    //存储到数据库
                    Province province = new Province();
                    province.setProvinceName(provinceObject.getString("name"));
                    province.setProvinceCode(provinceObject.getInt("id"));
                    province.save();
                }
                return true;
            }catch (JSONException e){
                e.printStackTrace();
            }
        }
        return false;
    }

    //解析 市 数据
    public static boolean handleCityResponse(String response, int provinceId){
        if(!TextUtils.isEmpty(response)){
            try {
                JSONArray allCities = new JSONArray(response);
                for(int i = 0; i < allCities.length(); i++){
                    JSONObject cityObject = allCities.getJSONObject(i);
                    //存储到数据库
                    City city = new City();
                    city.setCityName(cityObject.getString("name"));
                    city.setCityCode(cityObject.getInt("id"));
                    city.setProvinceId(provinceId);
                    city.save();
                }
                return true;
            }catch (JSONException e){
                e.printStackTrace();
            }
        }
        return false;
    }

    //解析 县 数据
    public static boolean handleCountyResponse(String response, int cityId){
        try {
            JSONArray allCounties = new JSONArray(response);
            for(int i = 0; i < allCounties.length(); i++){
                JSONObject countyObject = allCounties.getJSONObject(i);
                //存储到数据库
                County county = new County();
                county.setCountyName(countyObject.getString("name"));
                county.setWeatherId(countyObject.getString("weather_id"));
                county.setCityId(cityId);
                county.save();
            }
            return true;
        }catch (JSONException e){
            e.printStackTrace();
        }
        return false;
    }

    //将返回的JSON数据解析成 Weather 实体类
    public static Weather handleWeatherResponse(String response){
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray jsonArray = jsonObject.getJSONArray("HeWeather");
            String weatherContent = jsonArray.getJSONObject(0).toString();
            //返回解析后的数据。将 weatherContent 转成 Weather 类
            return new Gson().fromJson(weatherContent, Weather.class);
        }catch (JSONException e){
            e.printStackTrace();
        }
        return null;
    }
}
