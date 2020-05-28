package Adapter;

/**
 * @ClassName JP110VInterfaceImpl
 * @Author ShiHaiLin
 * @Date 2020/5/7 18:56
 * @Descriptiom  110v电压工作
 */
public class JP110VInterfaceImpl implements JP110VInterface {
    @Override
    public void connect() {
        System.out.println("日本110V电源开始工作...");
    }
}
