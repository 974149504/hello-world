package sip.core.header;

import com.allcam.gbgw.protocol.tools.Parser;

/**
 * SIP Event header (RFC 3265).
 * <p>
 * Event is a request header field (request-header). It appears in SUBSCRIBE and
 * NOTIFY requests. It provides a event-package name.
 */
public class EventHeader extends ParametricHeader
{
    /**
     * State delimiters.
     */
    private static final char[] DELIM = {',', ';', ' ', '\t', '\n', '\r'};

    /**
     * Costructs a new EventHeader.
     */
    public EventHeader(String eventPackage)
    {
        super(SipHeaders.EVENT, eventPackage);
    }

    /**
     * Costructs a new EventHeader.
     */
    public EventHeader(String eventPackage, String id)
    {
        super(SipHeaders.EVENT, eventPackage);
        if (id != null)
        {
            this.setParameter("id", id);
        }
    }

    /**
     * Costructs a new EventHeader.
     */
    public EventHeader(Header hd)
    {
        super(hd);
    }

    /**
     * Gets the event name.
     */
    public String getEvent()
    {
        return new Parser(value).getWord(DELIM);
    }

    /**
     * Gets 'id' parameter.
     */
    public String getId()
    {
        return this.getParameter("id");
    }

    /**
     * Whether it has 'id' parameter.
     */
    public boolean hasId()
    {
        return this.hasParameter("id");
    }

}
