package edu.hitsz.shoot;

import edu.hitsz.aircraft.AbstractAircraft;
import edu.hitsz.bullet.BaseBullet;

import java.util.List;

/**
 * 射击策略接口
 * 定义所有射击策略的统一接口
 *
 * @author hitsz
 */
public interface ShootStrategy {
    /**
     * 射击方法
     * @param aircraft 执行射击的飞机对象
     * @return 射击产生的子弹列表
     */
    List<BaseBullet> shoot(AbstractAircraft aircraft);
}

