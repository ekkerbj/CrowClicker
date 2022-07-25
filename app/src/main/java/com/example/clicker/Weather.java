package com.example.clicker;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Weather {

    private static final String TAG = "weather";
    public String temperature;
    public String feelsLike;
    public String dewPoint;
    public String windSpeed;
    public String windDir;
    public String windGust;
    public String date;
    public String precipProbability;
    public String humidity;
    public String pressure;
    public String cloudCover;

    public void populate(double lat, double lon, Date cal, Context context, final ClickerCallback callback) {
        String url = "https://api.darksky.net/forecast/9741785dc8b4e476aa45f20076c71fd9/" + lat + "," + lon + "," + (cal.getTime() / 1000);
        populate(url, context, callback);
    }

    public void populate(double lat, double lon, Context context, final ClickerCallback callback) {
        String url = "https://api.darksky.net/forecast/9741785dc8b4e476aa45f20076c71fd9/" + lat + "," + lon;
        populate(url, context, callback);
    }

    public void populate(String url, Context context, final ClickerCallback callback) {
        RequestQueue queue = Volley.newRequestQueue(context);
        StringRequest stringRequest = pullWeather(url, callback);
        queue.add(stringRequest);
    }

    public StringRequest pullWeather(String url, ClickerCallback callback) {
        return new StringRequest(Request.Method.GET, url,
                                 new Response.Listener<String>() {
                                     @Override
                                     public void onResponse(String response) {
                                         try {
                                             JSONObject reader = new JSONObject(response);
                                             JSONObject main = reader.getJSONObject("currently");
                                             temperature = ((int) Double.parseDouble(main.getString("temperature"))) + "";
                                             feelsLike = ((int) Double.parseDouble(main.getString("apparentTemperature"))) + "";
                                             dewPoint = ((int) Double.parseDouble(main.getString("dewPoint"))) + "";
                                             windSpeed = ((int) Double.parseDouble(main.getString("windSpeed"))) + "";
                                             windDir = getCardinalDirection(main.getDouble("windBearing"));
                                             windGust = ((int) Double.parseDouble(main.getString("windGust"))) + "";
                                             date = new SimpleDateFormat("MM-dd-yyyy h:mm a").format(new Date(1000 * Long.parseLong(main.getString("time"))));
                                             precipProbability = main.getString("precipProbability");
                                             humidity = main.getString("humidity");
                                             pressure = ((int) Double.parseDouble(main.getString("pressure"))) + "";
                                             cloudCover = main.getString("cloudCover");
                                         } catch (JSONException e) {
                                             Log.e(TAG, "Failure to create SheetAccess", e);
                                         }

                                         callback.onSuccess();
                                     }
                                 }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                callback.onFailure();
            }
        });
    }

    String getCardinalDirection(double input) {
        String[] directions = {"N", "NE", "E", "SE", "S", "SW", "W", "NW", "N"};
        int index = (int) Math.floor(((input - 22.5) % 360) / 45);
        return directions[index + 1];
    }
}
