package sip.core.dialog;

import com.allcam.gbgw.protocol.sip.core.address.NameAddress;
import com.allcam.gbgw.protocol.sip.core.message.Message;

/**
 * A SubscriberDialogListener listens for SubscriberDialog events. It collects
 * all SubscriberDialog callback functions.
 */
public interface SubscriberDialogListener
{
    /**
     * When a 2xx successfull final response is received for an SUBSCRIBE transaction.
     */
    void onDlgSubscriptionSuccess(SubscriberDialog dialog, int code, String reason, Message msg);

    /**
     * When a 300-699 response is received for an SUBSCRIBE transaction.
     */
    void onDlgSubscriptionFailure(SubscriberDialog dialog, int code, String reason, Message msg);

    /**
     * When SUBSCRIBE transaction expires without a final response.
     */
    void onDlgSubscribeTimeout(SubscriberDialog dialog);

    /**
     * When the dialog is terminated.
     */
    void onDlgSubscriptionTerminated(SubscriberDialog dialog);

    /**
     * When an incoming NOTIFY is received.
     */
    void onDlgNotify(SubscriberDialog dialog, NameAddress target, NameAddress notifier, NameAddress contact,
                     String state, String contentType, String body, Message msg);

}
