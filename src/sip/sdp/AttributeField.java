package sip.sdp;

/* HSC CHANGE START */
/* import org.zoolu.tools.Parser; */
/* HSC CHANGE END */

/**
 * SDP attribute field.
 * <p>
 * <BLOCKQUOTE>
 *
 * <PRE>
 * attribute-fields = &quot;a=&quot; (att-field &quot;:&quot; att-value) | att-field CRLF
 * </PRE>
 *
 * </BLOCKQUOTE>
 * @author Beowulf
 */
public class AttributeField extends SdpField
{
    /**
     * Creates a new AttributeField.
     */
    public AttributeField(String attribute)
    {
        super('a', attribute);
    }

    /**
     * Creates a new AttributeField.
     */
    public AttributeField(String attribute, String aValue)
    {
        super('a', attribute + ":" + aValue);
    }

    /**
     * Creates a new AttributeField.
     */
    public AttributeField(SdpField sf)
    {
        super(sf);
    }

    /**
     * Creates a new AttributeField.
     */
    public AttributeField(RtpMap rtpMap)
    {
        super('a', rtpMap.toString());
    }

    /**
     * Gets the attribute name.
     */
    public String getAttributeName()
    {
        int i = value.indexOf(":");
        if (i < 0)
        {
            return value;
        }
        else
        {
            return value.substring(0, i);
        }
    }

    /**
     * Gets the attribute value.
     */
    public String getAttributeValue()
    {
        int i = value.indexOf(":");
        if (i < 0)
        {
            return null;
        }
        else
        {
            return value.substring(i + 1);
        }
    }

}
