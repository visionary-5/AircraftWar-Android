package edu.hitsz.aircraft;

/**
 * 超级精英敌机工厂
 * 用于生产超级精英敌机
 * @author hitsz
 */
public class ElitePlusEnemyFactory implements EnemyFactory {

    @Override
    public AbstractAircraft createEnemy(int locationX, int locationY, int speedX, int speedY, int hp) {
        return new ElitePlusEnemy(locationX, locationY, speedX, speedY, hp);
    }
}

