package sip.core.dialog;

import com.allcam.gbgw.protocol.gb28181.message.XMLUtil;
import com.allcam.gbgw.protocol.sip.core.address.NameAddress;
import com.allcam.gbgw.protocol.sip.core.address.SipURL;
import com.allcam.gbgw.protocol.sip.core.header.ContactHeader;
import com.allcam.gbgw.protocol.sip.core.header.StatusLine;
import com.allcam.gbgw.protocol.sip.core.header.SubjectHeader;
import com.allcam.gbgw.protocol.sip.core.message.Message;
import com.allcam.gbgw.protocol.sip.core.message.MessageFactory;
import com.allcam.gbgw.protocol.sip.core.message.SipMethods;
import com.allcam.gbgw.protocol.sip.core.message.SipResponses;
import com.allcam.gbgw.protocol.sip.core.provider.ConnectionIdentifier;
import com.allcam.gbgw.protocol.sip.core.provider.SipProvider;
import com.allcam.gbgw.protocol.sip.core.provider.SipProviderListener;
import com.allcam.gbgw.protocol.sip.core.transaction.*;
import com.allcam.gbgw.protocol.tools.Log;
import com.allcam.gbgw.protocol.tools.LogLevel;
import lombok.extern.slf4j.Slf4j;

/**
 * Class InviteDialog can be used to manage invite dialogs. An InviteDialog can
 * be both client or server. (i.e. generating an INVITE request or responding to
 * an incoming INVITE request).
 * <p>
 * An InviteDialog can be in state inviting/waiting/invited, accepted/refused,
 * call, byed/byeing, and close.
 * <p>
 * InviteDialog supports the offer/answer model for the sip body, with the
 * following rules: <br> - both INVITE-offer/2xx-answer and 2xx-offer/ACK-answer
 * modes for incoming calls <br> - INVITE-offer/2xx-answer mode for outgoing
 * calls.
 */
@Slf4j
public class InviteDialog extends Dialog
    implements TransactionClientListener, InviteTransactionServerListener, AckTransactionServerListener,
    SipProviderListener
{
    /**
     * The last invite message
     */
    Message inviteReq;

    /**
     * The last ack message
     */
    private Message ackReq;

    /**
     * The InviteTransactionServer.
     */
    private InviteTransactionServer inviteTs;

    /**
     * The AckTransactionServer.
     */
    private AckTransactionServer ackTs;

    /**
     * The BYE TransactionServer.
     */
    private TransactionServer byeTs;

    /**
     * The InviteDialog listener
     */
    private InviteDialogListener listener;

    /**
     * Whether offer/answer are in INVITE/200_OK
     */
    private boolean inviteOffer;

    private static final int D_INIT = 0;

    private static final int D_WAITING = 1;

    private static final int D_INVITING = 2;

    private static final int D_INVITED = 3;

    private static final int D_REFUSED = 4;

    private static final int D_ACCEPTED = 5;

    private static final int D_CALL = 6;

    private static final int D_ReWAITING = 11;

    private static final int D_ReINVITING = 12;

    private static final int D_ReINVITED = 13;

    private static final int D_ReREFUSED = 14;

    private static final int D_ReACCEPTED = 15;

    private static final int D_BYEING = 7;

    private static final int D_BYED = 8;

    private static final int D_CLOSE = 9;

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
            case D_INVITING:
                return "D_INVITING";
            case D_INVITED:
                return "D_INVITED";
            case D_REFUSED:
                return "D_REFUSED";
            case D_ACCEPTED:
                return "D_ACCEPTED";
            case D_CALL:
                return "D_CALL";
            case D_ReWAITING:
                return "D_ReWAITING";
            case D_ReINVITING:
                return "D_ReINVITING";
            case D_ReINVITED:
                return "D_ReINVITED";
            case D_ReREFUSED:
                return "D_ReREFUSED";
            case D_ReACCEPTED:
                return "D_ReACCEPTED";
            case D_BYEING:
                return "D_BYEING";
            case D_BYED:
                return "D_BYED";
            case D_CLOSE:
                return "D_CLOSE";
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
        return status < D_ACCEPTED;
    }

    /**
     * Whether the dialog is in "confirmed" state.
     */
    @Override
    public boolean isConfirmed()
    {
        return status >= D_ACCEPTED && status < D_CLOSE;
    }

    /**
     * Whether the dialog is in "terminated" state.
     */
    @Override
    public boolean isTerminated()
    {
        return status == D_CLOSE;
    }

    /**
     * Whether the session is "active".
     */
    public boolean isSessionActive()
    {
        return (status == D_CALL);
    }

    /**
     * Gets the invite message
     */
    public Message getInviteMessage()
    {
        return inviteReq;
    }

    /**
     * Creates a new InviteDialog.
     */
    public InviteDialog(SipProvider sipProvider, InviteDialogListener listener)
    {
        super(sipProvider);
        init(listener);
    }

    /**
     * Creates a new InviteDialog.
     */
    public InviteDialog(InviteDialog preDialog, InviteDialogListener listener)
    {
        super(preDialog);
        init(listener);
    }

    /**
     * Creates a new InviteDialog.
     */
    public InviteDialog(SipProvider sipProvider, String fromUsername, InviteDialogListener listener)
    {
        super(sipProvider, fromUsername);
        init(listener);
    }

    /**
     * Creates a new InviteDialog for the already received INVITE request
     * <i>invite</i>.
     */
    public InviteDialog(SipProvider sipProvider, Message invite, InviteDialogListener listener)
    {
        super(sipProvider);
        init(listener);

        changeStatus(D_INVITED);
        inviteReq = invite;
        inviteTs = new InviteTransactionServer(sipProvider, inviteReq, this);
        update(Dialog.UAS, inviteReq);
    }

    /**
     * Inits the InviteDialog.
     */
    private void init(InviteDialogListener listener)
    {
        this.listener = listener;
        this.inviteReq = null;
        this.ackReq = null;
        this.inviteOffer = true;
        changeStatus(D_INIT);
    }

    /**
     * Starts a new InviteTransactionServer.
     */
    public void listen()
    {
        if (!statusIs(D_INIT))
        {
            return;
        }
        // else
        changeStatus(D_WAITING);
        inviteTs = new InviteTransactionServer(sipProvider, getFromUsername(), this);
        inviteTs.listen();
    }

    /**
     * Starts a new InviteTransactionClient and initializes the dialog state
     * information.
     *
     * @param callee            the callee url (and display name)
     * @param caller            the caller url (and display name)
     * @param contact           the contact url OR the contact username
     * @param sessionDescriptor SDP body
     * @param icsi              the ICSI for this session
     */
    public void invite(String callee, String caller, String contact, String sessionDescriptor, String icsi)
    {
        printLog("inside invite(callee,caller,contact,sdp)", LogLevel.MEDIUM);
        if (!statusIs(D_INIT))
        {
            return;
        }
        // else
        NameAddress toUrl = new NameAddress(callee);
        NameAddress fromUrl = new NameAddress(caller);
        SipURL requestUri = toUrl.getAddress();
        NameAddress contactUrl = sipProvider.buildContactUrl(contact, fromUrl);

        Message invite = MessageFactory.createInviteRequest(sipProvider,
            requestUri,
            toUrl,
            fromUrl,
            contactUrl,
            sessionDescriptor,
            icsi);
        invite(invite);
    }

    public void invite(SipURL callee, SipURL caller, String contact, String sdp, SubjectHeader subjectHeader)
    {
        printLog("inside invite(callee,caller,contact,sdp)", LogLevel.MEDIUM);
        if (!statusIs(D_INIT))
        {
            return;
        }

        NameAddress toUrl = new NameAddress(callee);
        NameAddress fromUrl = new NameAddress(caller);
        NameAddress contactUrl = sipProvider.buildContactUrl(contact, fromUrl);

        Message invite = MessageFactory.createInviteRequest(sipProvider, callee, toUrl, fromUrl, contactUrl, sdp, null);

        if (null != subjectHeader)
        {
            invite.setSubjectHeader(subjectHeader);
        }
        invite(invite);
    }

    /**
     * Starts a new InviteTransactionClient and initializes the dialog state
     * information
     *
     * @param invite the INVITE message
     */
    public void invite(Message invite)
    {
        printLog("inside invite(invite)", LogLevel.MEDIUM);
        if (!statusIs(D_INIT))
        {
            return;
        }
        // else
        changeStatus(D_INVITING);
        inviteReq = invite;
        update(Dialog.UAC, inviteReq);
        InviteTransactionClient inviteTc = new InviteTransactionClient(sipProvider, inviteReq, this);
        inviteTc.request();
    }

    /**
     * Starts a new InviteTransactionClient with offer/answer in 2xx/ack and
     * initializes the dialog state information
     */
    public void inviteWithoutOffer(String callee, String caller, String contact)
    {
        withoutOffer();
        invite(callee, caller, contact, null, null);
    }

    /**
     * Starts a new InviteTransactionClient with offer/answer in 2xx/ack and
     * initializes the dialog state information
     */
    public void inviteWithoutOffer(Message invite)
    {
        withoutOffer();
        invite(invite);
    }

    public void withoutOffer()
    {
        inviteOffer = false;
    }

    /**
     * Re-invites the remote user.
     * <p>
     * Starts a new InviteTransactionClient and changes the dialog state
     * information
     * <p>
     * Parameters: <br>- contact : the contact url OR the contact username; if
     * null, the previous contact is used <br>- sessionDescriptor : the
     * message body
     */
    public void reInvite(String contact, String sessionDescriptor)
    {
        printLog("inside reInvite(contact,sdp)", LogLevel.MEDIUM);
        if (!statusIs(D_CALL))
        {
            return;
        }
        // else
        Message invite = MessageFactory.createInviteRequest(this, sessionDescriptor);
        if (contact != null)
        {
            NameAddress contactUrl;
            if (contact.contains("sip:"))
            {
                contactUrl = new NameAddress(contact);
            }
            else
            {
                contactUrl = new NameAddress(new SipURL(contact, sipProvider.getViaAddress(), sipProvider.getPort()));
            }
            invite.setContactHeader(new ContactHeader(contactUrl));
        }
        reInvite(invite);
    }

    /**
     * Re-invites the remote user.
     * <p>
     * Starts a new InviteTransactionClient and changes the dialog state
     * information
     */
    public void reInvite(Message invite)
    {
        printLog("inside reInvite(invite)", LogLevel.MEDIUM);
        if (!statusIs(D_CALL))
        {
            return;
        }
        // else
        changeStatus(D_ReINVITING);
        inviteReq = invite;
        update(Dialog.UAC, inviteReq);
        InviteTransactionClient invite_tc = new InviteTransactionClient(sipProvider, inviteReq, this);
        invite_tc.request();
    }

    /**
     * Re-invites the remote user with offer/answer in 2xx/ack
     * <p>
     * Starts a new InviteTransactionClient and changes the dialog state
     * information
     */
    public void reInviteWithoutOffer(Message invite)
    {
        inviteOffer = false;
        reInvite(invite);
    }

    /**
     * Re-invites the remote user with offer/answer in 2xx/ack
     * <p>
     * Starts a new InviteTransactionClient and changes the dialog state
     * information
     */
    public void reInviteWithoutOffer(String contact, String sessionDescriptor)
    {
        inviteOffer = false;
        reInvite(contact, sessionDescriptor);
    }

    /**
     * Sends the empty body ack when offer/answer is in 2xx/ack
     */
    public void ackWithAnswer()
    {
        if (null == ackReq)
        {
            ackWithAnswer(null, null);
        }
        else
        {
            ackWithAnswer(ackReq);
        }
    }

    /**
     * Sends the ack when offer/answer is in 2xx/ack
     */
    public void ackWithAnswer(String contact, String sessionDescriptor)
    {
        if (contact != null)
        {
            setLocalContact(new NameAddress(contact));
        }
        Message ack = MessageFactory.create2xxAckRequest(this, sessionDescriptor);
        ackWithAnswer(ack);
    }

    /**
     * Sends the ack when offer/answer is in 2xx/ack
     */
    public void ackWithAnswer(Message ack)
    {
        ackReq = ack;
        // reset the offer/answer flag to the default value
        inviteOffer = true;
        AckTransactionClient ackTc = new AckTransactionClient(sipProvider, ack);
        ackTc.request();
        changeStatus(D_CALL);
    }

    /**
     * Responds with <i>resp</i>. This method can be called when the
     * InviteDialog is in D_INVITED or D_BYED states.
     * <p>
     * If the CSeq method is INVITE and the response is 2xx, it moves to state
     * D_ACCEPTED, adds a new listener to the SipProviderListener, and creates
     * new AckTransactionServer
     * <p>
     * If the CSeq method is INVITE and the response is not 2xx, it moves to
     * state D_REFUSED, and sends the response.
     */
    public void respond(Message resp)
    // private void respond(Message resp)
    {
        printLog("inside respond(resp)", LogLevel.MEDIUM);
        String method = resp.getCSeqHeader().getMethod();
        switch (method)
        {
            case SipMethods.INVITE:
                if (!verifyStatus(statusIs(D_INVITED) || statusIs(D_ReINVITED)))
                {
                    printLog("respond(): InviteDialog not in (re)invited state: No response now", LogLevel.HIGH);
                    return;
                }

                int code = resp.getStatusLine().getCode();
                // 1xx provisional responses
                if (code >= 100 && code < 200)
                {
                    inviteTs.respondWith(resp);
                    return;
                }

                // For all final responses establish the dialog
                if (code >= 200)
                {
                    // changeStatus(D_ACCEPTED);
                    update(Dialog.UAS, resp);
                }

                // 2xx success responses
                if (code >= 200 && code < 300)
                {
                    if (statusIs(D_INVITED))
                    {
                        changeStatus(D_ACCEPTED);
                    }
                    else
                    {
                        changeStatus(D_ReACCEPTED);
                    }
                    // terminates the INVITE Transaction server and activates an ACK Transaction server
                    inviteTs.terminate();
                    ConnectionIdentifier connId = inviteTs.getConnectionId();
                    ackTs = new AckTransactionServer(sipProvider, connId, resp, this);
                    ackTs.respond();
                }
                else
                {
                    // 300-699 fail responses
                    if (statusIs(D_INVITED))
                    {
                        changeStatus(D_REFUSED);
                    }
                    else
                    {
                        changeStatus(D_ReREFUSED);
                    }
                    inviteTs.respondWith(resp);
                }
                break;
            case SipMethods.BYE:
                if (!verifyStatus(statusIs(D_BYED)))
                {
                    return;
                }
                byeTs.respondWith(resp);
                break;
            case SipMethods.INFO:
            case SipMethods.MESSAGE:
                sipProvider.sendMessage(resp);
                break;
            default:
                log.warn("response message not recognized.");
                break;
        }
    }

    /**
     * Responds with <i>code</i> and <i>reason</i>. This method can be called
     * when the InviteDialog is in D_INVITED, D_ReINVITED states
     */
    public void respond(int code, String reason, String contact, String sdp)
    {
        printLog("inside respond(" + code + "," + reason + ")", LogLevel.MEDIUM);
        if (statusIs(D_INVITED) || statusIs(D_ReINVITED))
        {
            NameAddress contactAddress = null;
            if (contact != null)
            {
                contactAddress = new NameAddress(contact);
            }
            Message resp = MessageFactory.createResponse(inviteReq, code, reason, contactAddress);
            resp.setBody(sdp);
            respond(resp);
        }
        else
        {
            printWarning(
                "Dialog isn't in \"invited\" state: cannot respond (" + code + "/" + getStatus() + "/" + getDialogId() +
                    ")", LogLevel.MEDIUM);
        }
    }

    /**
     * Signals that the phone is ringing. This method should be called when the
     * InviteDialog is in D_INVITED or D_ReINVITED state
     */
    public void ring(String sdp)
    { // modified
        printLog("inside ring()", LogLevel.MEDIUM);
        respond(180, SipResponses.reasonOf(180), null, sdp);
    }

    /**
     * Accepts the incoming call. This method should be called when the
     * InviteDialog is in D_INVITED or D_ReINVITED state
     */
    public void accept(String contact, String sdp)
    {
        printLog("inside accept(sdp)", LogLevel.MEDIUM);
        respond(200, SipResponses.reasonOf(200), contact, sdp);
    }

    /**
     * Refuses the incoming call. This method should be called when the
     * InviteDialog is in D_INVITED or D_ReINVITED state
     */
    public void refuse(int code)
    {
        refuse(code, SipResponses.reasonOf(code));
    }

    /**
     * Refuses the incoming call. This method should be called when the
     * InviteDialog is in D_INVITED or D_ReINVITED state
     */
    public void refuse(int code, String reason)
    {
        printLog("inside refuse(" + code + "," + reason + ")", LogLevel.MEDIUM);
        respond(code, reason, null, null);
    }

    /**
     * Refuses the incoming call. This method should be called when the
     * InviteDialog is in D_INVITED or D_ReINVITED state
     */
    public void error()
    {
        printLog("inside error()", LogLevel.HIGH);
        refuse(500, SipResponses.reasonOf(500));
    }

    /**
     * Refuses the incoming call. This method should be called when the
     * InviteDialog is in D_INVITED or D_ReINVITED state
     */
    public void refuse()
    {
        printLog("inside refuse()", LogLevel.MEDIUM);
        refuse(403, SipResponses.reasonOf(403));
    }

    public void busy()
    {
        refuse(486, SipResponses.reasonOf(486)); // modified
    }

    /**
     * Termiante the call. This method should be called when the InviteDialog is
     * in D_CALL state
     * <p>
     * Increments the Cseq, moves to state D_BYEING, and creates new BYE
     * TransactionClient
     */
    public void bye()
    {
        printLog("inside bye()", LogLevel.MEDIUM);
        if (statusIs(D_CALL))
        {
            Message bye = MessageFactory.createByeRequest(this);
            bye(bye);
        }
        else
        {
            error();
        }
    }

    /**
     * Termiante the call. This method should be called when the InviteDialog is
     * in D_CALL state
     * <p>
     * Increments the Cseq, moves to state D_BYEING, and creates new BYE
     * TransactionClient
     */
    public void bye(Message bye)
    {
        printLog("inside bye(bye)", LogLevel.MEDIUM);
        if (statusIs(D_CALL))
        {
            changeStatus(D_BYEING);
            // dialog_state.incLocalCSeq(); // done by
            // MessageFactory.createRequest()
            TransactionClient tc = new TransactionClient(sipProvider, bye, this);
            tc.request();
            // listener.onDlgByeing(this);
        }
    }

    /**
     * Cancel the ongoing call request or a call listening. This method should
     * be called when the InviteDialog is in D_INVITING or D_ReINVITING state or
     * in the D_WAITING state
     */
    public void cancel()
    {
        printLog("inside cancel()", LogLevel.MEDIUM);
        if (statusIs(D_INVITING) || statusIs(D_ReINVITING))
        {
            Message cancel = MessageFactory.createCancelRequest(inviteReq, this);
            cancel(cancel);
        }
        else if (statusIs(D_WAITING) || statusIs(D_ReWAITING))
        {
            inviteTs.terminate();
        }
    }

    /**
     * Cancel the ongoing call request or a call listening. This method should
     * be called when the InviteDialog is in D_INVITING or D_ReINVITING state or
     * in the D_WAITING state
     */
    public void cancel(Message cancel)
    {
        printLog("inside cancel(cancel)", LogLevel.MEDIUM);
        if (statusIs(D_INVITING) || statusIs(D_ReINVITING))
        { // changeStatus(D_CANCELING);
            TransactionClient tc = new TransactionClient(sipProvider, cancel, null);
            tc.request();
        }
        else if (statusIs(D_WAITING) || statusIs(D_ReWAITING))
        {
            inviteTs.terminate();
        }
    }

    /**
     * send message
     */
    public void sendMsg(String msgBody)
    {
        Message message = MessageFactory.createRequest(this, SipMethods.MESSAGE);
        message.setBody(XMLUtil.XML_MANSCDP_TYPE, msgBody);
        TransactionClient tc = new TransactionClient(sipProvider, message, null);
        tc.request();
    }

    /**
     * Redirects the incoming call , specifing the <i>code</i> and <i>reason</i>.
     * This method can be called when the InviteDialog is in D_INVITED or
     * D_ReINVITED state
     */
    public void redirect(int code, String reason, String contact)
    {
        printLog("inside redirect(" + code + "," + reason + "," + contact + ")", LogLevel.MEDIUM);
        respond(code, reason, contact, null);
    }

    // ************** Inherited from SipProviderListener **************

    /**
     * Inherited from class SipProviderListener. Called when a new message is
     * received (out of any ongoing transaction) for the current InviteDialog.
     * Always checks for out-of-date methods (CSeq header sequence number).
     * <p>
     * If the message is ACK(2xx/INVITE) request, it moves to D_CALL state, and
     * fires <i>onDlgAck(this,body,msg)</i>.
     * <p>
     * If the message is 2xx(INVITE) response, it create a new
     * AckTransactionClient
     * <p>
     * If the message is BYE, it moves to D_BYED state, removes the listener
     * from SipProvider, fires onDlgBye(this,msg) then it responds with 200 OK,
     * moves to D_CLOSE state and fires onDlgClose(this)
     */
    @Override
    public void onReceivedMessage(SipProvider sipProvider, Message msg)
    {
        printLog("inside onReceivedMessage(sipProvider,message)", LogLevel.MEDIUM);
        if (msg.isRequest() && !(msg.isAck() || msg.isCancel()) &&
            msg.getCSeqHeader().getSequenceNumber() <= getRemoteCSeq())
        {
            printLog("Request message is too late (CSeq too small): Message discarded", LogLevel.HIGH);
            return;
        }

        if (msg.isRequest())
        {
            // invite received
            if (msg.isInvite())
            {
                verifyStatus(statusIs(D_INIT) || statusIs(D_CALL));
                // NOTE: if the inviteTs.listen() is used, you should not arrive here with the D_INIT state..
                // however state D_INIT has been included for robustness against further changes.
                if (statusIs(D_INIT))
                {
                    changeStatus(D_INVITED);
                }
                else
                {
                    changeStatus(D_ReINVITED);
                }
                inviteReq = msg;
                inviteTs = new InviteTransactionServer(sipProvider, inviteReq, this);
                update(Dialog.UAS, inviteReq);
                if (statusIs(D_INVITED))
                {
                    listener.onDlgInvite(this,
                        inviteReq.getToHeader().getNameAddress(),
                        inviteReq.getFromHeader().getNameAddress(),
                        inviteReq.getBody(),
                        inviteReq);
                }
                else
                {
                    listener.onDlgReInvite(this, inviteReq.getBody(), inviteReq);
                }
            }
            else if (msg.isAck())
            {
                if (!verifyStatus(statusIs(D_ACCEPTED) || statusIs(D_ReACCEPTED)))
                {
                    return;
                }
                changeStatus(D_CALL);
                // terminates the AckTransactionServer
                ackTs.terminate();
                listener.onDlgAck(this, msg.getBody(), msg);
                listener.onDlgCall(this);
            }
            else if (msg.isBye())
            {
                // bye received
                if (!verifyStatus(statusIs(D_CALL) || statusIs(D_BYEING)))
                {
                    return;
                }
                changeStatus(D_BYED);
                byeTs = new TransactionServer(sipProvider, msg, this);
                // automatically sends a 200 OK
                Message resp = MessageFactory.createResponse(msg, 200, null);
                respond(resp);
                listener.onDlgBye(this, msg);
                changeStatus(D_CLOSE);
                listener.onDlgClose(this);
            }
            else if (msg.isCancel())
            {
                // cancel received
                if (!verifyStatus(statusIs(D_INVITED) || statusIs(D_ReINVITED)))
                {
                    return;
                }
                // create a CANCEL TransactionServer and send a 200 OK (CANCEL)
                TransactionServer ts = new TransactionServer(sipProvider, msg, null);
                ts.respondWith(MessageFactory.createResponse(msg, 200));
                // automatically sends a 487 Cancelled
                Message resp = MessageFactory.createResponse(inviteReq, 487);
                respond(resp);
                listener.onDlgCancel(this, msg);
            }
            else if (msg.isInfo())
            {
                if (!verifyStatus(statusIs(D_CALL)))
                {
                    return;
                }
                Message resp = MessageFactory.createResponse(msg, 200);
                respond(resp);
                listener.onDlgInfo(this, msg);
            }
            else
            {
                Message resp = MessageFactory.createResponse(msg, 200);
                respond(resp);
                listener.onDlgMessage(this, msg);
            }
        }
        else if (msg.isResponse())
        {
            // keep sending ACK (if already sent) for any "200 OK" received
            if (!verifyStatus(statusIs(D_CALL)))
            {
                return;
            }
            int code = msg.getStatusLine().getCode();
            verifyThat(code >= 200 && code < 300, "code 2xx was expected");
            if (ackReq != null)
            {
                AckTransactionClient ackTc = new AckTransactionClient(sipProvider, ackReq);
                ackTc.request();
            }
        }
        else
        {
            log.warn("message not request or response received in invite dialog.");
        }
    }

    // ************** Inherited from InviteTransactionClientListener **************

    /**
     * Inherited from TransactionClientListener. When the
     * TransactionClientListener is in "Proceeding" state and receives a new 1xx
     * response
     * <p>
     * For INVITE transaction it fires
     * <i>onFailureResponse(this,code,reason,body,msg)</i>.
     */
    @Override
    public void onTransProvisionalResponse(TransactionClient tc, Message msg)
    {
        printLog("inside onTransProvisionalResponse(tc,mdg)", LogLevel.LOW);
        if (tc.getTransactionMethod().equals(SipMethods.INVITE))
        {
            StatusLine statusline = msg.getStatusLine();
            listener.onDlgInviteProvisionalResponse(this,
                statusline.getCode(),
                statusline.getReason(),
                msg.getBody(),
                msg);
        }
    }

    /**
     * Inherited from TransactionClientListener. When the
     * TransactionClientListener goes into the "Completed" state, receiving a
     * failure response
     * <p>
     * If called for a INVITE transaction, it moves to D_CLOSE state, removes
     * the listener from SipProvider.
     * <p>
     * If called for a BYE transaction, it moves to D_CLOSE state, removes the
     * listener from SipProvider, and fires <i>onClose(this,msg)</i>.
     */
    @Override
    public void onTransFailureResponse(TransactionClient tc, Message msg)
    {
        printLog("inside onTransFailureResponse(" + tc.getTransactionId() + ",msg)", LogLevel.LOW);
        if (tc.getTransactionMethod().equals(SipMethods.INVITE))
        {
            if (!verifyStatus(statusIs(D_INVITING) || statusIs(D_ReINVITING)))
            {
                return;
            }
            StatusLine statusline = msg.getStatusLine();
            int code = statusline.getCode();
            verifyThat(code >= 300 && code < 700, "error code was expected");
            if (statusIs(D_ReINVITING))
            {
                changeStatus(D_CALL);
                listener.onDlgReInviteFailureResponse(this, code, statusline.getReason(), msg);
            }
            else
            {
                changeStatus(D_CLOSE);
                if (code < 300 || code >= 400)
                {
                    listener.onDlgInviteFailureResponse(this, code, statusline.getReason(), msg);
                }
                else
                {
                    listener.onDlgInviteRedirectResponse(this, code, statusline.getReason(), msg.getContacts(), msg);
                }
                listener.onDlgClose(this);
            }
        }
        else if (tc.getTransactionMethod().equals(SipMethods.BYE))
        {
            if (!verifyStatus(statusIs(D_BYEING)))
            {
                return;
            }
            StatusLine statusline = msg.getStatusLine();
            int code = statusline.getCode();
            verifyThat(code >= 300 && code < 700, "error code was expected");
            changeStatus(InviteDialog.D_CALL);
            listener.onDlgByeFailureResponse(this, code, statusline.getReason(), msg);
        }
    }

    /**
     * Inherited from TransactionClientListener. When an
     * TransactionClientListener goes into the "Terminated" state, receiving a
     * 2xx response
     * <p>
     * If called for a INVITE transaction, it updates the dialog information,
     * moves to D_CALL state, add a listener to the SipProvider, creates a new
     * AckTransactionClient(ack,this), and fires
     * <i>onSuccessResponse(this,code,body,msg)</i>.
     * <p>
     * If called for a BYE transaction, it moves to D_CLOSE state, removes the
     * listener from SipProvider, and fires <i>onClose(this,msg)</i>.
     */
    @Override
    public void onTransSuccessResponse(TransactionClient tc, Message msg)
    {
        printLog("inside onTransSuccessResponse(tc,msg)", LogLevel.LOW);
        if (tc.getTransactionMethod().equals(SipMethods.INVITE))
        {
            if (!verifyStatus(statusIs(D_INVITING) || statusIs(D_ReINVITING)))
            {
                return;
            }
            StatusLine statusline = msg.getStatusLine();
            int code = statusline.getCode();
            if (!verifyThat(code >= 200 && code < 300 && msg.getTransactionMethod().equals(SipMethods.INVITE),
                "2xx for invite was expected"))
            {
                return;
            }
            boolean reInviting = statusIs(D_ReINVITING);
            update(Dialog.UAC, msg);
            if (inviteOffer)
            {
                // inviteReq=MessageFactory.createRequest(SipMethods.ACK,dialog_state,sdp.toString());
                // ack=MessageFactory.createRequest(this,SipMethods.ACK,null);
                ackReq = MessageFactory.create2xxAckRequest(this, null);
                AckTransactionClient ackTc = new AckTransactionClient(sipProvider, ackReq);
                ackTc.request();
                changeStatus(D_CALL);
            }
            if (!reInviting)
            {
                listener.onDlgInviteSuccessResponse(this, code, statusline.getReason(), msg.getBody(), msg);
                listener.onDlgCall(this);
            }
            else
            {
                listener.onDlgReInviteSuccessResponse(this, code, statusline.getReason(), msg.getBody(), msg);
            }
        }
        else if (tc.getTransactionMethod().equals(SipMethods.BYE))
        {
            if (!verifyStatus(statusIs(D_BYEING)))
            {
                return;
            }
            StatusLine statusline = msg.getStatusLine();
            int code = statusline.getCode();
            verifyThat(code >= 200 && code < 300, "2xx for bye was expected");
            changeStatus(D_CLOSE);
            listener.onDlgByeSuccessResponse(this, code, statusline.getReason(), msg);
            listener.onDlgClose(this);
        }
    }

    /**
     * Inherited from TransactionClientListener. When the TransactionClient goes
     * into the "Terminated" state, caused by transaction timeout
     */
    @Override
    public void onTransTimeout(TransactionClient tc)
    {
        printLog("inside onTransTimeout(tc,msg)", LogLevel.LOW);
        if (tc.getTransactionMethod().equals(SipMethods.INVITE))
        {
            if (!verifyStatus(statusIs(D_INVITING) || statusIs(D_ReINVITING)))
            {
                return;
            }
            cancel(); //modified
            changeStatus(D_CLOSE);
            listener.onDlgTimeout(this);
            listener.onDlgClose(this);
        }
        else if (tc.getTransactionMethod().equals(SipMethods.BYE))
        {
            if (!verifyStatus(statusIs(D_BYEING)))
            {
                return;
            }
            changeStatus(D_CLOSE);
            listener.onDlgClose(this);
        }
    }

    // ************** Inherited from InviteTransactionServerListener
    // **************

    /**
     * Inherited from TransactionServerListener. When the TransactionServer goes
     * into the "Trying" state receiving a request
     * <p>
     * If called for a INVITE transaction, it initializes the dialog
     * information, <br>
     * moves to D_INVITED state, and add a listener to the SipProvider, <br>
     * and fires <i>onInvite(caller,body,msg)</i>.
     */
    @Override
    public void onTransRequest(TransactionServer ts, Message req)
    {
        printLog("inside onTransRequest(ts,msg)", LogLevel.LOW);
        if (ts.getTransactionMethod().equals(SipMethods.INVITE))
        {
            if (!verifyStatus(statusIs(D_WAITING)))
            {
                return;
            }
            changeStatus(D_INVITED);
            inviteReq = req;
            update(Dialog.UAS, inviteReq);
            listener.onDlgInvite(this,
                inviteReq.getToHeader().getNameAddress(),
                inviteReq.getFromHeader().getNameAddress(),
                inviteReq.getBody(),
                inviteReq);
        }
    }

    /**
     * Inherited from InviteTransactionServerListener. When an
     * InviteTransactionServer goes into the "Confirmed" state receining an ACK
     * for NON-2xx response
     * <p>
     * It moves to D_CLOSE state and removes the listener from SipProvider.
     */
    @Override
    public void onTransFailureAck(InviteTransactionServer ts, Message msg)
    {
        printLog("inside onTransFailureAck(ts,msg)", LogLevel.LOW);
        if (!verifyStatus(statusIs(D_REFUSED) || statusIs(D_ReREFUSED)))
        {
            return;
        }
        if (statusIs(D_ReREFUSED))
        {
            changeStatus(D_CALL);
        }
        else
        {
            changeStatus(D_CLOSE);
            listener.onDlgClose(this);
        }
    }

    // ************ Inherited from AckTransactionServerListener ************

    /**
     * When the AckTransactionServer goes into the "Terminated" state, caused by
     * transaction timeout
     */
    @Override
    public void onTransAckTimeout(AckTransactionServer ts)
    {
        printLog("inside onAckSrvTimeout(ts)", LogLevel.LOW);
        if (!verifyStatus(
            statusIs(D_ACCEPTED) || statusIs(D_ReACCEPTED) || statusIs(D_REFUSED) || statusIs(D_ReREFUSED)))
        {
            return;
        }
        printLog("No ACK received..", LogLevel.HIGH);
        changeStatus(D_CLOSE);
        listener.onDlgClose(this);
    }

    // **************************** Logs ****************************/

    /**
     * Adds a new string to the default Log
     */
    @Override
    protected void printLog(String str, int level)
    {
        Log.compatLog("InviteDialog#" + dialogSqn + ": " + str, level);
    }

}
