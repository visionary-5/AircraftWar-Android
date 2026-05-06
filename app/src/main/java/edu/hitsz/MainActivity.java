package edu.hitsz;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import edu.hitsz.aircraft.HeroAircraft;
import edu.hitsz.application.EasyGame;
import edu.hitsz.application.HardGame;
import edu.hitsz.application.Main;
import edu.hitsz.application.NormalGame;
import edu.hitsz.application.AbstractGame;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showMenu();
    }

    private void showMenu() {
        setContentView(R.layout.activity_main);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        Main.screenWidth = dm.widthPixels;
        Main.screenHeight = dm.heightPixels;

        Button btnEasy = findViewById(R.id.btn_easy);
        Button btnNormal = findViewById(R.id.btn_normal);
        Button btnHard = findViewById(R.id.btn_hard);
        CheckBox cbSound = findViewById(R.id.cb_sound);

        btnEasy.setOnClickListener(v -> startGame("EASY", cbSound.isChecked()));
        btnNormal.setOnClickListener(v -> startGame("NORMAL", cbSound.isChecked()));
        btnHard.setOnClickListener(v -> startGame("HARD", cbSound.isChecked()));

        Button btnRankEasy = findViewById(R.id.btn_rank_easy);
        Button btnRankNormal = findViewById(R.id.btn_rank_normal);
        Button btnRankHard = findViewById(R.id.btn_rank_hard);

        btnRankEasy.setOnClickListener(v -> showLeaderboard("EASY"));
        btnRankNormal.setOnClickListener(v -> showLeaderboard("NORMAL"));
        btnRankHard.setOnClickListener(v -> showLeaderboard("HARD"));

        Button btnBattle = findViewById(R.id.btn_battle);
        if (btnBattle != null) {
            btnBattle.setOnClickListener(v -> showBattleDialog(cbSound.isChecked()));
        }

        Button btnRankBattle = findViewById(R.id.btn_rank_battle);
        if (btnRankBattle != null) {
            btnRankBattle.setOnClickListener(v -> showLeaderboard("BATTLE"));
        }
    }

    private void startGame(String difficulty, boolean soundEnabled) {
        HeroAircraft.resetInstance();

        AbstractGame game;
        switch (difficulty) {
            case "EASY":
                game = new EasyGame(this, difficulty, soundEnabled);
                break;
            case "HARD":
                game = new HardGame(this, difficulty, soundEnabled);
                break;
            case "NORMAL":
            default:
                game = new NormalGame(this, difficulty, soundEnabled);
                break;
        }
        setContentView(game);
    }

    private void showLeaderboard(String difficulty) {
        Intent intent = new Intent(this, LeaderboardActivity.class);
        intent.putExtra(LeaderboardActivity.EXTRA_DIFFICULTY, difficulty);
        startActivity(intent);
    }

    private void showBattleDialog(boolean soundEnabled) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        layout.setPadding(padding, padding, padding, padding);

        EditText etHost = new EditText(this);
        etHost.setHint("服务器IP");
        etHost.setText("10.0.2.2");
        etHost.setInputType(InputType.TYPE_CLASS_TEXT);

        EditText etPort = new EditText(this);
        etPort.setHint("端口");
        etPort.setText("9999");
        etPort.setInputType(InputType.TYPE_CLASS_NUMBER);

        layout.addView(etHost);
        layout.addView(etPort);

        new AlertDialog.Builder(this)
                .setTitle("联机对战")
                .setView(layout)
                .setPositiveButton("开始对战", (d, w) -> {
                    String host = etHost.getText().toString().trim();
                    int port;
                    try {
                        port = Integer.parseInt(etPort.getText().toString().trim());
                    } catch (NumberFormatException ex) {
                        port = 9999;
                    }
                    Intent intent = new Intent(this, BattleActivity.class);
                    intent.putExtra(BattleActivity.EXTRA_HOST, host);
                    intent.putExtra(BattleActivity.EXTRA_PORT, port);
                    intent.putExtra(BattleActivity.EXTRA_SOUND, soundEnabled);
                    startActivity(intent);
                })
                .setNegativeButton("取消", null)
                .show();
    }

    public void returnToMenu() {
        showMenu();
    }
}
