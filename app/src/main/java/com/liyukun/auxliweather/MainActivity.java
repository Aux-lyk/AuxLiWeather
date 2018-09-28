package com.liyukun.auxliweather;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Boolean showCityList = getIntent().getBooleanExtra("show_city_list",false);
        if(!showCityList){
            SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("weather", Context.MODE_PRIVATE);
            if(sharedPreferences.getString("weather", null) != null){
                Intent intent = new Intent(this, WeatherActivity.class);
                startActivity(intent);
                finish();
            }
        }
    }
}
