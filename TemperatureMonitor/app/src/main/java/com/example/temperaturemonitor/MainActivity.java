package com.example.temperaturemonitor;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.jsoup.Jsoup;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Instantiate the RequestQueue.
        final RequestQueue queue = Volley.newRequestQueue(this);


        final Button logInButton = findViewById(R.id.IButton);
        final TextView pass = findViewById(R.id.passText);
        final TextView name = findViewById(R.id.nameText);


        final Button continueButton = findViewById(R.id.continueButton);
        continueButton.setVisibility(View.INVISIBLE);

        final TextView result = findViewById(R.id.resultText);
        result.setVisibility(View.INVISIBLE);

        //Obter mail e password inserida, validar apartir da base e permitir acesso se adequado
        logInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String nameText = name.getText().toString();
                final String password = pass.getText().toString();

                try {

                    String url = "http://web.tecnico.ulisboa.pt/ist187028/Get_Users.php";

                    StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                            new Response.Listener<String>()
                            {
                                @Override
                                public void onResponse(String response) {
                                    // response

                                    String serverResponse = Jsoup.parse(response).text();

                                    if (serverResponse.equals("1")) {
                                        result.setText(R.string.result_ad);
                                        result.setVisibility(View.VISIBLE);
                                        continueButton.setVisibility(View.VISIBLE);
                                    } else if (serverResponse.equals("")) {
                                        result.setText(R.string.result_nor);
                                        result.setVisibility(View.VISIBLE);
                                        continueButton.setVisibility(View.VISIBLE);
                                    }
                                    else {
                                        result.setText(R.string.result_fail);
                                        result.setVisibility(View.VISIBLE);
                                        continueButton.setVisibility(View.INVISIBLE);
                                    }

                                }
                            },
                            new Response.ErrorListener()
                            {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    // error
                                    result.setText(error.getMessage());

                                }
                            }
                    ) {
                        @Override
                        protected Map<String, String> getParams()
                        {
                            Map <String, String>  params = new HashMap<String, String>();
                            params.put("email", nameText);
                            params.put("password", password);

                            return params;
                        }
                    };

                    queue.add(postRequest);


                }
                catch (Exception e){
                    result.setText(R.string.wrong);
                }




            }
        });

        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String resultText = result.getText().toString();
                boolean isAdmin = false;

                if(resultText.equals(getString(R.string.result_ad))) {
                    isAdmin = true;
                }

                Intent next = new Intent(getApplicationContext(), ListActivity.class);
                next.putExtra("isAdmin", isAdmin);

                startActivity(next);

            }
        });


    }
}
