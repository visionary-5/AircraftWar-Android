package edu.hitsz.application;

import android.content.Context;
import android.media.MediaPlayer;
import java.util.HashMap;
import java.util.Map;
import edu.hitsz.R;

/**
 * 音乐播放类 (Android 适配版)
 * 使用 MediaPlayer 实现
 */
public class MusicThread extends Thread {

    private Context context;
    private int resId;
    private boolean loop;
    private MediaPlayer mediaPlayer;

    // 映射文件名到资源ID (为了兼容原有的 String 路径调用)
    private static final Map<String, Integer> soundMap = new HashMap<>();
    static {
        soundMap.put("src/videos/bgm.wav", R.raw.bgm);
        soundMap.put("src/videos/bgm_boss.wav", R.raw.bgm_boss);
        soundMap.put("src/videos/bullet_hit.wav", R.raw.bullet_hit);
        soundMap.put("src/videos/game_over.wav", R.raw.game_over);
        soundMap.put("src/videos/get_supply.wav", R.raw.get_supply);
        soundMap.put("src/videos/bomb_explosion.wav", R.raw.bomb_explosion);
    }

    public MusicThread(Context context, String filename) {
        this(context, filename, false);
    }

    public MusicThread(Context context, String filename, boolean loop) {
        this.context = context;
        this.loop = loop;
        Integer id = soundMap.get(filename);
        this.resId = (id != null) ? id : R.raw.bgm;
    }

    @Override
    public void run() {
        mediaPlayer = MediaPlayer.create(context, resId);
        if (mediaPlayer == null) return;
        mediaPlayer.setLooping(loop);
        mediaPlayer.start();
        
        mediaPlayer.setOnCompletionListener(mp -> {
            if (!loop) {
                stopMusic();
            }
        });
    }

    public void stopMusic() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
