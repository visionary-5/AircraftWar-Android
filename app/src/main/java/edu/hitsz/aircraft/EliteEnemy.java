package edu.hitsz.aircraft;

import edu.hitsz.application.Main;
import edu.hitsz.shoot.StraightShootStrategy;
import edu.hitsz.observer.Observer;

/**
 * 精英敌机
 */
public class EliteEnemy extends AbstractAircraft implements Observer {

    private int score = 30;

    public EliteEnemy(int locationX, int locationY, int speedX, int speedY, int hp) {
        super(locationX, locationY, speedX, speedY, hp);
        this.shootStrategy = new StraightShootStrategy(1, 20, 1);
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
