package edu.hitsz.prop;

/**
 * 火力道具工厂
 * 具体创建者角色，负责创建火力道具
 * @author hitsz
 */
public class FirePropFactory implements PropFactory {
    @Override
    public AbstractProp createProp(int locationX, int locationY, int speedX, int speedY) {
        return new FireProp(locationX, locationY, speedX, speedY);
    }
}
