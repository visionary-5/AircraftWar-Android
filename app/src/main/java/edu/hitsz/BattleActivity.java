package edu.hitsz;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import edu.hitsz.aircraft.HeroAircraft;
import edu.hitsz.application.Main;
import edu.hitsz.application.OnlineGame;
import edu.hitsz.dao.ScoreDaoSQLiteImpl;
import edu.hitsz.dto.ScoreRecord;
import edu.hitsz.network.BattleClient;

public class BattleActivity extends AppCompatActivity
        implements BattleClient.Listener, OnlineGame.BattleHook {

    public static final String EXTRA_HOST = "host";
    public static final String EXTRA_PORT = "port";
    public static final String EXTRA_SOUND = "sound";

    private static final String DIFFICULTY = "BATTLE";

    private BattleClient client;
    private OnlineGame game;
    private boolean soundEnabled;
    private boolean started = false;
    private boolean resultShown = false;
    private AlertDialog waitingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        Main.screenWidth = dm.widthPixels;
        Main.screenHeight = dm.heightPixels;

        String host = getIntent().getStringExtra(EXTRA_HOST);
        int port = getIntent().getIntExtra(EXTRA_PORT, 9999);
        soundEnabled = getIntent().getBooleanExtra(EXTRA_SOUND, true);
        if (host == null || host.isEmpty()) host = "10.0.2.2";

        Toast.makeText(this, "正在连接服务器 " + host + ":" + port, Toast.LENGTH_SHORT).show();

        client = new BattleClient(host, port, this);
        client.connect();
    }

    @Override
    public void onWaiting() {
        Toast.makeText(this, "已连接，等待对手加入…", Toast.LENGTH_SHORT).show();
        if (waitingDialog == null || !waitingDialog.isShowing()) {
            waitingDialog = new AlertDialog.Builder(this)
                    .setTitle("等待对手加入")
                    .setMessage("已连接服务器，正在等待第二个玩家进入同一端口。")
                    .setCancelable(false)
                    .setNegativeButton("取消", (d, w) -> finish())
                    .create();
            waitingDialog.show();
        }
    }

    @Override
    public void onBattleStart() {
        if (started) return;
        started = true;
        dismissWaitingDialog();
        Toast.makeText(this, "对手已就绪，开始对战！", Toast.LENGTH_SHORT).show();
        HeroAircraft.resetInstance();
        game = new OnlineGame(this, DIFFICULTY, soundEnabled, client);
        game.setBattleHook(this);
        setContentView(game);
    }

    @Override
    public void onOpponentScore(int score) {
        if (game != null) game.onOpponentScore(score);
    }

    @Override
    public void onOpponentDead() {
        if (game != null) game.onOpponentDead();
    }

    @Override
    public void onBattleEnded(int myFinalScore, int opponentFinalScore) {
        if (game != null) game.onBattleEndedRemote(myFinalScore, opponentFinalScore);
        else onBothDead(myFinalScore, opponentFinalScore);
    }

    @Override
    public void onOpponentLeft() {
        if (game != null) game.onOpponentLeftRemote();
        else runOnUiThread(this::showOpponentLeftDialog);
    }

    @Override
    public void onError(String message) {
        runOnUiThread(() -> {
            dismissWaitingDialog();
            new AlertDialog.Builder(this)
                    .setTitle("连接失败")
                    .setMessage(message)
                    .setCancelable(false)
                    .setPositiveButton("返回", (d, w) -> finish())
                    .show();
        });
    }

    @Override
    public void onSelfDead(int myScore, int opponentScoreSoFar) {
        if (resultShown) return;
        if (waitingDialog != null && waitingDialog.isShowing()) return;
        waitingDialog = new AlertDialog.Builder(this)
                .setTitle("你已阵亡")
                .setMessage("我方得分：" + myScore + "\n对手得分：" + opponentScoreSoFar
                        + "\n\n等待对手阵亡…")
                .setCancelable(false)
                .setNegativeButton("放弃对战", (d, w) -> finish())
                .create();
        waitingDialog.show();
    }

    @Override
    public void onBothDead(int myScore, int opponentScore) {
        if (resultShown) return;
        resultShown = true;
        dismissWaitingDialog();
        // 把本局得分写入排行榜
        String time = new java.text.SimpleDateFormat("MM-dd HH:mm", java.util.Locale.getDefault())
                .format(new java.util.Date());
        new ScoreDaoSQLiteImpl(this).insertScore(
                new ScoreRecord("Player", myScore, time), DIFFICULTY);

        String winner;
        if (myScore > opponentScore) winner = "你赢了！";
        else if (myScore < opponentScore) winner = "你输了！";
        else winner = "平局！";

        new AlertDialog.Builder(this)
                .setTitle("对战结束 - " + winner)
                .setMessage("我方得分：" + myScore + "\n对手得分：" + opponentScore)
                .setCancelable(false)
                .setPositiveButton("查看排行榜", (d, w) -> {
                    Intent it = new Intent(this, LeaderboardActivity.class);
                    it.putExtra(LeaderboardActivity.EXTRA_DIFFICULTY, DIFFICULTY);
                    startActivity(it);
                    finish();
                })
                .setNegativeButton("返回主菜单", (d, w) -> finish())
                .show();
    }

    private void showOpponentLeftDialog() {
        if (resultShown) return;
        resultShown = true;
        dismissWaitingDialog();
        new AlertDialog.Builder(this)
                .setTitle("对手已离线")
                .setMessage("对手退出了对战。")
                .setCancelable(false)
                .setPositiveButton("返回", (d, w) -> finish())
                .show();
    }

    private void dismissWaitingDialog() {
        if (waitingDialog != null) {
            waitingDialog.dismiss();
            waitingDialog = null;
        }
    }

    @Override
    protected void onDestroy() {
        if (client != null) client.disconnect();
        super.onDestroy();
    }
}
