package com.example.myapplication.UI;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.myapplication.UI.New;
import com.example.myapplication.R;

import java.util.List;

/**
 * 新闻列表适配器：负责把 News 对象列表与自定义布局 newslist_item.xml 绑定，
 * 并用 Glide 加载网络图片（空地址或加载失败时回退到默认图）。
 */
public class NewAdapter extends BaseAdapter {

    private final Context context;
    private final List<New.ResultBean.NewslistBean> dataList;

    public NewAdapter(Context context, List<New.ResultBean.NewslistBean> list) {
        this.context = context;
        this.dataList = list;
    }

    @Override
    public int getCount() {
        return dataList.size();
    }

    @Override
    public Object getItem(int position) {
        return dataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context)
                    .inflate(R.layout.newslist_item, parent, false);
            holder = new ViewHolder();
            holder.ivImg = convertView.findViewById(R.id.ivNewsImg);
            holder.tvTitle = convertView.findViewById(R.id.tvNewsTitle);
            holder.tvDesc = convertView.findViewById(R.id.tvNewsDesc);
            holder.tvInfo = convertView.findViewById(R.id.tvNewsInfo);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        New.ResultBean.NewslistBean news = dataList.get(position);
        holder.tvTitle.setText(news.getTitle());
        holder.tvDesc.setText(news.getDescription() != null ? news.getDescription() : "");
        holder.tvInfo.setText(news.getSource() + " | " + news.getCtime());

        // Glide 加载网络图片，空地址或加载失败时显示默认图
        Glide.with(context)
                .load(news.getPicUrl())
                .placeholder(R.mipmap.ic_launcher)
                .error(R.mipmap.ic_launcher)
                .into(holder.ivImg);

        return convertView;
    }

    static class ViewHolder {
        ImageView ivImg;
        TextView tvTitle;
        TextView tvDesc;
        TextView tvInfo;
    }
}
