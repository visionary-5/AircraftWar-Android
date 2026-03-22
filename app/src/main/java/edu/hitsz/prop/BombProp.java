package edu.hitsz.prop;

import edu.hitsz.aircraft.HeroAircraft;
import edu.hitsz.aircraft.AbstractAircraft;
import edu.hitsz.observer.Observer;

import java.util.ArrayList;
import java.util.List;

/**
 * 炸弹道具类
 * 清除屏幕上的所有敌机和敌机子弹
 *
 * @author hitsz
 */
public class BombProp extends AbstractProp {

    /**
     * 观察者列表
     */
    private List<Observer> observers = new ArrayList<>();

    public BombProp(int locationX, int locationY, int speedX, int speedY) {
        super(locationX, locationY, speedX, speedY);
    }

    /**
     * 注册观察者
     * @param observer 观察者对象
     */
    public void registerObserver(Observer observer) {
        observers.add(observer);
    }

    /**
     * 移除观察者
     * @param observer 观察者对象
     */
    public void removeObserver(Observer observer) {
        observers.remove(observer);
    }

    /**
     * 通知所有观察者并返回被炸毁的敌机分数总和
     * @return 被炸毁的敌机分数总和
     */
    public int notifyObservers() {
        int totalScore = 0;
        for (Observer observer : observers) {
            // 在通知前，如果是敌机，先获取分数
            if (observer instanceof AbstractAircraft) {
                AbstractAircraft aircraft = (AbstractAircraft) observer;
                // 只有在敌机还有效时才计算分数
                if (!aircraft.notValid()) {
                    totalScore += getAircraftScore(aircraft);
                }
            }
            observer.update();
        }
        return totalScore;
    }

    /**
     * 获取敌机分数
     * @param aircraft 敌机对象
     * @return 敌机分数
     */
    private int getAircraftScore(AbstractAircraft aircraft) {
        String className = aircraft.getClass().getSimpleName();
        switch (className) {
            case "MobEnemy":
                return 10;
            case "EliteEnemy":
                return 50;
            case "ElitePlusEnemy":
                // 超级精英敌机可能只是减血，不一定消失，暂不计分
                return 0;
            case "BossEnemy":
                // Boss不受影响
                return 0;
            default:
                return 0;
        }
    }

    @Override
    public void activate(HeroAircraft heroAircraft) {
        System.out.println("BombSupply active!");
        // 通知所有观察者，炸弹爆炸
        notifyObservers();
    }
}
