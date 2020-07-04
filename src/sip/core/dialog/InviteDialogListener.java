package sip.core.dialog;

import com.allcam.gbgw.protocol.sip.core.address.NameAddress;
import com.allcam.gbgw.protocol.sip.core.header.MultipleHeader;
import com.allcam.gbgw.protocol.sip.core.message.Message;

/**
 * An InviteDialogListener listens for InviteDialog events. It collects all
 * InviteDialog callback functions.
 */
public interface InviteDialogListener
{
    /**
     * When an incoming INVITE is received
     */
    void onDlgInvite(InviteDialog dialog, NameAddress callee, NameAddress caller, String body, Message msg);

    /**
     * When an incoming Re-INVITE is received
     */
    void onDlgReInvite(InviteDialog dialog, String body, Message msg);

    /**
     * When a 1xx response response is received for an INVITE transaction
     */
    void onDlgInviteProvisionalResponse(InviteDialog dialog, int code, String reason, String body, Message msg);

    /**
     * When a 2xx successfull final response is received for an INVITE
     * transaction
     */
    void onDlgInviteSuccessResponse(InviteDialog dialog, int code, String reason, String body, Message msg);

    /**
     * When a 3xx redirection response is received for an INVITE transaction
     */
    void onDlgInviteRedirectResponse(InviteDialog dialog, int code, String reason, MultipleHeader contacts,
                                     Message msg);

    /**
     * When a 400-699 failure response is received for an INVITE transaction
     */
    void onDlgInviteFailureResponse(InviteDialog dialog, int code, String reason, Message msg);

    /**
     * When INVITE transaction expires
     */
    void onDlgTimeout(InviteDialog dialog);

    /**
     * When a 1xx response response is received for a Re-INVITE transaction
     */
    void onDlgReInviteProvisionalResponse(InviteDialog dialog, int code, String reason, String body, Message msg);

    /**
     * When a 2xx successfull final response is received for a Re-INVITE
     * transaction
     */
    void onDlgReInviteSuccessResponse(InviteDialog dialog, int code, String reason, String body, Message msg);

    /** When a 3xx redirection response is received for a Re-INVITE transaction */
    // public void onDlgReInviteRedirectResponse(InviteDialog dialog, int code,
    // String reason, MultipleHeader contacts, Message msg);

    /**
     * When a 400-699 failure response is received for a Re-INVITE transaction
     */
    void onDlgReInviteFailureResponse(InviteDialog dialog, int code, String reason, Message msg);

    /**
     * When a Re-INVITE transaction expires
     */
    void onDlgReInviteTimeout(InviteDialog dialog);

    /**
     * When an incoming ACK is received for an INVITE transaction
     */
    void onDlgAck(InviteDialog dialog, String body, Message msg);

    /**
     * When the INVITE handshake is successful terminated
     */
    void onDlgCall(InviteDialog dialog);

    /**
     * When an incoming CANCEL is received for an INVITE transaction
     */
    void onDlgCancel(InviteDialog dialog, Message msg);

    /**
     * When an incoming BYE is received
     */
    void onDlgBye(InviteDialog dialog, Message msg);

    /** When a BYE request traqnsaction has been started */
    // public void onDlgByeing(InviteDialog dialog);

    /**
     * When a success response is received for a Bye request
     */
    void onDlgByeSuccessResponse(InviteDialog dialog, int code, String reason, Message msg);

    /**
     * When a failure response is received for a Bye request
     */
    void onDlgByeFailureResponse(InviteDialog dialog, int code, String reason, Message msg);

    /**
     * When the dialog is finally closed
     */
    void onDlgClose(InviteDialog dialog);

    /**
     * When the dialog receive info
     */
    void onDlgInfo(InviteDialog dialog, Message info);

    /**
     * When the dialog receive message
     */
    void onDlgMessage(InviteDialog dialog, Message message);

}
