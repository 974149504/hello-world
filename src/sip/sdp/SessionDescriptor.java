package sip.sdp;

import com.allcam.common.utils.DateUtil;
import com.allcam.gbgw.protocol.sip.net.IpAddress;

import java.util.Vector;

/**
 * Class SessionDescriptor handles SIP message bodys formatted according to the
 * Session Description Protocol (SDP).
 * <p>
 * A session description consists of a session-level description (details that
 * apply to the whole session and all media streams) and zero or more
 * media-level descriptions (details that apply onto to a single media stream).
 * <p>
 * The session-level part starts with a `v=' line and continues to the first
 * media-level section. The media description starts with an `m=' line and
 * continues to the next media description or end of the whole session
 * description. In general, session-level values are the default for all media
 * unless overridden by an equivalent media-level value.
 * <p>
 * In the current implementation, the session-level description consists of the
 * v, o, s, c, and t SDP fields (lines).
 */
public class SessionDescriptor
{
    /**
     * Version filed.
     */
    private SdpField v;

    /**
     * Origin filed.
     */
    private OriginField o;
    /**
     * Version filed.
     */
    private UchanelField u;
    /**
     * Session-name filed.
     */
    private SessionNameField s;

    /**
     * Connection filed.
     */
    private ConnectionField c;

    /**
     * Time filed.
     */
    private TimeField t;

    private YField y;

    private FField f;

    /**
     * extern filed.
     */
    private SdpField extern;

    /**
     * Vector of session attributes (as Vector of SdpFields).
     */
    private Vector<AttributeField> av;

    /**
     * Vector of MediaDescriptors.
     */
    private Vector<MediaDescriptor> media;

    /**
     * Inits the SessionDescriptor.
     */
    private void init(OriginField origin, SessionNameField session, ConnectionField connection, TimeField time)
    {
        v = new SdpField('v', "0");
        o = origin;
        s = session;
        c = connection;
        t = time;
        av = new Vector<>();
        media = new Vector<>();
    }
    /**
     * Inits the SessionDescriptor.
     */
    private void init(OriginField origin, SessionNameField session,UchanelField chanel, ConnectionField connection, TimeField time)
    {
        v = new SdpField('v', "0");
        o = origin;
        s = session;
        u = chanel;
        c = connection;
        t = time;
        av = new Vector<>();
        media = new Vector<>();
    }
    /**
     * Creates a new SessionDescriptor.
     *
     * @param sd the SessionDescriptor clone
     */
    public SessionDescriptor(SessionDescriptor sd)
    {
        init(new OriginField(sd.o), new SessionNameField(sd.s), new ConnectionField(sd.c), new TimeField(sd.t));
        for (int i = 0; i < sd.media.size(); i++)
        {
            media.addElement(new MediaDescriptor(sd.media.elementAt(i)));
        }
    }

    /**
     * Creates a new SessionDescriptor specifing o, s, c, and t fields.
     *
     * @param origin     the OriginField
     * @param session    the SessionNameField
     * @param connection the ConnectionField
     * @param time       the TimeField
     */
    public SessionDescriptor(OriginField origin, SessionNameField session, ConnectionField connection, TimeField time)
    {
        init(origin, session, connection, time);
    }

    /**
     * Creates a new SessionDescriptor specifing o, s, c, and t fields.
     *
     * @param origin     the origin value
     * @param session    the session value
     * @param connection the connection value
     * @param time       the time value
     */
    public SessionDescriptor(String origin, String session, String connection, String time)
    {
        init(new OriginField(origin),
            new SessionNameField(session),
            new ConnectionField(connection),
            new TimeField(time));
    }

    public void incrementOLine()
    {
        String str = o.getSessionVersion();
        int intObj2 = Integer.parseInt(str);
        intObj2++;
        o = new OriginField(o.getUserName(), o.getSessionId(), Integer.toString(intObj2), o.getAddress());
    }

    /**
     * Creates a new SessionDescriptor.
     * <p>
     * with: <br>
     * o=<i>owner</i> <br>
     * s=Session SIP/SDP <br>
     * c=IP4 <i>address</i> <br>
     * t=0 0
     * <p>
     * if <i>address</i>==null, '127.0.0.1' is used <br>
     * if <i>owner</i>==null, 'user@'<i>address</i> is used
     *
     * @param owner   the owner
     * @param address the IPv4 address
     */
    public SessionDescriptor(String owner, String address)
    {
        if (address == null)
        {
            address = IpAddress.localIpAddress;
        }
        if (owner == null)
        {
            owner = "user@" + address;
        }
        init(new OriginField(owner, "0", "0", address),
            new SessionNameField("Play"),
            new ConnectionField("IP4", address),
            new TimeField());
    }

    /**
     * Creates a new SessionDescriptor.
     * <p>
     * with: <br>
     * o=<i>owner</i> <br>
     * s=Session SIP/SDP <br>
     * u=owner:1 <br>
     * c=IP4 <i>address</i> <br>
     * t=startTime endTime
     * <p>
     * if <i>address</i>==null, '127.0.0.1' is used <br>
     * if <i>owner</i>==null, 'user@'<i>address</i> is used
     *
     * @param owner the owner
     */
    public SessionDescriptor(String owner, String startTime, String endTime)
    {
        String address = IpAddress.localIpAddress;
        if (owner == null)
        {
            owner = "user@" + address;
        }

//        long start = DateUtil.timeStr2Long(GbUtils.fromGbTime(startTime),"yyyy-MM-dd hh:mm:ss")/1000;
//        long stop = DateUtil.timeStr2Long(GbUtils.fromGbTime(endTime),"yyyy-MM-dd hh:mm:ss")/1000;
        long start = DateUtil.timeStr2Long(startTime,"yyyy-MM-dd hh:mm:ss")/1000;
        long stop = DateUtil.timeStr2Long(endTime,"yyyy-MM-dd hh:mm:ss")/1000;

        init(new OriginField(owner, "0", "0", address),
            new SessionNameField("Playback"),
            new UchanelField(owner),
            new ConnectionField("IP4", address),
            new TimeField(String.valueOf(start), String.valueOf(stop)));
    }

    /**
     * Creates a default SessionDescriptor.
     * <p>
     * o=user@127.0.0.1 s=Session SIP/SDP c=127.0.0.1 t=0 0
     */
    public SessionDescriptor()
    {
        String address = IpAddress.localIpAddress;
        String owner = "user@" + address;
        init(new OriginField(owner, "0", "0", address),
            new SessionNameField("Session SIP/SDP"),
            new ConnectionField("IP4", address),
            new TimeField());
    }

    /**
     * Creates a new SessionDescriptor from String <i>sdp</i>
     *
     * @param sdp the entire SDP
     */
    public SessionDescriptor(String sdp)
    {
        SdpParser par = new SdpParser(sdp);
        // parse mandatory fields
        v = par.parseSdpField('v');
        if (v == null)
        {
            v = new SdpField('v', "0");
        }
        o = par.parseOriginField();
        if (o == null)
        {
            o = new OriginField("unknown");
        }
        s = par.parseSessionNameField();
        if (s == null)
        {
            s = new SessionNameField();
        }
        c = par.parseConnectionField();
        if (c == null)
        {
            c = new ConnectionField("IP4", "0.0.0.0");
        }
        t = par.parseTimeField();
        if (t == null)
        {
            t = new TimeField();
        }
        while (par.hasMore() && (!par.startsWith("a=") && !par.startsWith("m=")))
        { // skip
            // unknown
            // lines..
            par.goToNextLine();
        }
        // parse session attributes
        av = new Vector<>();
        while (par.hasMore() && par.startsWith("a="))
        {
            AttributeField attribute = par.parseAttributeField();
            av.addElement(attribute);
        }
        // parse media descriptors
        media = new Vector<>();
        MediaDescriptor md;
        while ((md = par.parseMediaDescriptor()) != null)
        {
            addMediaDescriptor(md);
        }
        y = par.parseYField();
        f = par.parseFField();
    }

    /**
     * Sets the origin 'o' field.
     *
     * @param origin the OriginField
     * @return this SessionDescriptor
     */
    public SessionDescriptor setOrigin(OriginField origin)
    {
        o = origin;
        return this;
    }

    /**
     * Gets the origin 'o' field
     */
    public OriginField getOrigin()
    {
        return o;
    }

    /**
     * Sets the session-name 's' field.
     *
     * @param session the SessionNameField
     * @return this SessionDescriptor
     */
    public SessionDescriptor setSessionName(SessionNameField session)
    {
        s = session;
        return this;
    }

    /**
     * Gets the session-name 's' field
     */
    public SessionNameField getSessionName()
    {
        return s;
    }

    /**
     * Sets the connection-information 'c' field.
     *
     * @param connection the ConnectionField
     * @return this SessionDescriptor
     */
    public SessionDescriptor setConnection(ConnectionField connection)
    {
        c = connection;
        return this;
    }

    /**
     * Gets the connection-information 'c' field
     */
    public ConnectionField getConnection()
    {
        return c;
    }

    /**
     * Sets the time 't' field.
     *
     * @param time the TimeField
     * @return this SessionDescriptor
     */
    public SessionDescriptor setTime(TimeField time)
    {
        t = time;
        return this;
    }

    /**
     * Sets the 'y' field.
     *
     * @param y the YField
     * @return this SessionDescriptor
     */
    public SessionDescriptor setY(YField y)
    {
        this.y = y;
        return this;
    }

    /**
     * Sets the 'f' field.
     *
     * @param f the FField
     * @return this SessionDescriptor
     */
    public SessionDescriptor setF(FField f)
    {
        this.f = f;
        return this;
    }

    /**
     * Gets the time 't' field
     */
    public TimeField getTime()
    {
        return t;
    }

    /**
     * Adds a new attribute for a particular media
     *
     * @param media     the MediaField
     * @param attribute an AttributeField
     * @return this SessionDescriptor
     */
    public SessionDescriptor addMedia(MediaField media, AttributeField attribute)
    { // printlog("DEBUG:
        // media:
        // "+media,5);
        // printlog("DEBUG: attribute: "+attribute,5);
        addMediaDescriptor(new MediaDescriptor(media, null, attribute));
        return this;
    }

    /**
     * Adds a new media.
     *
     * @param media      the MediaField
     * @param attributes Vector of AttributeField
     * @return this SessionDescriptor
     */
    public SessionDescriptor addMedia(MediaField media, Vector<AttributeField> attributes)
    {
        // printlog("DEBUG:
        // printlog("DEBUG: attribute: "+attributes,5);
        addMediaDescriptor(new MediaDescriptor(media, null, attributes));
        return this;
    }

    /**
     * Adds a new MediaDescriptor
     *
     * @param mediaDesc a MediaDescriptor
     * @return this SessionDescriptor
     */
    public SessionDescriptor addMediaDescriptor(MediaDescriptor mediaDesc)
    { // printlog("DEBUG:
        // media
        // desc:
        // "+mediaDesc,5);
        media.addElement(mediaDesc);
        return this;
    }

    /**
     * Adds a Vector of MediaDescriptors
     *
     * @param mediaDescs Vector if MediaDescriptor
     * @return this SessionDescriptor
     */
    public SessionDescriptor addMediaDescriptors(Vector<MediaDescriptor> mediaDescs)
    {
        // media.addAll(mediaDescs);
        for (int i = 0; i < mediaDescs.size(); i++)
        {
            media.addElement(mediaDescs.elementAt(i));
        }
        return this;
    }

    /**
     * Gets all MediaDescriptors
     */
    public Vector<MediaDescriptor> getMediaDescriptors()
    {
        return media;
    }

    /**
     * Removes all MediaDescriptors
     */
    public SessionDescriptor removeMediaDescriptor(String mediaType)
    {
        for (int i = media.size() - 1; i >= 0; i--)
        {
            if (media.elementAt(i).getMedia().getMedia().equals(mediaType))
            {
                media.removeElementAt(i);
            }
        }
        return this;
    }

    /**
     * Removes all MediaDescriptors
     */
    public SessionDescriptor removeMediaDescriptors()
    {
        // media.clear(); // not
        // supported by J2ME..
        media.setSize(0);
        return this;
    }

    /**
     * Gets the first MediaDescriptor of a particular media.
     *
     * @param mediaType the media type
     * @return the MediaDescriptor
     */
    public MediaDescriptor getMediaDescriptor(String mediaType)
    {
        for (int i = 0; i < media.size(); i++)
        {
            MediaDescriptor md = media.elementAt(i);
            if (md.getMedia().getMedia().equals(mediaType))
            {
                return md;
            }
        }
        return null;
    }

    /**
     * Adds a Vector of session attributes.
     *
     * @param attributeFields Vector of AttributeFields
     * @return this SessionDescriptor
     */
    public SessionDescriptor addAttributes(Vector<AttributeField> attributeFields)
    {
        for (int i = 0; i < attributeFields.size(); i++)
        {
            addAttribute(attributeFields.elementAt(i));
        }
        return this;
    }

    /**
     * Adds a new attribute
     *
     * @param attribute the new AttributeField
     * @return this MediaDescriptor
     */
    public SessionDescriptor addAttribute(AttributeField attribute)
    {
        av.addElement(new AttributeField(attribute));
        return this;
    }

    /**
     * Removes all session attributes.
     */
    public SessionDescriptor removeAttributes()
    {
        av.setSize(0);
        return this;
    }

    /**
     * Whether it has a particular attribute
     *
     * @param attributeName the attribute name
     * @return true if found, otherwise returns null
     */
    public boolean hasAttribute(String attributeName)
    {
        for (int i = 0; i < av.size(); i++)
        {
            if (av.elementAt(i).getAttributeName().equals(attributeName))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets a Vector of attribute values.
     *
     * @return a Vector of AttributeField
     */
    public Vector<AttributeField> getAttributes()
    {
        Vector<AttributeField> v = new Vector<>(av.size());
        for (int i = 0; i < av.size(); i++)
        {
            v.addElement(av.elementAt(i));
        }
        return v;
    }

    /**
     * Gets the first AttributeField of a particular attribute name.
     *
     * @param attributeName the attribute name
     * @return the AttributeField, or null if not found
     */
    public AttributeField getAttribute(String attributeName)
    {
        for (int i = 0; i < av.size(); i++)
        {
            AttributeField af = av.elementAt(i);
            if (af.getAttributeName().equals(attributeName))
            {
                return af;
            }
        }
        return null;
    }

    /**
     * Gets a Vector of attribute values of a particular attribute name.
     *
     * @param attributeName the attribute name
     * @return a Vector of AttributeField
     */
    public Vector<AttributeField> getAttributes(String attributeName)
    {
        Vector<AttributeField> v = new Vector<>(av.size());
        for (int i = 0; i < av.size(); i++)
        {
            AttributeField a = av.elementAt(i);
            if (a.getAttributeName().equals(attributeName))
            {
                v.addElement(a);
            }
        }
        return v;
    }

    public SessionDescriptor addSdpField(SdpField field)
    {
        extern = field;
        return this;
    }

    /**
     * Gets a String rapresentation
     */
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        if (v != null)
        {
            sb.append(v.toString());
        }
        if (o != null)
        {
            sb.append(o.toString());
        }
        if (s != null)
        {
            sb.append(s.toString());
        }
        if (u != null)
        {
            sb.append(u.toString());
        }
        if (c != null)
        {
            sb.append(c.toString());
        }
        if (t != null)
        {
            sb.append(t.toString());
        }
        for (int i = 0; i < av.size(); i++)
        {
            sb.append(av.elementAt(i).toString());
        }
        for (int i = 0; i < media.size(); i++)
        {
            sb.append(media.elementAt(i).toString());
        }
        if (y != null)
        {
            sb.append(y.toString());
        }
        if (f != null)
        {
            sb.append(f.toString());
        }
        if (extern != null)
        {
            sb.append(extern.toString());
        }
        return sb.toString();
    }

}
