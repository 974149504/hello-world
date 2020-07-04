package sip.net;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ServerSocket;

/**
 * TcpServer implements a TCP server wainting for incoming connection.
 */
public class TcpServer extends Thread
{
    /**
     * Default value for the maximum time that the tcp server can remain active
     * after been halted (in milliseconds)
     */
    private static final int DEFAULT_SOCKET_TIMEOUT = 5000;

    /**
     * Default ServerSocket backlog value
     */
    private static int SOCKET_BACKLOG = 50;

    /**
     * The TCP server socket
     */
    private ServerSocket serverSocket;

    /**
     * Maximum time that the server remains active without incoming connections
     * (in milliseconds)
     */
    private long aliveTime;

    /**
     * Whether it has been halted
     */
    private boolean stop;

    /**
     * Whether it is running
     */
    private boolean isRunning;

    /**
     * TcpServer listener
     */
    private TcpServerListener listener;

    /**
     * Costructs a new TcpServer
     */
    public TcpServer(int port, TcpServerListener listener)
        throws IOException
    {
        init(port, null, 0, listener);
        start();
    }

    /**
     * Costructs a new TcpServer
     */
    public TcpServer(int port, IpAddress bindIpaddr, TcpServerListener listener)
        throws IOException
    {
        init(port, bindIpaddr, 0, listener);
        start();
    }

    /**
     * Costructs a new TcpServer
     */
    public TcpServer(int port, IpAddress bindIpAddr, long aliveTime, TcpServerListener listener)
        throws IOException
    {
        init(port, bindIpAddr, aliveTime, listener);
        start();
    }

    /**
     * Inits the TcpServer
     */
    private void init(int port, IpAddress bindIpAddr, long aliveTime, TcpServerListener listener)
        throws IOException
    {
        this.listener = listener;
        if (bindIpAddr == null)
        {
            serverSocket = new ServerSocket(port);
        }
        else
        {
            serverSocket = new ServerSocket(port, SOCKET_BACKLOG, bindIpAddr.getInetAddress());
        }
        this.aliveTime = aliveTime;
        this.stop = false;
        this.isRunning = true;
    }

    public int getPort()
    {
        return serverSocket.getLocalPort();
    }

    /**
     * Whether the service is running
     */
    public boolean isRunning()
    {
        return isRunning;
    }

    /**
     * Stops running
     */
    public void halt()
    {
        stop = true;
        try
        {
            serverSocket.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Runs the server
     */
    @Override
    public void run()
    {
        Exception error = null;
        try
        {
//			serverSocket.setSoTimeout(socketTimeout); modified
            long expire = 0;
            if (aliveTime > 0)
            {
                expire = System.currentTimeMillis() + aliveTime;
            }
            // loop
            while (!stop)
            {
                TcpSocket socket;
                try
                {
                    socket = new TcpSocket(serverSocket.accept());
                }
                catch (InterruptedIOException ie)
                {
                    if (aliveTime > 0 && System.currentTimeMillis() > expire)
                    {
                        halt();
                    }
                    continue;
                }
                if (listener != null)
                {
                    listener.onIncomingConnection(this, socket);
                }
                if (aliveTime > 0)
                {
                    expire = System.currentTimeMillis() + aliveTime;
                }
            }
        }
        catch (Exception e)
        {
            error = e;
            stop = true;
        }
        isRunning = false;
        try
        {
            serverSocket.close();
        }
        catch (IOException e)
        {
        }
        serverSocket = null;

        if (listener != null)
        {
            listener.onServerTerminated(this, error);
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
    }

}
