package SpringTest;

/**
 * @ClassName HelloWorld
 * @Author ShiHaiLin
 * @Date 2020/5/28 17:18
 * @Description
 */
public class HelloWorld {

    private String message;
    public void setMessage(String message){
        this.message = message;
    }
    public void getMessage(){
        System.out.println("Your Message : " + message);
    }
}
