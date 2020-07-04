package sip.core.transaction;

import com.allcam.gbgw.protocol.sip.core.message.Message;
import com.allcam.gbgw.protocol.sip.core.provider.SipProvider;
import com.allcam.gbgw.protocol.sip.core.provider.SipStack;
import com.allcam.gbgw.protocol.sip.core.provider.TransactionIdentifier;
import com.allcam.gbgw.protocol.tools.Log;
import com.allcam.gbgw.protocol.tools.LogLevel;
import com.allcam.gbgw.protocol.tools.Timer;

/**
 * Generic client transaction as defined in RFC 3261 (Section 17.1.2). A
 * TransactionClient is responsable to create a new SIP transaction, starting
 * with a request message sent through the SipProvider and ending with a final
 * response.<BR>
 * The changes of the internal status and the received messages are fired to the
 * TransactionListener passed to the TransactionClient object.<BR>
 */

public class TransactionClient extends Transaction
{
    /**
     * the TransactionClientListener that captures the events fired by the
     * TransactionClient
     */
    private TransactionClientListener transactionListener;

    /**
     * retransmission timeout ("Timer E" in RFC 3261)
     */
    Timer retransmissionTo;

    /**
     * transaction timeout ("Timer F" in RFC 3261)
     */
    Timer transactionTo;

    /**
     * clearing timeout ("Timer K" in RFC 3261)
     */
    private Timer clearingTo;

    /**
     * Costructs a new TransactionClient.
     */
    protected TransactionClient(SipProvider sipProvider)
    {
        super(sipProvider);
        transactionListener = null;
    }

    /**
     * Creates a new TransactionClient
     */
    public TransactionClient(SipProvider sipProvider, Message req, TransactionClientListener listener)
    {
        super(sipProvider);
        request = new Message(req);
        init(listener, request.getTransactionId());
    }

    public TransactionClient(SipProvider sipProvider, Message req, TransactionClientListener listener, int timeout)
    {
        super(sipProvider);
        request = new Message(req);
        transactionTo = new Timer(timeout, "Transaction", this);
        init(listener, request.getTransactionId());
    }

    /**
     * Initializes timeouts and listener.
     */
    void init(TransactionClientListener listener, TransactionIdentifier transactionId)
    {
        this.transactionListener = listener;
        this.transactionId = transactionId;
        if (null == retransmissionTo)
        {
            retransmissionTo = SipStack.retransmissionTimer(this);
        }
        if (null == transactionTo)
        {
            transactionTo = new Timer(SipStack.transaction_timeout, "Transaction", this);
        }
        if (null == clearingTo)
        {
            clearingTo = new Timer(SipStack.clearing_timeout, "Clearing", this);
        }
        printLog("id: " + transactionId, LogLevel.HIGH);
        printLog("created", LogLevel.HIGH);
    }

    /**
     * Starts the TransactionClient and sends the transaction request.
     */
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
     * SipProvider when a new message is received for to the present
     * TransactionClient.
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
                    changeStatus(STATE_PROCEEDING);
                }
                if (transactionListener != null)
                {
                    transactionListener.onTransProvisionalResponse(this, msg);
                }
            }
            else if (code >= 200 && code < 700 && (statusIs(STATE_TRYING) || statusIs(STATE_PROCEEDING)))
            {
                retransmissionTo.halt();
                transactionTo.halt();
                changeStatus(STATE_COMPLETED);
                if (code < 300)
                {
                    if (transactionListener != null)
                    {
                        transactionListener.onTransSuccessResponse(this, msg);
                    }
                }
                else
                {
                    if (transactionListener != null)
                    {
                        transactionListener.onTransFailureResponse(this, msg);
                    }
                }
                transactionListener = null;
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
            if (to.equals(retransmissionTo) && (statusIs(STATE_TRYING) || statusIs(STATE_PROCEEDING)))
            {
                sipProvider.sendMessage(request);
                if (statusIs(STATE_PROCEEDING))
                {
                    retransmissionTo.retry(SipStack.max_retransmission_timeout);
                }
                else
                {
                    retransmissionTo.retry();
                }
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
            if (to.equals(clearingTo))
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
     * Terminates the transaction.
     */
    @Override
    public void terminate()
    {
        if (!statusIs(STATE_TERMINATED))
        {
            retransmissionTo.halt();
            transactionTo.halt();
            clearingTo.halt();
            sipProvider.removeSipProviderListener(transactionId);
            changeStatus(STATE_TERMINATED);
            transactionListener = null;
        }
    }

    // **************************** Logs ****************************/

    /**
     * Adds a new string to the default Log
     */
    @Override
    protected void printLog(String str, int level)
    {
        Log.compatLog("TransactionClient#" + transactionSqn + ": " + str, level);
    }

}
