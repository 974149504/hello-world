package sip.core.provider;

import com.allcam.gbgw.protocol.sip.core.message.Message;
import com.allcam.gbgw.protocol.sip.net.IpAddress;
import com.allcam.gbgw.protocol.sip.net.TcpConnection;
import com.allcam.gbgw.protocol.sip.net.TcpConnectionListener;
import com.allcam.gbgw.protocol.sip.net.TcpSocket;

import java.io.IOException;

/**
 * TcpTransport provides a TCP trasport service for SIP.
 */
class TcpTransport implements ConnectedTransport, TcpConnectionListener
{
    /**
     * TCP protocol type
     */
    private static final String PROTO_TCP = "tcp";

    /**
     * TCP connection
     */
    private TcpConnection tcpConn;

    /**
     * TCP connection
     */
    private ConnectionIdentifier connectionId;

    /**
     * The last time that has been used (in milliseconds)
     */
    private long lastTime;

    /**
     * the current received text.
     */
    private String text;

    /**
     * Transport listener
     */
    private TransportListener listener;

    /**
     * Creates a new TcpTransport
     */
    public TcpTransport(IpAddress remoteIpAddr, int remotePort, TransportListener listener, String host)
        throws IOException
    {
        this.listener = listener;
        TcpSocket socket = new TcpSocket(remoteIpAddr, remotePort, host);
        tcpConn = new TcpConnection(socket, this);
        connectionId = new ConnectionIdentifier(this);
        lastTime = System.currentTimeMillis();
        text = "";
    }

    /**
     * Costructs a new TcpTransport
     */
    public TcpTransport(TcpSocket socket, TransportListener listener)
    {
        this.listener = listener;
        tcpConn = new TcpConnection(socket, this);
        connectionId = null;
        lastTime = System.currentTimeMillis();
        text = "";
    }

    /**
     * Gets protocol type
     */
    @Override
    public String getProtocol()
    {
        return PROTO_TCP;
    }

    /**
     * Gets the remote IpAddress
     */
    @Override
    public IpAddress getRemoteAddress()
    {
        if (tcpConn != null)
        {
            return tcpConn.getRemoteAddress();
        }
        else
        {
            return null;
        }
    }

    /**
     * Gets the remote port
     */
    @Override
    public int getRemotePort()
    {
        if (tcpConn != null)
        {
            return tcpConn.getRemotePort();
        }
        else
        {
            return 0;
        }
    }

    /**
     * Gets the last time the Connection has been used (in millisconds)
     */
    @Override
    public long getLastTimeMillis()
    {
        return lastTime;
    }

    /**
     * Sends a Message through the connection. Parameters <i>dest_addr</i>/<i>dest_addr</i>
     * are not used, and the message is addressed to the connection remote peer.
     * <p>
     * Better use sendMessage(Message msg) method instead.
     */
    @Override
    public void sendMessage(Message msg, IpAddress destIpAddr, int destPort)
        throws IOException
    {
        sendMessage(msg);
    }

    /**
     * Sends a Message
     */
    @Override
    public void sendMessage(Message msg)
        throws IOException
    {
        if (tcpConn != null)
        {
            lastTime = System.currentTimeMillis();
            byte[] data = msg.toString().getBytes();
            tcpConn.send(data);
        }
    }

    /**
     * Stops running
     */
    @Override
    public void halt()
    {
        if (tcpConn != null)
        {
            tcpConn.halt();
        }
    }

    /**
     * Gets a String representation of the Object
     */
    @Override
    public String toString()
    {
        if (tcpConn != null)
        {
            return tcpConn.toString();
        }
        else
        {
            return null;
        }
    }

    // ************************* Callback methods *************************

    /**
     * When new data is received through the TcpConnection.
     */
    @Override
    public void onReceivedData(TcpConnection tcpConn, byte[] data, int len)
    {
        lastTime = System.currentTimeMillis();

        text += new String(data, 0, len, SipStack.encoding);
        SipParser par = new SipParser(text);
        Message msg = par.getSipMessage();
        while (msg != null)
        {
            msg.setRemoteAddress(tcpConn.getRemoteAddress().toString());
            msg.setRemotePort(tcpConn.getRemotePort());
            msg.setTransport(PROTO_TCP);
            msg.setConnectionId(connectionId);
            if (listener != null)
            {
                listener.onReceivedMessage(this, msg);
            }

            text = par.getRemainingString();
            par = new SipParser(text);
            msg = par.getSipMessage();
        }
    }

    /**
     * When TcpConnection terminates.
     */
    @Override
    public void onConnectionTerminated(TcpConnection tcpConn, Exception error)
    {
        if (listener != null)
        {
            listener.onTransportTerminated(this, error);
        }
        TcpSocket socket = tcpConn.getSocket();
        if (socket != null)
        {
            try
            {
                socket.close();
            }
            catch (Exception ignored)
            {
            }
        }
        this.tcpConn = null;
        this.listener = null;
    }

}
