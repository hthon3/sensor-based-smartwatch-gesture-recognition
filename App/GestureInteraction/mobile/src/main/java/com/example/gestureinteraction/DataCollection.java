package com.example.gestureinteraction;

import static com.example.gestureinteraction.R.layout.activity_data_collection;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.os.Environment;
import android.os.Vibrator;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DataCollection extends AppCompatActivity {
    private TextView mTextViewCounter, mTextViewSample, mTextViewAccel, mTextViewGyro;
    private Button mButtonSave, mButtonDiscard;
    private LineChart mLineChartAccel, mLineChartGyro;
    private Spinner mSpinnerGesture;
    private Vibrator vibrator;
    private List<String> dataList = new ArrayList<>();
    private String gestureType = "";
    private int counter = 0;
    private int selectedItem = 0;
    private IntentFilter messageFilter;
    private Receiver messageReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(activity_data_collection);
        mLineChartAccel = findViewById(R.id.chart_Accel_Graph);
        mLineChartGyro = findViewById(R.id.chart_Gyro_Graph);
        initialChart(mLineChartAccel, -20, 20);
        initialChart(mLineChartGyro, -10, 10);
        mSpinnerGesture = findViewById(R.id.spinner_Gesture);
        mTextViewCounter = findViewById(R.id.textView_Counter);
        mTextViewSample = findViewById(R.id.textView_Sample);
        mTextViewAccel = findViewById(R.id.textView_Accel_Header);
        mTextViewGyro = findViewById(R.id.textView_Gyro_Header);
        mButtonSave = findViewById(R.id.button_Save);
        mButtonDiscard = findViewById(R.id.button_Discard);

        mTextViewCounter.setText(String.format("Saved Samples: %d", counter));
        setToolbar();
        restore();
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        mButtonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String theFileName = gestureType + "_" + new SimpleDateFormat("MM-dd_HH-mm-ss").format(System.currentTimeMillis()) + ".csv";
                String finalData = "";
                for (String d : dataList){
                    finalData = finalData + d + "," + gestureType + "\n";
                }
                File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                File file = new File(path, theFileName);
                try {
                    FileWriter fileWriter = new FileWriter(file, true);
                    fileWriter.write(finalData);
                    fileWriter.close();
                    Toast.makeText(getApplicationContext(), theFileName + " is saved in the Download Folder", Toast.LENGTH_SHORT).show();
                    mTextViewCounter.setText(String.format("Saved Samples: %d", ++counter));
                } catch (IOException e){
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "File Not Found", Toast.LENGTH_SHORT).show();
                }
                restore();
            }
        });
        mButtonDiscard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                restore();
            }
        });
        String gestureSet[] = {"Wrist Lifting", "Wrist Dropping", "CW Circling", "CCW Circling",
                "Finger Flicking", "Finger Pinching", "Finger Snapping", "Finger Rubbing",
                "Hand Squeezing", "Hand Sweeping",
                "Hand Rotation", "Hand Waving", "Hand Left Flipping", "Hand Right Flipping",
                "Drawing Square", "Drawing Triangle", "Knocking", "Draw Letter A",
                "Draw Letter B", "Draw Letter C", "Draw Letter D", "Draw Letter E", "Null"};
        ArrayAdapter<String> adapter = new ArrayAdapter(this,android.R.layout.simple_spinner_item, gestureSet){
            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View v = null;
                v = super.getDropDownView(position, null, parent);
                if (position == selectedItem) {
                    v.setBackgroundColor(Color.LTGRAY);
                }
                else {
                    v.setBackgroundColor(Color.TRANSPARENT);
                }
                return v;
            }
        };
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerGesture.setAdapter(adapter);
        mSpinnerGesture.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                gestureType = adapterView.getItemAtPosition(i).toString();
                selectedItem = i;
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) { }
        });
    }

    private class Receiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            restore();
            Bundle extras = intent.getExtras();
            String dataString = null;
            if (extras != null) {
                dataString = extras.getString("sensor_data_collection");
            }
            if(dataString != null){
                dataList = new ArrayList<String>(Arrays.asList(dataString.split("\n")));
                updateCharts();
                vibrator.vibrate(100);
                mButtonSave.setEnabled(true);
                mButtonDiscard.setEnabled(true);
            }
        }
    }

    private void restore(){
        mTextViewSample.setTextColor(Color.BLACK);
        mTextViewAccel.setTextColor(Color.BLACK);
        mTextViewGyro.setTextColor(Color.BLACK);
        dataList.clear();
        mLineChartAccel.clear();
        mLineChartGyro.clear();
        mTextViewSample.setText("No Data Received From Smartwatch");
        mButtonSave.setEnabled(false);
        mButtonDiscard.setEnabled(false);
    }

    private void initialChart(LineChart chart, int sy, int ey){
        chart.getDescription().setEnabled(false);
        chart.setTouchEnabled(false);
        chart.setDragEnabled(false);
        XAxis xa = chart.getXAxis();
        YAxis ya = chart.getAxisLeft();
        YAxis rightYAxis = chart.getAxisRight();
        xa.setAxisMinimum(0);
        xa.setAxisMaximum(200);
        ya.setAxisMinimum(sy);
        ya.setAxisMaximum(ey);
        rightYAxis.setEnabled(false);

    }

    private void updateCharts(){
        int sampleNumber = dataList.size();
        boolean notValid = sampleNumber < 200;
        if (sampleNumber != 0){
            ArrayList<Entry> ax = new ArrayList<>(), ay = new ArrayList<>(), az = new ArrayList<>(),
                    gx = new ArrayList<>(), gy = new ArrayList<>(), gz = new ArrayList<>();
            for (int i = 0; i < sampleNumber;i++){
                String[] data = dataList.get(i).split(",");
                ax.add(new Entry(i, Float.parseFloat(data[0])));
                ay.add(new Entry(i, Float.parseFloat(data[1])));
                az.add(new Entry(i, Float.parseFloat(data[2])));
                gx.add(new Entry(i, Float.parseFloat(data[3])));
                gy.add(new Entry(i, Float.parseFloat(data[4])));
                gz.add(new Entry(i, Float.parseFloat(data[5])));
            }
            setChartData(mLineChartAccel, ax, ay, az, "Accel x", "Accel y", "Accel z");
            setChartData(mLineChartGyro, gx, gy, gz, "Gyro x", "Gyro y", "Gyro z");
        }
        if(notValid) {
            mTextViewSample.setTextColor(Color.RED);
            mTextViewAccel.setTextColor(Color.RED);
            mTextViewGyro.setTextColor(Color.RED);
        } else {
            mTextViewSample.setTextColor(Color.BLACK);
            mTextViewAccel.setTextColor(Color.BLACK);
            mTextViewGyro.setTextColor(Color.BLACK);
        }
        mTextViewSample.setText(String.format("Total Data Sample Length: %d", sampleNumber));
    }

    private void setChartData(LineChart chart, ArrayList<Entry> x, ArrayList<Entry> y, ArrayList<Entry> z, String a, String b, String c){
        ArrayList<ILineDataSet> lineDataSet = new ArrayList<>();
        lineDataSet.add(new LineDataSet(x, a));
        lineDataSet.add(new LineDataSet(y, b));
        lineDataSet.add(new LineDataSet(z, c));
        for (int i = 0; i < lineDataSet.size(); i++){
            int color = Color.BLACK;
            switch (i){
                case 0: color = Color.RED;break;
                case 1: color = Color.BLUE;break;
                case 2: color = Color.rgb(0,128,0);break;
            }
            ((LineDataSet) lineDataSet.get(i)).setColor(color);
            ((LineDataSet) lineDataSet.get(i)).setLineWidth(1f);
            ((LineDataSet) lineDataSet.get(i)).setDrawCircles(false);
        }
        chart.setData(new LineData(lineDataSet));
        chart.invalidate();
    }

    private void setToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Gesture Data Collection");
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setBackgroundColor(Color.DKGRAY);
    }

    @Override
    public void onStart(){
        messageFilter = new IntentFilter(Intent.ACTION_SEND);
        messageReceiver = new Receiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver, messageFilter);
        super.onStart();
    }


    @Override
    public void onStop(){
        LocalBroadcastManager.getInstance(this).unregisterReceiver(messageReceiver);
        super.onStop();
    }

}