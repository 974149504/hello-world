package sip.core.header;

import com.allcam.gbgw.protocol.sip.core.provider.SipParser;

/**
 * SIP Header Content-Length
 */
public class ContentLengthHeader extends Header
{

    public ContentLengthHeader(int len)
    {
        super(SipHeaders.Content_Length, String.valueOf(len));
    }

    public ContentLengthHeader(Header hd)
    {
        super(hd);
    }

    /**
     * Gets content-length of ContentLengthHeader
     */
    public int getContentLength()
    {
        return (new SipParser(value)).getInt();
    }

    /**
     * Set content-length of ContentLengthHeader
     */
    public void setContentLength(int cLength)
    {
        value = String.valueOf(cLength);
    }

}
