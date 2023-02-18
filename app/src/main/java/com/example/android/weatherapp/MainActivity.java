package com.example.android.weatherapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TextView t1_temp, t2_city, t3_weather_type, description_textView;
    private TextView wind_speed_textView, humidity_textView, visibility_textView, search_city_bn;

    private EditText cityEditText;
    String cityName;
    LocationManager locationManager;
    int PERMISSION_CODE = 1;
    private ImageView weatherIcon;
    private LinearLayout homeLinearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        t1_temp = (TextView) findViewById(R.id.temp_textView);
        t2_city = (TextView) findViewById(R.id.city_textView);
        t3_weather_type = (TextView) findViewById(R.id.weather_type_textView);
        search_city_bn = (TextView) findViewById(R.id.citySearch_button);
        cityEditText = (EditText) findViewById(R.id.city_editText);
        weatherIcon = (ImageView) findViewById(R.id.weather_icon);
        wind_speed_textView = (TextView) findViewById(R.id.wind_speed_tv);
        humidity_textView = (TextView) findViewById(R.id.humidity_tv);
        visibility_textView = (TextView) findViewById(R.id.visibility_tv);
        description_textView = (TextView) findViewById(R.id.desc_tv);

        homeLinearLayout = (LinearLayout) findViewById(R.id.home_linearLayout);

        search_city_bn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                findWeather();
            }
        });

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},PERMISSION_CODE);
        }
        Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        cityName = getCityName(location.getLongitude(), location.getLatitude());

        cityEditText.setText(cityName);
        if (cityEditText == null){
            Toast.makeText(MainActivity.this, "Your city not found,Please enter the city name", Toast.LENGTH_SHORT).show();
        }else {
            findWeather();
        }

        cityEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cityName = cityEditText.getText().toString().trim();
                if (cityName.isEmpty()){
                    cityEditText.setError("Enter cite name");
                }else {
                    findWeather();
                }
            }
        });


    }

    private String getCityName(double longitude, double latitude){
        String cityName = "Not Found";
        Geocoder geocoder = new Geocoder(getBaseContext(), Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude,longitude,10);
            for (Address ad: addresses){
                if (ad!=null){
                    String city = ad.getLocality();
                    if (city != null && !city.equals("")){
                        cityName = city;

                    }else {
                        Log.d("TAG","City not found");
                    }
                }
            }
        }catch (IOException e){
            e.printStackTrace();
        }
        return cityName;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_CODE){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();

            }else {
                Toast.makeText(this, "Please give me permission", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    public void findWeather() {
        String apiKey = "e7704bc895b4a8d2dfd4a29d404285b6";
        String city = cityEditText.getText().toString();
        String url = "https://api.openweathermap.org/data/2.5/weather?q=" + city + "&appid=" + apiKey;

        JsonObjectRequest jor = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONObject main_object = response.getJSONObject("main");
                    JSONArray array = response.getJSONArray("weather");
                    JSONObject object = array.getJSONObject(0);
                    String temp = main_object.getString("temp");
                    String feelsLike = main_object.getString("feels_like");
                    String weatherType = object.getString("main");
                    String desc = object.getString("description");
                    String cityName = response.getString("name");
                    String day = main_object.getString("temp_max");
                    String night = main_object.getString("temp_min");

                    if(weatherType.equals("Thunderstorm")) {
                        weatherIcon.setImageResource(R.drawable.thunderstorm);
                        homeLinearLayout.setBackgroundResource(R.drawable.rainy_bg);
                    }
                    else if(weatherType.equals("Drizzle") || weatherType.equals("Rain")) {
                        weatherIcon.setImageResource(R.drawable.showers);
                        homeLinearLayout.setBackgroundResource(R.drawable.rainy_bg);
                    }
                    else if(weatherType.equals("Snow")) {
                        weatherIcon.setImageResource(R.drawable.snow);
                        homeLinearLayout.setBackgroundResource(R.drawable.cloudy_bg);
                    }
                    else if(weatherType.equals("Mist") || weatherType.equals("Smoke") || weatherType.equals("Haze") || weatherType.equals("Dust")
                    || weatherType.equals("Fog") || weatherType.equals("Sand") || weatherType.equals("Ash") || weatherType.equals("Squall")
                    || weatherType.equals("Tornado")) {
                        weatherIcon.setImageResource(R.drawable.mist);
                        homeLinearLayout.setBackgroundResource(R.drawable.cloudy_bg);
                    }
                    else if(weatherType.equals("Clear")) {
                        weatherIcon.setImageResource(R.drawable.sunny);
                        homeLinearLayout.setBackgroundResource(R.drawable.sunny_bg);
                    }
                    else if(weatherType.equals("Clouds")) {
                        weatherIcon.setImageResource(R.drawable.broken_clouds);
                        homeLinearLayout.setBackgroundResource(R.drawable.cloudy_bg);
                    }



                    t2_city.setText(cityName);
                    t3_weather_type.setText(weatherType);

                    description_textView.setText(desc);


                    JSONObject wind_object = response.getJSONObject("wind");
                    String wind_speed = wind_object.getString("speed");
                    wind_speed_textView.setText(wind_speed + " km/h");

                    String humidity = main_object.getString("humidity");
                    humidity_textView.setText(humidity + "%");


                    double temp_int = Double.parseDouble(temp) - 273.15;
                    int i = (int) Math.round(temp_int);
                    t1_temp.setText(String.valueOf(i));


                    String visibility = response.getString("visibility");
                    double visb_int = Double.parseDouble(visibility) / 1000;
                    visibility_textView.setText(String.valueOf(visb_int) + " km");


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this, "Enter valid city name", Toast.LENGTH_SHORT).show();
            }
        });
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(jor);
    }
}
