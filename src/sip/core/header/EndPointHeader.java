package sip.core.header;

import com.allcam.gbgw.protocol.sip.core.address.NameAddress;
import com.allcam.gbgw.protocol.sip.core.address.SipURL;
import com.allcam.gbgw.protocol.sip.core.provider.SipParser;

/**
 * Abstract EndPointHeader is the base Class for SIP Headers such as FromHeader,
 * ToHeader. The "tag" parameter is used in the EndPointHeader. It serves as a
 * general mechanism to identify a dialog, which is the combination of the
 * Call-ID along with two tags, one from each participant in the dialog.
 */
public abstract class EndPointHeader extends NameAddressHeader
{
    /**
     * EndPoint parameters that should be removed from the returned NameAddress.
     * This tries to resolve a bug (?) of SIP when using SIP URL parameters in a
     * name-address within an EndPointHeader that may have some header
     * parameters.
     */
    private static final String[] ENDPOINT_PARAMS = {"tag", "expires"};

    /**
     * Creates a new EndPointHeader.
     */
    public EndPointHeader(String hname, NameAddress nameaddr)
    {
        super(hname, nameaddr);
    }

    /**
     * Creates a new EndPointHeader.
     */
    public EndPointHeader(String hname, SipURL url)
    {
        super(hname, url);
    }

    /**
     * Creates a new EndPointHeader.
     */
    public EndPointHeader(String hname, NameAddress nameaddr, String tag)
    {
        super(hname, nameaddr);
        if (tag != null)
        {
            setParameter("tag", tag);
        }
    }

    /**
     * Creates a new EndPointHeader.
     */
    public EndPointHeader(String hname, SipURL url, String tag)
    {
        super(hname, url);
        if (tag != null)
        {
            setParameter("tag", tag);
        }
    }

    /**
     * Creates a new EndPointHeader.
     */
    public EndPointHeader(Header hd)
    {
        super(hd);
    }

    /**
     * Gets 'tag' parameter.
     */
    public String getTag()
    {
        return this.getParameter("tag");
    }

    /**
     * Whether it has 'tag' parameter.
     */
    public boolean hasTag()
    {
        return this.hasParameter("tag");
    }

    /**
     * Gets NameAddress from the EndPointHeader. <br>
     * It extends the NameAddressHeader.getNameAddress() method, by removing
     * eventual EndPointHeader field parameters (e.g. 'tag' param) from the
     * returnerd NameAddress.
     *
     * @return the end point NameAddress or null if NameAddress does not exist
     * (that leads to the wildcard in case of ContactHeader)
     */
    @Override
    public NameAddress getNameAddress()
    {
        NameAddress naddr = (new SipParser(value)).getNameAddress();
        // patch for removing eventual 'tag' or other EndPointHeader parameters
        // from NameAddress
        SipURL url = naddr.getAddress();
        for (String endpointParam : ENDPOINT_PARAMS)
        {
            if (url.hasParameter(endpointParam))
            {
                url.removeParameter(endpointParam);
                naddr = new NameAddress(naddr.getDisplayName(), url);
            }
        }
        return naddr;
    }

}
