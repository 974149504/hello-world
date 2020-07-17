package udpandObserve;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.net.*;

/**
 * @ClassName UdpSend
 * @Author ShiHaiLin
 * @Date 2020/7/16 15:35
 * @Description
 */
@Component
@Slf4j
public class UdpSend {

    /**
     * 主机ip
     */
    @Value("${gwsapt.prop.host-ip}")
    private String hostIp;

    @Value("${gwsapt.prop.host-port}")
    private String hostPort;

    /**
     * 目标主机
     */
    private InetAddress host;

    @PostConstruct
    public void init() {
        try {
            this.host = InetAddress.getByName(hostIp);
        } catch (UnknownHostException e) {
            log.info(">>>>>>>>>>>>>>UnknownHost", e);
        }
        log.info(">>>>>>>>>>>>>>>>>>>>>>>hostIp: " + host.toString());
    }

    /**
     * udp报文发送
     *
     * @param data
     */
    public void sendUdpPacket(String data) throws SocketException {

        DatagramSocket socket = new DatagramSocket();
        if (StringUtils.isNotBlank(data)) {
            //将命令转为字节数组
            try {
                byte[] buf = data.getBytes("UTF-8");
                //数据报文
                DatagramPacket packet = new DatagramPacket(buf, buf.length, host, Integer.parseInt(hostPort));
                log.info(">>>>>>>>>>>>>>>>开始发送报文");
                socket.send(packet);
            } catch (UnknownHostException e) {
                log.info(">>>>>>>>>>>>>>>>socket目的主机未知/离线，请检查通信");
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                socket.close();
                log.info(">>>>>>>>>>>>>>>>报文发送结束");
            }
        }
    }


}
