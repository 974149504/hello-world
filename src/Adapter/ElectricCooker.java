package Adapter;

/**
 * @ClassName ElectricCooker
 * @Author ShiHaiLin
 * @Date 2020/5/7 18:59
 * @Descriptiom  需要110V才能工作的service
 */
public class ElectricCooker {
    private JP110VInterface ap110VInterface;

    public ElectricCooker(JP110VInterface ap110VInterface){
        this.ap110VInterface=ap110VInterface;
    }

    public void work(){
        // 电源方法执行
        ap110VInterface.connect();
        System.out.println("电饭煲开始工作...");
    }
    public static void main(String[] args) {
        //220V 电源接口  只有220v电压
        China220VInterface china220VInterface=new China220VInterfaceImpl();
        //适配器接口  通过适配器转换（220V转为110v)
        PowerAdapter powerAdapter=new PowerAdapter(china220VInterface);
        //电饭煲工作   只能110v工作()
        ElectricCooker electricCooker=new ElectricCooker(powerAdapter);
        electricCooker.work();

        System.out.println();

    }
}
