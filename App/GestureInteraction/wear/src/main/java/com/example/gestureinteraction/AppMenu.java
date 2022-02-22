package com.example.gestureinteraction;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.wear.widget.WearableLinearLayoutManager;

import com.example.gestureinteraction.databinding.ActivityAppMenuBinding;

import java.util.ArrayList;

public class AppMenu extends GestureActivity {
    private ActivityAppMenuBinding binding;
    private RecyclerView recyclerView;
    private AppMenuAdapter adapter;
    private int currFocus = 0;
    private View v;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAppMenuBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        recyclerView = binding.recyclerLauncherView;
        v = this.findViewById(android.R.id.content);

        ArrayList<AppItem> listItems = new ArrayList<>();
        listItems.add(new AppItem("Message", R.drawable.ic_baseline_chat));
        listItems.add(new AppItem("Music Player", R.drawable.ic_baseline_library_music));
        listItems.add(new AppItem("Settings", R.drawable.ic_baseline_settings));
        listItems.add(new AppItem("Timer", R.drawable.ic_baseline_timer));
        listItems.add(new AppItem("Weather", R.drawable.ic_baseline_wb_sunny));
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setLayoutManager(new WearableLinearLayoutManager(this));
        adapter = new AppMenuAdapter(this, listItems, new AppMenuAdapter.AdapterCallback() {
            @Override
            public void onItemClicked(final Integer menuPosition) {
                if (menuPosition == 0) {
                    Intent intent = new Intent(AppMenu.this, ChatMenu.class);
                    startActivity(intent);
                } else if (menuPosition == 1) {
                    Intent intent = new Intent(AppMenu.this, MusicMenu.class);
                    startActivity(intent);
                }
            }
        });
        recyclerView.setAdapter(adapter);
        updateUI("Null");
    }

    @Override
    public void updateUI(String result) {
        switch (result){
            case "Finger Snapping":
                currFocus = adapter.focusNextItem();
                break;
            case "Finger Waving":
                currFocus = adapter.focusPrevItem();
                break;
            case "Finger Pinching":
                recyclerView.getChildAt(currFocus).callOnClick();
                break;
        }
        if(currFocus != -1){
            recyclerView.smoothScrollToPosition(currFocus);
            recyclerView.requestFocus(currFocus);
        }
    }
}