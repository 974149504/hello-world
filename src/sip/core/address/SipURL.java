package sip.core.address;

import com.allcam.gbgw.protocol.sip.core.provider.SipParser;
import com.allcam.gbgw.protocol.tools.Parser;

import java.util.Objects;
import java.util.Vector;

/**
 * <p>
 * Class <i>SipURL</i> implements SIP URLs.
 * <p>
 * A SIP URL is a string of the form of: <BR>
 * <BLOCKQUOTE>
 *
 * <PRE>
 * &amp;nbsp&amp;nbsp sip:[user@]hostname[:port][;parameters]
 * </PRE>
 *
 * </BLOCKQUOTE>
 * <p>
 * If <i>port</i> number is ommitted, -1 is returned
 */
public class SipURL
{
    private static final String TRANSPORT_PARAM = "transport";

    private static final String MADDR_PARAM = "maddr";

    private static final String TTL_PARAM = "ttl";

    private static final String LR_PARAM = "lr";

    protected String url;

    /**
     * Creates a new SipURL based on a hostname or on a sip url as
     * sip:[user@]hostname[:port][;param1=value1]..
     */
    public SipURL(String sipurl)
    {
        if (sipurl.startsWith("sip:"))
        {
            url = sipurl;
        }
        else
        {
            url = "sip:" + sipurl;
        }
    }

    /**
     * Creates a new SipURL
     */
    public SipURL(String username, String hostname)
    {
        init(username, hostname, -1);
    }

    /**
     * Creates a new SipURL
     */
    public SipURL(String hostname, int port)
    {
        init(null, hostname, port);
    }

    /**
     * Creates a new SipURL
     */
    public SipURL(String username, String hostname, int port)
    {
        init(username, hostname, port);
    }

    /**
     * Inits the SipURL
     */
    private void init(String username, String hostname, int portnumber)
    {
        StringBuilder sb = new StringBuilder("sip:");
        if (username != null)
        {
            sb.append(username);
            if (username.indexOf('@') < 0)
            {
                sb.append('@');
                sb.append(hostname);
            }
        }
        else
        {
            sb.append(hostname);
        }

        if (portnumber > 0)
        {
            if (username == null || username.indexOf(':') < 0)
            {
                sb.append(":").append(portnumber);
            }
        }

        url = sb.toString();
    }

    /**
     * Creates and returns a copy of the URL
     */
    @Override
    public Object clone()
    {
        return new SipURL(url);
    }

    /**
     * Indicates whether some other Object is "equal to" this URL
     */
    @Override
    public boolean equals(Object obj)
    {
        SipURL newurl = (SipURL)obj;
        return url.equals(newurl.toString());
    }

    /**
     * Gets user name of SipURL (Returns null if user name does not exist)
     */
    public String getUserName()
    {
        int begin = 4;
        int end = url.indexOf('@', begin);
        if (end < 0)
        {
            return null;
        }
        else
        {
            return url.substring(begin, end);
        }
    }

    /**
     * Gets host of SipURL
     */
    public String getHost()
    {
        char[] hostTerminators = {':', ';', '?'};
        Parser par = new Parser(url);
        // skip "sip:user@"
        int begin = par.indexOf('@');
        if (begin < 0)
        {
            begin = 4;
        }
        else
        {
            begin++;
        }
        par.setPos(begin);
        int end = par.indexOf(hostTerminators);
        if (end < 0)
        {
            return url.substring(begin);
        }
        else
        {
            return url.substring(begin, end);
        }
    }

    /**
     * Gets port of SipURL; returns -1 if port is not specidfied
     */
    public int getPort()
    {
        char[] portTerminators = {';', '?'};
        // skip "sip:"
        Parser par = new Parser(url, 4);
        int begin = par.indexOf(':');
        if (begin < 0)
        {
            return -1;
        }
        else
        {
            begin++;
            par.setPos(begin);
            int end = par.indexOf(portTerminators);
            if (end < 0)
            {
                return Integer.parseInt(url.substring(begin));
            }
            else
            {
                return Integer.parseInt(url.substring(begin, end));
            }
        }
    }

    /**
     * Gets boolean value to indicate if SipURL has user name
     */
    public boolean hasUserName()
    {
        return getUserName() != null;
    }

    /**
     * Gets boolean value to indicate if SipURL has port
     */
    public boolean hasPort()
    {
        return getPort() >= 0;
    }

    /**
     * Whether two SipURLs are equals
     */
    public boolean equals(SipURL sipUrl)
    {
        return (Objects.equals(url, sipUrl.url));
    }

    /**
     * Gets string representation of URL
     */
    @Override
    public String toString()
    {
        return url;
    }

    /**
     * Gets the value of specified parameter.
     *
     * @return null if parameter does not exist.
     */
    public String getParameter(String name)
    {
        SipParser par = new SipParser(url);
        return ((SipParser)par.goTo(';').skipChar()).getParameter(name);
    }

    /**
     * Gets a String Vector of parameter names.
     *
     * @return null if no parameter is present
     */
    public Vector<String> getParameters()
    {
        SipParser par = new SipParser(url);
        return ((SipParser)par.goTo(';').skipChar()).getParameters();
    }

    /**
     * Whether there is the specified parameter
     */
    public boolean hasParameter(String name)
    {
        SipParser par = new SipParser(url);
        return ((SipParser)par.goTo(';').skipChar()).hasParameter(name);
    }

    /**
     * Whether there are any parameters
     */
    public boolean hasParameters()
    {
        return url != null && url.indexOf(';') >= 0;
    }

    /**
     * Adds a new parameter without a value
     */
    public void addParameter(String name)
    {
        url = url + ";" + name;
    }

    /**
     * Adds a new parameter with value
     */
    public void addParameter(String name, String value)
    {
        if (value != null)
        {
            url = url + ";" + name + "=" + value;
        }
        else
        {
            url = url + ";" + name;
        }
    }

    /**
     * Removes all parameters (if any)
     */
    public void removeParameters()
    {
        int index = url.indexOf(';');
        if (index >= 0)
        {
            url = url.substring(0, index);
        }
    }

    /**
     * Removes specified parameter (if present)
     */
    public void removeParameter(String name)
    {
        int index = url.indexOf(';');
        if (index < 0)
        {
            return;
        }
        Parser par = new Parser(url, index);
        while (par.hasMore())
        {
            int beginParam = par.getPos();
            par.skipChar();
            if (par.getWord(SipParser.param_separators).equals(name))
            {
                String top = url.substring(0, beginParam);
                par.goToSkippingQuoted(';');
                String bottom = "";
                if (par.hasMore())
                {
                    bottom = url.substring(par.getPos());
                }
                url = top.concat(bottom);
                return;
            }
            par.goTo(';');
        }
    }

    /**
     * Gets the value of transport parameter.
     *
     * @return null if no transport parameter is present.
     */
    public String getTransport()
    {
        return getParameter(TRANSPORT_PARAM);
    }

    /**
     * Whether transport parameter is present
     */
    public boolean hasTransport()
    {
        return hasParameter(TRANSPORT_PARAM);
    }

    /**
     * Adds transport parameter
     */
    public void addTransport(String proto)
    {
        addParameter(TRANSPORT_PARAM, proto.toLowerCase());
    }

    /**
     * Gets the value of maddr parameter.
     *
     * @return null if no maddr parameter is present.
     */
    public String getMaddr()
    {
        return getParameter(MADDR_PARAM);
    }

    /**
     * Whether maddr parameter is present
     */
    public boolean hasMaddr()
    {
        return hasParameter(MADDR_PARAM);
    }

    /**
     * Adds maddr parameter
     */
    public void addMaddr(String maddr)
    {
        addParameter(MADDR_PARAM, maddr);
    }

    /**
     * Gets the value of ttl parameter.
     *
     * @return 1 if no ttl parameter is present.
     */
    public int getTtl()
    {
        try
        {
            return Integer.parseInt(getParameter(TTL_PARAM));
        }
        catch (Exception e)
        {
            return 1;
        }
    }

    /**
     * Whether ttl parameter is present
     */
    public boolean hasTtl()
    {
        return hasParameter(TTL_PARAM);
    }

    /**
     * Adds ttl parameter
     */
    public void addTtl(int ttl)
    {
        addParameter(TTL_PARAM, Integer.toString(ttl));
    }

    /**
     * Whether lr (loose-route) parameter is present
     */
    public boolean hasLr()
    {
        return hasParameter(LR_PARAM);
    }

    /**
     * Adds lr parameter
     */
    public void addLr()
    {
        addParameter(LR_PARAM);
    }
}
