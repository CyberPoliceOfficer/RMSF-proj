package com.example.temperaturemonitor;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.jsoup.Jsoup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class LiveFeed extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_feed);

        final ImageView feed1 = findViewById(R.id.Feed1);

        final TextView minMax = findViewById(R.id.MinMax);


        final RequestQueue queue = Volley.newRequestQueue(this);

        String serial_aux = "xxx";

        if(getIntent().hasExtra("serial")) {
            serial_aux = getIntent().getExtras().getString("serial");
        }
        else {
            //crash:(
        }

        final String serial = serial_aux;

        /*

        final Handler handler = new Handler();

        Runnable runnableCode = new Runnable() {
            @Override
            public void run() {

                try {

                    String url = "http://web.tecnico.ulisboa.pt/ist187028/Get_Image.php";

                    StringRequest getRequest = new StringRequest(Request.Method.POST, url,
                            new Response.Listener<String>()
                            {
                                @Override
                                public void onResponse(String response) {
                                    // response
                                    String serverResponse = Jsoup.parse(response).text();

                                    String fancyServerResponse = serverResponse.substring(1, serverResponse.length() - 2);

                                    String[] parts = fancyServerResponse.split(",");



                                    float[][] floats = new float[32][24];

                                    int counter1 = 0;
                                    int counter2 = 0;


                                    for(String part: parts) {

                                        floats[counter1][counter2] = Float.valueOf(part);
                                        if(counter2 == 23) {
                                            counter2 = -1;
                                            counter1++;
                                        }
                                        counter2++;

                                    }



                                    float max = floats[0][0];
                                    float min = floats[0][0];

                                    for (int i = 0; i < 32; i++) {
                                        for(int j = 0; j < 24; j++) {
                                            if(floats[i][j] > max) max = floats[i][j];
                                            if(floats[i][j] < min) min = floats[i][j];
                                        }
                                    }

                                    String mM = "Min: " + min + "ºC   Max: " + max + "ºC";

                                    minMax.setText(mM);


                                    Bitmap operation = Bitmap.createBitmap(24,32, Bitmap.Config.ARGB_8888);

                                    for(int i = 0; i < 24; i++){
                                        for(int j = 0; j < 32; j++){

                                            float red = ((floats[j][i] - min)*255)/(max - min);
                                            int r = (int)red;
                                            int g = 0;
                                            int b = 50;

                                            operation.setPixel(i, j, Color.argb(255, r, g, b));
                                        }
                                    }

                                    feed1.setImageBitmap(operation);


                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    // error

                                }
                            }
                    ) {
                        @Override
                        protected Map<String, String> getParams()
                        {
                            Map <String, String>  params = new HashMap<String, String>();

                            params.put("serial", serial);

                            return params;
                        }
                    };

                    queue.add(getRequest);


                }
                catch (Exception e){
                    //feed1.setImageBitmap();
                }

                handler.postDelayed(this, 2000);
            }
        };

        handler.post(runnableCode);*/






        final Handler handler = new Handler();

        minMax.setVisibility(View.INVISIBLE);

        Runnable runnableCode = new Runnable() {
            @Override
            public void run() {

                try {

                    String url = "http://web.tecnico.ulisboa.pt/ist187028/Get_Hotspots.php";

                    StringRequest getRequest = new StringRequest(Request.Method.POST, url,
                            new Response.Listener<String>()
                            {
                                @Override
                                public void onResponse(String response) {
                                    // response
                                    String serverResponse = Jsoup.parse(response).text();

                                    String fancyServerResponse = serverResponse.substring(1, serverResponse.length() - 2);

                                    String[] parts = fancyServerResponse.split(",");


                                    for(int i = 0; i < 9; i = i + 3) {
                                        float x = Float.parseFloat(parts[i]);
                                        float y = Float.parseFloat(parts[i+1]);
                                        float r = Float.parseFloat(parts[i+2]);

                                        if(x < 0 || y < 0 || r < 0) {
                                            continue;
                                        }

                                        Log.d("fun", String.valueOf(x));
                                        Log.d("fun", String.valueOf(y));
                                        Log.d("fun", String.valueOf(r));

                                        Bitmap operation = Bitmap.createBitmap(24,32, Bitmap.Config.ARGB_8888);

                                        for(int w = 0; w < 24; w++){
                                            for(int j = 0; j < 32; j++){

                                                if( (x - w)*(x - w) + (y - j)*(y - j) < r*r) {
                                                    operation.setPixel(w, j, Color.argb(255, 255, 0, 0));
                                                }
                                                else {
                                                    operation.setPixel(w, j, Color.argb(255, 0, 0, 255));
                                                }

                                            }
                                        }

                                        feed1.setImageBitmap(operation);

                                    }



                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    // error

                                }
                            }
                    ) {
                        @Override
                        protected Map<String, String> getParams()
                        {
                            Map <String, String>  params = new HashMap<String, String>();

                            params.put("serial", serial);

                            return params;
                        }
                    };

                    queue.add(getRequest);


                }
                catch (Exception e){
                    //feed1.setImageBitmap();
                }

                handler.postDelayed(this, 2000);
            }
        };

        handler.post(runnableCode);

    }
}
