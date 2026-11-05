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

public class WechatFriendAdapter extends BaseAdapter {
    private Context context;
    private List<Friend> friendList;

    public WechatFriendAdapter(Context context, List<Friend> list){
        this.context = context;
        this.friendList = list;
    }
    @Override
    public int getCount() {
        return friendList.size();
    }
    @Override
    public Object getItem(int position) {
        return friendList.get(position);
    }
    @Override
    public long getItemId(int position) {
        return position;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if(convertView == null){
            convertView = LayoutInflater.from(context).inflate(R.layout.item_wechat_friend,parent,false);
            holder = new ViewHolder();
            holder.ivAvatar = convertView.findViewById(R.id.iv_avatar);
            holder.tvName = convertView.findViewById(R.id.tv_name);
            holder.tvMsg = convertView.findViewById(R.id.tv_msg);
            holder.tvTime = convertView.findViewById(R.id.tv_time);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }
        Friend f = friendList.get(position);
        holder.ivAvatar.setImageResource(f.getAvatarId());
        holder.tvName.setText(f.getName());
        holder.tvMsg.setText(f.getLastMsg());
        holder.tvTime.setText(f.getTime());
        return convertView;
    }
    static class ViewHolder{
        ImageView ivAvatar;
        TextView tvName;
        TextView tvMsg;
        TextView tvTime;
    }
}
