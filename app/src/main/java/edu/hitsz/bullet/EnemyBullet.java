package edu.hitsz.bullet;

import edu.hitsz.observer.Observer;

/**
 * @Author hitsz
 */
public class EnemyBullet extends BaseBullet implements Observer {

    public EnemyBullet(int locationX, int locationY, int speedX, int speedY, int power) {
        super(locationX, locationY, speedX, speedY, power);
    }

    @Override
    public void update() {
        // 炸弹爆炸时，敌机子弹直接消失
        vanish();
    }
}
