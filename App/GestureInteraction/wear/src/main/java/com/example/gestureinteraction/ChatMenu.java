package com.example.gestureinteraction;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gestureinteraction.databinding.ActivityChatMenuBinding;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class ChatMenu extends GestureActivity{
    private ActivityChatMenuBinding binding;
    private int currFocus = 0;

    private RecyclerView recyclerView;
    private ChatMenuAdapter adapter;
    private ImageButton mImageButtonAdd;
    private String newContact = "";
    private ArrayList<ChatItem> menuItems;
    private String iconPath = Environment.getExternalStorageDirectory().getPath() + File.separator + "GestInteraction" + File.separator + "chat" + File.separator + "icons";
    private String recordPath = Environment.getExternalStorageDirectory().getPath() + File.separator + "GestInteraction" + File.separator + "chat" + File.separator + "records";
    private View v;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatMenuBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mImageButtonAdd = binding.imageButtonAdd;
        v = this.findViewById(android.R.id.content);

        recyclerView = binding.recyclerLauncherView;
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        menuItems = loadChat();
        adapter = new ChatMenuAdapter(this, menuItems, new ChatMenuAdapter.AdapterCallback() {
            @Override
            public void onItemClicked(final Integer menuPosition) {
                Intent intent = new Intent(ChatMenu.this, ChatLayout.class);
                intent.putExtra("user_name", menuItems.get(menuPosition).getName());
                startActivity(intent);
            }
        });
        mImageButtonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ChatMenu.this, InputPanel.class);
                intent.putExtra("text_input", newContact);
                intent.putExtra("type_input", "Add Chat");
                startActivityForResult(intent, 0);
            }
        });
        recyclerView.setAdapter(adapter);
        updateUI("Null");
    }

    private ArrayList<ChatItem> loadChat() {
        ArrayList<ChatItem> users = new ArrayList<>();
        BufferedReader reader;
        try {
            File pf = new File(iconPath);
            File rf = new File(recordPath);
            if(rf.exists()){
                String[] userItems = rf.list();
                for(String u: userItems){
                    File f = new File(recordPath + File.separator + u);
                    if (f.length() == 0) {
                        f.delete();
                        continue;
                    }
                    String userName = u.replace(".txt", "");
                    File i = new File(iconPath + File.separator + userName + ".jpg");
                    Bitmap icon = null;
                    if(i.exists()){
                        InputStream ims = new FileInputStream(i);
                        icon = BitmapFactory.decodeStream(ims);
                        ims.close();
                    } else {
                        InputStream ims = new FileInputStream(new File(iconPath + File.separator + "Default.png"));
                        icon = BitmapFactory.decodeStream(ims);
                        ims.close();
                    }
                    reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(recordPath + File.separator + u))));
                    String line;
                    String lastLine = null;
                    while ((line = reader.readLine()) != null){
                        lastLine = line;
                    }
                    if (lastLine == null || lastLine == ""){
                        users.add(new ChatItem(icon, userName, "", 0));
                    } else {
                        String lastMessage[] = lastLine.split(";");
                        users.add(new ChatItem(icon, userName, lastMessage[2], Long.parseLong(lastMessage[0])));
                    }
                    reader.close();
                    users.sort(Comparator.comparingLong(ChatItem::getLastTime));
                    Collections.reverse(users);
                }
            } else {
                Toast.makeText(this, "No chat is Found", Toast.LENGTH_SHORT).show();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return users;
    }

    @Override
    public void updateUI(String result) {
        switch (result){
            case "Finger Snapping":
                currFocus = adapter.focusNextItem();
                if(currFocus != -1){
                    recyclerView.smoothScrollToPosition(currFocus);
                }
                break;
            case "Finger Waving":
                currFocus = adapter.focusPrevItem();
                if(currFocus != -1){
                    recyclerView.smoothScrollToPosition(currFocus);
                }
                break;
            case "Wrist Lifting":
                currFocus = adapter.ScrollUp();
                if (currFocus != -1){
                    recyclerView.smoothScrollToPosition(currFocus);
                }
                break;
            case "Wrist Dropping":
                currFocus = adapter.ScrollDown();
                if (currFocus != -1){
                    recyclerView.smoothScrollToPosition(currFocus+2);
                }
            case "Finger Pinching":
                recyclerView.getChildAt(currFocus).callOnClick();
                break;
            case "Knocking":
                mImageButtonAdd.callOnClick();
                break;
            case "Hand Rotation":
                finish();
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                newContact = data.getStringExtra("text_input");
                String action = data.getStringExtra("action");
                long time = System.currentTimeMillis();
                if(newContact != null && !newContact.equals("")){
                    if(action.equals("Submit")){
                        File newUser = new File(recordPath + File.separator + newContact + ".txt");
                        if (!newUser.exists()) {
                            try {
                                newUser.createNewFile();
                                Bitmap icon = null;
                                try{
                                    InputStream ims = new FileInputStream(new File(iconPath + File.separator + "Default.png"));
                                    icon = BitmapFactory.decodeStream(ims);
                                    ims.close();
                                } catch (Exception e){ }
                                adapter.addNewUser(icon, newContact, time);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        Intent intent = new Intent(ChatMenu.this, ChatLayout.class);
                        intent.putExtra("user_name", newContact);
                        startActivity(intent);
                    }
                }
            }
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        menuItems = loadChat();
        adapter.refresh(menuItems);
    }
}