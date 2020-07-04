package sip.core.provider;

import com.allcam.gbgw.protocol.sip.core.message.Message;
import com.allcam.gbgw.protocol.sip.net.IpAddress;

import java.io.IOException;

/**
 * ConnectedTransport is a generic CO transport service for SIP.
 */
interface ConnectedTransport extends Transport
{
    /**
     * Gets the remote IpAddress
     */
    IpAddress getRemoteAddress();

    /**
     * Gets the remote port
     */
    int getRemotePort();

    /**
     * Gets the last time the ConnectedTransport has been used (in millisconds)
     */
    long getLastTimeMillis();

    /**
     * Sends a Message
     */
    void sendMessage(Message msg)
        throws IOException;
}
