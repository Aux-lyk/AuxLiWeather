package com.liyukun.auxliweather.gson;

import com.google.gson.annotations.SerializedName;

public class Basic {
    //建立映射数据的city对应字段名cityName
    @SerializedName("city")
    public String cityName;

    @SerializedName("id")
    public String weatherId;

    public Update update;

    public class Update{
        @SerializedName("loc")
        public String updateTime;
    }
}
