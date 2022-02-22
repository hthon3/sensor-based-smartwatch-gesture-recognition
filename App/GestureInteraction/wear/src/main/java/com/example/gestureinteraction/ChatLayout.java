package com.example.gestureinteraction;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gestureinteraction.databinding.ActivityChatLayoutBinding;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.ArrayList;

public class ChatLayout extends GestureActivity {
    private ActivityChatLayoutBinding binding;
    private int currFocus;

    private TextView mTextViewUser;
    private ImageButton mImageButtonInput;
    private RecyclerView recyclerView;
    private MessageListAdapter adapter;
    private ArrayList<Message> listItems;
    private String userName = "";
    private String message = "";
    private ClipboardManager mClipboard;
    private String recordPath = Environment.getExternalStorageDirectory().getPath() + File.separator + "GestInteraction" + File.separator + "chat" + File.separator + "records";
    private View v;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatLayoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        v = this.findViewById(android.R.id.content);

        mTextViewUser = binding.textViewUser;
        mImageButtonInput = binding.imageButtonInput;
        recyclerView = binding.chatList;
        mClipboard = (ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            userName = extras.getString("user_name");
            mTextViewUser.setText(userName);
            listItems = loadRecord();
        }
        mImageButtonInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ChatLayout.this, InputPanel.class);
                intent.putExtra("text_input", message);
                intent.putExtra("type_input", "Input");
                startActivityForResult(intent, 0);
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MessageListAdapter(this, listItems, new MessageListAdapter.AdapterCallback() {
            @Override
            public void onItemClicked(final Integer menuPosition) {
                Toast.makeText(getApplicationContext(), "Copy", Toast.LENGTH_SHORT).show();
                ClipData clipData = ClipData.newPlainText("message", listItems.get(menuPosition).getMessage());
                mClipboard.setPrimaryClip(clipData);
            }
        });
        recyclerView.setAdapter(adapter);
        currFocus = listItems.size() - 1;
        if(currFocus < 0 ) currFocus = 0;
        recyclerView.smoothScrollToPosition(currFocus);
        updateUI("Null");
    }

    private ArrayList<Message> loadRecord() {
        ArrayList<Message> records = new ArrayList<>();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(recordPath + File.separator + userName + ".txt"))));
            String line = "";
            while ((line = reader.readLine()) != null) {
                String message[] = line.split(";");
                Message m = new Message(message[1], message[2], Long.parseLong(message[0]));
                if (records.isEmpty() || !records.get(records.size()-1).getDate().equals(m.getDate()))
                    records.add(new Message("", "", m.getCreateAt()));
                records.add(m);
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return records;
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
                    if(listItems.get(currFocus-1).getSender().equals("")){
                        recyclerView.smoothScrollToPosition(currFocus-1);
                    } else {
                        recyclerView.smoothScrollToPosition(currFocus);
                    }
                }
                break;
            case "Wrist Dropping":
                currFocus = adapter.ScrollDown();
                if (currFocus != -1){
                    recyclerView.smoothScrollToPosition(currFocus+2);
                }
                break;
            case "Finger Squeezing":
                if(currFocus != -1)
                    recyclerView.findViewHolderForAdapterPosition(currFocus).itemView.callOnClick();
                break;
            case "Knocking":
                mImageButtonInput.callOnClick();
                break;
            case "Hand Rotation":
                Intent intent = new Intent();
                intent.putExtra("text_input", listItems.get(listItems.size()-1).getMessage());
                setResult(RESULT_OK, intent);
                finish();
                break;
        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                message = data.getStringExtra("text_input");
                String action = data.getStringExtra("action");
                if (message != null && !message.equals("")){
                    if(action.equals("Submit")){
                        long time = System.currentTimeMillis();
                        try {
                            String newContent;
                            File f = new File(recordPath + File.separator + userName + ".txt");
                            if (f.length() == 0) {
                                newContent = String.format("%d;*;%s", time, message);
                            } else {
                                newContent = System.lineSeparator() + String.format("%d;*;%s", time, message);
                            }
                            Writer output = new BufferedWriter(new FileWriter(recordPath + File.separator + userName + ".txt", true));
                            output.append(newContent);
                            output.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        currFocus = adapter.sendMessage(message,time);
                        message = "";
                        recyclerView.smoothScrollToPosition(currFocus);
                    }
                }
            }
        }
    }
}