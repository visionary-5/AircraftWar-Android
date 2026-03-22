package edu.hitsz.aircraft;

import edu.hitsz.application.Main;
import edu.hitsz.shoot.NoShootStrategy;
import edu.hitsz.observer.Observer;

/**
 * 普通敌机
 */
public class MobEnemy extends AbstractAircraft implements Observer {

    private int score = 10;

    public MobEnemy(int locationX, int locationY, int speedX, int speedY, int hp) {
        super(locationX, locationY, speedX, speedY, hp);
        this.shootStrategy = new NoShootStrategy();
    }

    @Override
    public void forward() {
        super.forward();
        if (locationY >= Main.screenHeight) {
            vanish();
        }
    }

    @Override
    public void update() {
        vanish();
    }

    public int getScore() {
        return score;
    }
}
