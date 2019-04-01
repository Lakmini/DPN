package com.example.diabassistant;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

public class DRActivity extends AppCompatActivity {

    private static final int SELECT_PICTURE = 100;
    private static final String TAG = "DRActivity";
    private Button upload_btn;
    private Button predict_btn;
    private Button retrieve_btn;
    private TextView file_name_view;
    private TextView result_view;
    GridView androidGridView;
    String picturePath ;
    String base64_string;
    ArrayList<Bitmap> images = new ArrayList<Bitmap>();

            Integer[] imageIDs = {
            R.drawable.logo, R.drawable.logo, R.drawable.logo,
            R.drawable.logo, R.drawable.logo, R.drawable.logo,
            R.drawable.logo, R.drawable.logo, R.drawable.logo,
            R.drawable.logo,
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dr);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("DR");

        upload_btn = findViewById(R.id.btn);
        upload_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImageChooser();
            }
        });

        predict_btn = findViewById(R.id.btn_predict);

//        predict_btn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                upload();
//            }
//        });

        retrieve_btn = findViewById(R.id.btn_retrieve);
//        retrieve_btn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                retrieveSimilarImages();
//            }
//        });

        file_name_view = findViewById(R.id.file_name);

        androidGridView = (GridView) findViewById(R.id.grid_view);
      //  androidGridView.setAdapter(new ImageAdapterGridView(this, imageIDs ));

        result_view = findViewById(R.id.result);
        
        handlePermission();
    }

    private void retrieveSimilarImages()
    {
        convertToBase64();
        new retrieveFromServer().execute();
    }
    private void handlePermission() {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return;
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            //ask for permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    SELECT_PICTURE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case SELECT_PICTURE:
                for (int i = 0; i < permissions.length; i++) {
                    String permission = permissions[i];
                    if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                        boolean showRationale = ActivityCompat.shouldShowRequestPermissionRationale(this, permission);
                        if (showRationale) {
                            //  Show your own message here
                        } else {
                            showSettingsAlert();
                        }
                    }
                }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    /* Choose an image from Gallery */
    void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE);
    }

    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                if (resultCode == RESULT_OK) {
                    if (requestCode == SELECT_PICTURE) {
                        // Get the url from data
                        final Uri selectedImageUri = data.getData();
                        if (null != selectedImageUri) {
                            // Get the path from the Uri
                            String path = getPathFromURI(selectedImageUri);
                            //file_name_view.setText(path);
                            picturePath = path;
                            // Set the image in ImageView
                            findViewById(R.id.imgView).post(new Runnable() {
                                @Override
                                public void run() {
                                    ((ImageView) findViewById(R.id.imgView)).setImageURI(selectedImageUri);
                                }
                            });

                        }
                    }
                }
            }
        }).start();

    }

    /* Get the real path from the URI */
    public String getPathFromURI(Uri contentUri) {

        Cursor cursor = getContentResolver().query(contentUri, null, null, null, null);
        cursor.moveToFirst();
        String document_id = cursor.getString(0);
        document_id = document_id.substring(document_id.lastIndexOf(":") + 1);
        cursor.close();

        cursor = getContentResolver().query(
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                null, MediaStore.Images.Media._ID + " = ? ", new String[]{document_id}, null);
        cursor.moveToFirst();
        String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
        cursor.close();

        return path;
    }

    private void upload() {
        convertToBase64();

        Log.i("Base 64 STRING",base64_string);
        // Upload image to server
        new uploadToServer().execute();

    }

    private void convertToBase64()
    {
        // Image location URL
        Log.e("path", "----------------" + picturePath);
        if(picturePath == null)
        {
            AlertDialog alertDialog = new AlertDialog.Builder(DRActivity.this).create();
            alertDialog.setTitle("Error");
            alertDialog.setMessage("Please Upload an Image");
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            alertDialog.show();
            return;
        }
        // Image
        Bitmap bm = BitmapFactory.decodeFile(picturePath);
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 90, bao);
        byte[] ba = bao.toByteArray();

        base64_string = Base64.encodeToString(ba, Base64.DEFAULT);

    }


    private void showSettingsAlert() {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Alert");
        alertDialog.setMessage("App needs to access the Camera.");
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "DONT ALLOW",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        //finish();
                    }
                });
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "SETTINGS",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        openAppSettings(DRActivity.this);
                    }
                });
        alertDialog.show();
    }

    public static void openAppSettings(final Activity context) {
        if (context == null) {
            return;
        }
        final Intent i = new Intent();
        i.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        i.addCategory(Intent.CATEGORY_DEFAULT);
        i.setData(Uri.parse("package:" + context.getPackageName()));
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        context.startActivity(i);
    }
    public class uploadToServer extends AsyncTask<Void, Void, String> {

        private ProgressDialog pd = new ProgressDialog(DRActivity.this);
        protected void onPreExecute() {
            super.onPreExecute();
            pd.setMessage("Wait image uploading!");
            pd.show();
        }

        @Override
        protected String doInBackground(Void... params) {
            String response = "";
            try {
                URL url = new URL("http://192.168.1.6:5000/predictDRSeverityStage");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                connection.setRequestProperty("Accept","application/json");
                connection.setDoOutput(true);
                connection.setDoInput(true);

                JSONObject jsonParam = new JSONObject();
                jsonParam.put("query_base64", base64_string);

                Log.i("JSON", jsonParam.toString());
                DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
                outputStream.writeBytes(jsonParam.toString());

                outputStream.flush();
                outputStream.close();

                Log.i("STATUS", String.valueOf(connection.getResponseCode()));
                Log.i("MSG" , connection.getResponseMessage());

                int responseCode=connection.getResponseCode();

                if (responseCode == HttpsURLConnection.HTTP_OK) {
                    String line;
                    BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    while ((line=bufferedReader.readLine()) != null) {
                        response+=line;
                    }
                }
                else {
                    response="";

                }


                connection.disconnect();

                Log.i("RESPONSE BODY", response);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return response;

        }

        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            result_view.setText(result.replace("\"", ""));
            Log.i("RESULT",result);
            pd.hide();
            pd.dismiss();
        }
    }



    public class retrieveFromServer extends AsyncTask<Void, Void, String> {

        private ProgressDialog pd = new ProgressDialog(DRActivity.this);
        protected void onPreExecute() {
            super.onPreExecute();
            pd.setMessage("Waiting.......!");
            pd.show();
        }

        @Override
        protected String doInBackground(Void... params) {
            String response = "";
            try {
                URL url = new URL("http://192.168.1.6:5000/retrieveSimilarCases");
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
                //renderImageGrid(array);

            } catch (JSONException e) {
                e.printStackTrace();
            }

            Log.i("RESULT",result);
            pd.hide();
            pd.dismiss();
        }
    }




//    private void renderImageGrid(JSONArray imgarray)
//    {
//
//        Bitmap decodedImage;
//        images.clear();
//
//        for (int i=0; i<imgarray.length();i++)
//        {
//            try {
//                String str = imgarray.get(i).toString().replace("data:image/jpeg;base64,","");
//                Log.i("DECODEDSTRING", str);
//                byte []data = Base64.decode( str.getBytes(), 0);
//                decodedImage =  BitmapFactory.decodeByteArray(data, 0, data.length);
//                images.add(decodedImage);
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//
//
//        }
//
//        androidGridView.setAdapter(new ImageAdapterGridView(this, images));
//
//
//    }
}
