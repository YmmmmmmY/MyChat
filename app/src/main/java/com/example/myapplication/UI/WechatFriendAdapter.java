package com.example.myapplication.UI;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;

import java.util.List;

/**
 * 会话（好友）列表适配器：基于 RecyclerView，复用 ItemView 避免重复 inflate，
 * 配合 Glide/本地资源加载头像，点击回调交给 Fragment 处理。
 */
public class WechatFriendAdapter extends RecyclerView.Adapter<WechatFriendAdapter.FriendViewHolder> {

    private final Context context;
    private final List<Friend> friendList;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Friend friend);
    }

    public WechatFriendAdapter(Context context, List<Friend> list, OnItemClickListener listener) {
        this.context = context;
        this.friendList = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context)
                .inflate(R.layout.item_wechat_friend, parent, false);
        return new FriendViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendViewHolder holder, int position) {
        Friend f = friendList.get(position);
        holder.ivAvatar.setImageResource(f.getAvatarId());
        holder.tvName.setText(f.getName());
        holder.tvMsg.setText(f.getLastMsg());
        holder.tvTime.setText(f.getTime());
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(f);
        });
    }

    @Override
    public int getItemCount() {
        return friendList.size();
    }

    static class FriendViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAvatar;
        TextView tvName;
        TextView tvMsg;
        TextView tvTime;

        FriendViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.iv_avatar);
            tvName = itemView.findViewById(R.id.tv_name);
            tvMsg = itemView.findViewById(R.id.tv_msg);
            tvTime = itemView.findViewById(R.id.tv_time);
        }
    }
}
