package com.example.myapplication.UI;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.myapplication.R;

import java.util.List;

public class SongAdapter extends BaseAdapter {
    private Context ctx;
    private List<Song> songList;

    public SongAdapter(Context ctx, List<Song> songList) {
        this.ctx = ctx;
        this.songList = songList;
    }

    @Override
    public int getCount() {
        return songList.size();
    }

    @Override
    public Object getItem(int position) {
        return songList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(ctx).inflate(R.layout.item_song, parent, false);
            holder = new ViewHolder();
            holder.ivSmallCover = convertView.findViewById(R.id.ivSmallCover);
            holder.tvSongName = convertView.findViewById(R.id.tvSongName);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        Song song = songList.get(position);
        holder.ivSmallCover.setImageResource(song.getCoverId());
        holder.tvSongName.setText(song.getSongName());
        return convertView;
    }

    static class ViewHolder {
        ImageView ivSmallCover;
        TextView tvSongName;
    }
}
