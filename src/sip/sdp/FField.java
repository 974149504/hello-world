package sip.sdp;

/**
 * SDP f field.
 * <p>
 * f字段:f= f/编码格式/分辨率/帧率/码率类型/码率大小a/编码格式/码率大小/采样率
 * <p>
 * 各项具体含义:
 * f:后续参数为视频的参数;各参数间以“/”分割:
 * ———编码格式(十进制整数字符串表示):
 * 1———MPEG-4 2———H.264 3———SVAC 4———3GP
 * ———分辨率(十进制整数字符串表示):
 * 1———QCIF 2———CIF 3———4CIF 4———D1 5———720P 6———1080P/I
 * ———帧率(十进制整数字符串表示): 0~99
 * ———码率类型(十进制整数字符串表示):
 * 1———固定码率(CBR) 2———可变码率(VBR)
 * ———码率大小(十进制整数字符串表示): 0~100000(如1表示1kbps)
 * <p>
 * f:后续参数为音频的参数,各参数间以“/”分割:
 * ———编码格式(十进制整数字符串表示):
 * 1———G.711 2———G.723.1 3———G.729 4———G.722.1
 * ———码率大小(十进制整数字符串表示):
 * ———音频编码码率:
 * 1———5.3kbps(注:G.723.1中使用)
 * 2———6.3kbps(注:G.723.1中使用)
 * 3———8kbps(注:G.729中使用)
 * 4———16kbps(注:G.722.1中使用)
 * 96
 * GB/T28181—2016
 * 5———24kbps(注:G.722.1中使用)
 * 6———32kbps(注:G.722.1中使用)
 * 7———48kbps(注:G.722.1中使用)
 * 8———64kbps(注:G.711中使用)
 * ———采样率:
 * 1———8kHz(注:G.711/G.723.1/G.729中使用)
 * 2———14kHz(注:G.722.1中使用)
 * 3———16kHz(注:G.722.1中使用)
 * 4———32kHz(注:G.722.1中使用)
 */
public class FField extends SdpField
{
    /**
     * 视频编码格式(十进制整数字符串表示): 1-MPEG-4 2-H.264 3-SVAC 4-3GP 5-H.265
     */
    private static final int VCODE_MPEG = 1;

    private static final int VCODE_H264 = 2;

    private static final int VCODE_SVAC = 3;

    private static final int VCODE_3GP = 4;

    private static final int VCODE_H265 = 5;

    /**
     * 视频分辨率(十进制整数字符串表示): 1-QCIF 2-CIF 3-4CIF 4-D1 5-720P 6-1080P/I
     */
    private static final int VRL_QCIF = 1;

    private static final int VRL_CIF = 2;

    private static final int VRL_4CIF = 3;

    private static final int VRL_D1 = 4;

    private static final int VRL_720P = 5;

    private static final int VRL_1080P = 6;

    /**
     * 视频码率类型(十进制整数字符串表示): 1-固定码率(CBR) 2-可变码率(VBR)
     */
    private static final int VSTREAM_CBR = 1;

    private static final int VSTREAM_VBR = 2;

    /**
     * 音频编码格式(十进制整数字符串表示): 1-G.711 2-G.723.1 3-G.729 4-G.722.1
     */
    private static final int ACODE_G711 = 1;

    private static final int ACODE_G7231 = 2;

    private static final int ACODE_G729 = 3;

    private static final int ACODE_G7221 = 4;

    /* 音频编码码率: */

    /**
     * 1-5.3kbps(注:G.723.1中使用)
     */
    private static final int ARATE_5_3K = 1;

    /**
     * 2-6.3kbps(注:G.723.1中使用)
     */
    private static final int ARATE_6_3K = 2;

    /**
     * 3-8kbps(注:G.729中使用)
     */
    private static final int ARATE_8K = 3;

    /**
     * 4-16kbps(注:G.722.1中使用)
     */
    private static final int ARATE_16K = 4;

    /**
     * 5-24kbps(注:G.722.1中使用)
     */
    private static final int ARATE_24K = 5;

    /**
     * 6-32kbps(注:G.722.1中使用)
     */
    private static final int ARATE_32K = 6;

    /**
     * 7-48kbps(注:G.722.1中使用)
     */
    private static final int ARATE_48K = 7;

    /**
     * 8-64kbps(注:G.711中使用)
     */
    private static final int ARATE_64K = 8;

    /* 音频采样率 */

    /**
     * 1-8kHz(注:G.711/G.723.1/G.729中使用)
     */
    private static final int ARD_8K = 1;

    /**
     * 2-14kHz(注:G.722.1中使用)
     */
    private static final int ARD_14K = 2;

    /**
     * 3-16kHz(注:G.722.1中使用)
     */
    private static final int ARD_16K = 3;

    /**
     * 4-32kHz(注:G.722.1中使用)
     */
    private static final int ARD_32K = 4;

    /**
     * Creates f new OriginField.
     */
    public FField(Param param)
    {
        super('f', param.buildValue());
    }

    /**
     * Creates f new OriginField.
     */
    public FField(String origin)
    {
        super('f', origin);
    }

    /**
     * Creates f new OriginField.
     */
    public FField(SdpField sf)
    {
        super(sf);
    }

    public static class Param
    {
        /**
         * 视频编码格式
         */
        public int vCodec = VCODE_H264;

        /**
         * 视频分辨率
         */
        public int vResolution = VRL_720P;

        /**
         * 视频帧率
         */
        public int vFrame;

        /**
         * 视频码率类型
         */
        public int vStream;

        /**
         * 视频码率大小
         */
        public int vBitrate;

        /**
         * 音频编码格式
         */
        public int aCodec = ACODE_G711;

        /**
         * 音频码率大小
         */
        public int aBitrate;

        /**
         * 音频采样率
         */
        public int aSampling = ARD_8K;

        public Param(String sdp)
        {
            SessionDescriptor localSdp = new SessionDescriptor(sdp);
            MediaDescriptor vmd = localSdp.getMediaDescriptor("video");
            if (null != vmd)
            {
                RtpMap rtpMap = vmd.getRtpMap();
                if (null != rtpMap)
                {
                    String codec = rtpMap.getCodec();
                    if ("MPEG4".equalsIgnoreCase(codec))
                    {
                        this.vCodec = VCODE_MPEG;
                    }
                    else if ("SVAC".equalsIgnoreCase(codec))
                    {
                        this.vCodec = VCODE_SVAC;
                    }
                    else if ("3GP".equalsIgnoreCase(codec))
                    {
                        this.vCodec = VCODE_3GP;
                    }
                    else if ("H265".equalsIgnoreCase(codec))
                    {
                        this.vCodec = VCODE_H265;
                    }
                    else if ("HEVC".equalsIgnoreCase(codec))
                    {
                        this.vCodec = VCODE_H265;
                    }
                    else
                    {
                        this.vCodec = VCODE_H264;
                    }
                }
            }
        }

        public String buildValue()
        {
            return buildVideoValue() + " " + buildAudioValue();
        }

        private String buildVideoValue()
        {
            return "v" + f(vCodec) + f(vResolution) + f(vFrame) + f(vStream) + f(vBitrate);
        }

        private String buildAudioValue()
        {
            return "a" + f(aCodec) + f(aBitrate) + f(aSampling);
        }

        private String f(int value)
        {
            return 0 == value ? "/" : "/" + value;
        }
    }
}
