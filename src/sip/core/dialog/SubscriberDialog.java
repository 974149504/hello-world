package sip.core.dialog;

import com.allcam.gbgw.protocol.gb28181.message.XMLUtil;
import com.allcam.gbgw.protocol.sip.core.address.NameAddress;
import com.allcam.gbgw.protocol.sip.core.address.SipURL;
import com.allcam.gbgw.protocol.sip.core.header.AcceptHeader;
import com.allcam.gbgw.protocol.sip.core.header.ExpiresHeader;
import com.allcam.gbgw.protocol.sip.core.header.StatusLine;
import com.allcam.gbgw.protocol.sip.core.message.Message;
import com.allcam.gbgw.protocol.sip.core.message.MessageFactory;
import com.allcam.gbgw.protocol.sip.core.provider.SipProvider;
import com.allcam.gbgw.protocol.sip.core.provider.SipStack;
import com.allcam.gbgw.protocol.sip.core.transaction.TransactionClient;
import com.allcam.gbgw.protocol.sip.core.transaction.TransactionClientListener;
import com.allcam.gbgw.protocol.sip.core.transaction.TransactionServer;
import com.allcam.gbgw.protocol.tools.Log;
import com.allcam.gbgw.protocol.tools.LogLevel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * SubscriberDialog.
 */
@Slf4j
public class SubscriberDialog extends Dialog implements TransactionClientListener
{
    /**
     * Internal state D_INIT
     */
    private static final int D_INIT = 0;

    /**
     * Internal state D_SUBSCRIBING
     */
    private static final int D_SUBSCRIBING = 1;

    /**
     * Internal state D_SUBSCRIBED
     */
    private static final int D_ACCEPTED = 2;

    /**
     * Internal state D_PENDING
     */
    private static final int D_PENDING = 3;

    /**
     * Internal state D_ACTIVE
     */
    private static final int D_ACTIVE = 4;

    /**
     * Internal state D_TERMINATED
     */
    private static final int D_TERMINATED = 9;

    /**
     * String "active"
     */
    private static final String ACTIVE = "active";

    /**
     * String "pending"
     */
    private static final String PENDING = "pending";

    /**
     * String "terminated"
     */
    private static final String TERMINATED = "terminated";

    /**
     * The subscribe transaction
     */
    private TransactionClient subscribeTransaction;

    /**
     * The SubscriberDialog listener
     */
    private SubscriberDialogListener listener;

    /**
     * The event package name
     */
    private String event;

    /**
     * The subscription id
     */
    private String id;

    /**
     * The subscription target
     */
    private String target;

    /**
     * The subscription from
     */
    private String subscriber;

    /**
     * The subscription contact url
     */
    private String contact;

    /**
     * The subscription expires, default 600 sec
     */
    private int expires = 600;

    /**
     * The subscription body
     */
    private String subscribeBody;

    /**
     * The re subscribe task runnable
     */
    private Runnable reSubscribeTask = this::reSubscribe;

    /**
     * Gets the dialog state
     */
    @Override
    protected String getStatusDescription()
    {
        switch (status)
        {
            case D_INIT:
                return "D_INIT";
            case D_SUBSCRIBING:
                return "D_SUBSCRIBING";
            case D_ACCEPTED:
                return "D_ACCEPTED";
            case D_PENDING:
                return "D_PENDING";
            case D_ACTIVE:
                return "D_ACTIVE";
            case D_TERMINATED:
                return "D_TERMINATED";
            default:
                return null;
        }
    }

    @Override
    protected int getStatus()
    {
        return status;
    }

    // *************************** Public methods **************************

    /**
     * Whether the dialog is in "early" state.
     */
    @Override
    public boolean isEarly()
    {
        return (status < D_ACCEPTED);
    }

    /**
     * Whether the dialog is in "confirmed" state.
     */
    @Override
    public boolean isConfirmed()
    {
        return (status >= D_ACCEPTED && status < D_TERMINATED);
    }

    /**
     * Whether the dialog is in "active" state.
     */
    @Override
    public boolean isTerminated()
    {
        return (status == D_TERMINATED);
    }

    /**
     * Whether the subscription is "pending".
     */
    public boolean isSubscriptionPending()
    {
        return (status >= D_ACCEPTED && status < D_ACTIVE);
    }

    /**
     * Whether the subscription is "active".
     */
    public boolean isSubscriptionActive()
    {
        return (status == D_ACTIVE);
    }

    /**
     * Whether the subscription is "terminated".
     */
    public boolean isSubscriptionTerminated()
    {
        return (status == D_TERMINATED);
    }

    /**
     * Gets event type.
     */
    public String getEvent()
    {
        return event;
    }

    /**
     * Gets expires sec.
     */
    public int getExpires()
    {
        return expires;
    }

    /**
     * Gets the event "id" parameter.
     */
    public String getId()
    {
        return id;
    }

    // **************************** Costructors ****************************

    /**
     * Creates a new SubscriberDialog.
     */
    public SubscriberDialog(SipProvider sipProvider, String event, SubscriberDialogListener listener)
    {
        super(sipProvider);
        this.listener = listener;
        this.subscribeTransaction = null;
        this.event = event;
        this.id = null;
        changeStatus(D_INIT);
    }

    public void setup(String target, String subscriber, String contact, int expires)
    {
        this.target = target;
        this.subscriber = subscriber;
        this.contact = contact;
        this.expires = expires;
    }

    // *************************** Public methods **************************

    @Override
    protected void changeStatus(int newStatus)
    {
        super.changeStatus(newStatus);
        if (isTerminated())
        {
            SipStack.context().remove(reSubscribeTask);
        }
    }

    /**
     * re subscribe before expire
     */
    public void reSubscribe()
    {
        if (StringUtils.isNotBlank(subscribeBody))
        {
            subscribe(subscribeBody);
        }
    }

    /**
     * Sends a new SUBSCRIBE request (starts a new subscription). It also
     * initializes the dialog state information.
     *
     * @param body        the message body
     */
    public void subscribe(String body)
    {
        log.info("inside subscribe(target={},subscriber={},contact={},id={},expires={})", target, subscriber, contact, id, expires);

        if (null == target || null == subscriber)
        {
            return;
        }

        this.subscribeBody = body;
        SipURL requestUri = new SipURL(target);
        NameAddress toUrl = new NameAddress(target);
        NameAddress fromUrl = new NameAddress(subscriber);
        NameAddress contactUrl;
        if (contact != null)
        {
            contactUrl = new NameAddress(contact);
        }
        else
        {
            contactUrl = fromUrl;
        }
        Message req = MessageFactory.createSubscribeRequest(sipProvider,
            requestUri,
            toUrl,
            fromUrl,
            contactUrl,
            event,
            id,
            XMLUtil.XML_MANSCDP_TYPE,
            body);
        req.setHeader(new AcceptHeader("application/xml"));
        req.setExpiresHeader(new ExpiresHeader(expires));
        subscribe(req);
    }

    /**
     * Sends a new SUBSCRIBE request (starts a new subscription). It also
     * initializes the dialog state information.
     *
     * @param req the SUBSCRIBE message
     */
    private void subscribe(Message req)
    {
        printLog("inside subscribe(req)", LogLevel.MEDIUM);
        if (statusIs(D_TERMINATED))
        {
            printLog("subscription already terminated: request aborted", LogLevel.MEDIUM);
            return;
        }
        // else
        if (statusIs(D_INIT))
        {
            changeStatus(D_SUBSCRIBING);
        }
        update(UAC, req);
        // start client transaction
        subscribeTransaction = new TransactionClient(sipProvider, req, this);
        subscribeTransaction.request();
    }

    public void terminate()
    {
        changeStatus(D_TERMINATED);
        if (null != subscribeTransaction)
        {
            subscribeTransaction.terminate();
            subscribeTransaction = null;
        }
        listener = null;
    }

    // ************** Inherited from TransactionClientListener **************

    /**
     * When the TransactionClient is (or goes) in "Proceeding" state and
     * receives a new 1xx provisional response
     */
    @Override
    public void onTransProvisionalResponse(TransactionClient tc, Message resp)
    {
        printLog("onTransProvisionalResponse()", LogLevel.MEDIUM);
    }

    /**
     * When the TransactionClient goes into the "Completed" state receiving a
     * 2xx response
     */
    @Override
    public void onTransSuccessResponse(TransactionClient tc, Message resp)
    {
        printLog("onTransSuccessResponse()", LogLevel.MEDIUM);
        if (statusIs(D_ACTIVE))
        {
            StatusLine statusLine = resp.getStatusLine();
            if (listener != null)
            {
                listener.onDlgSubscriptionSuccess(this, statusLine.getCode(), statusLine.getReason(), resp);
            }
        }
        else
        {
            changeStatus(D_ACCEPTED);
            update(UAC, resp);
            StatusLine statusLine = resp.getStatusLine();
            if (listener != null)
            {
                listener.onDlgSubscriptionSuccess(this, statusLine.getCode(), statusLine.getReason(), resp);
            }
        }

        //在订阅失效前30秒重新订阅
        SipStack.context().runDelay(reSubscribeTask, expires - 30);
    }

    /**
     * When the TransactionClient goes into the "Completed" state receiving a
     * 300-699 response
     */
    @Override
    public void onTransFailureResponse(TransactionClient tc, Message resp)
    {
        printLog("onTransFailureResponse()", LogLevel.MEDIUM);
        changeStatus(D_TERMINATED);
        StatusLine statusLine = resp.getStatusLine();
        if (listener != null)
        {
            listener.onDlgSubscriptionFailure(this, statusLine.getCode(), statusLine.getReason(), resp);
        }
    }

    /**
     * When the TransactionClient goes into the "Terminated" state, caused by
     * transaction timeout
     */
    @Override
    public void onTransTimeout(TransactionClient tc)
    {
        printLog("onTransTimeout()", LogLevel.MEDIUM);
        changeStatus(D_TERMINATED);
        if (listener != null)
        {
            listener.onDlgSubscribeTimeout(this);
        }
    }

    // ***************** Inherited from SipProviderListener *****************

    /**
     * When a new Message is received by the SipProvider.
     */
    @Override
    public void onReceivedMessage(SipProvider sipProvider, Message msg)
    {
        printLog("onReceivedMessage()", LogLevel.MEDIUM);
        if (statusIs(D_TERMINATED))
        {
            printLog("subscription already terminated: message discarded", LogLevel.MEDIUM);
            return;
        }

        if (msg.isRequest() && msg.isNotify())
        {
            TransactionServer ts = new TransactionServer(sipProvider, msg, null);
            ts.respondWith(MessageFactory.createResponse(msg, 200, null));

            NameAddress to = msg.getToHeader().getNameAddress();
            NameAddress from = msg.getFromHeader().getNameAddress();
            NameAddress contact = null;
            if (msg.hasContactHeader())
            {
                contact = msg.getContactHeader().getNameAddress();
            }
            String state = null;
            if (msg.hasSubscriptionStateHeader())
            {
                state = msg.getSubscriptionStateHeader().getState();
            }
            String contentType = null;
            if (msg.hasContentTypeHeader())
            {
                contentType = msg.getContentTypeHeader().getContentType();
            }
            String body = null;
            if (msg.hasBody())
            {
                body = msg.getBody();
            }

            if (listener != null)
            {
                listener.onDlgNotify(this, to, from, contact, state, contentType, body, msg);
            }

            if (state != null)
            {
                if (state.equalsIgnoreCase(ACTIVE) && !statusIs(D_TERMINATED))
                {
                    changeStatus(D_ACTIVE);
                }
                else if (state.equalsIgnoreCase(PENDING) && statusIs(D_ACCEPTED))
                {
                    changeStatus(D_PENDING);
                }
                else if (state.equalsIgnoreCase(TERMINATED) && !statusIs(D_TERMINATED))
                {
                    changeStatus(D_TERMINATED);
                    if (listener != null)
                    {
                        listener.onDlgSubscriptionTerminated(this);
                    }
                }
            }
        }
        else
        {
            printLog("message is not a NOTIFY: message discarded", LogLevel.HIGH);
        }
    }

    // **************************** Logs ****************************/

    /**
     * Adds a new string to the default Log
     */
    @Override
    protected void printLog(String str, int level)
    {
        Log.compatLog("SubscriberDialog#" + dialogSqn + ": " + str, level);
    }

}
