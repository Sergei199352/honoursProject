package com.example.ReMemoryHons;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ReMemoryHons.faceRecon.FaceClassifier;
import com.example.ReMemoryHons.faceRecon.FaceRecognition;

import java.io.IOException;
import java.util.List;

public class registerFaceAdapter extends RecyclerView.Adapter<registerFaceAdapter.RegisterViewHolder> {
    Bitmap image;
    private final Context context;

    public registerFaceAdapter(Context context, List<Bitmap> faceList) {
        this.context = context;
        this.faceList = faceList;
    }

    private final List<Bitmap> faceList;
    FaceClassifier faceClassifier;
     // Add a listener member variable




    @NonNull
    @Override
    public RegisterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        try {
            faceClassifier = FaceRecognition.create(context.getAssets(), "facenet.tflite", 160, false);
        } catch (IOException e) {
            e.printStackTrace();
        }


        // creates new view holders for the items
        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.card_register_item_register, parent, false);
        return new RegisterViewHolder(view);
    }


    public void removeItem(int position) {
        if (position >= 0 && position < faceList.size()) {
            faceList.remove(position);
            notifyItemRemoved(position);

            // if the face list is empty then display a confirmation message and return to the Main page
            if (faceList.size() == 0){
                confirmed();
            }
        }
    }


    @Override
    public void onBindViewHolder(@NonNull RegisterViewHolder holder, int position) {
        // Binds the data from the dataset to the views inside the view holder

         image = faceList.get(position);

        holder.imageView.setImageBitmap(image);


        holder.regButton.setTag(position);
        holder.regButton.setOnClickListener(view -> {
            int position1 = (int) view.getTag();
            if (position1 >= faceList.size() ){
                position1 = 0;
            }
            Bitmap bitmap = faceList.get(position1);
            EditText memory = holder.memory;
            EditText editText = holder.input;
            String addData = memory.getText().toString();
            String data =editText.getText().toString();
            FaceClassifier.Classification recon = faceClassifier.classify(bitmap,true);
            if (data.equals("")|| addData.equals("")){
                if (data.equals("")){
                editText.setError("Please enter a name");}
                if (addData.equals("")){
                    memory.setError("Please enter a memory");
                }
            } else {


                faceClassifier.addNew(data, recon, addData);
                removeItem(position1);

            }


        });






    }
    private void confirmed(){
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.confirmed_layout);

        TextView alertMessageTextView = dialog.findViewById(R.id.alertMessage);
        Button dismissButton = dialog.findViewById(R.id.dismissButton);

        alertMessageTextView.setText("New Face Registered"); // Set your message here


        dismissButton.setOnClickListener(view -> {
            dialog.dismiss();
            Intent intent = new Intent(context, MainActivity.class);
            context.startActivity(intent);


        });
        dialog.show();



    }



    @Override
    public int getItemCount() {
        return faceList.size();
    }


    public static class RegisterViewHolder extends RecyclerView.ViewHolder{


        ImageView imageView;
        Button regButton;
        EditText input;
        EditText memory;


        public RegisterViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.layoutInputStr);
            regButton = itemView.findViewById(R.id.registButton);
            input = itemView.findViewById(R.id.layoutInput);
            memory =  itemView.findViewById(R.id.memoryInput);

        }
    }
}
