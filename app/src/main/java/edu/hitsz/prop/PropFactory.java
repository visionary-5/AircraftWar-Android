package edu.hitsz.prop;

/**
 * 道具工厂接口
 * 充当创建者角色
 * @author hitsz
 */
public interface PropFactory {
    /**
     * 创建道具
     * @param locationX 道具初始x坐标
     * @param locationY 道具初始y坐标
     * @param speedX 道具x方向速度
     * @param speedY 道具y方向速度
     * @return 创建的道具对象
     */
    AbstractProp createProp(int locationX, int locationY, int speedX, int speedY);
}
