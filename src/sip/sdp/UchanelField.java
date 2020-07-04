package sip.sdp;

/**
 * SDP session name field.
 * <p>
 * <BLOCKQUOTE>
 *
 * <PRE>
 * session-name-field = &quot;s=&quot; text CRLF
 * </PRE>
 *
 * </BLOCKQUOTE>
 */
public class UchanelField extends SdpField
{
    /**
     * Creates a new SessionNameField.
     */
    public UchanelField(String sessionName)
    {
        super('u', sessionName+":255");
    }

    /**
     * Creates a new void SessionNameField.
     */
    public UchanelField()
    {
        super('u', " ");
    }

    /**
     * Creates a new SessionNameField.
     */
    public UchanelField(SdpField sf)
    {
        super(sf);
    }

    /**
     * Gets the session name.
     */
    public String getSession()
    {
        return getValue();
    }

}
