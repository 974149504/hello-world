package sip.sdp;

import com.allcam.gbgw.protocol.tools.Parser;

/**
 * SDP origin field.
 * <p>
 * <BLOCKQUOTE>
 *
 * <PRE>
 * origin-field = &quot;o=&quot; username SP sess-id SP sess-version SP
 * nettype SP addrtype SP unicast-address CRLF
 * </PRE>
 *
 * </BLOCKQUOTE>
 */
public class OriginField extends SdpField
{
    /**
     * Creates a new OriginField.
     */
    public OriginField(String origin)
    {
        super('o', origin);
    }

    /**
     * Creates a new OriginField.
     */
    public OriginField(String username, String sessId, String sessVersion, String addrtype, String address)
    {
        super('o', username + " " + sessId + " " + sessVersion + " IN " + addrtype + " " + address);
    }

    /**
     * Creates a new OriginField.
     */
    public OriginField(String username, String sessId, String sessVersion, String address)
    {
        super('o', username + " " + sessId + " " + sessVersion + " IN IP4 " + address);
    }

    /**
     * Creates a new OriginField.
     */
    public OriginField(SdpField sf)
    {
        super(sf);
    }

    /**
     * Gets the user name.
     */
    public String getUserName()
    {
        return (new Parser(value)).getString();
    }

    /**
     * Gets the session id.
     */
    public String getSessionId()
    {
        return (new Parser(value)).skipString().getString();
    }

    /**
     * Gets the session version.
     */
    public String getSessionVersion()
    {
        return (new Parser(value)).skipString().skipString().getString();
    }

    /**
     * Gets the address type.
     */
    public String getAddressType()
    {
        return (new Parser(value)).skipString().skipString().skipString().skipString().getString();
    }

    /**
     * Gets the address.
     */
    public String getAddress()
    {
        return (new Parser(value)).skipString().skipString().skipString().skipString().skipString().getString();
    }

}
