package com.example.gestureinteraction;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Point;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.os.Environment;
import android.os.Vibrator;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
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

public class GestureTesting extends AppCompatActivity {
    private TextView mTextViewSample, mTextViewResult, mTextViewRate, mTextViewAccel, mTextViewGyro;
    private Button mButtonCorrect, mButtonIncorrect;
    private Vibrator vibrator;
    private LineChart mLineChartAccel, mLineChartGyro;
    private List<String> dataList = new ArrayList<>();
    private String resultType = "";
    private Classifier mClassifier;
    private IntentFilter messageFilter;
    private Receiver messageReceiver;
    private int selectedItem = 0;
    private String gestureSet[] = {"Wrist Lifting", "Wrist Dropping", "CW Circling", "CCW Circling",
            "Finger Flicking", "Finger Pinching", "Finger Snapping", "Finger Rubbing",
            "Hand Squeezing", "Hand Sweeping",
            "Hand Rotation", "Hand Waving", "Hand Left Flipping", "Hand Right Flipping",
            "Drawing Square", "Drawing Triangle", "Knocking", "Draw Letter A",
            "Draw Letter B", "Draw Letter C", "Draw Letter D", "Draw Letter E", "Null"};
    private String newType = gestureSet[0];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gesture_testing);
        mLineChartAccel = findViewById(R.id.chart_Accel_Graph);
        mLineChartGyro = findViewById(R.id.chart_Gyro_Graph);
        initialChart(mLineChartAccel);
        initialChart(mLineChartGyro);
        mTextViewResult = findViewById(R.id.textView_Result);
        mTextViewRate = findViewById(R.id.textView_Rate);
        mTextViewSample = findViewById(R.id.textView_Sample);
        mTextViewAccel = findViewById(R.id.textView_Accel_Header);
        mTextViewGyro = findViewById(R.id.textView_Gyro_Header);
        mButtonCorrect = findViewById(R.id.button_Correct);
        mButtonIncorrect = findViewById(R.id.button_Incorrect);
        mClassifier = Classifier.getInstance();

        setToolbar();
        restore();
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        mButtonCorrect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveData(resultType);
                restore();
            }
        });
        mButtonIncorrect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setPopupWindow(view);
            }
        });
    }

    private class Receiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            restore();
            Bundle extras = intent.getExtras();
            String dataString = null;
            if (extras != null) {
                dataString = extras.getString("sensor_data_classification");
            }
            if(dataString != null){
                dataList = new ArrayList<String>(Arrays.asList(dataString.split("\n")));
                updateCharts();
                classify();
                vibrator.vibrate(100);
                mButtonCorrect.setEnabled(true);
                mButtonCorrect.setTextColor(Color.rgb(0, 153, 0));
                mButtonIncorrect.setEnabled(true);
                mButtonIncorrect.setTextColor(Color.rgb(255, 0, 0));
            }
        }
    }

    private void restore(){
        resultType = "";
        mTextViewSample.setTextColor(Color.BLACK);
        mTextViewAccel.setTextColor(Color.BLACK);
        mTextViewGyro.setTextColor(Color.BLACK);
        dataList.clear();
        mLineChartAccel.clear();
        mLineChartGyro.clear();
        mTextViewResult.setText("No Gesture Detected");
        mTextViewRate.setText("");
        mTextViewSample.setText("No Data Received From Smartwatch");
        mButtonCorrect.setEnabled(false);
        mButtonCorrect.setTextColor(Color.GRAY);
        mButtonIncorrect.setEnabled(false);
        mButtonIncorrect.setTextColor(Color.GRAY);
    }

    private void initialChart(LineChart chart){
        chart.getDescription().setEnabled(false);
        chart.setTouchEnabled(false);
        chart.setDragEnabled(false);
        XAxis xa = chart.getXAxis();
        YAxis rightYAxis = chart.getAxisRight();
        xa.setAxisMinimum(0);
        xa.setAxisMaximum(200);
        rightYAxis.setEnabled(false);
    }

    private void setChartSize(LineChart chart, float sy, float ey){
        int s = (int) (sy + 1f);
        int l = (int) (ey + 1f);
        if(sy < 0) s = (int) (sy - 1f);
        if(ey < 0) l = (int) (ey - 1f);
        YAxis ya = chart.getAxisLeft();
        ya.setAxisMinimum(s);
        ya.setAxisMaximum(l);
    }

    private void updateCharts(){
        int sampleNumber = dataList.size();
        boolean notValid = sampleNumber < 200;
        float minA = 0f, maxA = 0f, minG = 0f, maxG = 0f;
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
            float max1 = Math.max(Math.max(Float.parseFloat(data[0]), Float.parseFloat(data[1])), Float.parseFloat(data[2]));
            float min1 = Math.min(Math.min(Float.parseFloat(data[0]), Float.parseFloat(data[1])), Float.parseFloat(data[2]));
            float max2 = Math.max(Math.max(Float.parseFloat(data[3]), Float.parseFloat(data[4])), Float.parseFloat(data[5]));
            float min2 = Math.min(Math.min(Float.parseFloat(data[3]), Float.parseFloat(data[4])), Float.parseFloat(data[5]));
            if(max1 > maxA) maxA = max1;
            if(min1 < minA) minA = min1;
            if(max2 > maxG) maxG = max2;
            if(min2 < minG) minG = min2;
        }
        setChartSize(mLineChartAccel, minA , maxA);
        setChartSize(mLineChartGyro, minG, maxG);
        setChartData(mLineChartAccel, ax, ay, az, "Accel x", "Accel y", "Accel z");
        setChartData(mLineChartGyro, gx, gy, gz, "Gyro x", "Gyro y", "Gyro z");
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

    private void classify(){
        List<Float> ax, ay, az, gx, gy, gz;
        ax = new ArrayList<>(); ay =new ArrayList<>(); az = new ArrayList<>();
        gx = new ArrayList<>(); gy =new ArrayList<>(); gz = new ArrayList<>();
        for (String d : dataList){
            String dataRow[] = d.split(",");
            ax.add(Float.parseFloat(dataRow[0]));
            ay.add(Float.parseFloat(dataRow[1]));
            az.add(Float.parseFloat(dataRow[2]));
            gx.add(Float.parseFloat(dataRow[3]));
            gy.add(Float.parseFloat(dataRow[4]));
            gz.add(Float.parseFloat(dataRow[5]));
        }
        String result[] = mClassifier.classification(this.getApplicationContext(), ax, ay, az, gx, gy, gz).split(",");
        ax.clear(); ay.clear(); az.clear();
        gx.clear(); gy.clear(); gz.clear();
        resultType = result[0];
        mTextViewResult.setText(resultType);
        if (result[1].equals("0")){
            mTextViewRate.setText("");
        } else {
            mTextViewRate.setText("Confidence Score: " + result[1]);
        }
    }

    private void setToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Gesture Recognition Testing");
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setBackgroundColor(Color.DKGRAY);
    }

    private void setPopupWindow(View view){
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_window, null);
        PopupWindow popupWindow = new PopupWindow(popupView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, true);
        popupWindow.setWidth((int) (size.x/1.2));
        popupWindow.setHeight(size.y/3);
        popupWindow.setElevation(20);
        Spinner mSpinnerGesture = (Spinner) popupWindow.getContentView().findViewById(R.id.spinner_Gesture);
        Button mButtonSubmit = (Button) popupWindow.getContentView().findViewById(R.id.button_Submit);
        Button mButtonDiscard = (Button) popupWindow.getContentView().findViewById(R.id.button_Discard);
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
        selectedItem = adapter.getPosition(newType);
        mSpinnerGesture.setSelection(selectedItem);
        mSpinnerGesture.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                newType = adapterView.getItemAtPosition(i).toString();
                selectedItem = i;
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) { }
        });
        mButtonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveData(newType);
                restore();
                popupWindow.dismiss();
            }
        });
        mButtonDiscard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                restore();
                popupWindow.dismiss();
            }
        });
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
    }

    private void saveData(String gestureType){
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
        } catch (IOException e){
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "File Not Found", Toast.LENGTH_SHORT).show();
        }
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