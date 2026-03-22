package edu.hitsz.aircraft;

import edu.hitsz.shoot.StraightShootStrategy;

/**
 * 英雄飞机，游戏玩家操控
 * 采用单例模式，确保游戏中只有一个英雄机实例
 * @author hitsz
 */
public class HeroAircraft extends AbstractAircraft {

    /**
     * 单例模式 - 静态实例变量
     */
    private static HeroAircraft instance = null;

    /**
     * 私有构造函数，防止外部实例化
     * @param locationX 英雄机位置x坐标
     * @param locationY 英雄机位置y坐标
     * @param speedX 英雄机射出的子弹的基准速度（英雄机无特定速度）
     * @param speedY 英雄机射出的子弹的基准速度（英雄机无特定速度）
     * @param hp    初始生命值
     */
    private HeroAircraft(int locationX, int locationY, int speedX, int speedY, int hp) {
        super(locationX, locationY, speedX, speedY, hp);
        // 初始化为直线射击策略：1颗子弹，威力30，向上发射（direction=-1）
        this.shootStrategy = new StraightShootStrategy(1, 30, -1);
    }

    /**
     * 懒汉式单例模式 - 获取唯一实例（无参版本）
     * @return HeroAircraft唯一实例
     */
    public static synchronized HeroAircraft getInstance() {
        if (instance == null) {
            // 使用默认参数创建实例
            instance = new HeroAircraft(400, 550, 0, 0, 1000);
        }
        return instance;
    }

    /**
     * 懒汉式单例模式 - 获取唯一实例（带参版本）
     * @param locationX 英雄机位置x坐标
     * @param locationY 英雄机位置y坐标
     * @param speedX 英雄机射出的子弹的基准速度
     * @param speedY 英雄机射出的子弹的基准速度
     * @param hp 初始生命值
     * @return HeroAircraft唯一实例
     */
    public static synchronized HeroAircraft getInstance(int locationX, int locationY, int speedX, int speedY, int hp) {
        if (instance == null) {
            instance = new HeroAircraft(locationX, locationY, speedX, speedY, hp);
        }
        return instance;
    }

    /**
     * 重置单例实例（仅用于测试）
     * 将单例实例设置为null，允许重新创建实例
     */
    public static synchronized void resetInstance() {
        instance = null;
    }

    @Override
    public void forward() {
        // 英雄机由鼠标控制，不通过forward函数移动
        // 不执行父类的forward逻辑，避免边界反向导致的异常移动
    }

    /**
     * 获取最大血量
     * @return 最大血量
     */
    public int getMaxHp() {
        return maxHp;
    }

    /**
     * 设置当前血量
     * @param hp 血量值
     */
    public void setHp(int hp) {
        this.hp = hp;
    }

}
