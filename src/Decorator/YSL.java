package Decorator;

/**
 * @ClassName YSL
 * @Author ShiHaiLin
 * @Date 2020/5/16 14:58
 * @Description
 */
public class YSL extends Cosmetic {

    public YSL(Girl girl){
        super(girl);
    }

    public void decoratorMethod(){
        System.out.println("再抹点杨树林");
    }

    @Override
    public void show() {
        super.show();
        this.decoratorMethod();
    }
}
