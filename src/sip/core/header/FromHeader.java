package sip.core.header;

import com.allcam.gbgw.protocol.sip.core.address.NameAddress;
import com.allcam.gbgw.protocol.sip.core.address.SipURL;

/**
 * SIP Header From. The From header field indicates the logical identity of the
 * initiator of the request, possibly the user's address-of-record. Like the To
 * header field, it contains a URI and optionally a display name. <BR>
 * The From field MUST contain a new "tag" parameter, chosen by the UAC.
 */
public class FromHeader extends EndPointHeader
{
    // public FromHeader()
    // { super(SipHeaders.From);
    // }

    public FromHeader(NameAddress nameaddr)
    {
        super(SipHeaders.From, nameaddr);
    }

    public FromHeader(SipURL url)
    {
        super(SipHeaders.From, url);
    }

    public FromHeader(NameAddress nameaddr, String tag)
    {
        super(SipHeaders.From, nameaddr, tag);
    }

    public FromHeader(SipURL url, String tag)
    {
        super(SipHeaders.From, url, tag);
    }

    public FromHeader(Header hd)
    {
        super(hd);
    }
}
