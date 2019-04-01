package com.example.diabassistant;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

public class DFUSegmentActivity extends AppCompatActivity {

    String base64_string;
    ImageView original_image_view;
    ImageView segmented_imageView;
    ImageView region_imageView;
    ArrayList<Bitmap> images = new ArrayList<Bitmap>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dfusegment);

        Bundle b = getIntent().getExtras();
        base64_string = null; // or other values
        if(b != null)
            base64_string= b.getString("key");

        original_image_view = findViewById(R.id.original_imageView);
        segmented_imageView =findViewById(R.id.segmented_imageView);
        region_imageView = findViewById(R.id.region_imageView);
        if(base64_string!=null)
        {
            decodeImage(base64_string);
        }

        new retrieveSegmentedImageFromServer().execute();
    }
    private void decodeImage(String encodedString)
    {
        Bitmap decodedImage;
        byte []data = Base64.decode( encodedString.getBytes(), 0);
        decodedImage =  BitmapFactory.decodeByteArray(data, 0, data.length);
        original_image_view.setImageBitmap(decodedImage);
    }

    public class retrieveSegmentedImageFromServer extends AsyncTask<Void, Void, String> {

        private ProgressDialog pd = new ProgressDialog(DFUSegmentActivity.this);
        protected void onPreExecute() {
            super.onPreExecute();
            pd.setMessage("Waiting.......!");
            pd.show();
        }

        @Override
        protected String doInBackground(Void... params) {
            String response = "";
            try {
                URL url = new URL("http://192.168.1.4:5000/retrieveSimilarCases");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                conn.setRequestProperty("Accept","application/json");
                conn.setDoOutput(true);
                conn.setDoInput(true);

                JSONObject jsonParam = new JSONObject();
                jsonParam.put("query_base64", base64_string);



                Log.i("JSON", jsonParam.toString());
                DataOutputStream os = new DataOutputStream(conn.getOutputStream());
                os.writeBytes(jsonParam.toString());

                os.flush();
                os.close();

                Log.i("STATUS", String.valueOf(conn.getResponseCode()));
                Log.i("MSG" , conn.getResponseMessage());

                int responseCode=conn.getResponseCode();

                if (responseCode == HttpsURLConnection.HTTP_OK) {
                    String line;
                    BufferedReader br=new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    while ((line=br.readLine()) != null) {
                        response+=line;
                    }
                }
                else {
                    response="";

                }

                conn.disconnect();

                Log.i("RESPONSE BODY", response);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return response;

        }

        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            JSONArray array = null;
            try {
                array = new JSONArray(result);
                bindImages(array);

            } catch (JSONException e) {
                e.printStackTrace();
            }

            Log.i("RESULT",result);
            pd.hide();
            pd.dismiss();
        }
    }

    private void bindImages(JSONArray imgarray)
    {

        Bitmap decodedImage;
        images.clear();

        for (int i=0; i<imgarray.length();i++)
        {
            try {
                String str = imgarray.get(i).toString().replace("data:image/jpeg;base64,","");
                Log.i("DECODEDSTRING", str);
                byte []data = Base64.decode( str.getBytes(), 0);
                decodedImage =  BitmapFactory.decodeByteArray(data, 0, data.length);
                images.add(decodedImage);
            } catch (JSONException e) {
                e.printStackTrace();
            }


        }
        segmented_imageView.setImageBitmap(images.get(0));
        region_imageView.setImageBitmap(images.get(1));



    }



}
