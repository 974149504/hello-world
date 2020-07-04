package sip.sdp;

import com.allcam.gbgw.protocol.tools.Parser;

import java.util.Vector;

/**
 * SDP media field.
 * <p>
 * <BLOCKQUOTE>
 *
 * <PRE>
 * media-field = &quot;m=&quot; media SP port [&quot;/&quot; integer] SP proto 1*(SP fmt) CRLF
 * </PRE>
 *
 * </BLOCKQUOTE>
 */
public class MediaField extends SdpField
{
    /**
     * Creates a new MediaField.
     */
    public MediaField(String mediaField)
    {
        super('m', mediaField);
    }

    /**
     * Creates a new MediaField.
     */
    public MediaField(String media, int port, int num, String transport, String formats)
    {
        super('m', null);
        value = media + " " + port;
        if (num > 0)
        {
            value += "/" + num;
        }
        value += " " + transport + " " + formats;
    }

    /**
     * Creates a new MediaField.
     *
     * @param formatlist a Vector of media formats (properly a Vector of Strings)
     */
    public MediaField(String media, int port, int num, String transport,
        /* HSC CHANGE BEGINS */
        Vector<String> formatlist)
    {
        /* HSC CHANGE ENDS */
        super('m', null);
        value = media + " " + port;
        if (num > 0)
        {
            value += "/" + num;
        }
        value += " " + transport;
        for (int i = 0; i < formatlist.size(); i++)
        {
            value += " " + formatlist.elementAt(i);
        }
    }

    /**
     * Creates a new SdpField.
     */
    public MediaField(SdpField sf)
    {
        super(sf);
    }

    /**
     * Gets the media type.
     */
    public String getMedia()
    {
        return new Parser(value).getString();
    }

    /**
     * Gets the media port.
     */
    public int getPort()
    {
        String port = (new Parser(value)).skipString().getString();
        int i = port.indexOf('/');
        if (i < 0)
        {
            return Integer.parseInt(port);
        }
        else
        {
            return Integer.parseInt(port.substring(0, i));
        }
    }

    /**
     * Gets the transport protocol.
     */
    public String getTransport()
    {
        return (new Parser(value)).skipString().skipString().getString();
    }

    /**
     * Gets the media formats.
     */
    public String getFormats()
    {
        return (new Parser(value)).skipString().skipString().skipString().skipWSP().getRemainingString();
    }

    /**
     * Gets the media formats as a Vector of String.
     */
    /* HSC CHANGE BEGINS */
    public Vector<String> getFormatList()
    {
        Vector<String> formatlist = new Vector<String>();
        /* HSC CHANGE ENDS */
        Parser par = new Parser(value);
        par.skipString().skipString().skipString();
        while (par.hasMore())
        {
            String fmt = par.getString();
            if (fmt != null && fmt.length() > 0)
            {
                formatlist.addElement(fmt);
            }
        }
        return formatlist;
    }

}
