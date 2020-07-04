/*
 * Copyright (C) 2005 Luca Veltri - University of Parma - Italy
 * Copyright (C) 2009 The Sipdroid Open Source Project
 *
 * This file is part of MjSip (http://www.mjsip.org)
 *
 * MjSip is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * MjSip is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MjSip; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * Author(s):
 * Luca Veltri (luca.veltri@unipr.it)
 */

package sip.core.transaction;

import com.allcam.gbgw.protocol.sip.core.message.Message;
import com.allcam.gbgw.protocol.sip.core.message.SipMethods;
import com.allcam.gbgw.protocol.sip.core.provider.ConnectionIdentifier;
import com.allcam.gbgw.protocol.sip.core.provider.SipProvider;
import com.allcam.gbgw.protocol.sip.core.provider.SipStack;
import com.allcam.gbgw.protocol.sip.core.provider.TransactionIdentifier;
import com.allcam.gbgw.protocol.tools.Log;
import com.allcam.gbgw.protocol.tools.LogLevel;
import com.allcam.gbgw.protocol.tools.Timer;

/**
 * ACK server transaction should follow an INVITE server transaction within an
 * INVITE Dialog in a SIP UAC. The AckTransactionServer sends the final response
 * message and retransmits it several times until the method terminate() is
 * called or the trasaction timeout fires.
 */
public class AckTransactionServer extends Transaction
{
    /**
     * the TransactionServerListener that captures the events fired by the
     * AckTransactionServer
     */
    private AckTransactionServerListener transactionListener;

    /**
     * last response message
     */
    private Message response;

    /**
     * retransmission timeout
     */
    private Timer retransmissionTo;

    /**
     * transaction timeout
     */
    private Timer transactionTo;

    /** Initializes timeouts and state */
    /**
     * Creates a new AckTransactionServer. The AckTransactionServer starts
     * sending a the <i>resp</i> message. It retransmits the resp several times
     * if no ACK request is received.
     */
    public AckTransactionServer(SipProvider sipProvider, Message resp, AckTransactionServerListener listener)
    {
        super(sipProvider);
        response = resp;
        init(listener, new TransactionIdentifier(SipMethods.ACK), null);
    }

    /**
     * Creates a new AckTransactionServer. The AckTransactionServer starts
     * sending the response message <i>resp</i> through the connection
     * <i>conn_id</i>.
     */
    public AckTransactionServer(SipProvider sipProvider, ConnectionIdentifier connectionId, Message resp,
        AckTransactionServerListener listener)
    {
        super(sipProvider);
        response = resp;
        init(listener, new TransactionIdentifier(SipMethods.ACK), connectionId);
    }

    /**
     * Initializes timeouts and listener.
     */
    private void init(AckTransactionServerListener listener, TransactionIdentifier transactionId,
        ConnectionIdentifier connectionId)
    {
        this.transactionListener = listener;
        this.transactionId = transactionId;
        this.connectionId = connectionId;
        transactionTo = new Timer(SipStack.transaction_timeout, "Transaction", this);
        retransmissionTo = SipStack.retransmissionTimer(this);
        printLog("id: " + transactionId, LogLevel.HIGH);
        printLog("created", LogLevel.HIGH);
    }

    /**
     * Starts the AckTransactionServer.
     */
    public void respond()
    {
        printLog("start", LogLevel.LOW);
        changeStatus(STATE_PROCEEDING);
        // transactionId=null; // it is not required since no
        // SipProviderListener is implemented
        // (CHANGE-040905) now timeouts started in listen()
        transactionTo.start();
        retransmissionTo.start();

        sipProvider.sendMessage(response, connectionId);
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
            if (to.equals(retransmissionTo) && statusIs(STATE_PROCEEDING))
            {
                printLog("Retransmission timeout expired", LogLevel.HIGH);
                sipProvider.sendMessage(response, connectionId);
                retransmissionTo.retry();
            }
            if (to.equals(transactionTo) && statusIs(STATE_PROCEEDING))
            {
                printLog("Transaction timeout expired", LogLevel.HIGH);
                if (transactionListener != null)
                {
                    transactionListener.onTransAckTimeout(this);
                }
                terminate();
            }
        }
        catch (Exception e)
        {
            printException(e, LogLevel.HIGH);
        }
    }

    /**
     * Method used to drop an active transaction.
     */
    @Override
    public void terminate()
    {
        retransmissionTo.halt();
        transactionTo.halt();
        changeStatus(STATE_TERMINATED);
        transactionListener = null;
    }

    // **************************** Logs ****************************/

    /**
     * Adds a new string to the default Log
     */
    @Override
    protected void printLog(String str, int level)
    {
        Log.compatLog("AckTransactionServer#" + transactionSqn + ": " + str, level);
    }

}
