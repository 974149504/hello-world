package sip.sdp;

import com.allcam.gbgw.protocol.tools.Parser;

/**
 * SDP connection field.
 * <p>
 * <BLOCKQUOTE>
 *
 * <PRE>
 * connection-field = &quot;c=&quot; nettype SP addrtype SP connection-address CRLF
 * ;a connection field must be present
 * ;in every media description or at the
 * ;session-level
 * </PRE>
 *
 * </BLOCKQUOTE>
 */
public class ConnectionField extends SdpField
{
    /**
     * Creates a new ConnectionField.
     */
    public ConnectionField(String connectionField)
    {
        super('c', connectionField);
    }

    /**
     * Creates a new ConnectionField.
     */
    public ConnectionField(String addressType, String address, int ttl, int num)
    {
        super('c', null);
        value = "IN " + addressType + " " + address;
        if (ttl > 0)
        {
            value += "/" + ttl;
        }
        if (num > 0)
        {
            value += "/" + num;
        }
    }

    /**
     * Creates a new ConnectionField.
     */
    public ConnectionField(String addressType, String address)
    {
        super('c', "IN " + addressType + " " + address);
    }

    /**
     * Creates a new ConnectionField.
     */
    public ConnectionField(SdpField sf)
    {
        super(sf);
    }

    /**
     * Gets the connection address.
     */
    public String getAddressType()
    {
        String type = (new Parser(value)).skipString().getString();
        return type;
    }

    /**
     * Gets the connection address.
     */
    public String getAddress()
    {
        String address = (new Parser(value)).skipString().skipString().getString();
        int i = address.indexOf("/");
        if (i < 0)
        {
            return address;
        }
        else
        {
            return address.substring(0, i);
        }
    }

    /**
     * Gets the TTL.
     */
    public int getTTL()
    {
        String address = (new Parser(value)).skipString().skipString().getString();
        int i = address.indexOf("/");
        if (i < 0)
        {
            return 0;
        }
        int j = address.indexOf("/", i);
        if (j < 0)
        {
            return Integer.parseInt(address.substring(i));
        }
        else
        {
            return Integer.parseInt(address.substring(i, j));
        }
    }

    /**
     * Gets the number of addresses.
     */
    public int getNum()
    {
        String address = (new Parser(value)).skipString().skipString().getString();
        int i = address.indexOf("/");
        if (i < 0)
        {
            return 0;
        }
        int j = address.indexOf("/", i);
        if (j < 0)
        {
            return 0;
        }
        return Integer.parseInt(address.substring(j));
    }

}
