package edu.hitsz.prop;

import edu.hitsz.aircraft.HeroAircraft;
import edu.hitsz.application.ImageManager;

/**
 * 加血道具类
 * 可使英雄机恢复一定血量，但不超过初始值
 *
 * @author hitsz
 */
public class BloodProp extends AbstractProp {

    private int bloodAmount = 100;

    public BloodProp(int locationX, int locationY, int speedX, int speedY) {
        super(locationX, locationY, speedX, speedY);
    }

    @Override
    public void activate(HeroAircraft heroAircraft) {
        // 恢复血量，但不超过最大血量
        int currentHp = heroAircraft.getHp();
        int maxHp = heroAircraft.getMaxHp();
        int newHp = Math.min(currentHp + bloodAmount, maxHp);
        heroAircraft.setHp(newHp);
        System.out.println("BloodSupply active! HP restored to " + newHp);
    }
}
