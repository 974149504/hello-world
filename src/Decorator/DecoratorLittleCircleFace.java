package Decorator;

/**
 * @ClassName DecoratorLittleCircleFace
 * @Author ShiHaiLin
 * @Date 2020/5/16 15:01
 * @Description 小圆脸出街必备神器（TEST类）
 */
public class DecoratorLittleCircleFace {
    public static void main(String[] args) {

        //新建一个光板小圆脸
        Girl littleCircleFace = new XYL();

     /*   //上点神仙水
        littleCircleFace = new SK2(littleCircleFace);
        littleCircleFace.show();

        //抹点杨树林
        littleCircleFace = new YSL(littleCircleFace);
        littleCircleFace.show();
     */

        littleCircleFace = new YSL(new SK2(littleCircleFace));
        littleCircleFace.show();


    }
}
