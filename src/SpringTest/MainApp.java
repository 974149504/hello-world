package SpringTest;

import java.util.Calendar;
import java.util.Date;

/**
 * @ClassName MainApp
 * @Author ShiHaiLin
 * @Date 2020/5/28 17:19
 * @Description
 */
public class MainApp {
    public static void main(String[] args) {
        /*ApplicationContext context = new FileSystemXmlApplicationContext
                ("C:/Users/ZARA/workspace/HelloSpring/src/Beans.xml");
        HelloWorld obj = (HelloWorld) context.getBean("helloWorld");
        obj.getMessage();*/
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());

        int length = c.get(Calendar.DAY_OF_WEEK);
        System.out.println(length);
    }
}
