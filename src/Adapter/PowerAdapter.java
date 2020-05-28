package Adapter;

/**
 * @ClassName PowerAdapter
 * @Author ShiHaiLin
 * @Date 2020/5/7 18:57
 * @Descriptiom  持有220V电压，实现110v电压
 */
public class PowerAdapter implements  JP110VInterface{
    private China220VInterface china220VInterface;

    //只提供220v电压时，需要调用适配器，因为该适配器实现了110v电压，所以可以调用110v用电器
    public PowerAdapter(China220VInterface china220VInterface){
        this.china220VInterface = china220VInterface;
    }


    @Override
    public void connect() {
        china220VInterface.connect();
    }
}
