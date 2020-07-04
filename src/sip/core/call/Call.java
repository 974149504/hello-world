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

package sip.core.call;

/* HSC CHANGES START */
// import org.zoolu.sdp.*;
// import java.util.Vector;
/* HSC CHANGES END */

import com.allcam.gbgw.protocol.sip.core.address.NameAddress;
import com.allcam.gbgw.protocol.sip.core.dialog.InviteDialog;
import com.allcam.gbgw.protocol.sip.core.dialog.InviteDialogListener;
import com.allcam.gbgw.protocol.sip.core.header.MultipleHeader;
import com.allcam.gbgw.protocol.sip.core.message.Message;
import com.allcam.gbgw.protocol.sip.core.provider.SipProvider;
import com.allcam.gbgw.protocol.tools.Log;
import com.allcam.gbgw.protocol.tools.LogLevel;

/**
 * Class Call implements SIP calls.
 * <p>
 * It handles both outgoing or incoming calls.
 * <p>
 * Both offer/answer models are supported, that is: <br>
 * i) offer/answer in invite/2xx, or <br>
 * ii) offer/answer in 2xx/ack
 */
public class Call implements InviteDialogListener
{
    /**
     * The SipProvider used for the call
     */
    protected SipProvider sip_provider;

    /**
     * The invite dialog (sip.dialog.InviteDialog)
     */
    protected InviteDialog dialog;

    /**
     * The user url
     */
    protected String from_url;

    /**
     * The user contact url
     */
    protected String contact_url;

    /**
     * The local sdp
     */
    protected String local_sdp;

    /**
     * The remote sdp
     */
    protected String remote_sdp;

    /**
     * The call listener (sipx.call.CallListener)
     */
    CallListener listener;

    /**
     * Creates a new Call.
     */
    public Call(SipProvider sip_provider, String from_url, String contact_url, CallListener call_listener)
    {
        this.sip_provider = sip_provider;
        this.listener = call_listener;
        this.from_url = from_url;
        this.contact_url = contact_url;
        this.dialog = null;
        this.local_sdp = null;
        this.remote_sdp = null;
    }

    /** Creates a new Call specifing the sdp */
    /*
     * public Call(SipProvider sipProvider, String fromUrl, String
     * contactUrl, String sdp, CallListener call_listener) {
     * this.sipProvider=sipProvider; this.log=sipProvider.getLog();
     * this.listener=call_listener; this.fromUrl=fromUrl;
     * this.contactUrl=contactUrl; localSdp=sdp; }
     */

    /** Gets the current invite dialog */
    /*
     * public InviteDialog getInviteDialog() { return dialog; }
     */

    /**
     * Gets the current local session descriptor
     */
    public String getLocalSessionDescriptor()
    {
        return local_sdp;
    }

    /**
     * Sets a new local session descriptor
     */
    public void setLocalSessionDescriptor(String sdp)
    {
        local_sdp = sdp;
    }

    /**
     * Gets the current remote session descriptor
     */
    public String getRemoteSessionDescriptor()
    {
        return remote_sdp;
    }

    /**
     * Whether the call is on (active).
     */
    public boolean isOnCall()
    {
        return dialog.isSessionActive();
    }

    /**
     * Waits for an incoming call
     */
    public void listen()
    {
        dialog = new InviteDialog(sip_provider, this);
        dialog.listen();
    }

    /**
     * Starts a new call, inviting a remote user (<i>callee</i>)
     */
    public void call(String callee)
    {
        call(callee, null, null, null, null);                        // modified by mandrajg
    }

    /**
     * Starts a new call, inviting a remote user (<i>callee</i>)
     */
    public void call(String callee, String sdp, String icsi)
    {        // modified by mandrajg
        call(callee, null, null, sdp, icsi);
    }

    /**
     * Starts a new call, inviting a remote user (<i>callee</i>)
     */
    public void call(String callee, String from, String contact, String sdp, String icsi)
    {    // modified by mandrajg
        printLog("calling " + callee, LogLevel.HIGH);
        if (from == null)
            from = from_url;
        if (contact == null)
            contact = contact_url;
        if (sdp != null)
            local_sdp = sdp;
        dialog = new InviteDialog(sip_provider, this);
        if (local_sdp != null)
            dialog.invite(callee, from, contact, local_sdp, icsi);    // modified by mandrajg
        else
            dialog.inviteWithoutOffer(callee, from, contact);
    }

    /**
     * Starts a new call with the <i>invite</i> message request
     */
    public void call(Message invite)
    {
        dialog = new InviteDialog(sip_provider, this);
        local_sdp = invite.getBody();
        if (local_sdp != null)
            dialog.invite(invite);
        else
            dialog.inviteWithoutOffer(invite);
    }

    /**
     * Answers at the 2xx/offer (in the ack message)
     */
    public void ackWithAnswer(String sdp)
    {
        local_sdp = sdp;
        dialog.ackWithAnswer(contact_url, sdp);
    }

    /**
     * Rings back for the incoming call
     */
    public void ring(String sdp)
    { // modified
        local_sdp = sdp;
        if (dialog != null)
            dialog.ring(sdp);
    }

    /**
     * Respond to a incoming call (invite) with <i>resp</i>
     */
    public void respond(Message resp)
    {
        if (dialog != null)
            dialog.respond(resp);
    }

    /** Accepts the incoming call */
    /*
     * public void accept() { accept(localSdp); }
     */

    /**
     * Accepts the incoming call
     */
    public void accept(String sdp)
    {
        local_sdp = sdp;
        if (dialog != null)
            dialog.accept(contact_url, local_sdp);
    }

    /**
     * Redirects the incoming call
     */
    public void redirect(String redirect_url)
    {
        if (dialog != null)
            dialog.redirect(302, "Moved Temporarily", redirect_url);
    }

    /**
     * Refuses the incoming call
     */
    public void refuse()
    {
        if (dialog != null)
            dialog.refuse();
    }

    /**
     * Cancels the outgoing call
     */
    public void cancel()
    {
        if (dialog != null)
            dialog.cancel();
    }

    /**
     * Close the ongoing call
     */
    public void bye()
    {
        if (dialog != null)
            dialog.bye();
    }

    /**
     * Modify the current call
     */
    public void modify(String contact, String sdp)
    {
        local_sdp = sdp;
        if (dialog != null)
            dialog.reInvite(contact, local_sdp);
    }

    /**
     * Closes an ongoing or incoming/outgoing call
     * <p>
     * It trys to fires refuse(), cancel(), and bye() methods
     */
    public void hangup()
    {
        if (dialog != null)
        { // try dialog.refuse(), cancel(), and bye()
            // methods..
            dialog.refuse();
            dialog.cancel();
            dialog.bye();
        }
    }

    public void busy()
    {
        if (dialog != null)
            dialog.busy(); // modified
    }

    // ************** Inherited from InviteDialogListener **************

    /**
     * Inherited from class InviteDialogListener and called by an InviteDialog.
     * Normally you should not use it. Use specific callback methods instead
     * (e.g. onCallIncoming()).
     */
    @Override
    public void onDlgInvite(InviteDialog d, NameAddress callee, NameAddress caller, String sdp, Message msg)
    {
        if (d != dialog)
        {
            printLog("NOT the current dialog", LogLevel.HIGH);
            return;
        }
        if (sdp != null && sdp.length() != 0)
            remote_sdp = sdp;
        if (listener != null)
            listener.onCallIncoming(this, callee, caller, sdp, msg);
    }

    /**
     * Inherited from class InviteDialogListener and called by an InviteDialag.
     * Normally you should not use it. Use specific callback methods instead
     * (e.g. onCallModifying()).
     */
    @Override
    public void onDlgReInvite(InviteDialog d, String sdp, Message msg)
    {
        if (d != dialog)
        {
            printLog("NOT the current dialog", LogLevel.HIGH);
            return;
        }
        if (sdp != null && sdp.length() != 0)
            remote_sdp = sdp;
        if (listener != null)
            listener.onCallModifying(this, sdp, msg);
    }

    /**
     * Inherited from class InviteDialogListener and called by an InviteDialag.
     * Normally you should not use it. Use specific callback methods instead
     * (e.g. onCallRinging()).
     */
    @Override
    public void onDlgInviteProvisionalResponse(InviteDialog d, int code, String reason, String sdp, Message msg)
    {
        if (d != dialog)
        {
            printLog("NOT the current dialog", LogLevel.HIGH);
            return;
        }
        if (sdp != null && sdp.length() != 0)
            remote_sdp = sdp;
        if (code == 180 || code == 183) // modified
            if (listener != null)
                listener.onCallRinging(this, msg);
    }

    /**
     * Inherited from class InviteDialogListener and called by an InviteDialag.
     * Normally you should not use it. Use specific callback methods instead
     * (e.g. onCallAccepted()).
     */
    @Override
    public void onDlgInviteSuccessResponse(InviteDialog d, int code, String reason, String sdp, Message msg)
    {
        if (d != dialog)
        {
            printLog("NOT the current dialog", LogLevel.HIGH);
            return;
        }
        if (sdp != null && sdp.length() != 0)
            remote_sdp = sdp;
        if (listener != null)
            listener.onCallAccepted(this, sdp, msg);
    }

    /**
     * Inherited from class InviteDialogListener and called by an InviteDialag.
     * Normally you should not use it. Use specific callback methods instead
     * (e.g. onCallRedirection()).
     */
    @Override
    public void onDlgInviteRedirectResponse(InviteDialog d, int code, String reason, MultipleHeader contacts,
                                            Message msg)
    {
        if (d != dialog)
        {
            printLog("NOT the current dialog", LogLevel.HIGH);
            return;
        }
        if (listener != null)
            listener.onCallRedirection(this, reason, contacts.getValues(), msg);
    }

    /**
     * Inherited from class InviteDialogListener and called by an InviteDialag.
     * Normally you should not use it. Use specific callback methods instead
     * (e.g. onCallRefused()).
     */
    @Override
    public void onDlgInviteFailureResponse(InviteDialog d, int code, String reason, Message msg)
    {
        if (d != dialog)
        {
            printLog("NOT the current dialog", LogLevel.HIGH);
            return;
        }
        if (listener != null)
            listener.onCallRefused(this, reason, msg);
    }

    /**
     * Inherited from class InviteDialogListener and called by an InviteDialag.
     * Normally you should not use it. Use specific callback methods instead
     * (e.g. onCallTimeout()).
     */
    @Override
    public void onDlgTimeout(InviteDialog d)
    {
        if (d != dialog)
        {
            printLog("NOT the current dialog", LogLevel.HIGH);
            return;
        }
        if (listener != null)
            listener.onCallTimeout(this);
    }

    /**
     * Inherited from class InviteDialogListener and called by an InviteDialag.
     * Normally you should not use it.
     */
    @Override
    public void onDlgReInviteProvisionalResponse(InviteDialog d, int code, String reason, String sdp, Message msg)
    {
        if (d != dialog)
        {
            printLog("NOT the current dialog", LogLevel.HIGH);
            return;
        }
        if (sdp != null && sdp.length() != 0)
            remote_sdp = sdp;
    }

    /**
     * Inherited from class InviteDialogListener and called by an InviteDialag.
     * Normally you should not use it. Use specific callback methods instead
     * (e.g. onCallReInviteAccepted()).
     */
    @Override
    public void onDlgReInviteSuccessResponse(InviteDialog d, int code, String reason, String sdp, Message msg)
    {
        if (d != dialog)
        {
            printLog("NOT the current dialog", LogLevel.HIGH);
            return;
        }
        if (sdp != null && sdp.length() != 0)
            remote_sdp = sdp;
        if (listener != null)
            listener.onCallReInviteAccepted(this, sdp, msg);
    }

    /**
     * Inherited from class InviteDialogListener and called by an InviteDialag.
     * Normally you should not use it. Use specific callback methods instead
     * (e.g. onCallReInviteRedirection()).
     */
    // public void onDlgReInviteRedirectResponse(InviteDialog d, int code,
    // String reason, MultipleHeader contacts, Message msg)
    // { if (d!=dialog) { printLog("NOT the current dialog",LogLevel.HIGH);
    // return; }
    // if (listener!=null)
    // listener.onCallReInviteRedirection(this,reason,contacts.getValues(),msg);
    // }

    /**
     * Inherited from class InviteDialogListener and called by an InviteDialag.
     * Normally you should not use it. Use specific callback methods instead
     * (e.g. onCallReInviteRefused()).
     */
    @Override
    public void onDlgReInviteFailureResponse(InviteDialog d, int code, String reason, Message msg)
    {
        if (d != dialog)
        {
            printLog("NOT the current dialog", LogLevel.HIGH);
            return;
        }
        if (listener != null)
            listener.onCallReInviteRefused(this, reason, msg);
    }

    /**
     * Inherited from class InviteDialogListener and called by an InviteDialag.
     * Normally you should not use it. Use specific callback methods instead
     * (e.g. onCallReInviteTimeout()).
     */
    @Override
    public void onDlgReInviteTimeout(InviteDialog d)
    {
        if (d != dialog)
        {
            printLog("NOT the current dialog", LogLevel.HIGH);
            return;
        }
        if (listener != null)
            listener.onCallReInviteTimeout(this);
    }

    /**
     * Inherited from class InviteDialogListener and called by an InviteDialag.
     * Normally you should not use it. Use specific callback methods instead
     * (e.g. onCallConfirmed()).
     */
    @Override
    public void onDlgAck(InviteDialog d, String sdp, Message msg)
    {
        if (d != dialog)
        {
            printLog("NOT the current dialog", LogLevel.HIGH);
            return;
        }
        if (sdp != null && sdp.length() != 0)
            remote_sdp = sdp;
        if (listener != null)
            listener.onCallConfirmed(this, sdp, msg);
    }

    /**
     * Inherited from class InviteDialogListener and called by an InviteDialag.
     * Normally you should not use it. Use specific callback methods instead
     * (e.g. onCallClosing()).
     */
    @Override
    public void onDlgCancel(InviteDialog d, Message msg)
    {
        if (d != dialog)
        {
            printLog("NOT the current dialog", LogLevel.HIGH);
            return;
        }
        if (listener != null)
            listener.onCallCanceling(this, msg);
    }

    /**
     * Inherited from class InviteDialogListener and called by an InviteDialag.
     * Normally you should not use it. Use specific callback methods instead
     * (e.g. onClosing()).
     */
    @Override
    public void onDlgBye(InviteDialog d, Message msg)
    {
        if (d != dialog)
        {
            printLog("NOT the current dialog", LogLevel.HIGH);
            return;
        }
        if (listener != null)
            listener.onCallClosing(this, msg);
    }

    /**
     * Inherited from class InviteDialogListener and called by an InviteDialag.
     * Normally you should not use it. Use specific callback methods instead
     * (e.g. onClosed()).
     */
    @Override
    public void onDlgByeFailureResponse(InviteDialog d, int code, String reason, Message msg)
    {
        if (d != dialog)
        {
            printLog("NOT the current dialog", LogLevel.HIGH);
            return;
        }
        if (listener != null)
            listener.onCallClosed(this, msg);
    }

    /**
     * Inherited from class InviteDialogListener and called by an InviteDialag.
     * Normally you should not use it. Use specific callback methods instead
     * (e.g. onClosed()).
     */
    @Override
    public void onDlgByeSuccessResponse(InviteDialog d, int code, String reason, Message msg)
    {
        if (d != dialog)
        {
            printLog("NOT the current dialog", LogLevel.HIGH);
            return;
        }
        if (listener != null)
            listener.onCallClosed(this, msg);
    }

    // -----------------------------------------------------

    /** When an incoming INVITE is accepted */
    // public void onDlgAccepted(InviteDialog dialog) {}
    /** When an incoming INVITE is refused */
    // public void onDlgRefused(InviteDialog dialog) {}

    /**
     * When the INVITE handshake is successful terminated
     */
    @Override
    public void onDlgCall(InviteDialog dialog)
    {
    }

    /** When an incoming Re-INVITE is accepted */
    // public void onDlgReInviteAccepted(InviteDialog dialog) {}
    /** When an incoming Re-INVITE is refused */
    // public void onDlgReInviteRefused(InviteDialog dialog) {}
    /** When a BYE request traqnsaction has been started */
    // public void onDlgByeing(InviteDialog dialog) {}

    /**
     * When the dialog is finally closed
     */
    @Override
    public void onDlgClose(InviteDialog dialog)
    {
    }

    @Override
    public void onDlgInfo(InviteDialog dialog, Message info)
    {
    }

    @Override
    public void onDlgMessage(InviteDialog dialog, Message message)
    {
    }

    // **************************** Logs ****************************/

    /**
     * Adds a new string to the default Log
     */
    protected void printLog(String str, int level)
    {
        Log.compatLog("Call#" + str, level);
    }
}
