package com.example.mygmailclient;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Scanner;


public class MainActivity extends Activity {

    TextView info;

    ImageView imageProfile;
    TextView textViewName, textViewEmail, textViewGender, textViewBirthday;
    String textName, textEmail, textGender, textBirthday, userImageUrl;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        info = (TextView) findViewById(R.id.text_view_info);


        imageProfile = (ImageView) findViewById(R.id.imageView1);
        textViewName = (TextView) findViewById(R.id.textViewNameValue);
        textViewEmail = (TextView) findViewById(R.id.textViewEmailValue);
        textViewGender = (TextView) findViewById(R.id.textViewGenderValue);
        textViewBirthday = (TextView) findViewById(R.id.textViewBirthdayValue);

        Intent intent = getIntent();
        textEmail = intent.getStringExtra("email_id");
        System.out.println(textEmail);
        textViewEmail.setText(textEmail);
        try {
            System.out.println("On Home Page***"
                    + GetNameInForeground.GOOGLE_USER_DATA);
            JSONObject profileData = new JSONObject(
                    GetNameInForeground.GOOGLE_USER_DATA);

            if (profileData.has("picture")) {
                userImageUrl = profileData.getString("picture");
                new GetImageFromUrl().execute(userImageUrl);
            }
            if (profileData.has("name")) {
                textName = profileData.getString("name");
                textViewName.setText(textName);
            }
            if (profileData.has("gender")) {
                textGender = profileData.getString("gender");
                textViewGender.setText(textGender);
            }
            if (profileData.has("birthday")) {
                textBirthday = profileData.getString("birthday");
                textViewBirthday.setText(textBirthday);
            }

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public class GetImageFromUrl extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... urls) {
            Bitmap map = null;
            for (String url : urls) {
                map = downloadImage(url);
            }
            return map;
        }

        // Sets the Bitmap returned by doInBackground
        @Override
        protected void onPostExecute(Bitmap result) {
            imageProfile.setImageBitmap(result);
        }

        // Creates Bitmap from InputStream and returns it
        private Bitmap downloadImage(String url) {
            Bitmap bitmap = null;
            InputStream stream = null;
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inSampleSize = 1;

            try {
                stream = getHttpConnection(url);
                bitmap = BitmapFactory.decodeStream(stream, null, bmOptions);
                stream.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            return bitmap;
        }

        private InputStream getHttpConnection(String urlString)
                throws IOException {
            InputStream stream = null;
            URL url = new URL(urlString);
            URLConnection connection = url.openConnection();

            try {
                HttpURLConnection httpConnection = (HttpURLConnection) connection;
                httpConnection.setRequestMethod("GET");
                httpConnection.connect();

                if (httpConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    stream = httpConnection.getInputStream();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return stream;
        }


        public boolean onCreateOptionsMenu(Menu menu) {
            // Inflate the menu; this adds items to the action bar if it is present.
            getMenuInflater().inflate(R.menu.menu_main, menu);
            return true;
        }

        /*
            @Override
            public boolean onOptionsItemSelected(MenuItem item) {
                // Handle action bar item clicks here. The action bar will
                // automatically handle clicks on the Home/Up button, so long
                // as you specify a parent activity in AndroidManifest.xml.
                int id = item.getItemId();

                //noinspection SimplifiableIfStatement
                if (id == R.id.action_settings) {
                    return true;
                }

                return super.onOptionsItemSelected(item);
            }
        */
        MyTask task;

        public void doSomething(View v) {
            ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo info = connectivityManager.getActiveNetworkInfo();
            if (info != null && info.isConnected()) {
                System.out.print("\n Что-то происходит \n");
            } else {
                System.out.print("\n Что-то не то происходит \n");
            }
            task = new MyTask();
            task.execute();
        }

        class MyTask extends AsyncTask<Void, Void, String> {
            public MyTask() {
                super();
            }


            @Override
            protected String doInBackground(Void... params) {
                try {
                    URL url = new URL("http://maps.googleapis.com/maps/api/geocode/json?address=1600+Amphitheatre+Parkway,+Mountain+View,+CA&sensor=true");
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setConnectTimeout(10000);
                    connection.setReadTimeout(10000);
                    connection.setRequestMethod("POST");
                    connection.setDoInput(true);
                    connection.connect();
                    int response = connection.getResponseCode();
                    Log.d("i don`t know...", "The response is: " + response);
                    InputStream is = connection.getInputStream();
                    Scanner scanner = new Scanner(is);
                    String str = "";
                    str = scanner.nextLine();
                    while (scanner.hasNext()) {
                        str = str.concat(scanner.nextLine());

                    }
                    System.out.print("\n" + str + "\n");
                    JSONObject object = new JSONObject(str);
                    JSONArray names = object.names();
                    return str;
                } catch (java.io.IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return "notWorking";
            }

            @Override
            protected void onPostExecute(String str) {
                info.setText(str);

            }

        }
    }

}
