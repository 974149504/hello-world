package sip.core.header;

import com.allcam.gbgw.protocol.sip.core.address.SipURL;

/**
 * SIP Request-line, i.e. the first line of a request message <BR>
 * The initial Request-URI of the message SHOULD be set to the value of the URI
 * in the To field.
 */
public class RequestLine
{
    protected String method;

    protected SipURL url;

    /**
     * Construct RequestLine <i>request</i> with <i>sipurl</i> as recipient
     */
    public RequestLine(String request, String sipUrl)
    {
        method = request;
        url = new SipURL(sipUrl);
    }

    public RequestLine(String request, SipURL sipUrl)
    {
        method = request;
        url = sipUrl;
    }

    /**
     * Create a new copy of the RequestLine
     */
    @Override
    public Object clone()
    {
        return new RequestLine(getMethod(), getAddress());
    }

    /**
     * Indicates whether some other Object is "equal to" this RequestLine
     */
    @Override
    public boolean equals(Object obj)
    {
        try
        {
            RequestLine r = (RequestLine)obj;
            return r.getMethod().equals(this.getMethod()) && r.getAddress().equals(this.getAddress());
        }
        catch (Exception e)
        {
            return false;
        }
    }

    @Override
    public String toString()
    {
        return method + " " + url + " SIP/2.0\r\n";
    }

    public String getMethod()
    {
        return method;
    }

    public SipURL getAddress()
    {
        return url;
    }
}
