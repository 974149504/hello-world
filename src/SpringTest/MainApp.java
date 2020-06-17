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



      /*  Calendar c = Calendar.getInstance();
        c.setTime(new Date());

        int length = c.get(Calendar.DAY_OF_WEEK);
        System.out.println("1".equals("a"));

        String s = "";
        try{
            s = "sadasdasasf";
        }catch (Exception e){
            e.printStackTrace();
        }
        String date = "2020-05-07 12:22:22";
        String subString = date.substring(0,10);

        System.out.println(subString);*/


        String str = "123456789456456521315468784553113154697/878945";

        int length = str.length();

        String subStr = str.substring(str.length() - 7,str.length());
        System.out.println(subStr);



    }
}
