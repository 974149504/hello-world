package Decorator;

/**
 * @ClassName SK2
 * @Author ShiHaiLin
 * @Date 2020/5/16 14:43
 * @Description sk II神仙水
 */
public class SK2 extends Cosmetic{

    public SK2(Girl girl){
        //调用父类构造器，使用神仙水装饰素颜小圆脸
        super(girl);
    }

    //装饰方法（基础类新增的功能）
    public void decoratorMethod(){

        System.out.println("倒出神仙水，轻抹于脸颊，分分钟变美");

    }

    @Override
    public void show() {
        super.show();
        this.decoratorMethod();
    }
}
