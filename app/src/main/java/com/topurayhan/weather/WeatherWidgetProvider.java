package com.topurayhan.weather;

import static android.content.Context.MODE_PRIVATE;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;

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


        views.setTextViewText(R.id.temperatureTextView,  temperature);
        views.setTextViewText(R.id.windTextView,  description);
        views.setTextViewText(R.id.CitytextView,  city);
        views.setTextViewText(R.id.humidityTextview,humidity);
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

                            SharedPreferences sharedPreferences = context.getSharedPreferences("myPrefs", MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("temperature", temperature);
                            editor.putString("humidity",humidity);
                            editor.putString("desc",desc);
                            editor.putString("city",city);
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


}