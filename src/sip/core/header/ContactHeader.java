package sip.core.header;

import com.allcam.gbgw.protocol.sip.core.address.NameAddress;
import com.allcam.gbgw.protocol.sip.core.address.SipURL;
import com.allcam.gbgw.protocol.sip.core.provider.SipParser;
import com.allcam.gbgw.protocol.tools.DateFormat;
import com.allcam.gbgw.protocol.tools.Parser;

import java.util.Date;

/**
 * SIP Header Contact. The Contact header field provides a SIP or SIPS URI that
 * can be used to contact that specific instance of the UA for subsequent
 * requests. The Contact header field MUST be present and contain exactly one
 * SIP URI in any request that can result in the establishment of a dialog (i.e.
 * INVITEs).
 * <p>
 * Note: for backward compatibility with legacy implementations the date format
 * in 'expires' parameter is still supported although it has been deprecated in
 * RFC 3261.
 */
public class ContactHeader extends EndPointHeader
{
    /**
     * Creates a ContactHeader with '*' as contact value
     */
    public ContactHeader()
    {
        super(new Header(SipHeaders.Contact, null));
        value = "*";
    }

    public ContactHeader(NameAddress nameaddr)
    {
        super(SipHeaders.Contact, nameaddr);
    }

    public ContactHeader(NameAddress nameaddr, String qvalue, String icsi)
    {
        super(SipHeaders.Contact, nameaddr);
        if (qvalue != null)
        {
            setParameter("q", qvalue);
        }
        setParameter("+g.3gpp.icsi-ref", icsi);
    }

    public ContactHeader(SipURL url)
    {
        super(SipHeaders.Contact, url);
    }

    public ContactHeader(Header hd)
    {
        super(hd);
    }

    public ContactHeader setExpires(Date expire)
    {
        setParameter("expires", "\"" + DateFormat.formatEEEddMMM(expire) + "\"");
        return this;
    }

    public ContactHeader setExpires(int secs)
    {
        setParameter("expires", Integer.toString(secs));
        return this;
    }

    public boolean isStar()
    {
        return value.indexOf('*') >= 0;
    }

    public boolean hasExpires()
    {
        return hasParameter("expires");
    }

    public boolean isExpired()
    {
        return getExpires() == 0;
    }

    public int getExpires()
    {
        int secs = -1;
        String expParam = getParameter("expires");
        if (expParam != null)
        {
            if (expParam.contains("GMT"))
            {
                Date date = (new SipParser((new Parser(expParam)).getStringUnquoted())).getDate();
                secs = (int)((date.getTime() - System.currentTimeMillis()) / 1000);
                if (secs < 0)
                {
                    secs = 0;
                }
            }
            else
            {
                secs = (new SipParser(expParam)).getInt();
            }
        }
        return secs;
    }

    public Date getExpiresDate()
    {
        Date date = null;
        String expParam = getParameter("expires");
        if (expParam != null)
        {
            if (expParam.contains("GMT"))
            {
                date = (new SipParser((new Parser(expParam)).getStringUnquoted())).getDate();
            }
            else
            {
                long secs = (new SipParser(expParam)).getInt();
                if (secs >= 0)
                {
                    date = new Date(System.currentTimeMillis() + secs * 1000);
                }
            }
        }
        return date;
    }

    public ContactHeader removeExpires()
    {
        removeParameter("expires");
        return this;
    }
}
