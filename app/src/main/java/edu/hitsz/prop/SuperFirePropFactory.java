package edu.hitsz.prop;

/**
 * 超级火力道具工厂
 * 具体创建者角色，负责创建超级火力道具
 * @author hitsz
 */
public class SuperFirePropFactory implements PropFactory {
    @Override
    public AbstractProp createProp(int locationX, int locationY, int speedX, int speedY) {
        return new SuperFireProp(locationX, locationY, speedX, speedY);
    }
}

