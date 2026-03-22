package edu.hitsz.aircraft;

/**
 * 精英敌机工厂
 * 具体创建者角色，负责创建精英敌机
 * @author hitsz
 */
public class EliteEnemyFactory implements EnemyFactory {
    @Override
    public AbstractAircraft createEnemy(int locationX, int locationY, int speedX, int speedY, int hp) {
        return new EliteEnemy(locationX, locationY, speedX, speedY, hp);
    }
}
