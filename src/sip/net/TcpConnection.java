package sip.net;

import java.io.*;

/**
 * TcpConnection provides a TCP connection oriented transport service.
 */
public class TcpConnection extends Thread
{
    /**
     * The reading buffer size
     */
    private static final int BUFFER_SIZE = 65535;

    /**
     * Default value for the maximum time that the tcp connection can remain
     * active after been halted (in milliseconds)
     */
    private static final int DEFAULT_SOCKET_TIMEOUT = 2000; // 2sec

    /**
     * The TCP socket
     */
    private TcpSocket socket;

    /**
     * Maximum time that the connection can remain active after been halted (in
     * milliseconds)
     */
    private int socketTimeout;

    /**
     * Maximum time that the connection remains active without receiving data
     * (in milliseconds)
     */
    private long aliveTime;

    /**
     * The InputStream
     */
    private InputStream istream;

    /**
     * The OutputStream
     */
    private OutputStream ostream;

    /**
     * InputStream/OutputStream error
     */
    private Exception error;

    /**
     * Whether it has been halted
     */
    private boolean stop;

    /**
     * Whether it is running
     */
    private boolean isRunning;

    /**
     * TcpConnection listener
     */
    private TcpConnectionListener listener;

    /**
     * Costructs a new TcpConnection
     */
    public TcpConnection(TcpSocket socket, TcpConnectionListener listener)
    {
        init(socket, 0, listener);
        start();
    }

    /**
     * Costructs a new TcpConnection
     */
    public TcpConnection(TcpSocket socket, long aliveTime, TcpConnectionListener listener)
    {
        init(socket, aliveTime, listener);
        start();
    }

    /**
     * Inits the TcpConnection
     */
    private void init(TcpSocket socket, long aliveTime, TcpConnectionListener listener)
    {
        this.listener = listener;
        this.socket = socket;
        this.socketTimeout = DEFAULT_SOCKET_TIMEOUT;
        this.aliveTime = aliveTime;
        this.stop = false;
        this.isRunning = true;

        this.istream = null;
        this.ostream = null;
        this.error = null;
        try
        {
            istream = new BufferedInputStream(socket.getInputStream());
            ostream = new BufferedOutputStream(socket.getOutputStream());
        }
        catch (Exception e)
        {
            error = e;
        }
    }

    /**
     * Whether the service is running
     */
    public boolean isRunning()
    {
        return isRunning;
    }

    /**
     * Gets the TcpSocket
     */
    public TcpSocket getSocket()
    {
        return socket;
    }

    /**
     * Gets the remote IP address
     */
    public IpAddress getRemoteAddress()
    {
        return socket.getAddress();
    }

    /**
     * Gets the remote port
     */
    public int getRemotePort()
    {
        return socket.getPort();
    }

    /**
     * Stops running
     */
    public void halt()
    {
        stop = true;
    }

    /**
     * Sends data
     */
    public void send(byte[] buff, int offset, int len)
        throws IOException
    {
        if (!stop && ostream != null)
        {
            ostream.write(buff, offset, len);
            ostream.flush();
        }
    }

    /**
     * Sends data
     */
    public void send(byte[] buff)
        throws IOException
    {
        send(buff, 0, buff.length);
    }

    /**
     * Runs the tcp receiver
     */
    @Override
    public void run()
    {
        byte[] buff = new byte[BUFFER_SIZE];
        long expire = 0;
        if (aliveTime > 0)
        {
            expire = System.currentTimeMillis() + aliveTime;
        }
        try
        {
            if (error != null)
            {
                throw error;
            }
//			socket.setSoTimeout(socketTimeout); modified
            // loop
            while (!stop)
            {
                int len = 0;
                if (istream != null)
                {
                    try
                    {
                        len = istream.read(buff);
                    }
                    catch (InterruptedIOException ie)
                    {
                        if (aliveTime > 0 && System.currentTimeMillis() > expire)
                        {
                            halt();
                        }
                        continue;
                    }
                }
                if (len < 0)
                { // error=new Exception("TCP connection closed");
                    stop = true;
                }
                else if (len > 0)
                {
                    if (listener != null)
                    {
                        listener.onReceivedData(this, buff, len);
                    }
                    if (aliveTime > 0)
                    {
                        expire = System.currentTimeMillis() + aliveTime;
                    }
                }
            }
        }
        catch (Exception e)
        {
            error = e;
            stop = true;
        }
        isRunning = false;
        if (istream != null)
        {
            try
            {
                istream.close();
            }
            catch (Exception e)
            {
            }
        }
        if (ostream != null)
        {
            try
            {
                ostream.close();
            }
            catch (Exception e)
            {
            }
        }
        if (listener != null)
        {
            listener.onConnectionTerminated(this, error);
        }
        listener = null;
    }

    /**
     * Gets a String representation of the Object
     */
    @Override
    public String toString()
    {
        return "tcp:";
        // modified + socket.getLocalAddress() + ":" + socket.getLocalPort() + "<->" + socket.getAddress() + ":" + socket.getPort();
    }

}
