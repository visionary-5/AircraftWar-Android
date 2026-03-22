package edu.hitsz.shoot;

import edu.hitsz.aircraft.AbstractAircraft;
import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.bullet.EnemyBullet;
import edu.hitsz.bullet.HeroBullet;

import java.util.LinkedList;
import java.util.List;

/**
 * 直线射击策略
 * 用于英雄机初始状态、精英敌机
 *
 * @author hitsz
 */
public class StraightShootStrategy implements ShootStrategy {

    /**
     * 子弹一次发射数量
     */
    private int shootNum;

    /**
     * 子弹伤害
     */
    private int power;

    /**
     * 子弹射击方向 (向上发射：-1，向下发射：1)
     */
    private int direction;

    public StraightShootStrategy(int shootNum, int power, int direction) {
        this.shootNum = shootNum;
        this.power = power;
        this.direction = direction;
    }

    @Override
    public List<BaseBullet> shoot(AbstractAircraft aircraft) {
        List<BaseBullet> res = new LinkedList<>();
        int x = aircraft.getLocationX();
        int y = aircraft.getLocationY() + direction * 2;
        int speedX = 0;
        int speedY = aircraft.getSpeedY() + direction * 5;

        BaseBullet bullet;
        for (int i = 0; i < shootNum; i++) {
            // 子弹发射位置相对飞机位置向前偏移
            // 多个子弹横向分散
            if (direction == -1) {
                // 英雄机向上射击
                bullet = new HeroBullet(x + (i * 2 - shootNum + 1) * 10, y, speedX, speedY, power);
            } else {
                // 敌机向下射击
                bullet = new EnemyBullet(x + (i * 2 - shootNum + 1) * 10, y, speedX, speedY, power);
            }
            res.add(bullet);
        }
        return res;
    }
}

