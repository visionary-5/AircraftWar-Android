package edu.hitsz.aircraft;

/**
 * 敌机工厂接口
 * 充当创建者角色
 * @author hitsz
 */
public interface EnemyFactory {
    /**
     * 创建敌机
     * @param locationX 敌机初始x坐标
     * @param locationY 敌机初始y坐标
     * @param speedX 敌机x方向速度
     * @param speedY 敌机y方向速度
     * @param hp 敌机生命值
     * @return 创建的敌机对象
     */
    AbstractAircraft createEnemy(int locationX, int locationY, int speedX, int speedY, int hp);
}
