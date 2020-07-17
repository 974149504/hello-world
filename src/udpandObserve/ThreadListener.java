package udpandObserve;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Observable;
import java.util.Observer;

/**
 * @ClassName ThreadListener
 * @Author ShiHaiLin
 * @Date 2020/7/13 20:34
 * @Description
 */
@Component
@Slf4j
public class ThreadListener implements Observer {

    @Value("${gwsapt.prop.socket-port}")
    private String socketPort;

    @Autowired
    private UdpReceive udpReceive;

    @Override
    public void update(Observable o, Object arg) {
        log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>RunThread意外中断<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
        //UdpReceive receive = new UdpReceive();
        udpReceive.addObserver(this);
        new Thread(udpReceive).start();
        log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>线程新建成功<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
    }
}
