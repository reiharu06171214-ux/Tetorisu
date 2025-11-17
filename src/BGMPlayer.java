import java.io.File;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

public class BGMPlayer {

    private Clip clip;

    // BGMを読み込む（load：ロード）
    public void load(String filepath) {
        try {
            File file = new File(filepath);
            AudioInputStream ais = AudioSystem.getAudioInputStream(file);

            clip = AudioSystem.getClip();
            clip.open(ais);

        } catch (Exception e) {
            System.out.println("BGMの読み込みに失敗しました: " + filepath);
            e.printStackTrace();
        }
    }

    // ループ再生（playLoop：プレイ・ループ）
    public void playLoop() {
        try {
            if (clip != null) {
                clip.stop();             // 念のため一度止める
                clip.setFramePosition(0); // 再生位置を最初に戻す
                clip.loop(Clip.LOOP_CONTINUOUSLY); // 無限ループ
                clip.start();           // 再生開始
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 一度だけ再生（playOne：プレイ・ワン）
    public void playOne() {
        try {
            if (clip != null) {
                clip.stop();
                clip.setFramePosition(0);
                clip.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 停止（stop：ストップ）
    public void stop() {
        try {
            if (clip != null && clip.isRunning()) {
                clip.stop();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 完全に閉じる（close：クローズ）
    public void close() {
        try {
            if (clip != null) {
                clip.stop();
                clip.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
