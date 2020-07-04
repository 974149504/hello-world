package sip.core.header;

import com.allcam.gbgw.protocol.sip.core.provider.SipParser;
import com.allcam.gbgw.protocol.tools.Parser;

import java.util.Date;

/**
 * SIP Header Expires.
 * <p>
 * Note: for backward compatibility with legacy implementations the date format
 * is still supported although it has been deprecated in RFC 3261.
 */
public class ExpiresHeader extends SipDateHeader
{
    public ExpiresHeader(String hvalue)
    {
        super(SipHeaders.Expires, hvalue);
    }

    /**
     * Creates a new ExpiresHeader based on a Date value.
     */
    public ExpiresHeader(Date date)
    {
        super(SipHeaders.Expires, date);
    }

    /**
     * Creates a new ExpiresHeader with delta-seconds as value.
     */
    public ExpiresHeader(int seconds)
    {
        super(SipHeaders.Expires, (String)null);
        value = String.valueOf(seconds);
    }

    public ExpiresHeader(Header hd)
    {
        super(hd);
    }

    /**
     * Gets boolean value to indicate if expiry value of ExpiresHeader is in
     * date format.
     */
    public boolean isDate()
    {
        return value.contains("GMT");
    }

    /**
     * Gets value of ExpiresHeader as delta-seconds
     */
    public int getDeltaSeconds()
    {
        int secs;
        if (isDate())
        {
            Date date = (new SipParser((new Parser(value)).getStringUnquoted())).getDate();
            secs = (int)((date.getTime() - System.currentTimeMillis()) / 1000);
            if (secs < 0)
            {
                secs = 0;
            }
        }
        else
        {
            secs = (new SipParser(value)).getInt();
        }

        return secs;
    }

    /**
     * Gets value of ExpiresHeader as absolute date
     */
    @Override
    public Date getDate()
    {
        Date date = null;
        if (isDate())
        {
            date = (new SipParser((new Parser(value)).getStringUnquoted())).getDate();
        }
        else
        {
            long secs = getDeltaSeconds();
            if (secs >= 0)
            {
                date = new Date(System.currentTimeMillis() + secs * 1000);
            }
        }
        return date;
    }

}
