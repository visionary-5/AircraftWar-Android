package edu.hitsz.shoot;

import edu.hitsz.aircraft.AbstractAircraft;
import edu.hitsz.bullet.BaseBullet;

import java.util.LinkedList;
import java.util.List;

/**
 * 不射击策略
 * 用于普通敌机（MobEnemy）
 *
 * @author hitsz
 */
public class NoShootStrategy implements ShootStrategy {

    @Override
    public List<BaseBullet> shoot(AbstractAircraft aircraft) {
        // 返回空列表，不发射子弹
        return new LinkedList<>();
    }
}

