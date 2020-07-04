package sip.core.dialog;

import com.allcam.gbgw.protocol.sip.core.address.NameAddress;
import com.allcam.gbgw.protocol.sip.core.authentication.DigestAuthentication;
import com.allcam.gbgw.protocol.sip.core.header.*;
import com.allcam.gbgw.protocol.sip.core.message.*;
import com.allcam.gbgw.protocol.sip.core.provider.SipProvider;
import com.allcam.gbgw.protocol.sip.core.provider.TransactionIdentifier;
import com.allcam.gbgw.protocol.sip.core.transaction.Transaction;
import com.allcam.gbgw.protocol.sip.core.transaction.TransactionClient;
import com.allcam.gbgw.protocol.sip.core.transaction.TransactionServer;
import com.allcam.gbgw.protocol.tools.Log;
import com.allcam.gbgw.protocol.tools.LogLevel;

import java.util.Hashtable;

/**
 * Class ExtendedInviteDialog can be used to manage extended invite dialogs.
 * <p>
 * An ExtendedInviteDialog allows the user: <br>- to handle authentication <br>-
 * to handle refer/notify <br>- to capture all methods within the dialog
 */
public class ExtendedInviteDialog extends InviteDialog
{
    /**
     * Max number of registration attempts.
     */
    private static final int MAX_ATTEMPTS = 3;

    /**
     * ExtendedInviteDialog listener.
     */
    private ExtendedInviteDialogListener dialogListener;

    /**
     * Acive transactions.
     */
    private Hashtable<TransactionIdentifier, Transaction> transactions;

    /**
     * User name.
     */
    private String username;

    /**
     * User name.
     */
    private String realm;

    /**
     * User's passwd.
     */
    private String passwd;

    /**
     * Nonce for the next authentication.
     */
    private String nextNonce;

    /**
     * Qop for the next authentication.
     */
    private String qop;

    /**
     * Number of authentication attempts.
     */
    private int attempts;

    /**
     * Creates a new ExtendedInviteDialog.
     */
    public ExtendedInviteDialog(SipProvider provider, ExtendedInviteDialogListener listener)
    {
        super(provider, listener);
        init(listener);
    }

    /**
     * Creates a new ExtendedInviteDialog.
     */
    public ExtendedInviteDialog(SipProvider provider, String username, String realm, String passwd,
                                ExtendedInviteDialogListener listener)
    {
        super(provider, listener);
        init(listener);
        this.username = username;
        this.realm = realm;
        this.passwd = passwd;
    }

    /**
     * Inits the ExtendedInviteDialog.
     */
    private void init(ExtendedInviteDialogListener listener)
    {
        this.dialogListener = listener;
        this.transactions = new Hashtable<>();
        this.username = null;
        this.realm = null;
        this.passwd = null;
        this.nextNonce = null;
        this.qop = null;
        this.attempts = 0;
    }

    /**
     * Sends a new request within the dialog
     */
    public void request(Message req)
    {
        TransactionClient t = new TransactionClient(sipProvider, req, this);
        transactions.put(t.getTransactionId(), t);
        t.request();
    }

    /**
     * Sends a new REFER within the dialog
     */
    public void refer(NameAddress refer_to)
    {
        refer(refer_to, null);
    }

    public void info(char c, int duration) // modified (again by Matthew Monacelli)
    {
        Message req = BaseMessageFactory.createRequest(this, SipMethods.INFO, null);
        req.setBody("application/dtmf-relay", "Signal=" + c + "\r\n+Duration=" + duration);
        request(req);
    }

    /**
     * Sends a new REFER within the dialog
     */
    public void refer(NameAddress refer_to, NameAddress referred_by)
    {
        Message req = MessageFactory.createReferRequest(this, refer_to, referred_by);
        request(req);
    }

    /**
     * Sends a new NOTIFY within the dialog
     */
    public void notify(int code, String reason)
    {
        notify((new StatusLine(code, reason)).toString());
    }

    /**
     * Sends a new NOTIFY within the dialog
     */
    public void notify(String sipfragment)
    {
        Message req = MessageFactory.createNotifyRequest(this, "refer", null, sipfragment);
        request(req);
    }

    /**
     * Responds with <i>resp</i>
     */
    @Override
    public void respond(Message resp)
    {
        printLog("inside respond(resp)", LogLevel.MEDIUM);
        String method = resp.getCSeqHeader().getMethod();
        if (method.equals(SipMethods.INVITE) || method.equals(SipMethods.CANCEL) || method.equals(SipMethods.BYE))
        {
            super.respond(resp);
        }
        else
        {
            TransactionIdentifier transaction_id = resp.getTransactionId();
            printLog("transaction-id=" + transaction_id, LogLevel.MEDIUM);
            if (transactions.containsKey(transaction_id))
            {
                printLog("responding", LogLevel.LOW);
                TransactionServer t = (TransactionServer)transactions.get(transaction_id);
                t.respondWith(resp);
            }
            else
            {
                printLog("transaction server not found; message discarded", LogLevel.MEDIUM);
            }
        }
    }

    /**
     * Accept a REFER
     */
    public void acceptRefer(Message req)
    {
        printLog("inside acceptRefer(refer)", LogLevel.MEDIUM);
        Message resp = MessageFactory.createResponse(req, 202, null);
        respond(resp);
    }

    /**
     * Refuse a REFER
     */
    public void refuseRefer(Message req)
    {
        printLog("inside refuseRefer(refer)", LogLevel.MEDIUM);
        Message resp = MessageFactory.createResponse(req, 603, null);
        respond(resp);
    }

    /**
     * Inherited from class SipProviderListener.
     */
    @Override
    public void onReceivedMessage(SipProvider sipProvider, Message msg)
    {
        printLog("Message received: " + msg.getFirstLine().substring(0, msg.toString().indexOf('\r')), LogLevel.LOW);
        if (msg.isResponse())
        {
            super.onReceivedMessage(sipProvider, msg);
        }
        else if (msg.isInvite() || msg.isAck() || msg.isCancel() || msg.isBye())
        {
            super.onReceivedMessage(sipProvider, msg);
        }
        else
        {
            TransactionServer t = new TransactionServer(this.sipProvider, msg, this);
            transactions.put(t.getTransactionId(), t);
            // t.listen();

            if (msg.isRefer())
            { // Message
                // resp=MessageFactory.createResponse(msg,202,"Accepted",null,null);
                // respond(resp);
                NameAddress referTo = msg.getReferToHeader().getNameAddress();
                NameAddress referredBy = null;
                if (msg.hasReferredByHeader())
                {
                    referredBy = msg.getReferredByHeader().getNameAddress();
                }
                dialogListener.onDlgRefer(this, referTo, referredBy, msg);
            }
            else if (msg.isNotify())
            {
                Message resp = MessageFactory.createResponse(msg, 200, null);
                respond(resp);
                String event = msg.getEventHeader().getValue();
                String sipfragment = msg.getBody();
                dialogListener.onDlgNotify(this, event, sipfragment, msg);
            }
            else
            {
                printLog("Received alternative request " + msg.getRequestLine().getMethod(), LogLevel.MEDIUM);
                dialogListener.onDlgAltRequest(this, msg.getRequestLine().getMethod(), msg.getBody(), msg);
            }
        }
    }

    /**
     * Inherited from TransactionClientListener. When the
     * TransactionClientListener goes into the "Completed" state, receiving a
     * failure response
     */
    @Override
    public void onTransFailureResponse(TransactionClient tc, Message msg)
    {
        printLog("inside onTransFailureResponse(" + tc.getTransactionId() + ",msg)", LogLevel.LOW);
        String method = tc.getTransactionMethod();
        StatusLine statusLine = msg.getStatusLine();
        int code = statusLine.getCode();
        String reason = statusLine.getReason();

        boolean isErr401 = false;
        boolean isErr407 = false;

        // AUTHENTICATION-BEGIN
        if (attempts < MAX_ATTEMPTS)
        {
            switch (code)
            {
                case 401:
                    if (msg.hasWwwAuthenticateHeader())
                    {
                        realm = msg.getWwwAuthenticateHeader().getRealmParam();
                        isErr401 = true;
                    }
                    break;

                case 407:
                    if (msg.hasProxyAuthenticateHeader())
                    {
                        realm = msg.getProxyAuthenticateHeader().getRealmParam();
                        isErr407 = true;
                    }
            }
        }

        if (isErr401 | isErr407)
        {
            attempts++;
            Message req = tc.getRequestMessage();
            req.setCSeqHeader(req.getCSeqHeader().incSequenceNumber());
            ViaHeader vh = req.getViaHeader();
            String newbranch = SipProvider.pickBranch();
            vh.setBranch(newbranch);
            req.removeViaHeader();

            req.addViaHeader(vh);
            WwwAuthenticateHeader wah;
            if (code == 401)
            {
                wah = msg.getWwwAuthenticateHeader();
            }
            else
            {
                wah = msg.getProxyAuthenticateHeader();
            }
            String qop_options = wah.getQopOptionsParam();
            qop = (qop_options != null) ? "auth" : null;
            RequestLine rl = req.getRequestLine();
            DigestAuthentication digest =
                new DigestAuthentication(rl.getMethod(), rl.getAddress().toString(), wah, qop, null, username, passwd);
            AuthorizationHeader ah;
            if (code == 401)
            {
                ah = digest.getAuthorizationHeader();
            }
            else
            {
                ah = digest.getProxyAuthorizationHeader();
            }
            req.setAuthorizationHeader(ah);
            transactions.remove(tc.getTransactionId());
            tc = new TransactionClient(sipProvider, req, this);
            transactions.put(tc.getTransactionId(), tc);
            tc.request();
            inviteReq = req;
        }
        else
            // AUTHENTICATION-END
            if (method.equals(SipMethods.INVITE) || method.equals(SipMethods.CANCEL) || method.equals(SipMethods.BYE))
            {
                super.onTransFailureResponse(tc, msg);
            }
            else if (tc.getTransactionMethod().equals(SipMethods.REFER))
            {
                transactions.remove(tc.getTransactionId());
                dialogListener.onDlgReferResponse(this, code, reason, msg);
            }
            else
            {
                String body = msg.getBody();
                transactions.remove(tc.getTransactionId());
                dialogListener.onDlgAltResponse(this, method, code, reason, body, msg);
            }
    }

    /**
     * Inherited from TransactionClientListener. When an
     * TransactionClientListener goes into the "Terminated" state, receiving a
     * 2xx response
     */
    @Override
    public void onTransSuccessResponse(TransactionClient t, Message msg)
    {
        printLog("inside onTransSuccessResponse(" + t.getTransactionId() + ",msg)", LogLevel.LOW);
        attempts = 0;
        String method = t.getTransactionMethod();
        StatusLine statusLine = msg.getStatusLine();
        int code = statusLine.getCode();
        String reason = statusLine.getReason();

        if (method.equals(SipMethods.INVITE) || method.equals(SipMethods.CANCEL) || method.equals(SipMethods.BYE))
        {
            super.onTransSuccessResponse(t, msg);
        }
        else if (t.getTransactionMethod().equals(SipMethods.REFER))
        {
            transactions.remove(t.getTransactionId());
            dialogListener.onDlgReferResponse(this, code, reason, msg);
        }
        else
        {
            String body = msg.getBody();
            transactions.remove(t.getTransactionId());
            dialogListener.onDlgAltResponse(this, method, code, reason, body, msg);
        }
    }

    /**
     * Inherited from TransactionClientListener. When the TransactionClient goes
     * into the "Terminated" state, caused by transaction timeout
     */
    @Override
    public void onTransTimeout(TransactionClient t)
    {
        printLog("inside onTransTimeout(" + t.getTransactionId() + ",msg)", LogLevel.LOW);
        String method = t.getTransactionMethod();
        if (method.equals(SipMethods.INVITE) || method.equals(SipMethods.BYE))
        {
            super.onTransTimeout(t);
        }
        else
        { // do something..
            transactions.remove(t.getTransactionId());
        }
    }

    // **************************** Logs ****************************/

    /**
     * Adds a new string to the default Log
     */
    @Override
    protected void printLog(String str, int level)
    {
        Log.compatLog("ExtendedInviteDialog#" + dialogSqn + ": " + str, level);
    }

}
