package com.example.gestureinteraction;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private TextView mTextViewDevice;
    private Button mButtonCollection, mButtonTest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTextViewDevice = findViewById(R.id.textView_Device);
        mButtonCollection = findViewById(R.id.button_DataCollection);
        mButtonTest = findViewById(R.id.button_Test);
        setToolbar();
        requestPermission();
        mButtonCollection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), DataCollection.class);
                startActivity(intent);
            }
        });
        mButtonTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), GestureTesting.class);
                startActivity(intent);
            }
        });
        mTextViewDevice.setText(getDevice());
    }

    private String getDevice(){
        BluetoothAdapter mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBtAdapter != null){
            Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();
            if (pairedDevices.size() > 0) {
                for (BluetoothDevice device : pairedDevices) {
                    return device.getName();
                }
                Toast.makeText(this, "A smartwatch found.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "No paired smartwatch found.", Toast.LENGTH_SHORT).show();
            }
        }
        return "No paired smartwatch found";
    }

    private void setToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Smartwatch Gesture Recognition");
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setBackgroundColor(Color.DKGRAY);
    }

    private void requestPermission(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE }, 1);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}