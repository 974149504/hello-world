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
import com.allcam.gbgw.protocol.sip.core.provider.ConnectionIdentifier;
import com.allcam.gbgw.protocol.sip.core.provider.SipProvider;
import com.allcam.gbgw.protocol.sip.core.provider.SipStack;
import com.allcam.gbgw.protocol.sip.core.provider.TransactionIdentifier;
import com.allcam.gbgw.protocol.tools.Log;
import com.allcam.gbgw.protocol.tools.LogLevel;
import com.allcam.gbgw.protocol.tools.Timer;

/**
 * Generic server transaction as defined in RFC 3261 (Section 17.2.2). A
 * TransactionServer is responsable to create a new SIP transaction that starts
 * with a request message received by the SipProvider and ends sending a final
 * response.<BR>
 * The changes of the internal status and the received messages are fired to the
 * TransactionListener passed to the TransactionServer object.<BR>
 * When costructing a new TransactionServer, the transaction type is passed as
 * String parameter to the costructor (e.g. "CANCEL", "BYE", etc..)
 */

public class TransactionServer extends Transaction
{
    /**
     * the TransactionServerListener that captures the events fired by the
     * TransactionServer
     */
    TransactionServerListener transaction_listener;

    /**
     * last response message
     */
    Message response;

    /**
     * clearing timeout ("Timer J" in RFC 3261)
     */
    Timer clearingTo;

    /**
     * Costructs a new TransactionServer.
     */
    protected TransactionServer(SipProvider sip_provider)
    {
        super(sip_provider);
        transaction_listener = null;
        response = null;
    }

    /**
     * Creates a new TransactionServer of type <i>method</i>.
     */
    public TransactionServer(SipProvider sipProvider, String method, TransactionServerListener listener)
    {
        super(sipProvider);
        init(listener, new TransactionIdentifier(method), null);
    }

    /**
     * Creates a new TransactionServer for the already received request <i>req</i>.
     */
    public TransactionServer(SipProvider provider, Message req, TransactionServerListener listener)
    {
        super(provider);
        request = new Message(req);
        init(listener, request.getTransactionId(), request.getConnectionId());

        printLog("start", LogLevel.LOW);
        changeStatus(STATE_TRYING);
        sipProvider.addSipProviderListener(transactionId, this);
    }

    /**
     * Initializes timeouts and listener.
     */
    private void init(TransactionServerListener listener, TransactionIdentifier transactionId,
        ConnectionIdentifier connectionId)
    {
        this.transaction_listener = listener;
        this.transactionId = transactionId;
        this.connectionId = connectionId;
        this.response = null;
        clearingTo = new Timer(SipStack.transaction_timeout, "Clearing", this);
        printLog("id: " + String.valueOf(transactionId), LogLevel.HIGH);
        printLog("created", LogLevel.HIGH);
    }

    /**
     * Starts the TransactionServer.
     */
    public void listen()
    {
        if (statusIs(STATE_IDLE))
        {
            printLog("start", LogLevel.LOW);
            changeStatus(STATE_WAITING);
            sipProvider.addSipProviderListener(transactionId, this);
        }
    }

    /**
     * Sends a response message
     */
    public void respondWith(Message resp)
    {
        response = resp;
        if (statusIs(STATE_TRYING) || statusIs(STATE_PROCEEDING))
        {
            sipProvider.sendMessage(response, connectionId);
            int code = response.getStatusLine().getCode();
            if (code >= 100 && code < 200 && statusIs(STATE_TRYING))
            {
                changeStatus(STATE_PROCEEDING);
            }
            if (code >= 200 && code < 700)
            {
                changeStatus(STATE_COMPLETED);
                clearingTo.start();
            }
        }
    }

    /**
     * Method derived from interface SipListener. It's fired from the
     * SipProvider when a new message is received for to the present
     * TransactionServer.
     */
    @Override
    public void onReceivedMessage(SipProvider sipProvider, Message msg)
    {
        if (msg.isRequest())
        {
            if (statusIs(STATE_WAITING))
            {
                request = new Message(msg);
                connectionId = msg.getConnectionId();
                this.sipProvider.removeSipProviderListener(transactionId);
                transactionId = request.getTransactionId();
                this.sipProvider.addSipProviderListener(transactionId, this);
                changeStatus(STATE_TRYING);
                if (transaction_listener != null)
                {
                    transaction_listener.onTransRequest(this, msg);
                }
            }
            else if (statusIs(STATE_PROCEEDING) || statusIs(STATE_COMPLETED))
            {
                // retransmission of the last response
                printLog("response retransmission", LogLevel.LOW);
                this.sipProvider.sendMessage(response, connectionId);
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
            if (to.equals(clearingTo))
            {
                printLog("Clearing timeout expired", LogLevel.HIGH);
                sipProvider.removeSipProviderListener(transactionId);
                changeStatus(STATE_TERMINATED);
                transaction_listener = null;
                // clearingTo=null;
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
            clearingTo.halt();
            sipProvider.removeSipProviderListener(transactionId);
            changeStatus(STATE_TERMINATED);
            transaction_listener = null;
            // clearingTo=null;
        }
    }

    // **************************** Logs ****************************/

    /**
     * Adds a new string to the default Log
     */
    @Override
    protected void printLog(String str, int level)
    {
        Log.compatLog("TransactionServer#" + transactionSqn + ": " + str, level);
    }

}
