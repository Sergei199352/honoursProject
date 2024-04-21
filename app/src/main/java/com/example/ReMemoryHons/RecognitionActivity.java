package com.example.ReMemoryHons;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;


import com.example.ReMemoryHons.faceRecon.FaceClassifier;
import com.example.ReMemoryHons.faceRecon.FaceRecognition;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import java.io.FileDescriptor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class RecognitionActivity extends AppCompatActivity {

    Button openGallery, openCamera;
    ImageView imageView;
    Uri imageUri;
    public static final int PERMISSION_CODE = 100;



    FaceDetectorOptions highAccuracyOpts =
            new FaceDetectorOptions.Builder()
                    .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                    .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
                    .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
                    .build();
    // initialization of the face detector
    FaceDetector detector;



    FaceClassifier faceClassifier;



    ActivityResultLauncher<Intent> galleryActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        imageUri = result.getData().getData();
                        Bitmap inputImage = uriToBitmap(imageUri);
                        Bitmap rotated = rotateBitmap(inputImage);
                        imageView.setImageBitmap(rotated);
                        faceDetectionMethod(rotated);
                    }
                }
            });


    ActivityResultLauncher<Intent> cameraActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Bitmap inputImage = uriToBitmap(imageUri);
                        Bitmap rotated = rotateBitmap(inputImage);
                        imageView.setImageBitmap(rotated);
                        imageView.setImageBitmap(rotated);
                        faceDetectionMethod(rotated);
                    }
                }
            });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recognition);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED || checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_DENIED){
                String[] permission = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
                requestPermissions(permission, PERMISSION_CODE);
            }
        }


        openGallery = findViewById(R.id.openGallery);
        openCamera = findViewById(R.id.cameraView);
        imageView = findViewById(R.id.imageView2);


        openGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                galleryActivityResultLauncher.launch(galleryIntent);
            }
        });


        openCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED || checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            == PackageManager.PERMISSION_DENIED){
                        String[] permission = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
                        requestPermissions(permission, PERMISSION_CODE);
                    }
                    else {
                        openCamera();
                    }
                }

                else {
                    openCamera();
                }
            }
        });


        detector = FaceDetection.getClient(highAccuracyOpts);



        try {
            faceClassifier = FaceRecognition.create(getAssets(), "facenet.tflite", 160, false);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    private void openCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Picture");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From the Camera");
        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        cameraActivityResultLauncher.launch(cameraIntent);
    }


    private Bitmap uriToBitmap(Uri selectedFileUri) {
        try {
            ParcelFileDescriptor parcelFileDescriptor =
                    getContentResolver().openFileDescriptor(selectedFileUri, "r");
            FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
            Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);

            parcelFileDescriptor.close();
            return image;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return  null;
    }


    @SuppressLint("Range")
    public Bitmap rotateBitmap(Bitmap input){
        String[] orientationColumn = {MediaStore.Images.Media.ORIENTATION};
        Cursor cur = getContentResolver().query(imageUri, orientationColumn, null, null, null);
        int orientation = -1;
        if (cur != null && cur.moveToFirst()) {
            orientation = cur.getInt(cur.getColumnIndex(orientationColumn[0]));
        }
        Log.d("tryOrientation",orientation+"");
        Matrix rotationMatrix = new Matrix();
        rotationMatrix.setRotate(orientation);
        Bitmap cropped = Bitmap.createBitmap(input,0,0, input.getWidth(), input.getHeight(), rotationMatrix, true);
        return cropped;
    }

    //TODO perform face detection
    public void faceDetectionMethod(Bitmap bitmap){

        // getting a mutable copy of the bitmap so we can draw the rectangle
        Bitmap mutBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);

    // hello


        InputImage image = InputImage.fromBitmap(bitmap, 0);
        Task<List<Face>> result =
                detector.process(image)
                        .addOnSuccessListener(
                                new OnSuccessListener<List<Face>>() {
                                    @Override
                                    public void onSuccess(List<Face> faces) {
                                        // Task completed successfully
                                        // ...
                                        Log.d("the face", "len = "+ faces.size());

                                        List<Bitmap> bitmaps = new ArrayList<>();
                                        for (Face face : faces) {
                                            Rect bounds = face.getBoundingBox();

                                            bitmaps.add(faceRecon(bounds, bitmap));



                                        }
                                        profileActivity(bitmaps);
                                        imageView.setImageBitmap(mutBitmap);
                                    }
                                })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Task failed with an exception
                                        // ...
                                    }
                                });
    }


    public Bitmap faceRecon (Rect bound, Bitmap bitmap){
        if (bound.top <0){

            bound.top = 0;
        }
        if (bound.left <0){
            bound.left = 0;
        }
        if (bound.right>bitmap.getWidth()){
            bound.right = bitmap.getWidth() - 1;
        }
        if (bound.bottom>bitmap.getHeight()){
            bound.bottom = bitmap.getHeight() - 1;
        }
        Bitmap faceCropped = Bitmap.createBitmap(bitmap, bound.left, bound.top, bound.width(), bound.height());
        //imageView.setImageBitmap(faceCropped);
        // adapting the face to the model input requirements
        faceCropped = Bitmap.createScaledBitmap(faceCropped, 160, 160, false);
        return faceCropped;



//        FaceClassifier.Classification recon = faceClassifier.classify(faceCropped, false);
//        if (recon != null){
//            Log.d("GET TITLE", recon.getTitle() +" gET DISTANCE " + recon.getDistance());
//            if (recon.getDistance() <1){
//
//
//
//            }
//            else{
//                confirmed("None of the registered faces match");
//            }
//        }else
//        {
//            confirmed("the given name is not registered");
//        }




    }
    private void confirmed(String string){
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.confirmed_layout);

        TextView alertMessageTextView = dialog.findViewById(R.id.alertMessage);
        Button dismissButton = dialog.findViewById(R.id.dismissButton);

        alertMessageTextView.setText(string); // Set your message here


        dismissButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();



            }
        });
        dialog.show();



    }

    public void profileActivity(List<Bitmap> list){

        Gson gson = new Gson();
        String json = gson.toJson(list);

        Intent intent = new Intent(this, Recognised.class);
        intent.putExtra("image_list_json", json);

        startActivity(intent);


    }





    @Override
    protected void onDestroy() {
        super.onDestroy();

    }
}