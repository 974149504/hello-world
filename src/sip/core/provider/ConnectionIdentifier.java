package sip.core.provider;

import com.allcam.gbgw.protocol.sip.net.IpAddress;

/**
 * ConnectionIdentifier is the reference for active transport connections.
 */
public class ConnectionIdentifier extends Identifier
{
    /**
     * Costructs a new ConnectionIdentifier.
     */
    public ConnectionIdentifier(String protocol, IpAddress remoteIpaddr, int remotePort)
    {
        super(getId(protocol, remoteIpaddr, remotePort));
    }

    /**
     * Costructs a new ConnectionIdentifier.
     */
    public ConnectionIdentifier(ConnectionIdentifier connId)
    {
        super(connId);
    }

    /**
     * Costructs a new ConnectionIdentifier.
     */
    public ConnectionIdentifier(String id)
    {
        super(id);
    }

    /**
     * Costructs a new ConnectionIdentifier.
     */
    public ConnectionIdentifier(ConnectedTransport conn)
    {
        super(getId(conn.getProtocol(), conn.getRemoteAddress(), conn.getRemotePort()));
    }

    /**
     * Gets the id.
     */
    private static String getId(String protocol, IpAddress remoteIpaddr, int remotePort)
    {
        return protocol + ":" + remoteIpaddr + ":" + remotePort;
    }

    @Override
    public boolean equals(Object obj)
    {
        return super.equals(obj);
    }

    @Override
    public int hashCode()
    {
        return super.hashCode();
    }

}
