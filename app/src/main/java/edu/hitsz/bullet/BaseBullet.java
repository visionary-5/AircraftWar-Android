package edu.hitsz.bullet;

import edu.hitsz.application.Main;
import edu.hitsz.basic.AbstractFlyingObject;

/**
 * 子弹类
 */
public abstract class BaseBullet extends AbstractFlyingObject {

    private int power = 10;

    public BaseBullet(int locationX, int locationY, int speedX, int speedY, int power) {
        super(locationX, locationY, speedX, speedY);
        this.power = power;
    }

    @Override
    public void forward() {
        locationX += speedX;
        locationY += speedY;

        // 判定 x 轴出界
        if (locationX <= 0 || locationX >= Main.screenWidth) {
            vanish();
        }

        // 判定 y 轴出界
        if (speedY > 0 && locationY >= Main.screenHeight ) {
            vanish();
        } else if (locationY <= 0){
            vanish();
        }
    }

    public int getPower() {
        return power;
    }
}
