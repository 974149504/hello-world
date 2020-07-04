package sip.core.header;

import com.allcam.gbgw.protocol.sip.core.address.NameAddress;
import com.allcam.gbgw.protocol.sip.core.address.SipURL;
import com.allcam.gbgw.protocol.sip.core.provider.SipParser;
import com.allcam.gbgw.protocol.tools.Parser;

/**
 * Abstract NameAddress Header is the base Class for SIP Headers such as
 * EndPointHeader. It contains a NameAddress, formed by a SIP URI and optionally
 * a display name.
 */
public abstract class NameAddressHeader extends ParametricHeader
{
    /**
     * Creates a new NameAddressHeader.
     */
    public NameAddressHeader(String hname, NameAddress nameaddr)
    {
        super(hname, nameaddr.toString());
    }

    /**
     * Creates a new NameAddressHeader.
     */
    public NameAddressHeader(String hname, SipURL url)
    {
        super(hname, url.toString());
    }

    /**
     * Creates a new NameAddressHeader.
     */
    public NameAddressHeader(Header hd)
    {
        super(hd);
    }

    /**
     * Gets NameAddress of NameAddressHeader (Returns null if NameAddress does
     * not exist - i.e. wildcard ContactHeader)
     */
    public NameAddress getNameAddress()
    {
        return (new SipParser(value)).getNameAddress();
    }

    /**
     * Sets NameAddress of NameAddressHeader
     */
    public void setNameAddress(NameAddress naddr)
    {
        value = naddr.toString();
    }

    // ***************** ParametricHeader's extended method *****************

    /**
     * Returns the index of the first semicolon before the first parameter.
     *
     * @returns the index of the semicolon before the first parameter, or -1 if
     * no parameter is present.
     */
    @Override
    protected int indexOfFirstSemi()
    {
        Parser par = new Parser(value);
        par.goToSkippingQuoted('>');
        if (par.getPos() == value.length())
        {
            par.setPos(0);
        }
        par.goToSkippingQuoted(';');
        if (par.getPos() < value.length())
        {
            return par.getPos();
        }
        else
        {
            return -1;
        }
    }

}
