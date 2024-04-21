package com.example.ReMemoryHons.faceRecon;

import static com.example.ReMemoryHons.MainActivity.registered;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.util.Pair;


import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Map;

/**
 * this class was downloaded from github together with the facenet model in the asset folder
 * git https://github.com/estebanuri/face_recognition.git
 */

public class FaceRecognition
        implements FaceClassifier {


    private static final int out = 512;




    private static final float mean_value = 128.0f;
    private static final float standardDeviation = 128.0f;

    private boolean isModelQuantized;

    private int inputSize;

    private int[] intValues;

    private float[][] emb;

    private ByteBuffer data;

    private Interpreter model;


// Sergey Stanislavchuk changed this function to add an additional string variable
    // that would store the optional information about the face
    public void addNew(String name, Classification rec, String string) {
        Pair data = new Pair(string, rec);
        registered.put(name,data);
    }

    private FaceRecognition() {}


    private static MappedByteBuffer modelLoader(AssetManager assets, String modelFilename)
            throws IOException {
        AssetFileDescriptor fileDescriptor = assets.openFd(modelFilename);

        try (FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
             FileChannel fileChannel = inputStream.getChannel()) {

            long startOffset = fileDescriptor.getStartOffset();
            long declaredLength = fileDescriptor.getDeclaredLength();

            return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
        }
    }




    public static FaceClassifier create(
            final AssetManager assetManager,
            final String modelFilename,
            final int inputSize,
            final boolean isQuantized)
            throws IOException {

        final FaceRecognition d = new FaceRecognition();
        d.inputSize = inputSize;

        try {
            d.model = new Interpreter(modelLoader(assetManager, modelFilename));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        d.isModelQuantized = isQuantized;
        // Pre-allocate buffers.
        int numBytesPerChannel;
        if (isQuantized) {
            numBytesPerChannel = 1; // Quantized
        } else {
            numBytesPerChannel = 4; // Floating point
        }
        d.data = ByteBuffer.allocateDirect(1 * d.inputSize * d.inputSize * 3 * numBytesPerChannel);
        d.data.order(ByteOrder.nativeOrder());
        d.intValues = new int[d.inputSize * d.inputSize];
        return d;
    }


    private Pair<String, Float> findNearest(float[] emb) {
        Pair<String, Float> ret = null;
        for (Map.Entry<String, Pair<String, Classification>> entry : registered.entrySet()) {
            final String name = entry.getKey()+ "."+ entry.getValue().first ;
            final float[] knownEmb = ((float[][]) entry.getValue().second.getEmbeeding())[0];

            float distance = 0;
            for (int i = 0; i < emb.length; i++) {
                float diff = emb[i] - knownEmb[i];
                distance += diff*diff;
            }
            distance = (float) Math.sqrt(distance);
            if (ret == null || distance < ret.second) {
                ret = new Pair<>(name, distance);
            }
        }
        return ret;
    }



    @Override
    public Classification classify(final Bitmap bitmap, boolean bool) {
        // extracting the the pixel values from the bitmap image and stores them in the intValues array.
        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        //resets the position in the byte Buffer to the beginning ensuring this way that the buffer is ready to receive the new data from the beginning
        data.rewind();

        // this section normalises the data in the image in two different ways deppending if the model is quantised or not
        // the loop iterates through each pixel and then assigns it to the bytebuffer
        for (int i = 0; i < inputSize; ++i) {
            for (int j = 0; j < inputSize; ++j) {
                int pixelValue = intValues[i * inputSize + j];
                if (isModelQuantized) {
                    // Quantized model
                    // this section transforms the data into high and low bytes
                    // the quantized models
                    data.put((byte) ((pixelValue >> 16) & 0xFF));
                    data.put((byte) ((pixelValue >> 8) & 0xFF));
                    data.put((byte) (pixelValue & 0xFF));
                } else { // Float model
                    data.putFloat((((pixelValue >> 16) & 0xFF) - mean_value) / standardDeviation);
                    data.putFloat((((pixelValue >> 8) & 0xFF) - mean_value) / standardDeviation);
                    data.putFloat(((pixelValue & 0xFF) - mean_value) / standardDeviation);
                }
            }
        }
        Object[] inputArray = {data};
        // Here outputMap is changed to fit the Face Mask detector
        Map<Integer, Object> outputMap = new HashMap<>();

        emb = new float[1][out];
        outputMap.put(0, emb);

        // Run the inference call.
        model.runForMultipleInputsOutputs(inputArray, outputMap);


        float distance = Float.MAX_VALUE;
        String id = "0";
        String label = "?";
        String memory = " ";

        if (registered.size() > 0) {
            final Pair<String, Float> nearest = findNearest(emb[0]);
            if (nearest != null) {
                final String name = nearest.first;

                distance = nearest.second;
                String[] parts = name.split("\\."); // Split by full stop


                     label = parts[0];
                     memory = parts[1];



            }
        }
        final int numDetectionsOutput = 1;
        Classification rec = new Classification(
                id,
                label,
                distance,
                new RectF(),memory);


        if (bool) {
            rec.setEmbeeding(emb);
        }

        return rec;
    }


}
