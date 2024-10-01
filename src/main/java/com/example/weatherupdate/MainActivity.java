package com.example.weatherupdate;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    // UI Elements
    TextView cityName, details;
    Button srcBtn;

    // API Key and URL for weather information
    String apiKey = "956da2cfa0356405ec3445655bf0822b";
    String url;

    // AdView for displaying banner ads
    private AdView adView;

    // AsyncTask for fetching weather information
    class getWeather extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            StringBuilder result = new StringBuilder();
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                String line = "";
                while ((line = reader.readLine()) != null) {
                    result.append(line).append("\n");
                }
                return result.toString();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            try {
                JSONObject jsonObject = new JSONObject(result);
                JSONObject main = jsonObject.getJSONObject("main");
                double temp = main.getDouble("temp") - 273.15;
                double feelsLike = main.getDouble("feels_like") - 273.15;
                double tempMin = main.getDouble("temp_min") - 273.15;
                double tempMax = main.getDouble("temp_max") - 273.15;

                String weatherInfo = "Temperature: " + String.format("%.2f", temp) + "°C\n" +
                        "Feels like: " + String.format("%.2f", feelsLike) + "°C\n" +
                        "Min Temp: " + String.format("%.2f", tempMin) + "°C\n" +
                        "Max Temp: " + String.format("%.2f", tempMax) + "°C";
                details.setText(weatherInfo);
            } catch (Exception e) {
                e.printStackTrace();
                details.setText("Error parsing weather data");
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Bind views
        cityName = findViewById(R.id.cityName);
        details = findViewById(R.id.details);
        srcBtn = findViewById(R.id.srcBtn);
        adView = findViewById(R.id.adView);

        // Initialize Mobile Ads SDK
        MobileAds.initialize(this, initializationStatus -> {});

        // Load Banner Ad
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        // Button click event for fetching weather
        srcBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String city = cityName.getText().toString().trim();
                if (!city.isEmpty()) {
                    url = "https://api.openweathermap.org/data/2.5/weather?q=" + city + "&appid=" + apiKey;
                    getWeather task = new getWeather();
                    try {
                        String temp = task.execute(url).get();
                        if (temp == null) {
                            details.setText("Cannot find weather details for " + city);
                        }
                    } catch (ExecutionException | InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Please enter a city name", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Apply system insets for better layout handling
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}
