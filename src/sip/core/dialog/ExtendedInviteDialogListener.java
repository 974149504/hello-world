package sip.core.dialog;

import com.allcam.gbgw.protocol.sip.core.address.NameAddress;
import com.allcam.gbgw.protocol.sip.core.message.Message;

/**
 * An ExtendedInviteDialogListener listens for ExtendedInviteDialog events. It
 * extends InviteDialogListener by adding ExtendedInviteDialog-specific callback
 * functions.
 */
public interface ExtendedInviteDialogListener extends InviteDialogListener
{

    /**
     * When an incoming REFER request is received within the dialog
     */
    void onDlgRefer(InviteDialog dialog, NameAddress refer_to, NameAddress referred_by, Message msg);

    /**
     * When a response is received for a REFER request within the dialog
     */
    void onDlgReferResponse(InviteDialog dialog, int code, String reason, Message msg);

    /**
     * When an incoming NOTIFY request is received within the dialog
     */
    void onDlgNotify(InviteDialog dialog, String event, String sipfragment, Message msg);

    /** When a response is received for a NOTIFY request within the dialog */
    // public void onDlgNotifyResponse(InviteDialog dialog, int code, String reason, Message msg);

    /**
     * When an incoming request is received within the dialog different from
     * INVITE, CANCEL, ACK, BYE, REFER, NOTIFY
     */
    void onDlgAltRequest(InviteDialog dialog, String method, String body, Message msg);

    /**
     * When a response is received for a request within the dialog different
     * from INVITE, CANCEL, ACK, BYE, REFER, NOTIFY
     */
    void onDlgAltResponse(InviteDialog dialog, String method, int code, String reason, String body, Message msg);

    /**
     * When a request timeout expires within the dialog different from INVITE,
     * CANCEL, ACK, BYE, REFER, NOTIFY
     */
    // void onDlgAltTimeout(InviteDialog dialog, String method);
}
