package com.example.Utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.diabassistant.R;

import java.util.ArrayList;

public class ImageAdapterGridView extends BaseAdapter {
    private Context mContext;
    ArrayList<Bitmap> imageIDs = new ArrayList<Bitmap>();
    Integer [] imgs;
    ArrayList<String> result;
    private static LayoutInflater inflater=null;
    public ImageAdapterGridView(Context c,  ArrayList<Bitmap> images,ArrayList<String> osNameList) {
        mContext = c;
        imageIDs = images;
        result = osNameList;
        inflater = ( LayoutInflater)c.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }




    public int getCount() {
        return imageIDs.size();
        //return imgs.length;
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view;

        if (convertView == null) {
            view  = new View(mContext);
            view = inflater.inflate(R.layout.gridview_layout, null);
            TextView textViewAndroid = (TextView)  view .findViewById(R.id.android_gridview_text);
            ImageView imageViewAndroid = (ImageView) view.findViewById(R.id.android_gridview_image);
            textViewAndroid.setText(result.get(position));
            imageViewAndroid.setImageBitmap(imageIDs.get(position));
        } else {
            view = (View) convertView;
        }
        return view;
    }
}

