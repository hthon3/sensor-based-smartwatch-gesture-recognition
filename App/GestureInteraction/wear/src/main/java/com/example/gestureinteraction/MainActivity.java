package com.example.gestureinteraction;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.gestureinteraction.databinding.ActivityMainBinding;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends Activity {

    private Button mDataCollectButton, mInteractionButton, mTestButton, mReloadButton;
    private ActivityMainBinding binding;
    private String folder = "/GestInteraction";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mDataCollectButton = binding.dataCollectButton;
        mInteractionButton = binding.interactionButton;
        mTestButton = binding.testButton;
        mReloadButton = binding.refreshButton;

        mDataCollectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), DataCollection.class);
                startActivity(intent);
            }
        });
        mInteractionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), AppMenu.class);
                startActivity(intent);
            }
        });
        mTestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), Test.class);
                startActivity(intent);
            }
        });
        mReloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                File f = new File(Environment.getExternalStorageDirectory(), folder);
                if (f.exists()) {
                    deleteRecursive(f);
                    createFolder();
                    Toast.makeText(MainActivity.this, "Data Reloaded", Toast.LENGTH_SHORT).show();
                } else{
                    createFolder();
                    Toast.makeText(MainActivity.this, "Data Loaded", Toast.LENGTH_SHORT).show();
                }
            }
        });
        requestPermission();
    }

    private void requestPermission(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE }, 1);
        }
    }

    private void createFolder(){
        File f = new File(Environment.getExternalStorageDirectory(), folder);
        if (!f.exists()) {
            f.mkdirs();
            File music = new File(f , "/music");
            music.mkdirs();
            loadItem("music", music);
            File chat = new File(f, "/chat");
            chat.mkdirs();
            loadItem("chat", chat);
            File chatRecord = new File(chat, "/records");
            chatRecord.mkdirs();
            loadItem("chat" + File.separator + "records", chatRecord);
            File chatImage = new File(chat, "/icons");
            chatImage.mkdirs();
            loadItem("chat" + File.separator + "icons", chatImage);
        }
    }
    private void loadItem(String as, File f) {
        AssetManager assetManager = getAssets();
        InputStream in = null;
        OutputStream out = null;
        try{
            String[] items = getAssets().list(as);
            for(String i: items){
                in = assetManager.open(as + File.separator + i);
                out = new FileOutputStream(new File(f, i));
                byte[] buffer = new byte[1024];
                int read;
                while ((read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                }
                in.close();
                in = null;
                out.flush();
                out.close();
                out = null;
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }
    void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                deleteRecursive(child);

        fileOrDirectory.delete();
    }
}