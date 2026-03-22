package edu.hitsz.shoot;

import edu.hitsz.aircraft.AbstractAircraft;
import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.bullet.EnemyBullet;
import edu.hitsz.bullet.HeroBullet;

import java.util.LinkedList;
import java.util.List;

/**
 * 散射策略
 * 用于超级精英敌机、英雄机拾取火力道具后
 * 子弹呈扇形散开发射
 *
 * @author hitsz
 */
public class ScatterShootStrategy implements ShootStrategy {

    /**
     * 子弹一次发射数量
     */
    private final int shootNum;

    /**
     * 子弹伤害
     */
    private final int power;

    /**
     * 子弹射击方向 (向上发射：-1，向下发射：1)
     */
    private final int direction;

    /**
     * 散射角度（度数）- 暂未使用，保留兼容性
     */
    private final int scatterAngle;

    public ScatterShootStrategy(int shootNum, int power, int direction, int scatterAngle) {
        this.shootNum = shootNum;
        this.power = power;
        this.direction = direction;
        this.scatterAngle = scatterAngle;
    }

    @Override
    public List<BaseBullet> shoot(AbstractAircraft aircraft) {
        List<BaseBullet> res = new LinkedList<>();
        int x = aircraft.getLocationX();
        int y = aircraft.getLocationY() + direction * 2;
        int speedY = aircraft.getSpeedY() + direction * 5;

        BaseBullet bullet;

        // 散射弹道：3颗子弹，呈扇形分布
        // 中间子弹：垂直发射
        if (direction == -1) {
            // 英雄机向上射击
            bullet = new HeroBullet(x, y, 0, speedY, power);
        } else {
            // 敌机向下射击
            bullet = new EnemyBullet(x, y, 0, speedY, power);
        }
        res.add(bullet);

        // 左侧子弹：向左侧散射
        if (direction == -1) {
            bullet = new HeroBullet(x - 10, y, -3, speedY, power);
        } else {
            bullet = new EnemyBullet(x - 10, y, -3, speedY, power);
        }
        res.add(bullet);

        // 右侧子弹：向右侧散射
        if (direction == -1) {
            bullet = new HeroBullet(x + 10, y, 3, speedY, power);
        } else {
            bullet = new EnemyBullet(x + 10, y, 3, speedY, power);
        }
        res.add(bullet);

        return res;
    }
}
