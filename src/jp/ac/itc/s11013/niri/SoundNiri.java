
package jp.ac.itc.s11013.niri;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;

public class SoundNiri {
    private Context context;
    private AudioManager audio;
    private SoundPool sound;
    private int[] raw;

    public SoundNiri(Context context) {
        this.context = context;
        load();
    }

    public void load() {
        raw = new int[4];
        // オーディオサービスの取得
        audio = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        // 音声ファイルのロード
        sound = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
        raw[0] = sound.load(context, R.raw.niri, 0);
        raw[1] = sound.load(context, R.raw.sininiri, 0);
        raw[2] = sound.load(context, R.raw.deji_niri1, 0);
        raw[3] = sound.load(context, R.raw.maru_niri1, 0);
    }

    public void unload() {
        for (int i : raw) {
            sound.stop(i);
            sound.unload(i);
        }
        sound.release();
    }

    public void play(int id) {
        // 着信音の最大値音量を取得
        int max_volume = audio.getStreamMaxVolume(AudioManager.STREAM_RING);
        // 現在の音量を取得
        int current_volume = audio.getStreamVolume(AudioManager.STREAM_RING);
        // ボリュームを計算
        float volume = (float) current_volume / max_volume;
        // サウンドの再生
        sound.play(raw[id], volume, volume, 1, 0, 1.0F);
    }
}