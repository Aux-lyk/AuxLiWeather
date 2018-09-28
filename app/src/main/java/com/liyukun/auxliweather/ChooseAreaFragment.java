package com.liyukun.auxliweather;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.liyukun.auxliweather.db.City;
import com.liyukun.auxliweather.db.County;
import com.liyukun.auxliweather.db.Province;
import com.liyukun.auxliweather.utils.HttpUtil;
import com.liyukun.auxliweather.utils.Utiluty;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ChooseAreaFragment extends Fragment {
    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTY = 2;
    private ProgressBar progressBar;
    private TextView titleText;
    private Button backButton;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private List<String> dataList = new ArrayList<>();

    private List<Province> provinceList;
    private List<City> cityList;
    private List<County> countyList;
    private Province selectedProvince;
    private City selectedCity;
    private int currentLevel;

    private String baseAddress = "http://guolin.tech/api/china/";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.choose_area,container,false);
        titleText = (TextView) view.findViewById(R.id.title_text);
        backButton = (Button) view.findViewById(R.id.back_btn);
        listView = (ListView) view.findViewById(R.id.list_view);
        progressBar = (ProgressBar) view.findViewById(R.id.circleProgressBar);
        adapter = new ArrayAdapter<>(getContext(),android.R.layout.simple_list_item_1,dataList);
        listView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                if(currentLevel == LEVEL_PROVINCE){
                    selectedProvince = provinceList.get(position);
                    queryCities();
                }else if(currentLevel == LEVEL_CITY){
                    selectedCity = cityList.get(position);
                    queryCounties();
                }else if(currentLevel == LEVEL_COUNTY){
                    //获取天气id 和 countyId
                    String weatherId = countyList.get(position).getWeatherId();
                    int countyId = countyList.get(position).getId();
                    //使用Intent跳转到WeatherActivity
                    Intent intent = new Intent(getActivity(), WeatherActivity.class);
                    //附加天气id 和 countyId
                    intent.putExtra("weather_id",weatherId);
                    intent.putExtra("county_id",countyId);
                    //开启目标活动
                    startActivity(intent);
                    //结束当前活动
                    getActivity().finish();
                }
            }
        });
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(currentLevel == LEVEL_COUNTY){
                    queryCities();
                }else if(currentLevel == LEVEL_CITY){
                    queryProvince();
                }
            }
        });
        queryProvince();
    }

    //查询省
    private void queryProvince(){
        titleText.setText("中国");
        backButton.setVisibility(View.GONE);
        //优先从数据库获取
        provinceList = DataSupport.findAll(Province.class);
        if(provinceList.size() > 0){
            dataList.clear();
            //更新视图数据
            for(Province province : provinceList){
                dataList.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            //list选中第一个
            listView.setSelection(0);
            //修改当前省市县等级
            currentLevel = LEVEL_PROVINCE;
        }else{ //数据库没有就从服务器获取
            String address = baseAddress;
            queryFromServer(address,"province");
        }
    }

    //查询市
    private void queryCities(){
        titleText.setText(selectedProvince.getProvinceName());
        backButton.setVisibility(View.VISIBLE);
        cityList = DataSupport.where("provinceId = ?",String.valueOf(selectedProvince.getId())).find(City.class);
        if(cityList.size() > 0){
            dataList.clear();
            for(City city : cityList){
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_CITY;
        }else{
            int prvinceCode = selectedProvince.getProvinceCode();
            String address = baseAddress + prvinceCode;
            queryFromServer(address,"city");
        }
    }

    //查询县
    private void queryCounties(){
        titleText.setText(selectedCity.getCityName());
        backButton.setVisibility(View.VISIBLE);
        countyList = DataSupport.where("cityId = ?",String.valueOf(selectedCity.getId())).find(County.class);
        if(countyList.size() > 0){
            dataList.clear();
            for(County county : countyList){
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_COUNTY;
        }else{
            int prvinceCode = selectedProvince.getProvinceCode();
            int cityCode = selectedCity.getCityCode();
            String address = baseAddress + prvinceCode + "/" + cityCode;
            queryFromServer(address,"county");
        }
    }

    //向服务器请求数据
    private void queryFromServer(String address, final String type){
        showProgressBar();
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                //通过runOnUiThread()回到主线程逻辑去操作UI
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressBar(); //操作UI的方法
                        Toast.makeText(getContext(),"加载失败",Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                //获取响应的数据
                String responseText = response.body().string();
                boolean result = false;
                //判断省市县后解析数据并存储到数据库
                switch (type){
                    case "province":
                        result = Utiluty.handleProvinceResponse(responseText);
                        break;
                    case "city":
                        result = Utiluty.handleCityResponse(responseText,selectedProvince.getId());
                        break;
                    case "county":
                        result = Utiluty.handleCountyResponse(responseText,selectedCity.getId());
                        break;
                }
                if(result){ //重新加载数据
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressBar();
                            Toast.makeText(getContext(),"加载成功",Toast.LENGTH_SHORT).show();
                            switch (type){
                                case "province":
                                    queryProvince();
                                    break;
                                case "city":
                                    queryCities();
                                    break;
                                case "county":
                                    queryCounties();
                                    break;
                            }
                        }
                    });
                }else{
                    Toast.makeText(getContext(),"加载解析失败",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void showProgressBar(){
        progressBar.setVisibility(View.VISIBLE);
    }

    private void closeProgressBar(){
        if(progressBar != null){
            progressBar.setVisibility(View.GONE);
        }
    }
}
