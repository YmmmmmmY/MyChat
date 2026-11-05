package com.example.myapplication;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;

public class MusicService extends Service {
    //歌曲播放完成时发送的广播Action，Fragment收到后复位界面
    public static final String ACTION_SONG_COMPLETE = "com.example.myapplication.SONG_COMPLETE";

    MediaPlayer mediaPlayer;
    private int currentSongId = -1;   // 当前/上次播放的歌曲资源ID
    private int resumePosition = 0;   // 停止时保存的播放进度(毫秒)

    // 供 Fragment 子线程查询当前播放进度/总时长/状态（单例 Service）
    private static MusicService sInstance;


    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent == null) return START_NOT_STICKY;
        sInstance = this;   // 记录实例，供 Fragment 查询播放进度
        String action = intent.getStringExtra("action");
        int songId = intent.getIntExtra("songResId", -1);

        if("play".equals(action)){
            // 未指定具体歌曲(如button3续播) → 使用上一次播放的歌曲；从未播放过则默认《不该》
            if(songId == -1){
                songId = (currentSongId == -1) ? R.raw.bugai : currentSongId;
            }
            if(songId == currentSongId && mediaPlayer != null){
                // 同一首且正在播放 → 不重复创建，避免重播
                if(!mediaPlayer.isPlaying()){
                    mediaPlayer.start();
                }
            }else if(songId == currentSongId && mediaPlayer == null){
                // 同一首但已被停止 → 从保存的进度续播
                playSong(songId, resumePosition);
            }else{
                // 切换到新的歌曲 → 从头播放
                currentSongId = songId;
                resumePosition = 0;
                playSong(songId, 0);
            }
        }else if("stop".equals(action)){
            saveAndRelease();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    // ===== 供 UI 计时线程读取的静态访问器 =====
    public static int getPosition() {
        if (sInstance != null && sInstance.mediaPlayer != null) {
            try { return sInstance.mediaPlayer.getCurrentPosition(); }
            catch (Exception ignored) { return 0; }
        }
        return 0;
    }
    public static int getDuration() {
        if (sInstance != null && sInstance.mediaPlayer != null) {
            try { return sInstance.mediaPlayer.getDuration(); }
            catch (Exception ignored) { return 0; }
        }
        return 0;
    }
    public static boolean isPlaying() {
        return sInstance != null && sInstance.mediaPlayer != null && sInstance.mediaPlayer.isPlaying();
    }

    // 供 UI 进度条拖动跳转：跳转到指定毫秒位置
    public static void seekTo(int ms) {
        if (sInstance != null && sInstance.mediaPlayer != null) {
            try { sInstance.mediaPlayer.seekTo(ms); } catch (Exception ignored) {}
        }
    }

    // 播放指定歌曲，并从 fromPosition 毫秒处开始(0表示从开头)
    private void playSong(int songId, int fromPosition){
        releasePlayer();
        mediaPlayer = MediaPlayer.create(this, songId);
        if(mediaPlayer != null){
            currentSongId = songId;
            if(fromPosition > 0){
                try { mediaPlayer.seekTo(fromPosition); } catch (IllegalStateException ignored) {}
            }
            mediaPlayer.setOnCompletionListener(mp -> {
                resumePosition = 0; // 自然播完 → 下次从头播放
                sendBroadcast(new Intent(ACTION_SONG_COMPLETE));
            });
            mediaPlayer.start();
        }
    }

    // 保存当前进度并释放播放器(用于“停止”)
    private void saveAndRelease(){
        if(mediaPlayer != null){
            try {
                if(mediaPlayer.isPlaying()){
                    resumePosition = mediaPlayer.getCurrentPosition();

                }
                mediaPlayer.stop();
            } catch (IllegalStateException ignored) {
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    // 直接释放播放器(用于切换歌曲/销毁)
    private void releasePlayer(){
        if(mediaPlayer != null){
            try { mediaPlayer.stop(); } catch (IllegalStateException ignored) {}
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        releasePlayer();
    }
}
