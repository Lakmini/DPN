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
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.Utils.Constants;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class ImageUploadActivity extends AppCompatActivity {

    private static final int SELECT_PICTURE = 100;
    private static final String TAG = "imageUploader";
    Button image_upload_btn ;
    Button severity_predict_btn;
    Button retrieve_image_btn;
    Button segment_btn;
    String picturePath ;
    String base64_string;
    private TextView result_view;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_upload);


        Bundle b = getIntent().getExtras();
        int value = -1;
        if(b != null)
            value = b.getInt("key");

        image_upload_btn = findViewById(R.id.img_upload_btn);
        severity_predict_btn = findViewById(R.id.severity_predict_btn);
        retrieve_image_btn = findViewById(R.id.retrieve_img_btn);
        result_view = findViewById(R.id.severity_result_view);
        segment_btn = findViewById(R.id.segment_img_btn);
        if(value == Constants.DR_IMAGE_UPLOAD_PAGE_INTENT_KEY)
        {
            setTitle("DR");
            segment_btn.setVisibility(View.GONE);
        }
        if(value == Constants.DFU_IMAGE_UPLOAD_PAGE_INTENT_KEY)
        {
            setTitle("DFU");
            retrieve_image_btn.setVisibility(View.GONE);
        }

        image_upload_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImageChooser();
            }
        });

        final int finalValue = value;
        severity_predict_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                upload(finalValue);
            }
        });

        retrieve_image_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                parseData(DRImageRetrieveActivity.class);
            }
        });

        segment_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                parseData(DFUSegmentActivity.class);
            }
        });

        handlePermission();

    }

    /* upload image code begin */

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
                            findViewById(R.id.imageView).post(new Runnable() {
                                @Override
                                public void run() {
                                    ((ImageView) findViewById(R.id.imageView)).setImageURI(selectedImageUri);
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
                        openAppSettings(ImageUploadActivity.this);
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

    /* upload image code end*/

    private void upload(int value) {

        if(picturePath == null)
        {
            AlertDialog alertDialog = new AlertDialog.Builder(ImageUploadActivity.this).create();
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
        convertToBase64();
        Log.i("Base 64 STRING",base64_string);
        // Upload image to server
        new uploadToServer(value).execute();

    }

    private void convertToBase64()
    {

        // Image
        Bitmap bm = BitmapFactory.decodeFile(picturePath);
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 90, bao);
        byte[] ba = bao.toByteArray();

        base64_string = Base64.encodeToString(ba, Base64.DEFAULT);

    }

    public class uploadToServer extends AsyncTask<Void, Void, String> {

        int value;
        String path;
        public uploadToServer(int val)
        {
            value = val;

        }

        private ProgressDialog pd = new ProgressDialog(ImageUploadActivity.this);
        protected void onPreExecute() {
            super.onPreExecute();
            pd.setMessage("Wait image uploading!");
            pd.show();
        }

        @Override
        protected String doInBackground(Void... params) {
            String response = "";
            try {
                URL url;
                if(value == Constants.DR_IMAGE_UPLOAD_PAGE_INTENT_KEY)
                {
                    url = new URL("http://192.168.1.4:5000/predictDRSeverityStage");
                }
                else
                {
                    //add dfu url here
                    url = new URL("http://192.168.1.4:5000/predictDRSeverityStage");
                }
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

    private void parseData(Class activity)
    {
        if(picturePath == null)
        {
            AlertDialog alertDialog = new AlertDialog.Builder(ImageUploadActivity.this).create();
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
        convertToBase64();
        Intent intent = new Intent(ImageUploadActivity.this, activity);
        Bundle b = new Bundle();
        b.putString("key", base64_string); //Your id
        intent.putExtras(b); //Put your id to your next Intent
        startActivity(intent);
    }



}
