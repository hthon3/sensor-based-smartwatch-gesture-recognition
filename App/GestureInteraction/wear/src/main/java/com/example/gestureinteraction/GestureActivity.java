package com.example.gestureinteraction;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.ArrayList;
import java.util.List;

public abstract class GestureActivity extends Activity implements SensorEventListener {
    private SensorManager mSensorManager;
    private Sensor mAccel, mGyro;
    private boolean isAccelPresent = false, isGyroPresent = false;
    protected List<Float> ax, ay, az, gx, gy, gz;
    private boolean isRecording = false;
    protected boolean moveDetected = false;
    private float threshold = 0.5f;

    private Classifier mClassifier;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mClassifier = Classifier.getInstance();
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccel = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mGyro = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        ax = new ArrayList<>(); ay =new ArrayList<>(); az = new ArrayList<>();
        gx = new ArrayList<>(); gy =new ArrayList<>(); gz = new ArrayList<>();
        samplingStart();
    }

    public void samplingStart(){
        registerSensorListeners();
        isRecording = true;
    }

    public void samplingStop(){
        isRecording = false;
        unregisterSensorListeners();
    }

    public void clearMemory(){
        ax.clear(); ay.clear(); az.clear();
        gx.clear(); gy.clear(); gz.clear();
    }

    public void dataHandling(){
        samplingStop();
        String gestureResult = mClassifier.classification(getApplicationContext(), ax, ay, az, gx, gy, gz);
        clearMemory();
        updateUI(gestureResult);
        samplingStart();
    }

    private void registerSensorListeners(){
        if(mAccel != null){
            mSensorManager.registerListener(this, mAccel, SensorManager.SENSOR_DELAY_FASTEST);
            isAccelPresent = true;
        }
        if(mGyro != null){
            mSensorManager.registerListener(this, mGyro, SensorManager.SENSOR_DELAY_FASTEST);
            isGyroPresent = true;
        }
    }

    private void unregisterSensorListeners(){
        if(isAccelPresent){
            mSensorManager.unregisterListener(this, mAccel);
            isAccelPresent = false;
        }
        if(isGyroPresent){
            mSensorManager.unregisterListener(this, mGyro);
            isGyroPresent = false;
        }

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(isRecording){
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                ax.add(event.values[0]);
                ay.add(event.values[1]);
                az.add(event.values[2]);
            }
            if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                gx.add(event.values[0]);
                gy.add(event.values[1]);
                gz.add(event.values[2]);
            }
            if (ax.size() >= 5) {
                checkMovement();
            } else {
                moveDetected = false;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }

    public void checkMovement(){
        if(!moveDetected){
            if (isMoveDetected(ax) || isMoveDetected(ay) || isMoveDetected(az) || isMoveDetected(gx) || isMoveDetected(gy) || isMoveDetected(gz)){
                moveDetected = true;
                ax = topPadding(ax);ay = topPadding(ay);az = topPadding(az);
                gx = topPadding(gx);gy = topPadding(gy);gz = topPadding(gz);
            } else {
                clearMemory();
            }
        } else {
            if (ax.size() >= 200 && gx.size() >= 200){
                dataHandling();
            }
        }
    }

    public boolean isMoveDetected(List<Float> data){
        if (data.isEmpty()){
            return false;
        } else {
            float sum = 0f;
            for (Float d : data){
                sum += d;
            }
            float avg = sum / (float) data.size();
            if (avg >= data.get(0) + threshold || avg <= data.get(0) - threshold){
                return true;
            } else {
                return false;
            }
        }
    }
    public List<Float> topPadding(List<Float> data){
        List<Float> newData = new ArrayList<>(data);
        Float pad = data.get(0);
        for (int i = 0; i < 10; i++){
            newData.add(0, pad);
        }
        return newData;
    }

    public abstract void updateUI(String result);

    public void setPopupWindow(View view, String message, int type){
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_icon, null);
        PopupWindow popupWindow = new PopupWindow(popupView, ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.MATCH_PARENT);
        ImageView icon = (ImageView) popupWindow.getContentView().findViewById(R.id.imageView_popupIcon);
        TextView text = (TextView) popupWindow.getContentView().findViewById(R.id.textView_popupMessage);
        ProgressBar bar = (ProgressBar) popupWindow.getContentView().findViewById(R.id.progressBar_popup);
        if (type != R.drawable.ic_baseline_volume_down && type != R.drawable.ic_baseline_volume_up){
            bar.setVisibility(View.GONE);
        } else {
            bar.setVisibility(View.VISIBLE);
        }
        icon.setImageResource(type);
        text.setText(message);
        popupWindow.setWidth(size.x);
        popupWindow.setHeight(size.y);
        view.post(new Runnable() {
            @Override
            public void run() {
                popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
                new Handler().postDelayed(new Runnable(){
                    public void run() {
                        popupWindow.dismiss();
                    }
                }, 500);
            }
        });
    }

    @Override
    public void onResume(){
        super.onResume();
        samplingStart();
    }
    @Override
    public void onPause(){
        super.onPause();
        samplingStop();
        clearMemory();
    }
}