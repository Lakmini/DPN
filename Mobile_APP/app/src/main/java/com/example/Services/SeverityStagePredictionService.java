package com.example.Services;

public class SeverityStagePredictionService {

//    String encodedString;
//    Activity activity;
//
//    public SeverityStagePredictionService(String estring , Activity a)
//    {
//        encodedString = estring;
//        activity = a;
//    }
//
//    private ProgressDialog pd = new ProgressDialog(DRActivity.this);
//    protected void onPreExecute() {
//        super.onPreExecute();
//        pd.setMessage("Wait image uploading!");
//        pd.show();
//    }
//
//    @Override
//    protected String doInBackground(Void... params) {
//
//        ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
//        nameValuePairs.add(new BasicNameValuePair("base64", encodedString));
//        nameValuePairs.add(new BasicNameValuePair("ImageName", System.currentTimeMillis() + ".jpg"));
//        try {
//            HttpClient httpclient = new DefaultHttpClient();
//            HttpPost httppost = new HttpPost(URL);
//            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
//            HttpResponse response = httpclient.execute(httppost);
//            String st = EntityUtils.toString(response.getEntity());
//            Log.v("log_tag", "In the try Loop" + st);
//
//        } catch (Exception e) {
//            Log.v("log_tag", "Error in http connection " + e.toString());
//        }
//        return "Success";
//
//    }
//
//    protected void onPostExecute(String result) {
//        super.onPostExecute(result);
//        pd.hide();
//        pd.dismiss();
//    }
}
