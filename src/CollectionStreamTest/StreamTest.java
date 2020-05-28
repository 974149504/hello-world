package CollectionStreamTest;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


/**
 * @ClassName StreamTest
 * @Author ShiHaiLin
 * @Date 2020/5/8 10:39
 * @Descriptiom
 */
public class StreamTest {
    private static List<Integer> list = Arrays.asList(2, 2, 5, 4, 3, 3, 5, 6, 15, 487, 32, 15, 62, 14, 3, 5, 4, 1, 8, 9, 10, 11, 23, 24);

    /**
     * 匹配数据
     *
     * @return
     */
    public static Boolean contains() {
        return list.stream().anyMatch(s -> Objects.equals(s, 23));
    }

    /**
     * 过滤数据,filter返回的仍是一个Stream，需要使用Stream.collect(Collectors.toList())转list;
     *
     * @return
     */
    public static List<Integer> filterTest() {
        return list.stream().filter(s -> s.equals(2)).distinct().collect(Collectors.toList());
    }

    /**
     * 汇总数据，需要转成IntStream或者DoubleStream，方法参数为“方法引用：：”，获取所有的Stream的值，再sum;
     *
     * @return
     */
    public static Integer summaryTest() {

        return list.stream().mapToInt(Integer::valueOf).sum();
    }


    /**
     * 集合数据转化
     *
     * @return
     */
    public static List<Integer> phraseTest() {
        return list.stream().map(StreamTest::transRule).collect(Collectors.toList());
    }

    /**
     * 转化集合单个元素的规则，可以操作复杂集合，入参和返参都是List<T>中的T泛型数据
     *
     * @param i
     * @return
     */
    private static Integer transRule(Integer i) {
        return i = i + 1;
    }


    public static void main(String[] args) {
        System.out.println(StreamTest.contains());
        System.out.println(StreamTest.filterTest());
        System.out.println(StreamTest.summaryTest());
        System.out.println(StreamTest.phraseTest());
    }

}
