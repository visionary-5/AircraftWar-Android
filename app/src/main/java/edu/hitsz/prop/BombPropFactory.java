package edu.hitsz.prop;

/**
 * 炸弹道具工厂
 * 具体创建者角色，负责创建炸弹道具
 * @author hitsz
 */
public class BombPropFactory implements PropFactory {
    @Override
    public AbstractProp createProp(int locationX, int locationY, int speedX, int speedY) {
        return new BombProp(locationX, locationY, speedX, speedY);
    }
}
