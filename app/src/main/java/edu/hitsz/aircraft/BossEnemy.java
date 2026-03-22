package edu.hitsz.aircraft;

import edu.hitsz.shoot.CircleShootStrategy;
import edu.hitsz.observer.Observer;

/**
 * Boss敌机
 * 实现环射弹道：同时发射20颗子弹，呈环形
 * 悬浮于界面上方左右移动
 *
 * @author hitsz
 */
public class BossEnemy extends AbstractAircraft implements Observer {

    /**
     * 出现次数计数器
     */
    private static int bossCount = 0;

    private int score = 100;

    public BossEnemy(int locationX, int locationY, int speedX, int speedY, int hp) {
        super(locationX, locationY, speedX, speedY, hp);
        // 初始化为环射策略：20颗子弹，威力20（降低伤害避免英雄机瞬间死亡）
        this.shootStrategy = new CircleShootStrategy(20, 20);
        bossCount++;
    }

    @Override
    public void forward() {
        super.forward();

        // Boss机悬浮于界面上方，只做左右移动
        // 当触碰边界时会自动反向（在AbstractFlyingObject中已实现）
    }

    @Override
    public void update() {
        // 炸弹爆炸时，Boss敌机不受影响
        // 什么都不做
    }

    /**
     * 获取Boss出现次数
     *
     * @return Boss出现次数
     */
    public static int getBossCount() {
        return bossCount;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }
}
