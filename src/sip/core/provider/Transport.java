package sip.core.provider;

import com.allcam.gbgw.protocol.sip.core.message.Message;
import com.allcam.gbgw.protocol.sip.net.IpAddress;

import java.io.IOException;

/**
 * Transport is a generic transport service for SIP.
 */
interface Transport
{
    /**
     * Gets protocol type
     */
    String getProtocol();

    /**
     * Stops running
     */
    void halt();

    /**
     * Sends a Message to a destination address and port
     */
    void sendMessage(Message msg, IpAddress destIpAddr, int destPort)
        throws IOException;

    /**
     * Gets a String representation of the Object
     */
    @Override
    String toString();
}
