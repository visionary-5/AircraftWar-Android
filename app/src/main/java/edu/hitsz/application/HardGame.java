package edu.hitsz.application;

import android.content.Context;
import edu.hitsz.aircraft.*;

/**
 * 困难模式游戏
 */
public class HardGame extends AbstractGame {

    private static final int DIFFICULTY_INCREASE_INTERVAL = 10000;
    private int lastDifficultyIncreaseTime = 0;
    private int bossAppearCount = 0;
    private static final int BOSS_HP_INCREMENT = 150;

    public HardGame(Context context, String difficulty, boolean soundEnabled) {
        super(context, difficulty, soundEnabled);
    }

    @Override
    protected void initGameParameters() {
        this.enemyMaxNumber = 7;
        this.cycleDuration = 400;
        this.heroShootPeriod = 200;
        this.eliteProbability = 0.4;
        this.mobEnemyHp = 40;
        this.eliteEnemyHp = 100;
        this.mobEnemySpeed = 12;
        this.eliteEnemySpeed = 10;
        this.bossScoreThreshold = 500;
        this.initialBossHp = 600;
    }

    @Override
    protected void generateEnemy() {
        if (Math.random() < (1 - eliteProbability)) {
            int mobEnemyWidth = ImageManager.MOB_ENEMY_IMAGE.getWidth();
            int locationX = (int) (Math.random() * (Main.screenWidth - mobEnemyWidth)) + mobEnemyWidth / 2;
            enemyAircrafts.add(mobEnemyFactory.createEnemy(locationX, (int) (Math.random() * Main.screenHeight * 0.05), 0, mobEnemySpeed, mobEnemyHp));
        } else {
            int eliteEnemyWidth = ImageManager.ELITE_ENEMY_IMAGE.getWidth();
            int locationX = (int) (Math.random() * (Main.screenWidth - eliteEnemyWidth));
            enemyAircrafts.add(eliteEnemyFactory.createEnemy(locationX, (int) (Math.random() * Main.screenHeight * 0.05), (int) (Math.random() * 4 - 2), eliteEnemySpeed, eliteEnemyHp));
        }
    }

    @Override
    protected void generateBoss() {
        bossAppearCount++;
        if (soundEnabled) {
            if (bgmThread != null) {
                bgmThread.stopMusic();
                bgmThread = null;
            }
            bgmThread = new MusicThread(getContext(), "src/videos/bgm_boss.wav", true);
            bgmThread.start();
        }
        int locationX = Main.screenWidth / 2;
        int locationY = ImageManager.BOSS_ENEMY_IMAGE.getHeight();
        enemyAircrafts.add(bossEnemyFactory.createEnemy(locationX, locationY, 5, 0, getBossHp()));
        bossExists = true;
        lastBossScore = score;
    }

    @Override
    protected boolean shouldIncreaseDifficulty() {
        return (time - lastDifficultyIncreaseTime) >= DIFFICULTY_INCREASE_INTERVAL;
    }

    @Override
    protected void increaseDifficulty() {
        lastDifficultyIncreaseTime = time;
        mobEnemyHp = (int) (mobEnemyHp * 1.15);
        eliteEnemyHp = (int) (eliteEnemyHp * 1.15);
        mobEnemySpeed = (int) (mobEnemySpeed * 1.10);
        eliteEnemySpeed = (int) (eliteEnemySpeed * 1.10);
        if (cycleDuration > 250) cycleDuration = Math.max(250, (int) (cycleDuration * 0.90));
        if (eliteProbability < 0.7) eliteProbability = Math.min(0.7, eliteProbability + 0.04);
        if (enemyMaxNumber < 10) enemyMaxNumber = Math.min(10, enemyMaxNumber + 1);
    }

    @Override
    protected int getBossHp() {
        return initialBossHp + BOSS_HP_INCREMENT * (bossAppearCount - 1);
    }
}
