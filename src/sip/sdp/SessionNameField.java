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
public class SessionNameField extends SdpField
{
    /**
     * Creates a new SessionNameField.
     */
    public SessionNameField(String sessionName)
    {
        super('s', sessionName);
    }

    /**
     * Creates a new void SessionNameField.
     */
    public SessionNameField()
    {
        super('s', " ");
    }

    /**
     * Creates a new SessionNameField.
     */
    public SessionNameField(SdpField sf)
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
