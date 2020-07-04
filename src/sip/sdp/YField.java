package sip.sdp;

/**
 * SDP y field.
 *
 * y字段:为十进制整数字符串,表示SSRC值。格式如下:dddddddddd。其中,第1位为历史或实时
 * 媒体流的标识位,0为实时,1为历史;第2位至第6位取20位SIP监控域ID之中的4到8位作为域标
 * 识,例如“13010000002000000001”中取数字“10000”;第7位至第10位作为域内媒体流标识,是一个与
 * 当前域内产生的媒体流SSRC值后4位不重复的四位十进制整数。
 */
public class YField extends SdpField
{
    /**
     * Creates a new OriginField.
     */
    public YField(String origin)
    {
        super('y', origin);
    }

    /**
     * Creates a new OriginField.
     */
    public YField(SdpField sf)
    {
        super(sf);
    }

}
