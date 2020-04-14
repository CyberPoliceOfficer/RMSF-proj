package com.example.temperaturemonitor;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.DefaultValueFormatter;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.ViewPortHandler;

import org.jsoup.Jsoup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class StatsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);


        final RequestQueue queue = Volley.newRequestQueue(this);

        String serial_aux = "xxx";

        if (getIntent().hasExtra("serial")) {
            serial_aux = getIntent().getExtras().getString("serial");
        } else {
            //crash:(
        }

        final String serial = serial_aux;

        final BarChart barChart = findViewById(R.id.barchart);

        barChart.setDrawBarShadow(false);
        barChart.setDrawValueAboveBar(true);
        barChart.setMaxVisibleValueCount(50);
        barChart.setPinchZoom(false);
        barChart.setDrawGridBackground(true);
        barChart.getDescription().setEnabled(false);
        barChart.getLegend().setEnabled(false);


        final ArrayList<BarEntry> entries = new ArrayList<BarEntry>();

        for (int i = 1; i <= 31; i++) {
            entries.add(new BarEntry(i, 0.01f));
        }

        YAxis leftAxis = barChart.getAxisLeft();
        YAxis rightAxis = barChart.getAxisRight();
        leftAxis.setAxisMaximum(50.0f);
        leftAxis.setAxisMinimum(0.0f);
        rightAxis.setAxisMaximum(50.0f);
        rightAxis.setAxisMinimum(0.0f);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setDrawGridLines(true);
        xAxis.setLabelCount(31);
        xAxis.setTextSize(7f);


        try {

            String url = "http://web.tecnico.ulisboa.pt/ist187028/Get_Last_month.php";

            StringRequest getRequest = new StringRequest(Request.Method.POST, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            // response
                            String serverResponse = Jsoup.parse(response).text();


                            String[] parts = serverResponse.split("\n");


                            for (String part : parts) {

                                String[] smallerParts = part.split(" ");

                                if(smallerParts.length != 2) continue;

                                int month = Integer.valueOf(smallerParts[0]);
                                float value = Float.valueOf(smallerParts[1]);
                                BarEntry extra = new BarEntry(month, 0.01f);
                                if(entries.contains(extra)) entries.remove(extra);
                                entries.add(new BarEntry(month, value));
                            }


                            BarDataSet barDataSet = new BarDataSet(entries, "Temperature Spikes");
                            barDataSet.setColors(ColorTemplate.COLORFUL_COLORS);

                            barDataSet.setValueFormatter(new ValueFormatter() {
                                @Override
                                public String getFormattedValue(float value) {
                                    if(value > 0.05f) return super.getFormattedValue(value);
                                    else return " ";
                                }
                            });

                            BarData data = new BarData(barDataSet);


                            barChart.setData(data);

                            barChart.invalidate();


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
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<String, String>();

                    params.put("serial", serial);

                    return params;
                }
            };

            queue.add(getRequest);


        } catch (Exception e) {
            //feed1.setImageBitmap();
        }


    }


}
