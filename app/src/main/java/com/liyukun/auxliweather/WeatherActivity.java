package com.liyukun.auxliweather;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.liyukun.auxliweather.gson.Forecast;
import com.liyukun.auxliweather.gson.Weather;
import com.liyukun.auxliweather.utils.HttpUtil;
import com.liyukun.auxliweather.utils.Utiluty;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {
    private ScrollView weatherLayout;
    private TextView titleCity;
    private TextView titleUpdateTime;
    private TextView degreeText;
    private TextView weatherInfoText;
    private LinearLayout forecastLayout;
    private TextView aqiText;
    private TextView pm25Text;
    private TextView comfortText;
    private TextView carWashText;
    private TextView sportText;
    private int countyId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);
        //初始化控件
        weatherLayout = (ScrollView) findViewById(R.id.weather_layout);
        titleCity = (TextView) findViewById(R.id.title_city);
        titleUpdateTime = (TextView) findViewById(R.id.title_update_time);
        degreeText = (TextView) findViewById(R.id.degree_text);
        weatherInfoText = (TextView) findViewById(R.id.weather_info_text);
        forecastLayout = (LinearLayout) findViewById(R.id.forecast_layout);
        aqiText = (TextView) findViewById(R.id.api_text);
        pm25Text = (TextView) findViewById(R.id.pm25_text);
        comfortText = (TextView) findViewById(R.id.comfort_text);
        carWashText = (TextView) findViewById(R.id.car_wash_text);
        sportText = (TextView) findViewById(R.id.sport_text);
        //获取缓存对象
        //优化-只取所需的具体的一个而不是一次性全部
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("weather", Context.MODE_PRIVATE);
        //优化end
//        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        //获取缓存数据
        String weatherString = sharedPreferences.getString("weather", null);
        countyId = sharedPreferences.getInt("county_id",0);
        int countyIdIntent = getIntent().getIntExtra("county_id",0);
        if(weatherString != null && countyId == countyIdIntent){ //有缓存并且countyId相等，则用缓存展示
            Weather weather = Utiluty.handleWeatherResponse(weatherString);
            showWeatherInfo(weather);
        }else {  //请求获取天气数据
            String weatherId = getIntent().getStringExtra("weather_id");
            weatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(weatherId);
        }
        //重新选择城市
        Button choiceNow = (Button) findViewById(R.id.choice_now);
        choiceNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(WeatherActivity.this, MainActivity.class);
                intent.putExtra("show_city_list", true);
                startActivity(intent);
                finish();
            }
        });
    }

    //根据天气id请求城市天气信息
    public void requestWeather(final String weatherId){
        String weatherUrl = "http://guolin.tech/api/weather?cityid=" + weatherId + "&key=01aeaf65459e48b583a4aeab0e39cdba";
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this,"获取天气数据失败",Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                final Weather weather = Utiluty.handleWeatherResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(weather != null && "ok".equals(weather.status)){
                            SharedPreferences.Editor editor =  getApplicationContext().getSharedPreferences("weather", Context.MODE_PRIVATE).edit();
                            editor.putString("weather", responseText);
                            editor.putInt("county_id", countyId);
                            editor.apply();
                            showWeatherInfo(weather);
                        }else {
                            Toast.makeText(WeatherActivity.this,"解析天气数据失败",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }

    //处理并展示 Weather 实体类的数据
    public void showWeatherInfo(Weather weather){
        // basic and now 的数据
        String cityName = weather.basic.cityName;
        String updateTime = "最后更新时间 " + weather.basic.update.updateTime.split(" ")[1];
        String degree = weather.now.temperature + "℃";
        String weatherInfo = weather.now.more.info;
        //将数据赋值给控件
        titleCity.setText(cityName);
        titleUpdateTime.setText(updateTime);
        degreeText.setText(degree);
        weatherInfoText.setText(weatherInfo);

        //先将未来天气子视图移除，即清空数据。因为新的条数可能不一致
        forecastLayout.removeAllViews();
        //添加信息未来天气信息
        for(Forecast forecast : weather.forecastList){
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item, forecastLayout, false);
            TextView dateText = (TextView) view.findViewById(R.id.date_text);
            TextView infoText = (TextView) view.findViewById(R.id.info_text);
            TextView maxText = (TextView) view.findViewById(R.id.max_text);
            TextView minText = (TextView) view.findViewById(R.id.min_text);
            dateText.setText(forecast.date);
            infoText.setText(forecast.more.info);
            maxText.setText(forecast.temperature.max);
            minText.setText(forecast.temperature.min);
            //每一天添加一个对应的子视图
            forecastLayout.addView(view);
        }

        // aqi 空气质量数据
        if(weather.aqi != null){
            aqiText.setText(weather.aqi.city.aqi);
            pm25Text.setText(weather.aqi.city.pm25);
        }

        // suggestion 生活建议数据
        String comfort = "舒适度：" + weather.suggestion.comfort.info;
        String carWash = "洗车指数：" + weather.suggestion.carwash.info;
        String sport = "运动建议：" + weather.suggestion.sport.info;
        comfortText.setText(comfort);
        carWashText.setText(carWash);
        sportText.setText(sport);

        //数据添加后显示 weatherLayout
        weatherLayout.setVisibility(View.VISIBLE);
    }
}
