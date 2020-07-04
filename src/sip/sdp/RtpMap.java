package sip.sdp;

import com.allcam.common.utils.StringUtil;

/**
 * SdpField rtpmap:
 * @author Beowulf
 */
public class RtpMap
{
    public static final String NAME = "rtpmap";

    private String codec;

    private int payload;

    private int clock;

    public static RtpMap ps()
    {
        return new RtpMap("PS", 96, 90000);
    }

    public static RtpMap mpeg4()
    {
        return new RtpMap("MPEG4", 97, 90000);
    }

    public static RtpMap h264()
    {
        return new RtpMap("H264", 98, 90000);
    }

    public RtpMap()
    {

    }

    public RtpMap(String codec, int payload, int clock)
    {
        this.codec = codec;
        this.payload = payload;
        this.clock = clock;
    }

    public RtpMap(AttributeField a)
    {
        String value = a.getAttributeValue();
        int cdIdx = value.indexOf(" ");
        int clIdx = value.indexOf("/");
        if (cdIdx >= 0 && clIdx > cdIdx)
        {
            String plStr = value.substring(0, cdIdx);
            codec = value.substring(cdIdx + 1, clIdx);
            String clockStr = value.substring(clIdx + 1);
            payload = StringUtil.toInt(plStr, 0);
            clock = StringUtil.toInt(clockStr.trim(), 0);
        }
    }

    public String getCodec()
    {
        return codec;
    }

    public void setCodec(String codec)
    {
        this.codec = codec;
    }

    public int getPayload()
    {
        return payload;
    }

    public void setPayload(int payload)
    {
        this.payload = payload;
    }

    public int getClock()
    {
        return clock;
    }

    public void setClock(int clock)
    {
        this.clock = clock;
    }

    @Override
    public String toString()
    {
        return NAME + ":" + payload + " " + codec + "/" + clock;
    }
}
