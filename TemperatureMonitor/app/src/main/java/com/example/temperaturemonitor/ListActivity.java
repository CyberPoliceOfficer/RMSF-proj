package com.example.temperaturemonitor;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Property;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.jsoup.Jsoup;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class ListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        final ListView listView = findViewById(R.id.listView);


        final RequestQueue queue = Volley.newRequestQueue(this);

        boolean is_admin_aux = false;

        if (getIntent().hasExtra("isAdmin")) {
            is_admin_aux = getIntent().getExtras().getBoolean("isAdmin");
        } else {
            //crash:(
        }

        final boolean is_admin = is_admin_aux;

        final ArrayList<ListData> listInfo = new ArrayList<ListData>();


        final SpecialAdapter adapter = new SpecialAdapter(this, 0, listInfo, is_admin);

        try {

            String url = "http://web.tecnico.ulisboa.pt/ist187028/Get_Nodes.php";

            StringRequest getResquest = new StringRequest(Request.Method.GET, url,
                    new Response.Listener<String>()
                    {
                        @Override
                        public void onResponse(String response) {
                            // response
                            String serverResponse = Jsoup.parse(response).text();

                            String[] parts = serverResponse.split(" ");


                            for(int i = 0; i < parts.length - 1; i = i + 2) {
                                listInfo.add(new ListData(parts[i], parts[i+1]));
                            }

                            listView.setAdapter(adapter);

                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            // error

                        }
                    }
            );

            queue.add(getResquest);


        }
        catch (Exception e){

        }


        //listInfo.add(new ListData("xy123cx", "Bucelas"));




    }


    public class ListData {
        String serial;
        String location;

        public ListData(String serial, String location) {
            this.serial = serial;
            this.location = location;
        }
        public String getSerial() {return serial;}
        public String getLocation() {return location;}
    }

    public class SpecialAdapter extends ArrayAdapter<ListData> {

        private Context context;
        private ArrayList<ListData> Alldata;
        private boolean isAdmin;

        //constructor, call on creation
        public SpecialAdapter(Context context, int resource, ArrayList<ListData> objects, boolean isAdmin) {
            super(context, resource, objects);

            this.context = context;
            this.Alldata = objects;
            this.isAdmin = isAdmin;

        }

        //called when rendering the list
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            //get the property we are displaying
            final ListData data = Alldata.get(position);

            //get the inflater and inflate the XML layout for each item
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.list_layout, null);

            TextView textView = view.findViewById(R.id.listText);
            Button button = view.findViewById(R.id.listButton);

            String fullText = " Serial: " + data.getSerial() + "       Localization: " + data.getLocation();

            textView.setText(fullText);
            //textView.setTextSize(10);

            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent next = new Intent(getApplicationContext(), Controls.class);
                    next.putExtra("isAdmin", isAdmin);
                    next.putExtra("serial", data.getSerial());

                    startActivity(next);

                }
            });

            return view;
        }

    }


}


