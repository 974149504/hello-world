package sip.core.dialog;

import com.allcam.gbgw.protocol.sip.core.address.NameAddress;
import com.allcam.gbgw.protocol.sip.core.message.Message;

/**
 * A NotifierDialogListener listens for NotifierDialog events. It collects all
 * NOTIFY callback functions.
 */
public interface NotifierDialogListener
{
    /**
     * When an incoming SUBSCRIBE is received.
     */
    void onDlgSubscribe(NotifierDialog dialog, NameAddress target, NameAddress subscriber, Message msg, int expire);

    /**
     * When NOTIFY transaction expires without a final response.
     */
    void onDlgNotifyTimeout(NotifierDialog dialog);

    /**
     * When a 300-699 response is received for a NOTIFY transaction.
     */
    void onDlgNotificationFailure(NotifierDialog dialog, int code, String reason, Message msg);

    /**
     * When a 2xx successfull final response is received for a NOTIFY
     * transaction.
     */
    void onDlgNotificationSuccess(NotifierDialog dialog, int code, String reason, Message msg);

}
