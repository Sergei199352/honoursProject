package com.example.ReMemoryHons;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EdgeEffect;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ReMemoryHons.faceRecon.FaceClassifier;
import com.example.ReMemoryHons.faceRecon.FaceRecognition;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class registFaceActivity extends AppCompatActivity {

    EditText editText;
    RecyclerView recyclerView;
    FaceClassifier faceClassifier;

    registerFaceAdapter adapter;





    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_face_layout);
        recyclerView = findViewById(R.id.recyclerView);

        // Getting the JSON string from the intent extras
        String json = getIntent().getStringExtra("image_list_json");

        // Deserializing JSON string back to bitmap list
        Gson gson = new Gson();
        Type type = new TypeToken<List<Bitmap>>() {}.getType();
        List<Bitmap> bitmaps = gson.fromJson(json, type);

        try {
            faceClassifier = FaceRecognition.create(getAssets(), "facenet.tflite", 160, false);
        } catch (IOException e) {
            e.printStackTrace();
        }


        // adapter
        adapter = new registerFaceAdapter(this, bitmaps);
        recyclerView.setAdapter(adapter);

        // layout manager
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        Button nav = findViewById(R.id.navButtonRegis);
        nav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(registFaceActivity.this,MainActivity.class));
            }
        });




    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }


    private void confirmed(){
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.confirmed_layout);

        TextView alertMessageTextView = dialog.findViewById(R.id.alertMessage);
        Button dismissButton = dialog.findViewById(R.id.dismissButton);

        alertMessageTextView.setText("New Face Registered"); // Set your message here


        dismissButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                Intent intent = new Intent(registFaceActivity.this, MainActivity.class);
                startActivity(intent);


            }
        });
        dialog.show();



    }


}
