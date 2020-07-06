import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.util.Arrays;

/**
 * @ClassName UdpTest
 * @Author ShiHaiLin
 * @Date 2020/7/6 14:18
 * @Description
 */
public class UdpTest {

   //udp报文发送

   public void udpSent() throws SocketException, UnsupportedEncodingException, UnknownHostException {
      String data = "hello,world";
      //发送socket
      DatagramSocket socket = new DatagramSocket();
      InetAddress host = InetAddress.getByName("172.16.20.235");
      //数据报文
      DatagramPacket packet = new DatagramPacket(data.getBytes("UTF-8"), data.getBytes("UTF-8").length, host, 7015);
      try {
         System.out.println("开始发送报文");
         socket.send(packet);
      } catch (UnsupportedEncodingException e) {
         System.out.println("不支持utf-8格式转码");
      } catch (UnknownHostException e) {
         System.out.println("socket目的主机未知/离线，请检查通信");
      } catch (SocketException e) {
         System.out.println("通信socket异常");
      } catch (Exception e) {
         e.printStackTrace();
      } finally {
         System.out.println("报文发送结束");
         socket.close();
      }

   }

   //udp报文接收

   public void udpReceive() throws IOException {
      DatagramSocket socket = new DatagramSocket(8015);
      DatagramPacket packet = new DatagramPacket(new byte[1024],1024);
      //方法会一直阻塞，直到对应端口接收到数据报文
      socket.receive(packet);
      byte[] arr = packet.getData();
      int len = packet.getLength();
      System.out.println(new String(arr,0,len));
      socket.close();
   }

   //测试InetAddress类

   public void inetAddressTest() {
      System.out.println("阿森，fuck u !");
      try {
         InetAddress address = InetAddress.getLocalHost();
         System.out.println("计算机名：" + address.getHostName());
         System.out.println("local ip:" + address.getHostAddress());
         //获取字节数组形式本地IP地址
         byte[] bytes = address.getAddress();
         System.out.println("字节数组形式local IP：" + Arrays.toString(bytes));
         //直接输出ip
         System.out.println(address);
         //String[] str = address.toString().split("/");
         System.out.println(address.toString().split("/")[1]);

      } catch (UnknownHostException e) {
         System.out.println("未知主机异常," + e.toString());
      } catch (Exception e) {
         e.printStackTrace();
      }

   }


   public void byteTextTest(){
      char[] chars = {0x08, 0x65, 0x6c, 0x6c, 0x6f, 0x2c, 0xf7, 0xd7};
      System.out.println(Arrays.toString(chars));
      String data = "hello,world";
      byte[] bytes = data.getBytes();
      System.out.println(Arrays.toString(bytes));
   }

}
