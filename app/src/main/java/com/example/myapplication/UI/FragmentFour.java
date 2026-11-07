package com.example.myapplication.UI;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import androidx.fragment.app.Fragment;

import com.example.myapplication.R;

import java.util.ArrayList;
import java.util.List;

public class FragmentFour extends Fragment {

    TextView tvShowText1;
    TextView tvShowText3;
    ImageView ivUserAvatar;
    Button btFinish;
    private int clickCount = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_four, container, false);

        //绑定控件

        ivUserAvatar = v.findViewById(R.id.iv_useravatar);
        TextView tvWelcome = v.findViewById(R.id.tv_welcome);
        ListView listView = v.findViewById(R.id.list_mine_menu);

        //取出MainActivity传递过来登录数据
        Bundle bundle = getArguments();
        String username = null;
        String avatarStr = null;
        if(bundle != null){
            username = bundle.getString("username");
            avatarStr = bundle.getString("avatarIdStr");
        }


        //加载头像，原逻辑不变
        if(avatarStr != null && !avatarStr.isEmpty()){
            if(avatarStr.startsWith("file:")){
                String path = avatarStr.substring(5);
                Bitmap bmp = BitmapFactory.decodeFile(path);
                if(bmp != null){
                    ivUserAvatar.setImageBitmap(bmp);
                }
            }else{
                int avatarResId = Integer.parseInt(avatarStr);
                ivUserAvatar.setImageResource(avatarResId);
            }
        }

        //欢迎账号信息
        tvWelcome.setText("欢迎回来，" + username);



        //“我”页面列表菜单：服务、收藏、朋友圈、作品、小店与卡包、表情、设置
        List<String> menuList = new ArrayList<>();
        menuList.add("服务");
        menuList.add("收藏");
        menuList.add("朋友圈");
        menuList.add("作品");
        menuList.add("小店与卡包");
        menuList.add("表情");
        menuList.add("设置");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                getActivity(),
                R.layout.item_mine_menu,
                R.id.tv_menu_title,
                menuList
        );
        listView.setAdapter(adapter);

        return v;
    }
}
