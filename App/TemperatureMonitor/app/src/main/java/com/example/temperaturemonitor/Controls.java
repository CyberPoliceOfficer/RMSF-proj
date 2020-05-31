package com.example.temperaturemonitor;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.textclassifier.TextLinks;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Base64;


public class Controls extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controls);

        boolean isAdmin = false;

        String serial_aux = "xxx";

        final int[] id = {0};

        final int minFanTemp = 30;
        final int maxFanTemp = 100;
        final int minRelayTemp = 50;
        final int maxRelayTemp = 125;

        final SeekBar tempForFanBar = findViewById(R.id.tempForFan);
        final SeekBar tempForRelayBar = findViewById(R.id.tempForRelay);
        final Switch relaySwitch = findViewById(R.id.relaySwitch);
        final TextView temperatureView = findViewById(R.id.temp);
        final TextView fanRPMView = findViewById(R.id.fanRPM);

        final TextView tempForFanValue = findViewById(R.id.tempFan);
        final TextView tempForRelayValue = findViewById(R.id.tempRelay);

        final Button feedButton = findViewById(R.id.feedButton);
        final Button histButton = findViewById(R.id.histButton);

        final RequestQueue queue = Volley.newRequestQueue(this);

        //Obter infomacao se é administrador e o numero de serie
        if(getIntent().hasExtra("isAdmin") && getIntent().hasExtra("serial")) {
            isAdmin = getIntent().getExtras().getBoolean("isAdmin");
            serial_aux = getIntent().getExtras().getString("serial");
        }
        else {
            //crash
        }

        final String serial = serial_aux;



        //Obter o estado atual dos controlos
        try {

            String url = "http://web.tecnico.ulisboa.pt/ist187028/Get_Thresholds_&_Activations.php";


            StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                    new Response.Listener<String>()
                    {
                        @Override
                        public void onResponse(String response) {
                            // response
                            String serverResponse = Jsoup.parse(response).text();

                            String[] parts = serverResponse.split(" ");

                            int aux = Integer.valueOf(parts[1]);
                            if(aux > 0) {
                                relaySwitch.setChecked(true);
                            }
                            else {
                                relaySwitch.setChecked(false);
                            }

                            tempForFanValue.setText(parts[2]);
                            tempForFanBar.setProgress((Integer.valueOf(parts[2]) - minFanTemp)*100/(maxFanTemp - minFanTemp));
                            tempForRelayValue.setText(parts[3]);
                            tempForRelayBar.setProgress((Integer.valueOf(parts[3]) - minRelayTemp)*100/(maxRelayTemp - minRelayTemp));


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


            queue.add(postRequest);


        }
        catch (Exception e){
            tempForFanValue.setText(R.string.wrong);
            tempForRelayValue.setText(R.string.wrong);
            relaySwitch.setText(R.string.wrong);
        }


        //Impedir certos controlos de nao é administrador
        if(!isAdmin) {
            tempForFanBar.setEnabled(false);
            tempForRelayBar.setEnabled(false);
            relaySwitch.setEnabled(false);
        }


        //Caso hajam alteracoes na barra, enviar atualizacao à base
        tempForFanBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tempForFanValue.setText(String.valueOf(Math.round(minFanTemp + (maxFanTemp-minFanTemp)*progress*0.01)));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                final int setTemp = (int) Math.round(minFanTemp + (maxFanTemp-minFanTemp)*seekBar.getProgress()*0.01);

                Response.Listener<JSONObject> listener = new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                    }
                };

                JSONObject jsonObject = new JSONObject();

                byte[] bytes = {'f', Byte.parseByte(String.valueOf(setTemp))};

                String encoded = Base64.getEncoder().encodeToString(bytes);

                try {
                    jsonObject.put("dev_id","arduino1");
                    jsonObject.put("payload_raw", encoded);
                    jsonObject.put("confirmed", true);
                    jsonObject.put("schedule", "last");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                String jsonURL = "https://integrations.thethingsnetwork.org/ttn-eu/api/v2/down/rmsf-project/12_xy?key=ttn-account-v2.sE4IzbkqRlnNKIiWOdXE3TumLg-7oU2NxysI7od2KzI";

                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,jsonURL, jsonObject, listener, null);

                queue.add(jsonObjectRequest);



                try {

                    String url = "http://web.tecnico.ulisboa.pt/ist187028/Set_Thresholds_1.php";

                    StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                            new Response.Listener<String>()
                            {
                                @Override
                                public void onResponse(String response) {
                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    // error
                                    tempForFanValue.setText(error.getMessage());

                                }
                            }
                    ) {
                        @Override
                        protected Map<String, String> getParams()
                        {
                            Map <String, String>  params = new HashMap<String, String>();

                            params.put("t1", String.valueOf(setTemp));
                            params.put("serial", serial);

                            return params;
                        }
                    };
                    queue.add(postRequest);
                }
                catch (Exception e){
                    tempForFanValue.setText(R.string.wrong);
                }

            }
        });

        //Caso hajam alteracoes na barra, enviar atualizacao à base
        tempForRelayBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tempForRelayValue.setText(String.valueOf(Math.round(minRelayTemp + (maxRelayTemp-minRelayTemp)*progress*0.01)));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                final int setTemp = (int) Math.round(minRelayTemp + (maxRelayTemp-minRelayTemp)*seekBar.getProgress()*0.01);

                Response.Listener<JSONObject> listener = new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                    }
                };

                JSONObject jsonObject = new JSONObject();

                byte[] bytes = {'r', Byte.parseByte(String.valueOf(setTemp))};

                String encoded = Base64.getEncoder().encodeToString(bytes);

                try {
                    jsonObject.put("dev_id","arduino1");
                    jsonObject.put("payload_raw", encoded);
                    jsonObject.put("confirmed", true);
                    jsonObject.put("schedule", "last");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                String jsonURL = "https://integrations.thethingsnetwork.org/ttn-eu/api/v2/down/rmsf-project/12_xy?key=ttn-account-v2.sE4IzbkqRlnNKIiWOdXE3TumLg-7oU2NxysI7od2KzI";

                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,jsonURL, jsonObject, listener, null);

                queue.add(jsonObjectRequest);

                try {

                    String url = "http://web.tecnico.ulisboa.pt/ist187028/Set_Thresholds_2.php";

                    StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                            new Response.Listener<String>()
                            {
                                @Override
                                public void onResponse(String response) {
                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    // error
                                    tempForRelayValue.setText(error.getMessage());

                                }
                            }
                    ) {
                        @Override
                        protected Map<String, String> getParams()
                        {
                            Map <String, String>  params = new HashMap<String, String>();

                            params.put("t2", String.valueOf(setTemp));
                            params.put("serial", serial);

                            return params;
                        }
                    };

                    queue.add(postRequest);
                }
                catch (Exception e){
                    tempForRelayValue.setText(R.string.wrong);
                }
            }
        });

        //Caso hajam alteracoes na switch, enviar atualizacao à base
        relaySwitch.setOnClickListener(new View.OnClickListener() {

            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {

                final boolean isCheckedaux = ((CompoundButton) v).isChecked();


                Response.Listener<JSONObject> listener = new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                    }
                };

                JSONObject jsonObject = new JSONObject();

                byte send = 0;

                if(isCheckedaux) {
                    send = 1;
                }

                byte[] bytes = {'p', send};

                String encoded = Base64.getEncoder().encodeToString(bytes);

                try {
                    jsonObject.put("dev_id","arduino1");
                    jsonObject.put("payload_raw", encoded);
                    jsonObject.put("confirmed", true);
                    jsonObject.put("schedule", "last");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                String jsonURL = "https://integrations.thethingsnetwork.org/ttn-eu/api/v2/down/rmsf-project/12_xy?key=ttn-account-v2.sE4IzbkqRlnNKIiWOdXE3TumLg-7oU2NxysI7od2KzI";

                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,jsonURL, jsonObject, listener, null);

                queue.add(jsonObjectRequest);


                try {

                    String url = "http://web.tecnico.ulisboa.pt/ist187028/Set_Activations.php";

                    StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                            new Response.Listener<String>()
                            {
                                @Override
                                public void onResponse(String response) {
                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    // error
                                    //relaySwitch.setText(error.getMessage());

                                }
                            }
                    ) {
                        @Override
                        protected Map<String, String> getParams()
                        {
                            Map <String, String>  params = new HashMap<String, String>();

                            params.put("serial", serial);

                            if(isCheckedaux) {
                                params.put("is_on", "1");
                            }
                            else {
                                params.put("is_on", "0");
                            }

                            return params;
                        }
                    };

                    queue.add(postRequest);
                }
                catch (Exception e){
                    //relaySwitch.setText(R.string.wrong);
                }
            }
        });


        //A cada meio segundo, obter medições atuais da base de dados

        final Handler handler = new Handler();

        Runnable runnableCode = new Runnable() {
            @Override
            public void run() {

                try {

                    String url = "http://web.tecnico.ulisboa.pt/ist187028/Get_Measurements.php";

                    StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                            new Response.Listener<String>()
                            {
                                @Override
                                public void onResponse(String response) {
                                    // response
                                    String serverResponse = Jsoup.parse(response).text();

                                    String[] parts = serverResponse.split(" ");
                                    temperatureView.setText(parts[1]);
                                    fanRPMView.setText(parts[2]);

                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    // error
                                    relaySwitch.setText(error.getMessage());

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

                    queue.add(postRequest);
                }
                catch (Exception e){
                    relaySwitch.setText(R.string.wrong);
                }


                handler.postDelayed(this, 500);
            }
        };

       handler.post(runnableCode);


       //Configurar botoes para passar para as proximas atividades

       feedButton.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {

               Intent next = new Intent(getApplicationContext(), LiveFeed.class);
               next.putExtra("serial", serial);
               startActivity(next);
           }
       });

       histButton.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               Intent next = new Intent(getApplicationContext(), StatsActivity.class);
               next.putExtra("serial", serial);
               startActivity(next);
           }
       });

    }
}
