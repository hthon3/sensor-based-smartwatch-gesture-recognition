package com.example.gestureinteraction;

import android.content.Context;

import com.example.gestureinteraction.ml.GestureModel;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Classifier {
    private static Classifier instance;
    private ArrayList<String> gestureSet;
    private final int TIME_STEP = 200;

    private Classifier(){
        gestureSet = new ArrayList<>();
        gestureSet.add("Wrist Lifting");gestureSet.add("Wrist Dropping");
        gestureSet.add("CW Circling");gestureSet.add("CCW Circling");
        gestureSet.add("Finger Flicking");gestureSet.add("Finger Pinching");
        gestureSet.add("Finger Snapping");gestureSet.add("Finger Rubbing");
        gestureSet.add("Hand Squeezing");gestureSet.add("Hand Sweeping");
        gestureSet.add("Hand Rotation");gestureSet.add("Hand Waving");
        gestureSet.add("Hand Left Flipping");gestureSet.add("Hand Right Flipping");
        gestureSet.add("Knocking");
        gestureSet.add("Draw Letter A");
        gestureSet.add("Draw Letter B");gestureSet.add("Draw Letter C");
        gestureSet.add("Draw Letter D");gestureSet.add("Draw Letter E");
        gestureSet.add("Null");
        Collections.sort(gestureSet);
    }

    public static Classifier getInstance(){
        if(instance == null){
            instance = new Classifier();
        }
        return instance;
    }

    public String classification(Context ctx, List<Float> ax, List<Float>ay, List<Float>az, List<Float>gx, List<Float>gy, List<Float>gz){
        String resultString = "";
        if (ax.size() == TIME_STEP && ay.size() == TIME_STEP && az.size() == TIME_STEP && gx.size() == TIME_STEP && gy.size() == TIME_STEP && gz.size() == TIME_STEP){
            List<Float> accel_data = new ArrayList<>();
            List<Float> gyro_data = new ArrayList<>();
            accel_data.addAll(ax);accel_data.addAll(ay);accel_data.addAll(az);
            gyro_data.addAll(gx);gyro_data.addAll(gy);gyro_data.addAll(gz);
            try{
                GestureModel model = GestureModel.newInstance(ctx);
                TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 600}, DataType.FLOAT32);
                inputFeature0.loadArray(toFloatArray(accel_data));
                TensorBuffer inputFeature1 = TensorBuffer.createFixedSize(new int[]{1, 600}, DataType.FLOAT32);
                inputFeature1.loadArray(toFloatArray(gyro_data));

                GestureModel.Outputs outputs = model.process(inputFeature0, inputFeature1);
                TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();
                float[] result = outputFeature0.getFloatArray();
                resultString = resultEvaluate(result);
                model.close();
            } catch (Exception e){ }
        } else {
            resultString = "Insufficient Sampling Number,0";
        }
        ax.clear(); ay.clear(); az.clear();
        gx.clear(); gy.clear(); gz.clear();
        return resultString;
    }

    private String resultEvaluate(float[] result){
        int largest = 0;
        String type = "";
        for ( int i = 1; i < result.length; i++ ) {
            if ( result[i] >= result[largest] ) largest = i;
        }
        type = String.format("%s,%.3f", gestureSet.get(largest), result[largest]);
        return type;
    }

    private float[] toFloatArray(List<Float> data) {
        int i=0;
        float[] array=new float[data.size()];
        for (Float f : data) {
            array[i++] = (f != null ? f: Float.NaN);
        }
        return array;
    }
}