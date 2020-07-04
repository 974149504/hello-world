package sip.core.transaction;

import com.allcam.gbgw.protocol.sip.core.message.Message;
import com.allcam.gbgw.protocol.sip.core.message.MessageFactory;
import com.allcam.gbgw.protocol.sip.core.provider.SipProvider;
import com.allcam.gbgw.protocol.sip.core.provider.SipStack;
import com.allcam.gbgw.protocol.sip.core.provider.TransactionIdentifier;
import com.allcam.gbgw.protocol.tools.LogLevel;
import com.allcam.gbgw.protocol.tools.Timer;

/**
 * INVITE client transaction as defined in RFC 3261 (Section 17.2.1). <BR>
 * An InviteTransactionClient is responsable to create a new SIP invite
 * transaction, starting with a invite message sent through the SipProvider and
 * ending with a final response. <BR>
 * The changes of the internal status and the received messages are fired to the
 * TransactionListener passed to the InviteTransactionClient object.
 */
public class InviteTransactionClient extends TransactionClient
{
    /**
     * the TransactionClientListener that captures the events fired by the
     * InviteTransactionClient
     */
    private TransactionClientListener transactionListener;

    /**
     * ack message
     */
    private Message ack;

    /**
     * end timeout for invite transactions ("Timer D" in RFC 3261)
     */
    private Timer endTo;

    /**
     * Creates a new ClientTransaction
     */
    public InviteTransactionClient(SipProvider sipProvider, Message req, TransactionClientListener listener)
    {
        super(sipProvider);
        request = new Message(req);
        init(listener, request.getTransactionId());
    }

    /**
     * Initializes timeouts and listener.
     */
    @Override
    void init(TransactionClientListener listener, TransactionIdentifier transactionId)
    {
        this.transactionListener = listener;
        this.transactionId = transactionId;
        this.ack = null;
        retransmissionTo = SipStack.retransmissionTimer(this);
        transactionTo = new Timer(SipStack.transaction_timeout, "Transaction", this);
        endTo = new Timer(SipStack.transaction_timeout, "End", this);
        printLog("id: " + transactionId, LogLevel.HIGH);
        printLog("created", LogLevel.HIGH);
    }

    /**
     * Starts the InviteTransactionClient and sends the invite request.
     */
    @Override
    public void request()
    {
        printLog("start", LogLevel.LOW);
        changeStatus(STATE_TRYING);
        retransmissionTo.start();
        transactionTo.start();

        sipProvider.addSipProviderListener(transactionId, this);
        connectionId = sipProvider.sendMessage(request);
    }

    /**
     * Method derived from interface SipListener. It's fired from the
     * SipProvider when a new message is catch for to the present
     * ServerTransaction.
     */
    @Override
    public void onReceivedMessage(SipProvider sipProvider, Message msg)
    {
        if (msg.isResponse())
        {
            int code = msg.getStatusLine().getCode();
            if (code >= 100 && code < 200 && (statusIs(STATE_TRYING) || statusIs(STATE_PROCEEDING)))
            {
                if (statusIs(STATE_TRYING))
                {
                    retransmissionTo.halt();
                    transactionTo.halt();
                    changeStatus(STATE_PROCEEDING);
                }
                if (transactionListener != null)
                {
                    transactionListener.onTransProvisionalResponse(this, msg);
                }
            }
            else if (code >= 300 && code < 700 &&
                (statusIs(STATE_TRYING) || statusIs(STATE_PROCEEDING) || statusIs(STATE_COMPLETED)))
            {
                if (statusIs(STATE_TRYING) || statusIs(STATE_PROCEEDING))
                {
                    retransmissionTo.halt();
                    transactionTo.halt();
                    ack = MessageFactory.createNon2xxAckRequest(this.sipProvider, request, msg);
                    changeStatus(STATE_COMPLETED);
                    connectionId = this.sipProvider.sendMessage(ack);
                    if (transactionListener != null)
                    {
                        transactionListener.onTransFailureResponse(this, msg);
                    }
                    transactionListener = null;
                    endTo.start();
                }
                else
                {
                    // retransmit ACK only in case of unreliable transport
                    this.sipProvider.sendMessage(ack);
                }
            }
            else if (code >= 200 && code < 300 && (statusIs(STATE_TRYING) || statusIs(STATE_PROCEEDING)))
            {
                if (transactionListener != null)
                {
                    transactionListener.onTransSuccessResponse(this, msg);
                }
                terminate();
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
            if (to.equals(retransmissionTo) && statusIs(STATE_TRYING))
            {
                printLog("Retransmission timeout expired", LogLevel.HIGH);
                sipProvider.sendMessage(request);
                retransmissionTo.retry();
            }
            if (to.equals(transactionTo))
            {
                printLog("Transaction timeout expired", LogLevel.HIGH);
                if (transactionListener != null)
                {
                    transactionListener.onTransTimeout(this);
                }
                terminate();
            }
            if (to.equals(endTo))
            {
                printLog("End timeout expired", LogLevel.HIGH);
                terminate();
            }
        }
        catch (Exception e)
        {
            printException(e, LogLevel.HIGH);
        }
    }

    /**
     * Terminates the transaction.
     */
    @Override
    public void terminate()
    {
        if (!statusIs(STATE_TERMINATED))
        {
            retransmissionTo.halt();
            transactionTo.halt();
            endTo.halt();
            sipProvider.removeSipProviderListener(transactionId);
            changeStatus(STATE_TERMINATED);
            transactionListener = null;
        }
    }

}
