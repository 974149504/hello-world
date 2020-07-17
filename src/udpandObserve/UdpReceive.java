package udpandObserve;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.net.*;
import java.net.DatagramSocket;
import java.util.Observable;

/**
 * @ClassName UdpReceive
 * @Author ShiHaiLin
 * @Date 2020/7/13 19:02
 * @Description
 */
@Component
@Slf4j
//@Scope(value = "prototype")
public class UdpReceive extends Observable implements Runnable {

    private DatagramSocket socket;

    @Value("${gwsapt.prop.socket-port}")
    private String socketPort;

    @Autowired
    private ThreadListener threadListener;

    @Autowired
    private ParsePacket parsePacket;

    @PostConstruct
    public void init() {
        log.info(">>>>>>>>>>>>>>>>>>>>>>>receiveSocketPort: " + socketPort);
        try {
            this.socket = new DatagramSocket(Integer.valueOf(this.socketPort));
            log.info(">>>>>>>>>>>socket is not null?" + Boolean.TRUE.equals(null != this.socket));
            this.addObserver(threadListener);
        } catch (SocketException e) {
            log.info(">>>>>>>>>>>>>fail to create socket");
        } catch (Exception e) {
            log.info(">>>>>>>>>>>>>fail to init thread!!!!");
        }
    }

    public DatagramSocket getSocket() {
        return this.socket;
    }

    @Override
    public void run() {
        while (true) {
            try {
                log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>start thread to receive Udp packet<<<<<<<<<<<<<<<<<<<<");
                byte[] buf = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buf, 0, buf.length);
                log.info(">>>>>>>>>>>>waiting for udp packet");
                this.socket.receive(packet);
                parsePacket.parseUdpPacket(packet.getLength(), packet.getData());
                continue;
                //throw new RuntimeException("故意抛出异常，中断线程运行，观察能否自动新建");
            } catch (Exception e) {
                reStartThread();
                e.printStackTrace();
                //跳出循环，保证run()执行完成，线程正常死亡
                break;
            }
        }
    }

    /**
     * 通知观察者重启线程
     */
    public void reStartThread() {
        if (true) {
            super.setChanged();
        }
        notifyObservers();
    }
}
