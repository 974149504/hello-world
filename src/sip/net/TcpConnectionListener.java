package sip.net;

/**
 * Listener for TcpConnection events.
 */
public interface TcpConnectionListener
{
    /**
     * When new data is received through the TcpConnection.
     */
    void onReceivedData(TcpConnection tcpConn, byte[] data, int len);

    /**
     * When TcpConnection terminates.
     */
    void onConnectionTerminated(TcpConnection tcpConn, Exception error);
}
