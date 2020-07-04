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
 * Nitin Khanna, Hughes Systique Corp. (Reason: Android specific change, optmization, bug fix)
 */

package sip.core.message;

import com.allcam.gbgw.protocol.sip.core.address.NameAddress;
import com.allcam.gbgw.protocol.sip.core.address.SipURL;
import com.allcam.gbgw.protocol.sip.core.dialog.Dialog;
import com.allcam.gbgw.protocol.sip.core.header.*;
import com.allcam.gbgw.protocol.sip.core.provider.SipProvider;
import com.allcam.gbgw.protocol.sip.core.provider.SipStack;
import org.apache.commons.lang3.StringUtils;

import java.util.Enumeration;
import java.util.Vector;

/**
 * BaseMessageFactory is used to create SIP messages, requests and responses by
 * means of two static methods: createRequest(), createResponse(). <BR>
 * A valid SIP request sent by a UAC MUST, at least, contain the following
 * header fields: To, From, CSeq, Call-ID, Max-Forwards, and Via; all of these
 * header fields are mandatory in all SIP requests. These sip header fields are
 * the fundamental building blocks of a SIP message, as they jointly provide for
 * most of the critical message routing services including the addressing of
 * messages, the routing of responses, limiting message propagation, ordering of
 * messages, and the unique identification of transactions. These header fields
 * are in addition to the mandatory request line, which contains the method,
 * Request-URI, and SIP version.
 */
public abstract class BaseMessageFactory
{

    /**
     * Creates a SIP request message.
     *
     * @param method     method name
     * @param requestUri request-uri
     * @param to         ToHeader NameAddress
     * @param from       FromHeader NameAddress
     * @param contact    Contact NameAddress (if null, no ContactHeader is added)
     * @param viaAddr    Via address
     * @param hostPort   Via port number
     * @param callId     Call-ID value
     * @param cseq       CSeq value
     * @param localTag   tag in FromHeader
     * @param remoteTag  tag in ToHeader (if null, no tag is added)
     * @param branch     branch value (if null, a random value is picked)
     * @param body       body (if null, no body is added)
     * @param qvalue     Q value (if ICSI is null, no Q value is added)
     * @param icsi       ICSI (if null, no ICSI is added)
     */
    public static Message createRequest(String method, SipURL requestUri, NameAddress to, NameAddress from,
                                        NameAddress contact, String proto, String viaAddr, int hostPort, boolean rport, String callId, long cseq,
                                        String localTag, String remoteTag, String branch, String body, String qvalue, String icsi)
    {
        Message req = new Message();
        // mandatory headers first (To, From, Via, Max-Forwards, Call-ID, CSeq):
        req.setRequestLine(new RequestLine(method, requestUri));
        ViaHeader via = new ViaHeader(proto, viaAddr, hostPort);
        if (rport)
        {
            via.setRport();
        }
        if (branch == null)
        {
            branch = SipProvider.pickBranch();
        }
        via.setBranch(branch);
        req.addViaHeader(via);
        req.setMaxForwardsHeader(new MaxForwardsHeader(70));
        if (remoteTag == null)
        {
            req.setToHeader(new ToHeader(to));
        }
        else
        {
            req.setToHeader(new ToHeader(to, remoteTag));
        }
        req.setFromHeader(new FromHeader(from, localTag));
        req.setCallIdHeader(new CallIdHeader(callId));
        req.setCSeqHeader(new CSeqHeader(cseq, method));
        // optional headers:
        // start modification by mandrajg
        if (contact != null)
        {
            if (((SipMethods.REGISTER.equals(method)) || (SipMethods.INVITE.equals(method))) && (icsi != null))
            {
                MultipleHeader contacts = new MultipleHeader(SipHeaders.Contact);
                contacts.addBottom(new ContactHeader(contact, qvalue, icsi));
                req.setContacts(contacts);
            }
            else
            {
                MultipleHeader contacts = new MultipleHeader(SipHeaders.Contact);
                contacts.addBottom(new ContactHeader(contact));
                req.setContacts(contacts);
            }
            // System.out.println("DEBUG: Contact: "+contact.toString());
        }
        if ((SipMethods.INVITE.equals(method)) && (icsi != null))
        {
            req.setAcceptContactHeader(new AcceptContactHeader(icsi));
        }

        req.setExpiresHeader(new ExpiresHeader(String.valueOf(SipStack.default_expires)));
        // add User-Agent header field
        if (SipStack.ua_info != null)
        {
            req.setUserAgentHeader(new UserAgentHeader(SipStack.ua_info));
        }
        // if (body!=null) req.setBody(body); else req.setBody("");
        req.setBody(body);
        return req;
    }

    /**
     * Creates a SIP request message. Where
     * <UL>
     * <LI> via address and port are taken from SipProvider
     * <LI> transport protocol is taken from request-uri (if transport parameter
     * is present) or the default transport for the SipProvider is used.
     * </UL>
     *
     * @param sipProvider the SipProvider used to fill the Via field
     */
    public static Message createRequest(SipProvider sipProvider, String method, SipURL requestUri, NameAddress to,
                                        NameAddress from, NameAddress contact, String callId, long cseq, String localTag, String remoteTag,
                                        String branch, String body, String icsi)
    {    // modified by mandrajg
        String viaAddr = sipProvider.getViaAddress();
        int hostPort = sipProvider.getPort();
        boolean rport = sipProvider.isRportSet();
        String proto;
        if (requestUri.hasTransport())
        {
            proto = requestUri.getTransport();
        }
        else
        {
            proto = sipProvider.getDefaultTransport();
        }

        return createRequest(method,
            requestUri,
            to,
            from,
            contact,
            proto,
            viaAddr,
            hostPort,
            rport,
            callId,
            cseq,
            localTag,
            remoteTag,
            branch,
            body,
            null,
            icsi);
    }

    /**
     * Creates a SIP request message. Where
     * <UL>
     * <LI> request-uri equals the To sip url
     * <LI> via address and port are taken from SipProvider
     * <LI> transport protocol is taken from request-uri (if transport parameter
     * is present) or the default transport for the SipProvider is used.
     * <LI> call_id is picked random
     * <LI> cseq is picked random
     * <LI> local_tag is picked random
     * <LI> branch is picked random
     * </UL>
     */
    public static Message createRequest(SipProvider sipProvider, String method, SipURL requestUri, NameAddress to,
                                        NameAddress from, NameAddress contact, String body)
    { // SipURL
        // requestUri=to.getAddress();
        String callId = sipProvider.pickCallId();
        int cseq = SipProvider.pickInitialCSeq();
        String localTag = SipProvider.pickTag();
        // String branch=SipStack.pickBranch();
        return createRequest(sipProvider,
            method,
            requestUri,
            to,
            from,
            contact,
            callId,
            cseq,
            localTag,
            null,
            null,
            body,
            null);
    }

    /**
     * Creates a SIP request message. Where
     * <UL>
     * <LI> request-uri equals the To sip url
     * <LI> via address and port are taken from SipProvider
     * <LI> transport protocol is taken from request-uri (if transport parameter
     * is present) or the default transport for the SipProvider is used.
     * <LI> contact is formed by the 'From' user-name and by the address and
     * port taken from SipProvider
     * <LI> call_id is picked random
     * <LI> cseq is picked random
     * <LI> local_tag is picked random
     * <LI> branch is picked random
     * </UL>
     */
    public static Message createRequest(SipProvider sipProvider, String method, NameAddress to, NameAddress from,
                                        String body)
    {
        String contactUser = from.getAddress().getUserName();
        NameAddress contact =
            new NameAddress(new SipURL(contactUser, sipProvider.getViaAddress(), sipProvider.getPort()));
        return createRequest(sipProvider, method, to.getAddress(), to, from, contact, body);
    }

    /**
     * Creates a SIP request message within a dialog, with a new branch via-parameter.
     *
     * @param dialog the Dialog used to compose the various Message headers
     * @param method the request method
     */
    public static Message createRequest(Dialog dialog, String method)
    {
        return createRequest(dialog, method, null);
    }

    /**
     * Creates a SIP request message within a dialog, with a new branch via-parameter.
     *
     * @param dialog the Dialog used to compose the various Message headers
     * @param method the request method
     * @param body   the message SDP body
     */
    public static Message createRequest(Dialog dialog, String method, String body)
    {
        NameAddress to = dialog.getRemoteName();
        NameAddress from = dialog.getLocalName();
        NameAddress target = dialog.getRemoteContact();
        if (target == null)
        {
            target = to;
        }
        SipURL requestUri = target.getAddress();
        if (requestUri == null)
        {
            requestUri = dialog.getRemoteName().getAddress();
        }
        SipProvider sipProvider = dialog.getSipProvider();
        String viaAddr = sipProvider.getViaAddress();
        int hostPort = sipProvider.getPort();
        boolean rport = sipProvider.isRportSet();
        String proto;
        if (target.getAddress().hasTransport())
        {
            proto = target.getAddress().getTransport();
        }
        else
        {
            proto = sipProvider.getDefaultTransport();
        }
        NameAddress contact = dialog.getLocalContact();
        if (contact == null)
        {
            contact = from;
        }
        // increment the CSeq, if method is not ACK nor CANCEL
        if (!SipMethods.isAck(method) && !SipMethods.isCancel(method))
        {
            dialog.incLocalCSeq();
        }
        String callId = dialog.getCallID();
        long cseq = dialog.getLocalCSeq();
        String localTag = dialog.getLocalTag();
        String remoteTag = dialog.getRemoteTag();
        // String branch=SipStack.pickBranch();
        Message req = createRequest(method,
            requestUri,
            to,
            from,
            contact,
            proto,
            viaAddr,
            hostPort,
            rport,
            callId,
            cseq,
            localTag,
            remoteTag,
            null,
            body,
            null,
            null);
        Vector<NameAddress> route = dialog.getRoute();
        if (route != null && route.size() > 0)
        {
            Vector<String> routeS = new Vector<>(route.size());
            for (Enumeration<NameAddress> e = route.elements(); e.hasMoreElements(); )
            {
                NameAddress elem = e.nextElement();
                routeS.add(elem.toString());
            }
            req.addRoutes(new MultipleHeader(SipHeaders.Route, routeS));
        }
        req.rfc2543RouteAdapt();
        return req;
    }

    /**
     * Creates a new INVITE request out of any pre-existing dialogs.
     */
    public static Message createInviteRequest(SipProvider sipProvider, SipURL requestUri, NameAddress to,
                                              NameAddress from, NameAddress contact, String body, String icsi)
    {
        String callId = sipProvider.pickCallId();
        int cseq = SipProvider.pickInitialCSeq();
        String localTag = SipProvider.pickTag();
        // String branch=SipStack.pickBranch();
        if (contact == null)
        {
            contact = from;
        }
        return createRequest(sipProvider,
            SipMethods.INVITE,
            requestUri,
            to,
            from,
            contact,
            callId,
            cseq,
            localTag,
            null,
            null,
            body,
            icsi);
    }

    /**
     * Creates a new INVITE request within a dialog (re-invite).
     *
     * @see #createRequest(Dialog, String, String)
     */
    public static Message createInviteRequest(Dialog dialog, String body)
    {
        return createRequest(dialog, SipMethods.INVITE, body);
    }

    /**
     * Creates an ACK request for a 2xx response.
     *
     * @see #createRequest(Dialog, String, String)
     */
    public static Message create2xxAckRequest(Dialog dialog, String body)
    {
        return createRequest(dialog, SipMethods.ACK, body);
    }

    /**
     * Creates an ACK request for a non-2xx response
     */
    public static Message createNon2xxAckRequest(SipProvider sipProvider, Message method, Message resp)
    {
        SipURL requestUri = method.getRequestLine().getAddress();
        FromHeader from = method.getFromHeader();
        ToHeader to = resp.getToHeader();
        String viaAddr = sipProvider.getViaAddress();
        int hostPort = sipProvider.getPort();
        boolean rport = sipProvider.isRportSet();
        String proto;
        if (requestUri.hasTransport())
        {
            proto = requestUri.getTransport();
        }
        else
        {
            proto = sipProvider.getDefaultTransport();
        }
        String branch = method.getViaHeader().getBranch();
        NameAddress contact = null;
        Message ack = createRequest(SipMethods.ACK,
            requestUri,
            to.getNameAddress(),
            from.getNameAddress(),
            contact,
            proto,
            viaAddr,
            hostPort,
            rport,
            method.getCallIdHeader().getCallId(),
            method.getCSeqHeader().getSequenceNumber(),
            from.getParameter("tag"),
            to.getParameter("tag"),
            branch,
            null,
            null,
            null);
        ack.removeExpiresHeader();
        if (method.hasRouteHeader())
        {
            ack.setRoutes(method.getRoutes());
        }
        return ack;
    }

    /**
     * Creates a request MESSAGE in dialog.
     */
    public static Message createMessageRequest(Message method, String body)
    {
        ToHeader to = method.getToHeader();
        FromHeader from = method.getFromHeader();
        SipURL requestUri = method.getRequestLine().getAddress();
        NameAddress contact = method.getContactHeader().getNameAddress();
        ViaHeader via = method.getViaHeader();
        String hostAddr = via.getHost();
        int hostPort = via.getPort();
        boolean rport = via.hasRport();
        String proto = via.getProtocol();
        String branch = method.getViaHeader().getBranch();
        return createRequest(SipMethods.MESSAGE,
            requestUri,
            to.getNameAddress(),
            from.getNameAddress(),
            contact,
            proto,
            hostAddr,
            hostPort,
            rport,
            method.getCallIdHeader().getCallId(),
            method.getCSeqHeader().getSequenceNumber() + 1,
            from.getParameter("tag"),
            to.getParameter("tag"),
            branch,
            body,
            null,
            null);
    }

    /**
     * Creates an ACK request for a 2xx-response. Contact value is taken from
     * SipStack
     */
    /*
     * public static Message create2xxAckRequest(Message resp, String body) {
     * ToHeader to=resp.getToHeader(); FromHeader from=resp.getFromHeader(); int
     * code=resp.getStatusLine().getCode(); SipURL request_uri;
     * request_uri=resp.getContactHeader().getNameAddress().getAddress(); if
     * (request_uri==null) request_uri=to.getNameAddress().getAddress(); String
     * branch=SipStack.pickBranch(); NameAddress contact=null; if
     * (SipStack.contactUrl!=null) contact=new
     * NameAddress(SipStack.contactUrl); return
     * createRequest(SipMethods.ACK,request_uri,to.getNameAddress(),from.getNameAddress(),contact,resp.getCallIdHeader().getCallId(),resp.getCSeqHeader().getSequenceNumber(),from.getParameter("tag"),to.getParameter("tag"),branch,body); }
     */

    /** Creates an ACK request for a 2xx-response within a dialog */
    /*
     * public static Message create2xxAckRequest(Dialog dialog, NameAddress
     * contact, String body) { return
     * createRequest(SipMethods.ACK,dialog,contact,body); }
     */

    /** Creates an ACK request for a 2xx-response within a dialog */
    /*
     * public static Message create2xxAckRequest(Dialog dialog, String body) {
     * return createRequest(SipMethods.ACK,dialog,body); }
     */

    /**
     * Creates a CANCEL request.
     */
    public static Message createCancelRequest(Message method, Dialog dialog)
    {
        ToHeader to = method.getToHeader();
        FromHeader from = method.getFromHeader();
        SipURL requestUri = method.getRequestLine().getAddress();
        NameAddress contact = method.getContactHeader().getNameAddress();
        ViaHeader via = method.getViaHeader();
        String hostAddr = via.getHost();
        int hostPort = via.getPort();
        boolean rport = via.hasRport();
        String proto = via.getProtocol();
        String branch = method.getViaHeader().getBranch();

        String localTag = dialog.getLocalTag();
        if (StringUtils.isBlank(localTag))
        {
            localTag = from.getParameter("tag");
        }
        String remoteTag = dialog.getRemoteTag();
        if (StringUtils.isBlank(remoteTag))
        {
            remoteTag = to.getParameter("tag");
        }

        return createRequest(SipMethods.CANCEL,
            requestUri,
            to.getNameAddress(),
            from.getNameAddress(),
            contact,
            proto,
            hostAddr,
            hostPort,
            rport,
            method.getCallIdHeader().getCallId(),
            method.getCSeqHeader().getSequenceNumber(),
            localTag,
            remoteTag,
            branch,
            "",
            null,
            null);
    }

    /**
     * Creates a BYE request.
     */
    public static Message createByeRequest(Dialog dialog)
    {
        Message msg = createRequest(dialog, SipMethods.BYE, null);
        msg.removeExpiresHeader();
        msg.removeContacts();
        return msg;
    }

    /**
     * Creates a new REGISTER request.
     * <p>
     * If contact is null, set contact as star * (register all)
     */
    public static Message createRegisterRequest(SipProvider sipProvider, NameAddress to, NameAddress from,
                                                NameAddress contact, String qvalue, String icsi)
    {
//        SipURL fromUrl = from.getAddress();
        SipURL toUrl = to.getAddress();
        SipURL registrar = new SipURL(toUrl.getUserName(), toUrl.getHost(), toUrl.getPort());
        String viaAddr = sipProvider.getViaAddress();
        int hostPort = sipProvider.getPort();
        boolean rport = sipProvider.isRportSet();
        String proto;
        if (toUrl.hasTransport())
        {
            proto = toUrl.getTransport();
        }
        else
        {
            proto = sipProvider.getDefaultTransport();
        }
        String callId = sipProvider.pickCallId();
        int cseq = SipProvider.pickInitialCSeq();
        String localTag = SipProvider.pickTag();
        // String branch=SipStack.pickBranch();
        Message req = createRequest(SipMethods.REGISTER,
            registrar,
            to,
            from,
            contact,
            proto,
            viaAddr,
            hostPort,
            rport,
            callId,
            cseq,
            localTag,
            null,
            null,
            null,
            qvalue,
            icsi);                        // modified by mandrajg
        // if no contact, deregister all
        if (contact == null)
        {
            ContactHeader star = new ContactHeader(); // contact is *
            req.setContactHeader(star);
            req.setExpiresHeader(new ExpiresHeader(String.valueOf(SipStack.default_expires)));
        }
        return req;
    }

    // ################ Can be removed? ################
    /**
     * Creates a new REGISTER request.
     * <p>
     * If contact is null, set contact as star * (register all)
     */
    /*
     * public static Message createRegisterRequest(SipProvider sipProvider,
     * NameAddress to, NameAddress contact) { return
     * createRegisterRequest(sipProvider,to,to,contact); }
     */

    /**
     * Creates a SIP response message.
     *
     * @param req      the request message
     * @param code     the response code
     * @param reason   the response reason
     * @param contact  the contact address
     * @param localTag the local tag in the 'To' header
     * @param body     the message body
     */
    public static Message createResponse(Message req, int code, String reason, String localTag, NameAddress contact,
                                         String contentType, String body)
    {
        Message resp = new Message();
        resp.setStatusLine(new StatusLine(code, reason));
        resp.setVias(req.getVias());
        if (code >= 180 && code < 300 && req.hasRecordRouteHeader())
        {
            resp.setRecordRoutes(req.getRecordRoutes());
        }
        ToHeader toh = req.getToHeader();
        if (localTag != null)
        {
            toh.setParameter("tag", localTag);
        }
        resp.setToHeader(toh);
        resp.setFromHeader(req.getFromHeader());
        resp.setCallIdHeader(req.getCallIdHeader());
        resp.setCSeqHeader(req.getCSeqHeader());
        if (contact != null)
        {
            resp.setContactHeader(new ContactHeader(contact));
        }
        // add Server header field
        if (SipStack.server_info != null)
        {
            resp.setServerHeader(new ServerHeader(SipStack.server_info));
        }
        // if (body!=null) resp.setBody(body); else resp.setBody("");
        if (contentType == null)
        {
            resp.setBody(body);
        }
        else
        {
            resp.setBody(contentType, body);
        }
        return resp;
    }

    /**
     * Creates a SIP response message. For 2xx responses generates the local tag
     * by means of the SipStack.pickTag(req) method.
     */
    public static Message createResponse(Message req, int code)
    {
        return createResponse(req, code, null);
    }

    /**
     * Creates a SIP response message. For 2xx responses generates the local tag
     * by means of the SipStack.pickTag(req) method.
     */
    public static Message createResponse(Message req, int code, NameAddress contact)
    {
        return createResponse(req, code, SipResponses.reasonOf(code), contact);
    }

    /**
     * Creates a SIP response message. For 2xx responses generates the local tag
     * by means of the SipStack.pickTag(req) method.
     */
    public static Message createResponse(Message req, int code, String reason, NameAddress contact)
    {
        String localtag = null;
        if (req.createsDialog() && !req.getToHeader().hasTag())
        {
            //fix issue 425 - also add tag to 18x responses
            if (SipStack.early_dialog || (code >= 101 && code < 300))
            {
                localtag = SipProvider.pickTag(req);
            }
        }
        return createResponse(req, code, reason, localtag, contact, null, null);
    }

}
