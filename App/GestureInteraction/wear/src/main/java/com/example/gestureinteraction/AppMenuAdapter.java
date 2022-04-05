package com.example.gestureinteraction;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class AppMenuAdapter extends RecyclerView.Adapter<AppMenuAdapter.RecyclerViewHolder> {
    private Context context;
    private AdapterCallback callback;
    private ArrayList<AppItem> dataSource = new ArrayList<AppItem>();
    private int currFocus = 0;

    public interface AdapterCallback {
        void onItemClicked(Integer menuPosition);
    }

    public AppMenuAdapter(Context context, ArrayList<AppItem> dataArgs, AdapterCallback callback) {
        this.context = context;
        this.dataSource = dataArgs;
        this.callback = callback;
    }
    @NonNull
    @Override
    public RecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.menu_item, parent, false);
        return new RecyclerViewHolder(view);
    }

    public static class RecyclerViewHolder extends RecyclerView.ViewHolder {
        RelativeLayout menuContainer;
        TextView appName;
        ImageView appIcon;
        public RecyclerViewHolder(View view) {
            super(view);
            menuContainer = view.findViewById(R.id.menu_container);
            appName = view.findViewById(R.id.textView_AppName);
            appIcon = view.findViewById(R.id.imageView_AppIcon);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerViewHolder holder, @SuppressLint("RecyclerView") final int position) {
        AppItem data_provider = dataSource.get(position);
        holder.appName.setText(data_provider.getText());
        holder.appIcon.setImageResource(data_provider.getIcon());
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

    public int focusPrevItem(){
        if(dataSource.size() == 0)
            return -1;
        currFocus -= 1;
        if(currFocus < 0)
            currFocus = dataSource.size() - 1;
        notifyDataSetChanged();
        return currFocus;
    }
    public int focusNextItem(){
        if(dataSource.size() == 0)
            return -1;
        currFocus += 1;
        if(currFocus > dataSource.size() - 1)
            currFocus = 0;
        notifyDataSetChanged();
        return currFocus;
    }
    public int ScrollUp(){
        if(dataSource.size() == 0)
            return -1;
        currFocus = 0;
        notifyDataSetChanged();
        return currFocus;
    }
    public int ScrollDown(){
        if(dataSource.size() == 0)
            return -1;
        currFocus = dataSource.size() - 1;
        notifyDataSetChanged();
        return currFocus;
    }

    @Override
    public int getItemCount() {
        return dataSource.size();
    }
}

class AppItem {
    private String text;
    private int icon;

    public AppItem(String text, int icon) {
        this.text = text;
        this.icon = icon;
    }

    public String getText() {
        return text;
    }

    public int getIcon() {
        return icon;
    }
}