package sip.net;

import java.net.InetAddress;

/**
 * IpAddress is an IP address.
 */
public class IpAddress
{

    /**
     * The host address/name
     */
    private String address;

    /**
     * The InetAddress
     */
    private InetAddress inetAddress;

    /**
     * Local IP address
     */
    public static String localIpAddress = "127.0.0.1";

    // ********************* Protected *********************

    /**
     * Creates an IpAddress
     */
    IpAddress(InetAddress iaddress)
    {
        init(null, iaddress);
    }

    /**
     * Inits the IpAddress
     */
    private void init(String address, InetAddress iaddress)
    {
        this.address = address;
        this.inetAddress = iaddress;
    }

    /**
     * Gets the InetAddress
     */
    InetAddress getInetAddress()
    {
        if (inetAddress == null)
        {
            try
            {
                inetAddress = InetAddress.getByName(address);
            }
            catch (java.net.UnknownHostException e)
            {
                inetAddress = null;
            }
        }
        return inetAddress;
    }

    // ********************** Public ***********************

    /**
     * Creates an IpAddress
     */
    public IpAddress(String address)
    {
        init(address, null);
    }

    /**
     * Creates an IpAddress
     */
    public IpAddress(IpAddress ipaddr)
    {
        init(ipaddr.address, ipaddr.inetAddress);
    }

    /**
     * Makes a copy
     */
    @Override
    public Object clone()
    {
        return new IpAddress(this);
    }

    /**
     * Wthether it is equal to Object <i>obj</i>
     */
    @Override
    public boolean equals(Object obj)
    {
        try
        {
            IpAddress ipaddr = (IpAddress)obj;
            return toString().equals(ipaddr.toString());
        }
        catch (Exception e)
        {
            return false;
        }
    }

    /**
     * Gets a String representation of the Object
     */
    @Override
    public String toString()
    {
        if (address == null && inetAddress != null)
        {
            address = inetAddress.getHostAddress();
        }
        return address;
    }

    // *********************** Static ***********************

    /**
     * Gets the IpAddress for a given fully-qualified host name.
     */
    public static IpAddress getByName(String hostAddr)
        throws java.net.UnknownHostException
    {
        InetAddress iaddr = InetAddress.getByName(hostAddr);
        return new IpAddress(iaddr);
    }

    /**
     * Sets the local IP address into the variable <i>localIpAddress</i>
     */
    public static void setLocalIpAddress(String contactIp)
    {
        localIpAddress = contactIp;
    }
}
