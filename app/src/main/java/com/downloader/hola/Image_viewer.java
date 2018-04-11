package com.downloader.hola;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

public class Image_viewer extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);

        String image=getIntent().getStringExtra("image");
        ImageView imageView= (ImageView) findViewById(R.id.image_viewer);

        Picasso.with(this).load(image).into(imageView);
    }
}
