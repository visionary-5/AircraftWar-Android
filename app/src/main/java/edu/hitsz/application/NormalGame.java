package edu.hitsz.application;

import android.content.Context;
import edu.hitsz.aircraft.*;

/**
 * 普通模式游戏
 */
public class NormalGame extends AbstractGame {

    private static final int DIFFICULTY_INCREASE_INTERVAL = 15000;
    private int lastDifficultyIncreaseTime = 0;
    private int bossAppearCount = 0;

    public NormalGame(Context context, String difficulty, boolean soundEnabled) {
        super(context, difficulty, soundEnabled);
    }

    @Override
    protected void initGameParameters() {
        this.enemyMaxNumber = 5;
        this.cycleDuration = 600;
        this.heroShootPeriod = 300;
        this.eliteProbability = 0.3;
        this.mobEnemyHp = 30;
        this.eliteEnemyHp = 80;
        this.mobEnemySpeed = 10;
        this.eliteEnemySpeed = 8;
        this.bossScoreThreshold = 600;
        this.initialBossHp = 500;
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
            // 背景音乐切换由具体的 Activity 或音乐管理器处理，这里暂时留空或调用简化的 MusicThread
            new MusicThread(getContext(), "src/videos/bgm_boss.wav", true).start();
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
        mobEnemyHp = (int) (mobEnemyHp * 1.10);
        eliteEnemyHp = (int) (eliteEnemyHp * 1.10);
        mobEnemySpeed = (int) (mobEnemySpeed * 1.08);
        eliteEnemySpeed = (int) (eliteEnemySpeed * 1.08);
        if (cycleDuration > 400) cycleDuration = Math.max(400, (int) (cycleDuration * 0.92));
        if (eliteProbability < 0.5) eliteProbability = Math.min(0.5, eliteProbability + 0.03);
        if (enemyMaxNumber < 7) enemyMaxNumber = Math.min(7, enemyMaxNumber + 1);
    }

    @Override
    protected int getBossHp() {
        return initialBossHp;
    }
}
