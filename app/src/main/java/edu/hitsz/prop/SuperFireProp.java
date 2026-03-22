package edu.hitsz.prop;

import edu.hitsz.aircraft.HeroAircraft;
import edu.hitsz.shoot.CircleShootStrategy;
import edu.hitsz.shoot.StraightShootStrategy;

/**
 * 超级火力道具类
 * 大幅提升英雄机火力，弹道由直射切换为环射，持续3秒后恢复直射
 *
 * @author hitsz
 */
public class SuperFireProp extends AbstractProp {

    public SuperFireProp(int locationX, int locationY, int speedX, int speedY) {
        super(locationX, locationY, speedX, speedY);
    }

    @Override
    public void activate(HeroAircraft heroAircraft) {
        System.out.println("SuperFireSupply active! 英雄机切换为环射弹道");
        // 切换为环射策略：20颗子弹，威力30
        heroAircraft.setShootStrategy(new CircleShootStrategy(20, 30));

        // 启动定时器线程，3秒后恢复直射状态
        new Thread(() -> {
            try {
                Thread.sleep(3000); // 持续3秒
                System.out.println("环射弹道效果结束，恢复直射状态");
                // 恢复为直射策略：1颗子弹，威力30，向上发射（direction=-1）
                heroAircraft.setShootStrategy(new StraightShootStrategy(1, 30, -1));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
