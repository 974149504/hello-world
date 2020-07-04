package sip.sdp;

import java.util.Objects;

/**
 * SdpField rapresents a SDP line field. It is formed by a 'type' (char) and a
 * 'value' (String).
 * <p>
 * A SDP line field is of the form &lt;type&gt; = &lt;value&gt;
 */
public class SdpField
{
    private char type;

    String value;

    /**
     * Creates a new SdpField.
     *
     * @param sType  the field type
     * @param sValue the field value
     */
    public SdpField(char sType, String sValue)
    {
        type = sType;
        value = sValue;
    }

    /**
     * Creates a new SdpField.
     *
     * @param sf the SdpField clone
     */
    public SdpField(SdpField sf)
    {
        type = sf.type;
        value = sf.value;
    }

    /**
     * Creates a new SdpField based on a SDP line of the form <type>=<value>.
     * The SDP value terminats with the end of the String or with the first CR
     * or LF char.
     *
     * @param str the &lt;type&gt; = &lt;value&gt; line
     */
    public SdpField(String str)
    {
        SdpParser par = new SdpParser(str);
        SdpField sf = par.parseSdpField();
        type = sf.type;
        value = sf.value;
    }

    /**
     * Creates and returns a copy of the SdpField.
     *
     * @return a SdpField clone
     */
    @Override
    public Object clone()
    {
        return new SdpField(this);
    }

    /**
     * Whether the SdpField is equal to Object <i>obj</i>
     *
     * @return true if equal
     */
    @Override
    public boolean equals(Object obj)
    {
        try
        {
            SdpField sf = (SdpField)obj;
            if (type != sf.type)
            {
                return false;
            }
            return Objects.equals(value, sf.value);
        }
        catch (Exception e)
        {
            return false;
        }
    }

    /**
     * Gets the type of field
     *
     * @return the field type
     */
    public char getType()
    {
        return type;
    }

    /**
     * Gets the value
     *
     * @return the field value
     */
    public String getValue()
    {
        return value;
    }

    /**
     * Gets string representation of the SdpField
     *
     * @return the string representation
     */
    @Override
    public String toString()
    {
        return type + "=" + value + "\r\n";
    }

}
