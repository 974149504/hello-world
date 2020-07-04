package sip.sdp;

import java.util.Vector;

/**
 * Class MediaDescriptor handles SDP media descpriptions.
 * <p>
 * A MediaDescriptor can be part of a SessionDescriptor, and contains details
 * that apply onto to a single media stream.
 * <p>
 * A single SessionDescriptor may convey zero or more MediaDescriptors.
 * <p>
 * In the current implementation, the MediaDescriptor consists of the m (media)
 * and c (connection information) fields, followed by zero or more a (attribute)
 * fields. The m field is mandatory for a MediaDescriptor.
 */
public class MediaDescriptor
{
    /**
     * Media field ('m').
     */
    private MediaField m;

    /**
     * Connection field ('c')
     */
    private ConnectionField c;

    /**
     * Vector of attribute fileds ('a')
     */
    private Vector<AttributeField> av;

    /**
     * Creates a new MediaDescriptor.
     *
     * @param md the cloned MediaDescriptor
     */
    public MediaDescriptor(MediaDescriptor md)
    {
        m = new MediaField(md.m);
        if (md.c != null)
        {
            c = new ConnectionField(md.c);
        }
        else
        {
            c = null;
        }
        /* HSC CHANGE STARTS */
        av = new Vector<>();
        /* HSC CHANGE ENDS */
        for (int i = 0; i < md.av.size(); i++)
        {
            av.addElement(new AttributeField(md.av.elementAt(i)));
        }
    }

    /**
     * Creates a new MediaDescriptor with m <i>media</i> and c <i>connection</i>.
     * No attribute is set by default.
     *
     * @param media      the MediaField
     * @param connection the ConnectionField, or null if no ConnectionField is present
     *                   in the MediaDescriptor
     */
    public MediaDescriptor(MediaField media, ConnectionField connection)
    {
        m = media;
        c = connection;
        /* HSC CHANGE BEGINS */
        av = new Vector<>();
        /* HSC CHANGE ENDS */
    }

    /**
     * Creates a new MediaDescriptor with m <i>media</i>, c <i>connection</i>,
     * and a <i>attribute</i>.
     *
     * @param media      the MediaField
     * @param connection the ConnectionField, or null if no ConnectionField is present
     *                   in the MediaDescriptor
     * @param attribute  the first AttributeField
     */
    public MediaDescriptor(MediaField media, ConnectionField connection, AttributeField attribute)
    {
        m = media;
        c = connection;
        /* HSC CHANGE BEGINS */
        av = new Vector<>();
        /* HSC CHANGE ENDS */
        if (attribute != null)
        {
            av.addElement(attribute);
        }
    }

    /**
     * Creates a new MediaDescriptor with m=<i>media</i> and c=<i>connection</i>,
     * with attributes 'a' equals to <i>attributes</i> (Vector of
     * AttributeField).
     *
     * @param media      the MediaField
     * @param connection the ConnectionField, or null if no ConnectionField is present
     *                   in the MediaDescriptor
     * @param attributes the Vector of AttributeField
     */
    public MediaDescriptor(MediaField media, ConnectionField connection, Vector<AttributeField> attributes)
    {
        m = media;
        c = connection;
        av = new Vector<>(attributes.size());
        av.setSize(attributes.size());
        for (int i = 0; i < attributes.size(); i++)
        {
            av.setElementAt(attributes.elementAt(i), i);
        }
    }

    /**
     * Creates a new MediaDescriptor with m <i>media</i>, c <i>connection</i>,
     * and a <i>attribute</i>.
     *
     * @param media      the media field vaule
     * @param connection the connection field vaule, or null if no connection field is
     *                   present in the MediaDescriptor
     * @param attribute  the first media attribute alue
     */
    public MediaDescriptor(String media, String connection, String attribute)
    {
        m = new MediaField(media);
        if (connection != null)
        {
            c = new ConnectionField(connection);
        }
        /* HSC CHANGE BEGINS */
        av = new Vector<>();
        /* HSC CHANGE ENDS */
        if (attribute != null)
        {
            av.addElement(new AttributeField(attribute));
        }
    }

    /**
     * Creates a new MediaDescriptor from String <i>str</i>.
     *
     * @param str
     *            the media field line
     */
    /*
     * public MediaDescriptor(String str) { SdpParser par=new SdpParser(str);
     * m=par.parseMediaField(); c=par.parseConnectionField(); av=new Vector();
     * AttributeField a=par.parseAttributeField(); while (a!=null) {
     * av.addElement(a); a=par.parseAttributeField(); } }
     */

    /**
     * Gets media.
     *
     * @return the MediaField
     */
    public MediaField getMedia()
    {
        return m;
    }

    /**
     * Gets connection information.
     *
     * @return the ConnectionField
     */
    public ConnectionField getConnection()
    {
        return c;
    }

    /**
     * Gets a Vector of attribute values.
     *
     * @return a Vector of AttributeField
     */
    public Vector<AttributeField> getAttributes()
    {
        Vector<AttributeField> v = new Vector<AttributeField>(av.size());
        /* HSC CHANGE ENDS */
        for (int i = 0; i < av.size(); i++)
        {
            v.addElement(av.elementAt(i));
        }
        return v;
    }

    /**
     * Adds a new attribute
     *
     * @param attribute the new AttributeField
     * @return this MediaDescriptor
     */
    public MediaDescriptor addAttribute(AttributeField attribute)
    {
        av.addElement(new AttributeField(attribute));
        return this;
    }

    /**
     * Whether it has a particular attribute
     *
     * @param aName the attribute name
     * @return true if found, otherwise returns null
     */
    public boolean hasAttribute(String aName)
    {
        for (int i = 0; i < av.size(); i++)
        {
            if (av.elementAt(i).getAttributeName().equals(aName))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets a particular attribute
     *
     * @param aName the attribute name
     * @return the AttributeField, or null if not found
     */
    public AttributeField getAttribute(String aName)
    {
        for (int i = 0; i < av.size(); i++)
        {
            AttributeField a = av.elementAt(i);
            if (a.getAttributeName().equals(aName))
            {
                return a;
            }
        }
        return null;
    }

    /**
     * Gets a Vector of attribute values of a particular attribute name.
     *
     * @param aName the attribute name
     * @return a Vector of AttributeFields
     */
    public Vector<AttributeField> getAttributes(String aName)
    {
        Vector<AttributeField> v = new Vector<>(av.size());
        for (int i = 0; i < av.size(); i++)
        {
            AttributeField a = av.elementAt(i);
            if (a.getAttributeName().equals(aName))
            {
                v.addElement(a);
            }
        }
        return v;
    }

    /**
     * Returns first codec.
     *
     */
    public RtpMap getRtpMap()
    {
        for (int i = 0; i < av.size(); i++)
        {
            AttributeField a = av.elementAt(i);
            if (RtpMap.NAME.equalsIgnoreCase(a.getAttributeName()))
            {
                return new RtpMap(a);
            }
        }
        return null;
    }

    /**
     * Gets a String rapresentation of the MediaDescriptor.
     *
     * @return the string representation
     */
    @Override
    public String toString()
    {
        StringBuilder str = new StringBuilder();
        str.append(m);
        if (c != null)
        {
            str.append(c);
        }
        for (int i = 0; i < av.size(); i++)
        {
            str.append(av.elementAt(i));
        }
        return str.toString();
    }

}
