package com.example.gestureinteraction;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class MessageListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private AdapterCallback callback;
    private List<Message> dataSource;
    private int currFocus;

    private static final int VIEW_TYPE_MESSAGE_SENT = 1;
    private static final int VIEW_TYPE_MESSAGE_RECEIVED = 2;
    private static final int VIEW_TYPE_MESSAGE_DATE = 3;

    public interface AdapterCallback {
        void onItemClicked(Integer menuPosition);
    }

    public MessageListAdapter(Context context, ArrayList<Message> dataArgs, MessageListAdapter.AdapterCallback callback) {
        this.context = context;
        this.dataSource = dataArgs;
        this.callback = callback;
        currFocus = dataSource.size() - 1;
        if(currFocus < 0 ) currFocus = 0;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == VIEW_TYPE_MESSAGE_SENT) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_self, parent, false);
            return new SentMessageHolder(view);
        } else if (viewType == VIEW_TYPE_MESSAGE_RECEIVED) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_other, parent, false);
            return new ReceivedMessageHolder(view);
        } else if (viewType == VIEW_TYPE_MESSAGE_DATE) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_date, parent, false);
            return new DateMessageHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") final int position) {
        Message message = dataSource.get(position);
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (callback != null) {
                    callback.onItemClicked(position);
                }
            }
        };
        switch (holder.getItemViewType()) {
            case VIEW_TYPE_MESSAGE_SENT:
                ((SentMessageHolder) holder).bind(message);
                ((SentMessageHolder) holder).messageContainer.setOnClickListener(listener);
                if(position == currFocus) {
                    ((SentMessageHolder) holder).messageContainer.setBackgroundColor(Color.DKGRAY);
                } else {
                    ((SentMessageHolder) holder).messageContainer.setBackgroundColor(Color.TRANSPARENT);
                }
                break;
            case VIEW_TYPE_MESSAGE_RECEIVED:
                ((ReceivedMessageHolder) holder).bind(message);
                ((ReceivedMessageHolder) holder).messageContainer.setOnClickListener(listener);
                if(position == currFocus) {
                    ((ReceivedMessageHolder) holder).messageContainer.setBackgroundColor(Color.DKGRAY);
                } else {
                    ((ReceivedMessageHolder) holder).messageContainer.setBackgroundColor(Color.TRANSPARENT);
                }
                break;
            case VIEW_TYPE_MESSAGE_DATE:
                ((DateMessageHolder) holder).bind(message);
                ((DateMessageHolder) holder).messageContainer.setOnClickListener(listener);
                if(position == currFocus) {
                    currFocus += 1;
                }
                break;
        }
    }

    @Override
    public int getItemCount() {
        return dataSource.size();
    }

    @Override
    public int getItemViewType(int position) {
        Message message = dataSource.get(position);
        if (message.getSender().equals("*")){
            return VIEW_TYPE_MESSAGE_SENT;
        } else if (message.getSender().equals("")){
            return VIEW_TYPE_MESSAGE_DATE;
        } else {
            return VIEW_TYPE_MESSAGE_RECEIVED;
        }
    }

    public static class DateMessageHolder extends RecyclerView.ViewHolder {
        ConstraintLayout messageContainer;
        TextView dateText;
        public DateMessageHolder(View view){
            super(view);
            messageContainer = view.findViewById(R.id.message_container);
            dateText = view.findViewById(R.id.text_chat_date);
        }
        void bind(Message message) {
            dateText.setText(message.getDate());
        }
    }

    public static class ReceivedMessageHolder extends RecyclerView.ViewHolder {
        ConstraintLayout messageContainer;
        TextView messageText, timeText;
        public ReceivedMessageHolder(View view) {
            super(view);
            messageContainer = view.findViewById(R.id.message_container_other);
            messageText = view.findViewById(R.id.text_chat_message_other);
            timeText = view.findViewById(R.id.text_chat_timestamp_other);
        }
        void bind(Message message) {
            messageText.setText(message.getMessage());
            timeText.setText(message.getTime());
        }
    }

    public static class SentMessageHolder extends RecyclerView.ViewHolder {
        ConstraintLayout messageContainer;
        TextView messageText, timeText;
        public SentMessageHolder(View view) {
            super(view);
            messageContainer = view.findViewById(R.id.message_container_self);
            messageText = view.findViewById(R.id.text_chat_message_self);
            timeText = view.findViewById(R.id.text_chat_timestamp_self);
        }
        void bind(Message message) {
            messageText.setText(message.getMessage());
            timeText.setText(message.getTime());
        }
    }

    public int focusPrevItem(){
        if(dataSource.size() == 0) return -1;
        currFocus -= 1;
        if(dataSource.get(currFocus).getSender().equals("")) currFocus -= 1;
        if(currFocus < 0) currFocus = dataSource.size() - 1;
        notifyDataSetChanged();
        return currFocus;
    }
    public int focusNextItem(){
        if(dataSource.size() == 0) return -1;
        currFocus += 1;
        if(dataSource.get(currFocus).getSender().equals("")) currFocus +=1;
        if(currFocus > dataSource.size() - 1) currFocus = 1;
        notifyDataSetChanged();
        return currFocus;
    }
    public int ScrollUp(){
        if(dataSource.size() == 0) return -1;
        if(currFocus - 4 >= 0)
            if (currFocus - 5 < 0){
                currFocus -= 2;
            } else {
                currFocus -= 4;
            }
        else
            currFocus = 1;
        if(dataSource.get(currFocus).getSender().equals("")) currFocus +=1;
        notifyDataSetChanged();
        return currFocus;
    }
    public int ScrollDown(){
        if(dataSource.size() == 0) return -1;
        if(currFocus + 4 <= dataSource.size() - 1)
            if (currFocus + 5 > dataSource.size() - 1){
                currFocus += 2;
            } else {
                currFocus += 4;
            }
        if(dataSource.get(currFocus).getSender().equals("")) currFocus +=1;
        notifyDataSetChanged();
        return currFocus;
    }

    public int sendMessage(String message, long time){
        Message m = new Message("*", message, time);
        if(dataSource.isEmpty() || !dataSource.get(dataSource.size()-1).getDate().equals(m.getDate()))
            dataSource.add(new Message("", "", m.getCreateAt()));
        dataSource.add(m);
        currFocus = dataSource.size() - 1;
        notifyDataSetChanged();
        return currFocus;
    }
}

class Message{
    String sender;
    String message;
    long timestamp;
    Message(String s, String m, long t){
        sender = s;
        message = m;
        timestamp = t;
    }
    public String getMessage() {
        return message;
    }
    public String getSender(){
        return sender;
    }
    public String getTime() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
        return String.format(simpleDateFormat.format(timestamp));
    }
    public String getDate() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM-dd-yyyy");
        return String.format(simpleDateFormat.format(timestamp));
    }
    public long getCreateAt(){
        return timestamp;
    }
}