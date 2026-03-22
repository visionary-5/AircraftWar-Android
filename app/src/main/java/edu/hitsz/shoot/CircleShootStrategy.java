package edu.hitsz.shoot;

import edu.hitsz.aircraft.AbstractAircraft;
import edu.hitsz.aircraft.HeroAircraft;
import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.bullet.EnemyBullet;
import edu.hitsz.bullet.HeroBullet;

import java.util.LinkedList;
import java.util.List;

/**
 * 环形射击策略
 * 用于Boss敌机、英雄机拾取超级火力道具后
 * 子弹向360度环形发射
 *
 * @author hitsz
 */
public class CircleShootStrategy implements ShootStrategy {

    /**
     * 子弹一次发射数量
     */
    private final int shootNum;

    /**
     * 子弹伤害
     */
    private final int power;

    public CircleShootStrategy(int shootNum, int power) {
        this.shootNum = shootNum;
        this.power = power;
    }

    @Override
    public List<BaseBullet> shoot(AbstractAircraft aircraft) {
        List<BaseBullet> res = new LinkedList<>();
        int x = aircraft.getLocationX();
        int y = aircraft.getLocationY();

        // 环射弹道：发射子弹，均匀分布在360度圆周上
        double angleStep = 2 * Math.PI / shootNum; // 每颗子弹之间的角度差
        int bulletSpeed = 6; // 子弹速度

        BaseBullet bullet;
        for (int i = 0; i < shootNum; i++) {
            double angle = i * angleStep;
            // 计算子弹的x和y方向速度分量
            int speedX = (int) (bulletSpeed * Math.cos(angle));
            int speedY = (int) (bulletSpeed * Math.sin(angle));

            // 判断是英雄机还是敌机
            if (aircraft instanceof HeroAircraft) {
                // 英雄机发射英雄子弹
                bullet = new HeroBullet(x, y, speedX, speedY, power);
            } else {
                // 敌机发射敌机子弹
                bullet = new EnemyBullet(x, y, speedX, speedY, power);
            }
            res.add(bullet);
        }
        return res;
    }
}
