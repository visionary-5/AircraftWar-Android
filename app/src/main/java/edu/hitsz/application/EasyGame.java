package edu.hitsz.application;

import android.content.Context;

/**
 * 简单模式游戏
 */
public class EasyGame extends AbstractGame {

    public EasyGame(Context context, String difficulty, boolean soundEnabled) {
        super(context, difficulty, soundEnabled);
    }

    @Override
    protected void initGameParameters() {
        this.enemyMaxNumber = 3;
        this.cycleDuration = 800;
        this.heroShootPeriod = 400;
        this.eliteProbability = 0.2;
        this.mobEnemyHp = 20;
        this.eliteEnemyHp = 60;
        this.mobEnemySpeed = 8;
        this.eliteEnemySpeed = 6;
        this.bossScoreThreshold = 0;
        this.initialBossHp = 0;
    }

    @Override
    protected void generateEnemy() {
        if (Math.random() < (1 - eliteProbability)) {
            int mobEnemyWidth = ImageManager.MOB_ENEMY_IMAGE.getWidth();
            int locationX = (int) (Math.random() * (Main.screenWidth - mobEnemyWidth)) + mobEnemyWidth / 2;
            enemyAircrafts.add(mobEnemyFactory.createEnemy(
                    locationX,
                    (int) (Math.random() * Main.screenHeight * 0.05),
                    0,
                    mobEnemySpeed,
                    mobEnemyHp
            ));
        } else {
            int eliteEnemyWidth = ImageManager.ELITE_ENEMY_IMAGE.getWidth();
            int locationX = (int) (Math.random() * (Main.screenWidth - eliteEnemyWidth));
            enemyAircrafts.add(eliteEnemyFactory.createEnemy(
                    locationX,
                    (int) (Math.random() * Main.screenHeight * 0.05),
                    (int) (Math.random() * 4 - 2),
                    eliteEnemySpeed,
                    eliteEnemyHp
            ));
        }
    }

    @Override
    protected void generateBoss() {
    }

    @Override
    protected boolean shouldIncreaseDifficulty() {
        return false;
    }

    @Override
    protected void increaseDifficulty() {
    }

    @Override
    protected int getBossHp() {
        return 0;
    }
}
