/*
 * Copyright (C) 2005 Luca Veltri - University of Parma - Italy
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
import com.allcam.gbgw.protocol.sip.core.provider.SipProvider;
import com.allcam.gbgw.protocol.tools.Log;
import com.allcam.gbgw.protocol.tools.LogLevel;

/**
 * ACK client transaction should follow an INVITE client transaction within an
 * INVITE Dialog in a SIP UAC. The AckTransactionClient simply sends an ACK
 * request message and terminates.
 */
public class AckTransactionClient extends Transaction
{

    /**
     * Creates a new AckTransactionClient.
     */
    public AckTransactionClient(SipProvider sipProvider, Message ack)
    {
        super(sipProvider);
        request = new Message(ack);
        transactionId = request.getTransactionId();
        printLog("id: " + transactionId, LogLevel.HIGH);
        printLog("created", LogLevel.HIGH);
    }

    /**
     * Starts the AckTransactionClient and sends the ACK request.
     */
    public void request()
    {
        sipProvider.sendMessage(request);
        changeStatus(STATE_TERMINATED);
    }

    /**
     * Method used to drop an active transaction.
     */
    @Override
    public void terminate()
    {
        changeStatus(STATE_TERMINATED);
        // (CHANGE-040421) free the link to transactionListener
    }

    // **************************** Logs ****************************/

    /**
     * Adds a new string to the default Log
     */
    @Override
    protected void printLog(String str, int level)
    {
        Log.compatLog("AckTransactionClient#" + transactionSqn + ": " + str, level);
    }

}
