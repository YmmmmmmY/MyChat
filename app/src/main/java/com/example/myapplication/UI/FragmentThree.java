package com.example.myapplication.UI;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.myapplication.UI.New;
import com.example.myapplication.NewsContentActivity;
import com.example.myapplication.R;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class FragmentThree extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // 天行网「综合新闻」接口地址：num=5 返回 5 条，rand=1 保证每次请求随机不同
    private static final String NEWS_URL =
            "https://apis.tianapi.com/generalnews/index?key=6eeb5bf0e6ec370bde0a787e5b6e1efa&num=5&rand=1";

    private String mParam1;
    private String mParam2;

    // 界面控件成员变量，与布局 id 一一对应
    private Button btStringNews;
    private Button btListNews;
    private TextView tvShowString;
    private ListView lvShowNews;

    // 新闻数据列表：ListView 与点击跳转共用同一份引用
    private final List<New.ResultBean.NewslistBean> newsList = new ArrayList<>();

    // 复用同一个 OkHttpClient（含连接池），避免每次请求都重建
    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .writeTimeout(5, TimeUnit.SECONDS)
            .build();

    private final Gson gson = new Gson();

    public FragmentThree() {
        // Required empty public constructor
    }

    public static FragmentThree newInstance(String param1, String param2) {
        FragmentThree fragment = new FragmentThree();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_three, container, false);

        // 绑定控件
        btStringNews = view.findViewById(R.id.btStringNews);
        btListNews = view.findViewById(R.id.btListNews);
        tvShowString = view.findViewById(R.id.tvShowString);
        lvShowNews = view.findViewById(R.id.lvShowNews);

        // 任务4：发送 GET 请求，把返回的 JSON 字符串原样显示在文本框
        btStringNews.setOnClickListener(v -> fetchRawJson());

        // 任务5/6/7：Gson 解析 JSON -> News 列表 -> 适配器 -> ListView
        btListNews.setOnClickListener(v -> fetchAndShowNews());

        // 任务8：点击某条新闻，携带标题与链接跳转到 WebView 详情页
        lvShowNews.setOnItemClickListener((parent, itemView, position, id) -> {
            New.ResultBean.NewslistBean item = newsList.get(position);
            String url = item.getUrl();
            if (url == null || url.isEmpty()) {
                Toast.makeText(requireContext(), "该新闻没有可用链接", Toast.LENGTH_SHORT).show();
                return;
            }
            NewsContentActivity.start(requireContext(), item.getTitle(), url);
        });

        return view;
    }

    // ========== 任务4：发送 GET 请求，将返回的 JSON 字符串直接输出 ==========
    private void fetchRawJson() {
        Request request = new Request.Builder().url(NEWS_URL).build();
        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                showOnUi("请求失败：" + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String body = (response.body() != null) ? response.body().string() : "";
                showOnUi(body);
            }
        });
    }

    // ========== 任务5/6/7：请求 + Gson 解析 -> News 列表 -> ListView ==========
    private void fetchAndShowNews() {
        Request request = new Request.Builder().url(NEWS_URL).build();
        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                showOnUi("请求失败：" + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String json = (response.body() != null) ? response.body().string() : "";
                // 解析为 New 模型（与 NewAdapter 共用同一套数据结构）
                New root = gson.fromJson(json, New.class);

                // 接口约定 code=200 才表示成功；否则给出服务端 msg 提示
                if (root == null || root.getCode() != 200
                        || root.getResult() == null
                        || root.getResult().getNewslist() == null) {
                    String tip = (root != null && root.getMsg() != null) ? root.getMsg() : "未获取到新闻数据";
                    showOnUi("加载失败：" + tip);
                    return;
                }

                List<New.ResultBean.NewslistBean> list = root.getResult().getNewslist();
                requireActivity().runOnUiThread(() -> {
                    tvShowString.setText("共获取到 " + list.size() + " 条新闻");
                    newsList.clear();
                    newsList.addAll(list);
                    NewAdapter adapter = new NewAdapter(requireContext(), newsList);
                    lvShowNews.setAdapter(adapter);
                });
            }
        });
    }

    // 统一切回主线程更新文本框
    private void showOnUi(String text) {
        requireActivity().runOnUiThread(() -> tvShowString.setText(text));
    }
}
