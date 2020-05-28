import com.sun.deploy.util.StringUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName LambdaTest
 * @Author ShiHaiLin
 * @Date 2020/4/28 20:40
 * @Descriptiom
 */

class Item {
    private String name;
    private Integer id;

    public void setName(String name) {
        this.name = name;
    }

    public Item(String name, Integer id) {
        this.name = name;
        this.id = id;
    }

    @Override
    public String toString() {
        return "Item{" +
                "name='" + name + '\'' +
                ", id=" + id +
                '}';
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public Integer getId() {
        return id;
    }
}
public class LambdaTest {
    public static void main(String[] args) throws FileNotFoundException {
        List<Item> list = new ArrayList<Item>();
        list.add(new Item("hello",1));
        list.add(new Item("sdsa",3));
        list.add(new Item("java",2));
        list.add(new Item("world",5));
        list.add(new Item("mind",8));
        list.sort(((o1, o2) -> o2.getId()-o1.getId()));
        list.forEach(System.out::println);
       /* File file = new File("C:\\Users\\HP\\Desktop\\input.bin");
        BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));*/
        String s;
        StringBuilder res = new StringBuilder();
        char[] buf = {0x53, 0x49, 0x50, 0x2f, 0x32, 0x2e, 0x30, 0x20,
                0x32, 0x30, 0x30, 0x20, 0x4f, 0x4b, 0x0d, 0x0a,
                0x56, 0x69, 0x61, 0x3a, 0x20, 0x53, 0x49, 0x50,
                0x2f, 0x32, 0x2e, 0x30, 0x2f, 0x55, 0x44, 0x50,
                0x20, 0x31, 0x30, 0x2e, 0x36, 0x34, 0x2e, 0x34,
                0x39, 0x2e, 0x34, 0x34, 0x3a, 0x37, 0x31, 0x30,
                0x30, 0x3b, 0x72, 0x70, 0x6f, 0x72, 0x74, 0x3d,
                0x37, 0x31, 0x30, 0x30, 0x3b, 0x62, 0x72, 0x61,
                0x6e, 0x63, 0x68, 0x3d, 0x7a, 0x39, 0x68, 0x47,
                0x34, 0x62, 0x4b, 0x33, 0x34, 0x37, 0x30, 0x36,
                0x32, 0x37, 0x37, 0x34, 0x32, 0x0d, 0x0a, 0x46,
                0x72, 0x6f, 0x6d, 0x3a, 0x20, 0x3c, 0x73, 0x69,
                0x70, 0x3a, 0x31, 0x33, 0x30, 0x39, 0x30, 0x39,
                0x31, 0x31, 0x35, 0x32, 0x32, 0x39, 0x33, 0x30,
                0x30, 0x39, 0x32, 0x30, 0x40, 0x31, 0x30, 0x2e,
                0x36, 0x34, 0x2e, 0x34, 0x39, 0x2e, 0x34, 0x34,
                0x3a, 0x37, 0x31, 0x30, 0x30, 0x3e, 0x3b, 0x74,
                0x61, 0x67, 0x3d, 0x33, 0x34, 0x36, 0x32, 0x37,
                0x39, 0x38, 0x34, 0x35, 0x0d, 0x0a, 0x54, 0x6f,
                0x3a, 0x20, 0x3c, 0x73, 0x69, 0x70, 0x3a, 0x33,
                0x33, 0x30, 0x31, 0x30, 0x36, 0x30, 0x32, 0x30,
                0x30, 0x31, 0x33, 0x31, 0x30, 0x30, 0x31, 0x39,
                0x33, 0x32, 0x35, 0x40, 0x31, 0x30, 0x2e, 0x36,
                0x34, 0x2e, 0x34, 0x39, 0x2e, 0x32, 0x31, 0x38,
                0x3a, 0x37, 0x31, 0x30, 0x30, 0x3e, 0x3b, 0x74,
                0x61, 0x67, 0x3d, 0x33, 0x37, 0x34, 0x34, 0x37,
                0x37, 0x32, 0x33, 0x38, 0x33, 0x0d, 0x0a, 0x43,
                0x61, 0x6c, 0x6c, 0x2d, 0x49, 0x44, 0x3a, 0x20,
                0x33, 0x37, 0x32, 0x32, 0x30, 0x33, 0x31, 0x32,
                0x30, 0x33, 0x0d, 0x0a, 0x43, 0x53, 0x65, 0x71,
                0x3a, 0x20, 0x32, 0x30, 0x20, 0x4d, 0x45, 0x53,
                0x53, 0x41, 0x47, 0x45, 0x0d, 0x0a, 0x55, 0x73,
                0x65, 0x72, 0x2d, 0x41, 0x67, 0x65, 0x6e, 0x74,
                0x3a, 0x20, 0x48, 0x69, 0x6b, 0x76, 0x69, 0x73,
                0x69, 0x6f, 0x6e, 0x0d, 0x0a, 0x43, 0x6f, 0x6e,
                0x74, 0x65, 0x6e, 0x74, 0x2d, 0x4c, 0x65, 0x6e,
                0x67, 0x74, 0x68, 0x3a, 0x20, 0x30, 0x0d, 0x0a,
                0x0d, 0x0a};
        for(char c:buf){
            System.out.print(c);
        }
        System.out.println("=================>>>>>>>>>>>>>>>>>");
        System.out.print(0x49);

        AttendanceInfo attendanceInfo = new AttendanceInfo();
        if(attendanceInfo != null){
            System.out.println("不为空");
        }else{
            System.out.println("为空");
        }





    }
}
