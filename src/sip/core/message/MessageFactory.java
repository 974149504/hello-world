package sip.core.message;

import com.allcam.gbgw.protocol.sip.core.address.NameAddress;
import com.allcam.gbgw.protocol.sip.core.address.SipURL;
import com.allcam.gbgw.protocol.sip.core.dialog.Dialog;
import com.allcam.gbgw.protocol.sip.core.header.EventHeader;
import com.allcam.gbgw.protocol.sip.core.header.ReferToHeader;
import com.allcam.gbgw.protocol.sip.core.header.ReferredByHeader;
import com.allcam.gbgw.protocol.sip.core.header.SubjectHeader;
import com.allcam.gbgw.protocol.sip.core.provider.SipProvider;

/**
 * Class sipx.message.MessageFactory extends class
 * sip.message.BaseMessageFactory.
 * <p/>
 * MessageFactory is used to create SIP messages (requests and responses).
 * <br />
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
public class MessageFactory extends BaseMessageFactory
{

    /**
     * Creates a new MESSAGE request (RFC3428)
     */
    public static Message createMessageRequest(SipProvider sipProvider, NameAddress to, NameAddress from,
                                               String subject, String type, String body)
    {
        SipURL requestUri = to.getAddress();
        String callId = sipProvider.pickCallId();
        int cseq = SipProvider.pickInitialCSeq();
        String localTag = SipProvider.pickTag();
        // String branch=SipStack.pickBranch();
        Message req = createRequest(sipProvider, SipMethods.MESSAGE,
            requestUri, to, from, null, callId, cseq, localTag,
            null, null, null, null);
        if (subject != null)
        {
            req.setSubjectHeader(new SubjectHeader(subject));
        }
        req.setBody(type, body);
        return req;
    }

    /**
     * Creates a new REFER request (RFC3515)
     */
    public static Message createReferRequest(SipProvider sipProvider,
        NameAddress recipient, NameAddress from, NameAddress contact,
        NameAddress referTo/* , NameAddress referred_by */)
    {
        SipURL requestUri = recipient.getAddress();
        String callid = sipProvider.pickCallId();
        int cseq = SipProvider.pickInitialCSeq();
        String localtag = SipProvider.pickTag();
        // String branch=SipStack.pickBranch();
        Message req = createRequest(sipProvider, SipMethods.REFER,
            requestUri, recipient, from, contact, callid, cseq, localtag,
            null, null, null, null);
        req.setReferToHeader(new ReferToHeader(referTo));
        req.setReferredByHeader(new ReferredByHeader(from));
        return req;
    }

    /**
     * Creates a new REFER request (RFC3515) within a dialog
     * <p>
     * parameters: <br> - <i>referTo</i> mandatory <br> - <i>referredBy</i>
     * optional
     */
    public static Message createReferRequest(Dialog dialog,
        NameAddress referTo, NameAddress referredBy)
    {
        Message req = createRequest(dialog, SipMethods.REFER, null);
        req.setReferToHeader(new ReferToHeader(referTo));
        if (referredBy != null)
        {
            req.setReferredByHeader(new ReferredByHeader(referredBy));
        }
        else
        {
            req.setReferredByHeader(new ReferredByHeader(dialog.getLocalName()));
        }
        return req;
    }

    /**
     * Creates a new SUBSCRIBE request (RFC3265) out of any pre-existing
     * dialogs.
     */
    public static Message createSubscribeRequest(SipProvider sipProvider,
                                                 SipURL recipient, NameAddress to, NameAddress from,
                                                 NameAddress contact, String event, String id, String contentType,
                                                 String body)
    {
        Message req = createRequest(sipProvider, SipMethods.SUBSCRIBE,
            recipient, to, from, contact, null);
        req.setEventHeader(new EventHeader(event, id));
        req.setBody(contentType, body);
        return req;
    }

    /**
     * Creates a new SUBSCRIBE request (RFC3265) within a dialog (re-subscribe).
     */
    public static Message createSubscribeRequest(Dialog dialog, String event,
                                                 String id, String contentType, String body)
    {
        Message req = createRequest(dialog, SipMethods.SUBSCRIBE, null);
        req.setEventHeader(new EventHeader(event, id));
        req.setBody(contentType, body);
        return req;
    }

    public static Message createNotifyRequest(SipProvider sipProvider,
                                              NameAddress to, NameAddress from, String event, String type, String body)
    {
        SipURL requestUri = to.getAddress();
        String callId = sipProvider.pickCallId();
        int cseq = SipProvider.pickInitialCSeq();
        String localTag = SipProvider.pickTag();

        Message req = createRequest(sipProvider, SipMethods.NOTIFY,
            requestUri, to, from, null, callId, cseq, localTag,
            null, null, null, null);
        if (null != event)
        {
            req.setEventHeader(new EventHeader(event));
        }
        req.setBody(type, body);
        return req;
    }

    /**
     * Creates a new NOTIFY request (RFC3265) within a dialog
     */
    public static Message createNotifyRequest(Dialog dialog, String event,
                                              String id, String contentType, String body)
    {
        Message req = createRequest(dialog, SipMethods.NOTIFY, null);
        req.removeExpiresHeader();
        req.setEventHeader(new EventHeader(event, id));
        req.setBody(contentType, body);
        return req;
    }

    /**
     * Creates a new NOTIFY request (RFC3265) within a dialog
     */
    public static Message createNotifyRequest(Dialog dialog, String event,
                                              String id, String sipfragment)
    {
        Message req = createRequest(dialog, SipMethods.NOTIFY, null);
        req.removeExpiresHeader();
        req.setEventHeader(new EventHeader(event, id));
        req.setBody("message/sipfrag;version=2.0", sipfragment);
        return req;
    }

}