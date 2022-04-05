package com.example.gestureinteraction;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gestureinteraction.databinding.ActivityMusicMenuBinding;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;

public class MusicMenu extends GestureActivity{
    private ActivityMusicMenuBinding binding;
    private int currFocus = 0;

    private RecyclerView recyclerView;
    private ImageButton mImageButtonSearch;
    private MusicMenuAdapter adapter;
    private String keyword = "";
    private ArrayList<MusicItem> listItems = new ArrayList<MusicItem>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMusicMenuBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mImageButtonSearch = binding.imageButtonSearch;

        recyclerView = binding.recyclerLauncherView;
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        listItems = loadSong();
        adapter = new MusicMenuAdapter(this, listItems, new MusicMenuAdapter.AdapterCallback() {
            @Override
            public void onItemClicked(final Integer menuPosition) {
                Intent intent = new Intent(MusicMenu.this, MusicLayout.class);
                intent.putExtra("song_name", listItems.get(menuPosition).getFileName());
                startActivity(intent);
            }
        });
        recyclerView.setAdapter(adapter);

        mImageButtonSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MusicMenu.this, InputPanel.class);
                intent.putExtra("text_input", keyword);
                intent.putExtra("type_input", "Search");
                startActivityForResult(intent, 0);
            }
        });
        updateUI("Null");
    }
    private ArrayList<MusicItem> loadSong(){
        ArrayList<MusicItem> songs = new ArrayList<>();
        MediaMetadataRetriever mmr;
        String path = Environment.getExternalStorageDirectory().getPath() + File.separator + "GestInteraction" + File.separator + "music";
        try {
            File f = new File(path);
            if(f.exists()) {
                String[] songItems = f.list();
                mmr = new MediaMetadataRetriever();
                for (String p : songItems) {
                    mmr.setDataSource(path + File.separator + p);
                    String albumName = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
                    int duration = (int) Long.parseLong(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
                    String time = String.format("%02d:%02d", (duration / 1000) / 60, (duration / 1000) % 60);
                    byte[] art = mmr.getEmbeddedPicture();
                    Bitmap songImage = BitmapFactory.decodeByteArray(art, 0, art.length);
                    songs.add(new MusicItem(songImage, albumName, time, p));
                }
                songs.sort(Comparator.comparing(MusicItem::getName));
            } else {
                Toast.makeText(this, "No Song is Found", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return songs;
    }

    @Override
    public void updateUI(String result){
        switch (result){
            case "Finger Flicking":
                currFocus = adapter.focusPrevItem();
                break;
            case "Hand Waving":
                currFocus = adapter.focusNextItem();
                break;
            case "Wrist Lifting":
                currFocus = adapter.ScrollUp();
                break;
            case "Wrist Dropping":
                currFocus = adapter.ScrollDown();
                break;
            case "Finger Snapping":
                recyclerView.getChildAt(currFocus).callOnClick();
                break;
            case "Knocking":
                mImageButtonSearch.callOnClick();
                break;
            case "Hand Rotation":
                finish();
                break;
        }
        if(currFocus != -1){
            recyclerView.smoothScrollToPosition(currFocus);
            recyclerView.requestFocus(currFocus);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                keyword = data.getStringExtra("text_input");
                String action = data.getStringExtra("action");
                if(keyword != null && !keyword.equals("")){
                    if(action.equals("Submit")){
                        int result = adapter.filter(keyword);
                        Toast.makeText(getApplicationContext(), String.format("Result: %d", result), Toast.LENGTH_SHORT).show();
                        currFocus = 0;
                        recyclerView.smoothScrollToPosition(currFocus);
                    }
                }
            }
        }
    }
}