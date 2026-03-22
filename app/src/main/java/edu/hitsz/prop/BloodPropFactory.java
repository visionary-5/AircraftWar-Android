package edu.hitsz.prop;

/**
 * 加血道具工厂
 * 具体创建者角色，负责创建加血道具
 * @author hitsz
 */
public class BloodPropFactory implements PropFactory {
    @Override
    public AbstractProp createProp(int locationX, int locationY, int speedX, int speedY) {
        return new BloodProp(locationX, locationY, speedX, speedY);
    }
}
