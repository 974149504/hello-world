package sip.core.dialog;

import com.allcam.gbgw.protocol.sip.core.address.NameAddress;
import com.allcam.gbgw.protocol.sip.core.header.MultipleHeader;
import com.allcam.gbgw.protocol.sip.core.message.Message;
import lombok.extern.slf4j.Slf4j;

/**
 * An ExtendedInviteDialogListener listens for ExtendedInviteDialog events. It
 * extends InviteDialogListener by adding ExtendedInviteDialog-specific callback
 * functions.
 */
@Slf4j
public class SimpleInviteDialogListener implements InviteDialogListener
{

    /**
     * When an incoming INVITE is received
     */
    @Override
    public void onDlgInvite(InviteDialog dialog, NameAddress callee, NameAddress caller, String body, Message msg)
    {
        log.debug("onDlgInvite dialog=[{}]", dialog.getDialogId());
    }

    /**
     * When an incoming Re-INVITE is received
     */
    @Override
    public void onDlgReInvite(InviteDialog dialog, String body, Message msg)
    {
        log.debug("onDlgReInvite dialog=[{}]", dialog.getDialogId());
    }

    /**
     * When a 1xx response response is received for an INVITE transaction
     */
    @Override
    public void onDlgInviteProvisionalResponse(InviteDialog dialog, int code, String reason, String body, Message msg)
    {
        log.debug("onDlgInviteProvisionalResponse dialog=[{}]", dialog.getDialogId());
    }

    /**
     * When a 2xx successfull final response is received for an INVITE
     * transaction
     */
    @Override
    public void onDlgInviteSuccessResponse(InviteDialog dialog, int code, String reason, String body, Message msg)
    {
        log.debug("onDlgInviteSuccessResponse dialog=[{}]", dialog.getDialogId());
    }

    /**
     * When a 3xx redirection response is received for an INVITE transaction
     */
    @Override
    public void onDlgInviteRedirectResponse(InviteDialog dialog, int code, String reason, MultipleHeader contacts,
                                            Message msg)
    {
        log.debug("onDlgInviteRedirectResponse dialog=[{}]", dialog.getDialogId());
    }

    /**
     * When a 400-699 failure response is received for an INVITE transaction
     */
    @Override
    public void onDlgInviteFailureResponse(InviteDialog dialog, int code, String reason, Message msg)
    {
        log.debug("onDlgInviteFailureResponse dialog=[{}]", dialog.getDialogId());
    }

    /**
     * When INVITE transaction expires
     */
    @Override
    public void onDlgTimeout(InviteDialog dialog)
    {
        log.debug("onDlgTimeout dialog=[{}]", dialog.getDialogId());
    }

    /**
     * When a 1xx response response is received for a Re-INVITE transaction
     */
    @Override
    public void onDlgReInviteProvisionalResponse(InviteDialog dialog, int code, String reason, String body, Message msg)
    {
        log.debug("onDlgReInviteProvisionalResponse dialog=[{}]", dialog.getDialogId());
    }

    /**
     * When a 2xx successfull final response is received for a Re-INVITE
     * transaction
     */
    @Override
    public void onDlgReInviteSuccessResponse(InviteDialog dialog, int code, String reason, String body, Message msg)
    {
        log.debug("onDlgReInviteSuccessResponse dialog=[{}]", dialog.getDialogId());
    }

    /**
     * When a 400-699 failure response is received for a Re-INVITE transaction
     */
    @Override
    public void onDlgReInviteFailureResponse(InviteDialog dialog, int code, String reason, Message msg)
    {
        log.debug("onDlgReInviteFailureResponse dialog=[{}]", dialog.getDialogId());
    }

    /**
     * When a Re-INVITE transaction expires
     */
    @Override
    public void onDlgReInviteTimeout(InviteDialog dialog)
    {
        log.debug("onDlgReInviteTimeout dialog=[{}]", dialog.getDialogId());
    }

    /**
     * When an incoming ACK is received for an INVITE transaction
     */
    @Override
    public void onDlgAck(InviteDialog dialog, String body, Message msg)
    {
        log.debug("onDlgAck dialog=[{}]", dialog.getDialogId());
    }

    /**
     * When the INVITE handshake is successful terminated
     */
    @Override
    public void onDlgCall(InviteDialog dialog)
    {
        log.debug("onDlgCall dialog=[{}]", dialog.getDialogId());
    }

    /**
     * When an incoming CANCEL is received for an INVITE transaction
     */
    @Override
    public void onDlgCancel(InviteDialog dialog, Message msg)
    {
        log.debug("onDlgCancel dialog=[{}]", dialog.getDialogId());
    }

    /**
     * When an incoming BYE is received
     */
    @Override
    public void onDlgBye(InviteDialog dialog, Message msg)
    {
        log.debug("onDlgBye dialog=[{}]", dialog.getDialogId());
    }

    /**
     * When a success response is received for a Bye request
     */
    @Override
    public void onDlgByeSuccessResponse(InviteDialog dialog, int code, String reason, Message msg)
    {
        log.debug("onDlgByeSuccessResponse dialog=[{}]", dialog.getDialogId());
    }

    /**
     * When a failure response is received for a Bye request
     */
    @Override
    public void onDlgByeFailureResponse(InviteDialog dialog, int code, String reason, Message msg)
    {
        log.debug("onDlgByeFailureResponse dialog=[{}]", dialog.getDialogId());
    }

    /**
     * When the dialog is finally closed
     */
    @Override
    public void onDlgClose(InviteDialog dialog)
    {
        log.debug("onDlgClose dialog=[{}]", dialog.getDialogId());
    }

    @Override
    public void onDlgInfo(InviteDialog dialog, Message info)
    {
        log.debug("onDlgInfo dialog=[{}]", dialog.getDialogId());
    }

    @Override
    public void onDlgMessage(InviteDialog dialog, Message message)
    {
        log.debug("onDlgMessage dialog=[{}]", dialog.getDialogId());
    }
}
