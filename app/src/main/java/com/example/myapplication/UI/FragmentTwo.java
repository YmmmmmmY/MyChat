package com.example.myapplication.UI;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.example.myapplication.MusicService;
import com.example.myapplication.R;
import com.example.myapplication.UI.Song;
import com.example.myapplication.UI.SongAdapter;
import java.util.ArrayList;
import java.util.List;

public class FragmentTwo extends Fragment {
    private MediaPlayer activityMediaPlayer;
    private Button button1, button2, button3, button4, button5;

    // 互斥模式：0=无独占 1=Activity组(1/2)独占 2=Service组(3/4)独占
    private static final int MODE_NONE = 0;
    private static final int MODE_ACTIVITY = 1;
    private static final int MODE_SERVICE = 2;

    // ListView相关
    ListView listViewSong;
    LinearLayout layoutNowPlay;
    ImageView ivBigCover;
    TextView tvBigSongName;
    // ========== 新增：时间TextView / 进度条 / 总时长 ==========
    private TextView tvTime;
    private TextView tvTotal;
    private SeekBar seekBar;
    private boolean isSeeking = false;   // 用户是否正在拖动进度条

    private List<Song> songList;
    private SongAdapter adapter;
    private int playingIndex = -1;   // 当前正在播放的列表项索引，-1 表示未播放
    private int selectedIndex = -1;  // 最近一次在列表选中的歌曲索引，用于 button3 续播
    private int activityResumePos = 0; // Activity播放器停止时保存的进度(毫秒)
    private int activityCurrentSongId = -1; // Activity播放器当前播放的歌曲资源ID
    private BroadcastReceiver completeReceiver;



    // ========== 新增：计时相关（子线程 + Handler） ==========
    private Thread timeThread;
    private boolean isTimeThreadRunning = true;

    // 子线程每 1 秒采集一次播放进度，打包给主线程刷新 UI
    private static class TimerData {
        final int pos;   // 当前播放位置(毫秒)
        final int dur;   // 歌曲总时长(毫秒)
        TimerData(int pos, int dur) { this.pos = pos; this.dur = dur; }
    }

    // Handler：主线程接收消息，更新tvTime与进度条
    private final Handler timeHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            if (msg.what == 1001 && msg.obj instanceof TimerData) {
                // 视图尚未绑定完或已销毁时直接跳过，避免空指针
                if (tvTime == null || tvTotal == null || seekBar == null) return true;
                TimerData d = (TimerData) msg.obj;
                if (!isSeeking) {
                    // 用户拖动进度条时不回写，避免滑块被“弹回”
                    tvTime.setText(formatTime(d.pos));
                    if (d.dur > 0) {
                        // 进度条按 当前位置/总时长 计算，歌曲结束正好涨满
                        seekBar.setMax(d.dur);
                        seekBar.setProgress(d.pos);
                        tvTotal.setText(formatTime(d.dur));
                    }
                    // 未播放(dur==0)时保持上一次的数值不变，即“与播放状态一致”
                }
            }
            return true;
        }
    });

    // 毫秒 -> HH:MM:SS
    private String formatTime(int ms) {
        int totalSec = ms / 1000;
        int h = totalSec / 3600;
        int m = (totalSec % 3600) / 60;
        int s = totalSec % 60;
        return String.format(java.util.Locale.getDefault(), "%02d:%02d:%02d", h, m, s);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_two, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //绑定5个按钮
        button1 = view.findViewById(R.id.button1);
        button2 = view.findViewById(R.id.button2);
        button3 = view.findViewById(R.id.button3);
        button4 = view.findViewById(R.id.button4);
        button5 = view.findViewById(R.id.button5);

        //绑定ListView控件
        listViewSong = view.findViewById(R.id.listViewSong);
        layoutNowPlay = view.findViewById(R.id.layoutNowPlay);
        ivBigCover = view.findViewById(R.id.ivBigCover);
        tvBigSongName = view.findViewById(R.id.tvBigSongName);
        // ========== 新增：绑定tvTime / tvTotal / progressBar ==========
        tvTime = view.findViewById(R.id.tvTime);
        tvTotal = view.findViewById(R.id.tvTotal);
        seekBar = view.findViewById(R.id.progressBar);
        // 进度条拖动跳转：按住暂停回写，松手调用播放器 seekTo
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar bar, int progress, boolean fromUser) {
                if (fromUser) {
                    tvTime.setText(formatTime(progress)); // 拖动时实时预览时间
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar bar) {
                isSeeking = true; // 开始拖动：暂停计时线程回写，避免抖动
            }
            @Override
            public void onStopTrackingTouch(SeekBar bar) {
                isSeeking = false;
                seekTo(bar.getProgress()); // 松手：真正跳转到指定位置
            }
        });

        // 初始化歌曲列表：名称 / 封面(drawable) / 音频(raw)
        songList = new ArrayList<>();
        songList.add(new Song("不该", R.drawable.p4, R.raw.bugai));
        songList.add(new Song("牡丹江", R.drawable.p5, R.raw.mudangjiang));
        songList.add(new Song("幸福了", R.drawable.p6, R.raw.xinfule));
        songList.add(new Song("西厢寻他", R.drawable.p7, R.raw.xixiangxunta));

        adapter = new SongAdapter(requireContext(), songList);
        listViewSong.setAdapter(adapter);

        // ========== 新增：启动计时子线程 ==========
        startTimeThread();

        // ========== button1 Activity播放（播放列表选中的歌曲，停止后续播） ==========
        button1.setOnClickListener(v -> {
            // 切换到 Activity 独占模式：禁用 Service 组(3/4)
            updateButtons(MODE_ACTIVITY);
            // 播放列表最近选中的歌曲（未选过则默认《不该》）
            int targetId = (selectedIndex >= 0) ? songList.get(selectedIndex).getAudioId() : R.raw.bugai;
            if(activityMediaPlayer == null){
                activityMediaPlayer = MediaPlayer.create(requireContext(), targetId);
                activityCurrentSongId = targetId;
                if(activityMediaPlayer != null && activityResumePos > 0){
                    activityMediaPlayer.seekTo(activityResumePos); //续播到上次位置
                }
            }else if(activityCurrentSongId != targetId){
                // 切换到另一首：重建播放器并从头播放
                activityMediaPlayer.stop();
                activityMediaPlayer.release();
                activityMediaPlayer = MediaPlayer.create(requireContext(), targetId);
                activityCurrentSongId = targetId;
                activityResumePos = 0;
            }
            if(activityMediaPlayer != null && !activityMediaPlayer.isPlaying()){
                activityMediaPlayer.start();
            }
            // 同步底部放大显示当前歌曲
            if(selectedIndex >= 0){
                Song s = songList.get(selectedIndex);
                ivBigCover.setImageResource(s.getCoverId());
                tvBigSongName.setText(s.getSongName());
                playingIndex = selectedIndex;
            }else{
                ivBigCover.setImageResource(R.drawable.p4);
                tvBigSongName.setText("不该");
                playingIndex = -2;
            }
            layoutNowPlay.setVisibility(View.VISIBLE);
        });

        // ========== button2 Activity停止（修复闪退空指针bug，并保存进度） ==========
        button2.setOnClickListener(v -> {
            if(activityMediaPlayer != null){
                activityResumePos = activityMediaPlayer.getCurrentPosition(); //保存进度以便续播
                activityMediaPlayer.stop();
                activityMediaPlayer.release();
                activityMediaPlayer = null;
            }
            // 解除独占，两组按钮全部恢复可用；但保留底部大图（显示当前歌曲封面+名称）
            playingIndex = -1;
            updateButtons(MODE_NONE);
        });

        // ========== button3 Service播放（续播列表最近选中的歌曲，默认《不该》） ==========
        button3.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), MusicService.class);
            intent.putExtra("action","play");
            if(selectedIndex >= 0){
                // 续播列表里最近选中的那首
                Song s = songList.get(selectedIndex);
                intent.putExtra("songResId", s.getAudioId());
                layoutNowPlay.setVisibility(View.VISIBLE);
                ivBigCover.setImageResource(s.getCoverId());
                tvBigSongName.setText(s.getSongName());
                playingIndex = selectedIndex;
            }else{
                // 没在列表选过 → 默认播放《不该》
                intent.putExtra("songResId", R.raw.bugai);
                layoutNowPlay.setVisibility(View.VISIBLE);
                ivBigCover.setImageResource(R.drawable.p4);
                tvBigSongName.setText("不该");
                playingIndex = -2;
            }
            requireContext().startService(intent);
            // 切换到 Service 独占模式：禁用 Activity 组(1/2)
            updateButtons(MODE_SERVICE);
        });

        // ========== button4 Service停止 ==========
        button4.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), MusicService.class);
            intent.putExtra("action","stop");
            requireContext().startService(intent);
            // 解除独占，两组按钮全部恢复可用
            updateButtons(MODE_NONE);
        });

        // ========== button5 关闭界面 ==========
        button5.setOnClickListener(v -> {
            requireActivity().finish();
        });

        // ========== ListView点击事件：点歌逻辑 ==========
        listViewSong.setOnItemClickListener((parent, itemView, position, id) -> {
            Song selectedSong = songList.get(position);
            Intent intent = new Intent(requireContext(), MusicService.class);
            if(position == playingIndex){
                // 点击当前正在播放歌曲 →停止播放（保留选中记录以便后续续播）
                intent.putExtra("action","stop");
                playingIndex = -1;
                layoutNowPlay.setVisibility(View.GONE);
                // 列表停止也解除独占，两组按钮恢复可用
                updateButtons(MODE_NONE);
            }else{
                // 点击别的歌曲 →切换播放新歌
                intent.putExtra("action","play");
                intent.putExtra("songResId", selectedSong.getAudioId());
                playingIndex = position;
                selectedIndex = position; //记录最近选中，供 button3 续播
                //底部放大展示封面歌名
                layoutNowPlay.setVisibility(View.VISIBLE);
                ivBigCover.setImageResource(selectedSong.getCoverId());
                tvBigSongName.setText(selectedSong.getSongName());
                // 列表属于 Service 播放 → 同样禁用 Activity 组(1/2)
                updateButtons(MODE_SERVICE);
            }
            requireContext().startService(intent);
        });

        // ========== 歌曲自然播完后，接收Service广播并复位界面状态 ==========
        completeReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                playingIndex = -1;
                layoutNowPlay.setVisibility(View.GONE);
                // 歌曲自然播完 → 计时与进度复位到 00:00:00，与播放状态一致
                if (tvTime != null) tvTime.setText("00:00:00");
                if (seekBar != null) seekBar.setProgress(0);
                if (tvTotal != null) tvTotal.setText("00:00:00");
                // Service 歌曲自然播完 → 解除独占
                updateButtons(MODE_NONE);
            }
        };
        ContextCompat.registerReceiver(requireContext(), completeReceiver,
                new IntentFilter(MusicService.ACTION_SONG_COMPLETE),
                ContextCompat.RECEIVER_NOT_EXPORTED);

        // 初始状态：两组按钮都可用（无任何独占）
        updateButtons(MODE_NONE);
    }

    // ========== 开启计时子线程（每秒采集真实播放进度） ==========
    private void startTimeThread(){
        timeThread = new Thread(() -> {
            while (isTimeThreadRunning){
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    break;
                }
                int pos = 0, dur = 0;
                // 优先取 Activity 播放器的进度；否则取 Service 播放器的进度
                if (activityMediaPlayer != null && activityMediaPlayer.isPlaying()){
                    try {
                        pos = activityMediaPlayer.getCurrentPosition();
                        dur = activityMediaPlayer.getDuration();

                    } catch (Exception ignored) {}
                } else if (MusicService.isPlaying()){
                    pos = MusicService.getPosition();
                    dur = MusicService.getDuration();

                }
                // 把当前进度/总时长交给主线程刷新 UI（与播放状态保持一致）
                Message m = timeHandler.obtainMessage(1001, new TimerData(pos, dur));
                timeHandler.sendMessage(m);
            }
        });
        timeThread.start();
    }

    // 拖动进度条跳转：优先 Activity 播放器，否则 Service 播放器（单位均为毫秒）
    private void seekTo(int ms) {
        if (activityMediaPlayer != null && activityMediaPlayer.isPlaying()) {
            try { activityMediaPlayer.seekTo(ms); } catch (Exception ignored) {}
        } else {
            MusicService.seekTo(ms);
        }
    }

    // 根据互斥模式启用/禁用两组按钮（button5 关闭界面始终可用）
    private void updateButtons(int mode){
        boolean none = (mode == MODE_NONE);
        button1.setEnabled(none || mode == MODE_ACTIVITY);
        button2.setEnabled(none || mode == MODE_ACTIVITY);
        button3.setEnabled(none || mode == MODE_SERVICE);
        button4.setEnabled(none || mode == MODE_SERVICE);
        // 列表属于 Service 侧：Activity 独占时一并禁用，避免两边同时出声
        listViewSong.setEnabled(mode != MODE_ACTIVITY);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // ========== 新增：停止计时线程、清空Handler消息 ==========
        isTimeThreadRunning = false;
        if(timeThread != null){
            timeThread.interrupt();
        }
        timeHandler.removeCallbacksAndMessages(null);

        //注销广播接收器，释放Activity内的MediaPlayer
        try {
            requireContext().unregisterReceiver(completeReceiver);
        } catch (IllegalArgumentException ignored) {
        }
        if(activityMediaPlayer != null){
            activityMediaPlayer.stop();
            activityMediaPlayer.release();
            activityMediaPlayer = null;
        }
    }
}