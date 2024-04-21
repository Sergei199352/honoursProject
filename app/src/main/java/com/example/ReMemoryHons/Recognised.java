package com.example.ReMemoryHons;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ReMemoryHons.faceRecon.FaceClassifier;
import com.example.ReMemoryHons.faceRecon.FaceRecognition;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;


public class Recognised extends AppCompatActivity {
    FaceClassifier faceClassifier;
    RecyclerView recyclerView;
    reconRecyclerAdapter adapter;






    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recognition_layout);
        // Deserializing JSON string back to List<Bitmap>
        String json = getIntent().getStringExtra("image_list_json");
        Gson gson = new Gson();
        Type type = new TypeToken<List<Bitmap>>() {}.getType();
        List<Bitmap> bitmaps = gson.fromJson(json, type);
        recyclerView = findViewById(R.id.recoRecycler);
        Button nav = findViewById(R.id.navButtonRegis2);
        nav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Recognised.this,MainActivity.class));
            }
        });





        try {
            faceClassifier = FaceRecognition.create(getAssets(), "facenet.tflite", 160, false);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // adapter
        adapter = new reconRecyclerAdapter(bitmaps,this);
        recyclerView.setAdapter(adapter);

        //layout manager
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);













    }
}
