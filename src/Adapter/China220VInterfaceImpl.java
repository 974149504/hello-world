package Adapter;

/**
 * @ClassName China220VInterfaceImpl
 * @Author ShiHaiLin
 * @Date 2020/5/7 18:53
 * @Descriptiom  220v电压开始工作
 */
public class China220VInterfaceImpl implements China220VInterface {
    @Override
    public void connect() {
        System.out.println("中国220V电源开始工作...");
    }
}
