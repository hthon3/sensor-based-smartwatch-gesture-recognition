package com.example.gestureinteraction;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gestureinteraction.databinding.ActivityDataCollectionBinding;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class DataCollection extends Activity implements SensorEventListener {
    private Button mStartButton;
    private TextView mTextViewAccelData, mTextViewGyroData, mTextViewTimer;
    private CircleProgressBar mProgressBar;
    private ActivityDataCollectionBinding binding;
    private Vibrator vibrator;
    private SensorManager mSensorManager;
    private Sensor mAccel, mGyro;
    private boolean isAccelPresent = false, isGyroPresent = false;

    private List<Float> ax, ay, az, gx, gy, gz;

    private final long TIME = 2000;
    private long mTimeLeft = TIME;

    private boolean isRecording = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDataCollectionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mProgressBar = binding.progressBar;
        mStartButton = binding.buttonStart;
        mTextViewAccelData = binding.textViewAccelData;
        mTextViewGyroData = binding.textViewGyroData;
        mTextViewTimer = binding.textViewTimer;
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccel = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mGyro = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        registerSensorListeners();
        unregisterSensorListeners();
        ax = new ArrayList<>(); ay =new ArrayList<>(); az = new ArrayList<>();
        gx = new ArrayList<>(); gy =new ArrayList<>(); gz = new ArrayList<>();

        mStartButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                samplingStart();
                new CountDownTimer(mTimeLeft, 1000) {
                    @Override
                    public void onTick(long l) {
                        mTimeLeft = l;
                        updateCountDownText();
                    }
                    @Override
                    public void onFinish() {
                        if (isRecording == true){
                            vibrator.vibrate(100);
                            isRecording = false;
                            Toast.makeText(DataCollection.this, "Error", Toast.LENGTH_SHORT).show();
                            if(mAccel != null){
                                mTextViewAccelData.setText(String.format("Accel Sample Number: %d", ax.size()));
                                mTextViewAccelData.setTextColor(Color.RED);
                            }
                            if (mGyro != null){
                                mTextViewGyroData.setText(String.format("Gyro Sample Number: %d", gx.size()));
                                mTextViewAccelData.setTextColor(Color.RED);
                            }
                            clearMemory();
                            unregisterSensorListeners();
                            mStartButton.setEnabled(true);
                        }
                        mTimeLeft = TIME;
                        updateCountDownText();
                    }
                }.start();
            }
        });
        updateCountDownText();
    }

    private ArrayList<String> recordData(){
        ArrayList<String> allData = new ArrayList<>();
        for (int i = 0; i < Math.min(ax.size(), gx.size()); i++){
            String data = ax.get(i) + "," + ay.get(i) + "," + az.get(i) + "," + gx.get(i) + "," + gy.get(i) + "," + gz.get(i) + "\n";
            allData.add(data);
        }
        clearMemory();
        return allData;
    }
    private void samplingStart(){
        registerSensorListeners();
        Toast.makeText(DataCollection.this, "Start", Toast.LENGTH_SHORT).show();
        mStartButton.setEnabled(false);
        isRecording = true;
    }

    private void samplingStop(){
        isRecording = false;
        vibrator.vibrate(100);
        if(mAccel != null){
            mTextViewAccelData.setText(String.format("Accel Sample Number: %d", ax.size()));
        }
        if (mGyro != null){
            mTextViewGyroData.setText(String.format("Gyro Sample Number: %d", gx.size()));
        }
        ArrayList<String> allData = recordData();
        new SendThread("/sensor_data_collection", allData).start();
        clearMemory();
        unregisterSensorListeners();
        mStartButton.setEnabled(true);
    }

    private void clearMemory(){
        ax.clear(); ay.clear(); az.clear();
        gx.clear(); gy.clear(); gz.clear();
    }
    private void updateCountDownText(){
        int min = (int) (mTimeLeft/1000)/60;
        int sec = (int) (mTimeLeft/1000)%60;
        mTextViewTimer.setText(String.format("Count Down: %02d:%02d", min, sec));
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
            mProgressBar.setProgress(Math.min(ax.size(), gx.size()));
        }
        if (ax.size() >= 200 && gx.size() >= 200){
            samplingStop();
        }
    }

    public void registerSensorListeners(){
        if(mAccel != null){
            mSensorManager.registerListener(this, mAccel, SensorManager.SENSOR_DELAY_FASTEST);
            isAccelPresent = true;
            mTextViewAccelData.setText("Accel Found");
            mTextViewAccelData.setTextColor(Color.GREEN);
        } else {
            mTextViewAccelData.setText("Accel Not Found");
            mTextViewAccelData.setTextColor(Color.RED);
        }
        if(mGyro != null){
            mSensorManager.registerListener(this, mGyro, SensorManager.SENSOR_DELAY_FASTEST);
            isGyroPresent = true;
            mTextViewGyroData.setText("Gyro Found");
            mTextViewGyroData.setTextColor(Color.GREEN);
        } else {
            mTextViewGyroData.setText("Gyro Not Found");
            mTextViewGyroData.setTextColor(Color.RED);
        }
    }

    public void unregisterSensorListeners(){
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
    public void onAccuracyChanged(Sensor sensor, int accuracy) { }

    private class SendThread extends Thread {
        String path;
        String message;
        SendThread(String p, ArrayList<String> data) {
            path = p;
            message = "";
            for (String s : data) { message += s; }
        }
        public void run() {
            Task<List<Node>> nodeListTask = Wearable.getNodeClient(getApplicationContext()).getConnectedNodes();
            try {
                List<Node> nodes = Tasks.await(nodeListTask);
                for (Node node : nodes) {
                    Task<Integer> sendMessageTask = Wearable.getMessageClient(getApplicationContext()).sendMessage(node.getId(), path, message.getBytes());
                    Integer result = Tasks.await(sendMessageTask);
                    Log.v("DataCollection", "Send data to " + node.getDisplayName());
                }
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}