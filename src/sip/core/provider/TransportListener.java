package sip.core.provider;

import com.allcam.gbgw.protocol.sip.core.message.Message;

/**
 * Listener for Transport events.
 */
interface TransportListener
{
    /**
     * When a new SIP message is received.
     */
    void onReceivedMessage(Transport transport, Message msg);

    /**
     * When Transport terminates.
     */
    void onTransportTerminated(Transport transport, Exception error);
}
