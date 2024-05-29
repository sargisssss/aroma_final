package com.candle.aroma;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.aroma.R;

public class BlogActivity4 extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blog4);
    }

    public void back(View view) {
        Intent intent = new Intent(BlogActivity4.this, HomeActivity.class);
        startActivity(intent);
    }
}