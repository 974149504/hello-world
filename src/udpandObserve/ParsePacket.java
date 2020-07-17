package udpandObserve;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.SocketException;

import static com.allcam.common.utils.AES256EncryptionUtils.bytesToHexString;

/**
 * @ClassName ParseUdpPacket
 * @Author ShiHaiLin
 * @Date 2020/7/17 9:42
 * @Description
 */
@Slf4j
@Component
public class ParsePacket {

    @Autowired
    private UdpSend udpSend;

    /**
     * 解析数据包
     *
     * @param len
     * @param buf
     */
    public void parseUdpPacket(int len, byte[] buf) throws SocketException {
        log.info(">>>>>>>>>>>>>>>>>>>>>>>>数据包长度： " + len);
        log.info(">>>>>>>>>>>>>>>>>>>>>>>>数据包： " + bytesToHexString(buf));
        udpSend.sendUdpPacket("080001000000010A");
    }

}
