/*
 * Copyright (C) 2009 The Sipdroid Open Source Project
 * Copyright (C) 2005 Luca Veltri - University of Parma - Italy
 *
 * This file is part of Sipdroid (http://www.sipdroid.org)
 *
 * Sipdroid is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This source code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this source code; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package sip.net;

import com.allcam.common.utils.XMLPaser;
import com.allcam.gbgw.protocol.gb28181.message.KeepaliveMessage;
import com.allcam.gbgw.protocol.gb28181.message.XMLUtil;
import com.allcam.gbgw.protocol.sip.core.address.NameAddress;
import com.allcam.gbgw.protocol.sip.core.address.SipURL;
import com.allcam.gbgw.protocol.sip.core.message.Message;
import com.allcam.gbgw.protocol.sip.core.message.MessageFactory;
import com.allcam.gbgw.protocol.sip.core.provider.SipProvider;
import com.allcam.gbgw.protocol.sip.core.transaction.TransactionClient;
import com.allcam.gbgw.protocol.sip.core.transaction.TransactionClientListener;
import lombok.extern.slf4j.Slf4j;

/**
 * KeepAliveSip thread, for keeping the connection up toward a target SIP node
 * (e.g. toward the serving proxy/gw or a remote UA). It periodically sends
 * keep-alive tokens in order to refresh TCP connection timeouts and/or NAT
 * TCP/UDP session timeouts.
 */
@Slf4j
public class KeepAliveSip implements TransactionClientListener
{
    /**
     * SipProvider
     */
    private SipProvider sipProvider;

    private String targetUrl;

    private String username;

    private TransactionClient transactionClient;

    private int failTimes;

    private long sn = 1;

    private KeepaliveFailListener keepaliveFailListener;

    public KeepAliveSip(SipProvider sipProvider, String targetUrl, String username)
    {
        this.sipProvider = sipProvider;
        this.targetUrl = targetUrl;
        this.username = username;
    }

    public void setKeepaliveFailListener(KeepaliveFailListener keepaliveFailListener)
    {
        this.keepaliveFailListener = keepaliveFailListener;
    }

    /**
     * Sends the keep-alive packet now.
     */
    public void sendToken()
    {
        if (targetUrl == null)
        {
            log.warn("targetTo url not set, may cause by not received and message from server.");
            return;
        }

        NameAddress targetTo = new NameAddress(targetUrl);

        KeepaliveMessage keepaliveMessage = new KeepaliveMessage();
        keepaliveMessage.setSn(String.valueOf((sn++)));
        keepaliveMessage.setDeviceId(username);
        keepaliveMessage.setStatus("OK");
        String body = XMLPaser.toXml(keepaliveMessage);

        SipURL self = new SipURL(username, IpAddress.localIpAddress);
        NameAddress from = new NameAddress(self);

        Message kaMsg =
            MessageFactory.createMessageRequest(sipProvider, targetTo, from, null, XMLUtil.XML_MANSCDP_TYPE, body);

        if (null != transactionClient)
        {
            transactionClient.terminate();
        }
        transactionClient = new TransactionClient(sipProvider, kaMsg, this, 5000);
        transactionClient.request();
    }

    @Override
    public void onTransProvisionalResponse(TransactionClient tc, Message resp)
    {

    }

    @Override
    public void onTransSuccessResponse(TransactionClient tc, Message resp)
    {
        failTimes = 0;
    }

    @Override
    public void onTransFailureResponse(TransactionClient tc, Message resp)
    {
        failTimes++;
        checkKeepaliveFail();
    }

    @Override
    public void onTransTimeout(TransactionClient tc)
    {
        failTimes++;
        checkKeepaliveFail();
    }

    private void checkKeepaliveFail()
    {
        if (failTimes >= 3)
        {
            if (null != keepaliveFailListener)
            {
                keepaliveFailListener.onKeepaliveFail();
            }
        }
    }

    /**
     * Gets a String representation of the Object
     */
    @Override
    public String toString()
    {
        String str = null;
        if (sipProvider != null)
        {
            str = "sip:" + sipProvider.getViaAddress() + ":" + sipProvider.getPort();
        }
        return str;
    }

    public interface KeepaliveFailListener
    {
        void onKeepaliveFail();
    }
}