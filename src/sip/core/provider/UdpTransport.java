package sip.core.provider;

import com.allcam.gbgw.protocol.sip.core.message.Message;
import com.allcam.gbgw.protocol.sip.net.*;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * UdpTransport provides an UDP transport service for SIP.
 */
@Slf4j
class UdpTransport implements Transport, UdpProviderListener
{
    /**
     * UDP protocol type
     */
    private static final String PROTO_UDP = "udpandObserve";

    /**
     * UDP provider
     */
    private UdpProvider udpProvider;

    /**
     * Transport listener
     */
    private TransportListener listener;

    private int port;

    /**
     * Creates a new UdpTransport
     */
    public UdpTransport(int port, TransportListener listener)
        throws IOException
    {
        this.listener = listener;
        UdpSocket socket = new UdpSocket(port);
        udpProvider = new UdpProvider(socket, this);
        this.port = socket.getLocalPort();
    }

    /**
     * Creates a new UdpTransport
     */
    public UdpTransport(int port, IpAddress ipaddr, TransportListener listener)
        throws IOException
    {
        this.listener = listener;
        UdpSocket socket = new UdpSocket(port, ipaddr);
        udpProvider = new UdpProvider(socket, this);
        this.port = socket.getLocalPort();
    }

    /**
     * Creates a new UdpTransport
     */
    public UdpTransport(UdpSocket socket, TransportListener listener)
    {
        this.listener = listener;
        udpProvider = new UdpProvider(socket, this);
        this.port = socket.getLocalPort();
    }

    /**
     * Gets protocol type
     */
    @Override
    public String getProtocol()
    {
        return PROTO_UDP;
    }

    public int getPort()
    {
        return port;
    }

    /**
     * Sends a Message to a destination address and port
     */
    @Override
    public void sendMessage(Message msg, IpAddress destIpAddr, int destPort)
        throws IOException
    {
        log.debug("UdpTransport sendMessage -->");
        if (udpProvider != null)
        {
            byte[] data = msg.toString().getBytes(SipStack.encoding);
            UdpPacket packet = new UdpPacket(data, data.length);
            packet.setIpAddress(destIpAddr);
            packet.setPort(destPort);
            udpProvider.send(packet);
        }
    }

    /**
     * Stops running
     */
    @Override
    public void halt()
    {
        if (udpProvider != null)
        {
            udpProvider.halt();
        }
    }

    /**
     * Gets a String representation of the Object
     */
    @Override
    public String toString()
    {
        if (udpProvider != null)
        {
            return udpProvider.toString();
        }
        else
        {
            return null;
        }
    }

    // ************************* Callback methods *************************

    /**
     * When a new UDP datagram is received.
     */
    @Override
    public void onReceivedPacket(UdpProvider udp, UdpPacket packet)
    {
        Message msg = new Message(packet);
        msg.setRemoteAddress(packet.getIpAddress().toString());
        msg.setRemotePort(packet.getPort());
        msg.setTransport(PROTO_UDP);
        log.debug("UdpTransport udp Received message");
        if (listener != null)
        {
            listener.onReceivedMessage(this, msg);
        }
    }

    /**
     * When DatagramService stops receiving UDP datagrams.
     */
    @Override
    public void onServiceTerminated(UdpProvider udp, Exception error)
    {
        if (listener != null)
        {
            listener.onTransportTerminated(this, error);
        }
        UdpSocket socket = udp.getUdpSocket();
        if (socket != null)
        {
            try
            {
                socket.close();
            }
            catch (Exception e)
            {
                log.error("socket close fail.", e);
            }
        }
        this.udpProvider = null;
        this.listener = null;
    }

}
