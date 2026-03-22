package edu.hitsz.aircraft;

import edu.hitsz.shoot.ScatterShootStrategy;

/**
 * 超级精英敌机
 */
public class ElitePlusEnemy extends EliteEnemy {

    private int damage = 30;

    public ElitePlusEnemy(int locationX, int locationY, int speedX, int speedY, int hp) {
        super(locationX, locationY, speedX, speedY, hp);
        this.shootStrategy = new ScatterShootStrategy(3, 20, 1, 30);
    }

    @Override
    public void update() {
        decreaseHp(damage);
    }
}
