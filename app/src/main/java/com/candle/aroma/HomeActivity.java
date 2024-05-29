package com.candle.aroma;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.aroma.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    DatabaseReference databaseReference;
    ValueEventListener eventListener;
    RecyclerView recyclerView;
    List<DataClass> dataList;
    MyAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);


        recyclerView = findViewById(R.id.recyclerView);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(HomeActivity.this, 3);
        recyclerView.setLayoutManager(gridLayoutManager);

        AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
        builder.setCancelable(false);
        builder.setView(R.layout.progress_layout);
        AlertDialog dialog = builder.create();
        dialog.show();

        dataList = new ArrayList<>();

        adapter = new MyAdapter(HomeActivity.this, dataList);
        recyclerView.setAdapter(adapter);

        databaseReference = FirebaseDatabase.getInstance().getReference("Products");
        dialog.show();

        eventListener = databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                dataList.clear();
                List<DataClass> tempList = new ArrayList<>();

                for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                    DataClass dataClass = itemSnapshot.getValue(DataClass.class);
                    dataClass.setKey(itemSnapshot.getKey());
                    tempList.add(dataClass);
                }

                Collections.reverse(tempList);

                for (int i = 0; i < Math.min(3, tempList.size()); i++) {
                    dataList.add(tempList.get(i));
                }

                adapter.notifyDataSetChanged();
                dialog.dismiss();
            }



            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                dialog.dismiss();
            }
        });
    }

    public void account(View view) {
        Intent intent = new Intent(HomeActivity.this, AccountActivity.class);
        startActivity(intent);
    }

    public void home(View view) {
        Intent intent = new Intent(HomeActivity.this, HomeActivity.class);
        startActivity(intent);
    }

    public void likedItems(View view) {
        Intent intent = new Intent(HomeActivity.this, LikedItemsActivity.class);
        startActivity(intent);
    }

    public void catalog(View view) {
        Intent intent = new Intent(HomeActivity.this, CatalogActivity.class);
        startActivity(intent);
    }

    public void cart(View view) {
        Intent intent = new Intent(HomeActivity.this, CartActivity.class);
        startActivity(intent);
    }

    public void blog1(View view) {
        Intent intent = new Intent(HomeActivity.this, BlogActivity1.class);
        startActivity(intent);
    }

    public void blog2(View view) {
        Intent intent = new Intent(HomeActivity.this, BlogActivity2.class);
        startActivity(intent);
    }

    public void blog3(View view) {
        Intent intent = new Intent(HomeActivity.this, BlogActivity3.class);
        startActivity(intent);
    }

    public void blog4(View view) {
        Intent intent = new Intent(HomeActivity.this, BlogActivity4.class);
        startActivity(intent);
    }

    public void blog5(View view) {
        Intent intent = new Intent(HomeActivity.this, BlogActivity5.class);
        startActivity(intent);
    }

}