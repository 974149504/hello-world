package sip.core.transaction;

import com.allcam.gbgw.protocol.sip.core.header.ContactHeader;
import com.allcam.gbgw.protocol.sip.core.message.Message;
import com.allcam.gbgw.protocol.sip.core.message.MessageFactory;
import com.allcam.gbgw.protocol.sip.core.message.SipMethods;
import com.allcam.gbgw.protocol.sip.core.provider.ConnectionIdentifier;
import com.allcam.gbgw.protocol.sip.core.provider.SipProvider;
import com.allcam.gbgw.protocol.sip.core.provider.SipStack;
import com.allcam.gbgw.protocol.sip.core.provider.TransactionIdentifier;
import com.allcam.gbgw.protocol.tools.LogLevel;
import com.allcam.gbgw.protocol.tools.Timer;

/**
 * INVITE server transaction as defined in RFC 3261 (Section 17.2.1). <BR>
 * An InviteTransactionServer is responsable to create a new SIP invite
 * transaction that starts with a INVITE message received by the SipProvider and
 * ends sending a final response. <BR>
 * The changes of the internal status and the received messages are fired to the
 * TransactionListener passed to the InviteTransactionServer object. <BR>
 * This implementation of InviteTransactionServer automatically generates a "100
 * Trying" response when the INVITE message is received (as suggested by
 * RFC3261)
 */
public class InviteTransactionServer extends TransactionServer
{
    /**
     * the TransactionServerListener that captures the events fired by the
     * InviteTransactionServer
     */
    private InviteTransactionServerListener transactionListener;

    /**
     * retransmission timeout ("Timer G" in RFC 3261)
     */
    private Timer retransmissionTo;

    /**
     * end timeout ("Timer H" in RFC 3261)
     */
    private Timer endTo;

    /**
     * from username
     */
    private String fromUsername;

    /**
     * Whether automatically sending 100 Trying on INVITE.
     */
    private boolean autoTrying;

    /**
     * Creates a new InviteTransactionServer.
     */
    public InviteTransactionServer(SipProvider sipProvider, String fromUsername,
        InviteTransactionServerListener listener)
    {
        super(sipProvider);
        init(listener, new TransactionIdentifier(SipMethods.INVITE, fromUsername), null);
    }

    /**
     * Creates a new InviteTransactionServer for the already received INVITE
     * request <i>invite</i>.
     */
    public InviteTransactionServer(SipProvider sipProvider, Message invite, InviteTransactionServerListener listener)
    {
        super(sipProvider);
        request = new Message(invite);
        init(listener, request.getTransactionId(), request.getConnectionId());

        changeStatus(STATE_TRYING);
        sipProvider.addSipProviderListener(transactionId, this);
        // automatically send "100 Tryng" response and go to STATE_PROCEEDING
        if (autoTrying)
        {
            Message trying100 = MessageFactory.createResponse(request, 100, null);
            respondWith(trying100);
            // this method makes it going automatically to STATE_PROCEEDING
        }
    }

    /**
     * Creates a new InviteTransactionServer for the already received INVITE
     * request <i>invite</i>.
     */
    public InviteTransactionServer(SipProvider sipProvider, Message invite, boolean autoTrying,
        InviteTransactionServerListener listener)
    {
        super(sipProvider);
        request = new Message(invite);
        init(listener, request.getTransactionId(), request.getConnectionId());
        this.autoTrying = autoTrying;

        changeStatus(STATE_TRYING);
        sipProvider.addSipProviderListener(transactionId, this);
        // automatically send "100 Tryng" response and go to STATE_PROCEEDING
        if (autoTrying)
        {
            Message trying100 = MessageFactory.createResponse(request, 100, null);
            respondWith(trying100);
            // this method makes it going automatically to STATE_PROCEEDING
        }
    }

    /**
     * Initializes timeouts and listener.
     */
    private void init(InviteTransactionServerListener listener, TransactionIdentifier transactionId,
        ConnectionIdentifier connectionId)
    {
        this.transactionListener = listener;
        this.transactionId = transactionId;
        this.connectionId = connectionId;
        //Default behavior for automatically sending 100 Trying on INVITE.
        autoTrying = true;
        retransmissionTo = SipStack.retransmissionTimer(this);
        endTo = new Timer(SipStack.transaction_timeout, "End", this);
        clearingTo = new Timer(SipStack.clearing_timeout, "Clearing", this);
        printLog("id: " + transactionId, LogLevel.HIGH);
        printLog("created", LogLevel.HIGH);
    }

    /**
     * Whether automatically sending 100 Trying on INVITE.
     */
    public void setAutoTrying(boolean autoTrying)
    {
        this.autoTrying = autoTrying;
    }

    /**
     * Starts the InviteTransactionServer.
     */
    @Override
    public void listen()
    {
        printLog("start", LogLevel.LOW);
        if (statusIs(STATE_IDLE))
        {
            changeStatus(STATE_WAITING);
            sipProvider.addSipProviderListener(transactionId, this);
//            sipProvider.addSipProviderListener(new TransactionIdentifier(SipMethods.OPTIONS), this);
        }
    }

    /**
     * Sends a response message
     */
    @Override
    public void respondWith(Message resp)
    {
        response = resp;
        int code = response.getStatusLine().getCode();
        if (statusIs(STATE_TRYING) || statusIs(STATE_PROCEEDING))
        {
            sipProvider.sendMessage(response, connectionId);
        }
        if (code >= 100 && code < 200 && statusIs(STATE_TRYING))
        {
            changeStatus(STATE_PROCEEDING);
            return;
        }

        if (code >= 200 && code < 300 && (statusIs(STATE_TRYING) || statusIs(STATE_PROCEEDING)))
        {
            terminate();
            return;
        }

        if (code >= 300 && code < 700 && (statusIs(STATE_TRYING) || statusIs(STATE_PROCEEDING)))
        {
            changeStatus(STATE_COMPLETED);
            // retransmission only in case of unreliable transport
            retransmissionTo.start();
            endTo.start();
        }
    }

    /**
     * Method derived from interface SipListener. It's fired from the
     * SipProvider when a new message is catch for to the present
     * ServerTransaction.
     */
    @Override
    public void onReceivedMessage(SipProvider sipProvider, Message msg)
    {
        if (msg.isRequest())
        {
            String reqMethod = msg.getRequestLine().getMethod();

            // invite received
            if (reqMethod.equals(SipMethods.INVITE))
            {
                if (statusIs(STATE_WAITING))
                {
                    TransactionIdentifier newTranslId = msg.getTransactionId();
                    if (this.sipProvider.addSipProviderListener(newTranslId, this))
                    {
                        this.sipProvider.removeSipProviderListener(transactionId);
                        transactionId = newTranslId;
                    }
                    else
                    {
                        printLog("same invite dispatch here, ignore it.", LogLevel.MEDIUM);
                        return;
                    }

                    request = new Message(msg);
                    connectionId = request.getConnectionId();

                    changeStatus(STATE_TRYING);
                    // automatically send "100 Tryng" response and go to STATE_PROCEEDING
                    if (autoTrying)
                    {
                        Message trying100 = MessageFactory.createResponse(request, 100, null);
                        respondWith(trying100);
                        // this method makes it going automatically to STATE_PROCEEDING
                    }
                    if (transactionListener != null)
                    {
                        transactionListener.onTransRequest(this, msg);
                    }
                    return;
                }
                if (statusIs(STATE_PROCEEDING) || statusIs(STATE_COMPLETED))
                {
                    // retransmission of the last response
                    this.sipProvider.sendMessage(response, connectionId);
                    return;
                }
            }

            if (reqMethod.equals(SipMethods.OPTIONS))
            {
                Message ok200 = MessageFactory.createResponse(msg, 200, null);
                ok200.removeServerHeader();
                ok200.addContactHeader(new ContactHeader(ok200.getToHeader().getNameAddress()), false);
                this.sipProvider.sendMessage(ok200, connectionId);
                return;
            }

            // ack received
            if (reqMethod.equals(SipMethods.ACK) && statusIs(STATE_COMPLETED))
            {
                retransmissionTo.halt();
                endTo.halt();
                changeStatus(STATE_CONFIRMED);
                if (transactionListener != null)
                {
                    transactionListener.onTransFailureAck(this, msg);
                }
                clearingTo.start();
            }
        }
    }

    /**
     * Method derived from interface TimerListener. It's fired from an active
     * Timer.
     */
    @Override
    public void onTimeout(Timer to)
    {
        try
        {
            if (to.equals(retransmissionTo) && statusIs(STATE_COMPLETED))
            {
                printLog("Retransmission timeout expired", LogLevel.HIGH);
                sipProvider.sendMessage(response, connectionId);
                retransmissionTo.retry();
            }
            if (to.equals(endTo) && statusIs(STATE_COMPLETED))
            {
                printLog("End timeout expired", LogLevel.HIGH);
                terminate();
            }
            if (to.equals(clearingTo) && statusIs(STATE_CONFIRMED))
            {
                printLog("Clearing timeout expired", LogLevel.HIGH);
                terminate();
            }
        }
        catch (Exception e)
        {
            printException(e, LogLevel.HIGH);
        }
    }

    /**
     * Method used to drop an active transaction
     */
    @Override
    public void terminate()
    {
        retransmissionTo.halt();
        clearingTo.halt();
        endTo.halt();
        sipProvider.removeSipProviderListener(transactionId);
        changeStatus(STATE_TERMINATED);
        transactionListener = null;
    }

}
