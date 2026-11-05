package com.example.myapplication.UI;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;

import com.example.myapplication.R;

import java.util.ArrayList;
import java.util.List;

public class FragmentOne extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_one, container, false);
        ListView listView = view.findViewById(R.id.listView_friend);

        List<Friend> dataList = new ArrayList<>();
        dataList.add(new Friend(android.R.drawable.ic_menu_gallery,"张三","晚上有空吗","19:20"));
        dataList.add(new Friend(android.R.drawable.ic_menu_gallery,"李四","收到","18:10"));
        dataList.add(new Friend(android.R.drawable.ic_menu_gallery,"王五","好的好的","昨天"));
        dataList.add(new Friend(android.R.drawable.ic_menu_gallery,"赵六","作业写完没","周一"));
        dataList.add(new Friend(android.R.drawable.ic_menu_gallery,"钱七","吃饭了吗","周日"));
        dataList.add(new Friend(android.R.drawable.ic_menu_gallery,"孙八","收到回复","周六"));
        dataList.add(new Friend(android.R.drawable.ic_menu_gallery,"周九","在哪呢","周五"));
        dataList.add(new Friend(android.R.drawable.ic_menu_gallery,"吴十","明白","周四"));
        dataList.add(new Friend(android.R.drawable.ic_menu_gallery,"郑十一","记得交实验","周三"));
        dataList.add(new Friend(android.R.drawable.ic_menu_gallery,"冯十二","下课一起走","周二"));

        WechatFriendAdapter adapter = new WechatFriendAdapter(getActivity(),dataList);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, v, position, id) -> {
            Friend friend = dataList.get(position);
            Toast.makeText(getActivity(), "点击好友："+friend.getName(), Toast.LENGTH_SHORT).show();
        });
        return view;
    }
}
