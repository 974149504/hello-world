package SpringTest;

import java.sql.SQLOutput;
import java.util.*;

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


        /*String str = "123456789456456521315468784553113154697/878945";

        int length = str.length();

        String subStr = str.substring(str.length() - 7,str.length());
        System.out.println(subStr);


        HashMap<String, String> data = new HashMap<>();
        data.put("name","shl");
        data.put("age","27");

        StringBuilder url = new StringBuilder("http://172.16.21.182:8098");

        data.forEach((k,v) ->{
            url.append("&" + k + "=");
            url.append(v);
        });

        System.out.println(url);*/


        /*String str = "172.16.21.102-1-出";
        Integer i = "入".equals(str.substring(str.length() - 1)) ? 0 : 1 ;

        System.out.println(i.toString());*/

        List<String> list = new ArrayList<>();
        list.add("45645assd");

        System.out.println(list.size() > 0);

    }
}
