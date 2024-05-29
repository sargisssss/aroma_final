package com.candle.aroma;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.aroma.R;

public class BlogActivity2 extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blog2);
    }

    public void back(View view) {
        Intent intent = new Intent(BlogActivity2.this, HomeActivity.class);
        startActivity(intent);
    }
}