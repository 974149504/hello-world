package sip.core.provider;

import com.allcam.gbgw.protocol.tools.Timer;
import com.allcam.gbgw.protocol.tools.TimerListener;

import java.nio.charset.Charset;

/**
 * SipStack includes all static attributes used by the sip stack.
 * <p>
 * Static attributes includes the logging configuration, default SIP port,
 * deafult supported transport protocols, timeouts, etc.
 */
public class SipStack
{
    /**
     * sip stack context
     */
    private static Context context;

    /**
     * Whether SipStack configuration has been already loaded
     */
    private static boolean is_init = false;

    /**
     * Default supported transport protocols.
     */
    public static String[] default_transport_protocols = {SipProvider.PROTO_UDP, SipProvider.PROTO_TCP};

    /**
     * Release
     */
    private static final String RELEASE = "acs sip stack";

    /**
     * Default SIP port. Note that this is not the port used by the running
     * stack, but simply the standard default SIP port. <br>
     * Normally it sould be set to 5060 as defined by RFC 3261. Using a
     * different value may cause some problems when interacting with other
     * unaware SIP UAs.
     */
    static int default_port = 5060;

    /**
     * Default max number of contemporary open transport connections.
     */
    static int default_nmax_connections = 32;

    /**
     * Whether adding 'rport' parameter on via header fields of outgoing
     * requests.
     */
    static boolean use_rport = true;

    /**
     * Whether adding (forcing) 'rport' parameter on via header fields of
     * incoming requests.
     */
    static boolean force_rport = false;

    // ******************** general configurations ********************

    /**
     * default max-forwards value (RFC3261 recommends value 70)
     */
    public static int max_forwards = 70;

    /**
     * starting retransmission timeout (milliseconds); called T1 in RFC2361;
     * they suggest T1=500ms
     */
    public static long retransmission_timeout = 2000;

    /**
     * maximum retransmission timeout (milliseconds); called T2 in RFC2361; they
     * suggest T2=4sec
     */
    public static long max_retransmission_timeout = 16000;

    /**
     * transaction timeout (milliseconds); RFC2361 suggests 64*T1=32000ms
     */
    public static long transaction_timeout = 128000;

    /**
     * clearing timeout (milliseconds); T4 in RFC2361; they suggest T4=5sec
     */
    public static long clearing_timeout = 5000;

    /**
     * Whether 1xx responses create an "early dialog" for methods that create
     * dialog.
     */
    public static boolean early_dialog = false;

    /**
     * Default 'expires' value in seconds. RFC2361 suggests 3600s as default
     * value.
     */
    public static int default_expires = 3600;

    /**
     * UA info included in request messages in the 'User-Agent' header field.
     * Use "NONE" if the 'User-Agent' header filed must not be added.
     */
    public static String ua_info = RELEASE;

    /**
     * Server info included in response messages in the 'Server' header field
     * Use "NONE" if the 'Server' header filed must not be added.
     */
    public static String server_info = RELEASE;

    /**
     * heartbeat interval
     */
    public static int heartbeatInterval = 30;

    /**
     * encoding
     */
    public static Charset encoding = Charset.forName("UTF-8");

    /**
     * Costructs a non-static SipStack
     */
    private SipStack()
    {
    }

    /**
     * Inits SipStack
     */
    public static void init(Context context)
    {
        SipStack.context = context;

        // user-agent info
        if (ua_info != null && (ua_info.length() == 0 || "NONE".equalsIgnoreCase(ua_info) ||
            "NO-UA-INFO".equalsIgnoreCase(ua_info)))
        {
            ua_info = null;
        }

        // server info
        if (server_info != null && (server_info.length() == 0 || "NONE".equalsIgnoreCase(server_info) ||
            "NO-SERVER-INFO".equalsIgnoreCase(server_info)))
        {
            server_info = null;
        }

        is_init = true;
    }

    /**
     * Whether SipStack has been already initialized
     */
    public static boolean isInit()
    {
        return is_init;
    }

    public static Context context()
    {
        return context;
    }

    public static Timer retransmissionTimer(TimerListener listener)
    {
        return new Timer(retransmission_timeout, max_retransmission_timeout, "Retransmission", listener);
    }
}
