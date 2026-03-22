package edu.hitsz.observer;

/**
 * 观察者接口
 * 所有需要响应炸弹效果的对象都需要实现此接口
 *
 * @author hitsz
 */
public interface Observer {
    /**
     * 更新方法，当炸弹爆炸时被调用
     */
    void update();
}

