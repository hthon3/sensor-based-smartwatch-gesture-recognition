package com.example.gestureinteraction;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class ChatMenuAdapter extends RecyclerView.Adapter<ChatMenuAdapter.RecyclerViewHolder> {
    private Context context;
    private ArrayList<ChatItem> dataSource = new ArrayList<ChatItem>();
    private AdapterCallback callback;
    private int currFocus = 0;

    public interface AdapterCallback {
        void onItemClicked(Integer menuPosition);
    }

    public ChatMenuAdapter(Context context, ArrayList<ChatItem> dataArgs, AdapterCallback callback) {
        this.context = context;
        this.dataSource = dataArgs;
        this.callback = callback;
    }
    @NonNull
    @Override
    public RecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.menu_preview_item, parent, false);
        return new RecyclerViewHolder(view);
    }

    public static class RecyclerViewHolder extends RecyclerView.ViewHolder {
        LinearLayout menuContainer;
        TextView menuItem, menuPreview, menuDate;
        ImageView menuImage;

        public RecyclerViewHolder(View view) {
            super(view);
            menuContainer = view.findViewById(R.id.layout_Container);
            menuItem = view.findViewById(R.id.textView_Name);
            menuImage = view.findViewById(R.id.imageView_Icon);
            menuPreview = view.findViewById(R.id.textView_Preview);
            menuDate = view.findViewById(R.id.textView_Date);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerViewHolder holder, @SuppressLint("RecyclerView") final int position) {
        ChatItem data_provider = dataSource.get(position);
        holder.menuItem.setText(data_provider.getName());
        holder.menuImage.setImageBitmap(data_provider.getIcon());
        holder.menuPreview.setText(data_provider.getPreview());
        holder.menuDate.setText(data_provider.getLastTimeInString());
        holder.menuContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (callback != null) {
                    callback.onItemClicked(position);
                }
            }
        });
        if(position == currFocus) {
            holder.menuContainer.setBackgroundColor(Color.DKGRAY);
        } else {
            holder.menuContainer.setBackgroundColor(Color.TRANSPARENT);
        }
    }

    public void addNewUser(Bitmap icon, String name, long t){
        ChatItem c = new ChatItem(icon, name, "", t);
        dataSource.add(c);
        currFocus = 0;
        dataSource.sort(Comparator.comparingLong(ChatItem::getLastTime));
        Collections.reverse(dataSource);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return dataSource.size();
    }

    public int focusPrevItem(){
        if(dataSource.size() == 0)
            return -1;
        currFocus -= 1;
        if(currFocus < 0) currFocus = dataSource.size() - 1;
        notifyDataSetChanged();
        return currFocus;
    }
    public int focusNextItem(){
        if(dataSource.size() == 0)
            return -1;
        currFocus += 1;
        if(currFocus > dataSource.size() - 1) currFocus = 0;
        notifyDataSetChanged();
        return currFocus;
    }
    public int ScrollUp(){
        if(dataSource.size() == 0)
            return -1;
        if(currFocus - 3 >= 0)
            if (currFocus - 4 < 0){
                currFocus -= 1;
            } else if (currFocus - 5 < 0) {
                currFocus -= 2;
            } else {
                currFocus -= 3;
            }
        else
            currFocus = 0;
        notifyDataSetChanged();
        return currFocus;
    }
    public int ScrollDown(){
        if(dataSource.size() == 0)
            return -1;
        if(currFocus + 3 <= dataSource.size() - 1)
            if (currFocus + 4 > dataSource.size() - 1){
                currFocus += 1;
            } else if (currFocus + 5 > dataSource.size() - 1) {
                currFocus += 2;
            } else {
                currFocus += 3;
            }
        notifyDataSetChanged();
        return currFocus;
    }

    public void refresh(ArrayList<ChatItem> dataArgs){
        this.dataSource = dataArgs;
        currFocus = 0;
        notifyDataSetChanged();
    }
}

class ChatItem {
    private Bitmap icon;
    private String name;
    private String preview;
    private long lastTime;

    public ChatItem(Bitmap icon, String name, String preview, long lastTime) {
        this.icon = icon;
        this.name = name;
        this.preview = preview;
        this.lastTime = lastTime;
    }

    public String getName() {
        return name;
    }

    public Bitmap getIcon() {
        return icon;
    }

    public String getPreview() { return preview; }

    public long getLastTime() {return lastTime; }

    public String getLastTimeInString(){
        SimpleDateFormat simpleDateFormat = null;
        long d = System.currentTimeMillis()- this.lastTime;
        if(d >= 86400000){
            simpleDateFormat = new SimpleDateFormat("dd/MM");
            return String.format(simpleDateFormat.format(this.lastTime));
        } else {
            long hd = d / 3600000;
            if(hd == 0){
                long md = d/60000;
                if(md == 0){
                    return "Now";
                } else {
                    return String.format("%dm", md);
                }
            } else {
                return String.format("%dhr", hd);
            }
        }
    }

}