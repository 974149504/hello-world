package sip.core.header;

/**
 * Header is the base Class for all SIP Headers
 */
public class Header
{
    /**
     * The header type
     */
    protected String name;

    /**
     * The header string, without terminating CRLF
     */
    protected String value;

    /**
     * Creates a void Header.
     */
    protected Header()
    {
        name = null;
        value = null;
    }

    /**
     * Creates a new Header.
     */
    public Header(String hname, String hvalue)
    {
        name = hname;
        value = hvalue;
    }

    /**
     * Creates a new Header.
     */
    public Header(Header hd)
    {
        name = hd.getName();
        value = hd.getValue();
    }

    /**
     * Creates and returns a copy of the Header
     */
    @Override
    public Object clone()
    {
        return new Header(getName(), getValue());
    }

    /**
     * Whether the Header is equal to Object <i>obj</i>
     */
    @Override
    public boolean equals(Object obj)
    {
        try
        {
            Header hd = (Header)obj;
            return hd.getName().equals(this.getName()) && hd.getValue().equals(this.getValue());
        }
        catch (Exception e)
        {
            return false;
        }
    }

    /**
     * Gets name of Header
     */
    public String getName()
    {
        return name;
    }

    /**
     * Gets value of Header
     */
    public String getValue()
    {
        return value;
    }

    /**
     * Sets value of Header
     */
    public void setValue(String hvalue)
    {
        value = hvalue;
    }

    /**
     * Gets string representation of Header
     */
    @Override
    public String toString()
    {
        return name + ": " + value + "\r\n";
    }
}
