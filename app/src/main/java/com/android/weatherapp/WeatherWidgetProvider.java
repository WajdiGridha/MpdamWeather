package com.android.weatherapp;

import static android.content.Context.MODE_PRIVATE;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.RemoteViews;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Implementation of App Widget functionality.
 */
public class WeatherWidgetProvider extends AppWidgetProvider {


    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds){
            Intent intent = new Intent(context, MainActivity.class);
            PendingIntent pendingIntent =
                    PendingIntent.getActivity(context, 0, intent,PendingIntent.FLAG_UPDATE_CURRENT |
                            PendingIntent.FLAG_IMMUTABLE);

            Intent updateIntent = new Intent(context, WeatherWidgetProvider.class);
            updateIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS,
                    appWidgetIds);

            PendingIntent pendingUpdate =
                    PendingIntent.getBroadcast(context, 0, updateIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT |
                                    PendingIntent.FLAG_IMMUTABLE);

            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.weather_widget_provider);
            views.setOnClickPendingIntent(R.id.widgetProvider, pendingIntent);

            appWidgetManager.updateAppWidget(appWidgetId, views);


        SharedPreferences sharedPreferences = context.getSharedPreferences("myPrefs", MODE_PRIVATE);
        String temperature = sharedPreferences.getString("temperature", "11");
        String humidity = sharedPreferences.getString("humidity", "");
        String description = sharedPreferences.getString("desc", "");
        String city = sharedPreferences.getString("city", "");
        String icon = sharedPreferences.getString("icon","");

        views.setTextViewText(R.id.temperatureTextView,  temperature);
        views.setTextViewText(R.id.windTextView,  description);
        views.setTextViewText(R.id.CitytextView,  city);
        views.setTextViewText(R.id.humidityTextview,humidity);
        views.setImageViewResource(R.id.img_weather_ic,getWeatherIcon(icon));
        views.setOnClickPendingIntent(R.id.update,pendingUpdate);



        appWidgetManager.updateAppWidget(appWidgetIds, views);

    }
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction().equals(AppWidgetManager.ACTION_APPWIDGET_UPDATE)) {

            fetchWeatherData(context);
        }

        super.onReceive(context, intent);
    }



    private void fetchWeatherData(Context context) {

        SharedPreferences sp = context.getSharedPreferences("my_preferences", Context.MODE_PRIVATE);
        String latitude = sp.getString("latitude", "");
        String longitude = sp.getString("longitude", "");


        String url= "https://api.openweathermap.org/data/2.5/weather?lat="+latitude+"&lon="+longitude+"&appid=488f4111e6b7924073ff22cd896b2e2a"+"&units=metric";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String temperature = String.valueOf((int) Math.round(response.getJSONObject("main").getDouble("temp")));
                            String humidity = response.getJSONObject("main").getString("humidity") + " %";
                            String desc = response.getJSONArray("weather").getJSONObject(0).getString("description");
                            String city = response.getString("name") ;
                            String icon = response.getJSONArray("weather").getJSONObject(0).getString("icon");



                            SharedPreferences sharedPreferences = context.getSharedPreferences("myPrefs", MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("temperature", temperature);
                            editor.putString("humidity",humidity);
                            editor.putString("desc",desc);
                            editor.putString("city",city);
                            editor.putString("icon",icon);
                            editor.apply();


                            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, WeatherWidgetProvider.class));
                            onUpdate(context, appWidgetManager, appWidgetIds);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                });

        RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.add(jsonObjectRequest);
    }

    public int getWeatherIcon(String icon) {
        switch (icon) {
            case "01d":
                return R.drawable.ic_01d;
            case "01n":
                return R.drawable.ic_01n;
            case "02d":
                return R.drawable.ic_02d;
            case "02n":
                return R.drawable.ic_02n;
            case "03d":
                return R.drawable.ic_03d;
            case "03n":
                return R.drawable.ic_03n;
            case "04d":
                return R.drawable.ic_04d;
            case "04n":
                return R.drawable.ic_04n;
            case "09d":
                return R.drawable.ic_09d;
            case "09n":
                return R.drawable.ic_09n;
            case "10d":
                return R.drawable.ic_10d;
            case "10n":
                return R.drawable.ic_10n;
            case "11d":
                return R.drawable.ic_11d;
            case "11n":
                return R.drawable.ic_11n;
            case "13d":
                return R.drawable.ic_13d;
            case "13n":
                return R.drawable.ic_13n;
            case "50d":
                return R.drawable.ic_50d;
            case "50n":
                return R.drawable.ic_50n;
            default:
                return R.drawable.ic_unknown;
        }
    }
}