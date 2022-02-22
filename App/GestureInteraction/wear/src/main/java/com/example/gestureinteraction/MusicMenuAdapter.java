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

import java.util.ArrayList;

public class MusicMenuAdapter extends RecyclerView.Adapter<MusicMenuAdapter.RecyclerViewHolder> {
    private Context context;
    private AdapterCallback callback;
    private ArrayList<MusicItem> dataSource;
    private ArrayList<MusicItem> dataSourceCopy;
    private int currFocus = 0;

    public interface AdapterCallback {
        void onItemClicked(Integer menuPosition);
    }

    public MusicMenuAdapter(Context context, ArrayList<MusicItem> dataArgs, AdapterCallback callback) {
        this.context = context;
        this.dataSource = new ArrayList<MusicItem>(dataArgs);
        this.dataSourceCopy = new ArrayList<MusicItem>(dataArgs);
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
        MusicItem data_provider = dataSource.get(position);
        holder.menuItem.setText(data_provider.getName());
        holder.menuImage.setImageBitmap(data_provider.getIcon());
        holder.menuPreview.setText(data_provider.getPreview());
        holder.menuDate.setVisibility(View.GONE);
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

    public int filter(String keyword) {
        dataSource.clear();
        if(keyword.equals("")){
            dataSource.addAll(dataSourceCopy);
        } else{
            keyword = keyword.toLowerCase();
            for(MusicItem item: dataSourceCopy){
                if(item.getName().toLowerCase().contains(keyword)){
                    dataSource.add(item);
                }
            }
        }
        currFocus = 0;
        notifyDataSetChanged();
        return dataSource.size();
    }
}

class MusicItem{
    private Bitmap icon;
    private String name;
    private String preview;

    public MusicItem(Bitmap icon, String name, String preview) {
        this.icon = icon;
        this.name = name;
        this.preview = preview;
    }

    public String getName() {
        return name;
    }

    public Bitmap getIcon() {
        return icon;
    }

    public String getPreview() {return preview;}
}