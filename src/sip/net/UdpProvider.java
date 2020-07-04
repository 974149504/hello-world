package sip.net;

import java.io.IOException;
import java.io.InterruptedIOException;

/**
 * UdpProvider provides an UDP send/receive service. On the receiver side it
 * waits for UDP datagrams and passes them to the UdpProviderListener.
 * <p>
 * If the attribute <i>aliveTime</i> has a non-zero value, the UdpProvider
 * stops after <i>aliveTime</i> milliseconds of inactivity.
 * <p>
 * When a new packet is received, the
 * onReceivedPacket(UdpProvider,DatagramPacket) method is fired.
 * <p>
 * Method onServiceTerminated(UdpProvider) is fired when the the UdpProvider
 * stops receiving packets.
 */
public class UdpProvider extends Thread
{
    /**
     * The reading buffer size
     */
    private static final int BUFFER_SIZE = 65535;

    /**
     * Default value for the maximum time that the UDP receiver can remain
     * active after been halted (in milliseconds)
     */
    private static final int DEFAULT_SOCKET_TIMEOUT = 2000;

    /**
     * UDP socket
     */
    private UdpSocket socket;

    /**
     * Maximum time that the UDP receiver can remain active after been halted
     * (in milliseconds)
     */
    private int socketTimeout;

    /**
     * Maximum time that the UDP receiver remains active without receiving UDP
     * datagrams (in milliseconds)
     */
    private long aliveTime;

    /**
     * Minimum size for received packets. Shorter packets are silently
     * discarded.
     */
    private int minimumLength;

    /**
     * Whether it has been halted
     */
    private boolean stop;

    /**
     * Whether it is running
     */
    private boolean isRunning;

    /**
     * UdpProvider listener
     */
    private UdpProviderListener listener;

    /**
     * Creates a new UdpProvider
     */
    public UdpProvider(UdpSocket socket, UdpProviderListener listener)
    {
        init(socket, 0, listener);
        start();
    }

    /**
     * Creates a new UdpProvider
     */
    public UdpProvider(UdpSocket socket, long aliveTime, UdpProviderListener listener)
    {
        init(socket, aliveTime, listener);
        start();
    }

    /**
     * Inits the UdpProvider
     */
    private void init(UdpSocket socket, long aliveTime, UdpProviderListener listener)
    {
        this.listener = listener;
        this.socket = socket;
        this.socketTimeout = DEFAULT_SOCKET_TIMEOUT;
        this.aliveTime = aliveTime;
        this.minimumLength = 0;
        this.stop = false;
        this.isRunning = true;
    }

    /**
     * Gets the UdpSocket
     */
    public UdpSocket getUdpSocket()
    {
        return socket;
    }

    /** Sets a new UdpSocket */
    /*
     * public void setUdpSocket(UdpSocket socket) { this.socket=socket; }
     */

    /**
     * Whether the service is running
     */
    public boolean isRunning()
    {
        return isRunning;
    }

    /**
     * Sets the maximum time that the UDP service can remain active after been
     * halted
     */
    public void setSoTimeout(int timeout)
    {
        socketTimeout = timeout;
    }

    /**
     * Gets the maximum time that the UDP service can remain active after been
     * halted
     */
    public int getSoTimeout()
    {
        return socketTimeout;
    }

    /**
     * Sets the minimum size for received packets. Packets shorter than that are
     * silently discarded.
     */
    public void setMinimumReceivedDataLength(int len)
    {
        minimumLength = len;
    }

    /**
     * Gets the minimum size for received packets. Packets shorter than that are
     * silently discarded.
     */
    public int getMinimumReceivedDataLength()
    {
        return minimumLength;
    }

    /**
     * Sends a UdpPacket
     */
    public void send(UdpPacket packet)
        throws IOException
    {
        if (!stop)
        {
            socket.send(packet);
        }
    }

    /**
     * Stops running
     */
    public void halt()
    {
        stop = true;
        socket.close(); // modified
    }

    /**
     * The main thread
     */
    @Override
    public void run()
    {
        byte[] buf = new byte[BUFFER_SIZE];
        UdpPacket packet = new UdpPacket(buf, buf.length);

        Exception error = null;
        long expire = 0;
        if (aliveTime > 0)
        {
            expire = System.currentTimeMillis() + aliveTime;
        }
        try
        {
//			socket.setSoTimeout(socketTimeout); modified
            // loop
            while (!stop)
            {
                try
                {
                    socket.receive(packet);
                }
                catch (InterruptedIOException ie)
                {
                    if (aliveTime > 0 && System.currentTimeMillis() > expire)
                    {
                        halt();
                    }
                    continue;
                }
                if (packet.getLength() >= minimumLength)
                {
                    if (listener != null)
                    {
                        listener.onReceivedPacket(this, packet);
                    }
                    if (aliveTime > 0)
                    {
                        expire = System.currentTimeMillis() + aliveTime;
                    }
                }
                packet = new UdpPacket(buf, buf.length);
            }
        }
        catch (Exception e)
        {
            error = e;
            stop = true;
        }
        isRunning = false;
        if (listener != null)
        {
            listener.onServiceTerminated(this, error);
        }
        listener = null;
    }

    /**
     * Gets a String representation of the Object
     */
    @Override
    public String toString()
    {
        return "udp:" + socket.getLocalAddress() + ":" + socket.getLocalPort();
    }

}
