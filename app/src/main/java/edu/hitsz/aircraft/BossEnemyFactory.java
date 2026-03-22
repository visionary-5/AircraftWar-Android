package edu.hitsz.aircraft;

/**
 * Boss敌机工厂
 * 用于生产Boss敌机
 * @author hitsz
 */
public class BossEnemyFactory implements EnemyFactory {

    @Override
    public AbstractAircraft createEnemy(int locationX, int locationY, int speedX, int speedY, int hp) {
        return new BossEnemy(locationX, locationY, speedX, speedY, hp);
    }
}

