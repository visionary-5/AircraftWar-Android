package edu.hitsz.application;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Looper;

import edu.hitsz.network.BattleClient;

/**
 * 联机对战模式：在普通模式基础上，
 *   - 通过 BattleClient 实时上报本机得分；
 *   - 在屏幕右上角同步绘制对手得分；
 *   - 自身阵亡时通知服务器，并由 Activity 等待对手阵亡后展示最终结果。
 */
public class OnlineGame extends NormalGame {

    public interface BattleHook {
        void onSelfDead(int myScore, int opponentScoreSoFar);
        void onBothDead(int myScore, int opponentScore);
        void onOpponentLeft();
    }

    private final BattleClient client;
    private BattleHook battleHook;

    private int lastReportedScore = -1;
    private volatile int opponentScore = 0;
    private volatile boolean opponentDead = false;
    private volatile boolean selfDead = false;
    private volatile boolean deadSent = false;
    private volatile boolean ended = false;

    public OnlineGame(Context context, String difficulty, boolean soundEnabled, BattleClient client) {
        super(context, difficulty, soundEnabled);
        this.client = client;
    }

    public void setBattleHook(BattleHook hook) {
        this.battleHook = hook;
    }

    public void onOpponentScore(int score) {
        this.opponentScore = score;
    }

    public void onOpponentDead() {
        this.opponentDead = true;
    }

    public void onBattleEndedRemote(int myFinal, int oppFinal) {
        if (ended) return;
        ended = true;
        if (battleHook != null) {
            new Handler(Looper.getMainLooper()).post(() -> battleHook.onBothDead(myFinal, oppFinal));
        }
    }

    public void onOpponentLeftRemote() {
        if (battleHook != null) {
            new Handler(Looper.getMainLooper()).post(() -> battleHook.onOpponentLeft());
        }
    }

    @Override
    protected void postProcessAction() {
        super.postProcessAction();
        int current = getScore();
        if (current != lastReportedScore) {
            lastReportedScore = current;
            if (client != null) client.sendScore(current);
        }
    }

    @Override
    protected void paintScoreAndLife(Canvas canvas) {
        super.paintScoreAndLife(canvas);
        Paint paint = getPaint();
        paint.setColor(Color.YELLOW);
        paint.setTextSize(40);
        paint.setFakeBoldText(true);
        String oppText = "OPPONENT:" + opponentScore + (opponentDead ? " (DEAD)" : "");
        float textWidth = paint.measureText(oppText);
        canvas.drawText(oppText, Main.screenWidth - textWidth - 30, 60, paint);
    }

    @Override
    protected void gameOver() {
        if (selfDead) return;
        selfDead = true;
        if (bgmThread != null) {
            bgmThread.stopMusic();
            bgmThread = null;
        }
        if (soundEnabled) {
            new MusicThread(getContext(), "src/videos/game_over.wav").start();
        }
        if (!deadSent && client != null) {
            deadSent = true;
            client.sendDead();
        }
        if (battleHook != null) {
            final int my = getScore();
            final int opp = opponentScore;
            new Handler(Looper.getMainLooper()).post(() -> battleHook.onSelfDead(my, opp));
        }
    }
}
