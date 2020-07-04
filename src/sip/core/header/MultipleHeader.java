package sip.core.header;

import com.allcam.gbgw.protocol.sip.core.provider.SipParser;

import java.util.Vector;

/**
 * MultipleHeader can be used to handle SIP headers that support comma-separated
 * (multiple-header) rapresentation, as explaned in section 7.3.1 of RFC 3261.
 */
public class MultipleHeader
{
    /**
     * The header type
     */
    protected String name;

    /**
     * Vector of header values (as Strings)
     */
    protected Vector<String> values;

    /**
     * whether to be rapresented with a comma-separated(compact) header line or
     * multiple header lines
     */
    protected boolean compact;

    protected MultipleHeader()
    {
        name = null;
        values = new Vector<>();
        compact = true;
    }

    /**
     * Costructs a MultipleHeader named <i>hname</i>
     */
    public MultipleHeader(String hname)
    {
        name = hname;
        values = new Vector<>();
        compact = true;
    }

    /**
     * Costructs a MultipleHeader named <i>hname</i> from a Vector of header
     * values (as Strings).
     */
    public MultipleHeader(String hname, Vector<String> hvalues)
    {
        name = hname;
        values = hvalues;
        compact = true;
    }

    /**
     * Costructs a MultipleHeader from a Vector of Headers. Each Header can be a
     * single header or a multiple-comma-separated header.
     */
    public MultipleHeader(Vector<Header> headers)
    {
        name = headers.elementAt(0).getName();
        values = new Vector<>(headers.size());
        for (int i = 0; i < headers.size(); i++)
        {
            addBottom(headers.elementAt(i));
        }
        compact = false;
    }

    /**
     * Costructs a MultipleHeader from a comma-separated header
     */
    public MultipleHeader(Header hd)
    {
        name = hd.getName();
        values = new Vector<>();
        SipParser par = new SipParser(hd.getValue());
        int comma = par.indexOfCommaHeaderSeparator();
        while (comma >= 0)
        {
            values.addElement(par.getString(comma - par.getPos()).trim());
            // skip comma
            par.skipChar();
            comma = par.indexOfCommaHeaderSeparator();
        }
        values.addElement(par.getRemainingString().trim());
        compact = true;
    }

    /**
     * Costructs a MultipleHeader from a MultipleHeader
     */
    public MultipleHeader(MultipleHeader mhd)
    {
        name = mhd.getName();
        values = mhd.getValues();
        compact = mhd.isCommaSeparated();
    }

    /**
     * Checks if Header <i>hd</i> contains comma-separated multi-header
     */
    public static boolean isCommaSeparated(Header hd)
    {
        SipParser par = new SipParser(hd.getValue());
        return par.indexOfCommaHeaderSeparator() >= 0;
    }

    /**
     * Sets the MultipleHeader rappresentation as comma-separated or multiple
     * headers
     */
    public void setCommaSeparated(boolean comma_separated)
    {
        compact = comma_separated;
    }

    /**
     * Whether the MultipleHeader rappresentation is comma-separated or multiple
     * headers
     */
    public boolean isCommaSeparated()
    {
        return compact;
    }

    /**
     * Gets the size of th MultipleHeader
     */
    public int size()
    {
        return values.size();
    }

    /**
     * Whether it is empty
     */
    public boolean isEmpty()
    {
        return values.isEmpty();
    }

    /**
     * Creates and returns a copy of Header
     */
    @Override
    public Object clone()
    {
        return new MultipleHeader(getName(), getValues());
    }

    /**
     * Indicates whether some other Object is "equal to" this Header
     */
    @Override
    public boolean equals(Object obj)
    {
        MultipleHeader hd = (MultipleHeader)obj;
        return hd.getName().equals(this.getName()) && hd.getValues().equals(this.getValues());
    }

    /**
     * Gets name of Header
     */
    public String getName()
    {
        return name;
    }

    /**
     * Gets a vector of header values
     */
    public Vector<String> getValues()
    {
        return values;
    }

    /**
     * Sets header values
     */
    public void setValues(Vector<String> v)
    {
        values = v;
    }

    /**
     * Gets a vector of headers
     */
    public Vector<Header> getHeaders()
    {
        Vector<Header> v = new Vector<>(values.size());
        for (int i = 0; i < values.size(); i++)
        {
            Header h = new Header(name, values.elementAt(i));
            v.addElement(h);
        }
        return v;
    }

    /**
     * Sets header values
     */
    public void setHeaders(Vector<Header> hdv)
    {
        values = new Vector<>(hdv.size());
        for (int i = 0; i < hdv.size(); i++)
        {
            values.addElement(hdv.elementAt(i).getValue());
        }
    }

    /**
     * Gets the i-value
     */
    public String getValue(int i)
    {
        return values.elementAt(i);
    }

    /**
     * Adds top
     */
    public void addTop(Header hd)
    {
        values.insertElementAt(hd.getValue(), 0);
    }

    /**
     * Gets top Header
     */
    public Header getTop()
    {
        return new Header(name, values.firstElement());
    }

    /**
     * Removes top Header
     */
    public void removeTop()
    {
        values.removeElementAt(0);
    }

    /**
     * Adds bottom
     */
    public void addBottom(Header hd)
    {
        if (!MultipleHeader.isCommaSeparated(hd))
        {
            values.addElement(hd.getValue());
        }
        else
        {
            addBottom(new MultipleHeader(hd));
        }
    }

    /**
     * Adds other MultipleHeader at bottom
     */
    public void addBottom(MultipleHeader mhd)
    {
        for (int i = 0; i < mhd.size(); i++)
        {
            values.addElement(mhd.getValue(i));
        }
    }

    /**
     * Gets bottom Header
     */
    public Header getBottom()
    {
        return new Header(name, values.lastElement());
    }

    /**
     * Removes bottom Header
     */
    public void removeBottom()
    {
        values.removeElementAt(values.size() - 1);
    }

    /**
     * Gets an Header containing the comma-separated(compact) representation.
     */
    public Header toHeader()
    {
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < values.size() - 1; i++)
        {
            str.append(values.elementAt(i)).append(", ");
        }
        if (values.size() > 0)
        {
            str.append(values.elementAt(values.size() - 1));
        }
        return new Header(name, str.toString());
    }

    /**
     * Gets comma-separated(compact) or multi-headers(extended) representation.<BR>
     * Note that an empty header is rapresentated as:<BR> - empty String (i.e.
     * ""), for multi-headers(extended) rapresentation, - empty-value Header
     * (i.e. "HeaderName: \r\n"), for comma-separated(compact) rapresentation.
     */
    @Override
    public String toString()
    {
        if (compact)
        {
            StringBuilder str = new StringBuilder(name + ": ");
            for (int i = 0; i < values.size() - 1; i++)
            {
                str.append(values.elementAt(i)).append(", ");
            }
            if (values.size() > 0)
            {
                str.append(values.elementAt(values.size() - 1));
            }
            return str + "\r\n";
        }
        else
        {
            StringBuilder str = new StringBuilder();
            for (int i = 0; i < values.size(); i++)
            {
                str.append(name).append(": ").append(values.elementAt(i)).append("\r\n");
            }
            return str.toString();
        }
    }

}
