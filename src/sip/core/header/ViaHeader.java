package sip.core.header;

import com.allcam.gbgw.protocol.sip.core.address.SipURL;
import com.allcam.gbgw.protocol.sip.core.provider.SipParser;

/**
 * SIP Header Via. The Via header field indicates the transport used for the
 * transaction and identifies the location where the response is to be sent.
 * <BR>
 * When the UAC creates a request, it MUST insert a Via into that request. The
 * protocol name and protocol version in the header field is SIP and 2.0,
 * respectively. <BR>
 * The Via header field value MUST contain a branch parameter. This parameter is
 * used to identify the transaction created by that request. This parameter is
 * used by both the client and the server. <BR>
 * The branch parameter value MUST be unique across space and time for all
 * requests sent by the UA. The exceptions to this rule are CANCEL and ACK for
 * non-2xx responses. A CANCEL request will have the same value of the branch
 * parameter as the request it cancels. An ACK for a non-2xx response will also
 * have the same branch ID as the INVITE whose response it acknowledges.
 */
public class ViaHeader extends ParametricHeader
{
    private static final String RECEIVED_PARAM = "received";

    private static final String RPORT_PARAM = "rport";

    private static final String BRANCH_PARAM = "branch";

    private static final String MADDR_PARAM = "maddr";

    private static final String TTL_PARAM = "ttl";

    public ViaHeader(String hvalue)
    {
        super(SipHeaders.Via, hvalue);
    }

    public ViaHeader(Header hd)
    {
        super(hd);
    }

    public ViaHeader(String host, int port)
    {
        super(SipHeaders.Via, "SIP/2.0/UDP " + host + ":" + port);
    }

    public ViaHeader(String proto, String host, int port)
    {
        super(SipHeaders.Via, "SIP/2.0/" + proto.toUpperCase() + " " + host + ":" + port); // modified
    }

    /**
     * Gets the transport protocol
     */
    public String getProtocol()
    {
        SipParser par = new SipParser(value);
        return par.goTo('/').skipChar().goTo('/').skipChar().skipWSP().getString();
    }

    /**
     * Gets "sent-by" parameter
     */
    public String getSentBy()
    {
        SipParser par = new SipParser(value);
        par.goTo('/').skipChar().goTo('/').skipString().skipWSP();
        if (!par.hasMore())
        {
            return null;
        }
        return value.substring(par.getPos(), par.indexOfSeparator());
    }

    /**
     * Gets host of ViaHeader
     */
    public String getHost()
    {
        String sentby = getSentBy();
        SipParser par = new SipParser(sentby);
        par.goTo(':');
        if (par.hasMore())
        {
            return sentby.substring(0, par.getPos());
        }
        else
        {
            return sentby;
        }
    }

    /**
     * Returns boolean value indicating if ViaHeader has port
     */
    public boolean hasPort()
    {
        String sentby = getSentBy();
        if (sentby.indexOf(":") > 0)
            return true;
        return false;
    }

    /**
     * Gets port of ViaHeader
     */
    public int getPort()
    {
        SipParser par = new SipParser(getSentBy());
        par.goTo(':');
        if (par.hasMore())
        {
            return par.skipChar().getInt();
        }
        return -1;
    }

    /**
     * Makes a SipURL from ViaHeader
     */
    public SipURL getSipURL()
    {
        return new SipURL(getHost(), getPort());
    }

    /**
     * Checks if "branch" parameter is present
     */
    public boolean hasBranch()
    {
        return hasParameter(BRANCH_PARAM);
    }

    /**
     * Gets "branch" parameter
     */
    public String getBranch()
    {
        return getParameter(BRANCH_PARAM);
    }

    /**
     * Sets "branch" parameter
     */
    public void setBranch(String value)
    {
        setParameter(BRANCH_PARAM, value);
    }

    /**
     * Checks if "received" parameter is present
     */
    public boolean hasReceived()
    {
        return hasParameter(RECEIVED_PARAM);
    }

    /**
     * Gets "received" parameter
     */
    public String getReceived()
    {
        return getParameter(RECEIVED_PARAM);
    }

    /**
     * Sets "received" parameter
     */
    public void setReceived(String value)
    {
        setParameter(RECEIVED_PARAM, value);
    }

    /**
     * Checks if "rport" parameter is present
     */
    public boolean hasRport()
    {
        return hasParameter(RPORT_PARAM);
    }

    /**
     * Gets "rport" parameter
     */
    public int getRport()
    {
        String value = getParameter(RPORT_PARAM);
        if (value != null)
        {
            return Integer.parseInt(value);
        }
        else
        {
            return -1;
        }
    }

    /**
     * Sets "rport" parameter
     */
    public void setRport()
    {
        setParameter(RPORT_PARAM, null);
    }

    /**
     * Sets "rport" parameter
     */
    public void setRport(int port)
    {
        if (port < 0)
        {
            setParameter(RPORT_PARAM, null);
        }
        else
        {
            setParameter(RPORT_PARAM, Integer.toString(port));
        }
    }

    /**
     * Checks if "maddr" parameter is present
     */
    public boolean hasMaddr()
    {
        return hasParameter(MADDR_PARAM);
    }

    /**
     * Gets "maddr" parameter
     */
    public String getMaddr()
    {
        return getParameter(MADDR_PARAM);
    }

    /**
     * Sets "maddr" parameter
     */
    public void setMaddr(String value)
    {
        setParameter(MADDR_PARAM, value);
    }

    /**
     * Checks if "ttl" parameter is present
     */
    public boolean hasTtl()
    {
        return hasParameter(TTL_PARAM);
    }

    /**
     * Gets "ttl" parameter
     */
    public int getTtl()
    {
        String value = getParameter(TTL_PARAM);
        if (value != null)
        {
            return Integer.parseInt(value);
        }
        else
        {
            return -1;
        }
    }

    /**
     * Sets "ttl" parameter
     */
    public void setTtl(int ttl)
    {
        setParameter(TTL_PARAM, Integer.toString(ttl));
    }

}
