package Util;
//import org.apache.commons.codec.binary.Base64;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
/**
 * @ClassName UrlToBase64
 * @Author ShiHaiLin
 * @Date 2020/6/17 10:52
 * @Description
 */
public class UrlToBase64 {

    /**
     * 服务器图片url转Base64
     * @param imageUrl
     * @return
     */
    public static String urlToBase64(String imageUrl){
        ByteArrayOutputStream data = new ByteArrayOutputStream();
        try {
            // 创建URL
            URL url = new URL(imageUrl);
            byte[] by = new byte[1024];
            // 创建链接
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            InputStream is = conn.getInputStream();
            // 将内容放到内存中
            int len = -1;
            while ((len = is.read(by)) != -1) {
                data.write(by, 0, len);
            }
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 对字节数组Base64编码
        //return Base64.encodeBase64String(data.toByteArray());包有问题
        return null;
    }

}
