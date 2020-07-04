package sip.core.message;

import com.allcam.gbgw.protocol.gb28181.message.XMLUtil;
import com.allcam.gbgw.protocol.sip.core.header.*;
import com.allcam.gbgw.protocol.sip.core.provider.SipParser;
import com.allcam.gbgw.protocol.sip.core.provider.SipStack;
import com.allcam.gbgw.protocol.sip.net.UdpPacket;

import java.util.Vector;

/**
 * Class BaseMessageOtp implements a generic SIP Message. It extends class
 * BaseMessage adding one-time-parsing functionality (it parses the entire
 * Message just when it is costructed). <p/> At the contrary, class BaseMessage
 * works in a just-in-time manner (it parses the message each time a particular
 * header field is requested).
 */
public abstract class BaseMessageOtp extends BaseMessage
{

    private RequestLine requestLine;

    private StatusLine statusLine;

    private Vector<Header> headers;

    protected String body;

    /**
     * Inits empty Message
     */
    private void init()
    {
        requestLine = null;
        statusLine = null;
        headers = null;
        body = null;
    }

    /**
     * Costructs a new empty Message
     */
    public BaseMessageOtp()
    {
        init();
        headers = new Vector<>();
    }

    /**
     * Costructs a new Message
     */
    public BaseMessageOtp(byte[] data, int offset, int len)
    {
        init();
        parseIt(new String(data, offset, len));
    }

    /**
     * Costructs a new Message
     */
    public BaseMessageOtp(UdpPacket packet)
    {
        init();
        parseIt(new String(packet.getData(), packet.getOffset(), packet.getLength()));
    }

    /**
     * Costructs a new Message
     */
    public BaseMessageOtp(String str)
    {
        init();
        parseIt(str);
    }

    /**
     * Sets the entire message
     */
    @Override
    public void setMessage(String str)
    {
        parseIt(str);
    }

    /**
     * Parses the Message from a String.
     */
    private void parseIt(String str)
    {
        SipParser par = new SipParser(str);
        String version = str.substring(0, 4);
        if (version.equalsIgnoreCase("SIP/"))
        {
            statusLine = par.getStatusLine();
        }
        else
        {
            requestLine = par.getRequestLine();
        }

        headers = new Vector<>();
        Header h = par.getHeader();
        while (h != null)
        {
            headers.addElement(h);
            h = par.getHeader();
        }
        ContentLengthHeader clh = getContentLengthHeader();
        if (clh != null)
        {
            int len = clh.getContentLength();
            body = par.getString(len);
        }
        else if (getContentTypeHeader() != null)
        {
            body = par.getRemainingString();
            if (body.length() == 0)
            {
                body = null;
            }
        }
    }

    /**
     * Gets string representation of Message
     */
    @Override
    public String toString()
    {
        StringBuilder str = new StringBuilder();
        if (requestLine != null)
        {
            str.append(requestLine.toString());
        }
        else if (statusLine != null)
        {
            str.append(statusLine.toString());
        }
        for (int i = 0; i < headers.size(); i++)
        {
            str.append(headers.elementAt(i));
        }
        str.append("\r\n");
        if (body != null)
        {
            str.append(body);
        }
        return str.toString();
    }

    /**
     * Gets message length
     */
    @Override
    public int getLength()
    {
        return toString().length();
    }

    // **************************** Requests ****************************/

    /**
     * Whether Message is a Request
     */
    @Override
    public boolean isRequest()
    {
        return requestLine != null;
    }

    /**
     * Whether Message is a <i>method</i> request
     */
    @Override
    public boolean isRequest(String method)
    {
        return requestLine != null && requestLine.getMethod().equalsIgnoreCase(method);
    }

    /**
     * Whether Message has Request-line
     */
    @Override
    protected boolean hasRequestLine()
    {
        return requestLine != null;
    }

    /**
     * Gets RequestLine in Message (Returns null if called for no request
     * message)
     */
    @Override
    public RequestLine getRequestLine()
    {
        return requestLine;
    }

    /**
     * Sets RequestLine of the Message
     */
    @Override
    public void setRequestLine(RequestLine rl)
    {
        requestLine = rl;
    }

    /**
     * Removes RequestLine of the Message
     */
    @Override
    public void removeRequestLine()
    {
        requestLine = null;
    }

    // **************************** Responses ****************************/

    /**
     * Whether Message is a Response
     */
    @Override
    public boolean isResponse()
        throws NullPointerException
    {
        return statusLine != null;
    }

    /**
     * Whether Message has Status-line
     */
    @Override
    protected boolean hasStatusLine()
    {
        return statusLine != null;
    }

    /**
     * Gets StautsLine in Message (Returns null if called for no response
     * message)
     */
    @Override
    public StatusLine getStatusLine()
    {
        return statusLine;
    }

    /**
     * Sets StatusLine of the Message
     */
    @Override
    public void setStatusLine(StatusLine sl)
    {
        statusLine = sl;
    }

    /**
     * Removes StatusLine of the Message
     */
    @Override
    public void removeStatusLine()
    {
        statusLine = null;
    }

    // **************************** Generic Headers
    // ****************************/

    /**
     * Removes Request\Status Line of the Message
     */
    @Override
    protected void removeFirstLine()
    {
        removeRequestLine();
        removeStatusLine();
    }

    /**
     * Gets the position of header <i>hname</i>.
     */
    protected int indexOfHeader(String hname)
    {
        for (int i = 0; i < headers.size(); i++)
        {
            Header h = headers.elementAt(i);
            if (hname.equalsIgnoreCase(h.getName()))
            {
                return i;
            }
        }
        return -1;
    }

    /**
     * Gets the first Header of specified name (Returns null if no Header is
     * found)
     */
    @Override
    public Header getHeader(String hname)
    {
        int i = indexOfHeader(hname);
        if (i < 0)
        {
            return null;
        }
        else
        {
            return headers.elementAt(i);
        }
    }

    /**
     * Gets a Vector of all Headers of specified name (Returns empty Vector if
     * no Header is found)
     */
    @Override
    public Vector<Header> getHeaders(String hname)
    {
        Vector<Header> v = new Vector<>();
        for (int i = 0; i < headers.size(); i++)
        {
            Header h = headers.elementAt(i);
            if (hname.equalsIgnoreCase(h.getName()))
            {
                v.addElement(h);
            }
        }
        return v;
    }

    /**
     * Adds Header at the top/bottom. The bottom is considered before the
     * Content-Length and Content-Type headers
     */
    @Override
    public void addHeader(Header header, boolean top)
    {
        if (top)
        {
            headers.insertElementAt(header, 0);
        }
        else
        {
            headers.addElement(header);
        }
    }

    /**
     * Adds a Vector of Headers at the top/bottom
     */
    @Override
    public void addHeaders(Vector<Header> headers, boolean top)
    {
        for (int i = 0; i < headers.size(); i++)
        {
            if (top)
            {
                this.headers.insertElementAt(headers.elementAt(i), i);
            }
            else
            {
                this.headers.addElement(headers.elementAt(i));
            }
        }
    }

    /**
     * Adds MultipleHeader(s) <i>mheader</i> at the top/bottom
     */
    @Override
    public void addHeaders(MultipleHeader mheader, boolean top)
    {
        if (mheader.isCommaSeparated())
        {
            addHeader(mheader.toHeader(), top);
        }
        else
        {
            addHeaders(mheader.getHeaders(), top);
        }
    }

    /**
     * Adds Header before the first header <i>refer_hname</i> .
     * <p>
     * If there is no header of such type, it is added at top
     */
    @Override
    public void addHeaderBefore(Header newHeader, String referNamer)
    {
        int i = indexOfHeader(referNamer);
        if (i < 0)
        {
            i = 0;
        }
        headers.insertElementAt(newHeader, i);
    }

    /**
     * Adds MultipleHeader(s) before the first header <i>refer_hname</i> .
     * <p>
     * If there is no header of such type, they are added at top
     */
    @Override
    public void addHeadersBefore(MultipleHeader mheader, String referName)
    {
        if (mheader.isCommaSeparated())
        {
            addHeaderBefore(mheader.toHeader(), referName);
        }
        else
        {
            int index = indexOfHeader(referName);
            if (index < 0)
            {
                index = 0;
            }
            Vector<Header> hs = mheader.getHeaders();
            for (int k = 0; k < hs.size(); k++)
            {
                headers.insertElementAt(hs.elementAt(k), index + k);
            }
        }
    }

    /**
     * Adds Header after the first header <i>refer_hname</i> .
     * <p>
     * If there is no header of such type, it is added at bottom
     */
    @Override
    public void addHeaderAfter(Header newHeader, String referName)
    {
        int i = indexOfHeader(referName);
        if (i >= 0)
        {
            i++;
        }
        else
        {
            i = headers.size();
        }
        headers.insertElementAt(newHeader, i);
    }

    /**
     * Adds MultipleHeader(s) after the first header <i>refer_hname</i> .
     * <p>
     * If there is no header of such type, they are added at bottom
     */
    @Override
    public void addHeadersAfter(MultipleHeader mheader, String referName)
    {
        if (mheader.isCommaSeparated())
        {
            addHeaderAfter(mheader.toHeader(), referName);
        }
        else
        {
            int index = indexOfHeader(referName);
            if (index >= 0)
            {
                index++;
            }
            else
            {
                index = headers.size();
            }
            Vector<Header> hs = mheader.getHeaders();
            for (int k = 0; k < hs.size(); k++)
            {
                headers.insertElementAt(hs.elementAt(k), index + k);
            }
        }
    }

    /**
     * Removes first Header of specified name
     */
    @Override
    public void removeHeader(String hname)
    {
        removeHeader(hname, true);
    }

    /**
     * Removes first (or last) Header of specified name.
     */
    @Override
    public void removeHeader(String hname, boolean first)
    {
        int index = -1;
        for (int i = 0; i < headers.size(); i++)
        {
            Header h = headers.elementAt(i);
            if (hname.equalsIgnoreCase(h.getName()))
            {
                index = i;
                if (first)
                {
                    i = headers.size();
                }
            }
        }
        if (index >= 0)
        {
            headers.removeElementAt(index);
        }
    }

    /**
     * Removes all Headers of specified name
     */
    @Override
    public void removeAllHeaders(String hname)
    {
        for (int i = 0; i < headers.size(); i++)
        {
            Header h = headers.elementAt(i);
            if (hname.equalsIgnoreCase(h.getName()))
            {
                headers.removeElementAt(i);
                i--;
            }
        }
    }

    /**
     * Sets the Header <i>hd</i> removing any previous headers of the same
     * type.
     */
    @Override
    public void setHeader(Header hd)
    {
        boolean first = true;
        String hname = hd.getName();
        for (int i = 0; i < headers.size(); i++)
        {
            Header h = headers.elementAt(i);
            if (hname.equalsIgnoreCase(h.getName()))
            {
                if (first)
                { // replace it
                    headers.setElementAt(h, i);
                    first = false;
                }
                else
                { // remove it
                    headers.removeElementAt(i);
                    i--;
                }
            }
        }
        if (first)
        {
            headers.addElement(hd);
        }
    }

    /**
     * Sets MultipleHeader <i>mheader</i>
     */
    @Override
    public void setHeaders(MultipleHeader mheader)
    {
        if (mheader.isCommaSeparated())
        {
            setHeader(mheader.toHeader());
        }
        else
        {
            boolean first = true;
            String hname = mheader.getName();
            for (int i = 0; i < headers.size(); i++)
            {
                Header h = headers.elementAt(i);
                if (hname.equalsIgnoreCase(h.getName()))
                {
                    if (first)
                    { // replace it
                        Vector<Header> hs = mheader.getHeaders();
                        for (int k = 0; k < hs.size(); k++)
                        {
                            headers.insertElementAt(hs.elementAt(k), i + k);
                        }
                        first = false;
                        i += hs.size() - 1;
                    }
                    else
                    { // remove it
                        headers.removeElementAt(i);
                        i--;
                    }
                }
            }
        }
    }

    // **************************** Specific Headers
    // ****************************/

    /**
     * Whether Message has Body
     */
    @Override
    public boolean hasBody()
    {
        return this.body != null;
    }

    /**
     * Gets body(content) type
     */
    @Override
    public String getBodyType()
    {
        return getContentTypeHeader().getContentType();
    }

    /**
     * Sets the message body
     */
    @Override
    public void setBody(String contentType, String body)
    {
        removeBody();
        if (body != null && body.length() > 0)
        {
            if (XMLUtil.XML_MANSCDP_TYPE.equals(contentType))
            {
                body = XMLUtil.appendXmlHead(SipStack.encoding, body);
            }
            setContentTypeHeader(new ContentTypeHeader(contentType));
            setContentLengthHeader(new ContentLengthHeader(body.getBytes(SipStack.encoding).length));
            this.body = body;
        }
        else
        {
            setContentLengthHeader(new ContentLengthHeader(0));
            this.body = null;
        }
    }

    /**
     * Gets message body. The end of body is evaluated from the Content-Length
     * header if present (SIP-RFC compliant), or from the end of message if no
     * Content-Length header is present (non-SIP-RFC compliant)
     */
    @Override
    public String getBody()
    {
        return this.body;
    }

    /**
     * Removes the message body (if it exists) and the final empty line
     */
    @Override
    public void removeBody()
    {
        removeContentLengthHeader();
        removeContentTypeHeader();
        this.body = null;
    }

}
