package Decorator;

/**
 * @ClassName Cosmetic
 * @Author ShiHaiLin
 * @Date 2020/5/16 14:22
 * @Description 化妆品抽象类，小圆脸所有的化妆品(装饰器抽象类，可以理解成需要在基础类中扩展的功能模块)
 */
public abstract class Cosmetic implements Girl {

    //需要被打扮的女孩（需要被装饰的对象）
    private Girl girl;

    //构造器入参装饰基础类，可理解为素颜小圆脸
    public Cosmetic(Girl girl) {

        this.girl = girl;

    }

    @Override
    public void show() {
        this.girl.show();
    }

}
