package edu.hitsz.aircraft;

/**
 * 普通敌机工厂
 * 具体创建者角色，负责创建普通敌机
 * @author hitsz
 */
public class MobEnemyFactory implements EnemyFactory {
    @Override
    public AbstractAircraft createEnemy(int locationX, int locationY, int speedX, int speedY, int hp) {
        return new MobEnemy(locationX, locationY, speedX, speedY, hp);
    }
}
