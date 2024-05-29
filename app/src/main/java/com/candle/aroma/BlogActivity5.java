package com.candle.aroma;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.aroma.R;

public class BlogActivity5 extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blog5);
    }

    public void back(View view) {
        Intent intent = new Intent(BlogActivity5.this, HomeActivity.class);
        startActivity(intent);
    }
}