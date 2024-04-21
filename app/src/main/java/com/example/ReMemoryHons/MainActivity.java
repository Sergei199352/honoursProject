package com.example.ReMemoryHons;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.widget.Button;

import com.example.ReMemoryHons.faceRecon.FaceClassifier;

import java.util.HashMap;


public class MainActivity extends AppCompatActivity {



    public static HashMap<String ,Pair<String, FaceClassifier.Classification>> registered = new HashMap<>();




    Button registerBtn,recognizeBtn, realTime;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);




        registerBtn = findViewById(R.id.buttonregister);
        recognizeBtn = findViewById(R.id.buttonrecognize);

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this,RegisterActivity.class));
            }
        });

        recognizeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this,RecognitionActivity.class));
            }

        });

    }
}