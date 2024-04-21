package com.example.ReMemoryHons;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ReMemoryHons.faceRecon.FaceClassifier;
import com.example.ReMemoryHons.faceRecon.FaceRecognition;

import java.io.IOException;
import java.util.List;

public class reconRecyclerAdapter extends RecyclerView.Adapter<reconRecyclerAdapter.ReconViewHolder> {


    private List<Bitmap> list;

    public reconRecyclerAdapter(List<Bitmap> list, Context context) {
        this.list = list;
        this.context = context;
    }

    FaceClassifier faceClassifier;
    Bitmap image;

    private Context context;

    @NonNull
    @Override
    public ReconViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        // initialiszing the classifier
        try {
            faceClassifier = FaceRecognition.create(context.getAssets(), "facenet.tflite", 160, false);
        } catch (IOException e) {
            e.printStackTrace();
        }

        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.card_recognised_item, parent, false);
        return new ReconViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReconViewHolder holder, int position) {
        image = list.get(position);
        holder.imageView.setImageBitmap(image);
        FaceClassifier.Classification recon = faceClassifier.classify(image,false);
        if (recon != null){

            Log.d("Title", recon.getTitle() + " Distance "+ recon.getDistance() );
            //Log.e("Attached Strings",recon.getMemory());
            if (recon.getDistance() < 1){
                holder.textView.setText(recon.getTitle());
                holder.memory.setText(recon.getMemory());

            }
        }
    }


    @Override
    public int getItemCount() {
        return list.size();
    }


    public static class ReconViewHolder extends RecyclerView.ViewHolder{

        ImageView imageView;
        TextView textView;
        TextView memory;


        public ReconViewHolder(@NonNull View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.layoutInputStr);
            textView = itemView.findViewById(R.id.layoutInput);
            memory = itemView.findViewById(R.id.memoryInput);

        }
    }






}
