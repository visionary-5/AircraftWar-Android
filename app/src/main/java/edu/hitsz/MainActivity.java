package edu.hitsz;

import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.Button;
import android.widget.CheckBox;
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

    public void returnToMenu() {
        showMenu();
    }
}
