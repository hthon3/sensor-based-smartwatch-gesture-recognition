package com.example.gestureinteraction;

import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;

import com.example.gestureinteraction.databinding.ActivityTestBinding;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class Test extends GestureActivity {
    private ActivityTestBinding binding;
    private TextView mTextViewGestureType;
    private CircleProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTestBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mTextViewGestureType = binding.textViewGestureType;
        mProgressBar = binding.progressBar;
        mTextViewGestureType.setText("No Movement Detected");
    }

    @Override
    public void checkMovement(){
        if(Math.min(ax.size(), gx.size()) > 5) {
            mProgressBar.setProgress(Math.min(ax.size(), gx.size()));
        } else {
            mProgressBar.setProgress(0);
        }
        if(!moveDetected){
            if (isMoveDetected(ax) || isMoveDetected(ay) || isMoveDetected(az) || isMoveDetected(gx) || isMoveDetected(gy) || isMoveDetected(gz)){
                moveDetected = true;
                mTextViewGestureType.setText("Movement Detected");
                ax = topPadding(ax);ay = topPadding(ay);az = topPadding(az);
                gx = topPadding(gx);gy = topPadding(gy);gz = topPadding(gz);
            } else {
                mTextViewGestureType.setText("No Movement Detected");
                clearMemory();
            }
        } else {
            if (ax.size() >= 200 && gx.size() >= 200){
                dataHandling();
            } else if (ax.size() >= 100 && gx.size() >= 100){
                List<Float> l_ax = ax.subList(ax.size()-30, ax.size());
                List<Float> l_ay = ay.subList(ay.size()-30, ay.size());
                List<Float> l_az = az.subList(az.size()-30, az.size());
                List<Float> l_gx = gx.subList(gx.size()-30, gx.size());
                List<Float> l_gy = gy.subList(gy.size()-30, gy.size());
                List<Float> l_gz = gz.subList(gz.size()-30, gz.size());
                if(isMoveStop(l_ax) && isMoveStop(l_ay) && isMoveStop(l_az) && isMoveStop(l_gx) && isMoveStop(l_gy) && isMoveStop(l_gz)){
                    ax = backPadding(ax);ay = backPadding(ay);az = backPadding(az);
                    gx = backPadding(gx);gy = backPadding(gy);gz = backPadding(gz);
                    dataHandling();
                }
            }
        }
    }
    @Override
    public void updateUI(String result) {

    }
}