package sip.core.provider;

import com.allcam.common.utils.RegularUtil;
import com.allcam.gbgw.protocol.gb28181.agent.SipStackEngine;
import com.allcam.gbgw.protocol.gb28181.message.XMLUtil;
import com.allcam.gbgw.protocol.sip.core.address.NameAddress;
import com.allcam.gbgw.protocol.sip.core.address.SipURL;
import com.allcam.gbgw.protocol.sip.core.header.ViaHeader;
import com.allcam.gbgw.protocol.sip.core.message.Message;
import com.allcam.gbgw.protocol.sip.core.message.MessageFactory;
import com.allcam.gbgw.protocol.sip.net.*;
import com.allcam.gbgw.protocol.tools.*;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * SipProvider implements the SIP transport layer, that is the layer responsable
 * for sending and receiving SIP messages. Messages are received by the callback
 * function defined in the interface SipProviderListener.
 * <p>
 * SipProvider implements also multiplexing/demultiplexing service through the
 * use of SIP interface identifiers and <i>onReceivedMessage()<i/> callback
 * function of specific SipProviderListener.
 * <p>
 * A SipProviderListener can be added to a SipProvider through the
 * addSipProviderListener(id,listener) method, where: <b> - <i>id<i/> is the
 * SIP interface identifier the listener has to be bound to, <b> - <i>listener<i/>
 * is the SipProviderListener that received messages are passed to. <p/> The SIP
 * interface identifier specifies the type of messages the listener is going to
 * receive for. Together with the specific SipProvider, it represents the
 * complete SIP Service Access Point (SAP) address/identifier used for
 * demultiplexing SIP messages at receiving side. <p/> The identifier can be of
 * one of the three following types: transactionId, dialog_id, or method_id.
 * These types of identifiers characterize respectively: <br> - messages within
 * a specific transaction, <br> - messages within a specific dialog, <br> -
 * messages related to a specific SIP method. It is also possible to use the the
 * identifier ANY to specify <br> - all messages that are out of any
 * transactions, dialogs, or already specified method types.
 * <p>
 * When receiving a message, the SipProvider first tries to look for a matching
 * transaction, then looks for a matching dialog, then for a matching method
 * type, and finally for a default listener (i.e. that with identifier ANY). For
 * the matched SipProviderListener, the method <i>onReceivedMessage()</i> is
 * fired.
 * <p>
 * Note: no 482 (Loop Detected) responses are generated for requests that does
 * not properly match any ongoing transactions, dialogs, nor method types.
 */
@Slf4j
public class SipProvider implements TransportListener, TcpServerListener
{

    // **************************** Constants ****************************

    /**
     * UDP protocol type
     */
    public static final String PROTO_UDP = "udpandObserve";

    /**
     * TCP protocol type
     */
    public static final String PROTO_TCP = "tcp";

    /**
     * TLS protocol type
     */
    public static final String PROTO_TLS = "tls";

    /**
     * SCTP protocol type
     */
    public static final String PROTO_SCTP = "sctp";

    /**
     * String value "auto-configuration" used for auto configuration of the host
     * address.
     */
    private static final String AUTO_CONFIGURATION = "AUTO-CONFIGURATION";

    /**
     * String value "auto-configuration" used for auto configuration of the host
     * address.
     */
    private static final String ALL_INTERFACES = "ALL-INTERFACES";

    /**
     * Identifier used as listener id for capturing ANY incoming messages that
     * does not match any active method_id, transactionId, nor dialog_id. <br>
     * In this context, "active" means that there is a active listener for that
     * specific method, transaction, or dialog.
     */
    public static final Identifier ANY = new Identifier("ANY");

    /**
     * Minimum length for a valid SIP message.
     */
    private static final int MIN_MESSAGE_LENGTH = 12;

    // ***************** Readable/configurable attributes *****************

    /**
     * Via address/name. Use 'auto-configuration' for auto detection, or let it
     * undefined.
     */
    private String viaAddr = null;

    /**
     * Local SIP port
     */
    private int hostPort = 0;

    /**
     * Network interface (IP address) used by SIP. Use 'ALL-INTERFACES' for
     * binding SIP to all interfaces (or let it undefined).
     */
    private String hostIfaddr = null;

    /**
     * Transport protocols (the first protocol is used as default)
     */
    private String[] transportProtocols = null;

    /**
     * Max number of (contemporary) open connections
     */
    private int nmaxConnections = 0;

    /**
     * Outbound proxy (host_addr[:hostPort]). Use 'NONE' for not using an
     * outbound proxy (or let it undefined).
     */
    private SocketAddress outboundProxy = null;

    /**
     * Outbound server
     */
    private String server = null;

    /**
     * Whether logging all packets (including non-SIP keepalive tokens).
     */
    private boolean logAllPackets = false;

    /**
     * Outbound proxy addr (for backward compatibility).
     */
    private String outboundAddr = null;

    /**
     * Outbound proxy port (for backward compatibility).
     */
    private int outboundPort = -1;

    // ********************* Non-readable attributes *********************

    /**
     * Network interface (IP address) used by SIP.
     */
    private IpAddress hostIpaddr = null;

    /**
     * Default transport
     */
    private String defaultTransport = null;

    /**
     * Whether using UDP as transport protocol
     */
    private boolean transportUdp = false;

    /**
     * Whether using TCP as transport protocol
     */
    private boolean transportTcp = false;

    /**
     * Whether using TLS as transport protocol
     */
    boolean transportTls = false;

    /**
     * Whether using SCTP as transport protocol
     */
    boolean transportSctp = false;

    /**
     * Whether adding 'rport' parameter on outgoing requests.
     */
    private boolean rport = true;

    /**
     * Whether forcing 'rport' parameter on incoming requests ('force-rport'
     * mode).
     */
    private boolean forceRport = false;

    /**
     * List of provider listeners
     */
    private Hashtable<Identifier, SipProviderListener> listeners = null;

    /**
     * UDP transport
     */
    private UdpTransport udp = null;

    /**
     * Tcp server
     */
    private TcpServer tcpServer = null;

    /**
     * Connections
     */
    private Hashtable<ConnectionIdentifier, ConnectedTransport> connections = null;

    /**
     * Creates a new SipProvider.
     */
    public SipProvider(String viaAddr, int port)
    {
        init(viaAddr, port, null, null);
        startTrasport();
    }

    /**
     * Creates a new SipProvider. Costructs the SipProvider, initializing the
     * SipProviderListeners, the transport protocols, and other attributes.
     */
    public SipProvider(String viaAddr, int port, String[] protocols, String ifaddr)
    {
        init(viaAddr, port, protocols, ifaddr);
        startTrasport();
    }

    /**
     * Inits the SipProvider, initializing the SipProviderListeners, the
     * transport protocols, the outbound proxy, and other attributes.
     */
    private void init(String viaddr, int port, String[] protocols, String ifaddr)
    {
        viaAddr = viaddr;
        if (viaAddr == null || viaAddr.equalsIgnoreCase(AUTO_CONFIGURATION))
        {
            viaAddr = IpAddress.localIpAddress;
        }
        hostPort = port;
        if (hostPort < 0)
        {
            hostPort = SipStack.default_port;
        }
        hostIpaddr = null;
        if (ifaddr != null && !ifaddr.equalsIgnoreCase(ALL_INTERFACES))
        {
            try
            {
                hostIpaddr = IpAddress.getByName(ifaddr);
            }
            catch (IOException e)
            {
                e.printStackTrace();
                hostIpaddr = null;
            }
        }
        transportProtocols = protocols;
        if (transportProtocols == null)
        {
            transportProtocols = SipStack.default_transport_protocols;
        }
        defaultTransport = transportProtocols[0];
        for (int i = 0; i < transportProtocols.length; i++)
        {
            transportProtocols[i] = transportProtocols[i].toLowerCase();
            if (transportProtocols[i].equals(PROTO_UDP))
            {
                transportUdp = true;
            }
            else if (transportProtocols[i].equals(PROTO_TCP))
            {
                transportTcp = true;
            }
        }
        if (nmaxConnections <= 0)
        {
            nmaxConnections = SipStack.default_nmax_connections;
        }

        // just for backward compatibility..
        if (outboundPort < 0)
        {
            outboundPort = SipStack.default_port;
        }
        if (outboundAddr != null)
        {
            if ("NONE".equalsIgnoreCase(outboundAddr) || "NO-OUTBOUND".equalsIgnoreCase(outboundAddr))
            {
                outboundProxy = null;
            }
            else
            {
                outboundProxy = new SocketAddress(outboundAddr, outboundPort);
            }
        }

        rport = SipStack.use_rport;
        forceRport = SipStack.force_rport;

        listeners = new Hashtable<>(10);
        connections = new Hashtable<>(10);
    }

    /**
     * Starts the transport services.
     */
    private void startTrasport()
    {
        // start udp
        if (transportUdp)
        {
            try
            {
                if (hostIpaddr == null)
                {
                    udp = new UdpTransport(hostPort, this);
                }
                else
                {
                    udp = new UdpTransport(hostPort, hostIpaddr, this);
                }
                hostPort = udp.getPort();
                printLog("udp is up", LogLevel.MEDIUM);
            }
            catch (Exception e)
            {
                printException(e, LogLevel.HIGH);
            }
        }
        // start tcp
        if (transportTcp)
        {
            try
            {
                if (hostIpaddr == null)
                {
                    tcpServer = new TcpServer(hostPort, this);
                }
                else
                {
                    tcpServer = new TcpServer(hostPort, hostIpaddr, this);
                }
                hostPort = tcpServer.getPort();
                printLog("tcp is up", LogLevel.MEDIUM);
            }
            catch (Exception e)
            {
                printException(e, LogLevel.HIGH);
            }
        }
        // printLog("transport is up",LogLevel.MEDIUM);
    }

    /**
     * Stops the transport services.
     */
    private void stopTrasport()
    {
        // stop udp
        if (udp != null)
        {
            printLog("udp is going down", LogLevel.LOWER);
            udp.halt();
            udp = null;
        }
        // stop tcp
        if (tcpServer != null)
        {
            printLog("tcp is going down", LogLevel.LOWER);
            tcpServer.halt();
            tcpServer = null;
        }
        haltConnections();
        connections = null;
    }

    public void haltConnections()
    { // modified
        if (connections != null)
        {
            printLog("connections are going down", LogLevel.LOWER);
            for (Enumeration<ConnectedTransport> e = connections.elements(); e.hasMoreElements(); )
            {
                ConnectedTransport c = e.nextElement();
                c.halt();
            }
            connections = new Hashtable<>(10);
        }
    }

    /**
     * Stops the SipProviders.
     */
    public void halt()
    {
        printLog("halt: SipProvider is going down", LogLevel.MEDIUM);
        stopTrasport();
        listeners = new Hashtable<>(10);
    }

    /**
     * Gets a String with the list of transport protocols.
     */
    private String transportProtocolsToString()
    {
        StringBuilder list = new StringBuilder(transportProtocols[0]);
        for (int i = 1; i < transportProtocols.length; i++)
        {
            list.append("/").append(transportProtocols[i]);
        }
        return list.toString();
    }

    // ************************** Public methods *************************

    /**
     * Gets via address.
     */
    public String getViaAddress()
    {
        viaAddr = IpAddress.localIpAddress;

        return viaAddr;
    }

    /** Sets via address. */
    /*
     * public void setViaAddress(String addr) { viaAddr=addr; }
     */

    /**
     * Gets host port.
     */
    public int getPort()
    {
        return hostPort;
    }

    /**
     * Whether binding the sip provider to all interfaces or only on the
     * specified host address.
     */
    public boolean isAllInterfaces()
    {
        return hostIpaddr == null;
    }

    /**
     * Gets host interface IpAddress.
     */
    public IpAddress getInterfaceAddress()
    {
        return hostIpaddr;
    }

    /**
     * Gets array of transport protocols.
     */
    public String[] getTransportProtocols()
    {
        return transportProtocols;
    }

    /**
     * Gets the default transport protocol.
     */
    public String getDefaultTransport()
    {
        return defaultTransport;
    }

    /**
     * Gets the default transport protocol.
     */
    public void setDefaultTransport(String proto)
    {
        defaultTransport = proto;
    }

    /**
     * Sets rport support.
     */
    public void setRport(boolean flag)
    {
        rport = flag;
    }

    /**
     * Whether using rport.
     */
    public boolean isRportSet()
    {
        return rport;
    }

    /**
     * Sets 'force-rport' mode.
     */
    public void setForceRport(boolean flag)
    {
        forceRport = flag;
    }

    /**
     * Whether using 'force-rport' mode.
     */
    public boolean isForceRportSet()
    {
        return forceRport;
    }

    /**
     * Whether has outbound proxy.
     */
    public boolean hasOutboundProxy()
    {
        return outboundProxy != null;
    }

    /**
     * Gets the outbound proxy.
     */
    public SocketAddress getOutboundProxy()
    {
        return outboundProxy;
    }

    /**
     * Sets the outbound proxy. Use 'null' for not using any outbound proxy.
     */
    public void setOutboundProxy(SocketAddress soaddr, String host)
    {
        outboundProxy = soaddr;
        server = host;
    }

    public NameAddress buildContactUrl(String contact, NameAddress fromUrl)
    {
        NameAddress contactUrl;
        if (contact != null)
        {
            if (contact.contains("sip:"))
            {
                contactUrl = new NameAddress(contact);
            }
            else
            {
                contactUrl = new NameAddress(new SipURL(contact, getViaAddress(), getPort()));
            }
        }
        else
        {
            contactUrl = fromUrl;
        }
        return contactUrl;
    }

    /**
     * Returns the list (Hashtable) of active listener_IDs.
     */
    public Hashtable<Identifier, SipProviderListener> getListeners()
    {
        return listeners;
    }

    /**
     * Adds a new listener to the SipProvider for caputering ANY message. It is
     * the same as using method
     * addSipProviderListener(SipProvider.ANY,listener).
     *
     * @param listener is the SipProviderListener.
     * @return It returns <i>true</i> if the SipProviderListener is added,
     * <i>false</i> if the listener_ID is already in use.
     */
    public boolean addSipProviderListener(SipProviderListener listener)
    {
        return addSipProviderListener(ANY, listener);
    }

    /**
     * Adds a new listener to the SipProvider.
     *
     * @param id       is the unique identifier for the messages which the listener
     *                 as to be associated to. It is used as key. It can identify a
     *                 method, a transaction, or a dialog. Use SipProvider.ANY to
     *                 capture all messages. Use SipProvider.PROMISQUE if you want to
     *                 capture all message in promisque mode (letting other listeners
     *                 to capture the same received messages).
     * @param listener is the SipProviderListener for this message id.
     * @return It returns <i>true</i> if the SipProviderListener is added,
     * <i>false</i> if the listener_ID is already in use.
     */
    public boolean addSipProviderListener(Identifier id, SipProviderListener listener)
    {
        log.info("adding SipProviderListener: {}", id);
        boolean ret;
        if (listeners.containsKey(id))
        {
            log.warn("trying to add a SipProviderListener with a id that is already in use.");
            ret = false;
        }
        else
        {
            listeners.put(id, listener);
            ret = true;
        }

        log.debug("{} listeners.", listeners.size());
        return ret;
    }

    /**
     * Removes a SipProviderListener.
     *
     * @param id is the unique identifier used to select the listened messages.
     * @return It returns <i>true</i> if the SipProviderListener is removed,
     * <i>false</i> if the identifier is missed.
     */
    public boolean removeSipProviderListener(Identifier id)
    {
        log.info("removing SipProviderListener: {}", id);
        if (null == id)
        {
            return false;
        }

        boolean ret;
        if (!listeners.containsKey(id))
        {
            log.warn("trying to remove a missed SipProviderListener.");
            ret = false;
        }
        else
        {
            listeners.remove(id);
            ret = true;
        }

        log.debug("{} listeners.", listeners.size());
        return ret;
    }

    /**
     * Sends a Message, specifing the transport portocol, nexthop address and
     * port.
     * <p>
     * This is a low level method and forces the message to be routed to a
     * specific nexthop address, port and transport, regardless whatever the
     * Via, Route, or request-uri, address to.
     * <p>
     * In case of connection-oriented transport, the connection is selected as
     * follows: <br> - if an existing connection is found matching the
     * destination end point (socket), such connection is used, otherwise <br> -
     * a new connection is established
     *
     * @return It returns a Connection in case of connection-oriented delivery
     * (e.g. TCP) or null in case of connection-less delivery (e.g. UDP)
     */
    public ConnectionIdentifier sendMessage(Message msg, String proto, String destAddr, int destPort, int ttl)
    {
        try
        {
            IpAddress destIpAddr = IpAddress.getByName(destAddr);
            return sendMessage(msg, proto, destIpAddr, destPort, ttl);
        }
        catch (Exception e)
        {
            printException(e, LogLevel.HIGH);
            return null;
        }
    }

    /**
     * Sends a Message, specifing the transport portocol, nexthop address and
     * port.
     */
    private ConnectionIdentifier sendMessage(final Message msg, final String proto, final IpAddress destIpAddr,
                                             final int destPort, int ttl)
    {
        // logs
        String destAddr = destIpAddr.toString();
        printMessageLog(proto, destAddr, destPort, msg.getLength(), msg, "sent");

        if (transportUdp && proto.equals(PROTO_UDP))
        {
            // UDP
            try
            {
                udp.sendMessage(msg, destIpAddr, destPort);
            }
            catch (IOException e)
            {
                printException(e, LogLevel.HIGH);
                return null;
            }
            return null;
        }
        else if (transportTcp && proto.equals(PROTO_TCP))
        {
            // TCP
            ConnectionIdentifier connId = new ConnectionIdentifier(proto, destIpAddr, destPort);
            if (connections == null || !connections.containsKey(connId))
            {
                printLog("no active connection found matching " + connId, LogLevel.MEDIUM);
                printLog("open " + proto + " connection to " + destIpAddr + ":" + destPort, LogLevel.MEDIUM);
                TcpTransport conn;
                try
                {
                    conn = new TcpTransport(destIpAddr, destPort, this, server);
                }
                catch (Exception e)
                {
                    printLog("connection setup FAILED", LogLevel.HIGH);
                    return null;
                }
                printLog("connection " + conn + " opened", LogLevel.HIGH);
                addConnection(conn);
                if (!msg.isRegister())
                {
                    SipStackEngine.getInstance().register();
                }
            }
            else
            {
                printLog("active connection found matching " + connId, LogLevel.MEDIUM);
                ConnectedTransport conn = connections.get(connId);
                printLog("sending data through conn " + conn, LogLevel.MEDIUM);
                try
                {
                    conn.sendMessage(msg);
                    connId = new ConnectionIdentifier(conn);
                }
                catch (IOException e)
                {
                    printException(e, LogLevel.HIGH);
                    return null;
                }
            }
            return connId;
        }
        else
        { // otherwise
            printWarning("Unsupported protocol (" + proto + "): Message discarded");
            return null;
        }
    }

    /**
     * Sends the message <i>msg</i>.
     * <p>
     * The destination for the request is computed as follows: <br> - if
     * <i>outboundAddr</i> is set, <i>outboundAddr</i> and <i>outboundPort</i>
     * are used, otherwise <br> - if message has Route header with lr option
     * parameter (i.e. RFC3261 compliant), the first Route address is used,
     * otherwise <br> - the request's Request-URI is considered.
     * <p>
     * The destination for the response is computed based on the sent-by
     * parameter in the Via header field (RFC3261 compliant)
     * <p>
     * As transport it is used the protocol specified in the 'via' header field
     * <p>
     * In case of connection-oriented transport: <br> - if an already
     * established connection is found matching the destination end point
     * (socket), such connection is used, otherwise <br> - a new connection is
     * established
     *
     * @return Returns a ConnectionIdentifier in case of connection-oriented
     * delivery (e.g. TCP) or null in case of connection-less delivery
     * (e.g. UDP)
     */
    public ConnectionIdentifier sendMessage(Message msg)
    {
        // select the transport protocol
        ViaHeader via = msg.getViaHeader();
        String proto;
        if (via != null)
        {
            proto = via.getProtocol().toLowerCase();
        }
        else
        {
            proto = getDefaultTransport().toLowerCase();
        }
        printLog("using transport " + proto, LogLevel.MEDIUM);

        // select the destination address and port
        String destAddr;
        int destPort = 0;
        int ttl = 0;

        if (!msg.isResponse())
        {
            if (outboundProxy != null)
            {
                destAddr = outboundProxy.getAddress().toString();
                destPort = outboundProxy.getPort();
            }
            else
            {
                if (msg.hasRouteHeader() && msg.getRouteHeader().getNameAddress().getAddress().hasLr())
                {
                    SipURL url = msg.getRouteHeader().getNameAddress().getAddress();
                    destAddr = url.getHost();
                    destPort = url.getPort();
                }
                else
                {
                    SipURL url = msg.getRequestLine().getAddress();
                    destAddr = url.getHost();
                    destPort = url.getPort();
                    if (url.hasMaddr())
                    {
                        destAddr = url.getMaddr();
                        if (url.hasTtl())
                        {
                            ttl = url.getTtl();
                        }
                        // update the via header by adding maddr and ttl params
                        via.setMaddr(destAddr);
                        if (ttl > 0)
                        {
                            via.setTtl(ttl);
                        }
                        msg.removeViaHeader();
                        msg.addViaHeader(via);
                    }
                }
            }
        }
        else
        { // RESPONSES
            SipURL url = via.getSipURL();
            if (via.hasReceived())
            {
                destAddr = via.getReceived();
            }
            else
            {
                destAddr = url.getHost();
            }
            if (via.hasRport())
            {
                destPort = via.getRport();
            }
            if (destPort <= 0)
            {
                destPort = url.getPort();
            }
        }

        if (destPort <= 0)
        {
            destPort = SipStack.default_port;
        }

        return sendMessage(msg, proto, destAddr, destPort, ttl);
    }

    public ConnectionIdentifier sendMessage(Message request, String body)
    {
        NameAddress msgTo = request.getFromHeader().getNameAddress();
        SipURL fromUrl = msgTo.getAddress();
        if (!fromUrl.getHost().matches(RegularUtil.IP_REGULAR))
        {
            //如果请求消息的from字段host不是ip格式，则使用via中的host和port替换
            SipURL sipUrl = request.getViaHeader().getSipURL();
            SipURL toUrl = new SipURL(fromUrl.getUserName(), sipUrl.getHost(), sipUrl.getPort());
            msgTo = new NameAddress(toUrl);
        }

        Message messageRequest = MessageFactory.createMessageRequest(this,
            msgTo,
            request.getToHeader().getNameAddress(),
            null,
            XMLUtil.XML_MANSCDP_TYPE,
            body);
        return sendMessage(messageRequest, request.getConnectionId());
    }

    /**
     * Sends the message <i>msg</i> using the specified connection.
     */
    public ConnectionIdentifier sendMessage(Message msg, ConnectionIdentifier connId)
    {
        if (logAllPackets || msg.getLength() > MIN_MESSAGE_LENGTH)
        {
            printLog("Sending message through conn " + connId, LogLevel.HIGH);
        }

        if (connId != null && connections.containsKey(connId))
        {
            printLog("active connection found matching " + connId, LogLevel.MEDIUM);
            ConnectedTransport conn = connections.get(connId);
            try
            {
                conn.sendMessage(msg);
                String proto = conn.getProtocol();
                String destAddr = conn.getRemoteAddress().toString();
                int destPort = conn.getRemotePort();
                printMessageLog(proto, destAddr, destPort, msg.getLength(), msg, "sent");
                return connId;
            }
            catch (Exception e)
            {
                printException(e, LogLevel.HIGH);
            }
        }
        // else
        printLog("no active connection found matching " + connId, LogLevel.MEDIUM);
        return sendMessage(msg);
    }

    /**
     * Processes the message received. It is called each time a new message is
     * received by the transport layer, and it performs the actual message
     * processing.
     */
    private void processReceivedMessage(Message msg)
    {
        try
        {
            printMessageLog(msg.getTransportProtocol(),
                msg.getRemoteAddress(),
                msg.getRemotePort(),
                msg.getLength(),
                msg,
                "received");

            // discard too short messages
            if (msg.getLength() <= 2)
            {
                if (logAllPackets)
                {
                    printLog("message too short: discarded\r\n", LogLevel.LOW);
                }
                return;
            }
            // discard non-SIP messages
            String firstLine = msg.getFirstLine();
            if (firstLine == null || !firstLine.toUpperCase().contains("SIP/2.0"))
            {
                if (logAllPackets)
                {
                    printLog("NOT a SIP message: discarded\r\n", LogLevel.LOW);
                }
                return;
            }

            // if a request, handle "received" and "rport" parameters
            if (msg.isRequest())
            {
                ViaHeader vh = msg.getViaHeader();
                boolean viaChanged = false;
                String srcAddr = msg.getRemoteAddress();
                int srcPort = msg.getRemotePort();
                String viaAddr = vh.getHost();
                int viaPort = vh.getPort();
                if (viaPort <= 0)
                {
                    viaPort = SipStack.default_port;
                }

                if (!viaAddr.equals(srcAddr))
                {
                    vh.setReceived(srcAddr);
                    viaChanged = true;
                }

                if (vh.hasRport())
                {
                    vh.setRport(srcPort);
                    viaChanged = true;
                }
                else
                {
                    if (forceRport && viaPort != srcPort)
                    {
                        vh.setRport(srcPort);
                        viaChanged = true;
                    }
                }

                if (viaChanged)
                {
                    msg.removeViaHeader();
                    msg.addViaHeader(vh);
                }
            }

            // is there any listeners?
            if (listeners == null || listeners.size() == 0)
            {
                printLog("no listener found: message discarded.", LogLevel.HIGH);
                return;
            }

            // after the callback check if the message is still valid
            if (!msg.isRequest() && !msg.isResponse())
            {
                printLog("No valid SIP message: message discarded.", LogLevel.HIGH);
                return;
            }

            // try to look for a transaction
            Identifier key = msg.getTransactionId();
            printLog("DEBUG: transaction-id: " + key, LogLevel.MEDIUM);
            if (listeners.containsKey(key))
            {
                printLog("message passed to transaction: " + key, LogLevel.MEDIUM);
                SipProviderListener sipListener = listeners.get(key);
                if (sipListener != null)
                {
                    sipListener.onReceivedMessage(this, msg);
                }
                return;
            }
            // try to look for a dialog
            key = msg.getDialogId();
            printLog("DEBUG: dialog-id: " + key, LogLevel.MEDIUM);
            if (listeners.containsKey(key))
            {
                printLog("message passed to dialog: " + key, LogLevel.MEDIUM);
                (listeners.get(key)).onReceivedMessage(this, msg);
                return;
            }
            // try to look for a UAS by username
            key = msg.getUsernameMethodId();
            if (listeners.containsKey(key))
            {
                printLog("message passed to username uas: " + key, LogLevel.MEDIUM);
                (listeners.get(key)).onReceivedMessage(this, msg);
                return;
            }
            // try to look for a UAS
            key = msg.getMethodId();
            if (listeners.containsKey(key))
            {
                printLog("message passed to uas: " + key, LogLevel.MEDIUM);
                (listeners.get(key)).onReceivedMessage(this, msg);
                return;
            }
            // try to look for a default UA
            if (listeners.containsKey(ANY))
            {
                printLog("message passed to uas: " + ANY, LogLevel.MEDIUM);
                (listeners.get(ANY)).onReceivedMessage(this, msg);
                return;
            }

            // if we are here, no listener_ID matched..
            printLog("No SipListener found matching that message: " + key, LogLevel.HIGH);
            printLog("Pending SipProviderListeners= " + listeners.size(), LogLevel.MEDIUM);
        }
        catch (Exception e)
        {
            printWarning("Error handling a new incoming message");
            printException(e, LogLevel.MEDIUM);
        }
    }

    /**
     * Adds a new Connection
     */
    private void addConnection(ConnectedTransport conn)
    {
        ConnectionIdentifier connId = new ConnectionIdentifier(conn);
        if (connections.containsKey(connId))
        {
            printLog("trying to add the already established connection " + connId, LogLevel.HIGH);
            printLog("connection " + connId + " will be replaced", LogLevel.HIGH);
            ConnectedTransport oldConn = connections.get(connId);
            oldConn.halt();
            connections.remove(connId);
        }
        else if (connections.size() >= nmaxConnections)
        { // remove the
            // older unused
            // connection
            printLog("reached the maximum number of connection: removing the older unused connection", LogLevel.HIGH);
            long olderTime = System.currentTimeMillis();
            ConnectionIdentifier olderId = null;
            for (Enumeration<ConnectedTransport> e = connections.elements(); e.hasMoreElements(); )
            {
                ConnectedTransport co = e.nextElement();
                if (co.getLastTimeMillis() < olderTime)
                {
                    olderId = new ConnectionIdentifier(co);
                }
            }
            if (olderId != null)
            {
                removeConnection(olderId);
            }
        }
        connections.put(connId, conn);

        //debug log
        for (Enumeration<ConnectionIdentifier> e = connections.keys(); e.hasMoreElements(); )
        {
            ConnectionIdentifier id = e.nextElement();
            printLog("conn-id=" + id + ": " + connections.get(id), LogLevel.LOW);
        }
    }

    /**
     * Removes a Connection
     */
    private void removeConnection(ConnectionIdentifier connId)
    {
        if (connections != null && connections.containsKey(connId))
        {
            ConnectedTransport conn = connections.get(connId);
            if (conn != null)
            {
                conn.halt();
            }
            connections.remove(connId);

            //debug log
            for (Enumeration<ConnectedTransport> e = connections.elements(); e.hasMoreElements(); )
            {
                ConnectedTransport co = e.nextElement();
                printLog("conn " + co.toString(), LogLevel.LOW);
            }
        }
    }

    /**
     * When a new SIP message is received.
     */
    @Override
    public void onReceivedMessage(Transport transport, Message msg)
    {
        SipStack.context().post(() -> processReceivedMessage(msg));
    }

    /**
     * When Transport terminates.
     */
    @Override
    public void onTransportTerminated(Transport transport, Exception error)
    {
        printLog("transport " + transport + " terminated", LogLevel.MEDIUM);
        if (transport.getProtocol().equals(PROTO_TCP))
        {
            ConnectionIdentifier connId = new ConnectionIdentifier((ConnectedTransport)transport);
            removeConnection(connId);
            SipStackEngine.getInstance().register();
        }
        if (error != null)
        {
            printException(error, LogLevel.HIGH);
        }
    }

    /**
     * When a new incoming Connection is established
     */
    @Override
    public void onIncomingConnection(TcpServer tcpServer, TcpSocket socket)
    {
        printLog("incoming connection from " + socket.getAddress() + ":" + socket.getPort(), LogLevel.MEDIUM);
        ConnectedTransport conn = new TcpTransport(socket, this);
        printLog("tcp connection " + conn + " opened", LogLevel.MEDIUM);
        addConnection(conn);
    }

    /**
     * When TcpServer terminates.
     */
    @Override
    public void onServerTerminated(TcpServer tcp_server, Exception error)
    {
        printLog("tcp server " + tcp_server + " terminated", LogLevel.MEDIUM);
    }

    // ************************** Other methods ***************************

    /**
     * Picks a fresh branch value. The branch ID MUST be unique across space and
     * time for all requests sent by the UA. The branch ID always begin with the
     * characters "z9hG4bK". These 7 characters are used by RFC 3261 as a magic
     * cookie.
     */
    public static String pickBranch()
    { // String
        // str=Long.toString(Math.abs(Random.nextLong()),16);
        // if (str.length()<5) str+="00000";
        // return "z9hG4bK"+str.substring(0,5);
        return "z9hG4bK" + Random.nextNumString(5);
    }

    /**
     * Picks an unique branch value based on a SIP message. This value could
     * also be used as transaction ID
     */
    public String pickBranch(Message msg)
    {
        StringBuffer sb = new StringBuffer();
        sb.append(msg.getRequestLine().getAddress().toString());
        sb.append(getViaAddress() + getPort());
        ViaHeader top_via = msg.getViaHeader();
        if (top_via.hasBranch())
        {
            sb.append(top_via.getBranch());
        }
        else
        {
            sb.append(top_via.getHost() + top_via.getPort());
            sb.append(msg.getCSeqHeader().getSequenceNumber());
            sb.append(msg.getCallIdHeader().getCallId());
            sb.append(msg.getFromHeader().getTag());
            sb.append(msg.getToHeader().getTag());
        }
        // return "z9hG4bK"+(new MD5(unique_str)).asHex().substring(0,9);
        return "z9hG4bK" + (new SimpleDigest(5, sb.toString())).asHex();
    }

    /**
     * Picks a new tag. A tag MUST be globally unique and cryptographically
     * random with at least 32 bits of randomness. A property of this selection
     * requirement is that a UA will place a different tag into the From header
     * of an INVITE than it would place into the To header of the response to
     * the same INVITE. This is needed in order for a UA to invite itself to a
     * session.
     */
    public static String pickTag()
    { // String
        // str=Long.toString(Math.abs(Random.nextLong()),16);
        // if (str.length()<8) str+="00000000";
        // return str.substring(0,8);
        return "z9hG4bK" + Random.nextNumString(8);
    }

    /**
     * Picks a new tag. The tag is generated uniquely based on message <i>req</i>.
     * This tag can be generated for responses in a stateless manner - in a
     * manner that will generate the same tag for the same request consistently.
     */
    public static String pickTag(Message req)
    { // return
        // String.valueOf(tag_generator++);
        // return (new MD5(request.toString())).asHex().substring(0,8);
        return (new SimpleDigest(8, req.toString())).asHex();
    }

    /**
     * Picks a new call-id. The call-id is a globally unique identifier over
     * space and time. It is implemented in the form "localid@host". Call-id
     * must be considered case-sensitive and is compared byte-by-byte.
     */
    public String pickCallId()
    { // String
        // str=Long.toString(Math.abs(Random.nextLong()),16);
        // if (str.length()<12) str+="000000000000";
        // return str.substring(0,12)+"@"+getViaAddress();
        return Random.nextNumString(12);// + "@" + getViaAddress();
    }

    /**
     * picks an initial CSeq
     */
    public static int pickInitialCSeq()
    {
        return 1;
    }

    /**
     * (<b>Deprecated</b>) Constructs a NameAddress based on an input string.
     * The input string can be a: <br> - <i>user</i> name, <br> -
     * <i>user@address</i> url, <br> - <i>"Name" &lt;sip:user@address&gt;</i>
     * address,
     * <p>
     * In the former case, a SIP URL is costructed using the outbound proxy as
     * host address if present, otherwise the local via address is used.
     */
    public NameAddress completeNameAddress(String str)
    {
        if (str.indexOf("<sip:") >= 0)
            return new NameAddress(str);
        else
        {
            SipURL url = completeSipURL(str);
            return new NameAddress(url);
        }
    }

    /**
     * Constructs a SipURL based on an input string.
     */
    private SipURL completeSipURL(String str)
    { // in case it is passed only the
        // 'user' field, add
        // '@'<outboundProxy>[':'<outboundPort>]
        if (!str.startsWith("sip:") && str.indexOf("@") < 0 && str.indexOf(".") < 0 && str.indexOf(":") < 0)
        { // may be it
            // is just
            // the user
            // name..
            String url = "sip:" + str + "@";
            if (outboundProxy != null)
            {
                url += outboundProxy.getAddress().toString();
                int port = outboundProxy.getPort();
                if (port <= 0 || port == SipStack.default_port)
                {
                    return new SipURL(url);
                }
                url += ":" + port;
            }
            else
            {
                url += getViaAddress();
                if (hostPort > 0 && hostPort != SipStack.default_port)
                {
                    url += ":" + hostPort;
                }
            }
            return new SipURL(url);
        }
        else
        {
            return new SipURL(str);
        }
    }

    // ******************************* Logs *******************************

    /**
     * Gets a String value for this object
     */
    @Override
    public String toString()
    {
        if (hostIpaddr == null)
        {
            return hostPort + "/" + transportProtocolsToString();
        }
        else
        {
            return hostIpaddr.toString() + ":" + hostPort + "/" + transportProtocolsToString();
        }
    }

    /**
     * Adds a new string to the default Log
     */
    private void printLog(String str, int level)
    {
        Log.compatLog(str, level);
    }

    /**
     * Adds a WARNING to the default Log
     */
    private void printWarning(String str)
    {
        Log.compatLog(str, LogLevel.MEDIUM);
    }

    /**
     * Adds the Exception message to the default Log
     */
    private void printException(Exception e, int level)
    {
        Log.compatLogEcp(e);
    }

    /**
     * Adds the SIP message to the messageslog
     */
    private void printMessageLog(String proto, String addr, int port, int len, Message msg, String str)
    {
        log.info("{}:{}/{} ({} bytes): {}\n-----Begin-of-message-----\n\n{}\n-----End-of-message-----\n",
            addr,
            port,
            proto,
            len,
            str,
            msg);
    }

}
