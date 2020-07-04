package sip.core.dialog;

import com.allcam.gbgw.protocol.gb28181.message.XMLUtil;
import com.allcam.gbgw.protocol.sip.core.address.NameAddress;
import com.allcam.gbgw.protocol.sip.core.header.EventHeader;
import com.allcam.gbgw.protocol.sip.core.header.ExpiresHeader;
import com.allcam.gbgw.protocol.sip.core.header.StatusLine;
import com.allcam.gbgw.protocol.sip.core.header.SubscriptionStateHeader;
import com.allcam.gbgw.protocol.sip.core.message.Message;
import com.allcam.gbgw.protocol.sip.core.message.MessageFactory;
import com.allcam.gbgw.protocol.sip.core.message.SipMethods;
import com.allcam.gbgw.protocol.sip.core.provider.Identifier;
import com.allcam.gbgw.protocol.sip.core.provider.MethodIdentifier;
import com.allcam.gbgw.protocol.sip.core.provider.SipProvider;
import com.allcam.gbgw.protocol.sip.core.provider.SipStack;
import com.allcam.gbgw.protocol.sip.core.transaction.TransactionClient;
import com.allcam.gbgw.protocol.sip.core.transaction.TransactionClientListener;
import com.allcam.gbgw.protocol.tools.Log;
import com.allcam.gbgw.protocol.tools.LogLevel;
import lombok.extern.slf4j.Slf4j;

/**
 * NotifierDialog.
 */
@Slf4j
public class NotifierDialog extends Dialog implements TransactionClientListener
{
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
     * The SubscriberDialog listener
     */
    private NotifierDialogListener listener;

    /**
     * The current subscribe method
     */
    private Message subscribeReq;

    /**
     * The current notify transaction
     */
    private TransactionClient notifyTransaction;

    private int expires;

    /**
     * The event name
     */
    private String event;

    /**
     * The subscription id
     */
    private String id;

    /**
     * Internal state D_INIT (the starting point)
     */
    private static final int D_INIT = 0;

    /**
     * Internal state D_WAITING (listening for the first subscription request)
     */
    private static final int D_WAITING = 1;

    /**
     * Internal state D_SUBSCRIBED (first subscription request arrived)
     */
    private static final int D_SUBSCRIBED = 2;

    /**
     * Internal state D_PENDING (first subscription request has been accepted)
     */
    private static final int D_PENDING = 3;

    /**
     * Internal state D_ACTIVE (subscription has been activated)
     */
    private static final int D_ACTIVE = 4;

    /**
     * Internal state D_TERMINATED (first subscription request has been refused
     * or subscription has been terminated)
     */
    private static final int D_TERMINATED = 9;

    // ************************* Protected methods ************************

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
            case D_WAITING:
                return "D_WAITING";
            case D_SUBSCRIBED:
                return "D_SUBSCRIBED";
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

    // ************************** Public methods **************************

    /**
     * Whether the dialog is in "early" state.
     */
    @Override
    public boolean isEarly()
    {
        return (status < D_PENDING);
    }

    /**
     * Whether the dialog is in "confirmed" state.
     */
    @Override
    public boolean isConfirmed()
    {
        return (status >= D_PENDING && status < D_TERMINATED);
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
        return (status >= D_SUBSCRIBED && status < D_ACTIVE);
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
     * Gets the event "id" parameter.
     */
    public String getId()
    {
        return id;
    }

    // **************************** Costructors ****************************

    /**
     * Creates a new NotifierDialog.
     */
    public NotifierDialog(SipProvider sipProvider, String fromUsername, NotifierDialogListener listener)
    {
        super(sipProvider, fromUsername);
        init(listener);
    }

    /**
     * Creates a new NotifierDialog for the already received SUBSCRIBE request
     * <i>subscribe</i>.
     */
    public NotifierDialog(SipProvider sipProvider, Message subscribe, NotifierDialogListener listener)
    {
        super(sipProvider);
        init(listener);

        changeStatus(D_SUBSCRIBED);
        subscribeReq = subscribe;
        update(Dialog.UAS, subscribe);
        EventHeader eh = subscribe.getEventHeader();
        if (eh != null)
        {
            event = eh.getEvent();
            id = eh.getId();
        }
    }

    /**
     * Inits the NotifierDialog.
     */
    private void init(NotifierDialogListener listener)
    {
        this.listener = listener;
        this.notifyTransaction = null;
        this.subscribeReq = null;
        this.event = null;
        this.id = null;
        this.expires = 0;
        changeStatus(D_INIT);
    }

    // *************************** Public methods **************************

    /**
     * Listen for the first subscription request.
     */
    public void listen()
    {
        printLog("inside method listen()", LogLevel.MEDIUM);
        if (!statusIs(D_INIT))
        {
            printLog("first subscription already received", LogLevel.MEDIUM);
            return;
        }
        // else
        changeStatus(D_WAITING);
        // listen for the first SUBSCRIBE request
        Identifier identifier = new MethodIdentifier(SipMethods.SUBSCRIBE, getFromUsername());
        sipProvider.removeSipProviderListener(identifier);
        sipProvider.addSipProviderListener(identifier, this);
    }

    public void listenNext()
    {
        new NotifierDialog(sipProvider, getFromUsername(), listener).listen();
    }

    /**
     * Accepts the subscription request (sends a "202 Accepted" response).
     */
    public void accept(String contact)
    {
        printLog("inside accept()", LogLevel.MEDIUM);
        respond(200, expires, contact);
    }

    /**
     * Refuses the subscription request.
     */
    public void refuse()
    {
        printLog("inside refuse()", LogLevel.MEDIUM);
        respond(403, -1, null);
    }

    /**
     * Responds with <i>code</i> and <i>reason</i>. This method can be called
     * when the InviteDialog is in D_INVITED, D_ReINVITED states
     */
    private void respond(int code, int expires, String contact)
    {
        printLog("inside respond(" + code + ")", LogLevel.MEDIUM);
        NameAddress contactUrl = null;
        if (contact != null)
        {
            contactUrl = new NameAddress(contact);
        }
        Message resp = MessageFactory.createResponse(subscribeReq, code, contactUrl);
        if (expires >= 0)
        {
            resp.setExpiresHeader(new ExpiresHeader(expires));
        }
        respond(resp);
    }

    /**
     * Responds with <i>resp</i>.
     */
    public void respond(Message resp)
    {
        printLog("inside respond(resp)", LogLevel.MEDIUM);
        if (statusIs(NotifierDialog.D_SUBSCRIBED) && resp.getStatusLine().getCode() >= 200)
        {
            update(UAS, resp);
        }
        sipProvider.sendMessage(resp, resp.getConnectionId());
    }

    /**
     * Activates the subscription (subscription goes into 'active' state).
     */
    public void activate()
    {
        activate(SipStack.default_expires);
    }

    /**
     * Activates the subscription (subscription goes into 'active' state).
     */
    public void activate(int expires)
    {
        notify(ACTIVE, expires, null, null);
    }

    /**
     * Makes the subscription pending (subscription goes into 'pending' state).
     */
    public void pending()
    {
        pending(SipStack.default_expires);
    }

    /**
     * Makes the subscription pending (subscription goes into 'pending' state).
     */
    public void pending(int expires)
    {
        notify(PENDING, expires, null, null);
    }

    /**
     * Terminates the subscription (subscription goes into 'terminated' state).
     */
    public void terminate()
    {
        terminate(null);
    }

    /**
     * Terminates the subscription (subscription goes into 'terminated' state).
     */
    public void terminate(String reason)
    {
        Message req = MessageFactory.createNotifyRequest(this, event, id, null, null);
        SubscriptionStateHeader sh = new SubscriptionStateHeader(TERMINATED);
        if (reason != null)
        {
            sh.setReason(reason);
        }
        // sh.setExpires(0);
        req.setSubscriptionStateHeader(sh);
        notify(req);
    }

    /**
     * Sends a NOTIFY.
     */
    public void notifyActive(String body)
    {
        notify(ACTIVE, 60, XMLUtil.XML_MANSCDP_TYPE, body);
    }

    /**
     * Sends a NOTIFY.
     */
    public void notify(String state, int expires, String contentType, String body)
    {
        Message req = MessageFactory.createNotifyRequest(this, event, id, contentType, body);
        if (state != null)
        {
            SubscriptionStateHeader sh = new SubscriptionStateHeader(state);
            if (expires >= 0)
            {
                sh.setExpires(expires);
            }
            req.setSubscriptionStateHeader(sh);
        }
        notify(req);
    }

    /**
     * Sends a NOTIFY.
     */
    public void notify(Message req)
    {
        String subscriptionState = req.getSubscriptionStateHeader().getState();
        if (subscriptionState.equalsIgnoreCase(ACTIVE) && (statusIs(D_SUBSCRIBED) || statusIs(D_PENDING)))
        {
            changeStatus(D_ACTIVE);
        }
        else if (subscriptionState.equalsIgnoreCase(PENDING) && statusIs(D_SUBSCRIBED))
        {
            changeStatus(D_PENDING);
        }
        else if (subscriptionState.equalsIgnoreCase(TERMINATED) && !statusIs(D_TERMINATED))
        {
            changeStatus(D_TERMINATED);
        }

        TransactionClient notifyTransaction = new TransactionClient(sipProvider, req, this);
        notifyTransaction.request();
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
        // do nothing.
    }

    /**
     * When the TransactionClient goes into the "Completed" state receiving a
     * 2xx response
     */
    @Override
    public void onTransSuccessResponse(TransactionClient tc, Message resp)
    {
        printLog("onTransSuccessResponse()", LogLevel.MEDIUM);
        StatusLine statusLine = resp.getStatusLine();
        if (listener != null)
        {
            listener.onDlgNotificationSuccess(this, statusLine.getCode(), statusLine.getReason(), resp);
        }
    }

    /**
     * When the TransactionClient goes into the "Completed" state receiving a
     * 300-699 response
     */
    @Override
    public void onTransFailureResponse(TransactionClient tc, Message resp)
    {
        printLog("onTransFailureResponse()", LogLevel.MEDIUM);
        StatusLine statusLine = resp.getStatusLine();
        if (listener != null)
        {
            listener.onDlgNotificationFailure(this, statusLine.getCode(), statusLine.getReason(), resp);
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
        if (!statusIs(D_TERMINATED))
        {
            changeStatus(D_TERMINATED);
            if (listener != null)
            {
                listener.onDlgNotifyTimeout(this);
            }
        }
    }

    // ************** Inherited from SipProviderListener **************

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

        if (msg.isRequest() && msg.isSubscribe())
        {
            subscribeReq = msg;
            this.expires = msg.getExpiresHeader().getDeltaSeconds();
            NameAddress target = msg.getToHeader().getNameAddress();
            NameAddress subscriber = msg.getFromHeader().getNameAddress();

            if (statusIs(NotifierDialog.D_WAITING))
            {
                if (expires == 0)
                {
                    log.info("dialog is waiting and receive expired subscribe.");
                    accept(null);
                    return;
                }
                changeStatus(D_SUBSCRIBED);
                this.sipProvider.removeSipProviderListener(new MethodIdentifier(SipMethods.SUBSCRIBE));
            }
            else
            {
                if (expires == 0)
                {
                    log.info("receive expired subscribe, terminate this dialog.");
                    if (listener != null)
                    {
                        listener.onDlgSubscribe(this, target, subscriber, msg, expires);
                    }
                    changeStatus(D_TERMINATED);
                    listener = null;
                    return;
                }
            }

            EventHeader eh = msg.getEventHeader();
            if (eh != null)
            {
                event = eh.getEvent();
                id = eh.getId();
            }
            update(UAS, msg);
            if (listener != null)
            {
                listener.onDlgSubscribe(this, target, subscriber, msg, expires);
            }
        }
        else
        {
            printLog("message is not a SUBSCRIBE: message discarded", LogLevel.HIGH);
        }
    }

    // **************************** Logs ****************************/

    /**
     * Adds a new string to the default Log
     */
    @Override
    protected void printLog(String str, int level)
    {
        Log.compatLog("NotifierDialog#" + dialogSqn + ": " + str, level);
    }

}
