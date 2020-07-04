package sip.core.dialog;

import com.allcam.gbgw.protocol.sip.core.address.NameAddress;
import com.allcam.gbgw.protocol.sip.core.header.FromHeader;
import com.allcam.gbgw.protocol.sip.core.header.Header;
import com.allcam.gbgw.protocol.sip.core.header.RecordRouteHeader;
import com.allcam.gbgw.protocol.sip.core.header.ToHeader;
import com.allcam.gbgw.protocol.sip.core.message.Message;
import com.allcam.gbgw.protocol.sip.core.provider.DialogIdentifier;
import com.allcam.gbgw.protocol.sip.core.provider.SipProvider;
import com.allcam.gbgw.protocol.sip.core.provider.SipProviderListener;
import com.allcam.gbgw.protocol.tools.Log;
import com.allcam.gbgw.protocol.tools.LogLevel;

import java.util.Vector;

/**
 * Class Dialog maintains a complete information status of a generic SIP dialog.
 * It has the following attributes:
 * <ul>
 * <li>sip-provider</li>
 * <li>call-id</li>
 * <li>local and remote URLs</li>
 * <li>local and remote contact URLs</li>
 * <li>local and remote cseqs</li>
 * <li>local and remote tags</li>
 * <li>dialog-id</li>
 * <li>route set</li>
 * </ul>
 */
public abstract class Dialog extends DialogInfo implements SipProviderListener
{

    /**
     * Identifier for the transaction client side of a dialog (UAC).
     */
    public final static int UAC = 0;

    /**
     * Identifier for the transaction server side of a dialog (UAS).
     */
    public final static int UAS = 1;

    /**
     * Dialogs counter
     */
    private static int dialog_counter = 0;

    // *********************** Protected attributes ***********************

    /**
     * Dialog sequence number
     */
    protected int dialogSqn;

    /**
     * SipProvider
     */
    protected SipProvider sipProvider;

    /**
     * Internal dialog state.
     */
    protected int status;

    /**
     * Dialog identifier
     */
    private DialogIdentifier dialogId;

    /**
     * from username
     */
    private String fromUsername;

    // ************************* Abstract methods *************************

    /**
     * Gets the dialog state
     */
    abstract protected String getStatusDescription();

    abstract protected int getStatus();

    /**
     * Whether the dialog is in "early" state.
     */
    abstract public boolean isEarly();

    /**
     * Whether the dialog is in "confirmed" state.
     */
    abstract public boolean isConfirmed();

    /**
     * Whether the dialog is in "terminated" state.
     */
    abstract public boolean isTerminated();

    /**
     * When a new Message is received by the SipProvider.
     */
    @Override
    abstract public void onReceivedMessage(SipProvider sipProvider, Message message);

    // **************************** Costructors ***************************

    /**
     * Creates a new empty Dialog
     */
    protected Dialog(SipProvider provider)
    {
        super();
        this.sipProvider = provider;
        this.dialogSqn = dialog_counter++;
        this.status = 0;
        this.dialogId = null;
    }

    protected Dialog(Dialog preDialog)
    {
        this(preDialog.sipProvider, preDialog.fromUsername);
    }

    protected Dialog(SipProvider provider, String fromUsername)
    {
        this(provider);
        this.fromUsername = fromUsername;
    }

    // ************************* Protected methods ************************

    protected String getFromUsername()
    {
        return fromUsername;
    }

    /**
     * Changes the internal dialog state
     */
    protected void changeStatus(int newStatus)
    {
        status = newStatus;
        printLog("changed dialog state: " + getStatus(), LogLevel.MEDIUM);

        // remove the sipProvider listener when going to "terminated" state
        if (isTerminated())
        {
            if (dialogId != null && sipProvider.getListeners().containsKey(dialogId))
            {
                sipProvider.removeSipProviderListener(dialogId);
            }
        }
        else if (isEarly() || isConfirmed())
        {
            // add sipProvider listener when going to "early" or "confirmed" state
            if (dialogId != null && !sipProvider.getListeners().containsKey(dialogId))
            {
                sipProvider.addSipProviderListener(dialogId, this);
            }
        }
    }

    /**
     * Whether the dialog state is equal to <i>st</i>
     */
    protected boolean statusIs(int st)
    {
        return status == st;
    }

    // ************************** Public methods **************************

    /**
     * Gets the SipProvider of this Dialog.
     */
    public SipProvider getSipProvider()
    {
        return sipProvider;
    }

    /**
     * Gets the inique Dialog-ID </i>
     */
    public DialogIdentifier getDialogId()
    {
        return dialogId;
    }

    /**
     * Updates empty attributes (tags, route set) and mutable attributes (cseqs,
     * contacts), based on a new message.
     *
     * @param side indicates whether the Dialog is acting as transaction client
     *             or server for the current message (use constant values
     *             Dialog.UAC or Dialog.UAS)
     * @param msg  the message that is used to update the Dialog state
     */
    public void update(int side, Message msg)
    {
        if (isTerminated())
        {
            printWarning("trying to update a terminated dialog: do nothing.", LogLevel.HIGH);
            return;
        }

        // update callId
        if (callId == null)
        {
            callId = msg.getCallIdHeader().getCallId();
        }

        // update names and tags
        if (side == UAC)
        {
            if (remoteName == null || remoteTag == null)
            {
                ToHeader to = msg.getToHeader();
                if (remoteName == null)
                {
                    remoteName = to.getNameAddress();
                }
                if (remoteTag == null)
                {
                    remoteTag = to.getTag();
                }
            }
            if (localName == null || localTag == null)
            {
                FromHeader from = msg.getFromHeader();
                if (localName == null)
                {
                    localName = from.getNameAddress();
                }
                if (localTag == null)
                {
                    localTag = from.getTag();
                }
            }
            localCseq = msg.getCSeqHeader().getSequenceNumber();
            // if (remoteCseq==-1) remoteCseq=SipProvider.pickInitialCSeq()-1;
        }
        else
        {
            if (localName == null || localTag == null)
            {
                ToHeader to = msg.getToHeader();
                if (localName == null)
                {
                    localName = to.getNameAddress();
                }
                if (localTag == null)
                {
                    localTag = to.getTag();
                }
            }
            if (remoteName == null || remoteTag == null)
            {
                FromHeader from = msg.getFromHeader();
                if (remoteName == null)
                {
                    remoteName = from.getNameAddress();
                }
                if (remoteTag == null)
                {
                    remoteTag = from.getTag();
                }
            }
            remoteCseq = msg.getCSeqHeader().getSequenceNumber();
            if (localCseq == -1)
            {
                localCseq = SipProvider.pickInitialCSeq() - 1;
            }
        }
        // update contact
        if (msg.hasContactHeader())
        {
            if ((side == UAC && msg.isRequest()) || (side == UAS && msg.isResponse()))
            {
                localContact = msg.getContactHeader().getNameAddress();
            }
            else
            {
                remoteContact = msg.getContactHeader().getNameAddress();
            }
        }
        // update route or record-route
        if (side == UAC)
        {
            if (msg.isRequest() && msg.hasRouteHeader() && route == null)
            {
                /* HSC CHANGES START */
                Vector<String> routeS = msg.getRoutes().getValues();
                route = new Vector<>(routeS.size());
                int size = routeS.size();
                for (int i = 0; i < size; i++)
                {
                    route.insertElementAt(new NameAddress(routeS.elementAt(i)), i);
                }
                /* HSC CHANGES END */
            }
            if (msg.isResponse() && msg.hasRecordRouteHeader())
            {
                /* HSC CHANGES START */
                Vector<Header> rr = msg.getRecordRoutes().getHeaders();
                int size = rr.size();
                route = new Vector<>(size);
                /* HSC CHANGES END */
                for (int i = 0; i < size; i++)
                {
                    route.insertElementAt((new RecordRouteHeader(rr.elementAt(size - 1 - i))).getNameAddress(), i);
                }
            }
        }
        else
        {
            if (msg.isRequest() && msg.hasRouteHeader() && route == null)
            {
                /* HSC CHANGES START */
                Vector<String> reverseRoute = msg.getRoutes().getValues();
                int size = reverseRoute.size();
                route = new Vector<>(size);
                for (int i = 0; i < size; i++)
                {
                    route.insertElementAt(new NameAddress(reverseRoute.elementAt(size - 1 - i)), i);
                }
                /* HSC CHANGES END */
            }
            if (msg.isRequest() && msg.hasRecordRouteHeader())
            {
                /* HSC CHANGES START */
                Vector<Header> rr = msg.getRecordRoutes().getHeaders();
                int size = rr.size();
                route = new Vector<>(size);
                for (int i = 0; i < size; i++)
                {
                    route.insertElementAt((new RecordRouteHeader(rr.elementAt(i))).getNameAddress(), i);
                }
                /* HSC CHANGES END */
            }
        }

        // update dialogId and sipProvider listener
        DialogIdentifier newId = new DialogIdentifier(callId, localTag, remoteTag);
        if (dialogId == null || !dialogId.equals(newId))
        {
            if (dialogId != null && sipProvider != null && sipProvider.getListeners().containsKey(dialogId))
            {
                sipProvider.removeSipProviderListener(dialogId);
            }
            dialogId = newId;
            printLog("new dialog id: " + dialogId, LogLevel.HIGH);
            if (sipProvider != null)
            {
                sipProvider.addSipProviderListener(dialogId, this);
            }
        }
    }

    // **************************** Logs ****************************/

    /**
     * Adds a new string to the default Log
     */
    protected void printLog(String str, int level)
    {
        Log.compatLog("Dialog:" + str, level);
    }

    /**
     * Adds a Warning message to the default Log
     */
    protected final void printWarning(String str, int level)
    {
        printLog("WARNING: " + str, level);
    }

    /**
     * Verifies the correct status; if not logs the event.
     */
    protected final boolean verifyStatus(boolean expression)
    {
        return verifyThat(expression, "dialog state mismatching");
    }

    /**
     * Verifies an event; if not logs it.
     */
    protected final boolean verifyThat(boolean expression, String str)
    {
        if (!expression)
        {
            if (str == null || str.length() == 0)
            {
                printWarning("expression check failed. ", 1);
            }
            else
            {
                printWarning(str, 1);
            }
        }
        return expression;
    }

}
