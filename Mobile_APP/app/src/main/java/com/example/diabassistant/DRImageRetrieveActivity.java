package com.example.diabassistant;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.widget.GridView;
import android.widget.ImageView;

import com.example.Utils.ImageAdapterGridView;

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

public class DRImageRetrieveActivity extends AppCompatActivity {

    ImageView sourceImageView;
    String base64_string;
    ArrayList<Bitmap> images = new ArrayList<Bitmap>();
    ArrayList<String> text = new ArrayList<String>();


//    Integer[] imageIDs = {
//            R.drawable.logo, R.drawable.logo, R.drawable.logo,
//            R.drawable.logo, R.drawable.logo, R.drawable.logo,
//            R.drawable.logo, R.drawable.logo, R.drawable.logo,
//            R.drawable.logo,
//    };
    GridView androidGridView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drimage_retrieve);

        Bundle b = getIntent().getExtras();
        base64_string = null; // or other values
        if(b != null)
            base64_string= b.getString("key");
        sourceImageView = findViewById(R.id.sourceImageView);

        if(base64_string!=null)
        {
            decodeImage(base64_string);
        }

        androidGridView = (GridView) findViewById(R.id.img_grid_view);
        //androidGridView.setAdapter(new ImageAdapterGridView(this, images, text ));

        new retrieveFromServer().execute();
    }

    private void decodeImage(String encodedString)
    {
        Bitmap decodedImage;
        byte []data = Base64.decode( encodedString.getBytes(), 0);
        decodedImage =  BitmapFactory.decodeByteArray(data, 0, data.length);
        sourceImageView.setImageBitmap(decodedImage);
    }

    public class retrieveFromServer extends AsyncTask<Void, Void, String> {

        private ProgressDialog pd = new ProgressDialog(DRImageRetrieveActivity.this);
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
                renderImageGrid(array);

            } catch (JSONException e) {
                e.printStackTrace();
            }

            Log.i("RESULT",result);
            pd.hide();
            pd.dismiss();
        }
    }




    private void renderImageGrid(JSONArray imgarray)
    {

        Bitmap decodedImage;
        images.clear();

        for (int i=0; i<imgarray.length();i++)
        {
            try {
                JSONObject obj = imgarray.getJSONObject(i);
                String str = obj.get("base64Format").toString().replace("data:image/jpeg;base64,","");
                Log.i("DECODEDSTRING", str);
                byte []data = Base64.decode( str.getBytes(), 0);
                decodedImage =  BitmapFactory.decodeByteArray(data, 0, data.length);
                images.add(decodedImage);

                String txt = obj.get("severityStage").toString();
                Log.i("SEVERITY STAGE",txt);
                text.add(txt);

            } catch (JSONException e) {
                e.printStackTrace();
            }


        }

        androidGridView.setAdapter(new ImageAdapterGridView(this, images,text));


    }
}
