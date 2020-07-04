package sip.net;

import javax.net.ssl.*;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * TcpSocket provides a uniform interface to TCP transport protocol, regardless
 * J2SE or J2ME is used.
 */
public class TcpSocket
{
    /**
     * Socket
     */
    private Socket socket;

    /**
     * Creates a new TcpSocket
     */
    TcpSocket()
    {
        socket = null;
    }

    /**
     * Creates a new TcpSocket
     */
    TcpSocket(Socket sock)
    {
        socket = sock;
    }

    private static boolean lock;

    /**
     * Creates a new UdpSocket
     */
    public TcpSocket(IpAddress ipaddr, int port, String host)
        throws java.io.IOException
    {
        if (host == null)
        {
            socket = new Socket();
        }
        else
        {
            SSLSocketFactory f = (SSLSocketFactory) SSLSocketFactory.getDefault();
            socket = f.createSocket();
        }

        if (lock)
        {
            throw new java.io.IOException();
        }
        lock = true;

        try
        {
            socket.connect(new InetSocketAddress(ipaddr.toString(), port), 10000);
        }
        catch (java.io.IOException e)
        {
            lock = false;
            throw e;
        }

        if (host != null)
        {
            HostnameVerifier hv = HttpsURLConnection.getDefaultHostnameVerifier();
            SSLSession s = ((SSLSocket)socket).getSession();
            if (!hv.verify(host, s))
            {
                lock = false;
                throw new java.io.IOException();
            }
        }
        lock = false;
    }

    /**
     * Closes this socket.
     */
    public void close()
        throws java.io.IOException
    {
        socket.close();
    }

    /**
     * Gets the address to which the socket is connected.
     */
    public IpAddress getAddress()
    {
        return new IpAddress(socket.getInetAddress());
    }

    /**
     * Gets an input stream for this socket.
     */
    public InputStream getInputStream()
        throws java.io.IOException
    {
        return socket.getInputStream();
    }

    /**
     * Gets the local address to which the socket is bound.
     */
    public IpAddress getLocalAddress()
    {
        return new IpAddress(socket.getLocalAddress());
    }

    /**
     * Gets the local port to which this socket is bound.
     */
    public int getLocalPort()
    {
        return socket.getLocalPort();
    }

    /**
     * Gets an output stream for this socket.
     */
    public OutputStream getOutputStream()
        throws java.io.IOException
    {
        return socket.getOutputStream();
    }

    /**
     * Gets the remote port to which this socket is connected.
     */
    public int getPort()
    {
        return socket.getPort();
    }

    /**
     * Gets the socket timeout.
     */
    public int getSoTimeout()
        throws java.net.SocketException
    {
        return socket.getSoTimeout();
    }

    /**
     * Enables/disables the socket timeou, in milliseconds.
     */
    public void setSoTimeout(int timeout)
        throws java.net.SocketException
    {
        socket.setSoTimeout(timeout);
    }

    /**
     * Converts this object to a String.
     */
    @Override
    public String toString()
    {
        return socket.toString();
    }

}
