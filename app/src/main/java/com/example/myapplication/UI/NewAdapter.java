package com.example.myapplication.UI;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;

import java.util.List;

/**
 * 新闻列表适配器（RecyclerView 版）：负责把 News 对象列表与自定义布局 newslist_item 绑定，
 * 并用 Glide 异步加载网络图片（空地址或加载失败时回退到默认图），图片随列表滚动懒加载。
 */
public class NewAdapter extends RecyclerView.Adapter<NewAdapter.NewsViewHolder> {

    private final Context context;
    private final List<New.ResultBean.NewslistBean> dataList;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(New.ResultBean.NewslistBean item);
    }

    public NewAdapter(Context context, List<New.ResultBean.NewslistBean> list, OnItemClickListener listener) {
        this.context = context;
        this.dataList = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public NewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context)
                .inflate(R.layout.newslist_item, parent, false);
        return new NewsViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull NewsViewHolder holder, int position) {
        New.ResultBean.NewslistBean news = dataList.get(position);
        holder.tvTitle.setText(news.getTitle());
        holder.tvDesc.setText(news.getDescription() != null ? news.getDescription() : "");
        holder.tvInfo.setText(news.getSource() + " | " + news.getCtime());

        // Glide 加载网络图片，空地址或加载失败时显示默认图（异步加载，不阻塞列表滚动）
        Glide.with(context)
                .load(news.getPicUrl())
                .placeholder(R.mipmap.ic_launcher)
                .error(R.mipmap.ic_launcher)
                .into(holder.ivImg);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(news);
        });
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    static class NewsViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImg;
        TextView tvTitle;
        TextView tvDesc;
        TextView tvInfo;

        NewsViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImg = itemView.findViewById(R.id.ivNewsImg);
            tvTitle = itemView.findViewById(R.id.tvNewsTitle);
            tvDesc = itemView.findViewById(R.id.tvNewsDesc);
            tvInfo = itemView.findViewById(R.id.tvNewsInfo);
        }
    }
}
