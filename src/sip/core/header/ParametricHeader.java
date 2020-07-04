package sip.core.header;

import com.allcam.gbgw.protocol.sip.core.provider.SipParser;
import com.allcam.gbgw.protocol.tools.Parser;

import java.util.Vector;

/**
 * Abstract ParametricHeader is the base class for all SIP Headers that include
 * parameters
 */
public abstract class ParametricHeader extends Header
{
    /**
     * Costructs the abstract ParametricHeader.
     */
    protected ParametricHeader(String hname, String hvalue)
    {
        super(hname, hvalue);
    }

    /**
     * Costructs the abstract ParametricHeader.
     */
    protected ParametricHeader(Header hd)
    {
        super(hd);
    }

    /**
     * Gets the first word.
     *
     * @returns the first word or null if no value is present beafore
     *          parameters.
     */
    /*
     * protected String getFirstWord() { int index=indexOfFirstSemi(); if (index<0)
     * return value; else return value.substring(0,index).trim(); }
     */

    /**
     * Returns the index of the first semicolon before the first parameter.
     *
     * @return the index of the semicolon before the first parameter, or -1 if
     * no parameter is present.
     */
    protected int indexOfFirstSemi()
    {
        // int index=(new Parser(value)).goToSkippingQuoted(';').skipChar().skipWSP().getPos();
        int index = (new Parser(value)).goToSkippingQuoted(';').getPos();
        return (index >= value.length()) ? -1 : index;
    }

    /**
     * Gets the value of specified parameter.
     *
     * @return the parameter value or null if parameter does not exist or
     * doesn't have a value (i.e. in case of flag parameter).
     */
    public String getParameter(String name)
    {
        int index = indexOfFirstSemi();
        if (index < 0)
        {
            return null;
        }
        return (new SipParser((new Parser(getValue(), index)).skipChar()
            .skipWSP())).getParameter(name);
    }

    /**
     * Gets a String Vector of parameter names.
     *
     * @return a Vector of String
     */
    public Vector<String> getParameterNames()
    {
        int index = indexOfFirstSemi();
        if (index < 0)
        {
            return new Vector<>();
        }
        return (new SipParser((new Parser(getValue(), index)).skipChar()
            .skipWSP())).getParameters();
    }

    /**
     * Whether there is the specified parameter
     */
    public boolean hasParameter(String name)
    {
        int index = indexOfFirstSemi();
        if (index < 0)
        {
            return false;
        }
        return (new SipParser((new Parser(getValue(), index)).skipChar().skipWSP())).hasParameter(name);
    }

    /**
     * Whether there are any parameters
     */
    public boolean hasParameters()
    {
        return indexOfFirstSemi() >= 0;
    }

    /**
     * Removes all parameters (if any)
     */
    public void removeParameters()
    {
        if (!hasParameters())
        {
            return;
        }
        String header = getValue();
        int i = header.indexOf(';');
        header = header.substring(0, i);
        setValue(header);
    }

    /**
     * Removes specified parameter (if present)
     */
    public void removeParameter(String name)
    {
        int index = indexOfFirstSemi();
        if (index < 0)
        {
            return;
        }
        String header = getValue();
        Parser par = new Parser(header, index);
        while (par.hasMore())
        {
            int beginParam = par.getPos();
            par.skipChar();
            if (par.getWord(SipParser.param_separators).equals(name))
            {
                String top = header.substring(0, beginParam);
                par.goToSkippingQuoted(';');
                String bottom = "";
                if (par.hasMore())
                {
                    bottom = header.substring(par.getPos());
                }
                header = top.concat(bottom);
                setValue(header);
                return;
            }
            par.goTo(';');
        }
    }

    /**
     * Sets the value of a specified parameter. Zero-length String is returned
     * in case of flag parameter (without value).
     */
    public void setParameter(String name, String value)
    {
        if (getValue() == null)
        {
            setValue("");
        }
        if (hasParameter(name))
        {
            removeParameter(name);
        }
        String header = getValue();
        header = header.concat(";" + name);
        if (value != null)
        {
            header = header.concat("=" + value);
        }
        setValue(header);
    }
}
