package sip.sdp;

import com.allcam.gbgw.protocol.tools.Parser;

/**
 * SDP attribute field.
 * <p>
 * <BLOCKQUOTE>
 *
 * <PRE>
 * time-fields = 1*( &quot;t=&quot; start-time SP stop-time *(CRLF repeat-fields) CRLF) [zone-adjustments CRLF]
 * </PRE>
 *
 * </BLOCKQUOTE>
 */
public class TimeField extends SdpField
{
    /**
     * Creates a new TimeField.
     */
    public TimeField(String timeField)
    {
        super('t', timeField);
    }

    /**
     * Creates a new TimeField.
     */
    public TimeField(String start, String stop)
    {
        super('t', start + " " + stop);
    }

    /**
     * Creates a new void TimeField.
     */
    public TimeField()
    {
        super('t', "0 0");
    }

    /**
     * Creates a new TimeField.
     */
    public TimeField(SdpField sf)
    {
        super(sf);
    }

    /**
     * Gets the start time.
     */
    public String getStartTime()
    {
        return (new Parser(value)).getString();
    }

    /**
     * Gets the stop time.
     */
    public String getStopTime()
    {
        return (new Parser(value)).skipString().getString();
    }

}
