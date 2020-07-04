package sip.core.header;

/**
 * SIP Status-line, i.e. the first line of a response message
 */
public class StatusLine
{
    protected int code;

    protected String reason;

    /**
     * Construct StatusLine
     */
    public StatusLine(int c, String r)
    {
        code = c;
        reason = r;
    }

    /**
     * Create a new copy of the request-line
     */
    @Override
    public Object clone()
    {
        return new StatusLine(getCode(), getReason());
    }

    /**
     * Indicates whether some other Object is "equal to" this StatusLine
     */
    @Override
    public boolean equals(Object obj)
    {
        try
        {
            StatusLine r = (StatusLine)obj;
            return r.getCode() == (this.getCode()) && r.getReason().equals(this.getReason());
        }
        catch (Exception e)
        {
            return false;
        }
    }

    @Override
    public String toString()
    {
        return "SIP/2.0 " + code + " " + reason + "\r\n";
    }

    public int getCode()
    {
        return code;
    }

    public String getReason()
    {
        return reason;
    }

    public boolean isSuccess()
    {
        return code >= 200 && code < 300;
    }
}
